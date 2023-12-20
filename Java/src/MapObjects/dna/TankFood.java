package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import Utils.MyMessageFormat;

/**
 * Запасти немного еды в жирки
 * @author Kerravitarr
 *
 */
public class TankFood extends CommandDo {
	/**Цена операции. А вы думали, бесплатно всё будет?*/
	private static final int HP_COST = 1;
	/**Максимальный размер желудка*/
	static final int TANK_SIZE = (int) Math.round(10 * AliveCell.MAX_HP);
	/**Функция добавки или убавки*/
	private final boolean isAdd;

	public TankFood(boolean isA) {super(1, isA ? "Add" : "Sub"); isAdd = isA;}

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
	/**Возвращает объём хранилища
	 * @param cell чьё хранилище смотрим
	 * @return сколько единиц оно может вместить
	 */
	public static int size(AliveCell cell){
		return cell.specMaxVal(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION);
	}
	/**Возвращает объём пустой части хранилища, сколько ещё сюда можно доложить
	 * @param cell чьё хранилище смотрим
	 * @return сколько единиц оно может вместить до полного
	 */
	public static int leftover(AliveCell cell){
		return size(cell) - cell.getFoodTank();
	}
	
	/**
	 * Закладывает еду в жирки
	 * Может положить, если место есть, а может и не положить, если место закончилось
	 * @param cell кто кладёт
	 * @param val сколько класть
	 */
	public static void add(AliveCell cell,int val) {
		if(val <= 0) return;
		//Сколько можем запасти
		val = (int) Math.min(val, cell.getHealth() - 2 * AliveCellProtorype.HP_PER_STEP);
		if(val <= 0) return;
		//Сколько у нас места осталось
		val = Math.min(val, leftover(cell));
		if(val <= 0) return;
		cell.addFoodTank(val);
		cell.addHealth(-val);
	}
	/**
	 * Забирает из желудка столько энергии, сколько получится
	 * @param cell кто
	 * @param val сколько хотим получить
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
		if(isAdd) {
			//Сколько можем запасти
			val = (int) Math.min(cell.getHealth() - 2 * AliveCellProtorype.HP_PER_STEP, val);
			//Сколько у нас места осталось
			val = Math.min(val, cell.specMaxVal(TANK_SIZE,AliveCellProtorype.Specialization.TYPE.ACCUMULATION)- cell.getFoodTank());
			val = Math.max(0, val);
			return Configurations.getProperty(TankFood.class,isFullMod() ? "value.A.L" : "value.A.S",HP_COST + val, val);
		} else {
			//Сколько можем взять
			val = (int) Math.min(val, cell.getFoodTank());
			val = Math.max(0, val);
			return Configurations.getProperty(TankFood.class,isFullMod() ? "value.S.L" : "value.S.S",val - HP_COST, val);
		}
	}
}
