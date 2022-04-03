package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;

/**
 * Крутится вокруг бота, выискивая яд
 * @author Kerravitarr
 *
 */
public class PosionNear extends EnemyNear {

	public PosionNear() {super("🔍 ☣","Яд рядом?");}
	
	@Override
	protected int explore(AliveCell cell) {
		return search(cell, OBJECT.POISON);
	}
}
