package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;

/**
 * Крутится вокруг бота, выискивая друга
 * @author Kerravitarr
 *
 */
public class FriendNear extends EnemyNear {

	public FriendNear() {super("🔍 (♡-_-♡)","Друг рядом?");}
	
	@Override
	protected int explore(AliveCell cell) {
		return search(cell, OBJECT.FRIEND);
	}
}
