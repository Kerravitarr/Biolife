package MapObjects.dna;

import MapObjects.AliveCell;

public class IAmSurrounded extends CommandExplore {

	@Override
	protected int explore(AliveCell cell) {
		return findEmptyDirection(cell) == null ? 0 : 1;
	}
}
