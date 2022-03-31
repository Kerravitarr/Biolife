package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Присасывается, объединясь, с ближайшей клеткой
 * @author Kerravitarr
 *
 */
public class ClingA extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 1;

	public ClingA() {this("□∪□ A","Присосаться A");};
	protected ClingA(String shotName, String longName) {super(1,shotName, longName); isInterrupt = true;}
	@Override
	protected void doing(AliveCell cell) {
		cling(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
	}
	/**
	 * Непосредственно присамывание
	 * @param cell - кто присасывается
	 * @param direction - в каком направлении цель
	 */
	protected void cling(AliveCell cell,DIRECTION direction) {	
		cell.addHealth(-HP_COST); // бот теряет на этом 1 энергию
		var see = cell.see(direction);
		switch (see) {
			case ENEMY:
			case FRIEND:{
				//--------- дошли до сюда, значит впереди живой бот -------------------
				Point point = nextPoint(cell,direction);
			    cell.setFriend((AliveCell) Configurations.world.get(point));
			}break;
			case ORGANIC:
			case CLEAN:
			case NOT_POISON:
			case POISON:
			case WALL:
				cell.getDna().interrupt(cell, see.nextCMD);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
}
