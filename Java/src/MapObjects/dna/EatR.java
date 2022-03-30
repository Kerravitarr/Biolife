package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class EatR extends EatA {

	@Override
	protected void doing(AliveCell cell) {
		eat(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
}
