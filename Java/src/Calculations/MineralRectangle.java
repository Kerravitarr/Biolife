/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import GUI.AllColors;
import GUI.WorldView.Transforms;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *Самое обычное прямоугольное залежо минералов.
 * Опять ребрендинк квадртаного солнца
 * @author Kerravitarr
 */
public class MineralRectangle extends MineralAbstract {
	static{
		final var builder = new ClassBuilder<MineralRectangle>(){
			@Override public MineralRectangle generation(JSON json, long version){return new MineralRectangle(json, version);}
			@Override public JSON serialization(MineralRectangle object) { return object.toJSON();}

			@Override public String serializerName() {return "Куб";}
			@Override public Class printName() {return MineralRectangle.class;}

		};
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,MineralRectangle>("width",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getWidth()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
			@Override public Integer get(MineralRectangle who) {return who.width;}
			@Override public void setValue(MineralRectangle who, Integer value) {who.width = value;}
		});
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,MineralRectangle>("height",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getHeight()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
			@Override public Integer get(MineralRectangle who) {return who.height;}
			@Override public void setValue(MineralRectangle who, Integer value) {who.height = value;}
		});
		final var attenuation = new ClassBuilder.NumberConstructorParamAdapter("super.attenuation",0d,0d,30d,0d,null){
			@Override public Double getDefault() {return Configurations.confoguration.DIRTY_WATER;}
		};
		builder.addConstructor(new ClassBuilder.Constructor<MineralRectangle>(){
			{
				addParam(new ClassBuilder.NumberConstructorParamAdapter("super.power",1,30,100,1,null));
				addParam(attenuation);
				addParam(new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "super.center";}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("width",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getWidth()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("height",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getHeight()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
				});
				addParam(new ClassBuilder.BooleanConstructorParam(){
					@Override public Object getDefault() {return false;}
					@Override public String name() { return "super.isLine";}
				});
				addParam(new ClassBuilder.StringConstructorParam(){
					@Override public Object getDefault() {return "Кубик";}
					@Override public String name() { return "super.name";}
					
				});
			}

			@Override
			public MineralRectangle build() {
				return new MineralRectangle(getParam(0,Integer.class),getParam(1,Double.class),new Trajectory(getParam(2,Point.class)),  getParam(3,Integer.class), getParam(4,Integer.class),getParam(5,Boolean.class), getParam(6,String.class));
			}
			@Override public String name() {return "";}
		});
		MineralAbstract.register(builder);
	}
	
	/**Ширина излучающей поверхности*/
	private int width;
	/**Высота излучающей поверхности*/
	private int height;
	
	/**Создаёт излучающу. прямоугольниую поверхность
	 * @param p сила излучения
	 * @param attenuation затухание. На сколько единиц/клетку уменьшается количество минералов вдали от объекта
	 * @param move движение ЦЕНТРА объекта
	 * @param width ширина полоски
	 * @param height высота полоски
	 * @param isLine если true, то объекта представляет собой только излучающую поверхность
	 * @param name название залежи
	 */
	public MineralRectangle(double p, double attenuation, Trajectory move, int width, int height, boolean isLine, String name) {
		super(p, attenuation,move,name,isLine);
		this.width = width;
		this.height = height;
	}
	protected MineralRectangle(JSON j, long v){
		super(j,v);
		this.width = j.get("width");
		this.height = j.get("height");
	}

	@Override
	public double getConcentration(Point pos) {
		if(getAttenuation() == 0d)
			return power;
		//Расстояние от точки до центра нашей полосы
		final var d = pos.distance(position);
		final var absX = Math.abs(d.x);
		final var absY = Math.abs(d.y);
		if (isLine) {
			if (absX <= width / 2 && absY <= height / 2) {
				//Внутри прямоугольника
				return Math.max(0, power - getAttenuation() * Math.min(width / 2 - absX, height / 2 - absY));
			} else {
				//Снаружи прямоугольника
				return Math.max(0, power - getAttenuation() * Math.max(absX - width / 2, absY - height / 2));
			}
		} else {
			if(absX <= width / 2 && absY <= height / 2 )
				return power;
			else
				return Math.max(0, power - getAttenuation() * Math.max(absX - width / 2, absY - height / 2));
		}
	}

	@Override
	protected void move() {}
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("width", width);
		j.add("height", height);
		return j;
	}
	@Override
	public void paint(Graphics2D g, Transforms transform, int posX, int posY, boolean isSelect) {
		if(getAttenuation() == 0d){
			if(posX == position.getX() && posY == position.getY()){
				g.setColor(isSelect ? AllColors.MINERALS_DARK : AllColors.MINERALS);				
				g.fillRect(transform.toScrinX(0), transform.toScrinY(0),transform.toScrin(Configurations.getWidth()), transform.toScrin(Configurations.getHeight()));
			}
			return;
		}
		
		final var x0 = transform.toScrinX(posX - width/2);
		final var y0 = transform.toScrinY(posY - height/2);
		
		final var w = transform.toScrin(width);
		final var h = transform.toScrin(height);
		
		final var x1 = x0 + w;
		final var y1 = y0 + h;
		//Сила излучения, как далеко оно распространяется
		final var s = Math.max(1, transform.toScrin((int)Math.round(power / getAttenuation())));
		//Соотношение цветов
		final var fractions = new float[] { 0.0f, 1.0f };
		//Сами цвета
		final var colors = new Color[] { AllColors.toDark(AllColors.MINERALS, (int) getColorAlfa(isSelect)), AllColors.MINERALS_DARK };

		//Рисуем внешнее излучение
		// Left
		g.setPaint(new GradientPaint(
			new Point2D.Double(x0, y0), colors[0],
			new Point2D.Double(x0 - s, y0), colors[1]));
		g.fill(new Rectangle2D.Double(x0 - s, y0, s, h));
		// Right
		g.setPaint(new GradientPaint(
			new Point2D.Double(x1, y0), colors[0],
			new Point2D.Double(x1 + s, y0), colors[1]));
		g.fill(new Rectangle2D.Double(x1, y0, s, h));

		// Top
		g.setPaint(new GradientPaint(
			new Point2D.Double(x0, y0), colors[0],
			new Point2D.Double(x0, y0 - s), colors[1]));
		g.fill(new Rectangle2D.Double(x0, y0 - s, w, s));

		// Bottom
		g.setPaint(new GradientPaint(
			new Point2D.Double(x0, y1), colors[0],
			new Point2D.Double(x0, y1 + s), colors[1]));
		g.fill(new Rectangle2D.Double(x0, y1, w, s));

		// Top Left
		g.setPaint(new RadialGradientPaint(
			new Rectangle2D.Double(x0 - s, y0 - s, s + s, s + s), 
			fractions, colors, CycleMethod.NO_CYCLE));
		g.fill(new Rectangle2D.Double(x0 - s, y0 - s, s, s));

		// Top Right
		g.setPaint(new RadialGradientPaint(
			new Rectangle2D.Double(x1 - s, y0 - s, s + s, s + s), 
			fractions, colors, CycleMethod.NO_CYCLE));
		g.fill(new Rectangle2D.Double(x1, y0 - s, s, s));

		// Bottom Left
		g.setPaint(new RadialGradientPaint(
			new Rectangle2D.Double(x0 - s, y1 - s, s + s, s + s), 
			fractions, colors, CycleMethod.NO_CYCLE));
		g.fill(new Rectangle2D.Double(x0 - s, y1, s, s));

		// Bottom Right
		g.setPaint(new RadialGradientPaint(
			new Rectangle2D.Double(x1 - s, y1 - s, s + s, s + s), 
			fractions, colors, CycleMethod.NO_CYCLE));
		g.fill(new Rectangle2D.Double(x1, y1, s, s));

		//Рисуем внутренне излучение
		if(isLine){
			// Left
			g.setPaint(new GradientPaint(
				new Point2D.Double(x0, y0), colors[0],
				new Point2D.Double(x0 + s, y0), colors[1]));
			g.fill(new Rectangle2D.Double(x0, y0, Math.min(s, w), h));
			// Right
			g.setPaint(new GradientPaint(
				new Point2D.Double(x1, y0), colors[0],
				new Point2D.Double(x1 - s, y0), colors[1]));
			g.fill(new Rectangle2D.Double(x1 - Math.min(s, w), y0, Math.min(s, w), h));

			// Top
			g.setPaint(new GradientPaint(
				new Point2D.Double(x0, y0), colors[0],
				new Point2D.Double(x0, y0 + s), colors[1]));
			g.fill(new Rectangle2D.Double(x0, y0, w, Math.min(s, h)));
			// Bottom
			g.setPaint(new GradientPaint(
				new Point2D.Double(x0, y1), colors[0],
				new Point2D.Double(x0, y1 - s), colors[1]));
			g.fill(new Rectangle2D.Double(x0, y1 - Math.min(s, h), w, Math.min(s, h)));					
		} else {
			g.setColor(colors[0]);
			g.fillRect(x0, y0,w, h);
		}
	}
}
