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
	/**Толкает объект относительно своего местоположения*/
	public PullR() {super("↭ O","Толкнуть О",false);};
	@Override
	protected void doing(AliveCell cell) {
		pull(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return relativeDirection(cell,param(dna,0, DIRECTION.size()));}
}
