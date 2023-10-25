package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import static MapObjects.CellObject.OBJECT.OWALL;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;
import MapObjects.CellObject;

/**
 * Безвозмездно отдаёт четверть от своего ХП соседу
 * Туда-же уходят минералы
 * @author Kerravitarr
 *
 */
public class Give extends CommandDoInterupted {
	
	/**Абсолютные координаты или относительные*/
	private final boolean isAbolute;

	public Give() {this(true);};

	protected Give(boolean isA) {
		super(isA, 1);
		isAbolute = isA;
		setInterrupt(isA, CellObject.OBJECT.BANE, ORGANIC,  WALL, CLEAN, OWALL);
	}

	@Override
	protected void doing(AliveCell cell) {
		give(cell,param(cell, 0, isAbolute));
	}
	/**
	 * Непосредственно отдать
	 * @param cell - кто даёт
	 * @param direction - в каком направлении цель искать
	 */
	protected void give(AliveCell cell,DIRECTION direction) {
		var see = cell.see(direction);
		switch (see.groupLeader) {
			case ALIVE -> {
				Point point = nextPoint(cell,direction);
				final var target = (AliveCell.AliveCellI) Configurations.world.get(point);
				var hlt0 = cell.getHealth();  // бот отдает четверть своей энергии
				var hlt = hlt0 / 4;
				cell.color(AliveCell.ACTION.GIVE,hlt);
				if(target instanceof AliveCell ac) ac.color(AliveCell.ACTION.RECEIVE,hlt);
				cell.addHealth(-hlt);
				target.addHealth(hlt);

				var min0 = cell.getMineral();  // бот отдает четверть своих минералов
				if (min0 > 3) {                 // только если их у него не меньше 4
					long min = min0 / 4;
					cell.color(AliveCell.ACTION.GIVE,min);
					if(target instanceof AliveCell ac) ac.color(AliveCell.ACTION.RECEIVE,min);
					cell.addMineral(- min);
					target.addMineral(min);
				}
			}
			case BANE, ORGANIC, WALL, CLEAN, OWALL -> cell.getDna().interrupt(cell, see);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
	
}
