package MapObjects;

import Calculations.Stream;
import java.awt.Color;
import java.awt.Graphics;

import MapObjects.Poison.TYPE;
import Utils.JSON;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;

/**
 * Описывает некий объект на карте
 * @author Илья
 *
 */
public abstract class CellObject {
	/**Статус*/
	public enum LV_STATUS {
		LV_ALIVE, LV_ORGANIC, LV_POISON, LV_WALL, GHOST;
		/**Имя типа объекта*/
		private String name;
		/**Все возможные состояния*/
		public final static LV_STATUS[] values = LV_STATUS.values();
		/**Количество состояний*/
		public final static int length = values.length;

		LV_STATUS() {name = Configurations.getProperty(getClass(), super.name());}
		
		public String toString() {
			return name;
		}
	};

    /**Состояние объекта*/
    protected LV_STATUS alive;
	/**Статус*/
	public enum OBJECT {
		/**Крайняя стена*/
		WALL(1),
		/**Пустота*/
		CLEAN(0,true,false,false),
		/**Органика*/
		ORGANIC(2),
		/**Друг или враг*/
		FRIEND(3,false,false,true),ENEMY(4,false,false,true),
		/**Яд или не яд для этой клетки*/
		POISON(5,true,true,false),NOT_POISON(6,true,true,false),
		/**Оканемевшая клетка*/
		OWALL(7,false,false,false),
		/**Какой-то бот*/
		BOT(8,false,false,true);
		public static final OBJECT[] myEnumValues = OBJECT.values();
		/**На сколько нужно сдвинуть счётчик команда дополнительно, типо развилка. Это-же - номер прерывания*/
		public final int nextCMD;
		/**Является это место пустым*/
		public final boolean isEmptyPlase;
		/**Является это место ядовитым*/
		public final boolean isPosion;
		/**Является это место ботом*/
		public final boolean isBot;
		/**Читабельное имя объекта*/
		public final String name;
		
		OBJECT(int nextCMD) {this(nextCMD,false,false,false);}
		OBJECT(int nextCMD, boolean isEmptyPlase, boolean isPosion, boolean isBot) {
			this.nextCMD=nextCMD;
			this.isEmptyPlase=isEmptyPlase;
			this.isPosion=isPosion;
			this.isBot=isBot;
			name = Configurations.getProperty(getClass(), super.name());
		}
		public static int size() {return myEnumValues.length;}
		public static OBJECT get(int index) {
			for (OBJECT object : myEnumValues) {
				if(object.nextCMD == index)
					return object;
			}
			return null;
		}
		public String toString() {
			return name;
		}
	};
	
	public class CellObjectRemoveException extends RuntimeException {
		public CellObjectRemoveException(){super();}
		@Override
		public String getMessage(){
			return "Удалили клетку " + CellObject.this;
		}
	}
	
    /**Цвет бота зависит от того, что он делает*/
	public Color color_DO;
    /**Позиция объекта*/
	private Point pos = new Point(0,0);
    //Счётчик, показывает ходил объект в этот ход или нет
	protected long stepCount = -1;
    /**Возраст объекта*/
    private long years = 0;
	
	public CellObject(long stepCount,LV_STATUS alive){
		this.stepCount=stepCount;
		this.alive=alive;
	}
    /**
     * Загрузка
     * @param cell - JSON объект, который содержит всю информацюи о клетке
     */
    public CellObject(JSON cell) {
    	setPos(new Point(cell.getJ("pos")));
    	this.alive = LV_STATUS.values()[(int)cell.get("alive")];
    	stepCount = cell.getL("stepCount");
    	years = cell.getL("years");
	}

	/**
	 * Метод показывает, может ли объект ходить за этот ход
	 * @param step
	 * @return
	 */
	public final boolean canStep(long step) {
		return getStepCount() != step;
	}
	/**
	 * Сделать шаг
	 * @param step
	 */
	public final void step(long step) {
		stepCount = step;
		years++;

		try {
			//Воздействие всех потоков на объект
			for(final var gz : Configurations.streams)
				gz.action(this);
			//Воздействие источников минералов на живую клетку
			switch (alive) {
				case LV_ALIVE -> {
					//for(final var o : Configurations.minerals)
						//minerals.action(this);
				}
				case LV_ORGANIC, LV_POISON, LV_WALL -> {}
				default -> throw new AssertionError("Мы не ожидали тут встретить объект типа " + alive);
			}
			//Ну и пусть теперь походит
			step();
		}catch (CellObjectRemoveException e) {
			//Мы умерли и уже удалили себя с поля. Помечаем себя призраком и уходим
			if(!aliveStatus(LV_STATUS.GHOST))
				throw e;
		}
	}
	/**
	 * Сделать шаг
	 * @param step
	 */
	abstract void step();
	/**
	 * Проверяет, такой-ли статус о объекта
	 * @param lvAlive
	 * @return
	 */
	public boolean aliveStatus(LV_STATUS lvAlive) {
		return getAlive() == lvAlive;
	}
	/**
	 * Возвращает позицию объекта
	 * @return
	 */
	public final Point getPos() {
		return pos;
	}
	/**
	 * Серелизует объект
	 * @return
	 */
	public JSON toJSON() {
		JSON make = new JSON();
		make.add("pos", getPos().toJSON());
		make.add("alive",getAlive().ordinal());
		make.add("stepCount",getStepCount());
		make.add("years",years);
		return toJSON(make);
	}
	public abstract JSON toJSON(JSON make);

