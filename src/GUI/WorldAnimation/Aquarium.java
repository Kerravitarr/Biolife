/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ColorRec;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Анимация для аквариума.
 * Тут сложнее. Аквариум. Это бак с водой стоящий на подоконнике. За ним - окно. За окном погода меняется. Можно сделать занавески
 * Окно имеет ширину, минимум как две ширины аквариума (максимум - бесконечно)
 * С высотой история та-же.
 * Рядом с аквариумом стоит лампа. Лампа должна разгораться когда на улице темнее
 * На улице есть смена дня и ночи
 * Солнце идёт по небосводу
 * Луна идёт ночью. Может стариться и рости
 * За окном может быть пруд и река... Отсылка к другим мирам!
 * Тучки ещё могут быть...
 * А река будет одна из озера, но будет петлять.
 * 
 * @author Kerravitarr
 */
public class Aquarium extends DefaultAnimation{
	
	/**Все статические переменные*/
	private static class Static{
		/**Продолжительность дня в секундах*/
		private static final int DAY_LONG = 24 * 60 * 60;
		/**Продолжительность года в секундах*/
		private static final int YEAR_LONG = 365 * DAY_LONG;
		/**Широта точки для исследования*/
		private static final double LATITUDE = Math.toRadians(59+57/60d);
		/**Косинус угла в искомой точки*/
		private static final double COS_LATITUDE = Math.cos(LATITUDE);
		/**Синус угла в искомой точки*/
		private static final double SIN_LATITUDE = Math.sin(LATITUDE);
		/**Синус угла наклона планеты*/
		private static final double SIN_EARTH_AXIS = Math.sin(Math.toRadians(23+26/60d));
		
		@Override public String toString(){return Utils.Utils.toString(this);}
		/**Текущая секунда года*/
		private int second = -1;
		/**Текущий день года*/
		private int day = -1;
		/**Произведение синуса склонения на синус широты*/
		private double sinDF;
		/**Произведение косинуса склонения на синус широты*/
		private double cosDF;
		/**Угол солнца над горизонтом*/
		private double elevation;
	}
	
