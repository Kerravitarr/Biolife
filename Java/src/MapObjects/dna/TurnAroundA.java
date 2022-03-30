package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class TurnAroundA extends CommandDo {

	public TurnAroundA() {super(1);};
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = DIRECTION.toEnum(param(cell,0, DIRECTION.size()));
	}
	
	/**Эта команда не занимает времени*/
	public boolean isDoing() {return false;};
}
