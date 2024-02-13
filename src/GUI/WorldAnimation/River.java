/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ColorRec;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Анимация для реки.
 * Река имеет два берега - правый и левый, а с остальных частей сшита
 * Река волнуется а с четырёх сторон к ней подходят четыре небольших ручейка.
 * 
 * Волны:
 * Каждая волная это набор пружинок I I I I соединённых друг с другом.
 * Волнуем первую - она тянет за собой вторую и т.д.
 * 
 * @author Kerravitarr
 */
public class River extends DefaultAnimation{
	/**Точка на берегу. Кубические координаты
	 *
	 * Рассмотрим кубическую систему координат
	 *  
	 *	-2,-2 -1,-2  0,-2  1,-2   2,-2  3,-2
	 *	  -2,-1 -1,-1  0,-1  1,-1   2,-1  3,-1 
	 *	-3,0  -2,0  -1,0   q,r   1,0   2,0   3,0
	 *	  -3,1  -2,1  -1,1   0,1   1,1   2,1   3,1
	 *  -4,2  -3,2  -2,2  -1,2   0,2   1,2   2,2
	 *    -4,3  -3,3  -2,3  -1,3   0,3   1,3   2,2
	 * 
	 * Так что q - изменяется по диагонали. А r - строка, просто строка
	 */
	private static class HexPoint{

		/**Направление*/
		private static enum Direction{
					 UP_RIGHT(-1,0),UP_LEFT(-1,1),
					RIGHT(0,-1), CENTER(0,0),LEFT(0,1),
					DOWN_RIGHT(1,-1),DOWN_LEFT(1,0)
			;
			/**Все направления*/
			public final static Direction[] values = Direction.values();
			/**Все направления, кроме центрального*/
			public final static Direction[] sides = Arrays.stream(values).filter(v -> v != CENTER).toArray(Direction[]::new);
			/**Смещение строки*/
			private final int dr;
			/**Смещение столбца, если строка чётная и если строка нечётная*/
			public final int dq;
			private Direction(int dr, int dq){this.dr = dr;this.dq = dq;}
		}
		
		/**Диагональ - oblique*/
		private int que;
		private int row;
		public HexPoint(){this(0,0);}
		public HexPoint(int que, int row){this.que = que;this.row = row;}
		/**Создаёт точку
		 * @param d направление, в котором эта точка находится, относительно начала координат
		 * @param radius удаление от начала координат
		 */
		public HexPoint(Direction d, int radius){this(d.dq*radius, d.dr*radius);}
		
		
		/**Складывает две точки. Делает так, будто эта точка теперь находится не относитсельно начала координат, а относительно точки a
		 * @param a
		 * @return эта точка с новыми координатами
		 */
		public HexPoint add(HexPoint a){
			return add(this, a);
		}
		public HexPoint next(Direction d){que += d.dq; row += d.dr; return this;}
		/**Складывает две точки. Делает так, будто точка a теперь находится не относитсельно начала координат, а относительно точки b
		 * @param a
		 * @param b
		 * @return точка a с новыми координатами
		 */
		public static HexPoint add(HexPoint a, HexPoint b){
			a.que += b.que;
			a.row += b.row;
			return a;
		}
		
		@Override
		public String toString(){return "r=" + row + ";q=" + que;}
	} 
	/**Одна клетка берега*/
	private static class Cell extends java.awt.geom.Path2D.Double {
		/**Сколлько сторон будет у полигона клеток*/
		public static final int SIDES = 6;
		/**Центральная точка*/
		private java.awt.geom.Point2D.Double center = new java.awt.geom.Point2D.Double(0, 0);
		/**Радиус клетки, в пикселях*/
		private double radius = 1;
		
	
		/**Местоположение клетки*/
		public final HexPoint point;
		
		/**Ил на клетке. То, что лежит на её дне*/
		private double silt = 10;
		/**Вода на клетке. Естественно над илом!*/
		private double water = 0;
		
