/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import GUI.AllColors;
import GUI.WorldView;
import MapObjects.CellObject;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**Круглый поток*/
public class StreamEllipse extends StreamAbstract {
	static{
		final var builder = new ClassBuilder<StreamEllipse>(){
			@Override public StreamEllipse generation(JSON json, long version){return new StreamEllipse(json, version);}
			@Override public JSON serialization(StreamEllipse object) { return object.toJSON();}

			@Override public String serializerName() {return "Точка";}
			@Override public Class printName() {return StreamVertical.class;}

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
					@Override public String name() {return "center";}
				};
		final var power = new ClassBuilder.NumberConstructorParamAdapter("power",-1000,1,1000,null,null);
		final var name = new ClassBuilder.StringConstructorParam(){
				@Override public Object getDefault() {return "Поток";}
				@Override public String name() { return "name";}

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
			@Override public String name() {return "ellipse.name";}
		});
		builder.addConstructor(new ClassBuilder.Constructor<StreamEllipse>(){
			{
				addParam(center);
				addParam(power);
				addParam(new ClassBuilder.NumberConstructorParamAdapter("r",0,0,0,0,null){
					@Override public Integer getDefault() {return Math.min(Configurations.getWidth(),Configurations.getHeight())/2;}
					@Override public Integer getSliderMaximum() {return Math.min(Configurations.getWidth(),Configurations.getHeight());}
				});
				addParam(name);
			}

			@Override
			public StreamEllipse build() {
				return new StreamEllipse(new Trajectory(getParam(0,Point.class)), getParam(1,Integer.class), getParam(2,Integer.class), getParam(3,String.class));
			}
			@Override public String name() {return "circle.name";}
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
	 * Об энергии:Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
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
	 * Об энергии:Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
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
	 * Об энергии:Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
	 * @param move движение ЦЕНТРА этого эллипса
	 * @param d диаметр круга
	 * @param shadow тип снижения мощности от максимума к минимуму
	 * @param name имя, как будут звать этот поток
	 */
	public StreamEllipse(Trajectory move, int d, StreamAttenuation shadow,String name) {
		this(move, d, d, shadow, name);
	}

	/**Создание круглого потока без изменения мощности на всём потоке
	 * Об энергии:Если больше 0, то клетку будет тянуть в центр потока
	 *			если меньше 0, выталкивать
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
	}
	private void setB2(int b2){
		this.b2 = b2;
		b = b2 / 2d;
		bb = b * b;
	}
	
	@Override
	public void action(CellObject cell) {
		final var pos = cell.getPos();
		final var d = position.distance(pos);
		if(Math.pow(d.x, 2) / (aa) + Math.pow(d.y, 2) / (bb) > 1) return; //Это не к нам
		
		if (a2 == b2) {
			//У нас круг!
			final var F = shadow.power(d.getHypotenuse() / a);
			if(cell.getAge() % Math.abs(F) == 0){
				final var dir = d.direction();
				if(dir == null) return;
				if(F > 0)	cell.moveD(dir);
				else		cell.moveD(dir.inversion());
			}
		} else {
			final var teta = Math.atan2(d.y,d.x);
			final var F = shadow.power(d.getHypotenuse() / Math.hypot(a * Math.cos(teta), b * Math.sin(teta)));
			
			if(cell.getAge() % Math.abs(F) == 0){
				final var dir = d.direction();
				if(dir == null) return;
				if(F > 0)	cell.moveD(dir);
				else		cell.moveD(dir.inversion());
			}
		}
	}
	@Override
	protected void move() {}
	
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
		final var x0 = transform.toScrinX((int)(posX - a));
		final var y0 = transform.toScrinY((int)(posY - b));
		
		final var w0 = transform.toScrin(a2);
		final var h0 = transform.toScrin(b2);	
		if(w0 == 0 || h0 == 0) return;
		final var isUp = shadow.maxPower > 0;
		g.setPaint(AllColors.STREAM);
		//g.fill(new Ellipse2D.Double(x0, y0,w0,h0));
		
		//А теперь приступим к порнографии - создании подэллипсов для движения!
		final var whc = 50; //Ширина/высота круга
		final var countCurc = Math.max(w0, h0) / whc; //Сколкьо будет кругов
		final var wx = w0 / (countCurc * 2); //Ширина круга
		final var hy = h0 / (countCurc * 2); //Высота круга
		for (int curcle = 0; curcle < countCurc; curcle++) {
			var F = shadow.power(1000,10,(curcle + 0.5d) / countCurc);
			final var step = isUp ? (F - (frame % F)) : (frame % F);	//"номер" кадра для колонки
			
			final var dx = curcle * wx + wx * step / Math.abs(F);
			final var dy = curcle * hy + hy * step / Math.abs(F);
			g.draw(new Ellipse2D.Double(x0 + dx, y0 + dy, w0 - dx * 2, h0 - dy * 2));
		}
	}

}
