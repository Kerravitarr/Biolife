/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import GUI.AllColors;
import GUI.WorldView;
import MapObjects.CellObject;
import Utils.JSON;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Круглый поток, представляющий собой циклическое движение
 * @author Kerravitarr
 */
public class StreamSwirl extends StreamAbstract {
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
	protected StreamSwirl(JSON j, long v) throws GenerateClassException{
		super(j,v);
		setR(j.get("r"));
	}

	private void setR(double r_){
		r = r_;
	}
	
	@Override
	public void action(CellObject cell) {
		final var pos = cell.getPos();
		final var d = position.distance(pos);
		if(d.getHypotenuse() > r) return; //Это не к нам
		
		//У нас круг!
		final var F = shadow.power(d.getHypotenuse() / r);
		if(cell.getAge() % Math.abs(F) == 0){
			//Вычисляем нормаль
			final var perpendicular = F < 0 ? Point.Vector.create(-d.y, d.x) : Point.Vector.create(d.y, -d.x);
			final var dir = perpendicular.direction();
			if(dir != null)
				cell.moveD(perpendicular.direction());
		}
	}
	@Override
	public List<ParamObject> getParams(){
		final var ret = new ArrayList<ParamObject>(1);
		ret.add(new ParamObject("d", 2,Configurations.getWidth(),2,null){
			@Override
			public void setValue(Object value) throws ClassCastException {
				setR(((Number) value).doubleValue() / 2);
			}
			@Override
			protected Object get() {
				return (int)(r*2);
			}
		});
		return ret;
	}
	@Override
	protected void move() {}
	
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("r", r);
		return j;
	}
	
	
	/**Специальный счётчик кадров, нужен для отрисовки "движения" воды*/
	private int frame = Integer.MAX_VALUE / 2;
	@Override
	public void paint(Graphics2D g, WorldView.Transforms transform, int posX, int posY) {
		final var x0 = transform.toScrinX(posX);
		final var y0 = transform.toScrinY(posY);
		
		final var r0 = transform.toScrin((int)(r));
		if(r0 == 0) return;
		final var isUp = shadow.maxPower > 0;
		g.setPaint(AllColors.STREAM);
		//g.fill(new Ellipse2D.Double(x0, y0,w0,h0));
		
		//А теперь приступим к порнографии - создании подкругов для движения!
		final var whc = 50; //Каждые сколько пк будет круг
		final var countCurc = r0 / whc; //Сколкьо будет кругов
		for (int curcle = 0; curcle < countCurc; curcle++) {
			var F = shadow.power(10000,100,(curcle + 0.5d) / countCurc);
			final var step = isUp ? (F - (frame % F)) : (frame % F);	//"номер" кадра для колонки
			final var angle_step = Math.PI * 2 * step / Math.abs(F);
			final var cr = (curcle + 1) * whc;
			final var countDel = whc / (Math.PI * cr);//На сколько частей разделим круг, чтобы длина каждой части была whc
			for (double angle = 0; angle < Math.PI * 2; angle+=countDel * 2) {
				final var a1 = angle_step + angle;
				final var a2 = a1 + countDel;
				final int x1 = (int) (x0 + cr * Math.cos(a1));
				final int y1 = (int) (y0 + cr * Math.sin(a1));
				final int x2 = (int) (x0 + cr * Math.cos(a2));
				final int y2 = (int) (y0 + cr * Math.sin(a2));
				g.drawLine(x1, y1, x2, y2);
			}
		}
		frame++;
	}

}
