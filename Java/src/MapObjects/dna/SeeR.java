package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class SeeR extends SeeA {
	@Override
	protected int explore(AliveCell cell) {
		return cell.seeA(relatively(cell, param(cell,0, DIRECTION.size()))).nextCMD;
	}
}
