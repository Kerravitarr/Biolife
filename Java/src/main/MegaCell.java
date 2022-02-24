package main;

import java.util.ArrayList;
import java.util.List;

import main.Point.DIRECTION;

public class MegaCell {

	private List<Cell> cels = new ArrayList<>();
	/**Флаг, показывает, что нам нужно пересчитаться - возможно колония мертва и пора создать несколько колоний*/
	boolean isNeedUpdate = false;
	private class Point{
		double x = 0;
		double y = 0;
	}
	Point point = new Point();
	
	public MegaCell(MegaCell megaCell, MegaCell megaCell2) {
		cels.addAll(megaCell.cels);
		cels.addAll(megaCell2.cels);
	}

	public MegaCell() {}

	public void add(Cell cell) {
		cels.add(cell);
	}

	public void moveA(DIRECTION direction) {
		point.x += 1.0*direction.addX/cels.size();
		point.y += 1.0*direction.addY/cels.size();
	}

	public void remove(Cell cell) {
		cels.remove(cell);
		isNeedUpdate = true;
	}

	public void step() {
		
	}

}
