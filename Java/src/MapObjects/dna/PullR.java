package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class PullR extends PullA {
	@Override
	protected void doing(AliveCell cell) {
		pull(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
}
