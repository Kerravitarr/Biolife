package MapObjects.dna;

import MapObjects.AliveCell;
/**
 * Задаёт философский вопрос - я многоклеточный?
 * @author Kerravitarr
 *
 */
public class IAmMulticellular extends CommandExplore {

	protected IAmMulticellular() {super("⚤","Я многокл?",2);}

	@Override
	protected int explore(AliveCell cell) {
		return cell.getFriends().size() == 0 ? 0 : 1;
	}
}
