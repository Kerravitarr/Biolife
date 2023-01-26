package MapObjects.dna;

import MapObjects.AliveCell;
/**
 * Задаёт философский вопрос - я многоклеточный?
 * @author Kerravitarr
 *
 */
public class IAmMulticellular extends CommandExplore {

	protected IAmMulticellular() {super(2);}

	@Override
	protected int explore(AliveCell cell) {
		return cell.getFriends().isEmpty() ? 0 : 1;
	}
	
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return numBranch == 0 ? "I" : "░";
	};
}
