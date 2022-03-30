package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class TurnAroundR extends TurnAroundA {

	@Override
	protected void doing(AliveCell cell) {
		cell.direction = relatively(cell, param(cell,0, DIRECTION.size()));
	}
}
