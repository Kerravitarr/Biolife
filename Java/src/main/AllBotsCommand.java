package main;

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
}
