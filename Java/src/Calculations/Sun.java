package Calculations;

/**
 * Солнышко, которое нас освещает
 * @author Илья
 *
 */
public class Sun {
	/**Форма солнца*/
	private final SunForm form;
	/**Траектория движения солнца*/
	private final SunMove move;
	
	/**Прямоугольное солнце*/
	public static class Rectangle extends SunForm{
		/**Ширина солнца*/
		private final int width;
		/**Высота солнца*/
		private final int height;
		
		/**Создание квадртаного солнца
		 * @param pos позиция верхнего левого угла солнца на данный момент
		 * @param width ширина, в клетках мира
		 * @param height высота, в клетках мира
		 * @param power максимальная энергия солнца
		 * @param shadow тип тени, которая окружает данное солнце
		 */
		public Rectangle(Point pos, int width, int height, double power, SunForm.SHADOW shadow){
			super(new Point(pos.getX() + width / 2, pos.getY() + height / 2),power,shadow);
			this.width = width;
			this.height = height;
		}

		@Override
		protected double getEnergy(Point pos) {
			/*final var delX = Math.abs(Point.subtractionX(position.getX(), pos.getX()));
			var delY = Point.subtractionY(position.getY(), pos.getY());
			if(delX*2 > width || Math.abs(delY)*2 > height) return 0d;
			switch (shadow) {
				case CENTER -> {
					delY = Math.abs(delY);
					if(delX * height > delY * width){
						return power * (delY * 2) / height;
					} else {
						return power * (delX * 2) / width;
					}
				}
				case DOWN -> {
					return power * (height - (delY + height/2)) / height;
				}
				case NONE -> {
					return power;
				}
				default -> throw new AssertionError();
			}*/ throw new AssertionError();
		}
	}
	/**Круглое солнце*/
	public static class Ellipse extends SunForm{
		/**Большая ось эллипса - лежит на оси Х*/
		private final int a2;
		/**Малая ось эллипса - лежит на оси Y*/
		private final int b2;
		
		/**Создание элипсовидного солнца
		 * @param pos позиция центра солнца на данный момент
		 * @param a2 большая ось эллипса - лежит на оси Х
		 * @param b2 малая ось эллипса - лежит на оси Y
		 * @param power максимальная энергия солнца
		 * @param shadow тип тени, которая окружает данное солнце
		 */
		public Ellipse(Point pos, int a2, int b2, double power, SunForm.SHADOW shadow){
			super(pos,power,shadow);
			this.a2 = a2;
			this.b2 = b2;
		}
		/**Создание круглоко солнца
		 * @param pos позиция центра солнца на данный момент
		 * @param d диаметр круга
		 * @param power максимальная энергия солнца
		 * @param shadow тип тени, которая окружает данное солнце
		 */
		public Ellipse(Point pos, int d, double power, SunForm.SHADOW shadow){
			this(pos, d,d, power, shadow);
		}

		@Override
		protected double getEnergy(Point pos) {
			/*final var delY = (double) Point.subtractionY(position.getY(), pos.getY());
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
			}*/ throw new AssertionError();
		}
	}
	/**Фирменное, форменное солнце, прижатое к верхней части экрана*/
	public static class SpecForm extends SunForm{
		/**ширина солнца, в клетках*/
		private final int w;
		/**высота солнца*/
		private final int h;
		/**форма солнца*/
		private final int form;
		
		/**Создание элипсовидного солнца
		 * @param posX позиция центра нижней части солнца на данный момент
		 * @param w ширина солнца, в клетках
		 * @param h высота солнца
		 * @param form особое число формы. 
		 * При x = 0(50%) y = x - треугольное
		 *		При x меньше 0(50%) y = (1 - x)^(-1 / (х - 1) - клин или гипербола стремящаяся к вертикальной тонкой линии посредине
		 *		При x больше 0(50%) y = (1 - x)^(x + 1) - круг стремящийся к квадрату на всю ширину
		 * @param power максимальная энергия солнца
		 * @param shadow тип тени, которая окружает данное солнце
		 */
		public SpecForm(int posX, int w, int h, int form, double power, SunForm.SHADOW shadow){
			super(new Point(posX, 0),power,shadow);
			this.w = w;
			this.h = h;
			this.form = form;
		}

		@Override
		protected double getEnergy(Point pos) {
			/*final double delX = Math.abs(Point.subtractionX(position.getX(), pos.getX()));
			if(pos.getY() > h || delX * 2 > w) return 0;
			switch (shadow) {
				case DOWN, CENTER -> {
					return power * Math.pow(1 - delX / w, form == 0 ? 1 : (form > 0 ? form + 1 : -(1d / (form - 1)))) * (h - pos.getY()) / h;
				}
				case NONE -> {
					return power * Math.pow(1 - delX / w, form == 0 ? 1 : (form > 0 ? form + 1 : -(1d / (form - 1))));
				}
				default -> throw new AssertionError();
			}*/ throw new AssertionError();
		}
	}
	/**Движения солнца по прямой в какую-то одну сторону*/
	public static class LineMove extends SunMove{
		/**Шаг звезды*/
		private final Point step;
		
