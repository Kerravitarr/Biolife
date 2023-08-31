package Calculations;

import Calculations.Point.DIRECTION;
import MapObjects.CellObject;
import static javax.swing.Spring.width;

/**
 * Поток жидкости
 *
 */
public class Stream {
	/**Прямоугольный вертикальный поток*/
	public static class VerticalRectangle extends StreamForm{
		/**Ширина потока*/
		private final int width;
		/**Высота потока*/
		private final int height;
		
		/**Создание квадртаного потока
		 * @param pos позиция верхнего левого угла потока на данный момент
		 * @param width ширина, в клетках мира
		 * @param height высота, в клетках мира
		 * @param power максимальная энергия потока. Не может быть 0. Если 1, то каждый шаг поток будет действовать на клетку
		 *			если > 0, то поток восходящий, то есть двигает клету к поверхности
		 * @param shadow тип снижения мощности от максимума к минимуму
		 * @param minP энергия на самом краешке
		 */
		public VerticalRectangle(Point pos, int width, int height, int power, StreamForm.SHADOW shadow, int minP){
			super(new Point(pos.getX() + width / 2, pos.getY() + height / 2),power,shadow, minP);
			this.width = width;
			this.height = height;
		}
		
		/**Создание квадртаного потока без убывания мощности
		 * @param pos позиция верхнего левого угла потока на данный момент
		 * @param width ширина, в клетках мира
		 * @param height высота, в клетках мира
		 * @param power максимальная энергия потока. Не может быть 0. Если 1, то каждый шаг поток будет действовать на клетку
		 *			если > 0, то поток восходящий, то есть двигает клету к поверхности
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
	/**Круглый поток*/
	public static class Ellipse extends StreamForm{
		/**Большая ось эллипса - лежит на оси Х*/
		private final int a2;
		/**Малая ось эллипса - лежит на оси Y*/
		private final int b2;
		
		/**Создание элипсовидного потока
		 * @param pos позиция центра на данный момент
		 * @param a2 большая ось эллипса - лежит на оси Х
		 * @param b2 малая ось эллипса - лежит на оси Y
		 * @param power максимальная энергия потока. Не может быть 0. Если 1, то каждый шаг поток будет действовать на клетку
		 *			если > 0, то поток восходящий, то есть двигает клету к поверхности
		 * @param shadow тип снижения мощности от максимума к минимуму
		 */
		public Ellipse(Point pos, int a2, int b2, int power, StreamForm.SHADOW shadow, int minP){
			super(pos,power,shadow, minP);
			this.a2 = a2;
			this.b2 = b2;
		}
		/**Создание круглого потока
		 * @param pos позиция центра на данный момент
		 * @param d диаметр круга
		 * @param power максимальная энергия потока. Не может быть 0. Если 1, то каждый шаг поток будет действовать на клетку
		 *			если > 0, то поток восходящий, то есть двигает клету к поверхности
		 * @param shadow тип снижения мощности от максимума к минимуму
		 */
		public Ellipse(Point pos, int d, int power, StreamForm.SHADOW shadow, int minP){
			this(pos, d,d, power, shadow, minP);
		}
		
