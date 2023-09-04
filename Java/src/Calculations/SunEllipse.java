/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import java.awt.Graphics;

/**
 * Пипец какое необычное эллипсоидное солнце
 * Может быть, конечно, круглы, но ведь может и не быть.
 * Бывает как круглым излучателем, так излучать и только окружностью
 * @author Kerravitarr
 */
public class SunEllipse extends SunAbstract {
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
	/**Если тут true, то у нас не круг, а окружность*/
	private final boolean isLine;
	
	/**Создаёт излучающий эллипс
	 * @param p сила излучения
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param a2 большая ось эллипса. Находится на оси Х
	 * @param b2 малая ось эллипса. Находится на оси Y
	 * @param isLine если true, то солнце представляет собой только излучающую окружность
	 */
	public SunEllipse(double p, Trajectory move, int a2, int b2, boolean isLine) {
		super(p, move);
		this.a2 = a2;
		this.b2 = b2;
		this.isLine = isLine;
		
		a = a2 / 2d;
		aa = a * a;
		b = b2 / 2d;
		bb = b * b;
	}
	/**Создаёт излучающий круг
	 * @param p сила излучения
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param d диаметр круга
	 * @param isLine если true, то солнце представляет собой только излучающую окружность
	 */
	public SunEllipse(double p, Trajectory move, int d, boolean isLine) {
		this(p, move,d,d,isLine);
	}

	@Override
	public double getEnergy(Point pos) {
		//Расстояние от центра до точки
		var d = pos.distance(position);
		if(isLine){
			if(a2 == b2){
				//У нас круг!
				return power - Configurations.DIRTY_WATER * Math.abs(d.getHypotenuse() - a);
			} else {
				//У нас эллипс. Расстояние от некой точки до эллипса...
				//Жесть это, а не матан.
				//Стырено отсюда: https://github.com/0xfaded/ellipse_demo/blob/master/ellipse.py
				var t = Math.PI / 4;
				double x = 0,y = 0;
				for (int i = 0; i < 3; i++) {
					final var ct = Math.cos(t);
					final var st = Math.sin(t);
					x = a * ct;
					y = b * st;
					
					final var ex = (aa - bb) * Math.pow(ct, 3) / a;
					final var ey = (bb-aa) * Math.pow(st, 3) / b;
					
					final var rx = x - ex;
					final var ry = y - ey;
					
					final var qx = d.x - ex;
					final var qy = d.y - ey;
					
					final var r = Math.hypot(ry, rx);
					final var q = Math.hypot(qy, qx);
					
					final var delta_c = r * Math.asin((rx*qy - ry*qx)/(r*q));
					final var delta_t = delta_c / Math.sqrt(a*a + b*b - x*x - y*y);
					t += delta_t;
					t = Math.min(Math.PI / 2, Math.max(0, t));
				}
				return power - Configurations.DIRTY_WATER * (Math.sqrt(x * x + y * y));
			}
		} else {
			if(a2 == b2){
				//У нас круг!
				if(d.getHypotenuse() <= a)
					return power;
				else
					return power - Configurations.DIRTY_WATER * (d.getHypotenuse() - a);
			} else {
				if(Math.pow(d.x, 2) / (aa) + Math.pow(d.y, 2) / (bb) <= 1){
					return power;
				} else {
					var t = Math.PI / 4;
					double x = 0,y = 0;
					for (int i = 0; i < 3; i++) {
						final var ct = Math.cos(t);
						final var st = Math.sin(t);
						x = a * ct;
						y = b * st;

						final var ex = (aa - bb) * Math.pow(ct, 3) / a;
						final var ey = (bb-aa) * Math.pow(st, 3) / b;

						final var rx = x - ex;
						final var ry = y - ey;

						final var qx = d.x - ex;
						final var qy = d.y - ey;

						final var r = Math.hypot(ry, rx);
						final var q = Math.hypot(qy, qx);

						final var delta_c = r * Math.asin((rx*qy - ry*qx)/(r*q));
						final var delta_t = delta_c / Math.sqrt(a*a + b*b - x*x - y*y);
						t += delta_t;
						t = Math.min(Math.PI / 2, Math.max(0, t));
					}
					return power - Configurations.DIRTY_WATER * (Math.sqrt(x * x + y * y));
				}
			}
		}
	}

	@Override
	protected void move() {}

	
	@Override
	public void paint(Graphics g) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}
	
}
