package MapObjects.dna;

import MapObjects.AliveCell;

public class HowMuchDW extends CommandExplore {
	
	public HowMuchDW() {super(1);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0);
		return cell.getDNA_wall() < param ? 0 : 1;
	}
}
