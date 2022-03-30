package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class ClingR extends ClingA {
	@Override
	protected void doing(AliveCell cell) {
		cling(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
}
