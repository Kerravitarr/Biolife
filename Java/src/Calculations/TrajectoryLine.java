/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

/**
 * Линейная траектория.
 * Хоть она и двигается по "линии", на самом деле движение описывается урвенением
 * y = kx + b и осуществляется в некотором интервале.
 * Как только интервал превышен, движение начинается со стартвой позиции
 * @author Kerravitarr
 */
public class TrajectoryLine extends Trajectory{
	/**Длина отрезка по которому двигаемся. В клетках поля*/
	private final double lenght;
	/**Шаг объекта. Определяющее на каком расстоянии от from находится точка*/
	private double step;
	/**Откуда двигаемся*/
	private final Point from;
	/**На сколько должны сдвинуться на каждый шаг*/
	private final double dx;
	/**На сколько должны сдвинуться на каждый шаг*/
	private final double dy;
	
	/**Создаёт линейную траекторию от точки from к точке to.
	 * pos - место в пространстве, где точка находится сейчас
	 * объект смещается каждый раз на 1 клетку мира
	 * @param speed скорость, в тиков на шаг
	 * @param from откуда двигаемся
	 * @param pos где сейчас находимся
	 * @param to куда двигаемся
	 */
	public TrajectoryLine(long speed, Point from, Point pos, Point to){
		super(speed, pos);
		this.from = from;
		step = Math.sqrt(Math.pow(pos.getX() - from.getX(), 2) + Math.pow(pos.getY() - from.getY(), 2));
		lenght = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getY() - from.getY(), 2));
		dx = (to.getX() - from.getX()) / lenght;
		dy = (to.getY() - from.getY()) / lenght;
	}
	/**Создаёт линейную траекторию от точки from к точке to с точки from
	 * объект смещается каждый раз на 1 клетку мира
	 * @param speed скорость, в тиков на шаг
	 * @param from откуда двигаемся
	 * @param to куда двигаемся
	 */
	public TrajectoryLine(long speed, Point from,Point to){
		this(speed, from, from, to);
	}

	@Override
	protected Point step() {
		step++;
		if(step > lenght)
			step -= lenght;
		return new Point((int)(from.getX() + dx * step), (int) (from.getY() + dy*step));
	}
}
