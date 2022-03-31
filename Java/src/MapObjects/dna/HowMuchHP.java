package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Проверяет, у нас больше ХП чем в параметре или меньше
 * @author Kerravitarr
 *
 */
public class HowMuchHP extends CommandExplore {
	
	public HowMuchHP() {super("♡∸","Сколько ХП",1,2);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, AliveCell.MAX_HP);
		return cell.getHealth() < param ? 0 : 1;
	}
}
