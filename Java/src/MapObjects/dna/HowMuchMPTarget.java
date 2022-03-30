package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;

public class HowMuchMPTarget extends CommandExplore {

	public HowMuchMPTarget() {super(1);}
	
	@Override
	protected int explore(AliveCell cell) {
		OBJECT see = cell.seeA(cell.direction);
		if (see.isBot) {
			Point point = nextPoint(cell,cell.direction);
			AliveCell target = (AliveCell) Configurations.world.get(point);
			return target.getMineral() < param(cell,0, AliveCell.MAX_MP) ? 0 : 1;
		} else {
			return 2;
		}
	}
}
