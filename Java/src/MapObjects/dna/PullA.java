package MapObjects.dna;

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
public class PullA extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;
	
	public PullA() {this("↭ A","Толкнуть A");};
	protected PullA(String shotName, String longName) {super(1,shotName, longName); isInterrupt = true;}
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
			case NOT_POISON:
			case POISON:{
				cell.addHealth(-HP_COST); // Но немного потратились на это
				Point point = nextPoint(cell,direction);
				CellObject target = Configurations.world.get(point);
				try {
					target.moveD(direction); // Вот мы и толкнули
				}catch (CellObjectRemoveException e) {
					// А она возьми да умри. Вот ржака!
				}
			}return;
			case ORGANIC:
			case ENEMY:
			case FRIEND:{
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
			}return;
			case WALL:
			case CLEAN:
				cell.getDna().interrupt(cell, see.nextCMD);
			return;
			default:
				throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	public String getParam(AliveCell cellObject, int numParam, int value) {return absoluteDirection(value);};
}
