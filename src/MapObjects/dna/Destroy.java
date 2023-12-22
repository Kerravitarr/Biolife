package MapObjects.dna;

import MapObjects.AliveCell;
/**
 * Уничтожает клетку. Делает количество её жизней отрицательными
 * @author Kerravitarr
 *
 */
public class Destroy extends CommandDo {

	protected Destroy() {super();}

	@Override
	protected void doing(AliveCell cell) {
		cell.bot2Organic();
	}
}
