package MapObjects.dna;

import java.awt.Color;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;
import panels.Legend;


public class DNABreakNow extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;

	public DNABreakNow() {super(1); isInterrupt = true;};
	@Override
	protected void doing(AliveCell cell) {
		OBJECT see = cell.seeA(cell.direction);
		switch (see) {
			case ENEMY:
			case FRIEND:
				cell.addHealth(-HP_COST); // бот теряет на этом 2 энергии в независимости от результата
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				if (bot.getDNA_wall() > 0) {
					bot.setDNA_wall(bot.getDNA_wall()-1);
				} else {
					int mc = param(cell,0); //Значение гена
					bot.DNAupdate(0, mc);
					bot.setGeneration(bot.getGeneration() + 1);
					bot.evolutionNode = bot.evolutionNode.newNode(bot, cell.getStepCount());
				}
				if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
					cell.color_DO = Color.BLACK;
				break;
			case CLEAN:
			case NOT_POISON:
			case ORGANIC:
			case POISON:
			case WALL:
				cell.getDna().interrupt(cell, see.nextCMD);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
}
