package Calculations;

import Utils.JSON;

public class Point{
	/**Направление, вектор среди точек*/
	public enum DIRECTION {
		UP(0,-1), UP_R(1,-1), RIGHT(1,0), DOWN_R(1,1), DOWN(0,1), DOWN_L(-1,1), LEFT(-1,0), UP_L(-1,-1);
		/**Все возможные значения направлений*/
		public static final DIRECTION[] values = DIRECTION.values();
		public static DIRECTION toEnum(int direction) {
			direction = direction % values.length;
			if (direction < 0)
				direction += values.length;
			return values[direction];
		}
		public static int toNum(DIRECTION direction) {
			return direction.ordinal();
		}
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
	/**Координата по Х*/
	private int x;
	/**Координата по Y*/
	private int y;
	public Point(int x, int y){
		setX(x);
		setY(y);
	}
	public Point(Point point) {
		setX(point.x);
		setY(point.y);
	}
	public Point(JSON j) {
		setX(j.get("x"));
		setY(j.get("y"));
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
	public void update(Point point) {
		setX(point.x);
		setY(point.y);
	}
	public boolean equals(Point obj) {
        return (this.x == obj.x) && (this.y == obj.y);
    }
	public int getX() {return x;}
	public int getY() {return y;}
	
	/**
	 * Вычисляет расстояние между двумя точками
	 * @param next следующая точка
	 * @return расстояние
	 */
	public double hypotenuse(Point next) {
		var delx = subtractionX(x,next.x);
		var dely = y - next.y;
		return Math.sqrt(delx*delx+dely*dely);
	}
	/**
	 * Функция нахождения расстояния между двумя точками. Иными словами
	 * [second.x-first.x, second.y-first.y]
	 * Если x больше 0, то значит вторая точка правее
	 * Если y больше 0, то значит вторая точка ниже
	 * @param first первая точка
	 * @param second вторая точка
	 * @return Расстояние между двумя точками.
	 */
	public static Point sub(Point first, Point second) {
		switch (Configurations.world_type) {
			case LINE_H -> {
				var del = second.x - first.x;
				if(del > 0 && del > Configurations.MAP_CELLS.width / 2)
					del -= Configurations.MAP_CELLS.width / 2;
				var cdel = del + (first.x <= second.x ?  - Configurations.MAP_CELLS.width : + Configurations.MAP_CELLS.width);
				return new Point(Math.abs(del) < Math.abs(cdel) ? del : cdel, second.x - first.y);
			}
			default -> throw new AssertionError();
		}
		var del = xs - xf;
		var cdel = del + (xf <= xs ?  - Configurations.MAP_CELLS.width : + Configurations.MAP_CELLS.width);
		return Math.abs(del) < Math.abs(cdel) ? del : cdel;
	}
	/**
	 * Функция нахождения минимального расстояния между двумя точками по Y
	 * @param yf первая точка по Y
	 * @param ys вторая точка по Y
	 * @return Расстояние между двумя точками.
	 */
	public static int subtractionY(int yf, int ys) {
		return ys - yf;
	}
	
	/**
	 * Возвращает направление от f к s
	 * @param f первая клетка
	 * @param s вторая клетка
	 * @return направление стрелки от f к s
	 */
	public static DIRECTION direction(Point f, Point s){
		var dy = s.y - f.y;
		var dx = subtractionX(f.x, s.x);
		for(var d : DIRECTION.values)
			if(d.addX == dx && dy == d.addY)
				return d;
		throw new IllegalArgumentException("Расстояние между точками не должно быть больше 1!");
	}
	
	private void setX(int x) {
		while(x >= Configurations.MAP_CELLS.width)
			x -= Configurations.MAP_CELLS.width;
		while(x < 0)
			x += Configurations.MAP_CELLS.width;
		this.x = x;
	}
	private void setY(int y) {
		this.y=y;
	}
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










