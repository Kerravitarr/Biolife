package MapObjects.dna;

import MapObjects.AliveCell;

public class IAmMulticellular extends CommandExplore {

	@Override
	protected int explore(AliveCell cell) {
		return cell.getFriends().size() == 0 ? 0 : 1;
	}
}
