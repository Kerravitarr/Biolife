package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;
import main.Point;


public class DNAWallBreak extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 1;

	public DNAWallBreak() {super(1); isInterrupt = true;};
	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST); // бот теряет на этом 1 энергию
		var see = cell.seeA(cell.direction);
		switch (see) {
			case ENEMY:
			case FRIEND:{
				cell.addHealth(-HP_COST); // На это нужно усилие
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				bot.setDNA_wall(Math.max(0, bot.getDNA_wall() - 2));
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