		@Override
		public void action(CellObject cell) {
			final double delY = Point.subtractionY(position.getY(), pos.getY());
			final double delX = Math.abs(Point.subtractionX(position.getX(), pos.getX()));
			if(Math.pow(delX*2 / a2, 2) + Math.pow(delY*2 / b2, 2) <= 1) return;
			
			
			
			final var pos = cell.getPos();
			final var delX = Math.abs(Point.subtractionX(position.getX(), pos.getX()));
			var delY = Point.subtractionY(position.getY(), pos.getY());
			if(delX*2 > width || Math.abs(delY)*2 > height) return;
			
			//Сила затягивания/выталкивания к центральной точке
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

		@Override
		protected double getEnergy(Point pos) {
			final var delY = (double) Point.subtractionY(position.getY(), pos.getY());
			final var delX = (double) Math.abs(Point.subtractionX(position.getX(), pos.getX()));
			if(Math.pow(delX*2 / a2, 2) + Math.pow(delY*2 / b2, 2) <= 1) return 0d;
			switch (shadow) {
				case CENTER -> {
					if(delX * b2 > delY * a2){
						return power * (Math.abs(delY) * 2) / b2;
					} else {
						return power * (delX * 2) / a2;
					}
				}
				case DOWN -> {
					if(Math.abs(delY) * 2 > b2) return 0d;
					else return power * (b2 - (delY + b2/2)) / b2;
				}
				case NONE -> {
					return power;
				}
				default -> throw new AssertionError();
			}
		}
	}
	
		
	/**Форма потока*/
	public abstract static class StreamForm{
		/**Позиция центра звезды*/
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
		/**Создание универсальной формы
		 * @param pos позиция центра потока при создании
		 * @param p максимальная энергия потока
		 * @param s способ рассеения мощности от расстояния.
		 * @param minP энергия на самом краешке
		 */
		protected StreamForm(Point pos, int p, SHADOW s, int minP) {
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
		protected StreamForm(Point pos, int p) {
			this(pos, p, SHADOW.NONE, p);
		}
		
		/**Обрабатывает сдувание клетки в определённую сторону потоком
		 * @param cell клетка, на которую поток воздействует
		 */
		protected abstract void action(CellObject cell);
	}

    /**
     * Создаёт гейзер
     *
     * @param center Позиция центра гейзера
     * @param width ширина гейзера
     * @param w ширина экрана в пикселях
     * @param h высота экрана в пикселях
     * @param dir направление движения воды в гейзере - вверх или вниз
     * @param calm показывает силу работы гейзера. Каждые сколько тиков клетку в
     * центре будет сдувать
     */
    public Stream(int center, int width, int w, int h, DIRECTION dir, int calm) {
        this.center = center;
        if (dir != DIRECTION.DOWN) {
            dir = DIRECTION.UP;
        }
        this.width = width / 2;
        this.dir = dir;
        this.calm = Math.max(1, calm);
        updateScreen(w, h);
    }


    /**
     * Клетка проходящая через гейзер. Или не проходящая, как получится
     */
    public void action(CellObject cell) {
        var distX = Point.subtractionX(center, cell.getPos().getX());
        if (Math.abs(distX) > width) {
            return;
        }
        //Сила затягивания клетки вниз. Линейный закон
        var F = calm + Math.abs(calm * distX / width);
        if (cell.getAge() % F == 0) {
            cell.moveD(dir); // Поехали по направлению!
        }		//Сила затягивания клетки к центру. Линейный закон
        var distY = ((double) cell.getPos().getY()) / Configurations.MAP_CELLS.height;
        F = (int) (calm + Configurations.MAP_CELLS.height * (Math.abs((distY - 0.5))));
        if (cell.getAge() % F == 0) { //Затягиваемся или выталкиваемся
            boolean right = distX > 0;
            if (distY < 0.5) {// Сдувание клетки в верхней части гейзера (засасывание/выталкивание)
                if (right && dir == DIRECTION.UP || !right && dir == DIRECTION.DOWN) {
                    cell.moveD(DIRECTION.RIGHT);
                } else if (right && dir == DIRECTION.DOWN || !right && dir == DIRECTION.UP) {
                    cell.moveD(DIRECTION.LEFT);
                } else if (Math.random() < 0.5) {
                    cell.moveD(DIRECTION.RIGHT);
                } else {
                    cell.moveD(DIRECTION.LEFT);
                }
            } else {
                if (right && dir == DIRECTION.UP || !right && dir == DIRECTION.DOWN) {
                    cell.moveD(DIRECTION.LEFT);
                } else if (right && dir == DIRECTION.DOWN || !right && dir == DIRECTION.UP) {
                    cell.moveD(DIRECTION.RIGHT);
                } else if (Math.random() < 0.5) {
                    cell.moveD(DIRECTION.RIGHT);
                } else {
                    cell.moveD(DIRECTION.LEFT);
                }
            }
        }
    }
}