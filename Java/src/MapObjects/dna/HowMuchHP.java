package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Проверяет, у нас больше ХП чем в параметре или меньше
 * @author Kerravitarr
 *
 */
public class HowMuchHP extends CommandExplore {
	
	public HowMuchHP() {super(1,2);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, AliveCell.MAX_HP);
		return cell.getHealth() >= param ? 0 : 1;
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0, AliveCell.MAX_HP));
	}
	
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return branchMoreeLees(cell,numBranch,dna);
	};
}
