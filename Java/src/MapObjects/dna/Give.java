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
		setInterrupt(isA, NOT_POISON, ORGANIC, POISON, WALL, CLEAN, OWALL);
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
		switch (see) {
			case ENEMY, FRIEND -> {
				Point point = nextPoint(cell,direction);
				AliveCell target = (AliveCell) Configurations.world.get(point);
				var hlt0 = cell.getHealth();  // бот отдает четверть своей энергии
				var hlt = hlt0 / 4;
				cell.color(AliveCell.ACTION.GIVE,hlt);
				target.color(AliveCell.ACTION.RECEIVE,hlt);
				cell.addHealth(-hlt);
				target.addHealth(hlt);

				var min0 = cell.getMineral();  // бот отдает четверть своих минералов
				if (min0 > 3) {                 // только если их у него не меньше 4
					long min = min0 / 4;
					cell.color(AliveCell.ACTION.GIVE,min);
					target.color(AliveCell.ACTION.RECEIVE,min);
					cell.setMineral(min0 - min);
					target.setMineral(target.getMineral() + min);
				}
			}
			case NOT_POISON, ORGANIC, POISON, WALL, CLEAN, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			case BOT -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
	
}
