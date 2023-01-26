package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
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
		var eff = cell.get(AliveCellProtorype.Specialization.TYPE.PHOTOSYNTHESIS);
        double t = 5 * eff * cell.getMineral() / AliveCell.MAX_MP;
		var param = param(cell, 0,Configurations.ADD_SUN_POWER + Configurations.BASE_SUN_POWER);
        double hlt = Configurations.sun.getEnergy(cell.getPos()) + t;
		return (hlt * eff) >= param ? 0 : 1;
	}
	
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0,Configurations.ADD_SUN_POWER + Configurations.BASE_SUN_POWER));
	}
	
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return branchMoreeLees(cell,numBranch,dna);
	};
}
