package MapObjects;

import java.awt.Graphics;

import MapObjects.Poison.TYPE;
import Utils.JSON;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;
import GUI.Legend;

/**
 * Описывает некий объект на карте
 * @author Илья
 *
 */
public abstract class CellObject {
	/**Статус, тип объекта.*/
	public enum LV_STATUS {
		LV_ALIVE, LV_ORGANIC, LV_POISON, LV_WALL,LV_CONNECTIVE_TISSUE, GHOST;
		/**Имя типа объекта*/
		private String name;
		/**Все возможные состояния*/
		public final static LV_STATUS[] values = LV_STATUS.values();
		/**Количество состояний*/
		public final static int length = values.length;

		LV_STATUS() {name = Configurations.getProperty(getClass(), super.name());}
		
		@Override
		public String toString() {
			return name;
		}
	};

    /**Состояние объекта*/
    protected LV_STATUS alive;
	/**Расшриенный статус объекта, показывает отношение между текущим объектом и другим*/
	public enum OBJECT {
		/**Пустая клетка*/
		CLEAN,
		/**Стена, ограничивающая мир*/
		WALL,
		/**Органика*/
		ORGANIC,
		/**Какой-то бот*/
		ALIVE,
		/**Друг или враг*/
		FRIEND(ALIVE),ENEMY(ALIVE),
		/**Родня связь*/		
		CONNECTION(ALIVE), /**Чей-то заполнитель*/ FILLING(ALIVE),
		/**Яд*/
		BANE,
		/**Яд или не яд для этой клетки*/
		POISON(BANE),NOT_POISON(BANE),
		/**Оканемевшая клетка*/
		OWALL,
		;
		/**Все возможные объекты*/
		public static final OBJECT[] values = OBJECT.values();
		/**Количество этих объектов*/
		public static int lenght = values.length;
		/**Читабельное имя объекта*/
		public final String name;
		/**Лидер группы. Например, для группы FRIEND и ENEMY лидер - ALIVE. Для всех остальных лидер - сам объект*/
		public final OBJECT groupLeader;
		
		OBJECT(){this(null);}
		OBJECT(OBJECT leader) {name = Configurations.getProperty(getClass(), super.name());groupLeader = leader != null ? leader : this;}
		@Override
		public String toString() { return name;}
		/**
		 * Преобразует объект в его проекцию
		 * @param o что за объект
		 * @return его тип
		 */
		private static OBJECT transform(LV_STATUS o){
			return switch (o) {
				case LV_ALIVE -> ALIVE;
				case LV_ORGANIC -> ORGANIC;
				case LV_POISON -> BANE;
				case LV_WALL -> WALL;
				case LV_CONNECTIVE_TISSUE -> ALIVE;
				default -> throw new AssertionError("Мы не ожидали тут встретить объект типа " + o);
			};
		}
	};
	
	public class CellObjectRemoveException extends RuntimeException {
		public CellObjectRemoveException(){super();}
		@Override
		public String getMessage(){
			return "Удалили клетку " + CellObject.this;
		}
	}
	
    /**Позиция объекта*/
	private Point pos = Point.create(0, 0);
	/**Импульс объекта. Клеток/ход*/
	private Point.PointD impuls = new Point.PointD(0, 0);
	
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
    	setPos(Point.create(cell.getJ("pos")));
    	this.alive = LV_STATUS.valueOf(cell.get("alive"));
    	stepCount = cell.getL("stepCount");
    	years = cell.getL("years");
		if(cell.containsKey("impuls")){
			impuls = new Point.PointD(cell.getJ("impuls"));
		}
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
			//Пущай походит
			step();
			//А теперь воздействия окружающей среды.
			
			//Воздействие всех потоков на объект
			for(final var gz : Configurations.streams)
				gz.push(this);
			switch (alive) {
				case LV_ALIVE -> {
					final var acp = ((AliveCellProtorype)this);
					//Воздействие источников минералов на живую клетку
					acp.addMineral((long) acp.mineralAround());
					//Всплытие/погружение
					if (acp.getBuoyancy() != 0) {
						if(acp.getBuoyancy() < 0) move(DIRECTION.DOWN, 1d/(acp.getBuoyancy() + 101d));
						else move(DIRECTION.UP, 1d/(101 - acp.getBuoyancy()));
					}
				}
				case LV_ORGANIC, LV_POISON, LV_WALL, LV_CONNECTIVE_TISSUE -> {}
				default -> throw new AssertionError("Мы не ожидали тут встретить объект типа '" + alive + "'");
			}
			//Воздействие граввитации
			final var g = Configurations.gravitation[alive.ordinal()];
			move(g.getDirection(pos), g.push());
			if(Math.abs(impuls.x) > 1 || Math.abs(impuls.y) > 1){
				final var d = impuls.direction();
				if(moveD(d)){
					addImpuls(-d.addX, -d.addY);
				}
			}
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
	 * Возвращает имупульс объекта
	 * @return
	 */
	public final Point.PointD getImpuls() {
		return impuls;
	}
	
