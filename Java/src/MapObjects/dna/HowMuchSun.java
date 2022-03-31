package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;

/**
 * Проверят как много солнца, можем ли мы получтить хотя бы что ни будь из него
 * @author Kerravitarr
 *
 */
public class HowMuchSun extends CommandExplore {

	protected HowMuchSun() {super("☀∸","Много солнца?", 2);}

	@Override
	protected int explore(AliveCell cell) {
		double t = (1 + cell.photosynthesisEffect) * cell.getMineral() / AliveCell.MAX_MP;
		return Configurations.sun.getEnergy(cell.getPos()) + t > 0 ? 0 : 1;
	}
}
