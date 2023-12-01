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
				j.add("leftUp",object.leftUp.toJSON());
				j.add("width",object.width);
				j.add("height",object.height);
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
				addParam(new ClassBuilder.Abstract2ConstructorParam(){
					@Override public int get1Minimum(){return 1;}
					@Override public int get1Default(){return Configurations.getWidth();}
					@Override public int get1Maximum(){return Integer.MAX_VALUE;}
					@Override public int get2Minimum(){return 1;}
					@Override public int get2Default(){return Configurations.getHeight();}
					@Override public int get2Maximum(){return Integer.MAX_VALUE;}
					@Override public String name() {return "wh";}
				});
			}
			@Override
			public TrajectoryRandom build() {
				return new TrajectoryRandom(getParam(0,Long.class),getParam(1,Long.class),getParam(2,Point.class),getParam(3,Point.class),getParam(4,Point.Vector.class).x,getParam(4,Point.Vector.class).y);
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
	/**Координаты верхнего левого угла прямоугольника, внутри которого генерируются клетки*/
	private final Point leftUp;
	/**Ширина, расстояние по X, ограничивающего приямоугольник*/
	private final int width;
	/**Высота, расстояние по Y, ограничивающего приямоугольник*/
	private final int height;
	
	
	
	//private final List<Point> lpoints = new ArrayList<>();
	
	/** * Создаёт линейную, траекторию от точки к точке.объект смещается каждый раз на 1 клетку мира
	 * @param speed скорость, в тиков на шаг
	 * @param seed уникальное зерно этой траектории
	 * @param start начальная точка траектории
	 * @param leftUp верхний левый ограничивающий угол
	 * @param width ширина, по оси X, ограничивающего прямоугольника
	 * @param height высота, по оси Y, ограничивающего прямоугольника
	 * 
	 */
	public TrajectoryRandom(long speed, long seed, Point start, Point leftUp, int width, int height){
		super(speed);
		this.seed = seed == 0 ? Long.MAX_VALUE : seed;
		this.start = start;
		this.leftUp = leftUp;
		this.width = width;
		this.height = height;
		
		lenght = (int)Math.ceil(Math.hypot(this.width, this.height));
		points = Arrays.asList(new Point[lenght]);
		position(Configurations.world.step); //Для генерации первых точек
	}
	protected TrajectoryRandom(JSON j, long version){
		super(j,version);
		seed = j.getL("SEED");
		start = Point.create(j.getJ("START"));
		leftUp = Point.create(j.getJ("leftUp"));
		this.width = j.get("width");
		this.height = j.get("height");
		
		lenght = (int)Math.ceil(Math.hypot(width, height));
		points = Arrays.asList(new Point[lenght]);
		position(Configurations.world.step); //Для генерации первых точек
	}

	private Point.Vector distance(Point.Vector from, Point.Vector to){
		return Point.Vector.create(to.x - from.x, to.y - from.y);
	}
	/**
	 * Создаёт траекторию постоянной длинны на основе 4х точек
	 * @param from первая точка траектории
	 * @param to последняя точка траектории
	 * @param C1 вспомогательная точка, для задания направления от начала траектории
	 * @param C4 вспомогательная точка, для задания направления ближе к концу линии
	 * @param mass массив, в которй будут записаны новые точки
	 */
	private void regenerate(Point.Vector from, Point.Vector to, Point.Vector C1, Point.Vector C4, List<Point> mass){
		final var F0 = 1;
		final var F1 = 1;
		final var F2 = 1 * 2;
		final var F3 = 1 * 2 * 3;
		final var F4 = 1 * 2 * 3 * 4;
		final var F5 = 1 * 2 * 3 * 4 * 5;
		
		final var isFromEqTo = from.equals(to);
		final var v = distance((isFromEqTo ? C4 : from), to);
		//Точка посреди лини для растяжения кривой до нужных размеров
		final var center = isFromEqTo ? C4.add(v.divide(2)) : from.add(v.divide(2));
		final double lx = isFromEqTo ? width : (width - Math.abs(v.x)); //Нераспределённая длинна траектории
		final double ly = isFromEqTo ? height : (height - Math.abs(v.y)); //Нераспределённая длинна траектории
		final var isUp = switch(v.direction()){case RIGHT,DOWN_R,DOWN,DOWN_L -> false; case LEFT,UP_L,UP,UP_R -> true;}; //Вверх смотрит вектор или вниз?
		//Вектор от центра к углу ограничивающего квадрата
		final var V2 = distance(center, isUp ? Point.Vector.create(leftUp.x + width, leftUp.y) : Point.Vector.create(leftUp.x + width, leftUp.y + height));
		final var V3 = distance(center, isUp ? Point.Vector.create(leftUp.x, leftUp.y + height) : Point.Vector.create(leftUp.x, leftUp.y));
		
		final var C2 = Point.Vector.create((int) Math.round(center.x + lx * V2.x / width),(int) Math.round(center.y + ly * V2.y / height));
		final var C3 = Point.Vector.create((int) Math.round(center.x + lx * V3.x / width),(int) Math.round(center.y + ly * V3.y / height));
		
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
	private Point.Vector generate(long index){
		if(index == 0) return Point.Vector.create(start.x, start.y);
		final var x = (int) (leftUp.x + Utils.Utils.randomByHash(index,width));
		final var y = (int) (leftUp.y + Utils.Utils.randomByHash(Utils.Utils.hashCode(index),height)); //Тут двойное хэширование, чтобы x и y различались!
		return Point.Vector.create(x, y);
	}
	@Override
	protected Point position(long wstep) {
		var num = wstep / lenght;
		final var p = wstep % lenght;
		if(num != number){
			number = num;
			var sf = num*seed; //Сид первой точки этой траектории или последней точки предыдущей траектории
			var se = sf + seed;//Сид последней точки этой траектории
			var sp = sf - seed;//Сид предыдущей первой точки траектории
			//Нам нужно сгенерировать точки...
			var pref = generate(sp); //Предыдущая точка траектории, нужна только для направления
			var from = generate(sf); //Первая точка нашей траектории
			var to = generate(se); //Последняя точка нашей траектории
			Point.Vector C4, C1;
			//Теперь нам нужны две вспомогательные точки:
			if(from.equals(to)){ //Если у нас начало и конец совпали
				final var d = Point.DIRECTION.toEnum((int) sf);
				C4 = Point.Vector.create(to.x - d.addX,to.y + d.addY);
			} else {
				C4 = from;
			}
			if(pref.equals(from)){ //Если у нас совпадает предущая последняя точка и эта первая
				final var d = Point.DIRECTION.toEnum((int) sp);
				C1 = Point.Vector.create(from.x - d.addX,from.y + d.addY);
			} else {
				C1 = pref;
			}
			regenerate(from,to,C1,C4,points);
			/*var speed = Point.Vector.create(0, 0);
			
			for (int i = 0; i < points.size() - 1; i++) {
				final var p1 = points.get(i);
				final var p2 = points.get(i+1);
				final var d1 = p1.distance(p2);
				speed = Point.Vector.create(speed.x + Math.abs(d1.x), speed.y + Math.abs(d1.y));
			}
			System.out.printf("Путь - %04.2f, скорость - %04.2f :[%04.2f:%04.2f] \n" ,speed.getHypotenuse(), (speed.getHypotenuse() / points.size()) , (((double)speed.x)/points.size()) ,(((double)speed.y)/points.size()));
			
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
					C4 = Point.Vector.create(to.x - d.addX * lenght / 2,to.y + d.addY * lenght / 2);
				} else {
					C4 = from;
				}
				if(pref.equals(from)){ //Если у нас совпадает предущая последняя точка и эта первая
					final var d = Point.DIRECTION.toEnum((int) sp);
					C1 = Point.Vector.create(from.x - d.addX * lenght / 2,from.y + d.addY * lenght / 2);
				} else {
					C1 = pref;
				}
				final var ret = Arrays.asList(new Point[lenght]);
				regenerate(from,to,C1,C4,ret);
				lpoints.addAll(ret);
			}*/
		}
		return points.get((int) p);
	}
	
	@Override
	public void paint(Graphics2D g, WorldView.Transforms transform, int frame) {
		final var dashed = new java.awt.BasicStroke(1, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
		final var os = g.getStroke();
		g.setColor(AllColors.TRAJECTORY_POINT);
		g.setStroke(dashed);
		g.drawRect(transform.toScrinX(leftUp.x), transform.toScrinY(leftUp.y), transform.toScrin(width), transform.toScrin(height));
		g.setStroke(os);
		
		//Рисуем линии
		final var r = transform.toScrin(1);
		final var r2 = r*2;
		var fromP = points.get(0);
		var fx = transform.toScrinX(fromP);
		var fy = transform.toScrinY(fromP);
		Utils.Utils.drawCircle(g, fx, fy, r);
		g.setColor(AllColors.TRAJECTORY_LINE);
		for (int i = 1; i < points.size(); i++) {
			final var p = points.get(i);
			final var x2 = transform.toScrinX(p);
			final var y2 = transform.toScrinY(p);
			if(Math.abs(fx - x2) <= r2 && Math.abs(fy - y2) <= r2){
				g.drawLine(fx, fy, x2, y2);
			}
			fx = x2;
			fy = y2;
		}
		g.setColor(AllColors.TRAJECTORY_POINT);
		Utils.Utils.drawCircle(g, fx, fy, r);
	}
}
