package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import static MapObjects.dna.CommandDNA.param;
import Utils.MyMessageFormat;

/**
 * Функция изменяет плотность клетки, заставляя последюю всплывать
 * @author Kerravitarr
 *
 */
public class Buoyancy extends CommandDo {
	/**Цена операции*/
	private final int HP_COST = 1;
	/**На сколько меняется плавучесть*/
	private final int DEL = 1;
	private final MyMessageFormat valueFormat = new MyMessageFormat("HP -= {0} W ={1}{2}");
	/**Тип команды*/
	private final boolean isUp;

	public Buoyancy(boolean isUp) {super(1, isUp ? "UP" : "DOWN");this.isUp = isUp;}

	@Override
	protected void doing(AliveCell cell) {
		var par = param(cell, 0, 100);
		cell.addHealth(-Math.abs(HP_COST * par / 10));//Переводит 1 хп в 0.1 плавучести
		cell.setBuoyancy((isUp ?  +DEL : -DEL) * par);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		final var param = param(cell, 0,100);
		return Configurations.getProperty(Buoyancy.class,isFullMod() ? "param.L" : "param.S", param);
	};
	
	@Override
	public String value(AliveCell cell) {
		var par = param(cell, 0, 100);
		return Configurations.getProperty(Buoyancy.class,isFullMod() ? "value.L" : "value.S", Math.abs(HP_COST * par / 10), (isUp ? '+' : '-'), DEL * par);
	}
}
