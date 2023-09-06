package Calculations;

import GUI.AllColors;
import GUI.WorldView.Transforms;
import java.awt.Graphics2D;

/**
 * Болванка минералов.
 * И да... Полная копипаста. Минералы отличаются не внешним видом!
 * @author Илья
 *
 */
public abstract class MineralAbstract extends DefaultEmitter{
	/**Коээфициент "затухания" для минералов. Каждая минеральная нычка может иметь своё затухание*/
	protected double attenuation;

	/**Создаёт источник минералов
	 * @param p максимальная энергия солнца, будто и не было тени
	 * @param a затухание. На сколько единиц/клетку уменьшается количество минералов вдали от объекта
	 * @param move форма движения солнца. Доступны изначально LineMove и EllipseMove
	 * @param name название залежи
	 */
	public MineralAbstract(double p,double a, Trajectory move, String name){
		super(p,move,name);
		attenuation = a;
	}
	/**
	 * Возвращает концентрацию минералов в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии. Может быть отрицательным числом - это не поглащение минеравло, а удалённость от источника
	 */
	public abstract double getConcentration(Point pos);
}
