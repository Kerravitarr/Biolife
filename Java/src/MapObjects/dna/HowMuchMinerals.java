package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;

public class HowMuchMinerals extends CommandExplore {
	
	@Override
	protected int explore(AliveCell cell) {
		return cell.getPos().getY() >= (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL) ? 0 : 1;
	}
}
