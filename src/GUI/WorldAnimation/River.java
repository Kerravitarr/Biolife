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
			RIGHT(1,0),
			UP_RIGHT(1,-1),
			UP_LEFT(0,-1),
			LEFT(-1,0),
			DOWN_LEFT(-1,1),
			DOWN_RIGHT(0,1),
			CENTER(0, 0),
			;
			/**Все направления*/
			public final static Direction[] values = Direction.values();
			/**Все направления, кроме центрального*/
			public final static Direction[] sides = Arrays.stream(values).filter(v -> v != CENTER).toArray(Direction[]::new);
			/**Смещение строки*/
			private final int dr;
			/**Смещение столбца, если строка чётная и если строка нечётная*/
			public final int dq;
			private Direction(int dq, int dr){this.dr = dr;this.dq = dq;}
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
	/**Одна клетка берега*/
	private static class Cell extends java.awt.geom.Path2D.Double {
		/**Сколлько сторон будет у полигона клеток*/
		private static final int SIDES = 6;
		/**На сколько сухой столб должен быть выше окружающего мира, чтобы свалиться*/
		private static double PILLAR_FALL = HexPoint.Direction.values.length;
		/**На сколько высоко мы можем закинуть ил вместе с течением. При течении = 1*/
		private static double JUMP_SILT = 1;
		/**На сколько должен быть высокий столб грязи под водой, чтобы упасть против течения. При течении = 1*/
		private static double STANDING_PILLAR = 2;
		/**На сколько мы должны быть выше окружающей грязи, чтобы упасть в неё против течения. При течении = 1*/
		private static double SILT_FALL = 2;
		
		/**Центральная точка*/
		private java.awt.geom.Point2D.Double center = new java.awt.geom.Point2D.Double(0, 0);
		/**Радиус клетки, в пикселях*/
		private double radius = 1;
		
	
		/**Местоположение клетки*/
		public final HexPoint point;
		/**Ил на клетке. То, что лежит на её дне*/
		private double _silt;
		/**Вода на клетке. Естественно над илом!*/
		private double _water;
		/**Сила течения, количество перенесённой воды за ход*/
		private double _flow = 0;
		/**Направление течения*/
		private HexPoint.Direction _flowDirection = HexPoint.Direction.CENTER;
		/**Цвет клетки*/
		private Color color = AllColors.SAND;
		/**Сколько воды добавляется клетке за каждый ход*/
		private double isMouth = 0;
		/**Флаг, показывающий что клетка является крайней, за ней - река. Показывается направление, в котором река*/
		private HexPoint.Direction isEnd = null;
		/**Массив всех соседей клетки*/
		private java.util.EnumMap<HexPoint.Direction,Cell> neighbours = null;
		
		public Cell(HexPoint p){
			point = p;
			setParams(0,10);
		}
		/** Иициализирует клетку
		 * @param isMouth количество воды, которое клетка получает за каждый ход. Относится только к источникам
		 * @param isEnd направление где карта заканчивается. Относится только к крайним клеткам
		 * @param neighbours список соседних клеток
		 */
		public void init(double isMouth, HexPoint.Direction isEnd, java.util.EnumMap<HexPoint.Direction,Cell> neighbours){
			this.isMouth = isMouth;
			this.isEnd = isEnd;
			this.neighbours = neighbours;
		}
		/**Сохраняет размеры клетки, перерисовывая её заодно
		 * @param woffset смещение относительно 0х по ширине экрана
		 * @param scale масштаб, размер одной клетки, пк/клетку
		 */
		public void setScale(int woffset, double scale){
			radius = scale * 0.9;
			final var x = scale * (Math.sqrt(3) * point.que  +  Math.sqrt(3)/2d * point.row);
			final var y = scale * (                         3d/2d * point.row);
			center = new java.awt.geom.Point2D.Double(x+woffset,y);
			updatePoints();
		}
		public void setParams(double water, double silt){
			assert java.lang.Double.isFinite(water) && java.lang.Double.isFinite(silt) : "Вода " + water + " грязь " + silt;
			_silt = silt;
			_water = water;
			final var s = silt+water;
			final var cs = AllColors.toDark(AllColors.SAND, (int) (255*silt/s));
			final var cw = AllColors.toDark(AllColors.WATER_RIVER, (int) (255*water/s));
			color = AllColors.blendA(cs,cw);
		}
		/**Уровнь клетки, относительно нуля. Высота, на которой находится верхняя кромка поля*/
		public double maxLavel(){return water() + silt();}
		public double water(){return _water;}
		public double silt(){return _silt;}
		
		/** Логика работы клетки
		 */
		public void step(){
			//На первом этапе - вода.			
			if(water() > 0){
				//Наш уровень воды, относительно мирового
				final var our_level = maxLavel();
				//Найдём средний уровень, какой мы хотим иметь по итогу
				var average_level = our_level;
				var count_target = 1;
				for(final var e : neighbours.entrySet()){
					if(e.getValue() == null) continue;
					final var lv = e.getKey() != _flowDirection ? e.getValue().maxLavel() : (e.getValue().maxLavel() - _flow);
					if(lv < our_level){
						count_target++;
						average_level += lv;
					}
				}
				if(count_target > 1){
					average_level /= count_target;					
					var costD = our_level - average_level;	//Каков в сумме размер всех перепадов высот
					//Узнаем, сколько нужно каждой клетке, чтобы её уровень стал как average_level
					for(final var e : neighbours.entrySet()){
						if(e.getValue() == null) continue;
						final var lv = e.getKey() != _flowDirection ? e.getValue().maxLavel() : (e.getValue().maxLavel() - _flow);
						if(lv >= our_level) continue;
						costD += average_level - lv;
					}
					//А вот столько воды мы можем дать...
					final var has_water = Math.min(water(), our_level - average_level);
					
					var nextFlow = 0d;
					var nextD = HexPoint.Direction.CENTER;
					for(final var e : neighbours.entrySet()){
						if(e.getValue() == null) continue;
						final var lv = e.getKey() != _flowDirection ? e.getValue().maxLavel() : (e.getValue().maxLavel() - _flow);
						if(lv >= our_level) continue;
						final var add = has_water * (average_level - lv) / costD;
						if(add > nextFlow){
							nextFlow = add;
							nextD = e.getKey();
						}
						e.getValue().setParams(e.getValue().water()+add, e.getValue().silt());
					}
					_flow = nextFlow;
					_flowDirection = nextD;
					setParams(water() - has_water, silt());
				} else {
					//Нет течения. Ни кому не отдаим нашу воду!
					_flow = 0d;
					_flowDirection = HexPoint.Direction.CENTER;
				}
				//Водичка протекла. Теперь проверим - а течение есть?
				if(_flow > 0){
					//Течение есть! Надо забрать земельку с противоположной стороны и передать её дальше
					final var our_silt = silt();
					final Function<HexPoint.Direction,Boolean> isContain = d -> {final var c = neighbours.get(d); return c != null && c.water() > 0;};
					final var isUpFlow = isContain.apply(_flowDirection.next());
					final var isDownFlow = isContain.apply(_flowDirection.prefur());
					
					for(final var e : neighbours.entrySet()){
						if(e.getValue() == null) continue;
						final var d = e.getKey();
						final var c = e.getValue();
						if(d == _flowDirection){
							//Прям по течению
							if(our_silt >= c.silt() - _flow * 4){
								//Мы можем закинуть наверх землю
							} else if(c.silt() - _flow * 4 - our_silt > 20){
								//У нас по курсу течения большой перепад высот. Это он хочет упасть к нам
								
							}
						} else if(d.isNear(_flowDirection)){
							//По течению, рядом
							if(our_silt >= c.silt() - _flow * 2){
								//Мы можем закинуть наверх землю
							} else if(c.silt() - _flow * 2 - our_silt > 10){
								//У нас по курсу течения большой перепад высот. Это он хочет упасть к нам
								
							}
						} else {
							//Не по течению
							if(our_silt <= c.silt() + _flow * 2){
								//С нами хотят поделиться землёй
								
							} else if(our_silt - _flow * 2 - c.silt() > 10){
								//Уже мы должны делиться землёй
								
							}
						}
						
					}
				} else {
					//Мы простая заводь. Но надо поглядеь, может и мы можем с кем ни будь добром поделиться!
					final var our_silt = silt();
					Cell max = null;
					var delta = HexPoint.Direction.values.length / 3d; //Сразу минимальная высота, которая только и может обвалиться.
					for(final var c : neighbours.values()){
						if(c == null) continue;
						if((our_silt - c.silt()) > delta){
							max = c;
							delta = our_silt - c.silt();
						}
					}
					if(max != null){
						//Разравниваемся
						final var ds = (silt() + max.silt()) / 2;
						setParams(water() , ds * 2/3);
						max.setParams(max.water() , ds * 1/3);
					}
				}
			} else {
				//У нас сухой столб. Он может обвалиться, если будет слишком высоким
				final var our_level = silt();
				Cell max = null;
				var delta = PILLAR_FALL; //Сразу минимальная высота, которая только и может обвалиться.
				for(final var c : neighbours.values()){
					if(c == null) continue;
					final var effH = c.silt() - c.water() * 3; //Каждый мокрый слой подтачивает нас, а не удерживает
					if((our_level - effH) > delta){
						max = c;
						delta = our_level - effH;
					}
				}
				if(max != null){
					//Осыпаемся!
					final var ds = (silt() + max.silt()) / 2;
					setParams(water() , ds * 2/3);
					max.setParams(max.water() , ds * 1/3);
				}
			}
			setParams(water() + isMouth , silt());
			if(isEnd != null && water() > 1)
				setParams( water() - 1 , silt());
			else if(isEnd != null && water() > 0)
				setParams( 0 , silt());
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
		public void draw(Graphics2D g) {
			final var tmpC = g.getColor();
			g.setColor(color);
			g.fill(this);
			if(radius > 10){
				g.setColor(Color.BLACK);
				Utils.Utils.centeredText(g, (int) center.x, (int) center.y-10, 10, water() > 0 ? String.format("w%.1f", water()) : "");
				Utils.Utils.centeredText(g, (int) center.x, (int) center.y+0, 10, String.format("s%.1f", silt()));
				Utils.Utils.centeredText(g, (int) center.x, (int) center.y+10, 10, String.format("l%.1f", maxLavel()));
				//Utils.Utils.centeredText(g, (int) center.x, (int) center.y+6, 12, point.toString());
			}
			g.setColor(tmpC);
		}
		
		@Override
		public String toString(){
			return "C "+point+" " + _water + ";"+_silt;
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
		 * А жесть в том, что в квадратной гесанальной сетке q ещё перевести надо в колонки!
		 */
		private Cell[][] map = new Cell[0][0];
		/**Массив клеток которые мы вызвавем для обработки*/
		private Cell[] call_cell = new Cell[0];
		
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
			final var i = call_cell.length - index % call_cell.length - 1; //У нас index идёт вверх. А i должен быть [0,map.length)
			final var t = call_cell[i];
			final var j = Utils.Utils.random(0, i); // случайный индекс от 0 до i
			//Меняем местами клетки, чтобы каждый раз вызывать их в разной последовательности
			final var c = call_cell[i] = call_cell[j];
			call_cell[j] = t;
			c.step();
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
			var minLenght = 0; //Минимальная длина речушки изначально, в клетках
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
					if(isValid(point)){
						final var cell = get(point);
						cell.setParams( cell.silt() / 4, cell.silt() / 2); //В русле глубина в половину ниже
						minLenght++;
					}
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
					if(isValid(point)){
						final var cell = get(point);
						cell.setParams( cell.silt() / 4, cell.silt() / 2); //В русле глубина в половину ниже
					}
					point = point.next(d);
				}
			}
			final var ml = minLenght;
			forEach(c -> {
				final var p = c.point;
				final var isM = (p.row == 0 && p.que == map.length / 2) || (p.row == map.length-1 && p.que == 0);
				final var isE = isLeft && (p.que + p.row / 2) == (map.length - 1) || !isLeft && (p.que + p.row / 2) == 0;
				final var neighbours = new java.util.EnumMap<HexPoint.Direction,Cell>(HexPoint.Direction.class);
				for(final var d : HexPoint.Direction.sides)
					neighbours.put(d, get(p,d));
				c.init(isM ? ml : 0, isE ? (isLeft ? HexPoint.Direction.RIGHT : HexPoint.Direction.LEFT) : null, neighbours);
			});
		}
		
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
		 * @param p
		 * @return 
		 */
		private Cell get(HexPoint p){
			assert isValid(p) : "Точка оказалась за границей! Точка " + p + " при поле " + map.length + "х"+ map.length;
			return map[p.row][p.que + p.row / 2];
		}
		/**Возвращает клетку относительно*/
		private Cell get(HexPoint p, HexPoint.Direction d){
			final var q = p.que + d.dq;
			final var r = p.row + d.dr;
			if(isValid(q,r))
				return map[r][q + r/2];
			else
				return null;
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
		
		
		final var count_cell = 11;//Configurations.getHeight();
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