		public Cell(HexPoint p){
			point = p;
		}
		/**Сохраняет размеры клетки, перерисовывая её заодно
		 * @param woffset смещение относительно 0х по ширине экрана
		 * @param scale масштаб, размер одной клетки, пк/клетку
		 */
		public void setScale(int woffset, double scale){
			radius = scale * 0.9;
			final var x = scale * (Math.sqrt(3) * point.que  +  Math.sqrt(3)/2 * point.row);
			final var y = scale * (                         3./2 * point.row);
			center = new java.awt.geom.Point2D.Double(x+woffset,y);
			updatePoints();
		}

		protected void updatePoints() {
			reset();
			for (int p = 0; p < SIDES; p++) {
				final var angle = (((double)p)/SIDES) * Math.PI * 2 + Math.PI / 2;
				final var x = (center.x + Math.cos(angle) * radius);
				final var y = (center.y + Math.sin(angle) * radius);
				if(p == 0){
					moveTo(x, y);
				} else {
					lineTo(x, y);
				}
			}
		}
		public void draw(Graphics2D g, int lineThickness, int colorValue) {
			// Store before changing.
			final var tmpS = g.getStroke();
			final var tmpC = g.getColor();

			g.setColor(new java.awt.Color(colorValue));
			g.setStroke(new java.awt.BasicStroke(lineThickness, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_MITER));

			g.fill(this);

			// Set values to previous when done.
			g.setColor(tmpC);
			g.setStroke(tmpS);
		}
	}
	/**Волна. Её один элемент*/
	private static class Wave{
		/**Размер волны. Расстояние между точками волны*/
		public final static double WIDTH = 10;
		/**Максимальная высота волны, пк*/
		public final static int HEIGHT = 25;
		/**Сопротивление движению волнам*/
		public final static double FRICTION = 1;
		/**Коэффициент упругости (жёсткости) волны*/
		public final static double K = 2;
		/**Масса, мера инертности, волны. Кг*/
		public final static double m = 1000;
		
		/**Непосредственно точка волны*/
		public static class Point{
			/**Текущая сила воздействия на точку, кг*м/кадр2*/
			public double F = 0;
			/**Текущая скорость точки, пк/кадр*/
			public double V = 0;
			/**Текущаяя высота точки. Проценты от максимальной высоты. [0,1]*/
			public double H = 0;
			/**Позиция по оси на экрае, пк*/
			public double x = 0;
			
			public void setF(double f){F = f;}
			public void setX(int border, boolean isLeft){
				final var a = F / Wave.m;
				V += a;
				H += V;
				if(H > 1){
					V = 0;
					H = 1;
				} else if(H < 0){
					V = 0;
					H = 0;
				} 
				final var height = H * Wave.HEIGHT;
				x = border + (isLeft ? - height : height);
			}
		}
		
		/**Волна*/
		public final Point wave = new Point();
		/**Позиция по оси на экране, пк*/
		public double y;
		
		
		public Wave(double y){this.y = y;}
		
		@Override public String toString(){return "H="+wave.H+"% y="+y;}
	}
	/**Берег реки*/
	private static class RiverBank{
		/**Количество отображаемых волн*/
		public int countWave = 0;
		/**Реально число волн. Сюда входят те волны, которые официально не отмечаются на экране*/
		private int realCW = 0;
		/**Все волны этого берега */
		public final List<Wave> waves = new ArrayList<>();
		
		/**Непосредственно фигура, которую образуют волны этого берега*/
		public final Path2D.Double figure = new Path2D.Double();
		
		private static final double minV = 0.00125;
		private static final double maxV = 0.02;
		
		/**Начальное положение самого первого элемента волн, радианы*/
		private double angle1 = - Math.PI/2;
		private double angle2 = - angle1;
		/**Скорость, с которой волна колеблется. Радиан на кадр*/
		private double speed1 = (minV + maxV) / 2;
		private double speed2 = speed1;
		
