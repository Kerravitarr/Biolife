package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.CellObject.OBJECT.ENEMY;
import static MapObjects.CellObject.OBJECT.FRIEND;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.OWALL;
import static MapObjects.CellObject.OBJECT.WALL;
import main.Configurations;

/**
 * Меняет клетку и её цель. 
 * Клетка как-бы просачивается на место цели
 * @author Kerravitarr
 *
 */
public class Swap extends CommandDoInterupted {
	/**Цена энергии на ход больше, так как мы не просто двигаемся, а должны ещё подвинуть и соседа*/
	protected final int HP_COST = 2;

	public Swap(boolean isA) {
		super(isA, 1);
		setInterrupt(isA, WALL,OWALL);
	}

	@Override
	protected void doing(AliveCell cell) {
		var dir = param(cell, 0, isAbolute);
		var obj = cell.see(dir);
		switch (obj) {
			case WALL,OWALL -> 	cell.getDna().interrupt(cell, obj.nextCMD);
			case CLEAN,POISON,NOT_POISON -> {
				if (cell.move(dir))
					cell.addHealth(-HP_COST); // бот теряет на этом энергию
				else
					cell.getDna().interrupt(cell, obj.nextCMD);
			}
			case ORGANIC,FRIEND,ENEMY,BOT -> {
				cell.addHealth(-HP_COST); // бот теряет на этом энергию
				Configurations.world.swap(cell, cell.getPos().next(dir));
			}
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
}
