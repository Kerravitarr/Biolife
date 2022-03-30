package MapObjects.dna;

import MapObjects.AliveCell;

public class HowMuchMP extends CommandExplore {

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, AliveCell.MAX_MP);
		return cell.getMineral() < param ? 0 : 1;
	}
}