		/**Карта самого поля
		 * Представляет собой высокосортную жесть:
		 * Каждый ряд карты (первое число) - row.
		 * А каждый столбец (второе числоа) - q.
		 * А жесть в том, что в квадратной гесанальной сетке часть клеток... Пустота! null
		 */
		private Cell[][] map = new Cell[0][0];
		
		/**Добавляет волны */
		private void addWave(int newSize){
			realCW = newSize + 3; //Дополнительные нужны, чтобы колебания выглядели натурально, хотя часть волн не могут колебаться сами по себе
			for(var i = countWave; i < realCW ; i++){
				if(waves.size() > i) continue;
				waves.add(new Wave((i - 1) * Wave.WIDTH));
			}
			countWave = newSize;
		}
		/**Установить количество волн на берегу*/
		public void setCountWaves(int newSize) {
			if (countWave > newSize) {
				countWave = newSize;
				realCW = newSize + 3;
			} else if (countWave < newSize) {
				addWave(newSize);
			}
		}
		/**Обновляет волны
		 * @param border граница игрового поля. Волны должны бегать вокруг него
		 * @param isLeft волны слева от границы бегают?
		 */
		public void updateWaves(int border, boolean isLeft){
			final var first = waves.get(0);
			final var last = waves.get(realCW - 1);
			{
				//Первую и последюю точки мы на синусе крутим
				
				final var prefF = first.wave.H;
				first.wave.H = 0.5 + Math.sin(angle1 += speed1)/2;
				if(prefF >= 0.5 && first.wave.H < 0.5){
					speed1 = Utils.Utils.betwin(minV, speed1 * Utils.Utils.random(90, 110) / 100d, maxV);
				}
				final var prefL = last.wave.H;
				last.wave.H = 0.5 + Math.sin(angle2 -= speed2)/2;
				if(prefL >= 0.5 && last.wave.H < 0.5){
					speed2 = Utils.Utils.betwin(minV, speed2 * Utils.Utils.random(90, 110) / 100d, maxV);
				}
				first.wave.x = last.wave.x = border;
			}
			//Остальные точки - мы высчитываем силы.
			for (int i = 1; i < realCW - 1; i++) {
				final var pref =  waves.get(i-1);
				final var point =  waves.get(i);
				final var next =  waves.get(i+1);
				
				final var f1 = -Wave.K * (point.wave.H - pref.wave.H);
				final var f2 = -Wave.K * (point.wave.H - next.wave.H);
				final var friction = -point.wave.V * Wave.FRICTION;
				point.wave.setF((f1 + f2) + friction);
			}
			//Ну и обновляем позиции
			for (int i = 1; i < realCW - 1; i++) {
				final var wave =  waves.get(i);
				wave.wave.setX(border, isLeft);			
			}
			
			figure.reset();
			figure.moveTo(first.wave.x,first.y);
			for (int i = 1; i < realCW; i++) {
				final var pref =  waves.get(i-1);
				final var point =  waves.get(i);
				if(i < realCW - 1){
					final var next =  waves.get(i+1);
					curveTo(figure, pref.wave.x, pref.y,point.wave.x, point.y,next.wave.x, next.y);
				} else {
					final var prefpref =  waves.get(i-2);
					curveToEnd(figure, prefpref.wave.x, prefpref.y,pref.wave.x, pref.y,point.wave.x, point.y);
				}
			}
		}
		/**Обновляет дополнительные реки
		 * @param height высота игрового поля в клетках
		 * @param isLeft волны слева от границы бегают?
		 */
		public void regenerateRiver(int height, boolean isLeft) {
			map = new Cell[height][height + (height / 2 + height % 2)];
			for (int r = 0; r < height; r++) {
				for (int c = 0; c < height; c++) {
					final var q = c - (r + (r&1)) / 2;
					map[r][c] = new Cell(new HexPoint(q, r));
				}
			}
			final var Rs = height / 2; //Радиус реки изначально.
			//А теперь прокопаем реку!
			//Находим точку, удалённую от центра копания - то есть от верхнего угла, в одну из сторон
			HexPoint point;
			if(isLeft)
				point = (new HexPoint(HexPoint.Direction.sides[0], Rs)).add(new HexPoint(height,0));
			else
				point = (new HexPoint(HexPoint.Direction.sides[0], Rs)).add(new HexPoint());
			//И копаем по кругу
			for (final var d : HexPoint.Direction.sides) {
				for (int j = 0; j < Rs; j++) {
					if(isValid(point)){
						final var cell = get(point);
						cell.silt /= 2; //В русле глубина в половину ниже
					}
					point = point.next(d);
				}
			}
			//А теперь с другой стороны копаем
			if(isLeft)
				point = (new HexPoint(HexPoint.Direction.sides[0], Rs)).add(new HexPoint(height/2,height));
			else
				point = (new HexPoint(HexPoint.Direction.sides[0], Rs)).add(new HexPoint(-height/2, height));
			//И копаем по кругу
			for (final var d : HexPoint.Direction.sides) {
				for (int j = 0; j < Rs; j++) {
					if(isValid(point)){
						final var cell = get(point);
						cell.silt /= 2; //В русле глубина в половину ниже
					}
					point = point.next(d);
				}
			}
		}
		
