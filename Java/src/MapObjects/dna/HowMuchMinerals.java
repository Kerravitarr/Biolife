package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype.Specialization;
import main.Configurations;
/**
 * Проверяет, можем ли мы добывать минералы хоть сколько да ни будь
 * @author Kerravitarr
 *
 */
public class HowMuchMinerals extends CommandExplore {
	
	protected HowMuchMinerals() {super("♢🠑","Есть минералы?",1,2);}

	@Override
	protected int explore(AliveCell cell) {
		var param = param(cell, 0,Configurations.CONCENTRATION_MINERAL*5);
		double realLv = cell.getPos().getY() - (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL);
		double dist = Configurations.MAP_CELLS.height * (1 - Configurations.LEVEL_MINERAL);
		return Configurations.CONCENTRATION_MINERAL * (realLv / dist) * 10 * cell.get(Specialization.TYPE.MINERALIZATION) >= param ? 0 : 1;
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0,Configurations.CONCENTRATION_MINERAL*5)+"мп");
	}
}
