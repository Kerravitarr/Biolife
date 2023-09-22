/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import Utils.ParamObject;
import Calculations.Point.DIRECTION;
import GUI.AllColors;
import GUI.WorldView;
import MapObjects.CellObject;
import Utils.JSON;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**Прямоугольный вертикальный поток*/
public class StreamHorizontal extends StreamAbstract {
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
	protected StreamHorizontal(JSON j, long v) throws GenerateClassException{
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
	public List<ParamObject> getParams() {
		final var ret = new ArrayList<ParamObject>(2);
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