		public void forEach(Consumer<Cell> action){
			final var height = map.length;
			for (int r = 0; r < height; r++) {
				for (int c = 0; c < height; c++) {
					final var q = c - (r + (r&1)) / 2;
					action.accept(map[r][c]);
				}
			}
		}
		/**Сохранить размеры поля рядом с рекой
		 * @param woffset смещение относительно нуля
		 * @param scale размер клетки, пк/клетку
		 */
		private void setSize(int woffset, double scale) {
			forEach(c -> c.setScale(woffset, scale));
		}
		/**Строит дугу
		 * @param where к какому пути добавить дугу
		 * @param x1 координаты предыдущей точки
		 * @param y1 координаты предыдущей точки
		 * @param x2 координаты текущей точки
		 * @param y2 координаты текущей точки
		 * @param x3 координаты следующей точки
		 * @param y3 координаты следующей точки
		 */
		private void curveTo(Path2D.Double where,double x1, double y1, double x2, double y2, double x3, double y3){
			final var cx1a = x1 + (x2 - x1) / 3;
			final var cy1a = y1 + (y2 - y1) / 3;
			final var cx1b = x2 - (x3 - x1) / 3;
			final var cy1b = y2 - (y3 - y1) / 3;
			where.curveTo(cx1a, cy1a, cx1b, cy1b, x2, y2);
			//where.lineTo(x2, y2);
		}
		/**Строит дугу
		 * @param where к какому пути добавить дугу
		 * @param x1 координаты предыдущей предыдущей точки
		 * @param y1 координаты предыдущей предыдущей точки
		 * @param x2 координаты предыдущей точки
		 * @param y2 координаты предыдущей точки
		 * @param x3 координаты текущей точки
		 * @param y3 координаты текущей точки
		 */
		private void curveToEnd(Path2D.Double where,double x1, double y1, double x2, double y2, double x3, double y3){
			final var cx2a = x2 + (x3 - x1) / 3;
			final var cy2a = y2 + (y3 - y1) / 3;
			final var cx2b = x3 - (x3 - x2) / 3;
			final var cy2b = y3 - (y3 - y2) / 3;
			where.curveTo(cx2a, cy2a, cx2b, cy2b, x3, y3);
			//where.lineTo(x3, y3);
		}
		/**Проверяет точку на принадлежность к полю*/
		private boolean isValid(HexPoint p){
			final var rq = p.que + p.row / 2;
			return p.row >= 0 && p.row < map.length && rq >= 0 && rq < map[0].length;
		}
		/**Возвращает ячейку поля
		 * @param p
		 * @return 
		 */
		private Cell get(HexPoint p){
			assert isValid(p) : "Точка оказалась за границей! Точка " + p + " при поле " + map.length + "х"+ map.length;
			return map[p.row][p.que + p.row / 2];
		}

	}
	/**Все статические переменные*/
	private static class Static{
		/**Количество отображаемых волн*/
		public int countWave = 0;
		/**Левый берег реки*/
		public final RiverBank left = new RiverBank();
		/**Правый берег реки*/
		public final RiverBank right = new RiverBank();
		
