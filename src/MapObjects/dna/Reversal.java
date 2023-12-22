package MapObjects.dna;

import Calculations.Configurations;
import Calculations.Point;
import MapObjects.AliveCell;

/**
 * Разворачивает клетку на 180 градусов
 * Вообще для этого есть функция TurnAroundR(DOWN), но зато эта функция не требует доп параметров
 * @author Kerravitarr
 *
 */
public class Reversal extends CommandDo {

	protected Reversal() {super();}

	@Override
	protected void doing(AliveCell cell) {
		cell.direction = cell.direction.inversion();
	}
	
	/**Эта команда не занимает времени*/
	@Override
	public boolean isDoing() {return false;};
	
	@Override
	public String value(AliveCell cell, DNA dna) {
		final var d = cell.direction;
        return isFullMod() ? 
				Configurations.getProperty(Align_UP.class,"value.L", d.toSString()) 
				: Configurations.getProperty(Align_UP.class,"value.S", d.toString());
	}
}
