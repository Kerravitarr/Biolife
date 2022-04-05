package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.CellObject.OBJECT.ENEMY;
import static MapObjects.CellObject.OBJECT.FRIEND;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.WALL;
import main.Point.DIRECTION;

/**
 * Безвозмездно отдаёт четверть от своего ХП соседу
 * Туда-же уходят минералы
 * @author Kerravitarr
 *
 */
public class GiveR extends GiveA {

	public GiveR() {super("➚ O","Отдать O",false);};
	@Override
	protected void doing(AliveCell cell) {
		give(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return relativeDirection(cell,param(dna,0, DIRECTION.size()));}
}
