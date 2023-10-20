package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;

/**
 * Посмотреть, что там в указанном направлении
 * @author Kerravitarr
 *
 */
public class See extends CommandExplore {

	public See(boolean isA) {super(isA, 1,OBJECT.lenght-1);}

	@Override
	protected int explore(AliveCell cell) {
		return cell.see(param(cell, 0, isAbolute)).ordinal();
		
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
	

	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		for(var o : OBJECT.values) {
			if(o.ordinal() == numBranch)
				return o.toString();
		}
		return super.getBranch(cell, numBranch, dna);
	}
}
