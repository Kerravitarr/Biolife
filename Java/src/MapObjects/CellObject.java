package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import Utils.JSONmake;
import main.Configurations;
import main.Point;
import main.World;
import main.Point.DIRECTION;

/**
 * Описывает некий объект на карте
 * @author Илья
 *
 */
public abstract class CellObject {
	/**Статус*/
	public enum LV_STATUS {LV_ALIVE,LV_ORGANIC,GHOST};
    /**Состояние объекта*/
    public LV_STATUS alive;
	/**Статус*/
	public enum OBJECT {WALL(1),CLEAN(0),ORGANIC(2),FRIEND(3),ENEMY(4), BOT(5);
		private static final OBJECT[] myEnumValues = OBJECT.values();
		/**На сколько нужно сдвинуть счётчик команда дополнительно, типо развилка*/
		int nextCMD;
		OBJECT(int nextCMD) {this.nextCMD=nextCMD;}
		public static int size() {return myEnumValues.length;}
	};
	
    /**Цвет бота зависит от того, что он делает*/
	protected Color color_DO;
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
    public CellObject(JSONmake cell) {
    	setPos(new Point(cell.getJ("pos")));
    	this.alive = LV_STATUS.values()[cell.getI("alive")];
    	stepCount = cell.getL("stepCount");
    	years = cell.getI("years");
	}

	/**
	 * Метод показывает, может ли объект ходить за этот ход
	 * @param step
	 * @return
	 */
	public final boolean canStep(long step) {
		return stepCount != step;
	}
	/**
	 * Сделать шаг
	 * @param step
	 */
	public final void step(long step) {
		stepCount = step;
		years++;

		/**
		 * Дополнительное правило карте.
		 * Слева есть восходящий поток жидкости и два нисходящих
		 */
		for(Geyser gz : Configurations.geysers)
			gz.action(this);
		
		step();
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
	public JSONmake toJSON() {
		JSONmake make = new JSONmake();
		make.add("pos", getPos().toJSON());
		make.add("alive",alive.ordinal());
		make.add("stepCount",stepCount);
		make.add("years",years);
		return toJSON(make);
	}
	public abstract JSONmake toJSON(JSONmake make);

	/**
	 * Возваращет некий аналог энергии
	 * @return
	 */
	public abstract long getHealth();
	/**
	 * Сохраняет некий аналог энергии
	 * @return
	 */
	abstract void setHealth(long h);

	/**
	 * Отдаёт следующие координаты относительно глобальных координат
	 * @param direction
	 * @return
	 */
	protected Point fromVektorA(DIRECTION direction) {
		Point point = new Point(getPos());
	    return point.next(direction);
	}
	/**
	 * Подглядывает за бота в абсолютном направлении
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	protected OBJECT seeA(DIRECTION direction) {
	    Point point = fromVektorA(direction);
	    OBJECT obj = Configurations.world.test(point);
	    if (obj != OBJECT.BOT)
	        return obj;
	    else if (isRelative(this, Configurations.world.get(point)))
	        return OBJECT.FRIEND;
	    else
	        return OBJECT.ENEMY;
	}
	/**
	 * Перемещает бота в абсолютном направлении
	 * @param direction
	 * @return
	 */
	protected boolean moveA(DIRECTION direction) {
		if(seeA(direction) == OBJECT.CLEAN){
			Point point = fromVektorA(direction);
			Configurations.world.clean(getPos());
	        setPos(point);
	        Configurations.world.add(this);
	        return true;
	    }
	    return false;
	}
	/**
	 * Перемещает бота в направлении, если не получится прямо в этом направлении - перемещает
	 * 		 в подобном направелнии. Например вниз, а затем вниз-право и вниз лево
	 * @param direction
	 * @return
	 */
	protected boolean moveD(DIRECTION direction) {
		if(moveA(direction))
			return true;
		
		if(Math.random() >= 0.5) {
			if(moveA(direction.next()))
				return true;
			if(moveA(direction.prev()))
				return true;
		} else {
			if(moveA(direction.prev()))
				return true;
			if(moveA(direction.next()))
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
	public void remove() {
		Configurations.world.clean(getPos());
		alive = LV_STATUS.GHOST;
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
		return "Cell in " + pos;
	}
}
