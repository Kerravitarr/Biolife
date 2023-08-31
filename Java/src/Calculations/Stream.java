package Calculations;

import Calculations.Point.DIRECTION;
import MapObjects.CellObject;

/**
 * Поток жидкости
 *
 */
public abstract class Stream {
	/**Прямоугольный вертикальный поток*/
	public static class VerticalRectangle extends Stream{
		/**Ширина потока*/
		private final int width;
		/**Высота потока*/
		private final int height;
		
		/**Создание квадртаного потока
		 * @param pos позиция верхнего левого угла потока на данный момент
		 * @param width ширина, в клетках мира
		 * @param height высота, в клетках мира
		 * @param power максимальная энергия потока. Не может быть 0. 
		 *			Если меньше 0, то каждый шаг поток будет тянуть клетку на дно
		 *			если больше 0, то поток восходящий, то есть двигает клету к поверхности
		 * @param shadow тип снижения мощности от максимума к минимуму
		 * @param minP энергия на самом краешке
		 */
		public VerticalRectangle(Point pos, int width, int height, int power, Stream.SHADOW shadow, int minP){
			super(new Point(pos.getX() + width / 2, pos.getY() + height / 2),power,shadow, minP);
			this.width = width;
			this.height = height;
		}
		
		/**Создание квадртаного потока без убывания мощности
		 * @param pos позиция верхнего левого угла потока на данный момент
		 * @param width ширина, в клетках мира
		 * @param height высота, в клетках мира
		 * @param power максимальная энергия потока. Не может быть 0. 
		 *			Если меньше 0, то каждый шаг поток будет тянуть клетку на дно
		 *			если больше 0, то поток восходящий, то есть двигает клету к поверхности
		 */
		public VerticalRectangle(Point pos, int width, int height, int power){
			super(new Point(pos.getX() + width / 2, pos.getY() + height / 2),power);
			this.width = width;
			this.height = height;
		}

		@Override
		public void action(CellObject cell) {
			final var pos = cell.getPos();
			final var delX = Math.abs(Point.subtractionX(position.getX(), pos.getX()));
			var delY = Point.subtractionY(position.getY(), pos.getY());
			if(delX*2 > width || Math.abs(delY)*2 > height) return;
			
			//Сила затягивания к центральной оси потка
			var F = switch(shadow){
				case NONE -> powerCenter;
				case LINE -> powerCenter - (powerCenter - minP) * delX * 2 / width;
				case PARABOLA -> (minP - powerCenter) / Math.pow(width / 2,2) * delX * delX + powerCenter;
				default -> throw new AssertionError();
			};
			if(F > 0 && cell.getAge() % F == 0)
				cell.moveD(DIRECTION.UP); // Поехали по направлению!
			else if(F < 0 && cell.getAge() % -F == 0)
				cell.moveD(DIRECTION.DOWN); // Поехали по направлению!
		}
	}
	/**Прямоугольный вертикальный поток*/
	public static class HorizontalRectangle extends Stream{
		/**Ширина потока*/
		private final int width;
		/**Высота потока*/
		private final int height;
		
		/**Создание квадртаного потока
		 * @param pos позиция верхнего левого угла потока на данный момент
		 * @param width ширина, в клетках мира
		 * @param height высота, в клетках мира
		 * @param power максимальная энергия потока. Не может быть 0. 
		 *			Если больше 0, то каждый шаг поток будет толкать клетку на восток (вправо)
		 *			если меньше 0, то поток будет толкать влево (на запад)
		 * @param shadow тип снижения мощности от максимума к минимуму
		 * @param minP энергия на самом краешке
		 */
		public HorizontalRectangle(Point pos, int width, int height, int power, Stream.SHADOW shadow, int minP){
			super(new Point(pos.getX() + width / 2, pos.getY() + height / 2),power,shadow, minP);
			this.width = width;
			this.height = height;
		}
		
		/**Создание квадртаного потока без убывания мощности
		 * @param pos позиция верхнего левого угла потока на данный момент
		 * @param width ширина, в клетках мира
		 * @param height высота, в клетках мира
		 * @param power максимальная энергия потока. Не может быть 0. 
		 *			Если больше 0, то каждый шаг поток будет толкать клетку на восток (вправо)
		 *			если меньше 0, то поток будет толкать влево (на запад)
		 */
		public HorizontalRectangle(Point pos, int width, int height, int power){
			super(new Point(pos.getX() + width / 2, pos.getY() + height / 2),power);
			this.width = width;
			this.height = height;
		}

		@Override
		public void action(CellObject cell) {
			final var pos = cell.getPos();
			final var delX = Math.abs(Point.subtractionX(position.getX(), pos.getX()));
			var delY = Point.subtractionY(position.getY(), pos.getY());
			if(delX*2 > width || Math.abs(delY)*2 > height) return;
			
			//Сила затягивания к центральной оси потка
			var F = switch(shadow){
				case NONE -> powerCenter;
				case LINE -> powerCenter - (powerCenter - minP) * delY * 2 / height;
				case PARABOLA -> (minP - powerCenter) / Math.pow(height / 2,2) * delY * delY + powerCenter;
				default -> throw new AssertionError();
			};
			if(F > 0 && cell.getAge() % F == 0)
				cell.moveD(DIRECTION.RIGHT); // Поехали по направлению!
			else if(F < 0 && cell.getAge() % -F == 0)
				cell.moveD(DIRECTION.LEFT); // Поехали по направлению!
		}
	}
	/**Круглый поток*/
	public static class Ellipse extends Stream{
		/**Большая ось эллипса - лежит на оси Х*/
		private final int a2;
		/**Малая ось эллипса - лежит на оси Y*/
		private final int b2;
		
