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
		lenght = (int)Math.ceil(Math.hypot(rectangle[0].x - rectangle[1].x, rectangle[0].y - rectangle[1].y));
		points = Arrays.asList(new Point[lenght]);
		position(Configurations.world.step); //Для генерации первых точек
	}
	protected TrajectoryRandom(JSON j, long version){
		super(j,version);
		seed = j.getL("SEED");
		start = Point.create(j.getJ("START"));
		rectangle[0] = Point.create(j.getJ("LU"));
		rectangle[1] = Point.create(j.getJ("RD"));
		lenght = (int)Math.ceil(Math.hypot(rectangle[0].x - rectangle[1].x, rectangle[0].y - rectangle[1].y));
		points = Arrays.asList(new Point[lenght]);
		position(Configurations.world.step); //Для генерации первых точек
	}
	/**
	 * Создаёт траекторию постоянной длинны на основе 4х точек
	 * @param from первая точка траектории
	 * @param to последняя точка траектории
	 * @param C1 вспомогательная точка, для задания направления от начала траектории
	 * @param C4 вспомогательная точка, для задания направления ближе к концу линии
	 */
	private void regenerate(Point from, Point to, Point C1, Point C4){
		final var C2 = C1; //Точка посреди лини для растяжения кривой до нужных размеров
		final var C3 = C1; //Точка посреди лини для растяжения кривой до нужных размеров
		
		for(var i = 0.0 ; i < lenght; i++){
			final var t = i/lenght;
			
			
			final var t2 = t * t;
			final var t3 = t2 * t;
			final var nt = 1.0 - t;
			final var nt2 = nt * nt;
			final var nt3 = nt2 * nt;
			//final var x = nt3 * P0.x + 3.0 * t * nt2 * P1.x + 3.0 * t2 * nt * P2.x + t3 * P3.x;
			//final var y = nt3 * P0.y + 3.0 * t * nt2 * P1.y + 3.0 * t2 * nt * P2.y + t3 * P3.y;
			//points.set((int)i, Point.create((int)Math.round(x),(int)Math.round(y)));
		}
	}
	private List<Point> regenerate2(Point preFrom, Point from, Point to, Point postTo){
		final var EPSILON = 1.0e-5;
		final var C = 20.0;
		
		final var nextI = Point.distance(preFrom, from).normalize();
		final var cur0 = nextI;
		final var next0 = Point.distance(from, to).normalize();
		final var tgR0 = cur0.add(next0).normalize();
		
		final var tgL1 = tgR0;
		final var cur1 = next0;
		final var deltaL1 = next0;
		final var next1 = Point.distance(to, postTo).normalize();
		final var tgR1 = cur1.add(next1).normalize();
		final var deltaR1 = Point.distance(to, postTo);
		final var l1 = Math.abs(tgL1.x) < EPSILON ? 0.0 : deltaL1.x / (C * tgL1.x);
        final var l2 = Math.abs(tgR1.x) < EPSILON ? 0.0 : deltaR1.x / (C * tgR1.x);
		
		final var P0 = from;
		final var P1 = tgL1.multiply(l1).add(from);
		final var P3 = to;
		final var P2 = new Point.PointD(to.x,to.y).add(tgR1.multiply(-l2));
		final var ret = new ArrayList<Point>(lenght);
		for(var i = 0.0 ; i < lenght; i++){
			final var t = i/lenght;
			final var t2 = t * t;
			final var t3 = t2 * t;
			final var nt = 1.0 - t;
			final var nt2 = nt * nt;
			final var nt3 = nt2 * nt;
			final var x = nt3 * P0.x + 3.0 * t * nt2 * P1.x + 3.0 * t2 * nt * P2.x + t3 * P3.x;
			final var y = nt3 * P0.y + 3.0 * t * nt2 * P1.y + 3.0 * t2 * nt * P2.y + t3 * P3.y;
			ret.add((int)i, Point.create((int)Math.round(x),(int)Math.round(y)));
		}
		return ret;
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
			var snum = num + seed; //Для участия сида в генерации
			var psnum = snum - 1; //Предыдущий сид
			var nsnum = snum + 1; //Следующий сид
			var nnsnum = nsnum + 1; //После следующий сид
			//Нам нужно сгенерировать точки...
			var pref = generate(psnum); //Предыдущая точка траектории
			var P1 = generate(snum); //Первая точка нашей траектории
			var P2 = generate(nsnum); //Последняя точка нашей траектории
			var next = generate(nnsnum); //Точка за нашей траекторией
			//regenerateT(pref,P1,P2,next);
			
			lpoints.clear();
			for(var i = -2; i < 2 ; i++){
				snum = num + seed + i; //Для участия сида в генерации
				psnum = snum - 1; //Предыдущий сид
				nsnum = snum + 1; //Следующий сид
				nnsnum = nsnum + 1; //После следующий сид
				pref = generate(psnum); //Предыдущая точка траектории
				P1 = generate(snum); //Первая точка нашей траектории
				P2 = generate(nsnum); //Последняя точка нашей траектории
				next = generate(nnsnum); //Точка за нашей траекторией
				lpoints.addAll(regenerate2(pref,P1,P2,next));
			}
		}
		return points.get((int) p);
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
		g.setColor(AllColors.TRAJECTORY_LINE);
		for (int i = 1; i < lpoints.size(); i++) {
			final var p = lpoints.get(i);
			final var x2 = transform.toScrinX(p);
			final var y2 = transform.toScrinY(p);
			if(Math.abs(fx - x2) <= r2 && Math.abs(fy - y2) <= r2)
				g.drawLine(fx, fy, x2, y2);
			fx = x2;
			fy = y2;
		}
		g.setColor(AllColors.TRAJECTORY_POINT);
		Utils.Utils.drawCircle(g, fx, fy, r2);
	}
}
