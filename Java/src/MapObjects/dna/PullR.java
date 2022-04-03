package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.dna.CommandDNA.param;
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
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return relativeDirection(cell,param(dna,0, DIRECTION.size()));}
	
	
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){
		DIRECTION direction = relatively(cell, param(dna,0, DIRECTION.size()));
		return getInterrupt(cell, dna, direction);
	}
}
