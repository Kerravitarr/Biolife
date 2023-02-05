package MapObjects.dna;

import MapObjects.AliveCell;
/**
 * Проверяет, как много слизи на моей поверхности
 * @author Kerravitarr
 *
 */
public class HMMucosa extends CommandExplore {

	protected HMMucosa() {super(1,2);}

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0);
		return cell.getMucosa() >= param ? 0 : 1;
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0));
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna) {
		return branchMoreeLees(cell, numBranch, dna);
	}
}
