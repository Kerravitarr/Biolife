package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import MapObjects.Poison.TYPE;
import Utils.JSON;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Описывает некий объект на карте
 * @author Илья
 *
 */
public abstract class CellObject {
	/**Статус*/
	public enum LV_STATUS {LV_ALIVE,LV_ORGANIC,LV_POISON,GHOST};
    /**Состояние объекта*/
    public LV_STATUS alive;
	/**Статус*/
	public enum OBJECT {
		WALL(1),
		CLEAN(0,true,false,false),
		ORGANIC(2),
		FRIEND(3,false,false,true),ENEMY(4,false,false,true),
		POISON(5,true,true,false),NOT_POISON(6,true,true,false),
		BOT(7,false,false,true);
		public static final OBJECT[] myEnumValues = OBJECT.values();
		/**На сколько нужно сдвинуть счётчик команда дополнительно, типо развилка. Это-же - номер прерывания*/
		public final int nextCMD;
		//Является это место пустым
		public final boolean isEmptyPlase;
		//Является это место ядовитым
		public final boolean isPosion;
		//Является это место ботом
		public final boolean isBot;
		OBJECT(int nextCMD) {this(nextCMD,false,false,false);}
		OBJECT(int nextCMD, boolean isEmptyPlase, boolean isPosion, boolean isBot) {
			this.nextCMD=nextCMD;
			this.isEmptyPlase=isEmptyPlase;
			this.isPosion=isPosion;
			this.isBot=isBot;
		}
		public static int size() {return myEnumValues.length;}
		public static OBJECT get(int index) {
			for (OBJECT object : myEnumValues) {
				if(object.nextCMD == index)
					return object;
			}
			return null;
		}
	};
	
	public class CellObjectRemoveException extends RuntimeException {
		CellObjectRemoveException(){
			super("Удалили клетку " + CellObject.this);
		}
	}
	
    /**Цвет бота зависит от того, что он делает*/
	public Color color_DO;
    /**Позиция органики*/
	private Point pos = new Point(0,0);
    //Счётчик, показывает ходил бот в этот ход или нет
	protected long stepCount = -1;
    /**Возраст бота*/
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
			/**
			 * Дополнительное правило карте.
			 * Слева есть восходящий поток жидкости и два нисходящих
			 */
			for(Geyser gz : Configurations.geysers)
				gz.action(this);
			step();
		}catch (CellObjectRemoveException e) {
			//Мы умерли, собственно пошли отсюда
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
	 * Рисует объект на экране
	 * @param g
	 */
	public abstract void paint(Graphics g);
	/**
	 * Проверяет, такой-ли статус о объекта
	 * @param lvAlive
	 * @return
	 */
	public boolean aliveStatus(LV_STATUS lvAlive) {
		return alive == lvAlive;
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
		make.add("alive",alive.ordinal());
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
	 */
	public void addHealth(double h) {
		setHealth(getHealth() + h);
	}

	/**
	 * Отдаёт следующие координаты относительно глобальных координат
	 * @param direction
	 * @return
	 */
	protected Point fromVektorA(DIRECTION direction) {
	    return getPos().next(direction);
	}
	/**
	 * Подглядывает за бота в абсолютном направлении
	 * @param direction направление, DIRECTION
	 * @return параметры OBJECT
	 */
	protected OBJECT seeA(DIRECTION direction) {
	    Point point = fromVektorA(direction);
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
	 * @param direction
	 * @return
	 */
	public boolean moveA(DIRECTION direction) {
		switch (seeA(direction)) {
			case FRIEND:
			case ENEMY:
			case ORGANIC:
			case WALL : return false;
			case CLEAN : {
				Point point = fromVektorA(direction);
				Configurations.world.move(this,point);
			} return true;
			case POISON:
			case NOT_POISON:{
				Point point = fromVektorA(direction);
				Poison poison = (Poison) Configurations.world.get(point);
				if(toxinDamage(poison.type, (int) poison.getHealth())) {
					poison.addHealth(Math.abs(getHealth()));
					destroy();
			        return true; // Не важно что мы вернём - мы мертвы
				} else {
					poison.remove_NE(); // Удаляем яд, который мы заменили
					Configurations.world.move(this, point);
				}
			}return true;
			default :
				throw new IllegalArgumentException("Unexpected value: " + seeA(direction));
		}
	}
	/**
	 * Перемещает бота в направлении, если не получится прямо в этом направлении - перемещает
	 * 		 в подобном направелнии. Например вниз, а затем вниз-право и вниз лево
	 * @param direction
	 * @return
	 */
	public boolean moveD(DIRECTION direction) {
		if (moveA(direction))
			return true;

		if (Configurations.rnd.nextBoolean()) {
			if (moveA(direction.next()))
				return true;
			if (moveA(direction.prev()))
				return true;
		} else {
			if (moveA(direction.prev()))
				return true;
			if (moveA(direction.next()))
				return true;
		}

		return false;
	}
	
	/**
	 * Родственные-ли боты?
	 * TODO Вообще пока смотрит только по ДНК, но возможно в будущем нужно смотреть будет не на 
	 *      генотип, а на фенотип!
	 * @param cell
	 * @param cell2
	 * @return
	 */
	/*private boolean isRelative(Cell bot0, Cell bot1) {
	    if (bot0.alive != LV_STATUS.LV_ALIVE || bot1.alive != LV_STATUS.LV_ALIVE) {
	        return false;
	    }
	    int dif = 0;    // счетчик несовпадений в геноме
	    for (int i = 0; i < mind.length; i++) {
	        if (bot0.mind[i] != bot1.mind[i]) {
	            dif = dif + 1;
	            if (dif == 2) {
	                return false;
	            } // если несовпадений в генеме больше 1
	        }     // то боты не родственики
	    }
	    return true;
	}*/
	/**
	 * Родственные-ли боты?
	 * Определеяет родственников по фенотипу, по тому как они выглядят
	 * @param cell
	 * @param cell2
	 * @return
	 */
	protected boolean isRelative(CellObject bot0, CellObject bot1) {
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
		Configurations.world.clean(getPos());
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
		}catch (CellObjectRemoveException e) {
		}
	}
	/**
	 * Экстренно перерисовывает объект
	 */
	public abstract void repaint();
	public long getAge() {
		return years;
	}
	public void setAge(long years2) {
		years = years2;
	}
	/**
	 * @param pos the pos to set
	 */
	public void setPos(Point pos) {
		this.pos.update(pos);
	}
	
	public String toString() {
		return "Cell " + Integer.toHexString(hashCode()) + " in " + pos + " type " + alive;
	}
	/**Что с нами сделал токсин. true, если он нас убьёт*/
	public abstract boolean toxinDamage(TYPE type, int damag);
	public long getStepCount() {
		return stepCount;
	}
}
