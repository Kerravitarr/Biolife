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

	/**Абсолютные координаты или относительные*/
	private final boolean isAbolute;

	public SeeA() {this(true);};
	protected SeeA(boolean isA) {super(1,OBJECT.size()-1);isAbolute = isA;}

	@Override
	protected int explore(AliveCell cell) {
		return cell.see(param(cell, 0, isAbolute)).nextCMD;
		
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, 0, isAbolute);
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
