package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import main.Configurations;

/**
 * Функция уменьшает плотность клетки, заставляя последюю всплывать
 * @author Kerravitarr
 *
 */
public class Buoyancy_UP extends CommandDo {
	/**Цена операции*/
	private final int HP_COST = 1;
	/**На сколько меняется плавучесть*/
	private final int DEL = 1;
	private final MessageFormat valueFormat = new MessageFormat("HP -= {0} W {1}= {2}");
	/**Тип команды*/
	private final boolean isUp;

	public Buoyancy_UP() {this(true);}
	protected Buoyancy_UP(boolean isUp) {super(1);this.isUp = isUp;}

	@Override
	protected void doing(AliveCell cell) {
		var par = param(cell, 0,200) - 100;
		cell.addHealth(-Math.abs(HP_COST * par / 10));//Переводит 1 хп в 0.1 плавучести
		cell.setBuoyancy(cell.getBuoyancy() + (isUp ?  +DEL : -DEL) * par);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		return Integer.toString(param(cell, 0,200) - 100);
	};
	
	public String value(AliveCell cell) {
		var par = param(cell, 0, 200) - 100;
		return valueFormat.format(Math.abs(HP_COST * par / 10), (isUp ? '+' : '-'), DEL * par);
	}
}
