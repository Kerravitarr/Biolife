package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import static MapObjects.dna.AddTankFood.TANK_SIZE;
import Utils.MyMessageFormat;

/**
 * Перевести жирки в еду
 * @author Kerravitarr
 *
 */
public class SubTankFood extends CommandDo {
	/**Цена операции. А вы думали, бесплатно всё будет?*/
	private final int HP_COST = 1;
	private final MyMessageFormat valueFormat = new MyMessageFormat("HP += {0} 🛢 = {1}");

	public SubTankFood() {super(1);}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		//Сколько хотим взять
		var val = param(cell, 0, TANK_SIZE);
		sub(cell,val);
	}
	/**
	 * Забирает из желудка столько энергии, сколько получится
	 * @param cell кто
	 * @param val сколько
	 */
	public static void sub(AliveCell cell, int val) {
		if(val <= 0) return;
		//Сколько можем взять
		val = Math.min(val, cell.getFoodTank());
		if(val <= 0) return;
		cell.addFoodTank(-val);
		cell.addHealth(val);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		return Integer.toString(param(dna, 0, TANK_SIZE));
	};
	
	@Override
	public String value(AliveCell cell) {
		//Сколько хотим взять
		var val = param(cell, 0, TANK_SIZE);
		//Сколько можем взять
		val = (int) Math.min(val, cell.getFoodTank());
		val = Math.max(0, val);
		return valueFormat.format(val - HP_COST, cell.getFoodTank() - val);
	}
}
