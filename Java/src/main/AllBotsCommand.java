package main;

import main.Cell.LV_STATUS;
import main.Cell.OBJECT;
import main.Point;
import main.Point.DIRECTION;

public class AllBotsCommand {
	/**
	 *  * Ищет свободные ячейки вокруг бота.
	 * Сначала прямо, потом лево право, потом зад лево/право и наконец назад
	 * @param bot
	 * @return Найденые координаты
 	 */
	public static Point findEmptyDirection(Cell bot) {
	    for (int i = 0; i < 5; i++) {
	        Point point = fromVektorR(bot, i);
	        OBJECT obj = World.world.test(point);
	        if (obj == OBJECT.CLEAN)
	            return point;
	        if (i != 0 && i != 4) {
	            point = fromVektorR(bot, -i);
		        obj = World.world.test(point);
		        if (obj == OBJECT.CLEAN)
		            return point;
	        }
	    }
	    return null;
	}
	/**
	 * Отдаёт следующие координаты относительно текущего смотра бота
	 * @param {*} bot - кто в центре
	 * @param {*} n - на сколько повернуть голову (+ по часовой)
	 * @return 
	 * @returns Координаты следующей точки в этом направлении
	 */
	public static Point fromVektorR(Cell bot, int n) {
	    return fromVektorA(bot, DIRECTION.toNum(bot.direction) + n);
	}
	/**
	 * Отдаёт следующие координаты относительно глобальных координат
	 * @param direction
	 * @return
	 */
	static Point fromVektorA(Cell bot,int direction) {
		Point point = new Point(bot.pos);
		DIRECTION dir = DIRECTION.toEnum(direction);
	    return point.next(dir);
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
	static boolean isRelative(Cell bot0, Cell bot1) {
	    if (bot0.alive != LV_STATUS.LV_ALIVE || bot1.alive != LV_STATUS.LV_ALIVE) {
	        return false;
	    }
	    int dif = 0;    // счетчик несовпадений в фенотипе
	    dif += Math.abs(bot0.phenotype.getRed() - bot1.phenotype.getRed());
	    dif += Math.abs(bot0.phenotype.getGreen() - bot1.phenotype.getGreen());
	    dif += Math.abs(bot0.phenotype.getBlue() - bot1.phenotype.getBlue());
	    return dif < 10;
	}
}
