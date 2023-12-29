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
		/**Максимальное значение, включетнльно, которое могут принимать численные переменные*/
		public static final int MAX_V = 100;
		
		/**Облачность. Показывает сколько облаков. 
		*	0	Ясно
		*	MAX_V/5	Переменная облачность
		*	MAX_V*2/5	Малооблачно
		*	MAX_V*3/5	Облачно
		*	MAX_V*4/5	Пасмурно
		 */
		public int cloud;
		/**Гроза. [0;MAX_V]*/
		public int storm;
		/** Осадки в ввиде дождя [0;MAX_V]*/
		public int rain;
		/** Осадки в ввиде снега [0;MAX_V]*/
		public int snow;
		/**Ветер [0;MAX_V]*/
		public int wind;
		/**Ветер дует направо, если true*/
		public boolean rightWind;
		/**Создаёт базовую погоду*/
		public Wether(){
			cloud = storm = rain = snow = wind = 0;
			rightWind = true;
		}
		/** @param key число, на основе которого создаётся новая погода*/
		public void update(long key){
			//Ветер
			final var up = random(key);
			if(up && wind < MAX_V) wind++;
			else if(!up && wind > 0) wind--;
			//Если есть ветер, то могут появиться облачка
			if(wind != 0){
				final var upc = random(Long.rotateLeft(key, 1));
				if(upc && cloud < MAX_V) cloud++;
				else if(!upc && cloud > 0) cloud--;
			} else if(random(Long.rotateLeft(key, 2))){//Шанс сменить направление ну очень небольшой
				rightWind = !rightWind;
			}
			//Если есть облачка, то могут пойти и осадки
			if(cloud > MAX_V*2/5){
				final var uprs = random(Long.rotateLeft(key, 4));
				if(uprs && Math.max(rain,snow) < MAX_V){
					if(rain == 0 && snow != 0) snow ++;
					else if(rain != 0 && snow == 0) rain ++;
					else if(rain == 0 && snow == 0) {
						switch ((int)Utils.Utils.randomByHash(key, 12)) {
							case 0,1,2,3,4,5 -> snow ++;
							case 6,7,8,9,10,11 -> rain ++;
							case 12 -> rain = ++snow;
						}
					}
				} else if(!uprs && Math.max(rain,snow) > 0){
					if(rain == 0 && snow != 0) snow --;
					else if(rain != 0 && snow == 0) rain --;
					else rain = --snow;
				}
			} else if(cloud > MAX_V/5) {
				final var uprs = random(Long.rotateLeft(key, 4));
				if(!uprs && Math.max(rain,snow) > 0){
					if(rain == 0 && snow != 0) snow --;
					else if(rain != 0 && snow == 0) rain --;
					else rain = --snow;
				}
			} else if(snow != 0 || rain != 0){
				snow = rain = 0;
			}
			//А если есть осадки, то может быть и гроза
			if(Math.max(rain,snow) > MAX_V / 2){
				final var ups = random(Long.rotateLeft(key, 6));
				if(ups && storm < MAX_V) storm++;
				else if(!ups && storm > 0) storm--;
			} else if(Math.max(rain,snow) > MAX_V / 4){
				if(random(Long.rotateLeft(key, 6)) && storm > 0) storm--;
			} else if(storm > 0) {
				storm = 0;
			}
		}
		private boolean random(long key){return Utils.Utils.randomByHash(key, 1) == 0;}
		@Override
		public String toString(){
			return String.format("Облачность %.0f%%, гроза %.0f%%, дождь %.0f%%, снег %.0f%%. ветер %s %.0f%%", cloud * 100d / MAX_V,  storm * 100d / MAX_V, rain * 100d / MAX_V, snow * 100d / MAX_V,rightWind ? "правый" : "левый", wind * 100d / MAX_V);
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
		/**Высота одной дуги облачка конкретного облака*/
		private double heightOne = 0;
		/**Ширина одной дуги облачка конкретного облака*/
		private double widthOne = 0;
		/**Все дуги, из которых может состоять облачко*/
		private final Arc2D.Double[] arcs = new Arc2D.Double[COUNT_C * (COUNT_C + 1) / 2];
		/** Пересоздаёт облачко
		 * @param hash хэш, для генерации чисел
		 * @param width ширина экрана
		 * @param height высота под облачка
		 * @param wether текущая погода
		 */
		public void regenerate(int hash, int width,int height,final Wether wether){
			isVisible = true;
			if(wind == 0){
				//У нас штиль, но нам нужно создать облако. Создаём самое маленькое, в любом месте
				count = width_line = 1;
				location.x = Utils.Utils.randomByHash(hash, 0, width);
			} else {
				//У нас нормальная погода. Можно и облако замутить нормальное за границами экрана
				width_line = Utils.Utils.normalize_value(Utils.Utils.randomByHash(hash, 0, wether.cloud), 0, Wether.MAX_V, 1, COUNT_C);
				final var maxC = (width_line*(width_line+1))/2; //Сколько у нам может быть дуг максимум
				count = width_line == 1 ? 1 : Utils.Utils.randomByHash(hash + maxC, maxC - width_line, maxC);
			}
			this.wind = (Utils.Utils.randomByHash(hash, 50, 150) / 100d) * wether.wind * WIND_TO_SPEED / Wether.MAX_V;
			if(!wether.rightWind) this.wind = -this.wind;
			scale = Utils.Utils.randomByHash(hash + count, 50, 150) / 100d;
			heightOne = HEIGHT_PER_CL * scale;
			widthOne = WIDTH_PER_CL * scale;
			size.width = (int) (width_line * widthOne);
			size.height = (int) (width_line * heightOne);
			if(!wether.rightWind) location.x = - size.width;
			else location.x = width + size.width;
			location.y = Utils.Utils.randomByHash((hash + size.height), 0, height) - size.height - heightOne/2;
			
			{//Буфферизируем наши дуги
				for (int i = 0; i < count; i++) {
					if (arcs[i] == null) {
						arcs[i] = new Arc2D.Double(0, 0, widthOne, heightOne * 2, 0, 180, Arc2D.OPEN);
					} else {
						arcs[i].width = widthOne;
						arcs[i].height = heightOne * 2;
					}
				}
			}
		}
		/** Рисует облачко
		 * @param g холст, на котором его надо нарисовать
		 */
		private void paint(Graphics2D g, WorldView.Transforms t) {
			var r = width_line;
			var y = location.y;
			var countC = 0;
			if(wind > 0 || count <= r){
				var x = t.toDScrinX(location.x);
				for(var i = 0 ; i < count ; i++){
					final var a = arcs[i];
					a.x = x; a.y= y;
					g.draw( a);
					if(++countC == r){
						countC = 0;
						r--;
						x -= r * widthOne - widthOne / 2;
						y -= heightOne;
					} else {
						x += widthOne;
					}
				}
			} else {
				var x = t.toDScrinX(location.x) + r * widthOne;
				for(var i = 0 ; i < count ; i++){
					final var a = arcs[i];
					a.x = x; a.y= y;
					g.draw( a);
					if(++countC == r){
						countC = 0;
						r--;
						x += r * widthOne - widthOne / 2;
						y -= heightOne;
					} else {
						x -= widthOne;
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
	private static final int PERIOD_LENGHT = 10;
	/**Выделение жирненьким по умолчанию*/
	private final static java.awt.BasicStroke DASHED = new java.awt.BasicStroke(2);
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
	/**Преобразователь кординат объектов в координаты мира*/
	private final WorldView.Transforms transform;
	
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
		this.transform = transform;
		if(state == null) state = new Static();
	}
	/** Цикл, проходящий по всем видимым облакам
	 * @param c обработчик каждого видимого облака
	 */
	private void forEachVisibleCloud(java.util.function.Consumer<Cloud> c){
		for (int i = 0, cc = 0; i < state.clouds.size() && cc < state.countCloud; i++) {
			final var cloud = state.clouds.get(i);
			if(cloud.isVisible) {
				c.accept(cloud);
				cc++;
			}
		}
	}
	
	@Override
	protected void nextStep(long step){
		final var s = step / PERIOD_LENGHT;
		if(s != state.next_step_update){
			state.next_step_update = s;
			final var w = state.expectation.wind;
			final var wr = state.expectation.rightWind;
			state.expectation.update(s);
			System.out.println(state.expectation);
			if(w > state.expectation.wind){
				//Ветер уменьшился
				forEachVisibleCloud(c -> c.wind -= Cloud.WIND_TO_SPEED);
			} else if(w < state.expectation.wind) {
				forEachVisibleCloud(c -> c.wind += Cloud.WIND_TO_SPEED);
			}
			if(wr != state.expectation.rightWind){ //Если ветер поменял сторону
				forEachVisibleCloud(c -> c.wind = -c.wind);
			}
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
				final var cx = transform.toScrinX(cloud.location.x);
				if (cx < -cloud.size.width || cx > width + cloud.size.width || cloud.location.y + cloud.size.height > height_sky)
					cloud.isVisible = false;
				else 
					state.countCloud++;
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
			cloud.regenerate(state.hashCountr++,width, height_sky,state.expectation);
			cloud.location.x = transform.toWorldX(cloud.location.x);
			state.countCloud++;
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
		forEachVisibleCloud(c -> c.paint(g, transform));
	}
	
}
