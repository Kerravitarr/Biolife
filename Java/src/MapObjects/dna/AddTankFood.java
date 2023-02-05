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
	private static final int HP_COST = 1;
	/**Максимальный размер желудка*/
	static final int TANK_SIZE = 10 * AliveCell.MAX_HP;
	private static final MyMessageFormat valueFormat = new MyMessageFormat("HP -= {0} 🛢 = {1}");

	public AddTankFood() {super(1);}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		//Сколько хотим запсти
		var val = param(cell, 0, cell.specMax(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION));
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
		val = Math.min(val, cell.specMax(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION) - cell.getFoodTank());
		if(val <= 0) return;
		cell.addFoodTank(val);
		cell.addHealth(-val);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		return Integer.toString(param(dna, 0, cell.specMax(10 * AliveCell.MAX_HP,AliveCellProtorype.Specialization.TYPE.ACCUMULATION)));
	};
	
	@Override
	public String value(AliveCell cell) {
		//Сколько хотим запсти
		var val = param(cell, 0, cell.specMax(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION));
		//Сколько можем запасти
		val = (int) Math.min(cell.getHealth() - 2 * AliveCellProtorype.HP_PER_STEP, val);
		//Сколько у нас места осталось
		val = Math.min(val, cell.specMax(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION)- cell.getFoodTank());
		val = Math.max(0, val);
		return valueFormat.format(HP_COST + val, cell.getFoodTank() + val);
	}
}
