package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;

/**
 * Присасывается, объединясь, с ближайшей клеткой
 * @author Kerravitarr
 *
 */
public class Cling extends CommandDoInterupted {
	/**Цена энергии на ход*/
	private final int HP_COST = 1;
	/**Абсолютные координаты или относительные*/
	private final boolean isAbolute;
	
	/**Присасывается к чему-то относительно МСК*/
	public Cling(boolean isA) {
		super(isA, 1);
		isAbolute = isA;
		setInterrupt(isA, ORGANIC, CLEAN, NOT_POISON, POISON, WALL);
	}
	@Override
	protected void doing(AliveCell cell) {
		cling(cell,param(cell, 0, isAbolute));
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
				var target = (AliveCell) Configurations.world.get(point);
				if(target.getMucosa() == 0)	//Если клетка в слизи, то к ней не присосаться 
					cell.setFriend(target);
			}
			case ORGANIC, CLEAN, NOT_POISON, POISON, WALL, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
}
