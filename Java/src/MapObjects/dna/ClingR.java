package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Присасывается, объединясь, с ближайшей клеткой
 * @author Kerravitarr
 *
 */
public class ClingR extends ClingA {
	public ClingR() {super("□∪□ O","Присосаться O");};
	@Override
	protected void doing(AliveCell cell) {
		cling(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return relativeDirection(cell,param(dna,0, DIRECTION.size()));}
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){return getInterrupt(cell, dna, false);}
}