	public Aquarium(WorldView.Transforms transform, int w, int h){
		//Нижняя часть поля
		final int xd[] = new int[8];
		final int yd[] = new int[8];
		//Поле, вода
		final int xw[] = new int[4];
		final int yw[] = new int[4];
		//Верхняя часть поля
		final int yu[] = new int[8];
		final var startPos = -1000; //Чисто отладочное число. Тут должно быть 0!!!
		xd[0] = xd[1] = startPos;
		xd[6] = xd[7] = xw[0] = xw[3] = transform.toScrinX(0);
		xd[4] = xd[5] = xw[1] = xw[2] = transform.toScrinX(Configurations.getWidth()-1);
		xd[2] = xd[3] = w;

		yd[1] = yd[2] = startPos;
		yw[0] = yw[1] = yd[5] = yd[6] = transform.toScrinY(0);
		yd[0] = yd[3] = yd[4] = yd[7] = yu[0] = yu[3] = yu[4] = yu[7] = transform.toScrinY(Configurations.getHeight()-3); //Место сшивания полей
		yw[2] = yw[3] = yu[5] = yu[6] = transform.toScrinY(Configurations.getHeight()-1);
		yu[1] = yu[2] = h;


		air = new ColorRec(xd,yd,Color.WHITE);
		table = new ColorRec(xd,yu, Color.WHITE);
		water = rectangle(transform.toScrinX(0), transform.toScrinY(0), transform.toScrinX(Configurations.getWidth()-1),transform.toScrinY(Configurations.getHeight()-1), AllColors.WATER_AQUARIUM );
		
		if(state == null) state = new Static();
		var fieldH = transform.toDScrin(Configurations.getHeight());
		var fieldW = transform.toDScrin(Configurations.getWidth());
		var winH = Math.max(fieldH * 2, h);
		var winW = Math.max(fieldW * 2, w);
		var winSize = (int) Math.max(winH,winW);
		var border = (int)Math.max(w - transform.toDScrinX(Configurations.getWidth()-1), h - transform.toDScrinY(Configurations.getHeight()-1));
		var widthCenter = winSize / 14;
		var x1 = transform.toScrinX(Configurations.getWidth()-1); //Конец поля. Для рисунка это максимальная координата
		var y1 = transform.toScrinY(Configurations.getHeight()-1);
		var x0 = x1 - winSize; //Начало поля. Для рисунка это минимальная координата
		var y0 = y1 - winSize;
		var xf0 = transform.toScrinX(0); //А это начало игрового поля
		var yf0 = transform.toScrinY(0);
		var xr0 = x0 - border; //А это начало окна. Игровое поле находится в рамке - это вот рамка
		var yr0 = y0 - border;
		var xr1 = x1 + border; //А это конец окна. Игровое поле находится в рамке - это вот рамка
		var yr1 = y1 + border;
		var cx = x1 - winSize/2; //Середина поля
		
		var figures = new ArrayList<ColorRec>();
		
		
		//Стекло
		{
			var glassColor = new Color(0x80cde8ff, true);
			final int xg[] = new int[6];
			final int yg[] = new int[6];
			xg[0] = xg[5] = x0;
			xg[1]=xg[2] = x1;
			xg[3]=xg[4] = xf0;
			yg[0]=yg[1] = y0;
			yg[2]=yg[3]=yf0;
			yg[4]=yg[5]=y1;
			figures.add(new ColorRec(xg,yg,glassColor));
		}
		
		//Рамка по краям
		figures.add(rectangle(x1,yr0,xr1,yr1,Color.BLACK));
		figures.add(rectangle(xr0, yr0, x0, yr1,Color.BLACK));
		figures.add(rectangle(xr0,y0,xr1,yr0,Color.BLACK));
		figures.add(rectangle(xr0,y1,xr1,yr1,Color.BLACK));
		
		//Рамка под стёкла. Тут только две рамки, потому что левый и верхний края не видны
		final var colorWin = new Color(0xFFFAFA);
		figures.add(rectangle(x1-widthCenter,y0,x1,yf0,colorWin));
		if(fieldW < widthCenter) //Если у нас слшиокм узкое поле, надо надорисовать рамку сбоку от него
			figures.add(rectangle(x1-widthCenter,yf0,xf0,y1,colorWin));
		figures.add(rectangle(x0,y1,xf0,y1 - widthCenter,colorWin));
		if(fieldH < widthCenter) //Если у нас слшиокм узкое поле, надо надорисовать рамку сбоку от него
			figures.add(rectangle(xf0,y1 - widthCenter,x1,yf0,colorWin));
		
		//Вертикальная перекладина
		figures.add(rectangle(cx-widthCenter,y0,cx,y1,colorWin));
		figures.add(rectangle(cx,y0,cx+widthCenter,yf0,colorWin));
		figures.add(rectangle(cx,yf0,Math.min(cx+widthCenter,xf0),y1,colorWin));
		
		//Ручка
		final var colorHand = new Color(0xE8E8EF);
		figures.add(rectangle(cx+widthCenter/5,y0 + winSize/2 - widthCenter*2,cx+widthCenter*4/5,y0 + winSize/2 - widthCenter,colorHand));
		figures.add(rectangle(cx+widthCenter*2/5,y0 + winSize/2 - widthCenter,cx+widthCenter*3/5,y0 + winSize/2,colorHand));
		
		//Разделение створок
		figures.add(rectangle(cx-widthCenter/4,y0,cx,y1,Color.BLACK));
		//figures.add(rectangle(cx,y0,cx+border/4,yf0,Color.BLACK));
		//figures.add(rectangle(cx,yf0,Math.min(cx+border/4,xf0),y1,Color.BLACK));
		
		//Лампа
		var lampW = (winSize / 2) / 3;
		//Подошва
		figures.add(rectangle(x0+lampW,y1,x0+lampW+lampW,y1-border,Color.BLACK));
		//Люстра
		var rucy = y0+winSize/4;
		var lampx1 = x1-winSize/4+lampW/2;
		var lampx2 = x1-winSize/4-lampW/2;
		var lampy2 = rucy+lampW/2;
		figures.add(rectangle(x1-winSize/4-lampW/6,rucy-lampW/2,x1-winSize/4+lampW/6,rucy,Color.BLACK));
		figures.add(new ColorRec(new int[]{x1-winSize/4-lampW/6,x1-winSize/4+lampW/6, lampx1, lampx2},new int[]{rucy,rucy,lampy2,lampy2},Color.BLACK));
		//Нога
		var xleg = x0-border;
		var yleg = y0+winSize/2-border;
		var legw = Math.max(1,lampW/6);
		var legyUp = rucy - legw*2;
		figures.add(new ColorRec(new int[]{xleg,xleg+legw, x0+lampW+legw*2, x0+lampW+legw*1},new int[]{yleg,yleg,y1,y1},Color.BLACK));
		figures.add(new ColorRec(new int[]{xleg,x1-winSize/4-lampW/6-legw, x1-winSize/4-lampW/6-legw, xleg},new int[]{yleg,legyUp,legyUp + legw,yleg+legw},Color.BLACK));
		figures.add(rectangle(x1-winSize/4-lampW/6-legw,legyUp,x1-winSize/4-lampW/6,legyUp + legw,Color.BLACK));
		
		//Конус света
		var colorLamp = (java.util.function.Function<Integer,java.awt.GradientPaint>)(alf) -> new java.awt.GradientPaint(
			new java.awt.geom.Point2D.Double((lampx2+lampx1)/2, lampy2), AllColors.toDark(new Color(0x80ffb46b, true), alf),
			new java.awt.geom.Point2D.Double((lampx2+lampx1)/2, y1), new Color(0x00ffb46b, true));
		if(Utils.Utils.normalize_value(yf0, y1, rucy+lampW/2, cx, lampx2) < xf0){
			//Свет лампы достреливает до пола
			lampGenerator = (alf) -> new ColorRec(new int[]{lampx2, lampx1, x1,xf0,xf0,cx},new int[]{lampy2,lampy2,yf0,yf0,y1,y1},colorLamp.apply(alf));
		} else {
			lampGenerator = (alf) -> new ColorRec(new int[]{lampx2, lampx1, x1,xf0},new int[]{lampy2,lampy2,yf0,yf0},colorLamp.apply(alf));
		}
		lamp = lampGenerator.apply(128);
		
		this.border = figures.toArray(ColorRec[]::new);
	}
	private ColorRec rectangle(int x0, int y0, int x1, int y1, Color color){
		final int xrb[] = new int[4];
		final int yrb[] = new int[4];
		xrb[0] = xrb[3] = x0;
		xrb[1] = xrb[2] = x1;
		yrb[0] = yrb[1] = y0;
		yrb[2] = yrb[3] = y1;
		return new ColorRec(xrb,yrb, color);
	}	
	@Override
	protected void nextFrame(){
		state.second = (state.second + 1 * 60 ) % Static.YEAR_LONG;
		var day = state.second / Static.DAY_LONG;
		if(day != state.day){
			state.day = day;
			var sinDec = Static.SIN_EARTH_AXIS * Math.sin(2 * Math.PI * (day - 81) / 365d);
			var Dec = Math.asin(sinDec); //Склоненние солнца
			state.sinDF = Static.SIN_LATITUDE * sinDec;
			state.cosDF = Static.COS_LATITUDE * Math.cos(Dec);
		}
		var sangle = Math.toRadians(state.second / (4 * 60d)); //Местное солнцечное время
		state.elevation = Math.toDegrees(Math.asin(state.sinDF - state.cosDF * Math.cos(sangle)));
		System.out.println(((state.second / 3600) % 24) +" " + state);
		if(state.second == 0 || 0 < state.elevation && state.elevation < 6){
			var alf = Utils.Utils.normalize_value(Utils.Utils.round(state.elevation * 100), 0, 600, 128, 0);
			alf = Utils.Utils.betwin(0, alf, 128);
			lamp = lampGenerator.apply(alf);
		}
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g, Rectangle visible) {
		air.paint(g);
		table.paint(g);
		for(var c : border)
			c.paint(g);
		lamp.paint(g);
	}
	
	/***/
	private static Static state;
	/**Воздух*/
	private ColorRec air;
	/**водичка*/
	private ColorRec water;
	/**стол*/
	private ColorRec table;
	/**Боковая рамка*/
	private ColorRec[] border;
	/**Лампа*/
	private ColorRec lamp;
	/**Функция создания лампы в зависимости от освещённости*/
	private java.util.function.Function<Integer,ColorRec> lampGenerator;
}
