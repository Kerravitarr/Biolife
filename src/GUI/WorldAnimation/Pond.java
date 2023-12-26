/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ColorRec;
import java.awt.Graphics2D;

/**
 * анимация для бассена
 * 
 * Бассейн имеет небо, снизу - землю. По краям бассейн склеен.
 * 
 * Небо имеет анимацию погоды: Ясный день, облачность:
 *	0	Ясно
 *	1	Малооблачно
 *	2	Облачно
 *	3	Пасмурно
 *	4	Переменная облачность
 * Гроза, 
 * Осадки: 
 * 0 - нет
 * Х1 - дождь: 01 - слабый, 51 - нормальный, 91 - сильный
 * Х2 - снег
 * Х3 - смешанные
 * Ветер - 
 *	0	Штиль
 *	Х1 - левый 01 - слабый, 51 - сильный, 91 - ураган
 *	Х2 - правый
 * 
 * Работает по принципу конечного автомата
 * @author Kerravitarr
 */
public class Pond extends DefaultAnimation{
	private class Wether{
		enum Cloudy{ 
			/**Ясно*/		CLEAR,
			/**Малооблачно*/PARTLY,
			/**Облачно*/	CLOUDY,
			/**Пасмурно*/	OVERCAST,
			/**Переменно*/	PARTIALLY,
		}
		
		/**Облачность
		 * 0	Ясно
		 *	1	Малооблачно
		 *	2	Облачно
		 *	3	Пасмурно
		 *	4	Переменная облачность
		 */
		int cloud;
	}
	
	/**Длина дня в тиках мира*/
	private static final int DAY_LENGHT = 1000;
	
	/**Небо*/
	private ColorRec sky;
	/**водичка*/
	private ColorRec water;
	/**дно*/
	private ColorRec dirt;
	
	public Pond(WorldView.Transforms transform, int w, int h){
		//Верхнее небо
		int xs[] = new int[4];
		int ys[] = new int[4];
		//Поле, вода
		int yw[] = new int[4];
		//Дно
		int yb[] = new int[4];

		xs[0] = xs[1] = 0;
		xs[2] = xs[3] = w;

		ys[0] = ys[3] = 0;
		ys[1] = ys[2] = yw[0] = yw[3] = transform.toScrinY(0);
		yb[0] = yb[3] = yw[1] = yw[2] = transform.toScrinY(Configurations.getHeight() - 1);
		yb[1] = yb[2] = h;
		sky = new ColorRec(xs,ys,AllColors.SKY);
		water = new ColorRec(xs,yw, AllColors.WATER_POND);
		dirt = new ColorRec(xs,yb, AllColors.DRY);
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g) {
		sky.paint(g);
		dirt.paint(g);
	}
	
}
