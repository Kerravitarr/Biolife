package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;

public class HowMuchSun extends CommandExplore {

	@Override
	protected int explore(AliveCell cell) {
		double t = (1 + cell.photosynthesisEffect) * cell.getMineral() / AliveCell.MAX_MP;
		return Configurations.sun.getEnergy(cell.getPos()) + t > 0 ? 0 : 1;
	}
}
