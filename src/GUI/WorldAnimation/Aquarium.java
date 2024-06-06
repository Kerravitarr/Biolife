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

		xd[0] = xd[1] = 0;
		xd[6] = xd[7] = xw[0] = xw[3] = transform.toScrinX(0);
		xd[4] = xd[5] = xw[1] = xw[2] = transform.toScrinX(Configurations.getWidth()-1);
		xd[2] = xd[3] = w;

		yd[1] = yd[2] = 0;
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
		//Сначала стёкла
		var glassColor = new Color(0xcde8ff);
		figures.add(rectangle(x0,y0,cx,y1,glassColor));
		figures.add(rectangle(cx,y0,x1,y0 + winSize/2,glassColor));
		{
			final int xg[] = new int[6];
			final int yg[] = new int[6];
			xg[0] = xg[5] = cx;
			xg[1]=xg[2] = x1;
			xg[3]=xg[4] = xf0;
			yg[0]=yg[1] = y0 + winSize/2;
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
		figures.add(rectangle(x1-border,y0,x1,yf0,Color.GRAY));
		if(fieldW < border) //Если у нас слшиокм узкое поле, надо надорисовать рамку сбоку от него
			figures.add(rectangle(x1-border,yf0,xf0,y1,Color.GRAY));
		figures.add(rectangle(x0,y1,xf0,y1 - border,Color.GRAY));
		if(fieldH < border) //Если у нас слшиокм узкое поле, надо надорисовать рамку сбоку от него
			figures.add(rectangle(xf0,y1 - border,x1,yf0,Color.GRAY));
		//Горизонтальная перекладина
		figures.add(rectangle(x0,y1 - winSize/2 - border/4,x1,y1 - winSize/2,Color.GRAY));
		figures.add(rectangle(x0,y1 - winSize/2,xf0,y1 - winSize/2 + border/4,Color.GRAY));
		figures.add(rectangle(xf0,y1 - winSize/2,x1,Math.min(y1 - winSize/2 + border/4, yf0),Color.GRAY));
		
		//Вертикальная перекладина
		figures.add(rectangle(cx-border,y0,cx,y1,Color.GRAY));
		figures.add(rectangle(cx,y0,cx+border,yf0,Color.GRAY));
		figures.add(rectangle(cx,yf0,Math.min(cx+border,xf0),y1,Color.GRAY));
		//Разделение створок
		figures.add(rectangle(cx-border/4,y0,cx,y1,Color.BLACK));
		figures.add(rectangle(cx,y0,cx+border/4,yf0,Color.BLACK));
		figures.add(rectangle(cx,yf0,Math.min(cx+border/4,xf0),y1,Color.BLACK));
		
		//Лампа
		var lampW = (winSize / 2) / 3;
		//Подошва
		figures.add(rectangle(x0+lampW,y1,x0+lampW+lampW,y1-border,Color.BLACK));
		//Люстра
		var rucy = y0+winSize/4;
		figures.add(rectangle(x1-winSize/4-lampW/6,rucy-lampW/2,x1-winSize/4+lampW/6,rucy,Color.BLACK));
		figures.add(new ColorRec(new int[]{x1-winSize/4-lampW/6,x1-winSize/4+lampW/6, x1-winSize/4+lampW/2, x1-winSize/4-lampW/2},new int[]{rucy,rucy,rucy+lampW/2,rucy+lampW/2},Color.BLACK));
		//Нога
		var xleg = x0-border;
		var yleg = y0+winSize/2-border;
		var legw = Math.max(1,lampW/6);
		var legyUp = rucy - legw*2;
		figures.add(new ColorRec(new int[]{xleg,xleg+legw, x0+lampW+legw*2, x0+lampW+legw*1},new int[]{yleg,yleg,y1,y1},Color.BLACK));
		figures.add(new ColorRec(new int[]{xleg,x1-winSize/4-lampW/6-legw, x1-winSize/4-lampW/6-legw, xleg},new int[]{yleg,legyUp,legyUp + legw,yleg+legw},Color.BLACK));
		figures.add(rectangle(x1-winSize/4-lampW/6-legw,legyUp,x1-winSize/4-lampW/6,legyUp + legw,Color.BLACK));
		
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
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g, Rectangle visible) {
		air.paint(g);
		table.paint(g);
		for(var c : border)
			c.paint(g);
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
}
