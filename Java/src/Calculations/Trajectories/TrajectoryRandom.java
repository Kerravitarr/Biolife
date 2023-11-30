/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Trajectories;

import Calculations.Configurations;
import Calculations.Point;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Траектория движения по случайным точкам
 * 
 * Работает следующим образом - каждые N циклов генерирует опорные точки. А потом между
 * двумя опорными точками рисует линию постоянной длинны
 * @author Kerravitarr
 */
public class TrajectoryRandom extends Trajectory{
	static{
		final var builder = new ClassBuilder<TrajectoryRandom>(){
			@Override public TrajectoryRandom generation(JSON json, long version){return new TrajectoryRandom(json, version);}
			@Override public JSON serialization(TrajectoryRandom object) { 
				final var j = object.toJSON();
				j.add("SEED",object.seed);
				j.add("START",object.start.toJSON());
				j.add("LU",object.rectangle[0].toJSON());
				j.add("RD",object.rectangle[1].toJSON());
				return j;
			}

			@Override public String serializerName() {return "Случайность";}
			@Override public Class printName() {return TrajectoryRandom.class;}

		};
		builder.addConstructor(new ClassBuilder.Constructor<TrajectoryRandom>(){
			{
				addParam(new ClassBuilder.NumberConstructorParamAdapter("super.speed", 0L,500L,1000L,0L,null));
				addParam(new ClassBuilder.NumberConstructorParamAdapter("seed", 0L,0L,100L,null,null){@Override public Long getDefault() {return Utils.Utils.hashCode(System.currentTimeMillis());}});
				addParam(new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "start";}
				});
				addParam(new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(0, 0);}
					@Override public String name() {return "LU";}
				});
				addParam(new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()-1, Configurations.getHeight()-1);}
					@Override public String name() {return "RD";}
				});
			}
			@Override
			public TrajectoryRandom build() {
				return new TrajectoryRandom(getParam(0,Long.class),getParam(1,Long.class),getParam(2,Point.class),getParam(3,Point.class),getParam(4,Point.class));
			}
			@Override public String name() {return "";}
		});
		Trajectory.register(builder);
	}
	/**Стартовая точка траектории. С которой мы начинаем движение*/
	private final Point start;
	/**Зерно генерации, чтобы все траектории от одного начала были одинаковыми*/
	private final long seed;
	/**"Номер" траектории в памяти*/
	private long number = -1;
	/**Период обновления точек. Или, длина траектории*/
	private final int lenght;
	/**Текущие точки траектории*/
	private final List<Point> points;
	/**Координаты верхнего левого и нижнего правого прямоугольника, внутри которого генерируются клетки*/
	private final Point[] rectangle = new Point[2];
	/**Ширина, расстояние по X, ограничивающего квадрата*/
	private final int width;
	/**Высота, расстояние по Y, ограничивающего квадрата*/
	private final int height;
	
	
	
	private final List<Point> lpoints = new ArrayList<>();
	
	/** * Создаёт линейную, траекторию от точки к точке.объект смещается каждый раз на 1 клетку мира
	 * @param speed скорость, в тиков на шаг
	 * @param seed уникальное зерно этой траектории
	 * @param start начальная точка траектории
	 * @param leftUp верхний левый ограничивающий угол
	 * @param rightDown нижний правый ограничивающий угол
	 * 
	 */
	public TrajectoryRandom(long speed, long seed, Point start, Point leftUp, Point rightDown){
		super(speed);
		this.seed = seed == 0 ? Utils.Utils.hashCode(seed) : seed;
		this.start = start;
		rectangle[0] = leftUp;
		rectangle[1] = rightDown;
		
		width = rectangle[1].x - rectangle[0].x;
		height = rectangle[1].y - rectangle[0].y;
		lenght = (int)Math.ceil(Math.hypot(width, height));
		points = Arrays.asList(new Point[lenght]);
		position(Configurations.world.step); //Для генерации первых точек
	}
	protected TrajectoryRandom(JSON j, long version){
		super(j,version);
		seed = j.getL("SEED");
		start = Point.create(j.getJ("START"));
		rectangle[0] = Point.create(j.getJ("LU"));
		rectangle[1] = Point.create(j.getJ("RD"));
		
		width = rectangle[1].x - rectangle[0].x;
		height = rectangle[1].y - rectangle[0].y;
		lenght = (int)Math.ceil(Math.hypot(width, height));
		points = Arrays.asList(new Point[lenght]);
		position(Configurations.world.step); //Для генерации первых точек
	}

	/**
	 * Создаёт траекторию постоянной длинны на основе 4х точек
	 * @param from первая точка траектории
	 * @param to последняя точка траектории
	 * @param C1 вспомогательная точка, для задания направления от начала траектории
	 * @param C4 вспомогательная точка, для задания направления ближе к концу линии
	 * @param mass массив, в которй будут записаны новые точки
	 */
	private void regenerate(Point from, Point to, Point C1, Point C4, List<Point> mass){
		final var F0 = 1;
		final var F1 = 1;
		final var F2 = 1 * 2;
		final var F3 = 1 * 2 * 3;
		final var F4 = 1 * 2 * 3 * 4;
		final var F5 = 1 * 2 * 3 * 4 * 5;
		
		final var isFromEqTo = from.equals(to);
		final var v = isFromEqTo ? C4.distance(to) : from.distance(to);
		//Точка посреди лини для растяжения кривой до нужных размеров
		final var center = isFromEqTo ? C4.add(v.divide(2)) : from.add(v.divide(2));
		final double l = isFromEqTo ? lenght : (lenght - v.getHypotenuse()); //Нераспределённая длинна траектории
		final var isUp = switch(v.direction()){case RIGHT,DOWN_R,DOWN,DOWN_L -> false; case LEFT,UP_L,UP,UP_R -> true;}; //Вверх смотрит вектор или вниз?
		//Вектор от центра к углу ограничивающего квадрата
		final var V2 = center.distance(isUp ? Point.create(rectangle[1].x, rectangle[0].y) : rectangle[1]);
		
		final Point C2 = Point.create((int) Math.round(center.x + l * V2.x / width),(int) Math.round(center.y  + l * V2.y / height));
		final Point C3 = Point.create((int) Math.round(center.x - l * (width-V2.x) / width),(int) Math.round(center.y  - l * (height-V2.y) / height));
		
		for(var i = 0.0 ; i < lenght; i++){
			final var t = i/lenght;
			final var B0 = (F5 / (F0 * F5)) * Math.pow(t, 0) * Math.pow(1 - t, 5);
			final var B1 = (F5 / (F1 * F4)) * Math.pow(t, 1) * Math.pow(1 - t, 4);
			final var B2 = (F5 / (F2 * F3)) * Math.pow(t, 2) * Math.pow(1 - t, 3);
			final var B3 = (F5 / (F3 * F2)) * Math.pow(t, 3) * Math.pow(1 - t, 2);
			final var B4 = (F5 / (F4 * F1)) * Math.pow(t, 4) * Math.pow(1 - t, 1);
			final var B5 = (F5 / (F5 * F0)) * Math.pow(t, 5) * Math.pow(1 - t, 0);
			final var x = B0 * from.x + B1 * C1.x + B2 * C2.x + B3 * C3.x + B4 * C4.x + B5 * to.x;
			final var y = B0 * from.y + B1 * C1.y + B2 * C2.y + B3 * C3.y + B4 * C4.y + B5 * to.y;
			mass.set((int)i, Point.create((int)Math.round(x),(int)Math.round(y)));
		}
	}
	/**
	 * Создаёт точку траектории по её индексу
	 * @param index порядковый номер точки
	 * @return точка на карте
	 */
	private Point generate(long index){
		if(index == 0) return start;
		final var x = (int) Utils.Utils.randomByHash(index,rectangle[0].x, rectangle[1].x);
		final var y = (int) Utils.Utils.randomByHash(Utils.Utils.hashCode(index),rectangle[0].y, rectangle[1].y); //Тут двойное хэширование, чтобы x и y различались!
		return Point.create(x, y);
	}
	@Override
	protected Point position(long wstep) {
		var num = wstep / lenght;
		final var p = wstep % lenght;
		if(num != number){
			number = num;
			var sf = num + seed; //Сид первой точки этой траектории или последней точки предыдущей траектории
			var se = sf + 1;//Сид последней точки этой траектории
			var sp = sf - 1;//Сид предыдущей первой точки траектории
			//Нам нужно сгенерировать точки...
			var pref = generate(sp); //Предыдущая точка траектории, нужна только для направления
			var from = generate(sf); //Первая точка нашей траектории
			var to = generate(se); //Последняя точка нашей траектории
			Point C4, C1;
			//Теперь нам нужны две вспомогательные точки:
			if(from.equals(to)){ //Если у нас начало и конец совпали
				final var d = Point.DIRECTION.toEnum((int) sf);
				C4 = Point.create(to.x - d.addX,to.y + d.addY);
			} else {
				C4 = from;
			}
			if(pref.equals(from)){ //Если у нас совпадает предущая последняя точка и эта первая
				final var d = Point.DIRECTION.toEnum((int) sp);
				C1 = Point.create(from.x - d.addX,from.y + d.addY);
			} else {
				C1 = pref;
			}
			regenerate(from,to,C1,C4,points);
			
			lpoints.clear();
			for(var i = -2; i < 2 ; i++){
				sf = num + seed + i; //Сид первой точки этой траектории или последней точки предыдущей траектории
				se = sf + 1;//Сид последней точки этой траектории
				sp = sf - 1;//Сид предыдущей первой точки траектории
				//Нам нужно сгенерировать точки...
				pref = generate(sp); //Предыдущая точка траектории, нужна только для направления
				from = generate(sf); //Первая точка нашей траектории
				to = generate(se); //Последняя точка нашей траектории=
				//Теперь нам нужны две вспомогательные точки:
				if(from.equals(to)){ //Если у нас начало и конец совпали
					final var d = Point.DIRECTION.toEnum((int) sf);
					C4 = Point.create(to.x - d.addX * lenght / 2,to.y + d.addY * lenght / 2);
				} else {
					C4 = from;
				}
				if(pref.equals(from)){ //Если у нас совпадает предущая последняя точка и эта первая
					final var d = Point.DIRECTION.toEnum((int) sp);
					C1 = Point.create(from.x - d.addX * lenght / 2,from.y + d.addY * lenght / 2);
				} else {
					C1 = pref;
				}
				final var ret = Arrays.asList(new Point[lenght]);
				regenerate(from,to,C1,C4,ret);
				lpoints.addAll(ret);
			}
		}
		return points.get((int) p);
	}
	
	private static class ColorGradient{
		/**Список всех уже посчитанных цветов*/
		private final Color[] colors = new Color[125];
		/**Создаёт круговой градиент
		 * @param from от какого цвета
		 * @param to к какому цвету
		 * @param isROYGBVR переход Красный-Оранжевый-Жёлтый-Зелёный-Голубой-Синий-Фиолетовый-Красный или обратный?
		 */
		public ColorGradient(Color from, Color to, boolean isROYGBVR){
			final float[] hsbfrom = new float[4];
			final float[] params = new float[4];
			Color.RGBtoHSB(from.getRed(), from.getGreen(), from.getBlue(), hsbfrom);
			hsbfrom[3] = from.getAlpha() / 255f;
			final var hsbto = new float[4];
			Color.RGBtoHSB(to.getRed(), to.getGreen(), to.getBlue(), hsbto);
			hsbto[3] = to.getAlpha() / 255f;
			if(hsbfrom[0] <= hsbto[0])
				params[0] = ((hsbto[0] - hsbfrom[0]) + (isROYGBVR ? 0 : -1)) / 100f;
			else
				params[0] = ((hsbto[0] - hsbfrom[0]) + (isROYGBVR ? 0 : +1)) / 100f;
			for (int i = 1; i < params.length; i++)
				params[i] = (hsbto[i] - hsbfrom[i]) / 100f;
			for (int i = 0; i < colors.length; i++) {
				colors[i] = getHSBColor(hsbfrom[0] + params[0] * i, hsbfrom[1] + params[1] * i, hsbfrom[2] + params[2] * i, hsbfrom[3] + params[3] * i);
			}
		}
		/**Возвращает один из цветов прогресса
		 * @param progress прогресс по шклае [0,1]. Может быть чуть больше 1, пока, до 1.5... Но лучше не заходить :)
		 * @return 
		 */
		public Color cyrcleGradient(double progress){
			final var p = Utils.Utils.betwin(0,(int) Math.round(progress*100),colors.length - 1);
			return colors[p];
		}
		private Color getHSBColor(float h, float s, float b, float a){
			while(h > 1)h -= 1f;
			while(h < 0)h += 1f;
			s = Utils.Utils.betwin(0f, s, 1f);
			b = Utils.Utils.betwin(0f, b, 1f);
			a = Utils.Utils.betwin(0f, a, 1f);
			return Utils.Utils.getHSBColor(h,s,b,a);
		}
	}
	@Override
	public void paint(Graphics2D g, WorldView.Transforms transform, int frame) {
		final var dashed = new java.awt.BasicStroke(1, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
		final var os = g.getStroke();
		g.setColor(AllColors.TRAJECTORY_POINT);
		g.setStroke(dashed);
		g.drawRect(transform.toScrinX(rectangle[0]), transform.toScrinY(rectangle[0]), transform.toScrin(rectangle[1].x-rectangle[0].x), transform.toScrin(rectangle[1].y-rectangle[0].y));
		g.setStroke(os);
		
		//Рисуем линии
		final var r = transform.toScrin(1);
		final var r2 = r*2;
		var fromP = lpoints.get(0);
		var fx = transform.toScrinX(fromP);
		var fy = transform.toScrinY(fromP);
		Utils.Utils.drawCircle(g, fx, fy, r);
		//g.setColor(AllColors.TRAJECTORY_LINE);
		ColorGradient HPColors = new ColorGradient(new Color(215,42,89,127),new Color(68,231,26,255), false);
		for (int i = 1; i < lpoints.size(); i++) {
			final var p = lpoints.get(i);
			final var x2 = transform.toScrinX(p);
			final var y2 = transform.toScrinY(p);
			if(Math.abs(fx - x2) <= r2 && Math.abs(fy - y2) <= r2){
				g.setColor(HPColors.cyrcleGradient((double) i / lpoints.size() ));
				g.drawLine(fx, fy, x2, y2);
			}
			fx = x2;
			fy = y2;
		}
		g.setColor(AllColors.TRAJECTORY_POINT);
		Utils.Utils.drawCircle(g, fx, fy, r2);
	}
}
