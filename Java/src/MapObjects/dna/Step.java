package MapObjects.dna;


import MapObjects.AliveCell;
import Calculations.Point.DIRECTION;

/**
 * Клетка должна сделать шаг
 * @author Kerravitarr
 *
 */
public class Step extends CommandDoInterupted {
	/**Цена энергии на ход*/
	protected final int HP_COST = 1;

	public Step(boolean isA) {
		super(isA, 1);
	}

	@Override
	protected void doing(AliveCell cell) {
		step(cell,param(cell, 0, isAbolute));
	}
	
	protected void step(AliveCell cell,DIRECTION dir) {
		cell.move(dir,1);
		cell.addHealth(-HP_COST); // бот теряет на этом энергию
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
}
