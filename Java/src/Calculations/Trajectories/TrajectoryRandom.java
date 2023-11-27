/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Trajectories;

import Calculations.Point;
import GUI.WorldView;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Траектория движения по случайным точкам
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
				addParam(new ClassBuilder.NumberConstructorParamAdapter("super.speed", 0,500,1000,0,null));
			}
			@Override
			public TrajectoryRandom build() {
				return null; //new TrajectoryRandom(getParam(0,Integer.class));
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
		this.seed = seed;
		this.start = start;
		rectangle[0] = leftUp;
		rectangle[1] = rightDown;
		lenght = (int)Math.ceil(Math.hypot(rectangle[0].x - rectangle[1].x, rectangle[0].y - rectangle[1].y));
		points = Arrays.asList(new Point[lenght]);
	}
	protected TrajectoryRandom(JSON j, long version){
		super(j,version);
		seed = j.getL("SEED");
		start = Point.create(j.getJ("START"));
		rectangle[0] = Point.create(j.getJ("LU"));
		rectangle[1] = Point.create(j.getJ("RD"));
		lenght = (int)Math.ceil(Math.hypot(rectangle[0].x - rectangle[1].x, rectangle[0].y - rectangle[1].y));
		points = Arrays.asList(new Point[lenght]);
	}
	/**
	 * Создаёт точку траектории по её индексу
	 * @param index порядковый номер точки
	 * @return точка на карте
	 */
	private Point generate(long index){
		if(index == 0) return start;
		final var x = (int) Utils.Utils.randomHash(index,rectangle[0].x, rectangle[1].x+1);
		final var y = (int) Utils.Utils.randomHash(index,rectangle[0].y, rectangle[1].y+1);
		return Point.create(x, y);
	}
	@Override
	protected Point position(long wstep) {
		final var num = wstep / lenght;
		final var p = wstep % lenght;
		if(num != number){
			//Нам нужно сгенерировать точки...
			final var trajP = new Point[4]; //4 реперные точки, через которые обязана пройти траектория
			final var pref = generate(num-1);
			trajP[1] = generate(num); //Первая точка траектории
			trajP[3] = generate(num+1); //Последняя точка траектории
			if(pref.equals(trajP[1])){
				trajP[0] = trajP[1].next(Point.DIRECTION.toEnum((int) num));
			} else {
				trajP[0] =  trajP[1].next(trajP[1].distance(pref).direction());
			}
			if(trajP[3].equals(trajP[1])){
				trajP[2] = trajP[3].next(Point.DIRECTION.toEnum((int) num+1));
			} else {
				trajP[2] = trajP[3].next(trajP[3].distance(trajP[1]).direction());
			}
			points.set(0, trajP[1]);
			points.set(lenght - 1, trajP[3]);
			points.set(lenght - 2, trajP[2]);
			
			number = num;
		}
		return points.get((int) p);
	}
	
	@Override
	public void paint(Graphics2D g, WorldView.Transforms transform) {
	}
}
