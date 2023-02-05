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
	private static final int HP_COST = 1;
	/**Размер желудка*/
	static final int TANK_SIZE = 10 * AliveCell.MAX_MP;
	
	private static final MyMessageFormat valueFormat = new MyMessageFormat("MP -= {0} 🛢 = {1}");

	public AddTankMineral() {super(1);}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		//Сколько хотим запсти
		var val = param(cell, 0, cell.specMax(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION));
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
		val = (int) Math.min(val, cell.specMax(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION) - cell.getMineralTank());
		if(val <= 0) return;
		cell.addMineralTank(val);
		cell.addMineral(-val);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		return Integer.toString(param(dna, 0, cell.specMax(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION)));
	};
	
	@Override
	public String value(AliveCell cell) {
		//Сколько хотим запсти
		var val = param(cell, 0, cell.specMax(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION));
		//Сколько можем запасти
		val = (int) Math.min(cell.getMineral(), val);
		//Сколько у нас места осталось
		val = Math.min(val, cell.specMax(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION)- cell.getMineralTank());
		val = Math.max(0, val);
		return valueFormat.format(val, cell.getMineralTank() + val);
	}
}
