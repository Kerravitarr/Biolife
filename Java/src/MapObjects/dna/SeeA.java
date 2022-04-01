package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Point.DIRECTION;

/**
 * Посмотреть, что там в указанном направлении
 * @author Kerravitarr
 *
 */
public class SeeA extends CommandExplore {

	public SeeA() {this("O_O А","Смотреть А");};
	protected SeeA(String shotName, String longName) {super(shotName, longName,1,OBJECT.size()-1);}

	@Override
	protected int explore(AliveCell cell) {
		return cell.see(DIRECTION.toEnum(param(cell,0, DIRECTION.size()))).nextCMD;
	}
	public String getParam(AliveCell cellObject, int numParam, int value) {return absoluteDirection(value);};
}
