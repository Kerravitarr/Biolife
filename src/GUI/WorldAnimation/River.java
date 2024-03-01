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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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
		private static enum Direction {
			RIGHT(1,0,"→"),
			UP_RIGHT(1,-1,"↗"),
			UP_LEFT(0,-1,"↖"),
			LEFT(-1,0,"←"),
			DOWN_LEFT(-1,1,"↙"),
			DOWN_RIGHT(0,1,"↘"),
			CENTER(0, 0,"⟲"),
			;
			/**Все направления*/
			public final static Direction[] values = Direction.values();
			/**Все направления, кроме центрального*/
			public final static Direction[] sides = Arrays.stream(values).filter(v -> v != CENTER).toArray(Direction[]::new);
			/**Смещение строки*/
			private final int dr;
			/**Смещение столбца, если строка чётная и если строка нечётная*/
			public final int dq;
			public final String sym;
			private Direction(int dq, int dr, String sym){this.dr = dr;this.dq = dq;this.sym = sym;}
			/**Проверяет, что два направления находятся рядом*/
			public boolean isNear(Direction d){
				return this == d || (d != CENTER && this != CENTER && (Math.abs(this.ordinal() - d.ordinal()) == 1 || (this == RIGHT && d == DOWN_RIGHT)|| (this == DOWN_RIGHT && d == RIGHT)));
			}
			public Direction next(){
				return this == CENTER ? this : (this == DOWN_RIGHT ? RIGHT : sides[ordinal() + 1]);
			}
			public Direction prefur(){
				return this == CENTER ? this : (this == RIGHT ? DOWN_RIGHT : sides[ordinal() - 1]);
			}
			public Direction back(){
				return this == CENTER ? this : sides[(ordinal() + sides.length / 2) % sides.length];
			}
			public static Direction valueOf(int dq, int dr){
				switch (dq) {
					case -1 -> {
						switch (dr) {
							case -1 -> {return UP_LEFT;}
							case 0 -> {return LEFT;}
							case 1 -> {return DOWN_LEFT;}
							default -> throw new AssertionError();
						}
					}
					case 0 -> {
						switch (dr) {
							case -1 -> {return UP_LEFT;}
							case 0 -> {return CENTER;}
							case 1 -> {return DOWN_RIGHT;}
							default -> throw new AssertionError();
						}
					}
					case 1 -> {
						switch (dr) {
							case -1 -> {return UP_RIGHT;}
							case 0 -> {return RIGHT;}
							case 1 -> {return DOWN_RIGHT;}
							default -> throw new AssertionError();
						}
					}
					default -> throw new AssertionError();
				}
			}
			@Override public String toString(){return sym;}
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
		public HexPoint clone(){
			return new HexPoint(que, row);
		}
		
		@Override
		public String toString(){return "q;r=" + que + ";" + row;}
	} 
	private static class HexVector{
		private double que;
		private double row;
		private HexPoint.Direction _direction = null;
		private double _lenght = -1;
		public HexVector(){this(0,0);}
		public HexVector(double q, double r){que = q; row = r;};
		/**Создаёт вектор, направленный от точки from к точке to
		 * @param from
		 * @param to 
		 */
		public HexVector(HexPoint from, HexPoint to){this(to.que - from.que, to.row - from .row);};
		/**Прибавляет вектор к вектору */
		public HexVector add(HexVector add){return add(add.que, add.row);}
		/**Прибавляет вектор к вектору */
		public HexVector add(double q, double r){
			_direction = null;_lenght = -1;
			que += q;
			row += r;
			return this;
		}
		/**Увеличивает вектор на заданую велечину*/
		public HexVector scale(double p){
			_direction = null;_lenght = -1;
			que *= p;
			row *= p;
			return this;
		}
		/**Уменьшает вектор на заданую велечину*/
		public HexVector div(double p){
			_direction = null;_lenght = -1;
			que /= p;
			row /= p;
			return this;
		}
		public double lenght(){
			if(_lenght < 0)
				_lenght = (Math.abs(que)+Math.abs(que + row)+Math.abs(row)) / 2;
			return _lenght;
		}
		/**Нормализует вектор и возвращает его-же*/
		public HexVector normalize(){
			div(lenght());
			return this;
		}
		public HexPoint.Direction direction(){
			if(_direction == null){
				final var nq = que / lenght();
				final var nr = row / lenght();
				_direction = HexPoint.Direction.valueOf((int)Math.round(nq),(int)Math.round(nr));
			}
			return _direction;
		}
		@Override
		public HexVector clone(){
			return new HexVector(que, row);
		}
		@Override
		public String toString(){return String.format("q;r=%.1f;%.1f%s", que , row, direction());}
	}
	/**Кораблик, который симулирует течение*/
	private static class Ship {
		/**Клетка, где кораблик находится*/
		private Cell cell;
		/**Позиция кораблика*/
		private HexVector point;
		
		/**Проверяет - плывёт ещё кораблик или уже нет?*/
		public boolean isActiv(){return cell != null;}
		/**Начало кораблика*/
		public void start(Cell c){
			cell = c;
			cell.isShip = true;
			point = new HexVector(cell.point.que,cell.point.row);
		}
		/**Шаг кораблика*/
		public void step(RiverBank bank){
			cell.isShip = false;
			point.add(cell.flow.direction().dq,cell.flow.direction().dr);
			if(bank.isValid(point)){
				cell = bank.get(point);
				cell.isShip = true;
			} else {
				cell = null;
			}
		}
	}
	/**Одна клетка берега*/
	private static class Cell extends java.awt.geom.Path2D.Double {
		/**Сколько земли тут будет изначально +- 0,5*/
		public static final double SILT_LV_DEF = 0.5;
		/**Сколлько сторон будет у полигона клеток*/
		private static final int SIDES = 6;
		/**Скорость перестройки ланшафта. Как быстро появляются реки и как быстро они исчезают*/
		private static final double EVOLUTION_SPEED = 0.1;
		
		/**Центральная точка*/
		private java.awt.geom.Point2D.Double center = new java.awt.geom.Point2D.Double(0, 0);
		/**Радиус клетки, в пикселях*/
		private double radius = 1;
	
		/**Местоположение клетки*/
		public final HexPoint point;
		/**Ил на клетке. То, что лежит на её дне*/
		private double _silt;
		/**Вода на клетке. Естественно над илом!*/
		private double _water = 0;
		/**Вода на клетке. Для следующего хода*/
		private double _water_next = 0;
		/**Количество корней на клетке. Показывает на сколько клетка не хочет отдавать свой ил*/
		public double roots = 0;
		/**Цвет клетки*/
		private Color color = AllColors.SAND;
		/**Флаг, показывающий что клетка является крайней, за ней - река. Показывается направление, в котором река*/
		private HexPoint.Direction isEnd = null;
		/**Массив всех соседей клетки*/
		private java.util.EnumMap<HexPoint.Direction,Cell> neighbours = null;
		/**Наличие на клетке "кораблика"*/
		private boolean isShip = false;
		
		/**Течение на клетке. Куда и как сильно*/
		private HexVector flow = new HexVector();
		/**Течение на клетке для следующего хода*/
		private HexVector flow_next = new HexVector();
		
		public Cell(HexPoint p){
			point = p;
		}
		/** Иициализирует клетку
		 * @param isEnd направление где карта заканчивается. Относится только к крайним клеткам
		 * @param neighbours список соседних клеток
		 */
		public void init(HexPoint.Direction isEnd, java.util.EnumMap<HexPoint.Direction,Cell> neighbours){
			this.isEnd = isEnd;
			this.neighbours = neighbours;
			final var lv = (int)(SILT_LV_DEF * 100);
			setParams(0,Utils.Utils.random(lv / 2, lv + lv / 2) / 100d);
		}
		/**Сохраняет размеры клетки, перерисовывая её заодно
		 * @param woffset смещение относительно 0х по ширине экрана
		 * @param scale масштаб, размер одной клетки, пк/клетку
		 */
		public void setScale(int woffset, double scale){
			radius = scale;
			final var x = scale * (Math.sqrt(3) * point.que  +  Math.sqrt(3)/2d * point.row);
			final var y = scale * (                         3d/2d * point.row);
			center = new java.awt.geom.Point2D.Double(x+woffset,y);
			
			reset();
			for (int p = 0; p < SIDES; p++) {
				final var angle = (((double)p)/SIDES) * Math.PI * 2 + Math.PI / 2;
				final var px = (center.x + Math.cos(angle) * radius);
				final var py = (center.y + Math.sin(angle) * radius);
				if(p == 0){
					moveTo(px, py);
				} else {
					lineTo(px, py);
				}
			}
		}
		public void setParams(double water, double silt){
			//if(!(java.lang.Double.isFinite(water) && java.lang.Double.isFinite(silt) && water >= 0 && silt >= 0))
			assert java.lang.Double.isFinite(water) && java.lang.Double.isFinite(silt) && water >= 0 && silt >= 0: "Вода " + water + " грязь " + silt;
			_silt = silt;
			_water = water;
		}
		/**Уровнь клетки, относительно нуля. Высота, на которой находится верхняя кромка поля*/
		public double maxLavel(){return water() + silt();}
		public double water(){return _water;}
		/**Нужно понимать, что вода у нас будет на каждой клетке... 
		 * Поэтому если нужно узнать "приведённое" значение воды. 
		 * Вероятность, что на клетке есть вода, то обращаться нужно к этой функции
		 * @return количество воды на экране[0,1]*/
		public double waterNormalize(){return erf(0.4*water());}
		public double silt(){return _silt;}
		/**Добавляет к илу кусочек и возвращает новое значение*/
		public double silt(double add){setParams(_water, _silt + add); return _silt;}
		/**Обновляет значение клетки*/
		public void update(){
			setParams((1 - EVOLUTION_SPEED) * _water + EVOLUTION_SPEED * _water_next, _silt);
			_water_next = 0;
			flow = flow.scale((1 - EVOLUTION_SPEED)).add(flow_next.scale(EVOLUTION_SPEED));
			flow_next.scale(0);
			
			//final var s = silt+water;
			final int wAlf = (int) Math.min(255, 255*waterNormalize());
			Color sand;
			{
				final var bAlf = (int) Math.min(25, _silt*25);
				final var cs = AllColors.toDark(Color.BLACK, bAlf);
				final var cw = AllColors.toDark(AllColors.SAND, 255-bAlf);
				sand = AllColors.blendA(cs,cw);
			}
			
			final var cs = AllColors.toDark(sand, 255-wAlf);
			final var cw = AllColors.toDark(AllColors.WATER_RIVER, wAlf);
			final var cg = AllColors.toDark(Color.GREEN, (int) Math.min(25, roots*25));
			color = AllColors.blendA(cs,cw,cg);
		}
		public void draw(Graphics2D g) {
			final var tmpC = g.getColor();
			g.setColor(color);
			g.fill(this);
			
			/*if(radius > 20){
				g.setColor(Color.BLACK);
				Utils.Utils.centeredText(g, (int) center.x, (int) center.y-10, 10, water() > 0 ? String.format("w%.1f", water()) : "");
				Utils.Utils.centeredText(g, (int) center.x, (int) center.y+0, 10, String.format("s%.1f", silt()));
				Utils.Utils.centeredText(g, (int) center.x, (int) center.y+10, 10, String.format("l%.1f", maxLavel()));
				//Utils.Utils.centeredText(g, (int) center.x, (int) center.y+6, 10, point.toString() + (isEnd != null ? "e" : "") + (isMouth > 0 ? isMouth : ""));
			} else */if(isShip){
				final var h = Math.max(1, radius/2 );
				g.setColor(Color.white);
				g.drawOval((int)(center.x - h/2), (int)(center.y - h/2), (int)h, (int)h);
			}
			{
				g.setColor(Color.BLACK);
				final var x1 = (int) center.x;
				final var y1 = (int) center.y;
				final var fx = radius * (Math.sqrt(3) * flow.que  +  Math.sqrt(3)/2d * flow.row);
				final var fy = radius * (                         3d/2d * flow.row);
				final var x2 = (int) (center.x + fx);
				final var y2 = (int) (center.y + fy);
				g.drawLine(x1, y1, x2, y2);
			}
			g.setColor(tmpC);
		}
		
		/**Расчитывает функцию ошибок гауса. Делает это с точностью около 1.2 * 10 ^ -7. Правда около 0, конечно, всё идёт по одному месту
		 * @param z
		 * @return 
		 */
		private static double erf(double z) {
			double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

			//Считаем по методу Горнера
			double ans = 1 - t * Math.exp(-z * z - 1.26551223
					+ t * (1.00002368
					+ t * (0.37409196
					+ t * (0.09678418
					+ t * (-0.18628806
					+ t * (0.27886807
					+ t * (-1.13520398
					+ t * (1.48851587
					+ t * (-0.82215223
					+ t * (0.17087277))))))))));
			return z >= 0 ? ans : -ans;
		}
		@Override
		public String toString(){
			return String.format("%s w;s;m%.1f;%.1f;%.1f", point,water(),silt(),maxLavel());
		}
	}
	/**Частика, которая будет определять непосредственно форму рек*/
	private static class Drop{
		/** Максимальный возраст капли*/
		public static int MAX_AGE = 0;
		/** Минимальный запас воды в капле, прежде чем она исчезнет*/
		public static final double MIN_WATER = 0.01;
		/** Скорость испарения капли. [0,1)*/
		public static final double EVAPORATION_RATE = 0.001;
		/** Скорость забора ила на клетках при движении*/
		public static final double SETTLING = 0.1;
		/** Сила переноса ила с точки на точку*/
		public static final double SILT_TRANSPORT = 10.0;
		/** Сила гравитации*/
		public static final double GRAVITY = 1.0;
		/** Эффективность передачи импульса при движении*/
		public static final double IMPULSE_TRANSFER = 1.0;
		/** Коэффициент подвижности берегов. Показывает как много земельки идёт от клетки к клетке*/
		public static final double COLLAPSE = 0.8;
		/**Максимальная крутость берега. Круче быть не может уже*/
		public static final double SILT_STEP = 0.01;
		
		/**Возраст катящейся капли*/
		private int age = 0;
		/**Позиция капли*/
		private HexVector position ;
		/**Скорость капли*/
		private HexVector speed = new HexVector();
		/**Количество воды в капле*/
		private double water = 1;
		/**Количество ила в капле*/
		private double silt = 0;
		
		/**Создаёт клетку на нужной позиции*/
		public Drop(HexPoint pos){position = new HexVector(pos.que, pos.row);}
	}
	/**Дерево, которое укрелпяет берег*/
	private static class Plant{
		/**Максимальный размер дерева*/
		public final static double MAX_SIZE = 1.5;
		/**Скорость роста*/
		public final static double GROW_RATE = 0.05;
		/**Максимальная крутость берега, на которой может вырасти дерево*/
		public final static double MAX_STEP = 0.8;
		/**Сколько может быть максимум воды на клетке, чтобы понять, что тут нам не рости*/
		public final static double MAX_WATER = 0.3;
		/**Максимальная высота, на которой может рости дерево*/
		public final static double MAX_CELL_SILT = 0.8;
		
		/**Местоположение дерева*/
		public Cell cell = null;
		/**Размер дерева*/
		private double size = 0;
		
		/**Проверить клетку на то, что на ней можно вырости*/
		public static boolean isGoodCell(Cell c){
			if(c.waterNormalize() >= MAX_WATER || c.silt() >= MAX_CELL_SILT)
				return false;
			//Это было самое простое. Теперь сложнее - перепад высот...
			var min = Double.MAX_VALUE;
			var max = Double.MIN_VALUE;
			
			for(var cell : c.neighbours.values()){
				if(cell == null) continue;
				min = Math.min(min, c.silt());
				max = Math.max(max, c.silt());
			}
			return (max - min) < MAX_STEP;
		}
		/**Сохраняет местоположение деревца
		 * @param cell клетка, на которой её создаём
		 */
		public void setPos(Cell cell) {
			this.cell = cell;
			editRoot( 1.0);
		}
		/**Проверка дерева на смерть. Если дерево умрёт, то оно само завершит работу*/
		public boolean isDie(){
			if(cell.waterNormalize() >= MAX_WATER || cell.silt() >= MAX_CELL_SILT || Utils.Utils.random(0, 1023) == 0){
				editRoot( -1.0);
				cell = null;
				return true;
			} else {
				return false;
			}
		}
		/**Рост дерева*/
		public void grow(){
			size += GROW_RATE * (MAX_SIZE - size);
		}
		/**Создаёт корневую систему
		 * @param size сколкьо добавить (убавить) корней
		 */
		private void editRoot(double size){
			cell.roots += size;
			for(var c : cell.neighbours.values()){
				if(c != null) c.roots += size * 0.5;
			}
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
		
		private static final double MIN_V = 0.00125;
		private static final double MAX_V = 0.02;
		
		/**Начальное положение самого первого элемента волн, радианы*/
		private double angle1 = - Math.PI/2;
		private double angle2 = - angle1;
		/**Скорость, с которой волна колеблется. Радиан на кадр*/
		private double speed1 = (MIN_V + MAX_V) / 2;
		private double speed2 = speed1;
		
		/**Карта самого поля
		 * Представляет собой высокосортную жесть:
		 * Каждый ряд карты (первое число) - row.
		 * А каждый столбец (второе числоа) - q.
		 * А жесть в том, что в квадратной гесанальной сетке q ещё перевести надо в колонки!
		 */
		private Cell[][] map = new Cell[0][0];
		/**Массив клеток которые мы вызвавем для обработки*/
		private Cell[] call_cell = new Cell[0];
		/**Сколько грязи мы потеряли за время моделирования*/
		private double exitS = 0;
		
		/**Все волны этого берега */
		public final List<Plant> plants = new ArrayList<>();
		/**Количество высаженных деревьев*/
		private int plants_c = 0;
		
		/**Плавающий кораблик*/
		private List<Ship> ships = new ArrayList<>();
		
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
					speed1 = Utils.Utils.betwin(MIN_V, speed1 * Utils.Utils.random(90, 110) / 100d, MAX_V);
				}
				final var prefL = last.wave.H;
				last.wave.H = 0.5 + Math.sin(angle2 -= speed2)/2;
				if(prefL >= 0.5 && last.wave.H < 0.5){
					speed2 = Utils.Utils.betwin(MIN_V, speed2 * Utils.Utils.random(90, 110) / 100d, MAX_V);
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
		/**Обновляет кораблик. Тип течение речушек*/
		public void updateShip(){
			for (int i = 0; i < ships.size(); i++) {
				Ship ship = ships.get(i);
				if(ship.isActiv()){
					ship.step(this);
				} else {
					ship.start(call_cell[call_cell.length - 1 - i]);
				}
			}
		}
		/**Обновляет клетки рек
		 * @param index 
		 */
		public void updateRivers(int index, boolean isLeft){
			final var i = (call_cell.length - 1) - index % call_cell.length; //У нас index идёт вверх. А i должен быть [0,map.length)
			final var t = call_cell[i];
			final var j = Utils.Utils.random(0, i); // случайный индекс от 0 до i
			//Меняем местами клетки, чтобы каждый раз вызывать их в разной последовательности
			final var c = call_cell[i] = call_cell[j];
			call_cell[j] = t;
			if(exitS > Cell.SILT_LV_DEF){ //Как только накопится грязь - дарим её клетке
				c.setParams(c.water(), c.silt() + Cell.SILT_LV_DEF);
				exitS -= Cell.SILT_LV_DEF;
			}
			//Запоминаем грязьку и ходим
			final var prefS = Arrays.stream(call_cell).mapToDouble(lc -> lc == null ? 0 : lc.silt()).sum() + c.silt();
			{
				final var d = new Drop(c.point);
				while(moveDrop(d)){}
			}
			//c.step();
			final var next = Arrays.stream(call_cell).mapToDouble(lc -> lc == null ? 0 : lc.silt()).sum() + c.silt();
			exitS += prefS - next;
			if(i % map.length == 0){
				//Прошли одну сторону. Надо обновить всё поле.
				forEach(update_c -> update_c.update());
				//А ещё немного растительности добавим. Для улучшения моделирования эроозии
				//Садим дерево
				if(Plant.isGoodCell(c)){
					addPlant(c);
				}
				//Обсчитываем все деревья
				for(int plant_i = 0, plant_c = 0; plant_c < plants_c; plant_i++){
					final var p = plants.get(plant_i);
					if(p.cell == null) continue;
					if(p.isDie()){
						plants_c--;
						continue;
					}
					plant_c++;
					//Живые деревья ростут
					p.grow();
					//А ещё живые могут семечки скинуть!
					if(Utils.Utils.random(0, 31) == 0){
						//Новая позиция
						final var npos = p.cell.point.clone().next(HexPoint.Direction.sides[Utils.Utils.random(0, HexPoint.Direction.sides.length - 1)]);
						if(!isValid(npos.que,npos.row)) continue;
						final var ncell = get(npos.que,npos.row);
						if(!Plant.isGoodCell(ncell)) continue;
						//Чем больше корней - тем больше шанс вырасти дереву нормальному
						if(Utils.Utils.random(0, 1023) <= ncell.roots * 1023) continue;
						addPlant(ncell);
						plant_c++;
					}
				}
			}
		}
		/**Обновляет дополнительные реки
		 * @param height высота игрового поля в клетках
		 * @param isLeft волны слева от границы бегают?
		 */
		public void regenerateRiver(int height, boolean isLeft) {
			map = new Cell[height][height];
			call_cell = new Cell[height * height];
			for (int r = 0; r < height; r++) {
				for (int c = 0; c < height; c++) {
					final var q = c - r / 2;
					call_cell[r * height + c] = map[r][c] = new Cell(new HexPoint(q, r));
				}
			}
			final var Rs = height / 2; //Радиус реки изначально.
			forEach(c -> {
				final var p = c.point;
				final var isE = isLeft && (p.que + p.row / 2) == (map.length - 1) || !isLeft && (p.que + p.row / 2) == 0;
				final var neighbours = new java.util.EnumMap<HexPoint.Direction,Cell>(HexPoint.Direction.class);
				for(final var d : HexPoint.Direction.sides)
					neighbours.put(d, get(p,d));
				c.init(isE ? (isLeft ? HexPoint.Direction.RIGHT : HexPoint.Direction.LEFT) : null, neighbours);
			});
			//А теперь прокопаем реку!
			//Находим точку, удалённую от центра копания - то есть от верхнего угла, в одну из сторон
			HexPoint point;
			final var startD = HexPoint.Direction.sides[HexPoint.Direction.sides.length - 2];
			if(isLeft)
				point = (new HexPoint(startD, Rs)).add(new HexPoint(height-1,0));
			else
				point = (new HexPoint(startD, Rs)).add(new HexPoint());
			//И копаем по кругу
			for (final var d : HexPoint.Direction.sides) {
				for (int j = 0; j < Rs; j++) {
					if(isValid(point))
						 get(point).silt(-get(point).silt());
					point = point.next(d);
				}
			}
			//А теперь с другой стороны копаем
			if(isLeft)
				point = (new HexPoint(startD, Rs)).add(new HexPoint(height/2,height-1));
			else
				point = (new HexPoint(startD, Rs)).add(new HexPoint(-height/2, height-1));
			//И копаем по кругу
			for (final var d : HexPoint.Direction.sides) {
				for (int j = 0; j < Rs; j++) {
					if(isValid(point))
						 get(point).silt(-get(point).silt());
					point = point.next(d);
				}
			}
			//Надо обновить всё поле.
			forEach(update_c -> update_c.update());
			
			Drop.MAX_AGE = height;
			ships = new ArrayList<>(height);
			for (int i = 0; i < height; i++) {
				ships.add(new Ship());
			}
		}
		/**Проходит по всем клеткам мира*/
		public void forEach(Consumer<Cell> action){
			for(final var cc : map)
				for(final var c : cc) 
					action.accept(c);
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
		private boolean isValid(int que, int row){
			final var col = que + row / 2;
			return row >= 0 && row < map.length && col >= 0 && col < map.length;
		}
		/**Проверяет точку на принадлежность к полю*/
		private boolean isValid(HexVector v){
			return isValid((int)v.que, (int)v.row);
		}
		/**Проверяет точку на принадлежность к полю*/
		private boolean isValid(HexPoint v){
			return isValid(v.que, v.row);
		}
		/**Возвращает ячейку поля
		 * @return 
		 */
		private Cell get(int que, int row){
			assert isValid(que,row) : "Точка оказалась за границей! Точка (" + que + ";" + row + ") при поле " + map.length + "х"+ map.length;
			return map[row][que + row / 2];
		}
		/**Возвращает ячейку поля
		 * @param p
		 * @return 
		 */
		private Cell get(HexVector v){
			return get((int)v.que, (int)v.row);
		}
		/**Возвращает ячейку поля
		 * @param p
		 * @return 
		 */
		private Cell get(HexPoint v){
			return get(v.que, v.row);
		}
		/**Возвращает клетку относительно*/
		private Cell get(HexPoint p, HexPoint.Direction d){
			final var q = p.que + d.dq;
			final var r = p.row + d.dr;
			if(isValid(q,r))
				return get(q, r);
			else
				return null;
		}
		
		/**Обрабатывает движение капельки по плато
		 * @param d капелька
		 * @return true, если движение своё капелька ещё не закончила
		 */
		private boolean moveDrop(Drop d) {
			final var cell = get((int)d.position.que,(int)d.position.row);
			if(d.age > Drop.MAX_AGE || d.water < Drop.MIN_WATER){
				cell.silt(d.silt);
				return false;
			}
			//Эффективность переноса ила
			final var siltEff = Math.max(0.0, Drop.SETTLING * (1d - cell.roots));
			final var f = new HexVector();
			for(var e : cell.neighbours.entrySet()){
				final var c = e.getValue();
				if(c == null){
					double del;
					if(cell.isEnd == null){
						del = -cell.silt();
					} else {
						del = cell.silt();
					}
					f.add((new HexVector(e.getKey().dq,e.getKey().dr)).scale(del));
				} else {
					final var del = cell.silt() - c.silt();
					f.add((new HexVector(cell.point,c.point)).scale(del));
				}
			}
			if(cell.isEnd != null){ //Крайние точки дополнительно утягивает за край
				f.add((new HexVector(cell.isEnd.dq, cell.isEnd.dr)).scale(Drop.SILT_STEP));
			}
			d.speed = d.speed.add(f.scale(Drop.GRAVITY / d.water)); //F=ma => a = F/m. V = V0+at
			if(cell.flow.lenght() > 0 && d.speed.lenght() > 0){
				final var nslow = cell.flow.clone().normalize();
				final var ndrop = d.speed.clone().normalize();
				//Сколярное произведение векторов...
				//Ну вообще, если я правильно понял, то это действует для любых ситуаций...
				//Но правильно ли я понял?
				final var dot = nslow.que * ndrop.que + nslow.row * ndrop.row;
				d.speed = d.speed.add(cell.flow.clone().scale(Drop.IMPULSE_TRANSFER * dot / (d.water + cell.water()) * cell.flow.lenght()));
			}
			//Если у нас есть скорость, то мы должны сделать её такой, будто за ход клетка сразу совершила ТП на следующую клетку...
			//Сложно? Сложно. А что делать? :)
			//Можно рассматривать скорость не как непосредственно скорость, а как параметр замедления времени при моделировании
			if(d.speed.lenght() > 0){
				d.speed = d.speed.normalize();
			}
			d.position = d.position.add(d.speed);
			//А теперь обновим данные для ячейки
			cell._water_next += d.water;
			cell.flow_next.add(d.speed.clone().scale(d.water));
			
			//И так. С этой клеткой закончили. Теперь перейдём к следующей!
			final var h_next = isValid(d.position) ? get(d.position).silt() : (cell.isEnd != null ? 0 : cell.silt() * 2);
			
			//Теперь перенесём массу пропорционально высоте и объёму воды!
			final var c_eq = Math.max(0.0, (cell.silt() - h_next) * (1.0 + Drop.SILT_TRANSPORT * Cell.erf(0.4 * cell.waterNormalize())));
			final var del_c_eq = c_eq - d.silt;
			final var trans = Math.min(cell.silt(), siltEff * del_c_eq);
			
			d.silt += trans;
			cell.silt(-trans);
			
			//Теперь испаряем часть капельки
			d.silt /= 1 - Drop.EVAPORATION_RATE;
			d.water*= 1 - Drop.EVAPORATION_RATE;
			
			if(!isValid(d.position)) return false;
			
			d.age++;
			return true;
		}
		/**Сажает дерево где скажут*/
		private void addPlant(Cell c){
			Plant p = null;
			if(plants_c < plants.size()){
				for(var plant_i = 0; plant_i < plants.size(); plant_i++){
					p = plants.get(plant_i);
					if(p.cell == null) break;
				}
				assert p != null;
			} else {
				p = new Plant();
				plants.add(p);
			}
			plants_c++;
			p.setPos(c);
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
		
		/**Индекс клетки береговой реки, которая будет обновлена за выбранный кадр*/
		private int cell = 0;
		
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
		/**Обновляет кораблики*/
		public void updateShip(){
			left.updateShip();
			right.updateShip();
		}
		/**Обновляет реки по берегам*/
		public void updateRivers(){
			for (int i = 0; i < right.map.length; i++) {
				right.updateRivers(cell, true);
				left.updateRivers(cell++, false);
				if(cell < 0) cell = 0; //Если слишком много кадров отснимем, то не должно быть отрицательных чисел всё равно!
			}
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
		
		//final var count_cell = Configurations.getHeight();/**
		final var count_cell = 64;h = h * 9 / 10;/***/
		if(count_cell <= 0) {
			scale = 1;
			return;
		}
		//2/3 - это потому что поле шестиугольников имеет немного другие размеры, накладываясь в сетку
		//А -1, потому что нижний шестиугольник должен зайти за нижнюю границу экрана
		scale = (((double)h) / (count_cell - 1)) * 2d/3d; 
		
		if(state == null) state = new Static();
		if(state.left.map.length != count_cell)
			state.regenerateRiver(count_cell);
		state.setSize((int)(leftBorder - scale * Math.sqrt(3) * (count_cell)), (int) (rightBorder + scale * 2/3), scale);
	}
	@Override
	protected void nextFrame(){
		if(countWave <= 0) return;
		if(state.countWave != countWave)
			state.setCountWaves(countWave);
		state.updateWaves(leftBorder, rightBorder);
		state.updateShip();
	}
	@Override
	protected void nextStep(long step){
		state.updateRivers();
	}
	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}
	@Override
	public void world(Graphics2D g) {
		left.paint(g);
		right.paint(g);
		
		state.left.forEach(c -> c.draw(g));
		state.right.forEach(c -> c.draw(g));
		
		g.setColor(AllColors.WATER_RIVER);
		g.fill(state.left.figure);
		g.fill(state.right.figure);
		
		
	}
	
}
