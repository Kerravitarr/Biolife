package Calculations;

import Calculations.Point.DIRECTION;
import MapObjects.CellObject;
import Utils.SaveAndLoad;

/**
 * Поток жидкости
 *
 */
public abstract class StreamAbstract implements SaveAndLoad.Serialization{

	/**Позиция центра потока*/
	protected Point position;
	/**Наибольшая энергия потока.
	 *	если тут 1, то клетку толкает каждый шаг. Если 2, то каждые 2 шага и т.д.
	 *  если тут -1, то клетку будет выталкивать.
	 * 
	 */
	protected int powerCenter;
	/**способ уменьшения мощности потока от расстояния*/
	protected StreamAttenuation shadow;
	/**Энергия на самом краешке*/
	protected final int minP;
	/**Разница между максимальной и минимальной энергией*/
	protected final int dP;
	
	/**Создание гейзера
	 * @param pos позиция центра потока при создании
	 * @param p максимальная энергия потока
	 * @param s способ рассеения мощности от расстояния.
	 * @param minP энергия на самом краешке
	 */
	protected StreamAbstract(Point pos, int p, StreamAttenuation s, int minP) {
		if(p == 0) throw new IllegalArgumentException("Мощность потока не может быть равна 0!");
		else if(p > 0 && minP < 0 || p < 0 && minP > 0 ) throw new IllegalArgumentException("Мощность потока не может менять направление от центра к краям!");
		position = pos;
		powerCenter = p;
		shadow = s;
		this.minP = minP;
		dP = powerCenter - minP;
	}
	/**Создание универсальной формы без снижения мощности потока
	 * @param pos позиция центра потока при создании
	 * @param p максимальная энергия потока
	 */
	protected StreamAbstract(Point pos, int p) {
		this(pos, p, new StreamAttenuation.NoneStreamAttenuation(), p);
	}

	/**Обрабатывает сдувание клетки в определённую сторону потоком
	 * @param cell клетка, на которую поток воздействует
	 */
	public abstract void action(CellObject cell);
	
	
}