package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import Utils.MyMessageFormat;

/**
 * Запасти немного минералов в виде... камней в почках
 * @author Kerravitarr
 *
 */
public class TankMineral extends CommandDo {
	/**Цена операции. А вы думали, бесплатно всё будет?*/
	private static final int HP_COST = 1;
	/**Размер желудка*/
	static final int TANK_SIZE = (int) Math.round(10 * AliveCell.MAX_MP);
	/**Функция добавки или убавки*/
	private final boolean isAdd;

	public TankMineral(boolean isA) {super(1, isA ? "Add" : "Sub"); isAdd = isA;}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		//Сколько хотим запсти
		var val = param(cell, 0, TANK_SIZE);
		if(isAdd)
			add(cell, val);
		else
			sub(cell, val);
	}
	/**
	 * Закладывает минералы в желудок
	 * @param cell кто кладёт
	 * @param val сколько класть
	 */
	public static void add(AliveCell cell,int val) {
		if(val <= 0) return;
		//Сколько можем запасти
		val = (int) Math.min(cell.getMineral(), val);
		if(val <= 0) return;
		//Сколько у нас места осталось
		val = (int) Math.min(val, cell.specMaxVal(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION) - cell.getMineralTank());
		if(val <= 0) return;
		cell.addMineralTank(val);
		cell.addMineral(-val);
	}
	/**
	 * Забирает из желудка столько минералов, сколько получится
	 * @param cell кто
	 * @param val сколько
	 */
	public static void sub(AliveCell cell, int val) {
		if(val <= 0) return;
		//Сколько можем взять
		val = (int) Math.min(val, cell.getMineralTank());
		if(val <= 0) return;
		cell.addMineralTank(-val);
		cell.addMineral(val);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		return Integer.toString(param(dna, 0, TANK_SIZE));
	};
	
	@Override
	public String value(AliveCell cell) {
		//Сколько хотим взять
		var val = param(cell, 0, TANK_SIZE);
		if(isAdd) {
			//Сколько можем запасти
			val = (int) Math.min(cell.getMineral(), val);
			//Сколько у нас места осталось
			val = Math.min(val, cell.specMaxVal(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION)- cell.getMineralTank());
			val = Math.max(0, val);
			return Configurations.getProperty(TankMineral.class,isFullMod() ? "value.A.L" : "value.A.S",HP_COST , val, val);
		} else {
			//Сколько можем взять
			val = (int) Math.min(val, cell.getMineralTank());
			val = Math.max(0, val);
			return Configurations.getProperty(TankMineral.class,isFullMod() ? "value.S.L" : "value.S.S",HP_COST , val, val);
		}
	}
}
