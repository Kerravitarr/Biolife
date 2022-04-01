package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class BiteR extends BiteA {
	@Override
	protected void doing(AliveCell cell) {
		bite(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
	public String getParam(AliveCell cell, int numParam, int value) {return relativeDirection(cell, value);};
}
