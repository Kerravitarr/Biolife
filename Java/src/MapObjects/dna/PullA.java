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
			case WALL, CLEAN -> cell.getDna().interrupt(cell, see.nextCMD);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return absoluteDirection(param(dna,0, DIRECTION.size()));}
	
	
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){
		DIRECTION direction = DIRECTION.toEnum(param(dna,0, DIRECTION.size()));
		return getInterrupt(cell, dna, direction);
	}
	public int getInterrupt(AliveCell cell, DNA dna,DIRECTION direction){
		var see = cell.see(direction);
		if(see == CellObject.OBJECT.WALL || see == CellObject.OBJECT.CLEAN)
			return see.nextCMD;
		else
			return -1;
	}
}
