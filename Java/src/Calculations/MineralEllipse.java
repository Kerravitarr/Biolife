/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import GUI.AllColors;
import GUI.WorldView.Transforms;
import Utils.JSON;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Эллипсоидная залежа минералов
 * @author Kerravitarr
 */
public class MineralEllipse extends MineralAbstract {
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
	 * @param attenuation затухание. На сколько единиц/клетку уменьшается количество минералов вдали от объекта
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param a2 большая ось эллипса. Находится на оси Х
	 * @param b2 малая ось эллипса. Находится на оси Y
	 * @param isLine если true, то объект представляет собой только излучающую окружность
	 * @param name название залежи
	 */
	public MineralEllipse(double p,double attenuation, Trajectory move, int a2, int b2, boolean isLine, String name) {
		super(p,attenuation, move,name);
		this.a2 = a2;
		this.b2 = b2;
		this.isLine = isLine;
		
		a = a2 / 2d;
		aa = a * a;
		b = b2 / 2d;
		bb = b * b;
	}
	protected MineralEllipse(JSON j, long v) throws GenerateClassException{
		super(j,v);
		this.a2 = j.get("a2");
		this.b2 = j.get("b2");
		this.isLine = j.get("isLine");
		
		a = a2 / 2d;
		aa = a * a;
		b = b2 / 2d;
		bb = b * b;
	}
	/**Создаёт излучающий круг
	 * @param p сила излучения
	 * @param attenuation затухание. На сколько единиц/клетку уменьшается количество минералов вдали от объекта
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param d диаметр круга
	 * @param isLine если true, то объект представляет собой только излучающую окружность
	 * @param name название солнца
	 * @param name название залежи
	 */
	public MineralEllipse(double p,double attenuation, Trajectory move, int d, boolean isLine, String name) {
		this(p,attenuation, move,d,d,isLine,name);
	}

	@Override
	public double getConcentration(Point pos) {
		//Расстояние от центра до точки
		var d = pos.distance(position);
		if (a2 == b2) {
			//У нас круг!
			if (!isLine && d.getHypotenuse() <= a) {
				return power;
			} else {
				return Math.max(0, power - attenuation * Math.abs(d.getHypotenuse() - a));
			}
		} else {
			//У нас эллипс. Расстояние от некой точки до эллипса...
			//Жесть это, а не матан.
			//Стырено отсюда: https://github.com/0xfaded/ellipse_demo/blob/master/ellipse.py
			if (!isLine && Math.pow(d.x, 2) / (aa) + Math.pow(d.y, 2) / (bb) <= 1) {
				return power;
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
				return Math.max(0, power - attenuation * dist);
			}
		}
	}
	@Override
	protected void move() {}
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("a2", a2);
		j.add("b2", b2);
		j.add("isLine", isLine);
		return j;
	}

	
	@Override
	public void paint(Graphics2D g, Transforms transform, int posX, int posY) {
		final var x0 = transform.toScrinX(posX);
		final var y0 = transform.toScrinY(posY);

		final var maxAlf = (255 * power / Configurations.getMaxConcentrationMinerals());
		final var colorMaxLight = AllColors.toDark(AllColors.MINERALS, (int)maxAlf );
		
		//Где солнышко заканчивается
		final var a0 = transform.toScrin(Math.max(a2, b2))/2;
		//Сколько энергии в солнышке
		final var p = transform.toScrin((int)Math.round(power / attenuation));
		//Где заканчивается свет от него
		final var s = Math.max(1, a0 + p);
		//А в процентах расстояние от 0 до границы солнца
		final var sunP = ((float)a0) / s;
		if(sunP == 0) return;
			
		float[] fractions;
		Color[] colors;
		if(isLine){
			//Соотношение цветов
			fractions = new float[] {(p >= a0 ? 0f : sunP * p / a0), sunP, 1.0f };
			//Сами цвета
			colors = new Color[] { AllColors.toDark(AllColors.MINERALS, (int) (a0 > p ? 0 : (maxAlf - maxAlf * a0 / p))) ,colorMaxLight , AllColors.MINERALS_DARK};
		} else {
			//Соотношение цветов
			fractions = new float[] { 0.0f, sunP, 1.0f };
			//Сами цвета
			 colors = new Color[]{colorMaxLight, colorMaxLight, AllColors.MINERALS_DARK};
		}
			
		if(a2 == b2){
			//Круглое солнышко - это збс
			g.setPaint(new RadialGradientPaint(
					new Point2D.Double(x0, y0), s,fractions, colors,CycleMethod.NO_CYCLE));
			g.fill(new Ellipse2D.Double(x0 - s, y0 - s, s*2,s*2));
		} else {
			//А эллипс надо сначала деформировать
			if(a > b) {
				final var at = AffineTransform.getScaleInstance(1, b / a);
				final var center = new Point2D.Double(x0, y0 * a / b);
				g.setPaint(new RadialGradientPaint(center, s, center, fractions, colors, CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, at));
				g.fill(new Ellipse2D.Double(x0 - s, y0 - s, s * 2, s * 2));
			} else {
				final var at = AffineTransform.getScaleInstance(a / b, 1);
				final var center = new Point2D.Double(x0 * b / a, y0);
				g.setPaint(new RadialGradientPaint(center, s, center, fractions, colors, CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, at));
				g.fill(new Ellipse2D.Double(x0 - s, y0 - s, s*2,s*2));
			}
		}
		
	}
	
}
