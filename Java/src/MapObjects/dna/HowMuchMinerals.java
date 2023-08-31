package MapObjects.dna;

import MapObjects.AliveCell;
import Calculations.Configurations;
/**
 * Проверяет, можем ли мы добывать минералы хоть сколько да ни будь
 * @author Kerravitarr
 *
 */
public class HowMuchMinerals extends CommandExplore {
	
	protected HowMuchMinerals() {super(1,2);}

	@Override
	protected int explore(AliveCell cell) {
		var param = param(cell, 0,Configurations.CONCENTRATION_MINERAL*10);
		return cell.mineralAround() >= param ? 0 : 1;
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0,Configurations.CONCENTRATION_MINERAL*10));
	}
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return branchMoreeLees(cell,numBranch,dna);
	};
}
