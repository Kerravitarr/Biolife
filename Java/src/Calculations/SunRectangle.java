/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import GUI.AllColors;
import GUI.WorldView.Transforms;
import Utils.JSON;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 *Самое обычное прямоугольное солнце.
 * Представляет собой стандартную длинную лампочку
 * от которой исходит полоска света, рассеивающаяся со временем
 * @author Kerravitarr
 */
public class SunRectangle extends SunAbstract {
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
	protected SunRectangle(JSON j, long v) throws GenerateClassException{
		super(j,v);
		this.width = j.get("width");
		this.height = j.get("height");
	}
	@Override
	public double getEnergy(Point pos) {
		final var DIRTY_WATER = Configurations.confoguration.DIRTY_WATER;
		if(DIRTY_WATER == 0d)
			return power;
		//Расстояние от точки до центра нашей полосы
		final var d = pos.distance(position);
		final var absX = Math.abs(d.x);
		final var absY = Math.abs(d.y);
		if (isLine) {
			if (absX <= width / 2 && absY <= height / 2) {
				//Внутри прямоугольника
				return Math.max(0, power - DIRTY_WATER * Math.min(width / 2 - absX, height / 2 - absY));
			} else {
				//Снаружи прямоугольника
				return Math.max(0, power - DIRTY_WATER * Math.max(absX - width / 2, absY - height / 2));
			}
		} else {
			if(absX <= width / 2 && absY <= height / 2 )
				return power;
			else
				return Math.max(0, power - DIRTY_WATER * Math.max(absX - width / 2, absY - height / 2));
		}
	}

	@Override
	public List<ParamObject> getParams(){
		final java.util.ArrayList<Calculations.ParamObject> ret = new ArrayList<ParamObject>(2);
		ret.add(new ParamObject("width", 1,Configurations.getWidth(),1,null){
			@Override
			public void setValue(Object value) throws ClassCastException {
				width = ((Number) value).intValue();
			}
			@Override
			protected Object get() {
				return width;
			}
		});
		ret.add(new ParamObject("height", 1,Configurations.getHeight(),1,null){
			@Override
			public void setValue(Object value) throws ClassCastException {
				height = ((Number) value).intValue();
			}
			@Override
			protected Object get() {
				return height;
			}
		});
		return ret;
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
	public void paint(Graphics2D g, Transforms transform, int posX, int posY) {
		final var DIRTY_WATER = Configurations.confoguration.DIRTY_WATER;
		if(DIRTY_WATER == 0d){
			if(posX == position.getX() && posY == position.getY()){
				//Если у нас чистая вода, то солнце осветит собой всё, что можно
				g.setColor(AllColors.SUN);				
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
		final var s = Math.max(1, transform.toScrin((int)Math.round(power / DIRTY_WATER)));
		//Соотношение цветов
		final var fractions = new float[] { 0.0f, 1.0f };
		//Сами цвета
		final var colors = new Color[] { AllColors.toDark(AllColors.SUN, (int) (64 + 192 * power / Configurations.getMaxSunPower())), AllColors.SUN_DARK };
		//final var colors = new Color[] { AllColors.SUN,AllColors.SUN};

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
