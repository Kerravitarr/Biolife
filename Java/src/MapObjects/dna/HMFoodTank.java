package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
/**
 * Проверяет, есть место в танке с едой или нет
 * @author Kerravitarr
 *
 */
public class HMFoodTank extends CommandExplore {

	protected HMFoodTank() {super(1,2);}

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, 10 * AliveCell.MAX_HP * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION));
		return cell.getFoodTank() >= param ? 0 : 1;
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0, 10 * AliveCell.MAX_HP * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION)));
	}
	public String getBranch(AliveCell cell, int numBranch, DNA dna) {
		return branchMoreeLees(cell, numBranch, dna);
	}
}
