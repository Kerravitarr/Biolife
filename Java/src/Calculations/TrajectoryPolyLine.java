/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import Utils.JSON;
import java.util.ArrayList;
import java.util.List;

/**
 * Линейная траектория состоящая из набора точек
 * Осуществляет движение от точки к точке
 * @author Kerravitarr
 */
public class TrajectoryPolyLine extends Trajectory{
	/**Шаг объекта. Определяющее на каком расстоянии от from находится точка*/
	private double step = 0;
	/**Все точки траектории*/
	private final List<Interval> points;
	/**Индекс текущей точки траектории*/
	private int pointIndex;
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
			dx = d.x / lenght;
			dy = d.y / lenght;
		}

		private Interval(JSON j) {
			lenght = j.get("lenght");
			dx = j.get("dx");
			dy = j.get("dy");
			from = new Point(j.getJ("from"));
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
	
	
	/**Создаёт линейную, зацикленную траекторию от точки к точке.
	 * объект смещается каждый раз на 1 клетку мира
	 * @param speed скорость, в тиков на шаг
	 * @param points наборт точек по которым движется объект. При этом, самая первая точка будет считаться и начальной
	 */
	public TrajectoryPolyLine(long speed, Point ... points){
		super(speed, points[0]);
		this.points = new ArrayList(points.length);
		for (int i = 0; i < points.length - 1; i++) {
			this.points.add(new Interval(points[i],points[i+1]));
		}
		this.points.add(new Interval(points[points.length - 1],points[0]));
		lenght = this.points.stream().reduce(0d, (a,b) -> a+b.lenght,Double::sum);
		pointIndex = 0;
	}
	protected TrajectoryPolyLine(JSON j, long version){
		super(j,version);
		step = j.get("step");
		pointIndex = j.get("pointIndex");
		points = j.getAJ("points").stream().map(p -> new Interval(p)).toList();
		lenght = this.points.stream().reduce(0d, (a,b) -> a+b.lenght,Double::sum);
	}

	@Override
	protected Point step() {
		step++;
		var point = points.get(pointIndex);
		if(step > point.lenght){
			step -= point.lenght;
			if(pointIndex >= points.size() - 1){
				pointIndex = 0;
			} else {
				pointIndex++;
			}
			point = points.get(pointIndex);
		}
		return new Point((int)(point.from.getX() + point.dx * step), (int) (point.from.getY() + point.dy*step));
	}
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("step", step);
		j.add("pointIndex", pointIndex);
		j.add("points", points.stream().map(p -> p.toJSON()).toList());
		return j;
	}
}
