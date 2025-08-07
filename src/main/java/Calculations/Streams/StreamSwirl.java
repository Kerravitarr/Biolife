/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Streams;

import Calculations.Configurations;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Graphics2D;

/**
 * Круглый поток, представляющий собой циклическое движение
 * @author Kerravitarr
 */
public class StreamSwirl extends StreamAbstract {
	static{
		final var builder = new ClassBuilder<StreamSwirl>(){
			@Override public StreamSwirl generation(JSON json, long version){return new StreamSwirl(json, version);}
			@Override public JSON serialization(StreamSwirl object) { return object.toJSON();}

			@Override public String serializerName() {return "Круговорот";}
			@Override public Class printName() {return StreamSwirl.class;}

		};
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,StreamSwirl>("d",0,0,0,0,null) {
			@Override public Integer get(StreamSwirl who) {return (int)(who.r*2);}
			@Override public void setValue(StreamSwirl who, Integer value) {who.setR(value);}
			@Override public Integer getDefault() {return Math.min(Configurations.getWidth(),Configurations.getHeight())/2;}
			@Override public Integer getSliderMaximum() {return Math.min(Configurations.getWidth(),Configurations.getHeight());}
		});
		final var center = new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "center";}
				};
		final var power = new ClassBuilder.NumberConstructorParamAdapter("super.power",-1000,1,1000,null,null);
		final var name = new ClassBuilder.StringConstructorParam(){
				@Override public Object getDefault() {return "Водоворот";}
				@Override public String name() { return "super.name";}

			};
		builder.addConstructor(new ClassBuilder.Constructor<StreamSwirl>(){
			{
				addParam(center);
				addParam(new ClassBuilder.NumberConstructorParamAdapter("d",0,0,0,0,null){
					@Override public Integer getDefault() {return getSliderMaximum()/2;}
					@Override public Integer getSliderMaximum() {return Math.min(Configurations.getWidth(),Configurations.getHeight());}
				});
				addParam(power);
				addParam(name);
			}

			@Override
			public StreamSwirl build() {
				return new StreamSwirl(new Trajectory(getParam(0,Point.class)), getParam(1,Integer.class), getParam(2,Integer.class), getParam(3,String.class));
			}
			@Override public String name() {return "";}
		});
		StreamAbstract.register(builder);
	}
	
	/**Диаметр потока*/
	private double r;
	
	/**Создание потока
	 * Об энергии:Если больше 0, то клетку будет толкать против часовой стрелки
	 *			если меньше 0, то по часовой
	 * @param move движение ЦЕНТРА этого потока
	 * @param d диаметр круга
	 * @param shadow тип снижения мощности от максимума к минимуму
	 * @param name имя, как будут звать этот поток
	 */
	public StreamSwirl(Trajectory move, int d, StreamAttenuation shadow,String name) {
		super(move, shadow, name);
		setR(d/2d);
	}

	/**Создание потока
	 * Об энергии:Если больше 0, то клетку будет толкать против часовой стрелки
	 *			если меньше 0, то по часовой
	 * @param move движение ЦЕНТРА этого потока
	 * @param d диаметр круга
	 * @param power максимальная энергия потока. Не может быть 0.
	 *			Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
	 * @param name имя, как будут звать этот поток
	 */
	public StreamSwirl(Trajectory move, int d, int power, String name) {
		super(move, power, name);
		setR(d/2d);
	}
	protected StreamSwirl(JSON j, long v){
		super(j,v);
		setR(j.get("r"));
	}

	private void setR(double r_){
		r = r_;
		updateMatrix();
	}
	
	@Override
	public Action action(Point pos) {
		final var d = position.distance(pos);
		if(d.getHypotenuse() > r) return null; //Это не к нам
		
		
		//У нас круг!
		//Вычисляем нормаль
		final var perpendicular = shadow.maxPower < 0 ? Point.Vector.create(-d.y, d.x) : Point.Vector.create(d.y, -d.x);
		final var dir = perpendicular.direction();
		if(dir == null) return null;
		else return new Action(dir, d.getHypotenuse() / r);
	}
	
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("r", r);
		return j;
	}
	
	
	@Override
	public void paint(Graphics2D g, WorldView.Transforms transform, int posX, int posY, int frame) {
		final var x0 = transform.toDScrinX(posX);
		final var y0 = transform.toDScrinY(posY);
		
		final var r0 = transform.toDScrin((int)(r*2)) / 2;
		if(r0 == 0) return;
		g.setPaint(AllColors.STREAM);
		//g.fill(new Ellipse2D.Double(x0, y0,w0,h0));
		
		//А теперь приступим к порнографии - создании подкругов для движения!
		final var countCurc = r0 < 50 ? 1 : (int) (r0 / 50); //Сколкьо будет кругов
		final var whc = r0 / countCurc; //Радиус одного круга
		for (int curcle = 0; curcle < countCurc; curcle++) {
			var F = shadow.frame(frame,(curcle + 0.5d) / countCurc);
			final var cr = (curcle + 1) * whc;
			final var angleLenght = whc / (Math.PI * cr);//Какая будет длина у частичек круга
			final var al2 = angleLenght * 2;
			for (double angle = al2 * F; angle < Math.PI * 2; angle+=al2) {
				final var a2 = angle + angleLenght;
				final int x1 = (int) (x0 + cr * Math.cos(angle));
				final int y1 = (int) (y0 + cr * Math.sin(angle));
				final int x2 = (int) (x0 + cr * Math.cos(a2));
				final int y2 = (int) (y0 + cr * Math.sin(a2));
				g.drawLine(x1, y1, x2, y2);
			}
		}
	}

}
