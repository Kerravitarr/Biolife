package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.CellObjectRemoveException;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;


public class PullA extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;
	
	public PullA() {super(1); isInterrupt = true;};
	@Override
	protected void doing(AliveCell cell) {
		pull(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
	}
	
	protected void pull(AliveCell cell,DIRECTION direction) {
		var see = cell.seeA(direction);
		switch (see) {
			case NOT_POISON:
			case ORGANIC:
			case POISON:
			case ENEMY:
			case FRIEND:{
				cell.addHealth(-HP_COST); // Но немного потратились на это
				Point point = fromVektor(cell,direction);
				CellObject target = Configurations.world.get(point);
				try {
					target.moveD(direction); // Вот мы и толкнули
				}catch (CellObjectRemoveException e) {
					// А она возьми да умри. Вот ржака!
				}
			}return;
			case WALL:
			case CLEAN:
				cell.getDna().interrupt(cell, see.nextCMD);
			default:
				throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
}
