package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.dna.CommandDNA.param;
import main.Point.DIRECTION;

/**
 * Кушает клетку, которую выбирает своей жертвой
 * @author Kerravitarr
 *
 */
public class EatR extends EatA {
	
	public EatR() {super("🍴 O","Съесть O");};
	@Override
	protected void doing(AliveCell cell) {
		eat(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return relativeDirection(cell,param(dna,0, DIRECTION.size()));}
	
	
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){
		DIRECTION direction = relatively(cell,param(dna,0, DIRECTION.size()));
		return getInterrupt(cell, dna, direction);
	}
}
