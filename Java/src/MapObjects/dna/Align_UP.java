package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class Align_UP extends CommandDo {

	@Override
	protected void doing(AliveCell cell) {
		cell.direction = DIRECTION.UP;
	}
	
	/**Эта команда не занимает времени*/
	public boolean isDoing() {return false;};
}
