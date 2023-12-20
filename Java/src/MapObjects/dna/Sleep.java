package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;

/**
 * Заставляет клетку заснуть. Сон - тратит очень мало энергии на сон
 * @author Kerravitarr
 *
 */
public class Sleep extends CommandDo {

	protected Sleep() {super(1);}

	@Override
	protected void doing(AliveCell cell) {
		cell.setSleep(param(cell, 0));
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
        return Configurations.getProperty(Sleep.class,isFullMod() ? "param.L" : "param.S",param(cell, 0));
	}
}
