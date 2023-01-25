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
public class CloneA extends Birth {
	/**Абсолютные координаты или относительные*/
	private final boolean isAbolute;
	
	public CloneA() {this(true);};
	protected CloneA(boolean isA) {super(2);isAbolute = isA; isInterrupt = true;};
	@Override
	protected void doing(AliveCell cell) {
		if(isAbolute)
			clone(cell,DIRECTION.toEnum(param(cell,1, DIRECTION.size())));
		else
			clone(cell,relatively(cell,param(cell,0, DIRECTION.size())));
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
				birth(cell,point,childCMD);
				cell.setFriend((AliveCell) Configurations.world.get(point));
			}
			case NOT_POISON, POISON -> {
				Point point = nextPoint(cell,direction);
				if(birth(cell,point,childCMD))
					cell.setFriend((AliveCell) Configurations.world.get(point));
			}
			case ENEMY, FRIEND, ORGANIC, WALL, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(numParam == 0)
			return isAbolute ? absoluteDirection(param(dna, numParam, DIRECTION.size())) : relativeDirection(cell, param(dna, numParam, DIRECTION.size()));
		else
			return "ci = " + String.valueOf((dna.getIndex() + param(cell,1)) % dna.size);
	}
	
	public int getInterrupt(AliveCell cell, DNA dna){
		if(isAbolute)
			return getInterruptA(cell, dna, 0,ENEMY, FRIEND, ORGANIC, WALL, OWALL);
		else
			return getInterruptR(cell, dna, 0,ENEMY, FRIEND, ORGANIC, WALL, OWALL);
	}
}
