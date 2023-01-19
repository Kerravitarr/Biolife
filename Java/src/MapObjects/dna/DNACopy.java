package MapObjects.dna;

import java.awt.Color;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import main.Configurations;
import main.Point;
import panels.Legend;

/**
 * Забирает в свой код чужое ДНК.
 * Копирует прям полностью, всё что есть у цели, прям 1 к 1
 * @author Kerravitarr
 *
 */
public class DNACopy extends CommandDNA {
	/**Цена энергии на ход*/
	private final int HP_COST = 8;
	/**Забирает себе часть ДНК цели*/
	public DNACopy() {super(0,0,"ДНК ⊡←⊙","Забрать ДНК"); isInterrupt = true;};
	@Override
	protected int perform(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		switch (see) {
			case ENEMY, FRIEND -> {
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
					cell.color(AliveCell.ACTION.BREAK_DNA,10);
				// Смены команды не будет, ведь мы эту команду перезаписали уже на нужную. Поэтому двигаем PC на шаг назад
				return -1;
			}
			case NOT_POISON, ORGANIC, POISON, WALL, CLEAN, OWALL -> {
				cell.getDna().interrupt(cell, see.nextCMD);
				return 0;
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	
	public boolean isDoing() {return true;};
	
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){
		var see = cell.see(cell.direction);
		if (see == NOT_POISON || see == ORGANIC || see == POISON || see == POISON || see == WALL || see == CLEAN)
			return see.nextCMD;
		else
			return -1;
	}
}
