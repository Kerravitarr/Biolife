package MapObjects.dna;

import MapObjects.AliveCell;
import Calculations.Configurations;

/**
 * Проверяет, мы выше или ниже высоты из параметра
 * @author Kerravitarr
 *
 */
public class HowHigh extends CommandExplore {

	public HowHigh() {super(1,2);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, Configurations.MAP_CELLS.height);
		return cell.getPos().getY() >= param ? 0 : 1;
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(cell, 0, Configurations.MAP_CELLS.height));
	}
	public String getBranch(AliveCell cell, int numBranch, DNA dna) {
		return branchMoreeLees(cell, numBranch, dna);
	}
}
