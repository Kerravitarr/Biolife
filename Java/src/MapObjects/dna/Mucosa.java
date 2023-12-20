package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Создаёт слизь вокруг клетки, чтобы к ней нельзя было пристать
 */
public class Mucosa extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 1;
	/**Тип команды*/
	private final boolean isUp;
	
	protected Mucosa(boolean isA) {super(isA ? "Add" : "Sub");isUp = isA;}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		cell.setMucosa(isUp ? cell.getMucosa() + 1 : 0);
	}
	
	@Override
	public String value(AliveCell cell) {
        return Configurations.getProperty(Mucosa.class,isFullMod() ? "value.L" : "value.S",HP_COST,isUp ? cell.getMucosa() + 1 : 0);
	}
}
