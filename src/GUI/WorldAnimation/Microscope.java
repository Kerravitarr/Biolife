/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import Calculations.Point;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ColorRec;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * Анимация для чашки петри.
 * Тут у нас есть посредине эллипс и вокруг него... Стол?
 * 
 * @author Kerravitarr
 */
public class Microscope extends DefaultAnimation{
	/**стол сверху*/
	private ColorRec table0;
	/**водичка*/
	private ColorRec water;
	/**стол снизу*/
	private ColorRec table1;
	
	public Microscope(WorldView.Transforms transform, int w, int h){
		final var a2 = Configurations.getWidth();
		final var b2 = Configurations.getHeight();
		final var a = a2/2d;
		final var b = b2/2d;
		//Поле, вода
		final var xw = new ArrayList<Integer>(a2 * b2);
		final var yw = new ArrayList<Integer>(a2 * b2);
		//Верхняя половина стола
		final var xut = new ArrayList<Integer>(a2 * b2);
		final var yut = new ArrayList<Integer>(a2 * b2);
		//Нижняя половина стола
		final var xdt = new ArrayList<Integer>(a2 * b2);
		final var ydt = new ArrayList<Integer>(a2 * b2);

		//Прочёсываем все точки слева направо, в поисках первых (верхних) наших 
		xut.add(0);
		yut.add(transform.toScrinY(b));
		boolean isFirst = true; //Флаг, чтобы первая точка в обязательном порядке была посредине
		for(var x = 0; x < a2; x++){
			var y = 0;
			for(; y < b2; y++){
				final var point = Point.create(x, y);
				if(point.valid()){
					final int sx = transform.toScrinX(x);
					final int sy;
					if(isFirst){
						isFirst = false;
						sy = transform.toScrinY(b);
					} else {
						sy = transform.toScrinY(y);
					}
					xw.add(sx);
					yw.add(sy);
					xut.add(sx);
					yut.add(sy);
					break;
				}
			}
			if(y == b2 && x > a){ //Когда мы не встретим ни одной правильной точки и пройдём больше половины пути по X - мы в конце. Заканчиваем
				break;
			}
		}
		xut.add(xut.get(xut.size()-1));
		yut.add(transform.toScrinY(b));
		xut.add(w);
		yut.add(transform.toScrinY(b));
		xut.add(w);
		yut.add(0);
		xut.add(0);
		yut.add(0);
		//А теперь пройдём тоже самое, но в обратную сторону
		xdt.add(w);
		ydt.add(transform.toScrinY(b));
		isFirst = true;
		for(var x = a2-1; x >= 0; x--){
			var y = b2-1;
			for(; y >= 0; y--){
				final var point = Point.create(x, y);
				if(point.valid()){
					final int sx = transform.toScrinX(x);
					final int sy;
					if(isFirst){
						isFirst = false;
						sy = transform.toScrinY(b);
					} else {
						sy = transform.toScrinY(y);
					}
					xw.add(sx);
					yw.add(sy);
					xdt.add(sx);
					ydt.add(sy);
					break;
				}
			}
			if(y == 0 && x < a){ //Когда мы не встретим ни одной правильной точки и пройдём больше половины пути по X - мы в самом начале. Заканчиваем
				break;
			}
		}
		xdt.add(xdt.get(xdt.size()-1));
		ydt.add(transform.toScrinY(b));
		xdt.add(0);
		ydt.add(transform.toScrinY(b));
		xdt.add(0);
		ydt.add(h);
		xdt.add(w);
		ydt.add(w);
		water = new ColorRec(xw.stream().mapToInt(Integer::intValue).toArray(),yw.stream().mapToInt(Integer::intValue).toArray(), AllColors.GLASS);
		table0 = new ColorRec(xut.stream().mapToInt(Integer::intValue).toArray(),yut.stream().mapToInt(Integer::intValue).toArray(),AllColors.OAK);
		table1 = new ColorRec(xdt.stream().mapToInt(Integer::intValue).toArray(),ydt.stream().mapToInt(Integer::intValue).toArray(), AllColors.OAK);

	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g) {
		table0.paint(g);
		table1.paint(g);
	}
	
}
