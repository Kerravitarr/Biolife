package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnAroundR extends TurnAroundA {

	public TurnAroundR() {super("♲ О","Повернуться О");};
	
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = relatively(cell, param(cell,0, DIRECTION.size()));
	}
}
