package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class GiveR extends GiveA {
	@Override
	protected void doing(AliveCell cell) {
		give(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
}
