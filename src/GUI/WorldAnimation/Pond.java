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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

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
		int cloud;
		/**Гроза. 0 - нет, 255 - в полную силу*/
		int storm;
		/** Осадки
		 * 0 нет
		 * 0х0F - дождь (0-15)
		 * 0хF0 - снег (0-15)
		 * Для смешанных можно выкруть и дождь и снег
		 */
		int Precipitation;
		/**Ветер [0;0x7F] от штиля до урагана левый. [0x80;0xFF] - правый*/
		int Wind;
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
		private int random(long key){return (int) Utils.Utils.randomByHash(key, 0, 0xFF);}
		@Override
		public String toString(){
			return String.format("Облачность %.0f%%, гроза %.0f%%, дождь %.0f%%, снег %.0f%%. ветер %s %.0f%%", cloud * 100d / 0xff,  storm * 100d / 0xff, (Precipitation & 0x0F) * 100d / 0x0f, ((Precipitation >> 4) & 0x0F) * 100d / 0x0f,(Wind & 0x80) == 0 ? "левый" : "правый", (Wind & ~0x80) * 100d / 0x7f);
		};
	}
	/**Облачно*/
	private static class Cloud{
		/**Ширина одной дуги облачка*/
		private final static int WIDTH_PER_CL = 40;
		/**Высота одной дуги облачка*/
		private final static int HEIGHT_PER_CL = 20;
		/**Максимальная плотность облаков, квадратных пикселей/облако*/
		public final static int MAX_CLOUD_DENSITY = WIDTH_PER_CL * HEIGHT_PER_CL;
		/**Максимальная ширина облачка, сколько тут будет максимум дуг в нижней части*/
		private final static int COUNT_C = 10;
		/**Коэффициент перевода ветра в скорость пикселей/кадр*/
		public final static double WIND_TO_SPEED = 0.01;
		
		/**Нужно ли рисовать данное облако?*/
		public boolean isVisible = false;
		/**Ветер. Показывает скорость облака в пк/кадр. Если положительный, то ветер правый*/
		public double wind = 0;
		/**Координаты облака. Причём y = 0 - это уровень воды!!!*/
		public java.awt.Point.Double location = new java.awt.Point.Double(0,0);
		/**Размеры облачка*/
		public java.awt.Dimension size = new java.awt.Dimension(0,0);
		/**Из скольки дуг состоит облачко*/
		public int count = 0;
		/**"ширина" основания облака в дугах*/
		public int width_line = 0;
		/**Масштаб облачка, для формирования якобы удалённости облачков*/
		private double scale = 0;
		private Arc2D.Double[] arcs = new Arc2D.Double[COUNT_C * (COUNT_C + 1) / 2];
		/** Пересоздаёт облачко
		 * @param hash хэш, для генерации чисел
		 * @param wind [0;0x7F] от штиля до урагана левый. [0x80;0xFF] - правый
		 * @param width
		 * @param height
		 * @param cloudy 
		 */
		public void regenerate(int hash, int wind, int width,int height, int cloudy){
			isVisible = true;
			if(wind == 0){
				//У нас штиль, но нам нужно создать облако. Создаём самое маленькое, в любом месте
				count = width_line = 1;
				location.x = Utils.Utils.randomByHash(hash, 0, width);
			} else {
				//У нас нормальная погода. Можно и облако замутить нормальное за границами экрана
				width_line = Utils.Utils.normalize_value(Utils.Utils.randomByHash(hash, 0, cloudy), 0, 0xFF, 1, COUNT_C);
				final var maxC = (width_line*(width_line+1))/2; //Сколько у нам может быть дуг максимум
				count = width_line == 1 ? 1 : Utils.Utils.randomByHash(hash + maxC, maxC - width_line, maxC);
			}
			this.wind = (Utils.Utils.randomByHash(hash, 50, 150) / 100d) * (wind & (~0x80)) * WIND_TO_SPEED;
			if((wind & 0x80) == 0) this.wind = -this.wind;
			scale = Utils.Utils.randomByHash(hash + count, 50, 150) / 100d;
			size.width = (int) (width_line * WIDTH_PER_CL * scale);
			size.height = (int) (width_line * HEIGHT_PER_CL * scale);
			if((wind & 0x80) != 0) location.x = - size.width;
			else location.x = width + size.width;
			location.y = Utils.Utils.randomByHash((hash + size.height), 0, height) - size.height;
			
			{//Буфферизируем наши дуги
				final var W = WIDTH_PER_CL * scale;
				final var H = HEIGHT_PER_CL * scale;
				for (int i = 0; i < count; i++) {
					if(arcs[i] == null) arcs[i] = new Arc2D.Double(0, 0, W, H*2, 0, 180, Arc2D.OPEN);	
				}
			}
		}
		/** Рисует облачко
		 * @param g холст, на котором его надо нарисовать
		 */
		private void paint(Graphics2D g) {
			var r = width_line;
			var y = location.y;
			var countC = 0;
			final var W = WIDTH_PER_CL * scale;
			final var H = HEIGHT_PER_CL * scale;
			if(wind > 0){
				var x = location.x;
				for(var i = 0 ; i < count ; i++){
					final var a = arcs[i];
					a.x = x; a.y= y;
					g.draw( a);
					if(++countC == r){
						countC = 0;
						r--;
						x -= r * W - W / 2;
						y -= H;
					} else {
						x += W;
					}
				}
			} else {
				var x = location.x + r * W;
				for(var i = 0 ; i < count ; i++){
					final var a = arcs[i];
					a.x = x; a.y= y;
					g.draw( a);
					if(++countC == r){
						countC = 0;
						r--;
						x += r * W - W / 2;
						y -= H;
					} else {
						x -= W;
					}
				}
			}
			
		}
	}
	/**Класс со всеми статическими переменными*/
	private static class Static{
		/**Ожидаемая погода*/
		public Wether expectation = new Wether();
		/**"Состояние" мира. Это число, которое можно считать номером состояния начиная от нулевого*/
		public long next_step_update = -1;
		/**Облака на небе*/
		public List<Cloud> clouds = new ArrayList<>();
		/**Количество реально оторбражаемых облаков на небе*/
		public int countCloud = 0;
		/**Счётчик хэшей для каждого облачка*/
		public int hashCountr = 0;
	}
	
	/**Длина одного периода в тиках мира*/
	private static final int PERIOD_LENGHT = 1000;
	/**Текущее состояние погоды, не зависящее от реального разрешения экрана*/
	private static Static state;
	/**Ширина экрана*/
	private final int width;
	/**Высота экрана*/
	private final int height;
	/**Высота неба*/
	private final int height_sky;
	/**Максимальное количество облаков*/
	private final int maxCloud;
	
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
		
		width = w;
		height = h;
		height_sky = transform.toScrinY(0);
		maxCloud = (width * height) / Cloud.MAX_CLOUD_DENSITY;
		if(state == null) state = new Static();
	}
	
	@Override
	protected void nextStep(long step){
		final var s = step / PERIOD_LENGHT;
		if(s != state.next_step_update){
			state.next_step_update = s;
			state.expectation = new Wether(s+1);
			System.out.println(state.expectation);
		}
	}
	@Override
	protected void nextFrame(){
		if(state.countCloud != 0){
			//Обрабатываем облака
			final var countCloud = state.countCloud;
			state.countCloud = 0;
			for (int i = 0, cc = 0; i < state.clouds.size() && cc < countCloud; i++) {
				final var cloud = state.clouds.get(i);
				if(!cloud.isVisible) continue;
				else cc++;
				cloud.location.x += cloud.wind;
				if (cloud.location.x < -cloud.size.width || cloud.location.x > width + cloud.size.width || cloud.location.y + cloud.size.height > height_sky) {
					cloud.isVisible = false;
				} else {
					state.countCloud++;
					if(state.expectation.Wind != 0x00 && cloud.wind == 0){//У нас изменилась сила ветра
						if((state.expectation.Wind & 0x80) != 0) cloud.wind += Cloud.WIND_TO_SPEED;
						else cloud.wind -= Cloud.WIND_TO_SPEED;
					}
				}
			}
		}
		final var expectedCloud = (state.expectation.cloud) * maxCloud / 0xFF;
		if(state.countCloud < expectedCloud){
			//У нас мало облаков, надо создать ещё!
			final Cloud cloud;
			if(state.countCloud < state.clouds.size()){
				//Используем уже существующее облако
				cloud = state.clouds.stream().filter(c -> !c.isVisible).findFirst().get();
			} else {
				//Надо создать новое облако
				cloud = new Cloud();
				state.clouds.add(cloud);
			}
			cloud.regenerate(state.hashCountr++,state.expectation.Wind,width, height_sky,state.expectation.cloud);
			state.countCloud++;
		} else if(state.expectation.Wind == 0x00 && state.countCloud != 0 && state.countCloud > expectedCloud){
			//У нас слишком много облаков и не стоит ждать, что их сдует ветер
			final var cloud = state.clouds.stream().filter(c -> !c.isVisible).findFirst().get();
			if(cloud.count > 1){
				cloud.count--;
			} else {
				cloud.isVisible = false;
			}
		}
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g) {
		sky.paint(g);
		dirt.paint(g);
		g.setColor(Color.BLACK);
		for (int i = 0, cc = 0; i < state.clouds.size() && cc < state.countCloud; i++) {
			final var c = state.clouds.get(i);
			if(c.isVisible) c.paint(g);
		}
	}
	
}
