package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;

/**
 * Проверят как много солнца, можем ли мы получтить хотя бы что ни будь из него
 * @author Kerravitarr
 *
 */
public class HowMuchSun extends CommandExplore {

	protected HowMuchSun() {super(1, 2);}

	@Override
	protected int explore(AliveCell cell) {
		var param = param(cell, 0,Configurations.ADD_SUN_POWER + Configurations.BASE_SUN_POWER);
		return cell.sunAround() >= param ? 0 : 1;
	}
	
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0,Configurations.ADD_SUN_POWER + Configurations.BASE_SUN_POWER));
	}
	
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return branchMoreeLees(cell,numBranch,dna);
	};
}
