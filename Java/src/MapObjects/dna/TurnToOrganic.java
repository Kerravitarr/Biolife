package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnToOrganic extends TurnToEnemy {

	public TurnToOrganic() {super("♲ 🍴","Повер. к еде");};
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = search(cell,CellObject.OBJECT.ORGANIC);
	}
}
