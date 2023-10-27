package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.WALL;
import static MapObjects.CellObject.OBJECT.OWALL;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.CellObjectRemoveException;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;

/**
 * Толкает объект рядом.
 * Если не толкнуть, то может оттолкнуть себя
 * @author Kerravitarr
 *
 */
public class Pull extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;
	
	/**Толкает объект относительно МСК*/
	public Pull(boolean isA) {
		super(isA, 1);
	}
	@Override
	protected void doing(AliveCell cell) {
		pull(cell,param(cell, 0, isAbolute));
	}
	/**
	 * Непосредственно толкает
	 * @param cell - кто толкает
	 * @param direction - в каком направлении цель
	 */
	protected void pull(AliveCell cell,DIRECTION direction) {
		var see = cell.see(direction);
		cell.addHealth(-HP_COST); // Но немного потратились на это
		switch (see.groupLeader) {
			case BANE, ORGANIC, ALIVE -> {
				Point point = nextPoint(cell,direction);
				CellObject target = Configurations.world.get(point);
				try {
					target.move(direction,1); // Вот мы и толкнули
				}catch (CellObjectRemoveException e) {
					// А она возьми да умри. Вот ржака!
				}
			}
			case WALL, OWALL  -> {
				//Мы не можем толкнуть в стену... Но всё относительно!
				cell.move(direction.inversion(),1);	
			}
			case CLEAN ->{
				//Пустота. Просто так потратили энергию
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
}
