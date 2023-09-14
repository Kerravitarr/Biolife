/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import Calculations.Point.DIRECTION;
import GUI.AllColors;
import GUI.WorldView;
import MapObjects.CellObject;
import Utils.JSON;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**Прямоугольный вертикальный поток*/
public class StreamVertical extends StreamAbstract {
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
	
	protected StreamVertical(JSON j, long v) throws GenerateClassException{
		super(j,v);
		this.width = j.get("width");
		this.height = j.get("height");
	}

	@Override
	public void action(CellObject cell) {
		final var pos = cell.getPos();
		final var d = position.distance(pos);
		final double absdx = Math.abs(d.x);
		if(absdx > width / 2 || Math.abs(d.y) > height / 2) return;
		//Сила затягивания к центральной оси потка
		var F = shadow.power(absdx / (width / 2));
		if(F > 0 && cell.getAge() % F == 0)
			cell.moveD(DIRECTION.UP); // Поехали по направлению!
		else if(F < 0 && cell.getAge() % -F == 0)
			cell.moveD(DIRECTION.DOWN); // Поехали по направлению!
	}

	@Override
	protected void move() {}
	@Override
	public List<ParamObject> getParams() {
		final java.util.ArrayList<Calculations.ParamObject> ret = new ArrayList<ParamObject>(2);
		ret.add(new ParamObject("width", 1,Configurations.getWidth(),1,null){
			@Override
			public void setValue(Object value) throws ClassCastException {
				width = ((Number) value).intValue();
			}
			@Override
			protected Object get() {
				return width;
			}
		});
		ret.add(new ParamObject("height", 1,Configurations.getHeight(),1,null){
			@Override
			public void setValue(Object value) throws ClassCastException {
				height = ((Number) value).intValue();
			}
			@Override
			protected Object get() {
				return height;
			}
		});
		return ret;	
	}
	
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("width", width);
		j.add("height", height);
		return j;
	}
	
	/**Специальный счётчик кадров, нужен для отрисовки "движения" воды*/
	private int frame = Integer.MAX_VALUE / 2;
	@Override
	protected void paint(Graphics2D g, WorldView.Transforms transform, int posX, int posY){
		
		final var x0 = transform.toScrinX(posX - width/2);
		final var y0 = transform.toScrinY(posY - height/2);
		
		final var w = transform.toScrin(width);
		final var h = transform.toScrin(height);
		final var isUp = shadow.maxPower > 0;
		g.setPaint(AllColors.STREAM);
		//g.fill(new Rectangle2D.Double(x0, y0, w, h));
		
		final var wRow = 20; //Ширина полосок
		final var countColumn = w / (wRow * 2); //Сколкьо будет двигающихся колонок на ширину экрана
		final var countRow = 4; //Сколкьо будет полосок
		for (double column = 0; column < countColumn; column++) {
			final var xl0 = x0 + column * wRow;
			final var xr0 = x0 + w - (column + 1) * wRow;
			
			var F = shadow.power(1000,10,(column + 0.5d) / countColumn);
			final var step = isUp ? (F - (frame % F)) : (frame % F);	//"номер" кадра для колонки
			final var delta = h / countRow; //Частота полосок в колонке
			final var delta0 = y0 + delta * step / Math.abs(F);
			for (int row = 0; row < countRow; row++) {
				final var yc = delta0 + delta * row;
				g.fill(new Rectangle2D.Double(xl0, yc, wRow, 2));
				g.fill(new Rectangle2D.Double(xr0, yc, wRow, 2));
			}
		}
		
		frame++;
	}
}
