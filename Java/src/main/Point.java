package main;

import Utils.JSON;

public class Point{
	/**Направление, вектор среди точек*/
	public enum DIRECTION {
		UP(0,-1), UP_R(1,-1), RIGHT(1,0), DOWN_R(1,1), DOWN(0,1), DOWN_L(-1,1), LEFT(-1,0), UP_L(-1,-1);
		
		private static final DIRECTION[] myEnumValues = DIRECTION.values();
		public static DIRECTION toEnum(int direction) {
			direction = direction % myEnumValues.length;
			if (direction < 0)
				direction += myEnumValues.length;
			return myEnumValues[direction];
		}
		public static int toNum(DIRECTION direction) {
			return direction.ordinal();
		}
		public static int size() {
			return myEnumValues.length;
		}
		
		public final int addX;
		public final int addY;
		DIRECTION(int x, int y){
			addX = x;
			addY = y;
		}
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
	};
	private static double pixelXDel;
	private static double pixelYDel;
	
	private int x;
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
	public int getRx() {
		return getRx(x);
	}
	public static int getRx(int x) {
		return (int) Math.round(x*Configurations.scale + pixelXDel);
	}
	public int getRy() {
		return getRy(y);
	}
	public static int getRy(int y) {
		return (int) Math.round(y*Configurations.scale + pixelYDel);
	}
	public int getRr() {
		return getRr(1);
	}
	public static int getRr(int r) {
		return (int) Math.round(r * Configurations.scale) ;
	}
	public Point next(DIRECTION dir) {
		return new Point(x + dir.addX,y + dir.addY);
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
	public String toString() {
		return "x: " + x + " y: " + y;
	}
	public void update(Point point) {
		setX(point.x);
		setY(point.y);
	}
	public static void update() {
		pixelXDel = Configurations.border.width + Configurations.scale/2;
		pixelYDel = Configurations.border.height + Configurations.scale/2;
	}
	public boolean equals(Point obj) {
        return (this.x == obj.x) && (this.y == obj.y);
    }
	public int getX() {return x;}
	public int getY() {return y;}
}