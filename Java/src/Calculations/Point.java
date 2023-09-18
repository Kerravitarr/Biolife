package Calculations;

import static Calculations.Configurations.WORLD_TYPE.CIRCLE;
import static Calculations.Configurations.WORLD_TYPE.FIELD_R;
import static Calculations.Configurations.WORLD_TYPE.LINE_H;
import static Calculations.Configurations.WORLD_TYPE.LINE_V;
import static Calculations.Configurations.WORLD_TYPE.RECTANGLE;
import Utils.JSON;

/**
Точка на карте мира.
Если мир бесконечный по какой либо плоскости, то точка ВСЕГДА будет в передлах этой плоскости
То есть, точка, оказавшаяся за границе, телепортируется автоматически.
Если в каком либо направлении есть стена, то тут - да, точка уйдёт за границу и тем самым покажет, что она там.

Для удобства в классе есть два "Вектора". Один единичный вектор - DIRECTION. И один полноценный вектор - Vector
@author Kerravitarr
*/
public final class Point{
	/**Все доступные точки мира*/
	private static Point[][] points;
	/**Тип мира, для которого были созданы точки выше. Отвечает за параметр валидности*/
	private static Configurations.WORLD_TYPE type;
	private final static int VECTOR_MIN_X = -5;
	private final static int VECTOR_MAX_X = 5;
	private final static int VECTOR_MIN_Y = -5;
	private final static int VECTOR_MAX_Y = 5;
	/**Базовые вектора мира, которые будут использоваться чаще всего*/
	private final static Vector[][] vectors = new Vector[VECTOR_MAX_X - VECTOR_MIN_X][VECTOR_MAX_Y-VECTOR_MIN_Y];
	static{
		final var maxX = VECTOR_MAX_X - VECTOR_MIN_X;
		final var maxY = VECTOR_MAX_Y - VECTOR_MIN_Y;
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				vectors[x][y] = new Vector(VECTOR_MIN_X + x,VECTOR_MIN_Y + y);
			}
		}
	}
	
	/**
	Единичный вектор. Хотя его длина не всегда равна 1, он точно указвыает все возможные направления на ближайшие, от текущей, точки.
	Заодно является всеми направления сразу
	*/
	public enum DIRECTION {
		UP(0,-1), UP_R(1,-1), RIGHT(1,0), DOWN_R(1,1), DOWN(0,1), DOWN_L(-1,1), LEFT(-1,0), UP_L(-1,-1);
		/**Все возможные значения направлений*/
		public static final DIRECTION[] values = DIRECTION.values();
		/**Преобразует число направления в один из векторов направления
		@param direction любое число, означающее направление (считается положительным. Отрицательные тоже могут быть, но там будет забавно)
		@return реальное направление
		*/
		public static DIRECTION toEnum(int direction) {
			direction = direction % values.length;
			if (direction < 0)
				direction += values.length;
			return values[direction];
		}
		public static int toNum(DIRECTION direction) {
			return direction.ordinal();
		}
		/**Количество возможных направлений
		@return целое, положительное число
		*/
		public static int size() {
			return values.length;
		}
		/**На сколько изменится X при движении в данную сторону*/
		public final int addX;
		/**На сколько изменится Y при движении в данную сторону*/
		public final int addY;
		DIRECTION(int x, int y){addX = x;addY = y;}
		
		/**Поворачивает вектор на direction*/
		public DIRECTION next(DIRECTION direction) {
			return next(toNum(direction));
		}
		/**Поворачивает вектор на direction*/
		public DIRECTION next(int direction) {
			return toEnum(toNum(this)+direction);
		}
		/**Поворачивает вектор на чуть-чуть по часовой стрелки*/
		public DIRECTION next() {
			return toEnum(toNum(this)+1);
		}
		/**Поворачивает вектор на чуть-чуть против часовой стрелки*/
		public DIRECTION prev() {
			return toEnum(toNum(this)-1);
		}
		/**Возвращает противположный вектор к текущему*/
		public DIRECTION inversion() {
			return next(size()/2);
		}
		/**
		 * Превращает направление в символ
		 */
		@Override
		public String toString() {
			switch (this) {
				case DOWN->{return "↓";}
				case DOWN_L->{return "↙";}
				case DOWN_R->{return "↘";}
				case LEFT->{return "←";}
				case RIGHT->{return "→";}
				case UP->{return "↑";}
				case UP_L->{return "↖";}
				case UP_R->{return "↗";}
				default->{return null;}
			}
		}
		/**Превращает направление в букву*/
		public String toSString() {
			switch (this) {
				case DOWN->{return "Ю";}
				case DOWN_L->{return "ЮЗ";}
				case DOWN_R->{return "ЮВ";}
				case LEFT->{return "З";}
				case RIGHT->{return "В";}
				case UP->{return "С";}
				case UP_L->{return "СЗ";}
				case UP_R->{return "СВ";}
				default->{return null;}
			}
		}
	};
	/**
	А это вектор. В отличии от Point вектор может иметь как положительные, так и отрицательные числа. 
	А ещё он НЕ учитывает границы мира. Так как это направление, то от текущей точки он может указывать на точку за границей мира
	*/
	public final static class Vector{
		/**Создаёт вектор нужного размера
		 * @param x длина по Х
		 * @param y длина по Y
		 * @return вектор, размеры которого мы задали
		 */
		public static Vector create(int x, int y) {
			if ((VECTOR_MIN_X <= x && x < VECTOR_MAX_X) && (VECTOR_MIN_Y <= y && y < VECTOR_MAX_Y))
				return vectors[x - VECTOR_MIN_X][y - VECTOR_MIN_Y];
			else
				return new Vector(x, y);
		}
		/**Направление по оси x*/
		public final int x;
		/**Направление по оси y*/
		public final int y;
		/**Гипотинуза, длина вектора*/
		private Double h = null;
		/**Направление вектора*/
		private DIRECTION d = null;
		
		private Vector(int x, int y){this.x = x;this.y = y;};
		/**Возвращает гипотинузу вектор
		 * @return агипотенуза вектора в клетках мира*/
		public double getHypotenuse(){
			if(h == null){
				h = Math.hypot(x, y);
			}
			return h;
		}
		/**Возвращает направление этого вектора
		 * @return направление. Но так как направления может не быть, если это
		 *			таже самая точка, то возвращается null
		 */
		public DIRECTION direction(){
            if(d == null && (x != 0 || y != 0)){
                d = switch (x) {
				    case -1 -> 
					    switch (y) {
						    case -1 -> Point.DIRECTION.UP_L;
						    case 0 -> Point.DIRECTION.LEFT;
						    case 1 -> Point.DIRECTION.DOWN_L;
							default -> null;
					    };
				    case 0 -> 
					    switch (y) {
						    case -1 -> Point.DIRECTION.UP;
						    case 0 -> null;
						    case 1 -> Point.DIRECTION.DOWN;
							default -> null;
					    };
				    case 1 -> 
					    switch (y) {
						    case -1 ->Point.DIRECTION.UP_R;
						    case 0 -> Point.DIRECTION.RIGHT;
						    case 1 -> Point.DIRECTION.DOWN_R;
							default -> null;
					    };
					default -> null;
			    };
                if(d == null){
			        //А жаль, могло-бы быть всё куда проще
			        if(x < 0){
				        if (y == 0) {
					        d = Point.DIRECTION.LEFT;
				        } else if (y < 0) {
					        final var tan = y/x;
					        if(tan < 0.41421356)
						        d = Point.DIRECTION.LEFT;
					        else if(tan > 2.41421356)
						        d = Point.DIRECTION.UP;
					        else 
						        d = Point.DIRECTION.UP_L;
				        } else {
					        final var tan = y/-x;
					        if(tan < 0.41421356)
						        d = Point.DIRECTION.LEFT;
					        else if(tan > 2.41421356)
						        d = Point.DIRECTION.DOWN;
					        else 
						        d = Point.DIRECTION.DOWN_L;
				        }
			        } else if (x == 0) {
				        if(y < 0) d = Point.DIRECTION.UP;
				        else if(y == 0) d = null;
				        else d = Point.DIRECTION.DOWN;
			        } else { //x > 0
				        if (y == 0) {
					        d = Point.DIRECTION.RIGHT;
				        } else if(y < 0) {
					        final var tan = -y/x;
					        if(tan < 0.41421356) //Тангенс 22,5 градуса - примерно 0.41421356. Это-же даёт -0/+1
						        d = Point.DIRECTION.RIGHT;
					        else if(tan > 2.41421356) //Тангенс 65,5 градусов - 2.41421356 Это-же даёт -1/+0
						        d = Point.DIRECTION.UP;
					        else  //Это-же даёт -1/+1
						        d = Point.DIRECTION.UP_R;
				        } else { //y > 0
					        final var tan = y/x;
					        if(tan < 0.41421356)
						        d = Point.DIRECTION.RIGHT;
					        else if(tan > 2.41421356)
						        d = Point.DIRECTION.DOWN;
					        else 
						        d = Point.DIRECTION.DOWN_R;
				        }
			        }
                }
            }
			return d;
		}
		
		@Override
		public String toString() {
			return "V⃗ (" + x + "; " + y + "). |V⃗|="+getHypotenuse();
		}
	}
	
	/**Координата по Х*/
	private final int x;
	/**Координата по Y*/
	private final int y;
	/**Валидность точки*/
	private final boolean isValid;
	
	private Point(int x, int y, boolean isValid){
		this.x = x;
		this.y = y;
		this.isValid = isValid;
	}
	/**Функция создания точки
	 * @param x координата, которая будет преобразована в координаты мира
	 * @param y координата, которая будет преобразована в координаты мира
	 * @return указатель на неизменяемый объект точки
	 */
	public static Point create(int x, int y) {
		//Нормализуем x и y
		final var width = Configurations.getWidth();
		final var height = Configurations.getHeight();
		switch (Configurations.confoguration.world_type) {
			case LINE_H -> {
				if(x < 0 || x >= width){
					x = x % width;
					if(x < 0)
						x += width;
				}
			}
			case LINE_V -> {
				if(y < 0 || y >= height){
					y = y % height;
					if(y < 0)
						y += height;
				}
			}
			case CIRCLE, RECTANGLE -> {}
			case FIELD_R -> {
				if(x < 0 || x >= width){
					x = x % width;
					if(x < 0)
						x += width;
				}
				if(y < 0 || y >= height){
					y = y % height;
					if(y < 0)
						y += height;
				}
			}
			default -> throw new AssertionError();
		}
		//Проверяем наш выделенный объём точек
		if(points == null || points.length != width || points[0].length != height || type != Configurations.confoguration.world_type){
			//Если надо, выделяем объём под все точки карты
			type = Configurations.confoguration.world_type;
			points = new Point[width][height];
			for (int tx = 0; tx < width; tx++) {
				for (int ty = 0; ty < height; ty++) {
					points[tx][ty] = new Point(tx, ty,valid(tx,ty));
				}
			}
		}
		if((0 <= x && x < width) && (0 <= y && y < height))
			return points[x][y];
		else
			return new Point(x, y,valid(x,y));
	}

	public static Point create(Point point) {
		return point;
	}

	public static Point create(JSON j) {
		return create(j.get("x"),j.get("y"));
	}
	
	
	/**Получить точку в указанном направлении от текущей
	 * @param dir в каком направлении нужна точка
	 * @return точка в нужном направлении
	 */
	public Point next(DIRECTION dir) {
		return Point.create(x + dir.addX, y + dir.addY);
	}
	/**Суммировать две точки
	 * @param point к какой точке прибавляем
	 * @return точка , являющаяся суммой этой и добавочной
	 */
	public Point add(Point point) {
		return Point.create(x + point.x, y + point.y);
	}
	/**Вычесть из этой точки, другую
	 * @param point к какой точке прибавляем
	 * @return точка , являющаяся суммой этой и добавочной
	 */
	public Point sub(Point point) {
		return Point.create(x - point.x, y - point.y);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Point p) {
			return equals(p);
		} else {
			return super.equals(obj);
		}
	}
	public boolean equals(Point obj) {
        return (this.x == obj.x) && (this.y == obj.y);
    }
	public int getX() {return x;}
	public int getY() {return y;}
	
	/**
	 * Функция нахождения расстояния между двумя точками.
		Иными словами. Полученный вектор указывает от точки this к точке second.
		this + return = second
	 * Если x больше 0, то значит вторая точка правее
	 * Если y больше 0, то значит вторая точка ниже
	 * @param second вторая точка
	 * @return Расстояние между двумя точками.
	 */
	public Vector distance(Point second) {
		return distance(this,second);
	}
	/**
	 * Функция нахождения расстояния между двумя точками.
		Иными словами. Полученный вектор указывает от точки first к точке second.
		furst + return = second
	 * Если x больше 0, то значит вторая точка правее
	 * Если y больше 0, то значит вторая точка ниже
	 * @param first первая точка
	 * @param second вторая точка
	 * @return Расстояние между двумя точками.
	 */
	public static Vector distance(Point first, Point second) {
		final var width = Configurations.confoguration.MAP_CELLS.width;
		final var height = Configurations.confoguration.MAP_CELLS.height;
		switch (Configurations.confoguration.world_type) {
			case LINE_H -> {
				//Расстояние между двумя точками. [-width;width]
				var del = second.x - first.x;
				if(!(-width / 2 <= del && del <= width / 2)){
					//Как только расстояние между двумя точками больше половины ширины экрана.
					//Нам ближе будет пройти с обратной стороны
					if(del > 0)
						del -= width;
					else 
						del += width;
				}
				return Vector.create(del, second.y - first.y);
			}
			case LINE_V -> {
				var del = second.y - first.y;
				if(!(-height / 2 <= del && del <= height / 2)){
					if(del > 0)
						del -= height;
					else 
						del += height;
				}
				return Vector.create(second.x - first.x, del);
			}
			case FIELD_R -> {
				var dx = second.x - first.x;
				if(!(-width / 2 <= dx && dx <= width / 2)){
					if(dx > 0)
						dx -= width;
					else 
						dx += width;
				}
				var dy = second.y - first.y;
				if(!(-height / 2 <= dy && dy <= height / 2)){
					if(dy > 0)
						dy -= height;
					else 
						dy += height;
				}
				return Vector.create(dx, dy);
			}
			case RECTANGLE, CIRCLE -> {
				return Vector.create(second.x - first.x, second.y - first.y);
			}
			default -> throw new AssertionError();
		}
	}
	/**
	 * Возвращает направление от this к s
		при условии, если две точки находятся рядом друг с другом
	 * @param s вторая клетка
	 * @return направление стрелки от this к s

	 * @throws IllegalArgumentException если расстояние между точками больш 1
	 */
	public DIRECTION direction(Point s){
		return direction(this,s);
	}
	/**
	 * Возвращает направление от f к s
		при условии, если две точки находятся рядом друг с другом
	 * @param f первая клетка
	 * @param s вторая клетка
	 * @return направление стрелки от f к s

	 * @throws IllegalArgumentException если расстояние между точками больш 1
	 */
	public static DIRECTION direction(Point f, Point s){
		var v = distance(f, s);
		for(var d : DIRECTION.values)
			if(d.addX == v.x && v.y == d.addY)
				return d;
		throw new IllegalArgumentException("Расстояние между точками должно быть ровно 1 клетка!");
	}
	
	private static boolean valid(int x, int y){
		final var width = Configurations.confoguration.MAP_CELLS.width;
		final var height = Configurations.confoguration.MAP_CELLS.height;
		switch (Configurations.confoguration.world_type) {
			case LINE_H -> {
				return y >= 0 && y < height;
			}
			case LINE_V -> {
				return x >= 0 && x < width;
			}
			case RECTANGLE -> {
				return y >= 0 && y < height && x >= 0 && x < width;
			}
			case CIRCLE -> {
				if(height == width){
					final var r = width / 2d;
					return Math.pow((r - 0.5) - x, 2) + Math.pow((r-0.5) - y, 2) <= r*r; //Уравнение окружности - x*x + y*y = r*r
				} else {
					final var a = width / 2d;
					final var b = height / 2d;
					return Math.pow((a - 0.5) - x, 2) / (a * a) + Math.pow((b-0.5) - y, 2) / (b * b) <= 1; //Уравнение эллипса - (x*x)/(a*a) + (y*y)/(b*b) = 1
				}
			}
			case FIELD_R-> {
				return true;
			}
			default -> throw new AssertionError();
		}
	}
	
	/**Проверяте точку на принадлежность текущему миру
	 * @return true, если точка находится на поле
	 */
	public boolean valid(){return isValid;}
	
	/**Упаковывает точку в JSON
	 * @return 
	 */
	public JSON toJSON() {
		JSON make = new JSON();
		make.add("x", x);
		make.add("y", y);
		return make;
	}
	@Override
	public String toString() {
		return "(P (" + x + "; " + y + "))";
	}
	
}










