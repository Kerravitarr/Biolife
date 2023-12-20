package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;

/**
 * Проверяет, окружён бот или есть хотя-бы одно сволодное поле
 * @author Kerravitarr
 *
 */
public class IAmSurrounded extends CommandExplore {

	protected IAmSurrounded() {super(2);}

	@Override
	protected int explore(AliveCell cell) {
		return cell.findEmptyDirection() == null ? 0 : 1;
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return Configurations.getProperty(IAmSurrounded.class,isFullMod() ? "branch"+numBranch+".L" : "branch"+numBranch+".S");
	};
}
