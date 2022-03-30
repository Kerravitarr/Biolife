package MapObjects.dna;

import MapObjects.AliveCell;

public class HowOldIAm extends CommandExplore {

	public HowOldIAm() {super(1);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, AliveCell.MAX_HP)*10;
		return cell.getAge() < param ? 0 : 1;
	}
}