		/**Создание элипсовидного потока
		 * @param pos позиция центра на данный момент
		 * @param a2 большая ось эллипса - лежит на оси Х
		 * @param b2 малая ось эллипса - лежит на оси Y
		 * @param power максимальная энергия потока. Не может быть 0. 
		 *			Если больше 0, то клетку будет тянуть в центр потока
		 *			если меньше 0, выталкивать
		 * @param shadow тип снижения мощности от максимума к минимуму
		 * @param minP энергия на самом краешке
		 */
		public Ellipse(Point pos, int a2, int b2, int power, Stream.SHADOW shadow, int minP){
			super(pos,power,shadow, minP);
			this.a2 = a2;
			this.b2 = b2;
		}
		/**Создание элипсовидного потока без изменения мощности на всём потоке
		 * @param pos позиция центра на данный момент
		 * @param a2 большая ось эллипса - лежит на оси Х
		 * @param b2 малая ось эллипса - лежит на оси Y
		 * @param power максимальная энергия потока. Не может быть 0. 
		 *			Если больше 0, то клетку будет тянуть в центр потока
		 *			если меньше 0, выталкивать
		 */
		public Ellipse(Point pos, int a2, int b2, int power){
			this(pos, b2, power, SHADOW.NONE, power);
		}
		/**Создание круглого потока
		 * @param pos позиция центра на данный момент
		 * @param d диаметр круга
		 * @param power максимальная энергия потока. Не может быть 0. 
		 *			Если больше 0, то клетку будет тянуть в центр потока
		 *			если меньше 0, выталкивать
		 * @param shadow тип снижения мощности от максимума к минимуму
		 * @param minP энергия на самом краешке
		 */
		public Ellipse(Point pos, int d, int power, Stream.SHADOW shadow, int minP){
			this(pos, d,d, power, shadow, minP);
		}
		/**Создание круглого потока без изменения мощности на всём потоке
		 * @param pos позиция центра на данный момент
		 * @param d диаметр круга
		 * @param power максимальная энергия потока. Не может быть 0. 
		 *			Если больше 0, то клетку будет тянуть в центр потока
		 *			если меньше 0, выталкивать
		 */
		public Ellipse(Point pos, int d, int power){
			this(pos, d,power, SHADOW.NONE, power);
		}
		
		@Override
		public void action(CellObject cell) {
			final var pos = cell.getPos();
			final double delY = Point.subtractionY(position.getY(), pos.getY());
			final double delX = Point.subtractionX(position.getX(), pos.getX());
			if(Math.pow(delX*2 / a2, 2) + Math.pow(delY*2 / b2, 2) <= 1) return;
			final var absDY = Math.abs(delY);
			final var absDX = Math.abs(delX);
			
			//Сила затягивания/выталкивания к центральной точке
			double F;
			switch(shadow){
				case NONE -> F = powerCenter;
				case LINE -> {
					if(absDX * b2 > absDY * a2){
						F = powerCenter - (powerCenter - minP) * absDY * 2 / b2;
					} else {
						F = powerCenter - (powerCenter - minP) * absDX * 2 / a2;
					}
				}
				case PARABOLA -> {
					if(absDX * b2 > absDY * a2){
						F = (minP - powerCenter) / Math.pow(b2 / 2,2) * delY * delY + powerCenter;
					} else {
						F = (minP - powerCenter) / Math.pow(a2 / 2,2) * delX * delX + powerCenter;
					}
				}
				default -> throw new AssertionError();
			}
			final var dx = Utils.Utils.betwin(-1, delX, 1);
			final var dy = Utils.Utils.betwin(-1, delY, 1);
			if(F > 0 && cell.getAge() % F == 0){
				for(var i : DIRECTION.values){
					if(-dx == i.addX && -dy == i.addY)
						cell.moveD(DIRECTION.LEFT);
				}
			}else if(F < 0 && cell.getAge() % -F == 0){
				for(var i : DIRECTION.values){
					if(dx == i.addX && dy == i.addY)
						cell.moveD(DIRECTION.LEFT);
				}
			}
		}
	}

	/**Позиция центра потока*/
	protected Point position;
	/**Наибольшая энергия потока.
	 *	если тут 1, то клетку толкает каждый шаг. Если 2, то каждые 2 шага и т.д.
	 *  если тут -1, то клетку будет выталкивать.
	 * 
	 */
	protected int powerCenter;
	/**способ уменьшения мощности потока от расстояния*/
	protected SHADOW shadow;
	/**Энергия на самом краешке*/
	protected final int minP;

	/**Способ снижения мощности*/
	public enum SHADOW {
		/**Нет уменьшения. Поток есть, а потом раз и нет*/
		NONE, 
		/**Параболический*/
		PARABOLA, 
		/**Линейный*/
		LINE,
	};
	/**Создание гейзера
	 * @param pos позиция центра потока при создании
	 * @param p максимальная энергия потока
	 * @param s способ рассеения мощности от расстояния.
	 * @param minP энергия на самом краешке
	 */
	protected Stream(Point pos, int p, SHADOW s, int minP) {
		if(p == 0) throw new IllegalArgumentException("Мощность потока не может быть равна 0!");
		else if(p > 0 && minP < 0 || p < 0 && minP > 0 ) throw new IllegalArgumentException("Мощность потока не может менять направление от центра к краям!");
		position = pos;
		powerCenter = p;
		shadow = s;
		this.minP = minP;
	}
	/**Создание универсальной формы без снижения мощности потока
	 * @param pos позиция центра потока при создании
	 * @param p максимальная энергия потока
	 */
	protected Stream(Point pos, int p) {
		this(pos, p, SHADOW.NONE, p);
	}

	/**Обрабатывает сдувание клетки в определённую сторону потоком
	 * @param cell клетка, на которую поток воздействует
	 */
	public abstract void action(CellObject cell);
}