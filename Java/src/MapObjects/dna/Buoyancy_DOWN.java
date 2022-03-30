package MapObjects.dna;

import MapObjects.AliveCell;


public class Buoyancy_DOWN extends CommandDo {
	private final int HP_COST = 1;
	private final int DEL = 1;

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);//Переводит 1 хп в 0.1 плавучести
		cell.setBuoyancy(Math.max(-10, cell.getBuoyancy() - DEL));
	}
}