		/**Линейное движение солнца с постоянной скоростью
		 * @param speed скорость движения. Как часто солнце будет шагать. При 1 - каждый раз, при 2 - каждые 2 хода и т.д.
		 * @param step какой шаг делать звезде каждый раз
		 */
		public LineMove(long speed, Point step){
			super(speed);
			this.step = step;
		}
		@Override
		protected Point step(Point last) {
			return last.add(step);
		}
	}
	/**Движение солнца по орбите вокруг определённой точки*/
	public static class EllipseMove extends SunMove{
		/**Центр, вокруг которого вращаемся*/
		private final Point center;
		/**Эксцентрическая аномалия эллипса. Угол, на который сместилось солнце от начала*/
		private double angle;
		/**Большая ось*/
		private final double a;
		/**Малая ось*/
		private final double b;
		/**Эксцентреситет*/
		private final double e;
		
		/**Движение солнца по орбите
		 * @param speed скорость движения. Как часто солнце будет шагать. При 1 - каждый раз, при 2 - каждые 2 хода и т.д.
		 *				с учётом, что весь круг занимает 360 шагов солнца
		 * @param center центр, вокруг которого будем крутиться
		 * @param startAngle начальный угол
		 * @param a2 большая ось эллипса - лежит на оси Х
		 * @param b2 малая ось эллипса - лежит на оси Y
		 */
		public EllipseMove(long speed, Point center,double startAngle, int a2, int b2){
			super(speed);
			this.center = center;
			this.angle = startAngle;
			this.a = a2 / 2d;
			this.b = b2 / 2d;
			this.e = Math.sqrt(1 - (b*b) / (a*a));
		}
		/**Движдение солнца по кругу
		 * @param speed скорость движения. Как часто солнце будет шагать. При 1 - каждый раз, при 2 - каждые 2 хода и т.д.
		 *				с учётом, что весь круг занимает 360 шагов солнца
		 * @param center центр, вокруг которого будем крутиться
		 * @param startAngle начальный угол
		 * @param d диаметр круга, по которому солнце будет летать
		 */
		public EllipseMove(long speed, Point center,double startAngle, int d){
			this(speed, center, startAngle, d, d);
		}
		@Override
		protected Point step(Point last) {
			angle += Math.PI / 180;
			return new Point((int)Math.round(center.getX() + a * (Math.cos(angle) - e)) ,(int)Math.round( center.getY() + a * Math.sqrt(1 - e * e) * Math.sin(angle)));
		}
	}
	
	/**Форма солнца*/
	public abstract static class SunForm{
		/**Позиция центра звезды*/
		protected Point position;
		/**тень у этой формы*/
		protected SHADOW shadow;
		/**Наибольшая энергия солнца*/
		protected double power;
		
		/**Тип тени звезды*/
		public enum SHADOW {
			/**Нет тени. Яркость везде одинаковая*/
			NONE, 
			/**Чем ниже - тем больше тени*/
			DOWN, 
			/**Чем дальше от центра, тем меньше света*/
			CENTER
		};
		/**Создание универсальной формы
		 * @param pos позиция центра солнца при создании
		 * @param p максимальная энергия солнца
		 * @param s тип тени
		 */
		protected SunForm(Point pos, double p, SHADOW s){position = pos;power=p;shadow = s;}
		
		/**Возвращает количество энергии в заданной точке
		 * @param pos позиция, где нас интересует энергия
		 * @return Количество энергии, что там находитя
		 */
		protected abstract double getEnergy(Point pos);
	}
	/**Траектория движения солнца*/
	public abstract static class SunMove{
		/**Скорость звезды*/
		private final long speed;
		/**Универсальный конструктор
		 * @param speed скорость движения. Как часто солнце будет шагать. При 1 - каждый раз, при 2 - каждые 2 хода и т.д.
		 */
		protected SunMove(long speed){
			if(speed < 1) throw new IllegalArgumentException("Скорость солнца не может быть меньше 1!");
			this.speed = speed;
		}
		
		/**Возвращает необходимость походить.
		 * @param step текущий шаг
		 * @return true, если требуется изменить позицию
		 */
		protected boolean isStep(long step){return step % speed == 0;}
		/**Возвращает новую позицию зведы
		 * @param last старая позиция звезды
		 * @return новая позиция звезды
		 */
		protected abstract Point step(Point last);
	}
	
	
	/**Создаёт солнце
	 * @param form форма солнца. Доступны изначально Rectangle, Ellipse и SpecForm
	 * @param move форма движения солнца. Доступны изначально LineMove и EllipseMove
	 */
	public Sun(SunForm form, SunMove move){
		this.form = form;
		this.move = move;
	}
	/**
	 * Возвращает количество солнечной энергии в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии. Может быть отрицательным числом - это не поглащение света, а удалённость от солнца
	 */
	public double getEnergy(Point pos) {
		return form.getEnergy(pos);
	}

	/**Шаг мира для пересчёт
	 * @param step номер шага мира
	 */
	public void step(long step) {
		if(move != null && move.isStep(step))
			form.position = move.step(form.position);
	}
}
