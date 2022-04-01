package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Посмотреть, что там в указанном направлении
 * @author Kerravitarr
 *
 */
public class SeeR extends SeeA {
	
	public SeeR() {super("O_O O","Смотреть O");};
	@Override
	protected int explore(AliveCell cell) {
		return cell.see(relatively(cell, param(cell,0, DIRECTION.size()))).nextCMD;
	}
	public String getParam(AliveCell cell, int numParam, int value) {return relativeDirection(cell, value);};
}
