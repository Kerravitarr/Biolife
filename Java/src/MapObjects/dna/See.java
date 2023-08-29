package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;

/**
 * Посмотреть, что там в указанном направлении
 * @author Kerravitarr
 *
 */
public class See extends CommandExplore {

	/**Абсолютные координаты или относительные*/
	private final boolean isAbolute;

	public See(boolean isA) {super(isA, 1,OBJECT.size()-1);isAbolute = isA;}

	@Override
	protected int explore(AliveCell cell) {
		return cell.see(param(cell, 0, isAbolute)).nextCMD;
		
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
	

	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		for(var o : OBJECT.myEnumValues) {
			if(o.nextCMD == numBranch)
				return o.toString();
		}
		return super.getBranch(cell, numBranch, dna);
	}
}
