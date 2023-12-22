/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Emitters;

import Calculations.Configurations;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import GUI.AllColors;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Polygon;
import java.awt.geom.Point2D;

/**
 *Самое обычное прямоугольное солнце.
 * Представляет собой стандартную длинную лампочку
 * от которой исходит полоска света, рассеивающаяся со временем
 * @author Kerravitarr
 */
public class SunRectangle extends SunAbstract {
	static{
		final var builder = new ClassBuilder<SunRectangle>(){
			@Override public SunRectangle generation(JSON json, long version){return new SunRectangle(json, version);}
			@Override public JSON serialization(SunRectangle object) { return object.toJSON();}

			@Override public String serializerName() {return "Горизонтальный";}
			@Override public Class printName() {return SunRectangle.class;}

		};
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,SunRectangle>("width",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getWidth()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
			@Override public Integer get(SunRectangle who) {return who.width;}
			@Override public void setValue(SunRectangle who, Integer value) {who.width = value;who.updateMatrix();}
		});
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,SunRectangle>("height",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getHeight()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
			@Override public Integer get(SunRectangle who) {return who.height;}
			@Override public void setValue(SunRectangle who, Integer value) {who.height = value;who.updateMatrix();}
		});
		builder.addConstructor(new ClassBuilder.Constructor<SunRectangle>(){
			{
				addParam(new ClassBuilder.NumberConstructorParamAdapter("super.power",1,30,100,1,null));
				addParam(new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "center";}
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
					@Override public Object getDefault() {return "Лампа";}
					@Override public String name() { return "super.name";}
					
				});
			}

			@Override
			public SunRectangle build() {
				return new SunRectangle(getParam(0,Integer.class),new Trajectory(getParam(1,Point.class)),  getParam(2,Integer.class), getParam(3,Integer.class),getParam(4,Boolean.class), getParam(5,String.class));
			}
			@Override public String name() {return "";}
		});
		SunAbstract.register(builder);
	}
	
	/**Ширина излучающей поверхности*/
	private int width;
	/**Высота излучающей поверхности*/
	private int height;
	
	/**Создаёт излучающую полоску света
	 * @param p сила излучения
	 * @param move движение ЦЕНТРА этой полоски
	 * @param width ширина полоски
	 * @param height высота полоски
	 * @param isLine если true, то солнце представляет собой только излучающую окружность
	 * @param name название солнца
	 */
	public SunRectangle(double p, Trajectory move, int width, int height, boolean isLine, String name) {
		super(p, move,name,isLine);
		this.width = width;
		this.height = height;
	}	
	protected SunRectangle(JSON j, long v){
		super(j,v);
		this.width = j.get("width");
		this.height = j.get("height");
	}
	@Override
	public double calculation(Point pos) {
		final var DIRTY_WATER = Configurations.confoguration.DIRTY_WATER;
		if(DIRTY_WATER == 0d)
			return getPower();
		//Расстояние от точки до центра нашей полосы
		final var d = pos.distance(position);
		final var absX = Math.abs(d.x);
		final var absY = Math.abs(d.y);
		if (getIsLine()) {
			if (absX <= width / 2 && absY <= height / 2) {
				//Внутри прямоугольника
				return Math.max(0, getPower() - DIRTY_WATER * Math.min(width / 2 - absX, height / 2 - absY));
			} else {
				//Снаружи прямоугольника
				return Math.max(0, getPower() - DIRTY_WATER * Math.max(absX - width / 2, absY - height / 2));
			}
		} else {
			if(absX <= width / 2 && absY <= height / 2 )
				return getPower();
			else
				return Math.max(0, getPower() - DIRTY_WATER * Math.max(absX - width / 2, absY - height / 2));
		}
	}
	
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("width", width);
		j.add("height", height);
		return j;
	}
	
	@Override
	public void paint(java.awt.Graphics2D g, GUI.WorldView.Transforms transform, int posX, int posY) {
		final var DIRTY_WATER = Configurations.confoguration.DIRTY_WATER;
		
		final var x0 = transform.toScrinX(posX - width/2);
		final var y0 = transform.toScrinY(posY - height/2);
		
		final var w = transform.toScrin(width);
		final var h = transform.toScrin(height);
		
		final var x1 = x0 + w;
		final var y1 = y0 + h;
		//Сила излучения, как далеко оно распространяется. В пк
		final var s = Math.max(1, transform.toScrin((int)Math.round(getPower() / DIRTY_WATER)));
		//Цвета
		final var colors = new Color[] { AllColors.SUN, AllColors.SUN_DARK };
		
		
		//   ________________________
		//	|\						/|
		//	| \					   / |
		//	|  \ ________________ /  |
		//	|   |\______________/|   |
		//	|   | |            | |   |
		//	|   | |____________| |   |
		//	|   |/______________\|   |
		//	|  /				  \  |
		//	| /					   \ |
		//	|/______________________\|

		final var yRL = new int[]{y0 - s, y0, y1, y1 + s};
		final var xUD = new int[]{x0 - s, x1 + s, x1, x0};
		
		//Рисуем внешнее излучение
		//<
		g.setPaint(new GradientPaint(
			new Point2D.Double(x0, y0), colors[0],
			new Point2D.Double(x0 - s, y0), colors[1]));
		g.fill(new Polygon(new int[]{x0 - s, x0, x0, x0 - s}, yRL, 4));
		//>
		g.setPaint(new GradientPaint(
			new Point2D.Double(x1, y0), colors[0],
			new Point2D.Double(x1 + s, y0), colors[1]));
		g.fill(new Polygon(new int[]{x1 + s, x1, x1, x1 + s}, yRL, 4));
		//‾
		g.setPaint(new GradientPaint(
			new Point2D.Double(x0, y0), colors[0],
			new Point2D.Double(x0, y0 - s), colors[1]));
		g.fill(new Polygon(xUD, new int[]{y0 - s, y0 - s, y0, y0}, 4));
		//_
		g.setPaint(new GradientPaint(
			new Point2D.Double(x0, y1), colors[0],
			new Point2D.Double(x0, y1 + s), colors[1]));
		g.fill(new Polygon(xUD, new int[]{y1 + s, y1 + s, y1, y1}, 4));
		
		//Рисуем внутренне излучение
		if(getIsLine()){
			final var ws = Math.min(s, h/2);
			final var ylRL = new int[]{y0 - ws, y0, y1, y1 + ws};
			final var hs = Math.min(s, w/2);
			final var xlUD = new int[]{x0,x1,x1-hs, x0+hs};
			//<
			g.setPaint(new GradientPaint(
				new Point2D.Double(x0, y0), colors[0],
				new Point2D.Double(x0 + s, y0), colors[1]));
			g.fill(new Polygon(new int[]{x0, x0 + ws, x0 + ws, x0}, ylRL, 4));	
			//>
			g.setPaint(new GradientPaint(
				new Point2D.Double(x1, y0), colors[0],
				new Point2D.Double(x1 - s, y0), colors[1]));
			g.fill(new Polygon(new int[]{x1, x1 - ws, x1 - ws, x1}, ylRL, 4));	
			//‾
			g.setPaint(new GradientPaint(
				new Point2D.Double(x0, y0), colors[0],
				new Point2D.Double(x0, y0 + s), colors[1]));
			g.fill(new Polygon(xlUD, new int[]{y0, y0, y0 + hs, y0 + hs}, 4));
			//_
			g.setPaint(new GradientPaint(
				new Point2D.Double(x0, y1), colors[0],
				new Point2D.Double(x0, y1 - s), colors[1]));
			g.fill(new Polygon(xlUD, new int[]{y1, y1, y1 - hs, y1 - hs}, 4));
			
			
		} else {
			g.setColor(colors[0]);
			g.fillRect(x0, y0,w, h);
		}
	}
}
