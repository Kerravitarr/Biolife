package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.WALL;
import static MapObjects.CellObject.OBJECT.OWALL;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.CellObjectRemoveException;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Толкает объект рядом.
 * Если не толкнуть, то может оттолкнуть себя
 * @author Kerravitarr
 *
 */
public class PullA extends CommandDoInterupted {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;
	/**Толкает объект относительно МСК*/
	public PullA() {this("↭ A","Толкнуть A",true);};

	protected PullA(String shotName, String longName, boolean isAbsolute) {
		super(1, shotName, longName);
		setInterrupt(isAbsolute, WALL,OWALL,  CLEAN);
	}
	@Override
	protected void doing(AliveCell cell) {
		pull(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
	}
	/**
	 * Непосредственно толкает
	 * @param cell - кто толкает
	 * @param direction - в каком направлении цель
	 */
	protected void pull(AliveCell cell,DIRECTION direction) {
		var see = cell.see(direction);
		switch (see) {
			case NOT_POISON, POISON -> {
				cell.addHealth(-HP_COST); // Но немного потратились на это
				Point point = nextPoint(cell,direction);
				CellObject target = Configurations.world.get(point);
				try {
					target.moveD(direction); // Вот мы и толкнули
				}catch (CellObjectRemoveException e) {
					// А она возьми да умри. Вот ржака!
				}
			}
			case ORGANIC, ENEMY, FRIEND -> {
				cell.addHealth(-HP_COST); // Но немного потратились на это
				Point point = nextPoint(cell,direction);
				CellObject target = Configurations.world.get(point);
				boolean targetStep = true;
				try {
					targetStep = target.moveD(direction);
				}catch (CellObjectRemoveException e) {
					// А она возьми да умри. Вот ржака!
				}
				if(!targetStep)
					cell.moveD(direction.inversion()); //Мы не смогли толкнуть цель, поэтому отлетаем сами
			}
			case WALL, OWALL -> {
				if(cell.moveD(direction.inversion())) {	//Мы не можем толкнуть в стену... Но всё относительно!
					return;
				} else {
					cell.getDna().interrupt(cell, see.nextCMD);	// Мы прям заперты - проблема
				}
			}
			case CLEAN -> cell.getDna().interrupt(cell, see.nextCMD);
			case BOT -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return absoluteDirection(param(dna,0, DIRECTION.size()));}
}
