package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Заставляет клетку заснуть. Сон - тратить всего 1 энергию на ход
 * @author Kerravitarr
 *
 */
public class Sleep extends CommandDo {

	protected Sleep() {super();}

	@Override
	protected void doing(AliveCell cell) {
		cell.setSleep(true);
	}
}
