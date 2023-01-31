package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.ENEMY;
import static MapObjects.CellObject.OBJECT.FRIEND;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.OWALL;
import static MapObjects.CellObject.OBJECT.WALL;

import MapObjects.AliveCell;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Клонируется и присасывается к потомку
 * @author Kerravitarr
 *
 */
public class Clone extends CommandDoInterupted {
	/**Абсолютные координаты или относительные*/
	private final boolean isAbolute;

	public Clone(boolean isA) {
		super(isA, 2);
		isAbolute = isA;
		setInterrupt(isA, ENEMY, FRIEND, ORGANIC, WALL, OWALL);
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
		var childCMD = 1 + 1 + param(cell,1); // Откуда будет выполняться команда ребёнка	
		var see = cell.see(direction);
		switch (see) {
			case CLEAN -> {
				Point point = nextPoint(cell,direction);
				Birth.birth(cell,point,childCMD);
				cell.setFriend((AliveCell) Configurations.world.get(point));
			}
			case NOT_POISON, POISON -> {
				Point point = nextPoint(cell,direction);
				if(Birth.birth(cell,point,childCMD))
					cell.setFriend((AliveCell) Configurations.world.get(point));
			}
			case ENEMY, FRIEND, ORGANIC, WALL, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			case BOT -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(numParam == 0)
			return isFullMod() ? param(dna, cell, numParam, isAbolute).toString() : param(dna, cell, numParam, isAbolute).toSString();
		else
			return "PCc = " + String.valueOf((dna.getIndex() + param(cell,1)) % dna.size);
	}
}
