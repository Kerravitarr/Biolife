/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

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
		final var speed = new ClassBuilder.NumberConstructorParamAdapter("super.speed",0,500,1000,0,null);
		final var center = new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "constructor.center";}
				};
		final var startAngle = new ClassBuilder.NumberConstructorParamAdapter("constructor.startAngle",-360,0,360,-360,360);
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
	private final Point center;
	/**Эксцентрическая аномалия эллипса. Угол, на который сместилось солнце от начала, Дополнительный параметр для смещения начального угла*/
	private double angle;
	/**Большая ось*/
	private final double a;
	/**Малая ось*/
	private final double b;
	
	
	/**Движение по орбите
	 * @param speed скорость движения. Как часто солнце будет шагать. При 1 - каждый раз, при 2 - каждые 2 хода и т.д.
	 *				с учётом, что весь круг занимает 360 шагов солнца
	 * @param center центр, вокруг которого будем крутиться
	 * @param startAngle начальный угол
	 * @param a2 большая ось эллипса - лежит на оси Х
	 * @param b2 малая ось эллипса - лежит на оси Y
	 */
	public TrajectoryEllipse(long speed, Point center,double startAngle, int a2, int b2){
		super(speed);
		this.center = center;
		this.angle = startAngle;
		this.a = a2 / 2d;
		this.b = b2 / 2d;
	}
	/**Движдение объекта по кругу
	 * @param speed скорость движения. Как часто солнце будет шагать. При 1 - каждый раз, при 2 - каждые 2 хода и т.д.
	 *				с учётом, что весь круг занимает 360 шагов солнца
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
	}

	@Override
	protected Point position(long step) {
		final var rangle = angle + step * Math.PI / 180d;
		return Point.create((int)Math.round(center.getX() + a * Math.cos(rangle)), (int)Math.round(center.getY() +  b * Math.sin(rangle)));
	}
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("angle", angle);
		j.add("center", center.toJSON());
		j.add("a", a);
		j.add("b", b);
		return j;
	}
	
	@Override
	public void paint(Graphics2D g, WorldView.Transforms transform) {
		int r = transform.toScrin(1);
		int a2 = transform.toScrin((int)(a*2));
		int b2 = transform.toScrin((int)(b*2));
		int cx = transform.toScrinX(center);
		int cy = transform.toScrinY(center);
		int sx = transform.toScrinX((int)(center.getX() + a * Math.cos(angle)));
		int sy = transform.toScrinY((int)(center.getY() + b * Math.sin(angle)));
		g.setColor(AllColors.TRAJECTORY_POINT);
		Utils.Utils.fillCircle(g, cx, cy, r);
		g.drawLine(cx-a2/2, cy-b2/2, sx, sy);
		g.setColor(AllColors.TRAJECTORY_LINE);
		g.drawOval(cx-a2/2, cy-b2/2, a2, b2);
	}
}
