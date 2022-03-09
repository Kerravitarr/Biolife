package main;

import Utils.JSONmake;

public class Point{
	/**Направление*/
	public enum DIRECTION {
		UP(0,-1), UP_R(1,-1), RIGHT(1,0), DOWN_R(1,1), DOWN(0,1), DOWN_L(-1,1), LEFT(-1,0), UP_L(-1,-1);
		
		private static final DIRECTION[] myEnumValues = DIRECTION.values();
		public static DIRECTION toEnum(int direction) {
			while (direction >= myEnumValues.length)
				direction -= myEnumValues.length;
			while (direction < 0)
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
		public DIRECTION next() {
			return toEnum(toNum(this)+1);
		}
		public DIRECTION prev() {
			return toEnum(toNum(this)-1);
		}
	};
	private static double pixelXDel;
	private static double pixelYDel;
	
	public int x;
	public int y;
	public Point(int x, int y){
		setX(x);
		setY(y);
	}
	public Point(Point point) {
		setX(point.x);
		setY(point.y);
	}
	public Point(JSONmake j) {
		setX(j.getI("x"));
		setY(j.getI("y"));
	}
	public int getRx() {
		return (int) Math.round(x*World.scale + pixelXDel);
	}
	public int getRy() {
		return (int) Math.round(y*World.scale + pixelYDel);
	}
	public int getRr() {
		return (int) Math.round(World.scale) ;
	}
	public Point next(DIRECTION dir) {
		setX(x + dir.addX);
		setY(y + dir.addY);
		return this;
	}
	public void setX(int x) {
		while(x >= World.MAP_CELLS.width)
			x -= World.MAP_CELLS.width;
		while(x < 0)
			x += World.MAP_CELLS.width;
		this.x = x;
	}
	public void setY(int y) {
		this.y=y;
	}
	public JSONmake toJSON() {
		JSONmake make = new JSONmake();
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
		pixelXDel = World.border.width + World.scale/2;
		pixelYDel = World.border.height + World.scale/2;
	}
}