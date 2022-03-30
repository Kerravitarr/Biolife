package MapObjects.dna;

import MapObjects.AliveCell;

public class HowMuchHP extends CommandExplore {
	
	public HowMuchHP() {super(1);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, AliveCell.MAX_HP);
		return cell.getHealth() < param ? 0 : 1;
	}
}
