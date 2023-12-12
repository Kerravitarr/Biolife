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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**Круглый поток*/
public class StreamEllipse extends StreamAbstract {
	static{
		final var builder = new ClassBuilder<StreamEllipse>(){
			@Override public StreamEllipse generation(JSON json, long version){return new StreamEllipse(json, version);}
			@Override public JSON serialization(StreamEllipse object) { return object.toJSON();}

			@Override public String serializerName() {return "Элиптический";}
			@Override public Class printName() {return StreamEllipse.class;}

		};
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,StreamEllipse>("a2",0,0,0,0,null) {
			@Override public Integer get(StreamEllipse who) {return who.a2;}
			@Override public void setValue(StreamEllipse who, Integer value) {who.setA2(value);}
			@Override public Integer getDefault() {return Configurations.getWidth()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
		});
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,StreamEllipse>("b2",0,0,0,0,null) {
			@Override public Integer get(StreamEllipse who) {return who.b2;}
			@Override public void setValue(StreamEllipse who, Integer value) {who.setB2(value);}
			@Override public Integer getDefault() {return Configurations.getHeight()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
		});
		final var center = new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "constructor.center";}
				};
		final var power = new ClassBuilder.NumberConstructorParamAdapter("super.power",-1000,1,1000,null,null);
		final var name = new ClassBuilder.StringConstructorParam(){
				@Override public Object getDefault() {return "Воронка";}
				@Override public String name() { return "super.name";}

			};
		builder.addConstructor(new ClassBuilder.Constructor<StreamEllipse>(){
			{
				addParam(center);
				addParam(power);
				addParam(new ClassBuilder.NumberConstructorParamAdapter("a2",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getWidth()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("b2",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getHeight()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
				});
				addParam(name);
			}

			@Override
			public StreamEllipse build() {
				return new StreamEllipse(new Trajectory(getParam(0,Point.class)), getParam(1,Integer.class), getParam(2,Integer.class), getParam(3,Integer.class), getParam(4,String.class));
			}
			@Override public String name() {return "ellipse";}
		});
		builder.addConstructor(new ClassBuilder.Constructor<StreamEllipse>(){
			{
				addParam(center);
				addParam(power);
				addParam(new ClassBuilder.NumberConstructorParamAdapter("d",0,0,0,0,null){
					@Override public Integer getDefault() {return Math.min(Configurations.getWidth(),Configurations.getHeight())/2;}
					@Override public Integer getSliderMaximum() {return Math.min(Configurations.getWidth(),Configurations.getHeight());}
				});
				addParam(name);
			}

			@Override
			public StreamEllipse build() {
				return new StreamEllipse(new Trajectory(getParam(0,Point.class)), getParam(1,Integer.class), getParam(2,Integer.class), getParam(3,String.class));
			}
			@Override public String name() {return "circle";}
		});
		StreamAbstract.register(builder);
	}
	
	
	/**Большая ось эллипса - лежит на оси Х*/
	private int a2;
	/**Большая полуось эллипса, лежит на оси X*/
	private double a;
	/**Квадрат большой полуоси эллипса*/
	private double aa;
	/**Малая ось эллипса - лежит на оси Y*/
	private int b2;
	/**Малая полуось эллипса, лежит на оси Y*/
	private double b;
	/**Квадрат малой полуоси элипса*/
	private double bb;

	/**Создание элипсовидного потока
	 * Об энергии:Если больше 0, то клетку будет выталкивать из центра
	 *	<br>		если меньше 0, затягивать в центр
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param a2 большая ось эллипса - лежит на оси Х
	 * @param b2 малая ось эллипса - лежит на оси Y
	 * @param shadow тип снижения мощности от максимума к минимуму
	 * @param name имя, как будут звать этот поток
	 */
	public StreamEllipse(Trajectory move, int a2, int b2, StreamAttenuation shadow, String name) {
		super(move, shadow, name);
		setA2(a2);
		setB2(b2);
	}

	/**Создание элипсовидного потока без изменения мощности на всём потоке
	 * Об энергии:Если больше 0, то клетку будет выталкивать из центра
	 *	<br>		если меньше 0, затягивать в центр
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param a2 большая ось эллипса - лежит на оси Х
	 * @param b2 малая ось эллипса - лежит на оси Y
	 * @param power максимальная энергия потока. Не может быть 0.
	 * @param name имя, как будут звать этот поток
	 */
	public StreamEllipse(Trajectory move, int a2, int b2, int power, String name) {
		this(move, b2, power, new StreamAttenuation.NoneStreamAttenuation(power),name);
	}

	/**Создание круглого потока
	 * Об энергии:Если больше 0, то клетку будет выталкивать из центра
	 *	<br>		если меньше 0, затягивать в центр
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param d диаметр круга
	 * @param shadow тип снижения мощности от максимума к минимуму
	 * @param name имя, как будут звать этот поток
	 */
	public StreamEllipse(Trajectory move, int d, StreamAttenuation shadow,String name) {
		this(move, d, d, shadow, name);
	}

	/**Создание круглого потока без изменения мощности на всём потоке
	 * Об энергии:Если больше 0, то клетку будет выталкивать из центра
	 *	<br>		если меньше 0, затягивать в центр
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param d диаметр круга
	 * @param power максимальная энергия потока. Не может быть 0.
	 *			Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
	 * @param name имя, как будут звать этот поток
	 */
	public StreamEllipse(Trajectory move, int d, int power, String name) {
		this(move, d,d,  power,name);
	}
	protected StreamEllipse(JSON j, long v){
		super(j,v);
		setA2(j.get("a2"));
		setB2(j.get("b2"));
	}

	private void setA2(int a2){
		this.a2 = a2;
		a = a2 / 2d;
		aa = a * a;
		updateMatrix();
	}
	private void setB2(int b2){
		this.b2 = b2;
		b = b2 / 2d;
		bb = b * b;
		updateMatrix();
	}
	
	@Override
	public Action action(Point pos) {
		final var d = position.distance(pos);
		if(Math.pow(d.x, 2) / (aa) + Math.pow(d.y, 2) / (bb) > 1 || d.direction() == null) return null; //Это не к нам
		final var dir = shadow.maxPower > 0 ? d.direction() : d.direction().inversion();
		
		if (a2 == b2) {
			//У нас круг!
			return new Action(dir, d.getHypotenuse() / a);
		} else {
			final var teta = Math.atan2(d.y,d.x);
			return new Action(dir, d.getHypotenuse() / Math.hypot(a * Math.cos(teta), b * Math.sin(teta)));
		}
	}
	
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("a2", a2);
		j.add("b2", b2);
		return j;
	}

	/**Специальный счётчик кадров, нужен для отрисовки "движения" воды*/
	@Override
	public void paint(Graphics2D g, WorldView.Transforms transform, int posX, int posY, int frame) {
		final var x0 = transform.toDScrinX(posX - a);
		final var y0 = transform.toDScrinY(posY - b);
		
		final var w0 = transform.toScrin(a2);
		final var h0 = transform.toScrin(b2);	
		if(w0 == 0 || h0 == 0) return;
		g.setPaint(AllColors.STREAM);
		//g.fill(new Ellipse2D.Double(x0, y0,w0,h0));
		
		//А теперь приступим к порнографии - создании подэллипсов для движения!
		final var D = Math.max(w0, h0);
		final var countCurc = D < 50 ? 1 : (D / 50); //Сколкьо будет кругов
		final var wx = w0 / (countCurc * 2); //Ширина круга
		final var hy = h0 / (countCurc * 2); //Высота круга
		for (int curcle = 0; curcle < countCurc; curcle++) {
			var F = curcle + shadow.frame(frame, (curcle + 0.5d) / countCurc);			
			final var dx = wx * F;
			final var dy = hy * F;
			g.draw(new Ellipse2D.Double(x0 + dx, y0 + dy, w0 - dx * 2, h0 - dy * 2));
		}
		g.draw(new Ellipse2D.Double(x0, y0, w0, h0));
	}

}
