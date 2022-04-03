package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Клонируется и присасывается к потомку
 * @author Kerravitarr
 *
 */
public class CloneR extends CloneA {

	public CloneR() {super("♡∪□ O","Клон и присос O");};
	@Override
	protected void doing(AliveCell cell) {
		clone(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(numParam == 0)
			return relativeDirection(cell, param(dna, 0, DIRECTION.size()));
		else
			return super.getParam(cell, numParam, dna);
	}
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){return getInterrupt(cell, dna, false);}
}
