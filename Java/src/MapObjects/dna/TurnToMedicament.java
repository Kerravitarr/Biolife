package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnToMedicament extends TurnToEnemy {

	public TurnToMedicament() {super("♲ 💊","Повер. к неяду");};
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = search(cell,CellObject.OBJECT.NOT_POISON);
	}
}
