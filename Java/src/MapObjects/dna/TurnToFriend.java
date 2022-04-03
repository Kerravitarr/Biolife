package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnToFriend extends TurnToEnemy {

	public TurnToFriend() {super("♲ (♡-_-♡)","Повер. к другу");};
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = search(cell,CellObject.OBJECT.FRIEND);
	}
}
