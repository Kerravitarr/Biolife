/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ColorRec;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Анимация для реки.
 * Река имеет два берега - правый и левый, а с остальных частей сшита
 * 
 * @author Kerravitarr
 */
public class River extends DefaultAnimation{
	/**Волна. Её один элемент*/
	private static class Wave{
		/**Точка волны*/
		public static class Point{
			/**Текущее значение координаты*/
			public Point2D.Double now;
			/**Значение координаты, вокруг которой точка может немного флуктуировать*/
			public Point2D.Double real;
			
			public Point(Point2D.Double p){real = p; now = new Point2D.Double(p.x, p.y);}
		}
		/**Начальная точка волны*/
		public Point first;
		/**Центральная точка волны, вспомогательная*/
		public Point centered;
		/**Конечная точка волны*/
		public Point second;
		/**Сама волна, которая будет начерчена на экране*/
		public QuadCurve2D.Double wave;
		/**Отображается эта волна на экране?*/
		public boolean isVisible;
	}
	/**Все статические переменные*/
	private static class Static{
		/**Количество отображаемых волн*/
		public int countWave = 0;
		/**Все волны, даже те, что не отображаются - для кэширования*/
		public final List<Wave> waves = new ArrayList<>();
	}
	
	/**Состояние анимации*/
	private static Static state;
	/**Берег левый*/
	private ColorRec left;
	/**водичка*/
	private ColorRec water;
	/**Берег правый*/
	private ColorRec right;
	
	public River(WorldView.Transforms transform, int w, int h){
		//Левый песочек
		int xl[] = new int[4];
		//Поле, вода
		int xw[] = new int[4];
		int yw[] = new int[4];
		//Правый песочек
		int xr[] = new int[4];

		xl[0] = xl[3] = 0;
		xl[1] = xl[2] = xw[0] = xw[3] = transform.toScrinX(0);
		xw[1] = xw[2] = xr[0] = xr[3] = transform.toScrinX(Configurations.getWidth()-1);
		xr[1] = xr[2] = w;

		yw[0] = yw[1] = 0;
		yw[2] = yw[3] = h;
		left = new ColorRec(xl,yw,AllColors.SAND);
		water = new ColorRec(xw,yw, AllColors.WATER_RIVER);
		right = new ColorRec(xr,yw, AllColors.SAND);
		
		if(state == null) state = new Static();
	}
		
	@Override
	protected void nextFrame(){
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g) {
		left.paint(g);
		right.paint(g);
		
		final var q = new QuadCurve2D.Double(50,0,70,50,50,140);
		g.fill(q);
	}
	
}
