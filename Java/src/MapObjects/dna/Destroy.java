package MapObjects.dna;

import MapObjects.AliveCell;

public class Destroy extends CommandDo {

	@Override
	protected void doing(AliveCell cell) {
		cell.setHealth(-cell.getHealth());
	}
}
