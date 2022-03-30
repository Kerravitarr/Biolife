package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class CloneR extends CloneA {
	
	@Override
	protected void doing(AliveCell cell) {
		clone(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
}
