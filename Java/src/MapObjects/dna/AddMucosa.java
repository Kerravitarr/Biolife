package MapObjects.dna;

import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Создаёт слизь вокруг клетки, чтобы к ней нельзя было пристать
 */
public class AddMucosa extends CommandDo {
	/**Цена энергии на ход*/
	private final int HP_COST = 1;
	
	private static final MyMessageFormat valueFormat = new MyMessageFormat("HP -= {0} 🌢 = {1}");
	
	protected AddMucosa() {super();}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		cell.setMucosa(cell.getMucosa() + 1);
	}
	
	@Override
	public String value(AliveCell cell) {
		return valueFormat.format(HP_COST, cell.getMucosa() + 1);
	}
}
