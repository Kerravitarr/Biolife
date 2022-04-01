package MapObjects.dna;

import MapObjects.AliveCell;
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

	public String getParam(AliveCell cell, int numParam, int value) {return relativeDirection(cell, value);};
}
