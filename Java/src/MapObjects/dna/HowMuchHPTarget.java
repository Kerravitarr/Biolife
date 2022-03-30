package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;

public class HowMuchHPTarget extends CommandExplore {

	public HowMuchHPTarget() {super(1);}
	
	@Override
	protected int explore(AliveCell cell) {
		OBJECT see = cell.seeA(cell.direction);
		if (see.isBot) {
			Point point = fromVektor(cell,cell.direction);
			AliveCell target = (AliveCell) Configurations.world.get(point);
			return target.getHealth() < param(cell,0, AliveCell.MAX_HP) ? 0 : 1;
		} else {
			return 2;
		}
	}
}
