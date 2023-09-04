package Calculations;

/**
 * Болванка солнышка. Любое солнце должно быть похоже на это!
 * @author Илья
 *
 */
public abstract class SunAbstract {
	/**Траектория движения солнца*/
	private final Trajectory move;
	/**Позиция солнца. Это условная позиция, наследник может с ней делать что угодно*/
	protected Point position;
	/**Наибольшая энергия солнца*/
	protected double power;

	/**Создаёт солнце
	 * @param p максимальная энергия солнца, будто и не было тени
	 * @param move форма движения солнца. Доступны изначально LineMove и EllipseMove
	 */
	public SunAbstract(double p, Trajectory move){
		this.move = move;
		position = move.start();
		power = p;
	}
	/**
	 * Возвращает количество солнечной энергии в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии. Может быть отрицательным числом - это не поглащение света, а удалённость от солнца
	 */
	public abstract double getEnergy(Point pos);
	/**Этот метод будет вызываться каждый раз, когда изменится местоположение объекта*/
	protected abstract void move();

	/**Шаг мира для пересчёт
	 * @param step номер шага мира
	 */
	public void step(long step) {
		if(move != null && move.isStep(step)){
			position = move.step();
			move();
		}
	}
}
