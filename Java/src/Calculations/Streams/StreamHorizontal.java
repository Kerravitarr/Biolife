/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Streams;

import Calculations.Configurations;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import Calculations.Point.DIRECTION;
import GUI.AllColors;
import GUI.WorldView;
import MapObjects.CellObject;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**Прямоугольный вертикальный поток*/
public class StreamHorizontal extends StreamAbstract {
	static{
		final var builder = new ClassBuilder<StreamHorizontal>(){
			@Override public StreamHorizontal generation(JSON json, long version){return new StreamHorizontal(json, version);}
			@Override public JSON serialization(StreamHorizontal object) { return object.toJSON();}

			@Override public String serializerName() {return "Горизонтальный";}
			@Override public Class printName() {return StreamHorizontal.class;}

		};
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,StreamHorizontal>("width",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getWidth()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
			@Override public Integer get(StreamHorizontal who) {return who.width;}
			@Override public void setValue(StreamHorizontal who, Integer value) {who.width = value;}
		});
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,StreamHorizontal>("height",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getHeight()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
			@Override public Integer get(StreamHorizontal who) {return who.height;}
			@Override public void setValue(StreamHorizontal who, Integer value) {who.height = value;}
		});
		builder.addConstructor(new ClassBuilder.Constructor<StreamHorizontal>(){
			{
				addParam(new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "center";}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("width",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getWidth()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("height",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getHeight()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("power",-1000,1,1000,null,null));
				addParam(new ClassBuilder.StringConstructorParam(){
					@Override public Object getDefault() {return "Течение";}
					@Override public String name() { return "super.name";}
					
				});
			}

			@Override
			public StreamHorizontal build() {
				return new StreamHorizontal(new Trajectory(getParam(0,Point.class)), getParam(1,Integer.class), getParam(2,Integer.class), getParam(3,Integer.class), getParam(4,String.class));
			}
			@Override public String name() {return "";}
		});
		StreamAbstract.register(builder);
	}
	
	/**Ширина потока*/
	private int width;
	/**Высота потока*/
	private int height;

	/**Создание квадртаного потока
	 *Об энергии:Если больше 0, то каждый шаг поток будет толкать клетку на восток (вправо)
	 *			если меньше 0, то поток будет толкать влево (на запад)
	 * @param move движение ЦЕНТРА этого потока
	 * @param width ширина, в клетках мира
	 * @param height высота, в клетках мира
	 * @param shadow тип снижения мощности от максимума к минимуму
	 * @param name имя, как будут звать этот поток
	 */
	public StreamHorizontal(Trajectory move, int width, int height, StreamAttenuation shadow,String name) {
		super(move, shadow, name);
		this.width = width;
		this.height = height;
	}

	/**Создание квадртаного потока без убывания мощности
	 *Об энергии:Если больше 0, то каждый шаг поток будет толкать клетку на восток (вправо)
	 *			если меньше 0, то поток будет толкать влево (на запад)
	 * @param move движение ЦЕНТРА этого потока
	 * @param width ширина, в клетках мира
	 * @param height высота, в клетках мира
	 * @param power максимальная энергия потока.
	 * @param name имя, как будут звать этот поток
	 */
	public StreamHorizontal(Trajectory move, int width, int height, int power, String name) {
		super(move, power,name);
		this.width = width;
		this.height = height;
	}
	protected StreamHorizontal(JSON j, long v){
		super(j,v);
		this.width = j.get("width");
		this.height = j.get("height");
	}

	@Override
	public void action(CellObject cell) {
		final var pos = cell.getPos();
		final var d = position.distance(pos);
		final double absdy = Math.abs(d.y);
		if(Math.abs(d.x) > width / 2 || absdy > height / 2) return;
		//Сила затягивания к центральной оси потка
		var F = shadow.power(absdy / (height / 2));
		if(F > 0 && cell.getAge() % F == 0)
			cell.moveD(DIRECTION.RIGHT); // Поехали по направлению!
		else if(F < 0 && cell.getAge() % -F == 0)
			cell.moveD(DIRECTION.LEFT); // Поехали по направлению!
	}
	@Override
	protected void move() {}
	
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("width", width);
		j.add("height", height);
		return j;
	}
	
	@Override
	protected void paint(Graphics2D g, WorldView.Transforms transform, int posX, int posY, int frame){
		
		final var x0 = transform.toScrinX(posX - width/2);
		final var y0 = transform.toScrinY(posY - height/2);
		
		final var w = transform.toScrin(width);
		final var h = transform.toScrin(height);
		final var isLeft = shadow.maxPower < 0;
		g.setPaint(AllColors.STREAM);
		//g.fill(new Rectangle2D.Double(x0, y0, w, h));
		
		final var hRow = 20; //Максимальня высота полосок
		final var countRow = h / (hRow * 2); //Сколкьо будет двигающихся строк на высоту экрана
		final var countColumn = 5; //На солько будет деление
		for (double row = 0; row < countRow; row++) {
			final var yl0 = y0 + row * hRow;
			final var yr0 = y0 + h - (row + 1) * hRow;
			
			var F = shadow.power(1000,10,(row + 0.5d) / countRow);
			final var step = isLeft ? (F - (frame % F)) : (frame % F);	//"номер" кадра для колонки
			final var delta = w / countColumn; //Частота полосок в колонке
			final var delta0 = x0 + delta * step / Math.abs(F);
			for (int column = 0; column < countColumn; column++) {
				final var xc = delta0 + delta * column;
				g.fill(new Rectangle2D.Double(xc, yl0, 2, hRow));
				g.fill(new Rectangle2D.Double(xc, yr0, 2, hRow));
			}
		}
	}
}
