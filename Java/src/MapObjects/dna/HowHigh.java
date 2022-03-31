package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;

/**
 * Проверяет, мы выше или ниже высоты из параметра
 * @author Kerravitarr
 *
 */
public class HowHigh extends CommandExplore {

	public HowHigh() {super("∸","Какая высота",1,2);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, Configurations.MAP_CELLS.height);
		return cell.getPos().getY() < param ? 0 : 1;
	}
}
