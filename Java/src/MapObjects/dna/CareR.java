package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Делится с окружающими.
 * Если у нашего соседа здоровья меньше, то мы ему отдаём всё, чтобы свести разницу к нулю.
 * Если у нашего соседа минералов меньше, то идея та-же
 * @author Kerravitarr
 *
 */
public class CareR extends CareA {
	
	public CareR() {super("↹ O","Поделиться О",false);};
	@Override
	protected void doing(AliveCell cell) {
		care(cell,relatively(cell, param(cell,0, DIRECTION.size())));
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return relativeDirection(cell,param(dna,0, DIRECTION.size()));}
}
