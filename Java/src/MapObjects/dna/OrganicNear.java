package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;

/**
 * Крутится вокруг бота, выискивая органику
 * @author Kerravitarr
 *
 */
public class OrganicNear extends EnemyNear {

	public OrganicNear() {super("🔍 🍴","Плоть рядом?");}
	
	@Override
	protected int explore(AliveCell cell) {
		return search(cell, OBJECT.ORGANIC);
	}
}
