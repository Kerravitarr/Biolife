package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class SeeA extends CommandExplore {

	public SeeA() {super(1);};
	@Override
	protected int explore(AliveCell cell) {
		return cell.seeA(DIRECTION.toEnum(param(cell,0, DIRECTION.size()))).nextCMD;
	}
}
