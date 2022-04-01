package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Толкает объект рядом.
 * Если не толкнуть, то может оттолкнуть себя
 * @author Kerravitarr
 *
 */
public class PullR extends PullA {
	public PullR() {super("↭ O","Толкнуть О");};
	@Override
	protected void doing(AliveCell cell) {
		pull(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
	public String getParam(AliveCell cell, int numParam, int value) {return relativeDirection(cell, value);};
}
