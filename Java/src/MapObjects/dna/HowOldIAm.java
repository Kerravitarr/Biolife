package MapObjects.dna;

import MapObjects.AliveCell;
/**
 * Проверяет, на сколько бот старый. В сотнях лет!
 * @author Kerravitarr
 *
 */
public class HowOldIAm extends CommandExplore {

	public HowOldIAm() {super(1,2);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0)*100;
		return cell.getAge() >= param ? 0 : 1;
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0) * 100);
	}
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return branchMoreeLees(cell,numBranch,dna);
	};
}
