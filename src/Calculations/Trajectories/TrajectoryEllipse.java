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

/**
 * Движение по эллипсу вокруг некоторого центра.
 * @author Kerravitarr
 */
public class TrajectoryEllipse extends Trajectory{
	static{
		final var builder = new ClassBuilder<TrajectoryEllipse>(){
			@Override public TrajectoryEllipse generation(JSON json, long version){return new TrajectoryEllipse(json, version);}
			@Override public JSON serialization(TrajectoryEllipse object) { return object.toJSON();}

			@Override public String serializerName() {return "Эллипс";}
			@Override public Class printName() {return TrajectoryEllipse.class;}
		};
		builder.addParam(new ClassBuilder.Abstract2Param<TrajectoryEllipse>() {
			@Override public int get1Minimum() {return 1;}
			@Override public int get1Default() {return get1Maximum() / 2;}
			@Override public int get1Maximum() {return Configurations.getWidth();}
			@Override public int get2Minimum() {return 1;}
			@Override public int get2Default() {return get2Maximum() / 2;}
			@Override public int get2Maximum() {return Configurations.getHeight();}
			@Override public Point.Vector get(TrajectoryEllipse who) {return Point.Vector.create((int)(who.a*2), (int)(who.b*2));}
			@Override public void setValue(TrajectoryEllipse who, Point.Vector value) {who.a = value.x/2d; who.b = value.y/2d;}
			@Override public String name() {return "AB";}
		});
		builder.addParam(new ClassBuilder.MapPointParam<TrajectoryEllipse>(){
			@Override public Point get(TrajectoryEllipse who) {return who.center;}
			@Override public void setValue(TrajectoryEllipse who, Point value) {who.center = value;}
			@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
			@Override public String name() {return "center";};
			
		});
		builder.addParam(new ClassBuilder.NumberParamAdapter<Double,TrajectoryEllipse>("startAngle",0d , 0d, 360d, 0d, 360d){
			@Override public Double get(TrajectoryEllipse who) { return who.angle;}
			@Override public void setValue(TrajectoryEllipse who, Double value) {who.angle = value;}
		});
		builder.addParam(new ClassBuilder.NumberParamAdapter<Long,TrajectoryEllipse>("super.speed",-1000L , 500L, 1000L, null, null){
			@Override public Long get(TrajectoryEllipse who) { return who.getSpeed();}
			@Override public void setValue(TrajectoryEllipse who, Long value) {who.setSpeed(value); who.clockwise = value > 0; }
		});
		
		final var speed = new ClassBuilder.NumberConstructorParamAdapter("super.speed",-1000,500,1000,null,null);
		final var center = new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "parameter.center";}
				};
		final var startAngle = new ClassBuilder.NumberConstructorParamAdapter("parameter.startAngle",-360,0,360,-360,360);
		builder.addConstructor(new ClassBuilder.Constructor<TrajectoryEllipse>(){
			{
				addParam(speed);
				addParam(center);
				addParam(startAngle);
				addParam(new ClassBuilder.NumberConstructorParamAdapter("a2", 0, 0, 0, 0, null){
					@Override public Integer getDefault() {return getSliderMaximum() / 2;}
					@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("b2", 0, 0, 0, 0, null){
					@Override public Integer getDefault() {return getSliderMaximum()/ 2;}
					@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
				});
			}
			@Override
			public TrajectoryEllipse build() {
				return new TrajectoryEllipse(getParam(0,Integer.class),getParam(1,Point.class),Math.toRadians(getParam(2,Integer.class)),getParam(3,Integer.class),getParam(4,Integer.class));
			}
			@Override public String name() {return "ellipse";}
		});
		
		builder.addConstructor(new ClassBuilder.Constructor<TrajectoryEllipse>(){
			{
				addParam(speed);
				addParam(center);
				addParam(startAngle);
				addParam(new ClassBuilder.NumberConstructorParamAdapter("d", 0,0,0,0,null){
					@Override public Integer getDefault() {return getSliderMaximum()/ 2;}
					@Override public Integer getSliderMaximum() {return Math.min(Configurations.getWidth(), Configurations.getHeight());}
				});
			}
			@Override
			public TrajectoryEllipse build() {
				return new TrajectoryEllipse(getParam(0,Integer.class),getParam(1,Point.class),Math.toRadians(getParam(2,Integer.class)),getParam(3,Integer.class));
			}
			@Override public String name() {return "circle";}
		});
		Trajectory.register(builder);
	}
	
	/**Центр, вокруг которого вращаемся*/
	private Point center;
	/**Начальный угол, от которого меряется всё остальное*/
	private double angle;
	/**Большая ось*/
	private double a;
	/**Малая ось*/
	private double b;
	/**Направление*/
	private boolean clockwise;
	
	
	/**Движение по орбите
	 * @param speed скорость движения. Как часто солнце будет шагать. При 1 - каждый раз, при 2 - каждые 2 хода и т.д.
	 *				с учётом, что весь круг занимает 360 шагов солнца
	 * <br>Если скорость положительная - движение по часовой стрелке
	 * <br>Если скорость отрицательная - движение против часовой стрелке
	 * @param center центр, вокруг которого будем крутиться
	 * @param startAngle начальный угол
	 * @param a2 большая ось эллипса - лежит на оси Х
	 * @param b2 малая ось эллипса - лежит на оси Y
	 */
	public TrajectoryEllipse(long speed, Point center,double startAngle, int a2, int b2){
		super(Math.abs(speed));
		this.center = center;
		this.angle = startAngle;
		this.a = a2 / 2d;
		this.b = b2 / 2d;
		clockwise = speed > 0;
	}
	/**Движдение объекта по кругу
	 * @param speed скорость движения. Как часто солнце будет шагать. При 1 - каждый раз, при 2 - каждые 2 хода и т.д.
	 *				с учётом, что весь круг занимает 360 шагов солнца
	 * <br>Если скорость положительная - движение по часовой стрелке
	 * <br>Если скорость отрицательная - движение против часовой стрелке
	 * @param center центр, вокруг которого будем крутиться
	 * @param startAngle начальный угол
	 * @param d диаметр круга, по которому солнце будет летать
	 */
	public TrajectoryEllipse(long speed, Point center,double startAngle, int d){
		this(speed, center, startAngle, d, d);
	}
	protected TrajectoryEllipse(JSON json, long version){
		super(json,version);
		this.center = Point.create(json.getJ("center"));
		this.angle = json.get("angle");
		this.a = json.get("a");
		this.b = json.get("b");
		this.clockwise = json.get("clockwise");
	}

	@Override
	protected Point position(long step) {
		final var rangle = angle + (clockwise ? (step * Math.PI / 180d) : -(step * Math.PI / 180d));
		return Point.create((int)Math.round(center.getX() + a * Math.cos(rangle)), (int)Math.round(center.getY() +  b * Math.sin(rangle)));
	}
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("angle", angle);
		j.add("center", center.toJSON());
		j.add("a", a);
		j.add("b", b);
		j.add("clockwise", clockwise);
		return j;
	}
	
	@Override
	protected void paint(Graphics2D g, WorldView.Transforms transform, int frame, int dx, int dy) {
		final var dashed = new java.awt.BasicStroke(1, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
		final var os = g.getStroke();
		g.setColor(AllColors.TRAJECTORY_POINT);
		g.setStroke(dashed);
		final var cxp = center.getX() + dx;
		final var cyp = center.getY() + dy;
		//Рисуем опорные лини
		final var cx = transform.toScrinX(cxp);
		final var cy = transform.toScrinY(cyp);
		final var a2 = transform.toScrin(a*2);
		final var b2 = transform.toScrin(b*2);
		final var ub = cy - b2/2;
		final var db = cy + b2/2;
		g.drawLine(cx, ub, cx, db);
		final var la = cx - a2/2;
		final var ra = cx + a2/2;
		g.drawLine(la, cy, ra, cy);
		
		g.setStroke(os);
		//Теперь точка центра
		final var r = transform.toScrin(1);
		Utils.Utils.fillCircle(g, cx, cy, r);
		//А теперь окружающий эллипс
		g.setColor(AllColors.TRAJECTORY_LINE);
		g.drawOval(cx-a2/2, cy-b2/2, a2, b2);
		//И начальный угол
		final var sx = transform.toScrinX(cxp + a * Math.cos(angle));
		final var sy = transform.toScrinY(cyp + b * Math.sin(angle));
		g.drawLine(cx, cy, sx, sy);	
	}
}