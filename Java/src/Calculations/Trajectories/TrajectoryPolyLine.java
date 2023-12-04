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
import java.util.List;

/**
 * Линейная траектория состоящая из набора точек
 * Осуществляет движение от точки к точке
 * @author Kerravitarr
 */
public class TrajectoryPolyLine extends Trajectory{
	static{
		final var builder = new ClassBuilder<TrajectoryPolyLine>(){
			@Override public TrajectoryPolyLine generation(JSON json, long version){return new TrajectoryPolyLine(json, version);}
			@Override public JSON serialization(TrajectoryPolyLine object) { return object.toJSON();}

			@Override public String serializerName() {return "Полилиния";}
			@Override public Class printName() {return TrajectoryPolyLine.class;}

		};
		builder.addConstructor(new ClassBuilder.Constructor<TrajectoryPolyLine>(){
			{
				addParam(new ClassBuilder.NumberConstructorParamAdapter("super.speed", 0,500,1000,0,null));
				addParam(new ClassBuilder.BooleanConstructorParam(){
					@Override public Object getDefault() {return false;}
					@Override public String name() {return "isJamp";}
					
				});
				addParam(new ClassBuilder.MapPointVectorConstructorParam(){
					@Override public Point[] getDefault() {return new Point[]{Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2)};}
					@Override public String name() {return "points";}
				});
			}
			@Override
			public TrajectoryPolyLine build() {
				return new TrajectoryPolyLine(getParam(0,Integer.class),getParam(1,Boolean.class),getParam(2,Point[].class));
			}
			@Override public String name() {return "";}
		});
		Trajectory.register(builder);
	}
	
	/**Все точки траектории*/
	private final List<Interval> points;
	/**Суммарная длина отрезка по которому двигаемся. В клетках поля*/
	private final double lenght;
	
	/**Интервал перемещения*/
	private class Interval{
		/**Откуда двигаемся*/
		private final Point from;
		/**Длина отрезка по которому двигаемся. В клетках поля*/
		private final double lenght;
		/**На сколько должны сдвинуться на каждый шаг*/
		private final double dx;
		/**На сколько должны сдвинуться на каждый шаг*/
		private final double dy;

		private Interval(Point from, Point to) {
			this.from = from;
			var d = from.distance(to);
			lenght = d.getHypotenuse();
			if(lenght != 0){
				dx = d.x / lenght;
				dy = d.y / lenght;
			} else {
				dx = dy = 0;
			}
		}

		private Interval(JSON j) {
			lenght = j.get("lenght");
			dx = j.get("dx");
			dy = j.get("dy");
			from = Point.create(j.getJ("from"));
		}

		private JSON toJSON() {
			final var j = new JSON();
			j.add("from", from.toJSON());
			j.add("lenght", lenght);
			j.add("dx", dx);
			j.add("dy", dy);
			return j;
		}
	}
	
	
	
	/** * Создаёт линейную, траекторию от точки к точке.объект смещается каждый раз на 1 клетку мира
	 * @param speed скорость, в тиков на шаг
	 * @param isJamp
	 *		Если false, то 1-2-3-4-1-2-3-4...
	 *		А вот если true, то он будет двигаться от точки 1 к точке 2, затем прыгнет к точке 3 и будет двигаться к точке 4 и так далее.
	 *		Если точек будет чётное количество, то тут всё легко. 1-2 3-4 1-2 3-4... 
	 *			Если нужно какой-то прыжок пропустить, можно точку продублировать. Но следует понимать, что в таком случае точек станет нечётное число
	 *			И возникнет следующий случай
	 *		Если точек будет нечётное количество, то движение осуществляется кусочками: 1-2 3-4 5-1 2-3 4-5 1-2 3-4
	 * @param points наборт точек по которым движется объект. При этом, самая первая точка будет считаться и начальной
	 */
	public TrajectoryPolyLine(long speed,boolean isJamp, Point ... points){
		super(speed);
		if(!isJamp){
			this.points = new ArrayList(points.length + 1);
			for (int i = 0; i < points.length - 1; i++) {
				this.points.add(new Interval(points[i],points[i+1]));
			}
			this.points.add(new Interval(points[points.length - 1],points[0]));
		} else if(points.length % 2 == 0) {
			this.points = new ArrayList(points.length / 2);
			for (int i = 0; i < points.length - 1; i+=2) {
				this.points.add(new Interval(points[i],points[i+1]));
			}
		} else {
			this.points = new ArrayList(points.length + 1);
			for (int i = 0; i < points.length - 1; i+=2) {
				this.points.add(new Interval(points[i],points[i+1]));
			}
			this.points.add(new Interval(points[points.length - 1],points[0]));
			for (int i = 1; i < points.length - 1; i+=2) {
				this.points.add(new Interval(points[i],points[i+1]));
			}
		}
		lenght = this.points.stream().reduce(0d, (a,b) -> a+b.lenght,Double::sum);
		
	}
	protected TrajectoryPolyLine(JSON j, long version){
		super(j,version);
		points = j.getAJ("points").stream().map(p -> new Interval(p)).toList();
		lenght = this.points.stream().reduce(0d, (a,b) -> a+b.lenght,Double::sum);
	}

	@Override
	protected Point position(long wstep) {
		if(lenght == 0d){
			return points.get(0).from;
		} else {
			final var x = (int) (wstep / lenght);
			var step = wstep - lenght * x;
			for(var point : points){
				if(step > point.lenght){
					step -= point.lenght;
				} else {
					return Point.create((int)(point.from.getX() + point.dx * step), (int) (point.from.getY() + point.dy*step));
				}
			}
			throw new UnknownError("Мы сюда вообще не можем дойти...");
		}
	}
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("points", points.stream().map(p -> p.toJSON()).toList());
		return j;
	}
	
	@Override
	protected void paint(Graphics2D g, WorldView.Transforms transform, int frame, int dx, int dy) {
		final var r = transform.toScrin(1);
		final var r2 = r*2;
		//Рисуем точки
		g.setColor(AllColors.TRAJECTORY_POINT);
		for(final var i : points){
			final var fx = i.from.x + dx;
			final var fy = i.from.y + dy;
			final var x1 = transform.toScrinX(fx);
			final var y1 = transform.toScrinY(fy);
			final var x2 = transform.toScrinX(fx + i.dx*i.lenght);
			final var y2 = transform.toScrinY(fy + i.dy*i.lenght);
			Utils.Utils.fillCircle(g, x1, y1, r);
			Utils.Utils.fillCircle(g, x2, y2, r);
		}
		
		//Рисуем линии
		var fromP = position(0);
		var fx = transform.toScrinX(fromP.x+dx);
		var fy = transform.toScrinY(fromP.y+dy);
		g.setColor(AllColors.TRAJECTORY_LINE);
		for (int i = 1; i < lenght; i++) {
			final var p = position(i);
			final var x2 = transform.toScrinX(p.x+dx);
			final var y2 = transform.toScrinY(p.y+dy);
			if(Math.abs(fx - x2) <= r2 && Math.abs(fy - y2) <= r2)
				g.drawLine(fx, fy, x2, y2);
			fx = x2;
			fy = y2;
			fromP = p;
		}
		
	}
}
