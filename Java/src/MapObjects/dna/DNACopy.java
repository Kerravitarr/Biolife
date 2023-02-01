package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;

/**
 * Забирает в свой код чужое ДНК.
 * Копирует прям полностью, всё что есть у цели, прям 1 к 1
 * @author Kerravitarr
 *
 */
public class DNACopy extends DNABreak {
	/**Забирает себе часть ДНК цели*/
	public DNACopy() {super(0);};
	@Override
	protected void doing(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		switch (see) {
			case ENEMY, FRIEND -> {
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				bot.addHealth(HP_COST_MANY);	//Так как они поменяны местами, и траты их надо поменять местами!
				cell.addHealth(-HP_COST_MANY);
				breakDNAMany(bot,cell,bot.getDna().getPC(),bot.getDna().size);
			}
			case NOT_POISON, ORGANIC, POISON, WALL, CLEAN, OWALL -> {
				cell.getDna().interrupt(cell, see.nextCMD);
			}
			case BOT -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}

	@Override
	protected int perform(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		doing(cell);
		// Смены команды не будет, ведь мы эту команду перезаписали уже на нужную. Поэтому двигаем PC на шаг назад
		return see == OBJECT.ENEMY || see == OBJECT.FRIEND ? -1 : 0;
	}
}
