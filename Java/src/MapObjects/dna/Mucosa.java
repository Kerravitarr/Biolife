package MapObjects.dna;

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
	
	private static final MyMessageFormat valueFormat = new MyMessageFormat("HP -= {0} 🌢 = {1}");
	
	protected Mucosa(boolean isA) {super(isA ? "Add" : "Sub");isUp = isA;}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		cell.setMucosa(isUp ? cell.getMucosa() + 1 : 0);
	}
	
	@Override
	public String value(AliveCell cell) {
		return valueFormat.format(HP_COST, isUp ? cell.getMucosa() + 1 : 0);
	}
}
