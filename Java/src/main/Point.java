package main;

import panels.JSONmake;

public class Point{
	/**Направление*/
	public enum DIRECTION {
		UP(0,-1), UP_R(1,-1), RIGHT(1,0), DOWN_R(1,1), DOWN(0,1), DOWN_L(-1,1), LEFT(-1,0), UP_L(-1,-1);
		
		private static final DIRECTION[] myEnumValues = DIRECTION.values();
		static DIRECTION toEnum(int direction) {
			while (direction >= myEnumValues.length)
				direction -= myEnumValues.length;
			while (direction < 0)
				direction += myEnumValues.length;
			return myEnumValues[direction];
		}
		static int toNum(DIRECTION direction) {
			return direction.ordinal();
		}
		static int size() {
			return myEnumValues.length;
		}
		
		final int addX;
		final int addY;
		DIRECTION(int x, int y){
			addX = x;
			addY = y;
		}
	};
	
	int x;
	int y;
	public Point(int x, int y){
		setX(x);
		setY(y);
	}
	public Point(Point point) {
		x = point.x;
		y = point.y;
	}
	public Point(JSONmake j) {
		x = j.getI("x");
		y = j.getI("y");
	}
	public int getRx() {
		return (int) Math.round(World.border.width+x*World.scale + World.scale/2);
	}
	public int getRy() {
		return (int) Math.round(World.border.height+y*World.scale + World.scale/2);
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
		//this.y=Math.max(0, Math.min(y, MAP_CELLS.height -1));
		this.y=y;
	}
	public JSONmake toJSON() {
		JSONmake make = new JSONmake();
		make.add("x", x);
		make.add("y", y);
		return make;
	}
}