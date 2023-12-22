package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.OWALL;
import static MapObjects.CellObject.OBJECT.WALL;

import MapObjects.AliveCell;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;
import MapObjects.CellObject;

/**
 * Клонируется и присасывается к потомку
 * @author Kerravitarr
 *
 */
public class Clone extends CommandDoInterupted {

	public Clone(boolean isA) {
		super(isA, 2,1,CellObject.OBJECT.ALIVE, ORGANIC, WALL, OWALL);
	};
	@Override
	protected void doing(AliveCell cell) {
		clone(cell,param(cell, 0, isAbolute));
	}
	/**
	 * Непосредственно клонироваться и присосаться
	 * @param cell - кто
	 * @param direction - в какую сторону
	 */
	protected void clone(AliveCell cell,DIRECTION direction) {	
		var childCMD = param(cell,1); // Откуда будет выполняться команда ребёнка	
		var see = cell.see(direction);
		switch (see.groupLeader) {
			case CLEAN -> {
				Point point = nextPoint(cell,direction);
				Birth.birth(cell,point,childCMD);
				cell.setConnect((AliveCell) Configurations.world.get(point));
			}
			case BANE -> {
				Point point = nextPoint(cell,direction);
				if(Birth.birth(cell,point,childCMD))
					cell.setConnect((AliveCell) Configurations.world.get(point));
			}
			case ALIVE, ORGANIC, WALL, OWALL -> cell.getDna().interrupt(cell, see);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if (numParam == 0) {
			return getDirectionParam(cell, 0, dna);
		} else {
			final var param = param(cell, 1);
			return Configurations.getProperty(Birth.class,isFullMod() ? "param.L" : "param.S",(dna.getPC() + param) % dna.size);
		}
	}
}
