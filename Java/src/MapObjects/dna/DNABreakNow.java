package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;

import java.awt.Color;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;
import panels.Legend;

/**
 * Команда ломает заменяяет ген, который будет выполняться в ход цели
 * На тот, что хочет.
 * Для ограничения введено важное условие
 * Работает только против того, на кого смотришь!
 * @author Kerravitarr
 *
 */
public class DNABreakNow extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;
	/**Подменяет команду у того, на кого смотрит на параметр*/
	public DNABreakNow() {super(1,"ДНК Х","Подменить команду"); isInterrupt = true;};
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
					int mc = param(cell,0); //Значение гена
					bot.DNAupdate(0, mc);
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
		StringBuilder sb = new StringBuilder();
		var param = param(dna,0);
		sb.append(param);
		sb.append(" (");
		sb.append(CommandList.list[param]);
		sb.append(")");
		return sb.toString();
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
