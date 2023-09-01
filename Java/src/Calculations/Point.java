package Calculations;

import Utils.JSON;

/**
Точка на карте мира.
Если мир бесконечный по какой либо плоскости, то точка ВСЕГДА будет в передлах этой плоскости
То есть, точка, оказавшаяся за границе, телепортируется автоматически.
Если в каком либо направлении есть стена, то тут - да, точка уйдёт за границу и тем самым покажет, что она там.

Для удобства в классе есть два "Вектора". Один единичный вектор - DIRECTION. И один полноценный вектор - Vector
@author Kerravitarr
*/
public class Point{
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
	public static class Vector{
		/**Направление по оси x*/
		public final int x;
		/**Направление по оси y*/
		public final int y;
		/**Гипотинуза, дляна вектора*/
		private Double h = null;
		
		private Vector(int x, int y){this.x = x;this.y = y;};
		/**Возвращает гипотинузу вектора*/
		public double getHypotenuse(){
			if(h == null){
				h = Math.sqrt(x*x+y*y);
			}
			return h;
		}
		@Override
		public String toString() {
			return "[x: " + x + " y: " + y + "] => " + h;
		}
	}
	
	/**Координата по Х*/
	private int x;
	/**Координата по Y*/
	private int y;
	public Point(int x, int y){
		setXY(x,y);
	}
	public Point(Point point) {
		setXY(point.x,point.y);
	}
	public Point(JSON j) {
		setXY(j.get("x"),j.get("y"));
	}
	/**Получить точку в указанном направлении от текущей
	 * @param dir в каком направлении нужна точка
	 * @return точка в нужном направлении
	 */
	public Point next(DIRECTION dir) {
		return new Point(x + dir.addX,y + dir.addY);
	}
	/**Суммировать две точки
	 * @param point к какой точке прибавляем
	 * @return точка , являющаяся суммой этой и добавочной
	 */
	public Point add(Point point) {
		return new Point(x + point.x,y + point.y);
	}
	/**Вычесть из этой точки, другую
	 * @param point к какой точке прибавляем
	 * @return точка , являющаяся суммой этой и добавочной
	 */
	public Point sub(Point point) {
		return new Point(x - point.x,y - point.y);
	}
	public void update(Point point) {
		setXY(point.x,point.y);
	}
	public boolean equals(Point obj) {
        return (this.x == obj.x) && (this.y == obj.y);
    }
	public int getX() {return x;}
	public int getY() {return y;}
	
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
		switch (Configurations.world_type) {
			case LINE_H -> {
				//Расстояние между двумя точками. [-width;width]
				var del = second.x - first.x;
				if(!(-Configurations.MAP_CELLS.width / 2 <= del && del <= Configurations.MAP_CELLS.width / 2)){
					//Как только расстояние между двумя точками больше половины ширины экрана.
					//Нам ближе будет пройти с обратной стороны
					if(del > 0)
						del = Configurations.MAP_CELLS.width - del;
					else 
						del += Configurations.MAP_CELLS.width;
				}
				return new Vector(del, second.y - first.y);
			}
			case LINE_V -> {
				var del = second.y - first.y;
				if(!(-Configurations.MAP_CELLS.height / 2 <= del && del <= Configurations.MAP_CELLS.height / 2)){
					if(del > 0)
						del = Configurations.MAP_CELLS.height - del;
					else 
						del += Configurations.MAP_CELLS.height;
				}
				return new Vector(second.x - first.x, del);
			}
			case FIELD_R -> {
				var dx = second.x - first.x;
				if(!(-Configurations.MAP_CELLS.width / 2 <= dx && dx <= Configurations.MAP_CELLS.width / 2)){
					if(dx > 0)
						dx = Configurations.MAP_CELLS.width - dx;
					else 
						dx += Configurations.MAP_CELLS.width;
				}
				var dy = second.y - first.y;
				if(!(-Configurations.MAP_CELLS.height / 2 <= dy && dy <= Configurations.MAP_CELLS.height / 2)){
					if(dy > 0)
						dy = Configurations.MAP_CELLS.height - dy;
					else 
						dy += Configurations.MAP_CELLS.height;
				}
				return new Vector(dx, dy);
			}
			case FIELD_C -> {
				if(Configurations.MAP_CELLS.height == Configurations.MAP_CELLS.width){
					//У нас круглый мир. Это сильно упрощает картину!
					final var r = Configurations.MAP_CELLS.width / 2;
					//Хотя даже в круглом мире упрощение до жути относительное
					var dx = second.x - first.x;
					var dy = second.y - first.y;
					final var h = Math.sqrt(dx*dx + dy*dy);
					if(!(-r <= h && h <= r)){
						//Расстояние больше радиуса. А всем известно, что в таком случае надо тупо идти в другую сторону
						//Иными словами мы переносим точку через центр
						if(dx > 0)
							dx = (int) ((dx * r / h) - dx);
						else
							dx += (dx * r / h);
						if(dy > 0)
							dy = (int) ((dy * r / h) - dy);
						else
							dy += (dy * r / h);
					}
					return new Vector(dx, dy);
				} else {
					//Эллипс.... Ну всё. Писец.
					//Я пока не понял, как работает телепортация в мире эллипсов, так что не в этот раз :)
					//Надо найти радиус, тогда можно провернуть тоже, что и с кругом. Но какой радиус в конкретной точке... Вопрос
					if(0 == 0) //Чтобы компилятор не ругался
						throw new IllegalArgumentException("К сожалению, разомкнутый мир должен быть только круглым! Я не придумал как обсчитывать элипсы");
					final var r = 0;
					var dx = second.x - first.x;
					var dy = second.y - first.y;
					final var h = Math.sqrt(dx*dx + dy*dy);
					if(!(-r <= h && h <= r)){
						//Расстояние больше радиуса. А всем известно, что в таком случае надо тупо идти в другую сторону
						//Иными словами мы переносим точку через центр
						if(dx > 0)
							dx = (int) ((dx * r / h) - dx);
						else
							dx += (dx * r / h);
						if(dy > 0)
							dy = (int) ((dy * r / h) - dy);
						else
							dy += (dy * r / h);
					}
					return new Vector(dx, dy);
				}
			}
			case RECTANGLE, CIRCLE -> {
				return new Vector(second.x - first.x, second.y - first.y);
			}
			default -> throw new AssertionError();
		}
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
		throw new IllegalArgumentException("Расстояние между точками не должно быть больше 1!");
	}
	/**Проверяте точку на принадлежность текущему миру
	 * @return true, если точка находится на поле
	 */
	public boolean valid(){
		switch (Configurations.world_type) {
			case LINE_H -> {
				return y > 0 && y < Configurations.MAP_CELLS.height;
			}
			case LINE_V -> {
				return x > 0 && x < Configurations.MAP_CELLS.width;
			}
			case RECTANGLE -> {
				return y > 0 && y < Configurations.MAP_CELLS.height && x > 0 && x < Configurations.MAP_CELLS.width;
			}
			case CIRCLE -> {
				if(Configurations.MAP_CELLS.height == Configurations.MAP_CELLS.width){
					final var r = Configurations.MAP_CELLS.width / 2;
					return Math.pow(r - x, 2) + Math.pow(r - y, 2) < r*r; //Уравнение окружности - x*x + y*y = r*r
				} else {
					throw new IllegalArgumentException("К сожалению, разомкнутый мир должен быть только круглым! Я не придумал как обсчитывать элипсы");
				}
			}
			case FIELD_R, FIELD_C-> {
				return true;
			}
			default -> throw new AssertionError();
		}
	}
	
	
	/**Сохраняет координаты, нормализуя их при необходимости
	 * @param x не нормализованная координата x
	 * @param y не нормализованная координата y
	 */
	private void setXY(int x, int y) {
		switch (Configurations.world_type) {
			case LINE_H -> {
				if(x < 0 || x >= Configurations.MAP_CELLS.width){
					this.x = x % Configurations.MAP_CELLS.width;
					if(this.x < 0)
						this.x += Configurations.MAP_CELLS.width;
				}else{
					this.x = x;
				}
				this.y = y;
			}
			case LINE_V -> {
				this.x = x;
				if(y < 0 || y >= Configurations.MAP_CELLS.height){
					this.y = y % Configurations.MAP_CELLS.height;
					if(this.y < 0)
						this.y += Configurations.MAP_CELLS.height;
				}else{
					this.y = y;
				}
			}
			case CIRCLE, RECTANGLE -> {
				this.x = x;
				this.y = y;
			}
			case FIELD_R -> {
				if(x < 0 || x >= Configurations.MAP_CELLS.width){
					this.x = x % Configurations.MAP_CELLS.width;
					if(this.x < 0)
						this.x += Configurations.MAP_CELLS.width;
				}else{
					this.x = x;
				}
				if(y < 0 || y >= Configurations.MAP_CELLS.height){
					this.y = y % Configurations.MAP_CELLS.height;
					if(this.y < 0)
						this.y += Configurations.MAP_CELLS.height;
				}else{
					this.y = y;
				}
			}
			case FIELD_C -> {
				if(Configurations.MAP_CELLS.height == Configurations.MAP_CELLS.width){
					//У нас круг. Точки телепортируются через центр поля
					final var r = Configurations.MAP_CELLS.width / 2;
					//Найдём расстояние от заданных координат до центра окружности
					final var vec = new Vector(r - x, r - y);
					if(vec.getHypotenuse() > r){
						//Мы находимся от центра дальше радиуса... Надо двигаться к центру
						//Вот на столько шагов
						final var scale = ((int) vec.getHypotenuse()) / Configurations.MAP_CELLS.width;
						this.x = (int) (x + (vec.x / vec.getHypotenuse()) * scale);
						this.y = (int) (y + (vec.y / vec.getHypotenuse()) * scale);
					} else {
						this.x = x;
						this.y = y;
					}
				} else {
					throw new IllegalArgumentException("К сожалению, разомкнутый мир должен быть только круглым! Я не придумал как обсчитывать элипсы");
				}
			}
			default -> throw new AssertionError();
		}
	}
	
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
		return "x: " + x + " y: " + y;
	}
	
}










