package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.OWALL;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;

import MapObjects.AliveCell;
import Calculations.Configurations;
import Calculations.Point;
import MapObjects.CellObject;

/**
 * Проламывает защиту ДНК у своей цели
 * @author Kerravitarr
 *
 */
public class DNAWallBreak extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 1;
	/**Ломает ДНК того, на кого смотрит*/
	public DNAWallBreak() {super(); isInterrupt = true;};
	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST); // бот теряет на этом 1 энергию
		var see = cell.see(cell.direction);
		switch (see.groupLeader) {
			case ALIVE -> {
				cell.addHealth(-HP_COST); // На это нужно усилие
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				bot.setDNA_wall(Math.max(0, bot.getDNA_wall() - 2));
			}
			case ORGANIC, CLEAN, BANE, WALL, OWALL -> cell.getDna().interrupt(cell, see);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){
		var see = cell.see(cell.direction);
		if (see == ORGANIC || see == CLEAN || see.groupLeader == CellObject.OBJECT.BANE || see == WALL || see == OWALL)
			return see.ordinal();
		else
			return -1;
	}
}
