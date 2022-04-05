package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Присасывается, объединясь, с ближайшей клеткой
 * @author Kerravitarr
 *
 */
public class ClingA extends CommandDoInterupted {
	/**Цена энергии на ход*/
	private final int HP_COST = 1;

	public ClingA() {this("□∪□ A","Присосаться A",true);};

	protected ClingA(String shotName, String longName, boolean isAbsolute) {
		super(1, shotName, longName);
		setInterrupt(isAbsolute, ORGANIC, CLEAN, NOT_POISON, POISON, WALL);
	}
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
			case ENEMY, FRIEND -> {
				//--------- дошли до сюда, значит впереди живой бот -------------------
				Point point = nextPoint(cell,direction);
				cell.setFriend((AliveCell) Configurations.world.get(point));
			}
			case ORGANIC, CLEAN, NOT_POISON, POISON, WALL -> cell.getDna().interrupt(cell, see.nextCMD);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return absoluteDirection(param(dna,0, DIRECTION.size()));}
}
