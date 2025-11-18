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
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

	private static class DynamicColor extends ColorRec{
		/**Функция создания прямоугольника в зависимости от освещённости*/
		private java.util.function.Function<Integer,ColorRec> colorGenerator;
		/**Текущее состояние*/
		private ColorRec now;
		
		public DynamicColor(java.util.function.Function<Integer,ColorRec> gen){
			super(0, 0, 0, 0, null);
			colorGenerator = gen;
			setLight(1000);
		}
		/**Устанавливает освещённость
		 * @param sunState освещёность [0,1000]. Где 0 - солнца нет, а 1000 - солнце светит на всю катушку
		 */
		public void setLight(int sunState){
			var alf = Utils.Utils.normalize_value(sunState, 0, 1000, 0, 128);
			now = colorGenerator.apply(alf);
		}
		@Override
		public void paint(Graphics2D g, java.awt.geom.Area field) {
			now.paint(g,field);
		}
	}
	
	/**Все статические переменные*/
	private static class Static{
		/**Продолжительность дня в секундах*/
		private static final int DAY_LONG = 24 * 60 * 60;
		/**Продолжительность года в секундах*/
		private static final int YEAR_LONG = 365 * DAY_LONG;
		/**Широта точки, где находится дом*/
		private static final double LATITUDE = Math.toRadians(59+57/60d);
		/**Косинус угла в точке, где находится дом*/
		private static final double COS_LATITUDE = Math.cos(LATITUDE);
		/**Синус угла в точке, где находится дом*/
		private static final double SIN_LATITUDE = Math.sin(LATITUDE);
		/**Синус угла наклона планеты*/
		private static final double SIN_EARTH_AXIS = Math.sin(Math.toRadians(23+26/60d));
        ///Окно, за которым и будет целый мир
        private static final java.awt.image.BufferedImage WINDOW;
        
		/**Возвращате угол наклона солнца над горизонтом
		 * @return угол наклона солнца, градусы
		 */
		public double getElevaion(){
			var sangle = Math.toRadians(second / (4 * 60d)); //Местное солнцечное время
			return Math.toDegrees(Math.asin(sinDF - cosDF * Math.cos(sangle)));
		}
		/**Возвращате угол наклона солнца над горизонтом
		 * @return угол наклона солнца, градусы
		 */
		public double getMaxElevaion(){
			return Math.toDegrees(Math.asin(sinDF + cosDF));
		}
		
		@Override public String toString(){return Utils.Utils.toString(this);}
		/**Текущая секунда года*/
		private int second = 3 * 30 * 24 * 60 * 60;
		/**Текущий день года*/
		private int day = -1;
		/**Произведение синуса склонения на синус широты*/
		private double sinDF;
		/**Произведение косинуса склонения на синус широты*/
		private double cosDF;
		/**Угол солнца над горизонтом*/
		private double elevation;
        
        static {
            var resourse = Static.class.getResource("/worlds/Aquarium_window.png");
            if(resourse == null){
                System.err.println("Не найдено изображение!");
                WINDOW = null;
            } else {
                java.awt.image.BufferedImage bufferedImage = null;
                try {
                    bufferedImage = javax.imageio.ImageIO.read(resourse);
                } catch (IOException ex) {
                    System.getLogger(Aquarium.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                WINDOW = bufferedImage;
            }
        }
	}
	private static double fourier(double val, int mount){
		var angle = 360 * val;
		var ret = 0;
		for (int i = 1; i < 5; i++) {
			var ds = Utils.Utils.randomByHash(mount + i * 1, -100,100);
			var dc = Utils.Utils.randomByHash(mount + i * 2, -100,100);
			var as = Utils.Utils.randomByHash(mount + i * 3, 1,3);
			var ac = Utils.Utils.randomByHash(mount + i * 4, 1,3);
			ret += ds * Math.sin(Math.toRadians(as*angle)) + dc * Math.cos(Math.toRadians(ac*angle));
		}
		return 0.03 * ret;
	}
	public Aquarium(WorldView.Transforms transform, int w, int h){
		super(transform);
		//Поле, вода
		water = ColorRec.rectangle(transform.toScrinX(0), transform.toScrinY(0), transform.toScrinX(Configurations.getWidth()-1),transform.toScrinY(Configurations.getHeight()-1), AllColors.WATER_AQUARIUM );
		
		if(state == null) state = new Static();
		var fieldH = transform.toDScrin(Configurations.getHeight());
		var fieldW = transform.toDScrin(Configurations.getWidth());
		var winH = Math.max(fieldH * 2, h);
		var winW = Math.max(fieldW * 2, w);
		var winSize = (int) Math.max(winH,winW);
		var border = (int)Math.max(w - transform.toDScrinX(Configurations.getWidth()-1), h - transform.toDScrinY(Configurations.getHeight()-1));
		var widthCenter = winSize / 14; //Ширина центральной части окна
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
		var mount_ys = (int)(y1 - winSize * 0.3); //С какой высоты начинаются горы
		var mount_ye = (int)(y1 - winSize * 0.25); //Куда нижняя часть гор стремится
		var mount_xs = x0 + widthCenter;
		
		var figures = new ArrayList<ColorRec>(this.staticColor.length);
		
		//Рисовать мы будем с заднего фона, постепенно пробираясь вперёд.
		//Фон. 
		figures.add(ColorRec.rectangle(x0,y0,x1,y1,new Color(0xE8DFFE)));
		//Солнце. Обязательно в этом месте!!!
		{
			var sun_h = (mount_ys - yr0); //Максимальная высота солнца над горизонтом
			var sun_r = widthCenter;
			figures.add(new DynamicColor((alfa) -> {
				if(sun_r == 0) return new ColorRec(0, 0, 0,0,null);
				
				var maxE = state.getMaxElevaion();
				var sy = mount_ys - sun_h * state.elevation / 56d;
				var sx = cx + 1.5 * (sun_h * maxE / 56d) * ((state.second % 86400 - 43200) / 86400d);
				var rectangle = new java.awt.geom.Ellipse2D.Double(sx-sun_r,sy-sun_r,sun_r * 2,sun_r * 2);
				var area = new java.awt.geom.Area(rectangle);
				var gradient = new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Double(sx,sy), sun_r, new float[]{0.0f, 1.0f}, new Color[]{AllColors.SUN,AllColors.toDark(AllColors.SUN, 0)},java.awt.RadialGradientPaint.CycleMethod.NO_CYCLE);
				return new ColorRec(0, 0, 0,0,null){
					@Override
					public void paint(Graphics2D g, java.awt.geom.Area field) {
						if(sy > mount_ys) return;
						area.subtract(field);
						g.setPaint(gradient);
						g.fill(area);
					}
				};
			}));
		}
		
		
		//Болото
		figures.add(ColorRec.rectangle(xr0, mount_ys, xr1, yr1, new Color(0x56412E)));
		//Озеро на карте
		{
			var widtw = (x1-mount_xs) * 0.7;
			figures.add(new ColorRec(0, 0, 0, 0, null){
				@Override
				public void paint(Graphics2D g, java.awt.geom.Area field) {
					g.setColor(new Color(0xAB9DC1));
					g.fillOval((int)(mount_xs-widtw/2), mount_ys, (int)widtw, mount_ye-mount_ys);
				}
			});
		}
		//А теперь рисуем горы
		{
			var colors = new Color[]{new Color(0xC2B5E0),new Color(0x897A99),new Color(0x645069)};
			for (int m = 0, my = mount_ys, mx = mount_xs; m < colors.length; m++) {
				var m_color = colors[m];
				var m_angle = Math.atan2(y0 - my, x1 - mx);
				var down_angl = Math.atan2(mount_ye - my, x1 - mx);
				var m_lenght = Math.hypot(y0 - my, x1 - mx);
				var down_lenght = Math.hypot(mount_ye - my, x1 - mx);
				var hash = m * Configurations.getHeight() + Configurations.getWidth();
				
				//Рисуем гору вправо
				{
					final int[] xm = new int[100];
					final int[] ym = new int[xm.length];
					var ul = (m_lenght / (xm.length - 2));
					var dl = (down_lenght / (xm.length - 2));
					xm[0] = mx;
					ym[0] = my;
					for (int i = 1; i < xm.length - 1; i++) {
						var fy = fourier(((double)i) / (xm.length - 2),hash);
						xm[i] = (int)(mx + i * ul * Math.cos(m_angle));
						var up_y = my + ul * (i * Math.sin(m_angle) + fy);
						var down_y = my + dl * (i * Math.sin(down_angl));
						ym[i] = (int)(Math.min(up_y, down_y));
					}
					xm[xm.length - 1] = x1;
					ym[xm.length - 1] = mount_ye;
					figures.add(new ColorRec(xm,ym, m_color));
				}
				//А теперь тоже самое, но влево
				{
					final int[] xm = new int[100];
					final int[] ym = new int[xm.length];
					var ul = (m_lenght / (xm.length - 2));
					var dl = (down_lenght / (xm.length - 2));
					xm[0] = mx;
					ym[0] = my;
					for (int i = 1; i < xm.length - 1; i++) {
						var fy = fourier(((double)i) / (xm.length - 2),hash);
						xm[i] = (int)(mx - i * ul * Math.cos(m_angle));
						var up_y = my + ul * (i * Math.sin(m_angle) + fy);
						var down_y = my + dl * (i * Math.sin(down_angl));
						ym[i] = (int)(Math.min(up_y, down_y));
					}
					xm[xm.length - 1] = (int)(mx - m_lenght);
					ym[xm.length - 1] = mount_ye;
					figures.add(new ColorRec(xm,ym, m_color));
				}
				//А теперь перейдём к следующей гОре горЕ
				var delta = Utils.Utils.randomByHash(hash - 1, -10,10) / 100d;
				my += (mount_ye - mount_ys) * delta;
				mx += (x1 - mount_xs) * delta;
			}
			//А это ближайшая, тёмная гора с речкой
			var delta = 0.25;
			var my = (int)(mount_ys + (mount_ye - mount_ys) * delta);
			var mx = (int)(mount_xs + (x1 - mount_xs) * delta);
			var m_color = new Color(0x452C41);
			var m_angle = Math.atan2((y0+yf0)/2 - my, x1 - mx);
			var m_lenght = Math.hypot((y0+yf0)/2 - my, x1 - mx);
			var down_angl = Math.atan2(mount_ye - my, x1 - mx);
			var down_lenght = Math.hypot(mount_ye - my, x1 - mx);
			var hash = 100 * Configurations.getHeight() + Configurations.getWidth();

			//Рисуем гору вправо
			final int[] xm = new int[100];
			final int[] ym = new int[xm.length];
			var ul = (m_lenght / (xm.length - 2));
			var dl = (down_lenght / (xm.length - 2));
			xm[0] = mx;
			ym[0] = my;
			for (int i = 1; i < xm.length - 1; i++) {
				var fy = fourier(((double)i) / (xm.length - 2),hash);
				xm[i] = (int)(mx + i * ul * Math.cos(m_angle));
				var up_y = my + ul * (i * Math.sin(m_angle) + fy);
				var down_y = my + dl * (i * Math.sin(down_angl));
				ym[i] = (int)(Math.min(up_y, down_y));
			}
			xm[xm.length - 1] = x1;
			ym[xm.length - 1] = mount_ye;
			figures.add(new ColorRec(xm,ym, m_color));
		}
		//Травка под окном
		{
			var yaxis = (yf0 + y1) / 2;
			var height = (yf0 - y1) / 8;
			final int[] xd = new int[100];
			final int[] yd = new int[xd.length];
			xd[0] = xd[xd.length - 1] = x0;
			yd[0] = yd[yd.length - 3] = yaxis;
			var elements = xd.length - 4 - 1;
			var width = cx - x0;
			for (int i = 1; i < xd.length - 3; i++) {
				xd[i] = x0 + i * width / elements;
				yd[i] = (int)(yaxis - height * Math.sin(Math.toRadians(180 * i / elements)));
			}
			xd[xd.length - 3] = xd[xd.length - 2] = cx;
			yd[yd.length - 2] = yd[yd.length - 1] = yr1;
			
			figures.add(new ColorRec(xd,yd, new Color(0x2D1925)));
			//figures.add(rectangle(x0, yaxis, cx, yr1, new Color(0x2D1925)));
		}
		
		
		//А теперь серый фильтр, который будет символизировать ночь
		figures.add(new DynamicColor((alf) -> ColorRec.rectangle(x0,y0,x1,y1,AllColors.toDark(Color.BLACK, alf*2))));
		//Стекло
		{
			var glassColor = new Color(0x60cde8ff, true);
			figures.add(ColorRec.rectangle(x0,y0,x1,y1,glassColor));
		}
		//Поверх стелка - рамка по краям, рама стекла
		figures.add(ColorRec.rectangle(x1,yr0,xr1,yr1,Color.BLACK));
		figures.add(ColorRec.rectangle(xr0,y1,xr1,yr1,Color.BLACK));
		
		//Рамка под стёкла. Тут только две рамки, потому что левый и верхний края не видны
		final var colorWin = new Color(0xFFFAFA);
		figures.add(ColorRec.rectangle(x1-widthCenter,y0,x1,y1,colorWin));
		figures.add(ColorRec.rectangle(x0,y1,x1,y1 - widthCenter,colorWin));
		
		//Вертикальная перекладина
		figures.add(ColorRec.rectangle(cx-widthCenter,y0,cx+widthCenter,y1,colorWin));
		
		//Ручка
		final var colorHand = new Color(0xE8E8EF);
		figures.add(ColorRec.rectangle(cx+widthCenter/5,y0 + winSize/2 - widthCenter*2,cx+widthCenter*4/5,y0 + winSize/2 - widthCenter,colorHand));
		figures.add(ColorRec.rectangle(cx+widthCenter*2/5,y0 + winSize/2 - widthCenter,cx+widthCenter*3/5,y0 + winSize/2,colorHand));
		
		//Разделение створок
		figures.add(ColorRec.rectangle(cx-widthCenter/4,y0,cx,y1,Color.BLACK));
		
		//Лампа
		var lampW = (winSize / 2) / 3;
		//Подошва
		figures.add(ColorRec.rectangle(x0+lampW,y1,x0+lampW+lampW,y1-border,Color.BLACK));
		//Люстра
		var rucy = y0+winSize/4;
		var lampx1 = x1-winSize/4+lampW/2;
		var lampx2 = x1-winSize/4-lampW/2;
		var lampy2 = rucy+lampW/2;
		figures.add(ColorRec.rectangle(x1-winSize/4-lampW/6,rucy-lampW/2,x1-winSize/4+lampW/6,rucy,Color.BLACK));
		figures.add(new ColorRec(new int[]{x1-winSize/4-lampW/6,x1-winSize/4+lampW/6, lampx1, lampx2},new int[]{rucy,rucy,lampy2,lampy2},Color.BLACK));
		//Нога
		var xleg = x0-border;
		var yleg = y0+winSize/2-border;
		var legw = Math.max(1,lampW/6);
		var legyUp = rucy - legw*2;
		figures.add(new ColorRec(new int[]{xleg,xleg+legw, x0+lampW+legw*2, x0+lampW+legw*1},new int[]{yleg,yleg,y1,y1},Color.BLACK));
		figures.add(new ColorRec(new int[]{xleg,x1-winSize/4-lampW/6-legw, x1-winSize/4-lampW/6-legw, xleg},new int[]{yleg,legyUp,legyUp + legw,yleg+legw},Color.BLACK));
		figures.add(ColorRec.rectangle(x1-winSize/4-lampW/6-legw,legyUp,x1-winSize/4-lampW/6,legyUp + legw,Color.BLACK));
		
		//А теперь стёкла аквариума.
		var w_glass_a = Math.min(widthCenter/4,xr1-x1)/2;
		{
			var glassColor = new Color(0xF0a8ccd7, true);
			figures.add(ColorRec.rectangle(xf0-w_glass_a,yf0-w_glass_a,xf0,y1+w_glass_a,glassColor));
			figures.add(ColorRec.rectangle(x1,yf0-w_glass_a,x1+w_glass_a,y1+w_glass_a,glassColor));
			figures.add(ColorRec.rectangle(xf0-w_glass_a,y1,x1+w_glass_a,y1+w_glass_a,glassColor));
		}
		
		//Затемнение помещения ночью
		{
			figures.add(new DynamicColor((alf) -> ColorRec.rectangle(x1-widthCenter,y0,x1,y1,AllColors.toDark(Color.BLACK, alf))));
			figures.add(new DynamicColor((alf) -> ColorRec.rectangle(x0,y1,x1,y1 - widthCenter,AllColors.toDark(Color.BLACK, alf))));
			figures.add(new DynamicColor((alf) -> ColorRec.rectangle(cx-widthCenter,y0,cx+widthCenter,y1 - widthCenter,AllColors.toDark(Color.BLACK, alf))));
		}
		//Конус света
		{
			var colorLamp = (java.util.function.Function<Integer,java.awt.GradientPaint>)(alf) -> new java.awt.GradientPaint(
				new java.awt.geom.Point2D.Double((lampx2+lampx1)/2, lampy2), AllColors.toDark(new Color(0x80ffb46b, true), alf),
				new java.awt.geom.Point2D.Double((lampx2+lampx1)/2, y1), new Color(0x00ffb46b, true));
			figures.add(new DynamicColor((alf) -> new ColorRec(new int[]{lampx2, lampx1, x1,xf0,xf0,cx},new int[]{lampy2,lampy2,yf0,yf0,y1,y1},colorLamp.apply(alf))));
		}/**/
		this.staticColor = figures.toArray(ColorRec[]::new);
        
        
        //Рама
        //Место, куда надо поставить аквариум. Нижний правый угол - 500х480, ширина/высота - 240
        var artWinSize = Math.max(fieldH, fieldW) / 240d;
        window = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        if(Configurations.getWidth() > Configurations.getHeight()){
            var wx = (int) Math.round(x1-500*artWinSize);
            var ww = (int) Math.round(artWinSize * Static.WINDOW.getWidth(null));
            //artWinSize = artWinSize * Configurations.getWidth() / Configurations.getHeight();
            var wy = (int) Math.round(y1-480*artWinSize);
            var wh = (int) Math.round(artWinSize * Static.WINDOW.getHeight(null));  
            
            var g = window.getGraphics();
            g.drawImage(Static.WINDOW,wx,wy,ww,wh, null);
            g.dispose();
        } else {
            var wy = (int) Math.round(y1-480*artWinSize);
            var wh = (int) Math.round(artWinSize * Static.WINDOW.getHeight(null));  
            //artWinSize = artWinSize * Configurations.getHeight() / Configurations.getWidth();
            var wx = (int) Math.round(x1-500*artWinSize);
            var ww = (int) Math.round(artWinSize * Static.WINDOW.getWidth(null)); 
            
            var g = window.getGraphics();
            g.drawImage(Static.WINDOW,wx,wy,ww,wh, null);
            g.dispose();
        }
        ///А теперь вырезаем игровое поле
        for (int x = xf0; x <= x1; ++x) {
            for (int y = yf0; y <= y1; ++y) {
                window.setRGB(x, y, 0x00);
            }
        }
	}
	@Override
	protected void nextFrame(){
		state.second = (state.second + 60) % Static.YEAR_LONG;
		var day = state.second / Static.DAY_LONG;
		if(day != state.day){
			state.day = day;
			var sinDec = Static.SIN_EARTH_AXIS * Math.sin(2 * Math.PI * (day - 81) / 365d);
			var Dec = Math.asin(sinDec); //Склоненние солнца
			state.sinDF = Static.SIN_LATITUDE * sinDec;
			state.cosDF = Static.COS_LATITUDE * Math.cos(Dec);
		}
		state.elevation = state.getElevaion();
		//System.out.println(((state.second / 3600) % 24) + " " + state);
		if(state.second == 0 || 0 < state.elevation && state.elevation < 6){
			var alf = Utils.Utils.normalize_value(Utils.Utils.round(state.elevation * 100), 0, 600, 1000, 0);
			alf = Utils.Utils.betwin(0, alf, 1000);
			for(var c : staticColor)
				if(c instanceof DynamicColor dc)
					dc.setLight(alf);
		}
		((DynamicColor)staticColor[1]).setLight(Utils.Utils.round(state.elevation));
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g, Rectangle visible, java.awt.geom.Area field) {
		for(var c : staticColor)
			c.paint(g,field);
        g.drawImage(window,0,0,null);
	}
	
	/***/
	private static Static state;
	/**водичка*/
	private ColorRec water;
	/**Раскраска под статик*/
	private ColorRec[] staticColor = new ColorRec[0];
    ///Окно, которое надо отрисовать
    private final java.awt.image.BufferedImage window;
}
