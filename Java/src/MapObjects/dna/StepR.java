package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class StepR extends StepA {
	@Override
	protected void doing(AliveCell cell) {
		DIRECTION dir = relatively(cell, param(cell,0, DIRECTION.size()));
		step(cell,dir);
	}
}
