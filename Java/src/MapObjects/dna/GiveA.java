package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Безвозмездно отдаёт четверть от своего ХП соседу
 * Туда-же уходят минералы
 * @author Kerravitarr
 *
 */
public class GiveA extends CommandDo {

	public GiveA() {this("➚ A","Отдать A");};
	protected GiveA(String shotName, String longName) {super(1,shotName, longName); isInterrupt = true;}

	@Override
	protected void doing(AliveCell cell) {
		give(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
	}
	/**
	 * Непосредственно отдать
	 * @param cell - кто даёт
	 * @param direction - в каком направлении цель искать
	 */
	protected void give(AliveCell cell,DIRECTION direction) {
		var see = cell.see(direction);
		switch (see) {
			case ENEMY, FRIEND -> {
				Point point = nextPoint(cell,direction);
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
			}
			case NOT_POISON, ORGANIC, POISON, WALL, CLEAN -> cell.getDna().interrupt(cell, see.nextCMD);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return absoluteDirection(param(dna,0, DIRECTION.size()));}
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){return getInterrupt(cell, dna, true);}
	protected int getInterrupt(AliveCell cell, DNA dna, boolean isA){
		if(isA)
			return getInterruptA(cell, dna, 0,NOT_POISON, ORGANIC, POISON, WALL, CLEAN);
		else
			return getInterruptR(cell, dna, 0,NOT_POISON, ORGANIC, POISON, WALL, CLEAN);
	}
}
