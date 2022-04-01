package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Point.DIRECTION;
/**
 * Ищет первый попавшийся объект по часовой стрелке
 * @author Kerravitarr
 *
 */
public class WhoIsNearby extends CommandExplore {

	public WhoIsNearby() {super("O_O 🔄","Кто рядом?",OBJECT.size());}
	
	@Override
	protected int explore(AliveCell cell) {
		for (int i = 0; i < DIRECTION.size(); i++) {
			var dir = cell.direction.next(i);
			var see = cell.see(dir);
			if(see != OBJECT.CLEAN && see != OBJECT.WALL)
				return see.nextCMD;
		}
		return OBJECT.size();
	}
}
