package MapObjects.dna;

import java.awt.Color;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;
import panels.Legend;

/**
 * Вставляет свою программу в код другой клетки
 * Ограничений два:
 * 1. Вставить можно только программу, а ты сам её не выполняешь
 * 2. Нельзя вставить саму функцию. Ибо иначе треш начинается
 * @author Kerravitarr
 *
 */
public class DNAInsert extends CommandDNA {
	/**Цена энергии на ход*/
	private final int HP_COST = 4;

	public DNAInsert() {super(1,0,"ДНК ⊡→⊙","Подменить ДНК"); isInterrupt = true;};
	@Override
	protected int perform(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		int length_DNA = param(cell,0, cell.getDna().size);
		switch (see) {
			case ENEMY:
			case FRIEND:
				cell.addHealth(-HP_COST);
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				if (bot.getDNA_wall() > 0) {
					bot.setDNA_wall(bot.getDNA_wall()-1);
				} else {
					// Встраиваемая комбинация начинается сразу за командой и её параметром
					// Мы не можем встроить команду на встраивание. Вот главная особенность!
					var dna = cell.getDna();
					var PC = dna.getIndex();
					for (int i = 0; i < length_DNA && (dna.get(PC,2 + i)) != (CommandList.block5 + 2); i++)
						bot.DNAupdate(i, dna.get(PC,2 + i));
					bot.setGeneration(bot.getGeneration() + 1);
					bot.evolutionNode = bot.evolutionNode.newNode(bot, cell.getStepCount());
				}
				if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
					cell.color_DO = Color.BLACK;
				break;
			case NOT_POISON:
			case ORGANIC:
			case POISON:
			case WALL:
			case CLEAN:
				cell.getDna().interrupt(cell, see.nextCMD);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + see);
		}
		return length_DNA; // Но этот код не наш, мы его не выполняем!
	}
	
	public boolean isDoing() {return true;};
}
