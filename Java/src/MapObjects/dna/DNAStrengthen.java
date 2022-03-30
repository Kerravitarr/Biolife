package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Фотосинтез.
 * Если бот близко к солнышку, то можно получить жизни.
 * При этом, есть специальный флаг - photosynthesisEffect число, меняющееся от 0 до 4, показывает сколько дополнительно очков сможет получить ораганизм за фотосинтез
 * используя минералы
 */
public class DNAStrengthen extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		cell.setDNA_wall(cell.getDNA_wall() + 1);
	}
}
