package MapObjects.dna;

import MapObjects.AliveCell;


public class Sleep extends CommandDo {

	@Override
	protected void doing(AliveCell cell) {
		cell.setSleep(true);
	}
}
