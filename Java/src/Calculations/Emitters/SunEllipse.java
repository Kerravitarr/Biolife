/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Emitters;

import Calculations.Configurations;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import GUI.AllColors;
import GUI.WorldView.Transforms;
import Utils.ClassBuilder;
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
 * Пипец какое необычное эллипсоидное солнце
 * Может быть, конечно, круглы, но ведь может и не быть.
 * Бывает как круглым излучателем, так излучать и только окружностью
 * @author Kerravitarr
 */
public class SunEllipse extends SunAbstract {
	static{
		final var builder = new ClassBuilder<SunEllipse>(){
			@Override public SunEllipse generation(JSON json, long version){return new SunEllipse(json, version);}
			@Override public JSON serialization(SunEllipse object) { return object.toJSON();}

			@Override public String serializerName() {return "Элиптическое";}
			@Override public Class printName() {return SunEllipse.class;}

		};
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,SunEllipse>("a2",0,0,0,0,null) {
			@Override public Integer get(SunEllipse who) {return who.a2;}
			@Override public void setValue(SunEllipse who, Integer value) {who.setA2(value);}
			@Override public Integer getDefault() {return Configurations.getWidth()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
		});
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,SunEllipse>("b2",0,0,0,0,null) {
			@Override public Integer get(SunEllipse who) {return who.b2;}
			@Override public void setValue(SunEllipse who, Integer value) {who.setB2(value);}
			@Override public Integer getDefault() {return Configurations.getHeight()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
		});
		final var power = new ClassBuilder.NumberConstructorParamAdapter("super.power",1,30,200,1,null);
		final var center = new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "constructor.center";}
				};
		final var name = new ClassBuilder.StringConstructorParam(){
				@Override public Object getDefault() {return "Солнце";}
				@Override public String name() { return "super.name";}
			};
		final var isLine = new ClassBuilder.BooleanConstructorParam(){
				@Override public Object getDefault() {return false;}
				@Override public String name() { return "super.isLine";}

			};
		builder.addConstructor(new ClassBuilder.Constructor<SunEllipse>(){
			{
				addParam(center);
				addParam(power);
				addParam(new ClassBuilder.NumberConstructorParamAdapter("a2",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getWidth()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("b2",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getHeight()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
				});
				addParam(isLine);
				addParam(name);
			}

			@Override
			public SunEllipse build() {
				return new SunEllipse(getParam(1,Integer.class),new Trajectory(getParam(0,Point.class)),  getParam(2,Integer.class), getParam(3,Integer.class), getParam(4,Boolean.class), getParam(5,String.class));
			}
			@Override public String name() {return "ellipse";}
		});
		builder.addConstructor(new ClassBuilder.Constructor<SunEllipse>(){
			{
				addParam(center);
				addParam(power);
				addParam(new ClassBuilder.NumberConstructorParamAdapter("d",0,0,0,0,null){
					@Override public Integer getDefault() {return Math.min(Configurations.getWidth(),Configurations.getHeight())/2;}
					@Override public Integer getSliderMaximum() {return Math.min(Configurations.getWidth(),Configurations.getHeight());}
				});
				addParam(isLine);
				addParam(name);
			}

			@Override
			public SunEllipse build() {
				return new SunEllipse(getParam(1,Integer.class),new Trajectory(getParam(0,Point.class)),  getParam(2,Integer.class), getParam(3,Boolean.class), getParam(4,String.class));
			}
			@Override public String name() {return "circle";}
		});
		SunAbstract.register(builder);
	}
	
	
	
	/**Большая ось эллипса - лежит на оси Х*/
	private int a2;
	/**Большая полуось эллипса, лежит на оси X*/
	private double a;
	/**Квадрат большой полуоси эллипса*/
	private double aa;
	/**Малая ось эллипса - лежит на оси Y*/
	private int b2;
	/**Малая полуось эллипса, лежит на оси Y*/
	private double b;
	/**Квадрат малой полуоси элипса*/
	private double bb;
	
	/**Создаёт излучающий эллипс
	 * @param p сила излучения
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param a2 большая ось эллипса. Находится на оси Х
	 * @param b2 малая ось эллипса. Находится на оси Y
	 * @param isLine если true, то солнце представляет собой только излучающую окружность
	 * @param name название солнца
	 */
	public SunEllipse(double p, Trajectory move, int a2, int b2, boolean isLine, String name) {
		super(p, move,name,isLine);
		setA2(a2);
		setB2(b2);
	}
	/**Создаёт излучающий круг
	 * @param p сила излучения
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param d диаметр круга
	 * @param isLine если true, то солнце представляет собой только излучающую окружность
	 * @param name название солнца
	 */
	public SunEllipse(double p, Trajectory move, int d, boolean isLine, String name) {
		this(p, move,d,d,isLine,name);
	}
	protected SunEllipse(JSON j, long v){
		super(j,v);
		setA2(j.get("a2"));
		setB2(j.get("b2"));
	}
	private void setA2(int a2){
		this.a2 = a2;
		a = a2 / 2d;
		aa = a * a;
		updateMatrix();
	}
	private void setB2(int b2){
		this.b2 = b2;
		b = b2 / 2d;
		bb = b * b;
		updateMatrix();
	}
	@Override
	public double calculation(Point pos) {
		if(Configurations.confoguration.DIRTY_WATER == 0d)
			return getPower();
		//Расстояние от центра до точки
		var d = pos.distance(position);
		if (a2 == b2) {
			//У нас круг!
			if (!getIsLine() && d.getHypotenuse() <= a) {
				return getPower();
			} else {
				return Math.max(0, getPower() - Configurations.confoguration.DIRTY_WATER * Math.abs(d.getHypotenuse() - a));
			}
		} else {
			//У нас эллипс. Расстояние от некой точки до эллипса...
			//Жесть это, а не матан.
			//Стырено отсюда: https://github.com/0xfaded/ellipse_demo/blob/master/ellipse.py
			if (!getIsLine() && Math.pow(d.x, 2) / (aa) + Math.pow(d.y, 2) / (bb) <= 1) {
				return getPower();
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
				return Math.max(0, getPower() - Configurations.confoguration.DIRTY_WATER * dist);
			}
		}
	}
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("a2", a2);
		j.add("b2", b2);
		return j;
	}	
	
	@Override
	public void paint(Graphics2D g, Transforms transform, int posX, int posY) {
		
		final var x0 = transform.toScrinX(posX);
		final var y0 = transform.toScrinY(posY);

		final var maxAlf = 255;
		final var colorMaxLight = AllColors.toDark(AllColors.SUN, (int)maxAlf );
		
		//Где солнышко заканчивается
		final var a0 = transform.toScrin(Math.max(a2, b2))/2;
		//Сколько энергии в солнышке
		final var p = transform.toScrin((int)Math.round(getPower() / Configurations.confoguration.DIRTY_WATER));
		//Где заканчивается свет от него
		final var s = Math.max(1, a0 + p);
		//А в процентах расстояние от 0 до границы солнца
		final var sunP = ((float)a0) / s;
		if(sunP == 0) return;
			
		float[] fractions;
		Color[] colors;
		if(getIsLine()){
			//Соотношение цветов
			fractions = new float[] {(p >= a0 ? 0f : sunP - sunP * p / a0), sunP, 1.0f };
			//Сами цвета
			colors = new Color[] { AllColors.toDark(AllColors.SUN, (int) (a0 > p ? 0 : (maxAlf - maxAlf * a0 / p))) ,colorMaxLight , AllColors.SUN_DARK};
		} else {
			//Соотношение цветов
			fractions = new float[] { 0.0f, sunP, 1.0f };
			//Сами цвета
			 colors = new Color[]{colorMaxLight, colorMaxLight, AllColors.SUN_DARK};
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
