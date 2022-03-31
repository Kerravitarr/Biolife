package MapObjects.dna;

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
			case CLEAN:
				Point point = nextPoint(cell,direction);
		        birth(cell,point,childCMD);
		        cell.setFriend((AliveCell) Configurations.world.get(point));
			break;
			case NOT_POISON:
			case POISON:
				point = nextPoint(cell,direction);
				if(birth(cell,point,childCMD))
					cell.setFriend((AliveCell) Configurations.world.get(point));
            break;
			case ENEMY:
			case FRIEND:
			case ORGANIC:
			case WALL:
				cell.getDna().interrupt(cell, see.nextCMD);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
}
