package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
/**
 * Проверяет, у нас больше минералов чем в параметре или меньше
 * @author Kerravitarr
 *
 */
public class HMMineralTank extends CommandExplore {

	protected HMMineralTank() {super(1,2);}


	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, cell.specMax(AddTankMineral.TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION));
		return cell.getMineralTank() >= param ? 0 : 1;
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0, cell.specMax(AddTankMineral.TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION)));
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna) {
		return branchMoreeLees(cell, numBranch, dna);
	}
}
