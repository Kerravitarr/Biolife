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
	
	protected Buoyancy_UP(String shotName,String longName) {super(shotName,longName);}

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
		cell.addHealth(-HP_COST);//Переводит 1 хп в 0.1 плавучести
		cell.setBuoyancy(cell.getBuoyancy() + (isUp ?  +DEL : -DEL));
	}
}
