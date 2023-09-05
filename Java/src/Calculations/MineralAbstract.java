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
public abstract class MineralAbstract extends SunAbstract{

	/**Создаёт источник минералов
	 * @param p максимальная энергия солнца, будто и не было тени
	 * @param move форма движения солнца. Доступны изначально LineMove и EllipseMove
	 */
	public MineralAbstract(double p, Trajectory move){
		super(p,move);
	}
	/**
	 * Возвращает концентрацию минералов в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии. Может быть отрицательным числом - это не поглащение минеравло, а удалённость от источника
	 */
	@Override
	public abstract double getPoint(Point pos);
	/**Этот метод будет вызываться каждый раз, когда изменится местоположение объекта*/
	@Override
	protected abstract void move();
	/**
	 * Функция непосредственного рисования залежей в указанных координатах.
	 * Объект должн отрисовать себя так, будто он находится где ей сказанно
	 * @param g холст, на котором надо начертить объект
	 * @param transform преобразователь размеров мировых в размеры экранные
	 * @param posX текущаяя координата
	 * @param posY текущаяя координата
	 */
	@Override
	protected abstract void paint(Graphics2D g, Transforms transform, int posX, int posY);
}
