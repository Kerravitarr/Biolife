/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import MapObjects.CellObject;

/**Круглый поток*/
public abstract class StreamEllipse extends StreamAbstract {
	/**Большая ось эллипса - лежит на оси Х*/
	private final int a2;
	/**Большая полуось эллипса, лежит на оси X*/
	private final double a;
	/**Квадрат большой полуоси эллипса*/
	private final double aa;
	/**Малая ось эллипса - лежит на оси Y*/
	private final int b2;
	/**Малая полуось эллипса, лежит на оси Y*/
	private final double b;
	/**Квадрат малой полуоси элипса*/
	private final double bb;

	/**Создание элипсовидного потока
	 * @param pos позиция центра на данный момент
	 * @param a2 большая ось эллипса - лежит на оси Х
	 * @param b2 малая ось эллипса - лежит на оси Y
	 * @param power максимальная энергия потока. Не может быть 0.
	 *			Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
	 * @param shadow тип снижения мощности от максимума к минимуму
	 * @param minP энергия на самом краешке
	 */
	public StreamEllipse(Point pos, int a2, int b2, int power, StreamAttenuation shadow, int minP) {
		super(pos, power, shadow, minP);
		this.a2 = a2;
		this.b2 = b2;
		
		a = a2 / 2d;
		aa = a * a;
		b = b2 / 2d;
		bb = b * b;
	}

	/**Создание элипсовидного потока без изменения мощности на всём потоке
	 * @param pos позиция центра на данный момент
	 * @param a2 большая ось эллипса - лежит на оси Х
	 * @param b2 малая ось эллипса - лежит на оси Y
	 * @param power максимальная энергия потока. Не может быть 0.
	 *			Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
	 */
	public StreamEllipse(Point pos, int a2, int b2, int power) {
		this(pos, b2, power, new StreamAttenuation.NoneStreamAttenuation(), power);
	}

	/**Создание круглого потока
	 * @param pos позиция центра на данный момент
	 * @param d диаметр круга
	 * @param power максимальная энергия потока. Не может быть 0.
	 *			Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
	 * @param shadow тип снижения мощности от максимума к минимуму
	 * @param minP энергия на самом краешке
	 */
	public StreamEllipse(Point pos, int d, int power, StreamAttenuation shadow, int minP) {
		this(pos, d, d, power, shadow, minP);
	}

	/**Создание круглого потока без изменения мощности на всём потоке
	 * @param pos позиция центра на данный момент
	 * @param d диаметр круга
	 * @param power максимальная энергия потока. Не может быть 0.
	 *			Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
	 */
	public StreamEllipse(Point pos, int d, int power) {
		this(pos, d,d,  power);
	}

	@Override
	public void action(CellObject cell) {
		final var pos = cell.getPos();
		final var d = position.distance(pos);
		if(Math.pow(d.x, 2) / (aa) + Math.pow(d.y, 2) / (bb) > 1) return; //Это не к нам
		
		if (a2 == b2) {
			//У нас круг!
			final var F = shadow.power(minP, powerCenter, d.getHypotenuse() / a);
			if(Math.abs(F) % cell.getAge() == 0){
				if(F > 0)
					cell.moveD(d.direction());
				else
					cell.moveD(d.direction().inversion());
			}
		} else {
			double tx = 0.707, ty = 0.707;
			for (int i = 0; i < 3; i++) {
				final var x = a * tx;
				final var y = b * ty;

				final var ex = (aa - bb) * Math.pow(tx, 3) / a;
				final var ey = (bb - aa) * Math.pow(ty, 3) / b;

				final var rx = x - ex;
				final var ry = y - ey;

				final var qx = Math.abs(d.x) - ex;
				final var qy = Math.abs(d.y) - ey;

				final var r = Math.hypot(ry, rx);
				final var q = Math.hypot(qy, qx);

				tx = Utils.Utils.betwin(0, (qx * r / q + ex) / a, 1);
				ty = Utils.Utils.betwin(0, (qy * r / q + ey) / b, 1);

				final var t = Math.hypot(tx, ty);
				tx /= t;
				ty /= t;
			}
			final var dist = Math.hypot(d.x - Math.copySign(a * tx, d.x), d.y - Math.copySign(b * ty, d.y));
			final var F = shadow.power(minP, powerCenter, dist);
			
			if(Math.abs(F) % cell.getAge() == 0){
				if(F > 0)
					cell.moveD(d.direction());
				else
					cell.moveD(d.direction().inversion());
			}
		}
		throw new AssertionError();
	}
	/**Возвращает примерное значение арктангенса.
	 * погрешность на отрезке [0, 1] не более .0049- радиана
	 *
	 * @param vec вектор, чей арктангенс вычисляем
	 * @return значение угла этого вектора в радианах
	 */
	private static double atan2(Point.Vector vec) {
		final var ONE_HALF_PI = Math.PI / 2;
		var x = vec.x;
		var y = vec.y;
		if (y >= 0.0) {
			if (x >= 0.0) {
				if (x >= y) return atanImpl(y / x);
				else return ONE_HALF_PI - atanImpl(x / y);
			} else {
				x = -x;
				if (x < y) return ONE_HALF_PI + atanImpl(x / y);
				else return Math.PI - atanImpl(y / x);
			}
		} else {
			if (x <= 0.0) {
				if (x <= y) return -Math.PI + atanImpl(y / x);
				else return -ONE_HALF_PI - atanImpl(x / y);
			} else {
				x = -x;
				if (x > y) return -ONE_HALF_PI + atanImpl(x / y);
				else return -atanImpl(y / x);
			}
		}
	}
	/**
	 * Реализация функции arctg(x)
	 * (погрешность в районе 0.0049- радиана)
	 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6010512/
	 */
	private static double atanImpl(double tan) {
		return (8 * tan) / (3 + Math.sqrt(25 + tan*tan*80/3));
	}

}
