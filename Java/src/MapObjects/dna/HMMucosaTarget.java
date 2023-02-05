package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;
/**
 * Смотрит, сколько слизи у цели
 * @author Kerravitarr
 *
 */
public class HMMucosaTarget extends CommandExplore {

	public HMMucosaTarget() {super(1,3);}
	
	@Override
	protected int explore(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		if (see.isBot) {
			Point point = nextPoint(cell,cell.direction);
			AliveCell target = (AliveCell) Configurations.world.get(point);
			return target.getMucosa()>= param(cell,0) ? 0 : 1;
		} else {
			return 2;
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(dna, 0));
	}
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna) {
		return switch (numBranch) {
			case 0 -> "≥П";
			case 1 -> "<П";
			default -> "∅";
		};
	}
}