	/**
	 * Сохраняет импульс для клетки
	 * @param impuls новое значение импульса
	 */
	protected final void setImpuls(Point.PointD impuls){
		setImpuls(impuls.x,impuls.y);
	}
	/**
	 * Сохраняет импульс для клетки
	 * @param dx значение по х
	 * @param dy значение по y
	 */
	protected final void setImpuls(double dx, double dy){
		final var nx = Math.abs(dx);
		final var ny = Math.abs(dy);
		if(!(1e-306 <= nx && nx <= 10_000_000) && nx != 0 || !(1e-306 <= ny && ny <= 10_000_000) && ny != 0)
			impuls.x = impuls.x;
		impuls.x = dx;
		impuls.y = dy;
	}
	/**
	 * Добавляет к текущему импульсу новые значения
	 * @param dx добавка по х
	 * @param dy добавка по y
	 */
	protected final void addImpuls(double dx, double dy){
		final var nx = Math.abs(impuls.x + dx);
		final var ny = Math.abs(impuls.y + dy);
		if(!(1e-306 <= nx && nx <= 10_000_000) && nx != 0 || !(1e-306 <= ny && ny <= 10_000_000) && ny != 0)
			impuls.x = impuls.x;
		impuls.x += dx;
		impuls.y += dy;
	}
	/**
	 * Серелизует объект
	 * @return
	 */
	public JSON toJSON() {
		JSON make = new JSON();
		make.add("pos", getPos().toJSON());
		make.add("alive",getAlive());
		make.add("stepCount",getStepCount());
		make.add("years",years);
		make.add("impuls",impuls.toJSON());
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
	 * 
	 * Только для живой клетки не возвращает ALIVE, а возвращает ENEMY или FRIEND
	 * Только для жиовй клетки не возвращает BANE, а возвращает POISON или NOT_POISON
	 * Только для яда возвращает не BANE, а возвращает POISON или NOT_POISON
	 * Для всех остальных ENEMY, FRIEND, POISON и NOT_POISON недоступны
	 * 
	 * 
	 * @param direction направление, DIRECTION
	 * @return параметры OBJECT
	 */
	public OBJECT see(DIRECTION direction) {
		return see(getPos().next(direction));
	}
	/**
	 * Подглядывает за бота в абсолютном направлении
	 * 
	 * Только для живой клетки не возвращает ALIVE, а возвращает ENEMY или FRIEND
	 * Только для жиовй клетки не возвращает BANE, а возвращает POISON или NOT_POISON
	 * Только для жиовй клетки не возвращает LV_CONNECTIVE_TISSUE, а возвращает CONNECTION или FILLING
	 * Только для яда возвращает не BANE, а возвращает POISON или NOT_POISON
	 * Для всех остальных ENEMY, FRIEND, POISON и NOT_POISON недоступны
	 * 
	 * 
	 * @param point в какой точке карты
	 * @return параметры OBJECT
	 */
	public OBJECT see(Point point) {
		if(!point.valid())
			return OBJECT.WALL;
		final var o = Configurations.world.get(point);
		if(o == null)
			return OBJECT.CLEAN;
		switch (o.alive) {
			case LV_WALL,LV_ORGANIC -> {return OBJECT.transform(o.alive);}
			case LV_ALIVE -> {
				if(alive == LV_STATUS.LV_ALIVE){
					if(this.isRelative(o)) return OBJECT.FRIEND;
					else return OBJECT.ENEMY;
				} else {
					return OBJECT.transform(o.alive);
				}
			}
			case LV_CONNECTIVE_TISSUE -> {
				if(alive == LV_STATUS.LV_ALIVE){
					if(this.isRelative(o)) return OBJECT.CONNECTION;
					else return OBJECT.FILLING;
				} else {
					return OBJECT.transform(o.alive);
				}
			}
			case LV_POISON ->{
				switch (alive) {
					case LV_POISON -> {
						if(this.isRelative(o)) return OBJECT.NOT_POISON;
						else return OBJECT.POISON;
					}
					case LV_ALIVE -> {
						if(((AliveCell) this).poisonType == ((Poison)o).getType()) return OBJECT.NOT_POISON;
						else return OBJECT.POISON;
					}
					default -> { return OBJECT.transform(o.alive);}
				}
			}
			default -> throw new UnsupportedOperationException("Unimplemented case: " + o.alive);
		}
	}
	/**
	 * Подглядывает за объект в абсолютном направлении
	 * ENEMY, FRIEND, POISON и NOT_POISON недоступны
	 * 
	 * @param point в какой точке карты
	 * @return параметры OBJECT
	 */
	protected static OBJECT test(Point point) {
		if(!point.valid())
			return OBJECT.WALL;
		final var o = Configurations.world.get(point);
		if(o == null) return OBJECT.CLEAN;
		else return OBJECT.transform(o.alive);
	}
	/**
	 * Толкает объект в обсолютном направлении.Это значит, что объекту будет дан импульс в каком-то одном направлении
	 * А вот сможет бот туда сходить или нет - вопрос уже куда серьёзнее
	 * @param direction направление, в котором следует двигаться
	 * @param power сила с которой клетка должна будет сдвинуться в эту сторону. В Клетках/ход
	 */
	public void move(DIRECTION direction, double power){
		if(direction == null) return;
		impuls.x += direction.addX * power;
		impuls.y += direction.addY * power;
	}
	/**
	 * Перемещает бота в абсолютном направлении
	 * @param direction направление, в котором следует двигаться
	 * @return true, если клетка сходила куда её попросили и 
	 * 			false, если движение по каким либо причинам невозможно
	 * @throws CellObjectRemoveException если объект во времядвижения того. Умер
	 */
	public boolean move(DIRECTION direction) {
		switch (see(direction).groupLeader) {
			case WALL, OWALL -> {
				return false;
			}
			case ALIVE, ORGANIC-> {
				return false;
			}
			case CLEAN -> {
				Point point = getPos().next(direction);
				Configurations.world.move(this,point);
				return true;
			}
			case BANE -> {
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
			default -> throw new UnsupportedOperationException("Unimplemented case: " + see(direction));
		}
	}
	/**
	 * Перемещает бота в направлении, если не получится прямо в этом направлении - перемещает
	 * 		 в подобном направелнии. Например вниз, а затем вниз-право и вниз лево
	 * @param direction
	 * @return true, если движение удалось
	 */
	private boolean moveD(DIRECTION direction) {
		if (move(direction))
			return true;
		final var next = direction.next();
		final var prev = direction.prev();

		if (getAge() % 2 == 0) {
			if (move(next))
				return true;
			if (move(prev))
				return true;
		} else {
			if (move(prev))
				return true;
			if (move(next))
				return true;
		}
		//Там что-то есть. Мы не смогли походить, значит передали импульс дальше
		final var o1 = see(direction);
		final var o2 = see(next);
		final var o3 = see(prev);
		final var c = ((isConditionForMove(o1) ? 1 : 0) + (isConditionForMove(o2) ? 1 : 0) + (isConditionForMove(o3) ? 1 : 0));
		if(c == 0) return false;
		//Там кто-то есть. Ему и отдадим импульс
		final var p = 1d/c;
		moveD(o1,direction,p);
		moveD(o2,next,p);
		moveD(o3,prev,p);		
		return true;
	}
	/**Проверяет, можно-ли туда отдать импульс?
	 * @param o
	 * @return 
	 */
	private boolean isConditionForMove(OBJECT o){
		return o == OBJECT.WALL || o == OBJECT.OWALL || o.groupLeader == OBJECT.ALIVE || o == OBJECT.ORGANIC;
	}
	/**Непосредственно отдаёт импульс
	 * @param o
	 * @param d
	 * @param p 
	 */
	private void moveD(OBJECT o,DIRECTION d, double p){
		switch (o) {
			case WALL, OWALL -> move(d.inversion(),p);
			case ALIVE, ORGANIC-> Configurations.world.get(getPos().next(d)).move(d,p);
		}
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
	 * @throws CellObjectRemoveException выкидывает для удаления всего и вся вертикально по всему стеку вызова
	 */
	public void destroy() throws CellObjectRemoveException{
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
		this.pos = pos;
	}
	
	@Override
	public String toString() {
		return "Cell " + Integer.toHexString(hashCode()) + " in " + pos + " type " + getAlive() + " class " + this.getClass().getSimpleName();
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
	/** * Не смог я в этот раз уйти от рисования...Очень жаль :(
 Эта функция должна отобразить объект на холсте согласно установленному режиму
	 * @param g где рисуем
	 * @param legend легенда, по которой рисуем
	 * @param cx координата ЦЕНТРА на холсте, где клетка находится
	 * @param cy координата ЦЕНТРА на холсте, где клетка находится
	 * @param r размер в пк квадрата, которым клетка окружена
	 */
	public abstract void paint(Graphics g, Legend legend, int cx, int cy, int r);
}
