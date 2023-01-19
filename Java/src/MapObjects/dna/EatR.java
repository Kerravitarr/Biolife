package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Кушает клетку, которую выбирает своей жертвой
 * @author Kerravitarr
 *
 */
public class EatR extends EatA {
	
	public EatR() {super("🍴 O","Съесть O",false);};
	@Override
	protected void doing(AliveCell cell) {
		eat(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return relativeDirection(cell,param(dna,0, DIRECTION.size()));}
}
