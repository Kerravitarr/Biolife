package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;

/**
 * Посмотреть, что там в указанном направлении
 * 
 * Кое что про функцию see:
* Только для живой клетки не возвращает ALIVE, а возвращает ENEMY или FRIEND
* Только для жиовй клетки не возвращает BANE, а возвращает POISON или NOT_POISON
* 
 * @author Kerravitarr
 *
 */
public class See extends CommandExplore {

	public See(boolean isA) {super(isA, 1,OBJECT.lenght-1 - 2);}

	@Override
	protected int explore(AliveCell cell) {
		var see = cell.see(param(cell, 0, isAbolute)).ordinal();
		if(see >= OBJECT.ALIVE.ordinal())
			see--;
		if(see >= OBJECT.BANE.ordinal())
			see--;
		return see;
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return getDirectionParam(cell,numParam,dna);
	}
	

	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		if(numBranch >= OBJECT.ALIVE.ordinal())
			numBranch++;
		if(numBranch >= OBJECT.BANE.ordinal())
			numBranch++;
		for(var o : OBJECT.values) {
			if(o.ordinal() == numBranch)
				return o.toString();
		}
		return super.getBranch(cell, numBranch, dna);
	}
}
