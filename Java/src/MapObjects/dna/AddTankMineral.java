package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import Utils.MyMessageFormat;

/**
 * Запасти немного минералов в... камни в почках
 * @author Kerravitarr
 *
 */
public class AddTankMineral extends CommandDo {
	/**Цена операции. А вы думали, бесплатно всё будет?*/
	private final int HP_COST = 1;
	private final MyMessageFormat valueFormat = new MyMessageFormat("MP -= {0} 🛢 {1} = {2}");

	public AddTankMineral() {super(1);}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		//Сколько хотим запсти
		var val = param(cell, 0, AliveCell.MAX_MP * A);
		addMineral(cell, val);
	}
	/**
	 * Закладывает минералы в желудок
	 * @param cell кто кладёт
	 * @param val сколько класть
	 */
	public static void addMineral(AliveCell cell,int val) {
		if(val <= 0) return;
		//Сколько можем запасти
		val = (int) Math.min(cell.getMineral(), val);
		if(val <= 0) return;
		//Сколько у нас места осталось
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		val = (int) Math.min(val, AliveCell.MAX_MP * A - cell.getMineralTank());
		if(val <= 0) return;
		cell.addMineralTank(val);
		cell.addMineral(-val);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		return Integer.toString(param(dna, 0, AliveCell.MAX_MP * A));
	};
	
	public String value(AliveCell cell) {
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		//Сколько хотим запсти
		var val = param(cell, 0, AliveCell.MAX_MP * A);
		//Сколько можем запасти
		val = (int) Math.min(cell.getMineral(), val);
		//Сколько у нас места осталось
		val = (int) Math.min(val, AliveCell.MAX_MP * A - cell.getMineralTank());
		val = Math.max(0, val);
		return valueFormat.format(val, cell.getMineralTank() + val);
	}
}
