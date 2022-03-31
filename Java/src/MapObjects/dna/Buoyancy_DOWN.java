package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Функция уменьшает плотность клетки, заставляя последюю всплывать
 * @author Kerravitarr
 *
 */
public class Buoyancy_DOWN extends Buoyancy_UP {
	
	protected Buoyancy_DOWN() {super("■","Стать тяжелее");}

	@Override
	protected void doing(AliveCell cell) {
		buoyancy(cell,false);
	}
}
