package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import Utils.MyMessageFormat;

/**
 * Запасти немного еды в жирки
 * @author Kerravitarr
 *
 */
public class AddTankFood extends CommandDo {
	/**Цена операции. А вы думали, бесплатно всё будет?*/
	private final int HP_COST = 1;
	private final MyMessageFormat valueFormat = new MyMessageFormat("HP -= {0} 🛢 {1} = {2}");

	public AddTankFood() {super(1);}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		//Сколько хотим запсти
		var val = param(cell, 0, AliveCell.MAX_HP * A);
		addFood(cell, val);
	}
	
	/**
	 * Закладывает еду в желудок
	 * @param cell кто кладёт
	 * @param val сколько класть
	 */
	public static void addFood(AliveCell cell,int val) {
		if(val <= 0) return;
		//Сколько можем запасти
		val = (int) Math.min(cell.getHealth() - 2 * AliveCellProtorype.HP_PER_STEP, val);
		if(val <= 0) return;
		//Сколько у нас места осталось
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		val = (int) Math.min(val, AliveCell.MAX_HP * A - cell.getFoodTank());
		if(val <= 0) return;
		cell.addFoodTank(val);
		cell.addHealth(-val);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		return Integer.toString(param(dna, 0, AliveCell.MAX_HP * A));
	};
	
	public String value(AliveCell cell) {
		var A = 10 * cell.get(AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
		//Сколько хотим запсти
		var val = param(cell, 0, AliveCell.MAX_HP * A);
		//Сколько можем запасти
		val = (int) Math.min(cell.getHealth() - 2 * AliveCellProtorype.HP_PER_STEP, val);
		//Сколько у нас места осталось
		val = (int) Math.min(val, AliveCell.MAX_HP * A - cell.getFoodTank());
		val = Math.max(0, val);
		return valueFormat.format(HP_COST + val, cell.getFoodTank() + val);
	}
}
