package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class BiteR extends BiteA {
	public BiteR() {super("🍗 О", "Кусить О",false);};
	@Override
	protected void doing(AliveCell cell) {
		bite(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return relativeDirection(cell,param(dna,0, DIRECTION.size()));}
}
