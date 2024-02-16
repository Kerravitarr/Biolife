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
		private double lenght = -1;
		public HexVector(){this(0,0);}
		public HexVector(double q, double r){que = q; row = r;};
		/**Прибавляет вектор к вектору */
		public HexVector add(double q, double r){
			direction = null;lenght = -1;
			que += q;
			row += r;
			return this;
		}
		public double lenght(){
			if(lenght < 0)
				lenght = (Math.abs(que)+Math.abs(que + row)+Math.abs(row)) / 2;
			return lenght;
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
		public String toString(){return String.format("q;r=%.1f;%.1f%s", que , row, direction());}
	}
	/**Одна клетка берега*/
	private static class Cell extends java.awt.geom.Path2D.Double {
		/**Сколько земли тут будет изначально +- 0,5*/
		public static final int SILT_LV_DEF = 20;
		/**Сколлько сторон будет у полигона клеток*/
		private static final int SIDES = 6;
		/**Во сколько раз столб земли должен быть выше окружения, чтобы упасть*/
		private static final double PILLAR_FALL = 4;
		/**Сколько грязи с собой забирает течение. При течении = 1*/
		private static final double GET_SILT = 0.001;
		/**На сколько высоко мы можем закинуть ил вместе с течением. При течении = 1*/
		private static final double JUMP_SILT = 1e10;
		/**Во сколько должен быть высокий столб грязи под водой, чтобы упасть против течения*/
		private static final double STANDING_PILLAR = 2;
		
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
		/**Течение в клетке*/
		private HexVector _flow = new HexVector();
		/**Цвет клетки*/
		private Color color = AllColors.SAND;
		/**Показывает, что это у нас клетка источника*/
		private boolean isMouth = false;
		/**Флаг, показывающий что клетка является крайней, за ней - река. Показывается направление, в котором река*/
		private HexPoint.Direction isEnd = null;
		/**Массив всех соседей клетки*/
		private java.util.EnumMap<HexPoint.Direction,Cell> neighbours = null;
		/**Мы - карйняя стенка у какой-то из стороны. Мы грязью ни с кем не делимся, как порядочная стенка*/
		private boolean isWail = true;
		/**Наличие на клетке "кораблика"*/
		private boolean isShip = false;
		
		
		private HexVector _flow_t = new HexVector();
		
		
		public Cell(HexPoint p){
			point = p;
		}
		/** Иициализирует клетку
		 * @param isMouth эта клетка - источник?
		 * @param isEnd направление где карта заканчивается. Относится только к крайним клеткам
		 * @param neighbours список соседних клеток
		 */
		public void init(boolean isMouth, HexPoint.Direction isEnd, java.util.EnumMap<HexPoint.Direction,Cell> neighbours){
			this.isMouth = isMouth;
			this.isEnd = isEnd;
			this.neighbours = neighbours;
			this.isWail = isEnd == null && neighbours.values().stream().filter(v -> v != null).count() != HexPoint.Direction.sides.length;
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
		
		/** Логика работы клетки
		 */
		public void step(){			
			//На первом этапе - вода.			
			if(water() > 0){
				waterStep();
				//Передаём кораблик дальше
				if(isShip && _flow.lenght() > 0){
					final var tar = neighbours.get(_flow.direction());
					if(tar != null) tar.isShip = true;
				}
				//Водичка протекла. Теперь проверим - а течение есть?
				if(_flow.lenght() > 0 && !isWail){
					//Течение есть! Надо забрать земельку с противоположной стороны и передать её дальше
					//Сколько грязьки мы возьмём с нашей клетки
					//А ещё если мы конец, то мы ни чего не возьмём со стороны реки, но течение есть и значит грязьку мы отдадим!
					siltStep();
				} else if(!isWail){
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
				_flow_t = new HexVector(0.8 * _flow_t.que + 0.2 * _flow.que,0.8 * _flow_t.row + 0.2 * _flow.row);
			} else {
				//У нас сухой столб. Он может обвалиться, если будет слишком высоким
				final var our_level = silt();
				Cell max = null;
				var delta = our_level * PILLAR_FALL; //Сразу минимальная высота, которая только и может обвалиться.
				for(final var c : neighbours.values()){
					if(c == null) continue;
					final var effH = c.silt(); //Каждый мокрый слой подтачивает нас, а не удерживает
					if((our_level - effH) > delta || effH <= 0){
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
			isShip = false;
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
				g.setColor(Color.white);
				final var s = _flow_t.lenght();
				final var x = s * (Math.sqrt(3) * _flow_t.que  +  Math.sqrt(3)/2d * _flow_t.row);
				final var y = s * (                         3d/2d * _flow_t.row);
				g.drawLine((int) center.x, (int) center.y, (int) (center.x + x), (int) (center.y + y));
			} else if(isShip){
				final var h = Math.max(1, radius / 2);
				g.setColor(Color.white);
				g.drawOval((int)(center.x - h/2), (int)(center.y - h/2), (int)h, (int)h);
			}
			g.setColor(tmpC);
		}
		private void waterStep(){
			//Наш уровень воды, относительно мирового
			final var our_level = maxLavel();
			//Вспомогательный массив отфильтрованных занчений
			final java.util.Map.Entry<HexPoint.Direction,Cell>[] filter = new java.util.Map.Entry[HexPoint.Direction.values.length];
			var filterSize = 0;
			//Найдём средний уровень, какой мы хотим иметь по итогу
			var average_level = our_level;
			var max_al = silt();
			var count_target = 1;
			for(final var e : neighbours.entrySet()){
				if(e.getValue() == null) continue;
				final var flow = e.getKey() == _flow.direction() ? _flow.lenght() : 0;
				final var lv = e.getValue().maxLavel() - flow;
				if(lv < our_level){
					count_target++;
					average_level += lv;
					max_al = Math.max(max_al, e.getValue().silt() - flow);
					filter[filterSize++] = e;
				}
			}
			if(count_target > 1){
				//Средний уровень не может быть ниже, чем высота ила где ни будь
				average_level =  Math.max(average_level / count_target, max_al);					
				var costD = 0d;	//Каков в сумме размер всех перепадов высот
				//Узнаем, сколько нужно каждой клетке, чтобы её уровень стал как average_level
				for (int i = 0; i < filterSize; i++) {
					final var e = filter[i];
					final var lv = e.getKey() != _flow.direction() ? e.getValue().maxLavel() : (e.getValue().maxLavel() - _flow.lenght());
					costD += average_level - lv;
				}
				//А вот столько воды мы можем дать...
				final var has_water = Math.min(water(), our_level - average_level);

				var nextFlow = new HexVector();
				if(costD > 0){
					for (int i = 0; i < filterSize; i++) {
						final var e = filter[i];
						final var c = e.getValue();
						final var lv = e.getKey() != _flow.direction() ? c.maxLavel() : (c.maxLavel() - _flow.lenght());
						final var forse = (average_level - lv) / costD;
						if(Math.abs(forse) < 1e-10) continue; //Если тут прибавочка на уровне погрешности вычислений, то не прибавляем
						final var add = has_water * forse;
						nextFlow.add(e.getKey().dq * forse,e.getKey().dr * forse);
						c.setParams(c.water() + add, c.silt());
						//c._flow.add(_flow.que * forse,_flow.row * forse);
					}
					setParams(water() - has_water, silt());
				}
				_flow = nextFlow;
				if(isEnd != null && water() > 0){
					//С последней клетки вода утекает на три следующие - в сторону реки
					final var f = water() / 6;
					_flow = new HexVector(isEnd.dq * f ,isEnd.dr * f);
					setParams(water() / 2 , silt());
				} else if(isEnd != null) {
					_flow = new HexVector();
					setParams(0, silt());
				}
			} else {
				//Нет течения. Ни кому не отдаим нашу воду!
				_flow = new HexVector();
			}
		}
		private void siltStep(){
			//Вспомогательный массив отфильтрованных занчений
			final java.util.Map.Entry<HexPoint.Direction,Cell>[] filter = new java.util.Map.Entry[HexPoint.Direction.values.length];
			var filterSize = 0;
			final var our_silt = silt();
			final var give = Math.min(our_silt,  _flow.lenght() * GET_SILT);
			var hasStil = give;
			var needStil = 0d;

			final Function<HexPoint.Direction,Boolean> isContain = d -> {final var c = neighbours.get(d); return c != null && c.water() > 0;};
			final var isUpFlow = isContain.apply(_flow.direction().next());
			final var isDownFlow = isContain.apply(_flow.direction().prefur());
			final var Kmain = isUpFlow && isDownFlow ? 0.5 : ((isUpFlow || isDownFlow) ? 2d/3d : 1.0);
			final var Ksubmain = isUpFlow && isDownFlow ? 0.25 : 1d/3d;
			final var breakColumn = our_silt * STANDING_PILLAR;
			filterSize = 0;
			for(final var e : neighbours.entrySet()){
				if(e.getValue() == null) continue;
				final var d = e.getKey();
				final var c = e.getValue();
				final var s = c.silt();
				if(d.isNear(_flow.direction())){
					if(c.water() == 0) continue; //Те, что по пути, мы их подпираем и не трогаем
					final var flow = _flow.lenght() * (d == _flow.direction() ? Kmain : Ksubmain);
					//По течению
					if(s > breakColumn){
						//У нас по курсу течения большой перепад высот. Это он хочет упасть к нам
						final var delS = (s - breakColumn) / 3;
						c.setParams(c.water(), s - delS);
						hasStil += delS;
					} else if(our_silt + flow * JUMP_SILT>= s){
						needStil += Math.pow(flow, 4) * GET_SILT; //Переносная сила потока изменяется в 4й степени к скорости
						filter[filterSize++] = e;
					}
				} else {
					//Не по течению
					final var flow = _flow.lenght() / 3;
					if(s + flow * JUMP_SILT >= our_silt){
						//С нами хотят поделиться землёй
						final var delS = Math.min(s, Math.pow(flow, 4) * GET_SILT);
						c.setParams(c.water(), s - delS);
						hasStil += delS;
					} else if(our_silt > s * STANDING_PILLAR){
						//Мы на столько высокие, что падаем туда!
						needStil += (our_silt - s * STANDING_PILLAR) / 3;
						filter[filterSize++] = e;
					} 
				}
			}
			//Иногда нам отдавать ни чего не нужно, или нужно на столько мало, что скорее уж 0, чем число
			if(needStil > 0){
				//Коэффициент передачи. Показывает сколько мы реально можем отдать
				final var hasK = hasStil / needStil;
				for (int i = 0; i < filterSize; i++) {
					final var e = filter[i];
					final var d = e.getKey();
					final var c = e.getValue();
					var s = c.silt();
					if(d.isNear(_flow.direction())){
						s += Math.pow(_flow.lenght() * (d == _flow.direction() ? Kmain : Ksubmain), 4)  * GET_SILT * hasK;
					} else {
						s += (our_silt - s * STANDING_PILLAR) / 3 * hasK;
					}
					c.setParams(c.water(), s);
				}
			}
			setParams(water(), silt() - give);
		}
		@Override
		public String toString(){
			return String.format("%s w;s;m%.1f;%.1f;%.1f", point,water(),silt(),maxLavel());
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
		/**Сколько грязи мы потеряли за время моделирования*/
		private double exitS = 0;
		/**Какая максимальная высота земли на текущий момент*/
		private double maxHeight = 0;
		/**Какая максимальная высота земли будет на следующий ход*/
		private double maxHeightNext = 0;
		
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
			if(exitS > 1){ //Как только накопится грязь - дарим её клетке
				c.setParams(c.water(), c.silt() + 1);
				exitS -= 1;
			}
			//Источник всегда имеет на 1 выше воды, чем самая высокая гора
			if(c.isMouth){
				c.setParams(Math.max(0, (maxHeightNext + 1) - c.water() - c.silt()) , c.silt());
				c.isShip = i % 2 == 0;
			}
			final var prefS = c.neighbours.values().stream().mapToDouble(lc -> lc == null ? 0 : lc.silt()).sum() + c.silt();
			c.step();
			final var next = c.neighbours.values().stream().mapToDouble(lc -> lc == null ? 0 : lc.silt()).sum() + c.silt();
			exitS += prefS - next;
			maxHeightNext = Math.max(maxHeightNext, c.silt());
			if(i == 0){
				maxHeight = maxHeightNext;
				maxHeightNext = 0;
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
				final var isM = (p.row == 0 && p.que == map.length / 2);// || (p.row == map.length-1 && p.que == 0);
				final var isE = isLeft && (p.que + p.row / 2) == (map.length - 1) || !isLeft && (p.que + p.row / 2) == 0;
				final var neighbours = new java.util.EnumMap<HexPoint.Direction,Cell>(HexPoint.Direction.class);
				for(final var d : HexPoint.Direction.sides)
					neighbours.put(d, get(p,d));
				c.init(isM, isE ? (isLeft ? HexPoint.Direction.RIGHT : HexPoint.Direction.LEFT) : null, neighbours);
			});
			maxHeight = Cell.SILT_LV_DEF;
			
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
						 get(point).setParams( 0, Cell.SILT_LV_DEF / 4);
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
						 get(point).setParams( 0, Cell.SILT_LV_DEF / 4);
					point = point.next(d);
				}
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
