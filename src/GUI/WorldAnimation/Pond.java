/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ColorRec;
import Utils.RingBuffer;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

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
	/**Погода, конечный автомат*/
	private static class Wether{
		/**Облачность. Показывает сколько облаков. 
		*	0	Ясно
		*	50	Переменная облачность
		*	100	Малооблачно
		*	150	Облачно
		*	200	Пасмурно
		 */
		byte cloud;
		/**Гроза. 0 - нет, 255 - в полную силу*/
		byte storm;
		/** Осадки
		 * 0 нет
		 * 0х0F - дождь (0-15)
		 * 0хF0 - снег (0-15)
		 * Для смешанных можно выкруть и дождь и снег
		 */
		byte Precipitation;
		/**Ветер [0;0x7F] от штиля до урагана левый. [0x80;0xFF]*/
		byte Wind;
		/**Создаёт базовую погоду*/
		public Wether(){
			cloud = 0;
			storm = 0;
			Precipitation = 0;
			Wind = 0;
		}
		/** @param key число, на основе которого создаётся погода*/
		public Wether(long key){
			cloud = random(key);
			storm = random(Long.rotateLeft(key, 1));
			Precipitation = random(Long.rotateLeft(key, 2));
			Wind = random(Long.rotateLeft(key, 3));
		}
		private byte random(long key){return (byte) Utils.Utils.randomByHash(key, 0, 0xFF);}
	}
	/**Облачно*/
	private static class Cloud{
		/**Минимальная ширина облачка, в пк*/
		public final static int MIN_W = 50;
		
	}
	
	/**Длина одного периода в тиках мира*/
	private static final int PERIOD_LENGHT = 1000;
	
	
	/**Текущая погода*/
	private static Wether now = new Wether();
	/**Ожидаемая погода*/
	private static Wether expectation = new Wether();
	/**"Состояние" мира. Это число, которое можно считать номером состояния начиная от нулевого*/
	private static long next_step_update = -1;
	
	/**Облака на небе*/
	private RingBuffer<Cloud> clouds;
	
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
		
		clouds = new RingBuffer<>(w / Cloud.MIN_W);
	}
	
	@Override
	protected void nextStep(long step){
		final var s = step / PERIOD_LENGHT;
		if(s != next_step_update){
			next_step_update = s;
			expectation = new Wether(s);
		}
	}
	@Override
	protected void nextFrame(){
		final var cc = clouds.size();
		
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
