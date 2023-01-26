package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;
import panels.Legend;

/**
 * Команда ломает заменяяет один из будущих генов цели
 * На тот, что хочет.
 * Для ограничения введено важное условие
 * Работает только против того, на кого смотришь!
 * @author Kerravitarr
 *
 */
public class DNABreakNext extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;
	/**Ломает ДНК того, на кого смотит на определённый*/
	public DNABreakNext() {super(2,"ГЕН Х","Подменить ген"); isInterrupt = true;};
	@Override
	protected void doing(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		switch (see) {
			case ENEMY, FRIEND -> {
				cell.addHealth(-HP_COST); // бот теряет на этом 2 энергии в независимости от результата
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				if (bot.getDNA_wall() > 0) {
					bot.setDNA_wall(bot.getDNA_wall()-1);
				} else {
					int ma = param(cell,0); // Индекс гена
					int mc = param(cell,1); // Его значение
					bot.DNAupdate(ma, mc);
					bot.setGeneration(bot.getGeneration() + 1);
					bot.evolutionNode = bot.evolutionNode.newNode(bot, cell.getStepCount());
				}
				if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
					cell.color(AliveCell.ACTION.BREAK_DNA,10);
			}
			case CLEAN, NOT_POISON, ORGANIC, POISON, WALL, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(numParam == 0){
			StringBuilder sb = new StringBuilder();
			var param = param(dna,0);
			sb.append("PC t+=");
			sb.append(param);
			return sb.toString();
		}else {
			StringBuilder sb = new StringBuilder();
			var param = param(dna,1);
			sb.append(param);
			sb.append(" (");
			sb.append(CommandList.list[param]);
			sb.append(")");
			return sb.toString();
		}
	}
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){
		var see = cell.see(cell.direction);
		if (see == CLEAN || see == NOT_POISON || see == ORGANIC || see == POISON || see == WALL)
			return see.nextCMD;
		else
			return -1;
	}
}
