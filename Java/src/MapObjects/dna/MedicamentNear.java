package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;

/**
 * Крутится вокруг бота, выискивая яд который безопасен для клетки
 * @author Kerravitarr
 *
 */
public class MedicamentNear extends EnemyNear {

	public MedicamentNear() {super("🔍 💊","Безопасн. яд рядом?");}
	
	@Override
	protected int explore(AliveCell cell) {
		return search(cell, OBJECT.NOT_POISON);
	}
}
