package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Заставляет клетку заснуть. Сон - тратит очень мало энергии на сон
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
