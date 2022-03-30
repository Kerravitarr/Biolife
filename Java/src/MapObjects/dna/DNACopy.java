package MapObjects.dna;

import java.awt.Color;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;
import panels.Legend;


public class DNACopy extends CommandDNA {
	/**Цена энергии на ход*/
	private final int HP_COST = 8;

	public DNACopy() {super(0); isInterrupt = true;};
	@Override
	protected int perform(AliveCell cell) {
		OBJECT see = cell.seeA(cell.direction);
		switch (see) {
			case ENEMY:
			case FRIEND:
				cell.addHealth(-HP_COST); // бот теряет на этом 2 энергии в независимости от результата
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				var bot_dna = bot.getDna();
				var pc = bot_dna.getIndex();
				for (int i = 0; i < bot_dna.size; i++)
					cell.DNAupdate(i, bot_dna.get(pc,i));
				cell.setGeneration(cell.getGeneration() + 1);
				cell.evolutionNode = cell.evolutionNode.newNode(bot, cell.getStepCount());
				if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
					cell.color_DO = Color.GRAY;
				// Смены команды не будет, ведь мы эту команду перезаписали уже на нужную
				return 0;
			case NOT_POISON:
			case ORGANIC:
			case POISON:
			case WALL:
			case CLEAN:
				cell.getDna().interrupt(cell, see.nextCMD);
				return 1;
			default:
				throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	
	public boolean isDoing() {return true;};
}
