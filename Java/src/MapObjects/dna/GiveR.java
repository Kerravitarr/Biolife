package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Безвозмездно отдаёт четверть от своего ХП соседу
 * Туда-же уходят минералы
 * @author Kerravitarr
 *
 */
public class GiveR extends GiveA {

	public GiveR() {super("➚ O","Отдать O");};
	@Override
	protected void doing(AliveCell cell) {
		give(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
}
