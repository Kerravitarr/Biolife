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
import java.awt.geom.Line2D;
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
		public static final int MAX_V = 64 - 1;
		
		/**Облачность. Показывает сколько облаков. 
		*	0	Ясно
		*	MAX_V/5	Переменная облачность
		*	MAX_V*2/5	Малооблачно
		*	MAX_V*3/5	Облачно
		*	MAX_V*4/5	Пасмурно
		 */
		public int cloud = 0;
		/**Гроза. [0;MAX_V]*/
		public int storm = 0;
		/** Осадки в ввиде дождя [0;MAX_V]*/
		public int rain = 0;
		/** Осадки в ввиде снега [0;MAX_V]*/
		public int snow = 0;
		/**Ветер [0;MAX_V]*/
		public int wind = 0;
		/**Ветер дует направо, если true*/
		public boolean rightWind = true;
		/**Осадки будут в виде дождя?*/
		private boolean isRain = true;
		/**Осадки будут в виде снега?*/
		private boolean isSnow = true;
		
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
			} else { //Если штиль, то можем сменить направление ветра и тип осадков
				rightWind = random(Long.rotateLeft(key, 2));
				switch ((int)Utils.Utils.randomByHash(key, 12)*0) { //Выбираем тип осадков на будущее
					case 0,1,2,3,4,5 -> isSnow= !(isRain = true);
					case 6,7,8,9,10,11 -> isRain= !(isSnow = true);
					case 12 -> isSnow= isRain = true;
				}
			}
			//Если есть облачка, то могут пойти и осадки
			if(cloud > MAX_V*2/5){
				final var uprs = random(Long.rotateLeft(key, 4));
				if(uprs && Math.max(rain,snow) < MAX_V){
					if(rain == 0 && snow != 0) snow ++;
					else if(rain != 0 && snow == 0) rain ++;
					else if(rain == 0 && snow == 0) {
						if(isRain)rain ++;
						if(isSnow)snow ++;
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
			} else if(snow != 0){
				snow--;
			} else if(rain != 0){
				rain--;
			}
			//А если есть осадки, то может быть и гроза
			if(Math.max(rain,snow) > MAX_V / 2){
				final var ups = random(Long.rotateLeft(key, 6));
				if(ups && storm < MAX_V) storm++;
				else if(!ups && storm > 0) storm--;
			} else if(Math.max(rain,snow) > MAX_V / 4){
				if(random(Long.rotateLeft(key, 6)) && storm > 0) storm--;
			} else if(storm > 0) {
				storm--;
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
		/**Коэффициент перевода ветра в скорость пикселей/кадр. Где число - максимальная скорость, пк/кадр*/
		public final static double WIND_TO_SPEED = 1d / Wether.MAX_V;
		/**Счётчик для работы всех рандомайзеров*/
		private static int hashCounter = 0;
		
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
		/**Показывающий как мы отрисовывам облачко - как левое или как правое. */
		private boolean isRight = false;
		/**Все дуги, из которых может состоять облачко*/
		private final Arc2D.Double[] arcs = new Arc2D.Double[COUNT_C * (COUNT_C + 1) / 2];
		/**Это дождливая тучка?*/
		private boolean isRainable = false;
		
		/**Капельки дождя*/
		public List<Rain> blobs = new ArrayList<>();
		/**Количество реально оторбражаемых капелек дождя от этой тучки*/
		public int countBlob = 0;
		
		/**Создание облачка*/
		public Cloud(){
		}
		/** Пересоздаёт облачко
		 * @param width ширина экрана
		 * @param height высота под облачка
		 * @param wether текущая погода
		 * @param size_ размер облачка в зависимости от его расположения в массиве. Передние облака побольше задних. [0.5; 1.0]
		 */
		public void regenerate(int width,int height,final Wether wether, double size_){
			isVisible = true;
			this.isRight = wether.rightWind;
			isRainable = Utils.Utils.randomByHash(hashCounter++, 0, Wether.MAX_V) < wether.wind;
			if(wind == 0){
				//У нас штиль, но нам нужно создать облако. Создаём самое маленькое, в любом месте
				count = width_line = 1;
				location.x = Utils.Utils.randomByHash(hashCounter++, 0, width);
			} else {
				//У нас нормальная погода. Можно и облако замутить нормальное за границами экрана
				width_line = Utils.Utils.normalize_value(Utils.Utils.randomByHash(hashCounter++, 0, wether.cloud), 0, Wether.MAX_V, 1, COUNT_C);
				final var maxC = (width_line*(width_line+1))/2; //Сколько у нам может быть дуг максимум
				count = width_line == 1 ? 1 : Utils.Utils.randomByHash(hashCounter++, maxC - width_line, maxC);
			}
			this.wind = (Utils.Utils.randomByHash(hashCounter++, 50, 150) / 100d) * WIND_TO_SPEED * wether.wind;
			if(!this.isRight) this.wind = -this.wind;
			scale = size_;
			heightOne = HEIGHT_PER_CL * scale;
			widthOne = WIDTH_PER_CL * scale;
			size.width = (int) (width_line * widthOne);
			size.height = (int) (width_line * heightOne);
			if(this.isRight) location.x = - size.width;
			else location.x = width + size.width;
			location.y = Utils.Utils.randomByHash(hashCounter++, 0, height - Cloud.HEIGHT_PER_CL * 2) - size.height - heightOne/2;
			
			{//Буфферизируем наши дуги
				var r = width_line;
				var y = location.y;
				var countC = 0;
				for (int i = 0; i < count; i++) {
					if (arcs[i] == null) {
						arcs[i] = new Arc2D.Double(0, y, widthOne, heightOne * 2, 0, 180, Arc2D.OPEN);
					} else {
						final var a = arcs[i];
						a.width = widthOne;
						a.height = heightOne * 2;
						arcs[i].y= y;
					}
					if(++countC == r){
						countC = 0;
						r--;
						y -= heightOne;
					}
				}
			}
		}
		/**Добавляет капельку дождя*/
		public void addRain(){
			Rain rain = null;
			int index = 0;
			if(countBlob < blobs.size()){
				//Используем уже существующее облако
				for (int b = 0; b < blobs.size(); b++) {
					final var blob = blobs.get(b);
					if(!blob.isVisible){
						rain = blob;
						index = b;
						break;
					}
				}
			} else {
				//Надо создать новое облако
				rain = new Rain();
				blobs.add(rain);
				index = blobs.size() - 1;
			}
			if(width_line == 1)
				rain.regenerate(location.x,location.y, (int) (widthOne / 2));
			else
				rain.regenerate(location.x,location.y, Utils.Utils.randomByHash(hashCounter++, 0, size.width));
			countBlob++;
		}
		/** Рисует облачко
		 * @param g холст, на котором его надо нарисовать
		 */
		private void paint(Graphics2D g, WorldView.Transforms t) {
			if(countBlob > 0){ //Отрисовываем капельки, если нужно
				g.setColor(Color.BLACK);
				blobs.stream().filter(b -> b.isVisible).forEach(b -> b.paint(g, t));
			}
			
			var r = width_line;
			var y = location.y;
			var countC = 0;
			var x = t.toDScrinX(location.x);
			if(isRight || count <= r){
				for(var i = 0 ; i < count ; i++){
					final var a = arcs[i];
					a.x = x;
					paintCloud(g, a);
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
				x += r * widthOne; //А теперь будем рисовать с другого конца, поэтому х перемещаем
				for(var i = 0 ; i < count ; i++){
					final var a = arcs[i];
					a.x = x;
					paintCloud(g, a);
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
		
		/** Рисует дугу облачка
		 * @param g холст, на котором его надо нарисовать
		 * @param a дуга, которая представляет из себя облачко
		 */
		private void paintCloud(Graphics2D g, Arc2D.Double a) {
			g.setColor(AllColors.SKY);
			g.fill( a);
			g.setColor(Color.BLACK);
			g.draw( a);
		}
	}
	/**Одна капелька дождя*/
	private static class Rain{
		/**Высота одной капли дождя*/
		private final static int HEIGHT_BLOB = 5;
		
		/**Нужно ли рисовать объект*/
		public boolean isVisible = false;
		/**Координата по X на которой существует каелька. Координата в клетках игрового поля!*/
		private double xCell = 0;
		/**Дополнительное смещение в пикселях по оси X для капельки. Делается специально, потому что базово позиция определяется в координатах мира, а дополнительно - экрана.*/
		private double xOffset = 0;
		/**Координата y в пикселях, где находится капля*/
		private double y = 0 ;
		/**Высота капельки в пикселях*/
		private double height = HEIGHT_BLOB;
		/**Росчерк капельки*/
		private final Line2D.Double line = new Line2D.Double();
		
		/**Обновляет параметры капельки
		 * @param x линия x на которой будет падать капелька
		 * @param y стартовая высота капельки
		 */
		public void regenerate(double x, double y, int xOffset){
			isVisible = true;
			
			this.xOffset = xOffset;
			xCell = x;
			this.y = y;
			height = HEIGHT_BLOB;
		}
		
		
		/** Рисует каплюньку
		 * @param g холст, на котором его надо нарисовать
		 */
		private void paint(Graphics2D g, WorldView.Transforms t) {
			var x = t.toDScrinX(xCell) + xOffset;
			line.setLine(x, y, x, y+height);
			g.draw(line);
		}
		
	}
			
	/**Класс со всеми статическими переменными*/
	private static class Static{
		/**Ожидаемая погода*/
		public final Wether expectation = new Wether();
		/**"Состояние" мира. Это число, которое можно считать номером состояния начиная от нулевого*/
		public long next_step_update = -1;
		/**Облака на небе*/
		public final List<Cloud> clouds = new ArrayList<>();
		/**Количество реально оторбражаемых облаков на небе*/
		public int countCloud = 0;
		/**Количество реально оторбражаемых капелек дождя на небе*/
		public int countBlob = 0;
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
				if(wr) forEachVisibleCloud(c -> c.wind -= Cloud.WIND_TO_SPEED);
				else forEachVisibleCloud(c -> c.wind += Cloud.WIND_TO_SPEED);
			} else if(w < state.expectation.wind) {
				if(wr) forEachVisibleCloud(c -> c.wind += Cloud.WIND_TO_SPEED);
				else forEachVisibleCloud(c -> c.wind -= Cloud.WIND_TO_SPEED);
			}
			if(wr != state.expectation.rightWind){ //Если ветер поменял сторону
				if(state.expectation.rightWind)
					forEachVisibleCloud(c -> c.wind = Math.abs(c.wind));
				else
					forEachVisibleCloud(c -> c.wind = -Math.abs(c.wind));
			}
		}
	}
	@Override
	protected void nextFrame(){
		if(state.countCloud != 0)updateCloudPos();
		final var expectedCloud = (state.expectation.cloud) * maxCloud / Wether.MAX_V;
		if(state.countCloud < expectedCloud) generateCloud();
	}
	/**Пеерсчитывает позицию облаков*/
	private void updateCloudPos(){
		final var countCloud = state.countCloud;
		state.countCloud = 0;
		for (int i = 0, cc = 0; i < state.clouds.size() && cc < countCloud; i++) {
			final var cloud = state.clouds.get(i);
			if(!cloud.isVisible) continue;
			else cc++;
			cloud.location.x += cloud.wind;
			final var cx = transform.toScrinX(cloud.location.x);
			if (cx < -(cloud.size.width + Cloud.WIDTH_PER_CL) || cx > width + cloud.size.width || cloud.location.y + cloud.size.height > height_sky){
				cloud.isVisible = false;
				continue;
			}
			state.countCloud++;
			if(state.expectation.rain > 0 && cloud.isRainable){
				//У дождливой тучки могут появиться капельки
				if(Utils.Utils.randomByHash(state.hashCountr++, 0, Rain.HEIGHT_BLOB + height_sky * (Wether.MAX_V-state.expectation.rain) / Wether.MAX_V) == 0){
					//У нас будет капелька!
					cloud.addRain();
					state.countBlob++;
				}
			}
		}
		if(state.countBlob > 0){
			final var countBlob = state.countBlob;
			state.countBlob = 0;
			for (int i = 0, cc = 0; i < state.clouds.size() && cc < countBlob; i++) {
				final var cloud = state.clouds.get(i);
				if(cloud.countBlob > 0){
					final var ccb = cloud.countBlob;
					cloud.countBlob = 0;
					final var blobs = cloud.blobs;
					for (int j = 0, cb = 0; j < blobs.size() && cb < ccb; j++) {
						final var blob = blobs.get(j);
						if(!blob.isVisible) continue;
						else cb++;
						blob.y++; //Сдвижка на 1 пиксель за кадр
						if(blob.y >= height_sky){
							blob.isVisible = false;
							continue;
						}
						if(blob.y + blob.height > height_sky) blob.height = height_sky - blob.y;
						cloud.countBlob++;
					}
					state.countBlob += cloud.countBlob;
				}
			}
		}
	}
	/**Создаёт облачко*/
	private void generateCloud(){
		Cloud cloud = null;
		int index = 0;
		if(state.countCloud < state.clouds.size()){
			//Используем уже существующее облако
			for (int i = 0; i < state.clouds.size(); i++) {
				final var c = state.clouds.get(i);
				if(!c.isVisible){
					cloud = c;
					index = i;
					break;
				}
			}
		} else {
			//Надо создать новое облако
			cloud = new Cloud();
			state.clouds.add(cloud);
			index = state.clouds.size() - 1;
		}
		final var expectedCloud = (state.expectation.cloud) * maxCloud / Wether.MAX_V;
		cloud.regenerate(width, height_sky,state.expectation, Utils.Utils.normalize_value(index, 0, expectedCloud, 10, 100)/100d);
		cloud.location.x = transform.toWorldX(cloud.location.x);
		state.countCloud++;
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g) {
		sky.paint(g);
		dirt.paint(g);
		forEachVisibleCloud(c -> c.paint(g, transform));
	}
	
}