	/**
	 * Возваращет некий аналог энергии
	 * @return
	 */
	public abstract double getHealth();
	/**
	 * Сохраняет некий аналог энергии
	 * @return
	 */
	abstract void setHealth(double h);
	/**
	 * Добавляет энергию к существующей
	 * @param h сколько энергии прибавить. Равносильно setHealth(getHealth() + h);
	 */
	public void addHealth(double h) {
		setHealth(getHealth() + h);
	}
	/**
	 * Подглядывает за бота в абсолютном направлении
	 * @param direction направление, DIRECTION
	 * @return параметры OBJECT
	 */
	protected OBJECT see(DIRECTION direction) {
	    Point point = getPos().next(direction);
	    OBJECT obj = Configurations.world.test(point);
	    if (obj.isBot) {
	    	if (isRelative(this, Configurations.world.get(point)))
		        return OBJECT.FRIEND;
		    else
		        return OBJECT.ENEMY;
	    } else {
	        return obj;
	    }
	}
	/**
	 * Перемещает бота в абсолютном направлении
	 * @param direction направление, в котором следует двигаться
	 * @return true, если клетка сходила куда её попросили и 
	 * 			false, если движение по каким либо причинам невозможно
	 * @throws CellObjectRemoveException если объект во времядвижения того. Умер
	 */
	public boolean move(DIRECTION direction) {
		switch (see(direction)) {
			case FRIEND, ENEMY, ORGANIC, WALL, OWALL -> {
				return false;
			}
			case CLEAN -> {
				Point point = getPos().next(direction);
				Configurations.world.move(this,point);
				return true;
			}
			case POISON, NOT_POISON -> {
				Point point = getPos().next(direction);
				Poison poison = (Poison) Configurations.world.get(point);
				if(Poison.createPoison(getPos(), poison.getType(), stepCount, poison.getHealth(), poison.getStream())) {
					//Яд смог на нас как-то воздействовать. Это печально, но не всё ещё потерянно!
					poison.remove_NE(); //Яда больше нет
					var nObj = Configurations.world.get(getPos()); //Теперь этот некто занимает точку в пространстве
					Configurations.world.move(nObj, point);	//Я точно не знаю кто тут теперь... Но пускай он ходит :)
					if(nObj != this) { //И это не мы, значит мы - мертвы
						alive = LV_STATUS.GHOST;
						throw new CellObjectRemoveException();
					} else {
						return true;
					}
				} else {	//Почему-то с нами яд не взаимодействовал, так что походить мы туда не можем
					return false;
				}
			}
			case BOT -> throw new UnsupportedOperationException("Unimplemented case: " + see(direction));
		}
		throw new IllegalArgumentException("Unexpected value: " + see(direction));
	}
	/**
	 * Перемещает бота в направлении, если не получится прямо в этом направлении - перемещает
	 * 		 в подобном направелнии. Например вниз, а затем вниз-право и вниз лево
	 * @param direction
	 * @return true, если движение удалось
	 */
	public boolean moveD(DIRECTION direction) {
		if (move(direction))
			return true;

		if (getAge() % 2 == 0) {
			if (move(direction.next()))
				return true;
			if (move(direction.prev()))
				return true;
		} else {
			if (move(direction.prev()))
				return true;
			if (move(direction.next()))
				return true;
		}

		return false;
	}
	
	/**
	 * Родственные-ли боты?
	 * Определеяет родственников по фенотипу, по тому как они выглядят
	 * @param bot0 - первый бот в сравнении
	 * @param bot1 - второй бот в сравнеии
	 * @return
	 */
	public static boolean isRelative(CellObject bot0, CellObject bot1) {
	    if (!bot0.aliveStatus(bot1.alive))
	        return false;
	    else
	    	return bot0.isRelative(bot1);
	}
	/**
	 * Функция должна показать, это одинаковые объекты
	 * @param bot0
	 * @return
	 */
	abstract boolean isRelative(CellObject bot0);

	/**
	 * Убирает бота с карты и проводит все необходимые процедуры при этом
	 */
	public void destroy() {
		Configurations.world.clean(this);
		alive = LV_STATUS.GHOST;
		throw new CellObjectRemoveException();
	}
	/**
	 * Убирает бота с карты и проводит все необходимые процедуры при этом
	 * не вызывает исключение, что может быть важно, когда функция вызвается
	 * не на нас
	 */
	public void remove_NE() {
		try {
			destroy();
		}catch (CellObjectRemoveException e) {}
	}
	/**Возвращает возраст объекта
	 * @return возраст, в шагах мира
	 */
	public long getAge() {
		return years;
	}
	public void setAge(long years2) {
		years = years2;
	}
	/**
	 * @param pos the pos to set
	 */
	public final void setPos(Point pos) {
		this.pos.update(pos);
	}
	
	@Override
	public String toString() {
		return "Cell " + Integer.toHexString(hashCode()) + " in " + pos + " type " + getAlive();
	}
	/**
	 * Что с нами сделал токсин.
	 * @param type - тип токсина
	 * @param damag - его сила
	 * @return true, если он нас убьёт
	 */
	public abstract boolean toxinDamage(TYPE type, int damag);
	public long getStepCount() {
		return stepCount;
	}

	/**
	 * @return the alive
	 */
	public LV_STATUS getAlive() {
		return alive;
	}
}
