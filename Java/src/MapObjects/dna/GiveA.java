package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;


public class GiveA extends CommandDo {
	
	public GiveA() {super(1); isInterrupt = true;};
	@Override
	protected void doing(AliveCell cell) {
		give(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
	}
	
	protected void give(AliveCell cell,DIRECTION direction) {
		var see = cell.seeA(direction);
		switch (see) {
			case ENEMY:
			case FRIEND:{
				Point point = fromVektor(cell,direction);
				AliveCell target = (AliveCell) Configurations.world.get(point);
				var hlt0 = cell.getHealth();  // бот отдает четверть своей энергии
				var hlt = hlt0 / 4;
	            cell.addHealth(-hlt);
	            target.addHealth(hlt);

	            var min0 = cell.getMineral();  // бот отдает четверть своих минералов
		        if (min0 > 3) {                 // только если их у него не меньше 4
		        	long min = min0 / 4;
		            cell.setMineral(min0 - min);
		            target.setMineral(target.getMineral() + min);
		        }
			}return;
			case NOT_POISON:
			case ORGANIC:
			case POISON:
			case WALL:
			case CLEAN:
				cell.getDna().interrupt(cell, see.nextCMD);
			default:
				throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
}
