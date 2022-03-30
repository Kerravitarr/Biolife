package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class CareR extends CareA {
	@Override
	protected void doing(AliveCell cell) {
		care(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
}
