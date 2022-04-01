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
	public String getParam(AliveCell cell, int numParam, int value) {return relativeDirection(cell, value);};
}
