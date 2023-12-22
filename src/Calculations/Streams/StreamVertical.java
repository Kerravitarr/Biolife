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
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**Прямоугольный вертикальный поток*/
public class StreamVertical extends StreamAbstract {
	static{
		final var builder = new ClassBuilder<StreamVertical>(){
			@Override public StreamVertical generation(JSON json, long version){return new StreamVertical(json, version);}
			@Override public JSON serialization(StreamVertical object) { return object.toJSON();}

			@Override public String serializerName() {return "Вертикальный";}
			@Override public Class printName() {return StreamVertical.class;}

		};
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,StreamVertical>("width",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getWidth()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
			@Override public Integer get(StreamVertical who) {return who.width;}
			@Override public void setValue(StreamVertical who, Integer value) {who.width = value;who.updateMatrix();}
		});
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,StreamVertical>("height",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getHeight()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
			@Override public Integer get(StreamVertical who) {return who.height;}
			@Override public void setValue(StreamVertical who, Integer value) {who.height = value;who.updateMatrix();}
		});
		builder.addConstructor(new ClassBuilder.Constructor<StreamVertical>(){
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
				addParam(new ClassBuilder.NumberConstructorParamAdapter("super.power",-1000,1,1000,null,null));
				addParam(new ClassBuilder.StringConstructorParam(){
					@Override public Object getDefault() {return "Конвекция";}
					@Override public String name() { return "super.name";}
					
				});
			}

			@Override
			public StreamVertical build() {
				return new StreamVertical(new Trajectory(getParam(0,Point.class)), getParam(1,Integer.class), getParam(2,Integer.class), getParam(3,Integer.class), getParam(4,String.class));
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
	 *Об энергии: Если меньше 0, то каждый шаг поток будет тянуть клетку на дно
	 *			если больше 0, то поток восходящий, то есть двигает клету к поверхности
	 * @param move движение ЦЕНТРА этого потока
	 * @param width ширина, в клетках мира
	 * @param height высота, в клетках мира
	 * @param shadow тип снижения мощности от максимума к минимуму
	 * @param name имя, как будут звать этот поток
	 */
	public StreamVertical(Trajectory move, int width, int height, StreamAttenuation shadow,String name) {
		super(move, shadow, name);
		this.width = width;
		this.height = height;
	}

	/**Создание квадртаного потока без убывания мощности
	 *Об энергии: Если меньше 0, то каждый шаг поток будет тянуть клетку на дно
	 *			если больше 0, то поток восходящий, то есть двигает клету к поверхности
	 * @param move движение ЦЕНТРА этого потока
	 * @param width ширина, в клетках мира
	 * @param height высота, в клетках мира
	 * @param power максимальная энергия потока. Не может быть 0.
	 *			Если меньше 0, то каждый шаг поток будет тянуть клетку на дно
	 *			если больше 0, то поток восходящий, то есть двигает клету к поверхности
	 * @param name имя, как будут звать этот поток
	 */
	public StreamVertical(Trajectory move, int width, int height, int power, String name) {
		super(move, power,name);
		this.width = width;
		this.height = height;
	}
	
	protected StreamVertical(JSON j, long v){
		super(j,v);
		this.width = j.get("width");
		this.height = j.get("height");
	}

	@Override
	public Action action(Point pos) {
		final var d = position.distance(pos);
		final double absdx = Math.abs(d.x);
		if(absdx > width / 2 || Math.abs(d.y) > height / 2) return null;
		return new Action(shadow.maxPower > 0 ? DIRECTION.UP : DIRECTION.DOWN, absdx / (width / 2));
	}
	
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
		g.setPaint(AllColors.STREAM);
		//g.fill(new Rectangle2D.Double(x0, y0, w, h));
		
		final var wRow = 20; //Ширина полосок
		final var countColumn = w / (wRow * 2); //Сколкьо будет двигающихся колонок на ширину экрана
		final var countRow = 4; //Сколкьо будет полосок
		for (double column = 0; column < countColumn; column++) {
			final var xl0 = x0 + column * wRow;
			final var xr0 = x0 + w - (column + 1) * wRow;
			
			var F = shadow.frame(frame,(column + 0.5d) / countColumn);
			final var delta = h / countRow; //Частота полосок в колонке
			final var delta0 = y0 + delta * F;
			for (int row = 0; row < countRow; row++) {
				final var yc = delta0 + delta * row;
				g.fill(new Rectangle2D.Double(xl0, yc, wRow, 2));
				g.fill(new Rectangle2D.Double(xr0, yc, wRow, 2));
			}
		}
	}
}
