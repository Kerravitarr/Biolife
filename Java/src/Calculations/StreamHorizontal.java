/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import Calculations.Point.DIRECTION;
import MapObjects.CellObject;

/**Прямоугольный вертикальный поток*/
public abstract class StreamHorizontal extends StreamAbstract {

	/**Ширина потока*/
	private final int width;
	/**Высота потока*/
	private final int height;

	/**Создание квадртаного потока
	 * @param pos позиция верхнего левого угла потока на данный момент
	 * @param width ширина, в клетках мира
	 * @param height высота, в клетках мира
	 * @param power максимальная энергия потока. Не может быть 0.
	 *			Если больше 0, то каждый шаг поток будет толкать клетку на восток (вправо)
	 *			если меньше 0, то поток будет толкать влево (на запад)
	 * @param shadow тип снижения мощности от максимума к минимуму
	 * @param minP энергия на самом краешке
	 */
	public StreamHorizontal(Point pos, int width, int height, int power, StreamAttenuation shadow, int minP) {
		super(new Point(pos.getX() + width / 2, pos.getY() + height / 2), power, shadow, minP);
		this.width = width;
		this.height = height;
	}

	/**Создание квадртаного потока без убывания мощности
	 * @param pos позиция верхнего левого угла потока на данный момент
	 * @param width ширина, в клетках мира
	 * @param height высота, в клетках мира
	 * @param power максимальная энергия потока. Не может быть 0.
	 *			Если больше 0, то каждый шаг поток будет толкать клетку на восток (вправо)
	 *			если меньше 0, то поток будет толкать влево (на запад)
	 */
	public StreamHorizontal(Point pos, int width, int height, int power) {
		super(new Point(pos.getX() + width / 2, pos.getY() + height / 2), power);
		this.width = width;
		this.height = height;
	}

	@Override
	public void action(CellObject cell) {
		final var pos = cell.getPos();
		final var d = position.distance(pos);
		final double absdy = Math.abs(d.y);
		if(Math.abs(d.x) > width / 2 || absdy > height / 2) return;
		//Сила затягивания к центральной оси потка
		var F = shadow.power(minP, powerCenter, absdy / (height / 2));
		if(F > 0 && cell.getAge() % F == 0)
			cell.moveD(DIRECTION.RIGHT); // Поехали по направлению!
		else if(F < 0 && cell.getAge() % -F == 0)
			cell.moveD(DIRECTION.LEFT); // Поехали по направлению!
	}
	
}
