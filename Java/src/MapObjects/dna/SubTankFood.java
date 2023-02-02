package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import Utils.MyMessageFormat;

/**
 * Перевести жирки в еду
 * @author Kerravitarr
 *
 */
public class SubTankFood extends CommandDo {
	/**Цена операции. А вы думали, бесплатно всё будет?*/
	private final int HP_COST = 1;
	private final MyMessageFormat valueFormat = new MyMessageFormat("HP += {0} 🛢 {1} = {2}");

	public SubTankFood() {super(1);}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		//Сколько хотим взять
		var val = param(cell, 0, AliveCell.MAX_HP * A);
		if(val <= 0) return;
		//Сколько можем взять
		val = (int) Math.min(val, cell.getFoodTank());
		if(val <= 0) return;
		cell.addFoodTank(-val);
		cell.addHealth(val);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		return Integer.toString(param(dna, 0, AliveCell.MAX_HP * A));
	};
	
	public String value(AliveCell cell) {
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		//Сколько хотим взять
		var val = param(cell, 0, AliveCell.MAX_HP * A);
		//Сколько можем взять
		val = (int) Math.min(val, cell.getFoodTank());
		val = Math.max(0, val);
		return valueFormat.format(val - HP_COST, cell.getFoodTank() - val);
	}
}
