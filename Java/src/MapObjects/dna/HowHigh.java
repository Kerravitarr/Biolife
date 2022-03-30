package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;

public class HowHigh extends CommandExplore {

	public HowHigh() {super(1);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, Configurations.MAP_CELLS.height);
		return cell.getPos().getY() < param ? 0 : 1;
	}
}
