package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.ENEMY;
import static MapObjects.CellObject.OBJECT.FRIEND;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.WALL;
import static MapObjects.CellObject.OBJECT.OWALL;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

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
		setInterrupt(isA, WALL,ORGANIC,FRIEND,ENEMY,OWALL);
	}

	@Override
	protected void doing(AliveCell cell) {
		step(cell,param(cell, 0, isAbolute));
	}
	
	protected void step(AliveCell cell,DIRECTION dir) {
		if (cell.move(dir))
			cell.addHealth(-HP_COST); // бот теряет на этом 1 энергию
		else
			cell.getDna().interrupt(cell,cell.see(dir).nextCMD);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
}
