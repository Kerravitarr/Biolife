package MapObjects.dna;

import MapObjects.AliveCell;

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
	
	public Buoyancy_UP() {this("☁","Стать легче");}
	
	protected Buoyancy_UP(String shotName,String longName) {super(1,shotName,longName);}

	@Override
	protected void doing(AliveCell cell) {
		buoyancy(cell,true);
	}
	/**
	 * Функция изменения плавучести
	 * @param cell - клетка, у которой меняем плавучесть
	 * @param isUp - true, значит плотность уменьшается
	 */
	protected void buoyancy(AliveCell cell,boolean isUp) {
		var par = param(cell, 0,200) - 100;
		cell.addHealth(-HP_COST * par / 10);//Переводит 1 хп в 0.1 плавучести
		cell.setBuoyancy(cell.getBuoyancy() + (isUp ?  +DEL : -DEL) * par);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		return Integer.toString(param(cell, 0,200) - 100);
	};
}
