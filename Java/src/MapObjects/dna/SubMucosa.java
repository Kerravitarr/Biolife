package MapObjects.dna;

import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Убирает всю слизь вокруг клетки
 */
public class SubMucosa extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 1;
	private static final MyMessageFormat valueFormat = new MyMessageFormat("HP -= {0} 🌢 = 0");
	
	protected SubMucosa() {super();}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		cell.setMucosa(0);
	}
	@Override
	public String value(AliveCell cell) {
		return valueFormat.format(HP_COST);
	}
}
