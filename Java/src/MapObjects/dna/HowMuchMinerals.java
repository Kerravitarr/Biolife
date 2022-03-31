package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;
/**
 * Проверяет, можем ли мы добывать минералы хоть сколько да ни будь
 * @author Kerravitarr
 *
 */
public class HowMuchMinerals extends CommandExplore {
	
	protected HowMuchMinerals() {super("♢🠑","Есть минералы?",2);}

	@Override
	protected int explore(AliveCell cell) {
		return cell.getPos().getY() >= (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL) ? 0 : 1;
	}
}
