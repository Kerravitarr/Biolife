package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.ENEMY;
import static MapObjects.CellObject.OBJECT.FRIEND;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Клонируется и присасывается к потомку
 * @author Kerravitarr
 *
 */
public class CloneA extends Birth {

	public CloneA() {this("♡∪□ А","Клон и присос А");};

	protected CloneA(String shotName,String longName) {super(2,shotName,longName); isInterrupt = true;};
	@Override
	protected void doing(AliveCell cell) {
		clone(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
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
			return absoluteDirection(param(dna, 0, DIRECTION.size()));
		else
			return "ci = " + String.valueOf((dna.getIndex() + param(cell,1)) % dna.size);
	}
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){return getInterrupt(cell, dna, true);}
	protected int getInterrupt(AliveCell cell, DNA dna, boolean isA){
		if(isA)
			return getInterruptA(cell, dna, 0,ENEMY, FRIEND, ORGANIC, WALL);
		else
			return getInterruptR(cell, dna, 0,ENEMY, FRIEND, ORGANIC, WALL);
	}
}
