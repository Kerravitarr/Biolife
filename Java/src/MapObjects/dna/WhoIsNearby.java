package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import Calculations.Point;
import Calculations.Point.DIRECTION;
/**
 * Ищет первый попавшийся объект
 * @author Kerravitarr
 *
 */
public class WhoIsNearby extends CommandExplore {

	/**
	 * Ищет первый попавшийся объект
	 */
	public WhoIsNearby() {super(OBJECT.size() - 2);}
	
	@Override
	protected int explore(AliveCell cell) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
			if(i == 0 || i == 4) {
				var ret = see(cell,i);
				if(ret != -1)
					return ret;
			} else {
				int dirI = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
				var ret = see(cell,dirI);
				if(ret != -1)
					return ret;
				ret = see(cell,-dirI);
				if(ret != -1)
					return ret;
			}
		}
		return OBJECT.size() - 2;
	}
	
	private int see(AliveCell cell, int dirNum) {
		var dir = relatively(cell, DIRECTION.toEnum(dirNum));
		var see = cell.see(dir);
		if (see != OBJECT.CLEAN && see != OBJECT.WALL) {
			var ret = see.nextCMD;
			if (ret > OBJECT.CLEAN.nextCMD && ret > OBJECT.WALL.nextCMD)
				ret -= 2;
			else if (ret > OBJECT.CLEAN.nextCMD)
				ret--;
			else if (ret > OBJECT.WALL.nextCMD)
				ret--;
			return ret;
		} else {
			return -1;
		}
	}
	
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		for(var o : OBJECT.myEnumValues) {
			if(o == OBJECT.CLEAN || o == OBJECT.WALL)
			if(o.nextCMD == numBranch)
				return o.toString();
		}
		return "∅";
	}
}
