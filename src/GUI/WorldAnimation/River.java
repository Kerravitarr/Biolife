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
		public String toString(){return "q;r=" + que + ";" + row;}
	} 
	private static class HexVector{
		private double que;
		private double row;
		private HexPoint.Direction direction = null;
		private double _lenght = -1;
		public HexVector(){this(0,0);}
		public HexVector(double q, double r){que = q; row = r;};
		/**Создаёт вектор, направленный от точки from к точке to
		 * @param from
		 * @param to 
		 */
		public HexVector(HexPoint from, HexPoint to){this(to.que - from.que, to.row - from .row);};
		/**Прибавляет число к вектору */
		public HexVector add(double add){return add(add, add);}
		/**Прибавляет вектор к вектору */
		public HexVector add(HexVector add){return add(add.que, add.row);}
		/**Прибавляет вектор к вектору */
		public HexVector add(double q, double r){
			direction = null;_lenght = -1;
			que += q;
			row += r;
			return this;
		}
		/**Увеличивает вектор на заданую велечину*/
		public HexVector scale(double p){
			direction = null;_lenght = -1;
			que *= p;
			row *= p;
			return this;
		}
		/**Уменьшает вектор на заданую велечину*/
		public HexVector div(double p){
			direction = null;_lenght = -1;
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
			if(direction == null){
				final var nq = que / lenght();
				final var nr = row / lenght();
				direction = HexPoint.Direction.valueOf((int)Math.round(nq),(int)Math.round(nr));
			}
			return direction;
		}
		@Override
		public HexVector clone(){
			return new HexVector(que, row);
		}
		@Override
		public String toString(){return String.format("q;r=%.1f;%.1f%s", que , row, direction());}
	}
	/**Одна клетка берега*/
	private static class Cell extends java.awt.geom.Path2D.Double {
		/**Сколько земли тут будет изначально +- 0,5*/
		public static final int SILT_LV_DEF = 20;
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
		private final boolean isShip = true;
		
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
			setParams(0,Utils.Utils.random(SILT_LV_DEF / 2, SILT_LV_DEF + SILT_LV_DEF / 2));
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
			final var s = silt+water;
			final int wAlf = (int) Math.min(255, water*100);
			final var cs = AllColors.toDark(AllColors.SAND, 255-wAlf);
			final var cw = AllColors.toDark(AllColors.WATER_RIVER, wAlf);
			color = AllColors.blendA(cs,cw);
		}
		/**Уровнь клетки, относительно нуля. Высота, на которой находится верхняя кромка поля*/
		public double maxLavel(){return water() + silt();}
		public double water(){return _water;}
		public double silt(){return _silt;}
		/**Добавляет к илу кусочек и возвращает новое значение*/
		public double silt(double add){setParams(_water, _silt + add); return _silt;}
		/**Обновляет значение клетки*/
		public void update(){
			setParams((1 - EVOLUTION_SPEED) * _water + EVOLUTION_SPEED * _water_next, _silt);
			_water_next = 0;
			flow = flow.scale((1 - EVOLUTION_SPEED)).add(flow_next.scale(EVOLUTION_SPEED));
			flow_next.scale(0);
		}
		public void draw(Graphics2D g) {
			final var tmpC = g.getColor();
			g.setColor(color);
			g.fill(this);
			
			if(radius > 20){
				g.setColor(Color.BLACK);
				Utils.Utils.centeredText(g, (int) center.x, (int) center.y-10, 10, water() > 0 ? String.format("w%.1f", water()) : "");
				Utils.Utils.centeredText(g, (int) center.x, (int) center.y+0, 10, String.format("s%.1f", silt()));
				Utils.Utils.centeredText(g, (int) center.x, (int) center.y+10, 10, String.format("l%.1f", maxLavel()));
				//Utils.Utils.centeredText(g, (int) center.x, (int) center.y+6, 10, point.toString() + (isEnd != null ? "e" : "") + (isMouth > 0 ? isMouth : ""));
			} else if(isShip){
				final var h = Math.max(1, radius / 2);
				g.setColor(Color.white);
				g.drawOval((int)(center.x - h/2), (int)(center.y - h/2), (int)h, (int)h);
			}
			g.setColor(tmpC);
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
		 * А жесть в том, что в квадратной гесанальной сетке q ещё перевести надо в колонки!
		 */
		private Cell[][] map = new Cell[0][0];
		/**Массив клеток которые мы вызвавем для обработки*/
		private Cell[] call_cell = new Cell[0];
		/**Сколько грязи мы потеряли за время моделирования*/
		private double exitS = 0;
		
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
			if(exitS > 1){ //Как только накопится грязь - дарим её клетке
				c.setParams(c.water(), c.silt() + 1);
				exitS -= 1;
			}
			//Запоминаем грязьку и ходим
			final var prefS = c.neighbours.values().stream().mapToDouble(lc -> lc == null ? 0 : lc.silt()).sum() + c.silt();
			{
				final var d = new Drop(c.point);
				while(moveDrop(d)){}
			}
			//c.step();
			final var next = c.neighbours.values().stream().mapToDouble(lc -> lc == null ? 0 : lc.silt()).sum() + c.silt();
			exitS += prefS - next;
			if(i == 0){
				//Нулевой шаг. Это самый конец. Надо запомнить все числа и начать с самого начала
				forEach(с -> c.update());
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
			Drop.MAX_AGE = height;
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
		private boolean isValid(HexPoint p){
			return isValid(p.que, p.row);
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
		private Cell get(HexPoint p){
			assert isValid(p) : "Точка оказалась за границей! Точка " + p + " при поле " + map.length + "х"+ map.length;
			return get(p.que, p.row);
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
			for(var c : cell.neighbours.values()){
				final var del = cell.silt() - c.silt();
				f.add((new HexVector(cell.point,c.point)).scale(del));
			}
			if(cell.isEnd != null){ //Крайние точки дополнительно утягивает за край
				final var del = cell.silt();
				final var n = cell.isEnd.next();
				final var p = cell.isEnd.prefur();
				f.add((new HexVector(cell.isEnd.dq, cell.isEnd.dr)).scale(del));
				f.add((new HexVector(n.dq, n.dr)).scale(del));
				f.add((new HexVector(p.dq, p.dr)).scale(del));
			}
			d.speed = d.speed.add(f.scale(Drop.GRAVITY / d.water)); //F=ma => a = F/m. V = V0+at
			if(cell.flow.lenght() > 0 && d.speed.lenght() > 0){
				final var fsnq = cell.flow.que / cell.flow.lenght();
				final var fsnr = cell.flow.row / cell.flow.lenght();
				final var dsnq = d.speed.que / d.speed.lenght();
				final var dsnr = d.speed.row / d.speed.lenght();
				//Сколярное произведение векторов...
				//Ну вообще, если я правильно понял, то это действует для любых ситуаций...
				//Но правильно ли я понял?
				final var dot = fsnq * dsnq + fsnr * dsnr;
				d.speed = d.speed.add(Drop.IMPULSE_TRANSFER * dot / (d.water + cell.water()) * cell.flow.lenght());
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
			final var h_next = isValid((int)d.position.que,(int)d.position.row) ? get((int)d.position.que,(int)d.position.row).silt() : 0;
			
			//Теперь перенесём массу пропорционально высоте и объёму воды!
			final var c_eq = Math.max(0.0, (cell.silt() - h_next) * (1.0 + Drop.SILT_TRANSPORT * erf(0.4 * cell.water())));
			final var del_c_eq = c_eq - d.silt;
			
			d.silt += siltEff * del_c_eq;
			cell._silt -= siltEff * del_c_eq;
			
			//Теперь испаряем часть капельки
			d.silt /= 1 - Drop.EVAPORATION_RATE;
			d.water*= 1 - Drop.EVAPORATION_RATE;
			
			if(!isValid((int)d.position.que,(int)d.position.row)) return false;
			
			shoreCollapse(get((int)d.position.que,(int)d.position.row));
			
			d.age++;
			return true;
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
		/**Проверяет крутость берега вокруг точки и обрушивает его, если надо*/
		private void shoreCollapse(Cell cell){
			
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
		/**Обновляет реки по берегам*/
		public void updateRivers(){
			for (int i = 0; i < left.call_cell.length; i++) {
				left.updateRivers(cell, true);
				right.updateRivers(cell++, false);
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
		
		final var count_cell = Configurations.getHeight();/**
		final var count_cell = 15;/***/
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