		/**Установить количество волн на обоих берегах*/
		public void setCountWaves(int newSize){
			left.setCountWaves(newSize);
			right.setCountWaves(newSize);
			countWave = newSize;
		}
		
		/**Обновляет волны*/
		public void updateWaves(int leftBorder, int rightBorder){
			left.updateWaves(leftBorder, true);
			right.updateWaves(rightBorder, false);
		}
		/**Обновляет дополнительные реки
		 * @param height высота игрового поля в клетках
		 */
		public void regenerateRiver(int height) {
			left.regenerateRiver(height, true);
			right.regenerateRiver(height, false);
		}
		/**Сохраняет размеры для берегов
		 * @param leftWOff Смещение игрового поля для левого берега
		 * @param rightWOff Смещение игрового поля для правого берега
		 * @param scale масштаб, пк/клетку
		 */
		private void setSize(int leftWOff,int rightWOff, double scale) {
			left.setSize(leftWOff,scale);
			right.setSize(rightWOff, scale);
		}
	}
	
	/**Состояние анимации*/
	private static Static state;
	/**Берег левый*/
	private final ColorRec left;
	/**водичка*/
	private final ColorRec water;
	/**Берег правый*/
	private final ColorRec right;
	
	/**Количество волн для текущей анимации*/
	private final int countWave;
	/**Левая граница игрового поля*/
	private final int leftBorder;
	/**Правая граница игрового поля*/
	private final int rightBorder;
	/**Масштаб или размер одной клетки шестиугольника, пк/клетку*/
	private final double scale;
	
	public River(WorldView.Transforms transform, int w, int h){
		//Левый песочек
		int xl[] = new int[4];
		//Поле, вода
		int xw[] = new int[4];
		int yw[] = new int[4];
		//Правый песочек
		int xr[] = new int[4];

		xl[0] = xl[3] = 0;
		xl[1] = xl[2] = xw[0] = xw[3] = transform.toScrinX(0);
		xw[1] = xw[2] = xr[0] = xr[3] = transform.toScrinX(Configurations.getWidth()-1);
		xr[1] = xr[2] = w;

		yw[0] = yw[1] = 0;
		yw[2] = yw[3] = h;
		left = new ColorRec(xl,yw,AllColors.SAND);
		water = new ColorRec(xw,yw, AllColors.WATER_RIVER);
		right = new ColorRec(xr,yw, AllColors.SAND);
		
		countWave = (int) Math.ceil(h / Wave.WIDTH);
		leftBorder = transform.toScrinX(0);
		rightBorder = transform.toScrinX(Configurations.getWidth()-1);
		final var ch = 10;
		scale = ((double)h) / ch;
		
		if(state == null) state = new Static();
		if(state.left.map.length != ch)
			state.regenerateRiver(ch);
		state.setSize( 0, rightBorder, scale);
	}
		
	@Override
	protected void nextFrame(){
		if(countWave <= 0) return;
		if(state.countWave != countWave)
			state.setCountWaves(countWave);
		state.updateWaves(leftBorder, rightBorder);
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g) {
		left.paint(g);
		right.paint(g);
		
		g.setColor(AllColors.WATER_RIVER);
		g.fill(state.left.figure);
		g.fill(state.right.figure);
		
		state.left.forEach(c -> c.draw(g, 0, 0x008844));
		//state.right.forEach(c -> c.draw(g, 0, 0x008844));
		
	}
	
}
