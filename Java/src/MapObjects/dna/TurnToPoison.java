package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnToPoison extends TurnToEnemy {

	public TurnToPoison() {super("♲ ☣","Повер. к яду");};
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = search(cell,CellObject.OBJECT.POISON);
	}
}
