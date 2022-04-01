package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;
/**
 * Смотрит, у цели ХП больше параметра или нет
 * @author Kerravitarr
 *
 */
public class HowMuchHPTarget extends CommandExplore {

	public HowMuchHPTarget() {super("O_O ♡∸","ХП у него ск?",1,3);}
	
	@Override
	protected int explore(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		if (see.isBot) {
			Point point = nextPoint(cell,cell.direction);
			AliveCell target = (AliveCell) Configurations.world.get(point);
			return target.getHealth() < param(cell,0, AliveCell.MAX_HP) ? 0 : 1;
		} else {
			return 2;
		}
	}
	public String getParam(AliveCell cell, int numParam, int value) {return String.valueOf(getParam(value,AliveCell.MAX_HP)) + "HP";};
}
