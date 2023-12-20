package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import Calculations.Point.DIRECTION;

/**
 * Ориентирует клетку вверх.
 * Вообще для этого есть функция TurnAroundA(UP), но зато эта функция не требует доп параметров
 * @author Kerravitarr
 *
 */
public class Align_UP extends CommandDo {

	protected Align_UP() {super();}

	@Override
	protected void doing(AliveCell cell) {
		cell.direction = DIRECTION.UP;
	}
	
	@Override public boolean isDoing() {return false;};
	
	@Override
	public String value(AliveCell cell, DNA dna) {
        return isFullMod() ? 
				Configurations.getProperty(Align_UP.class,"value.L", DIRECTION.UP.toSString()) 
				: Configurations.getProperty(Align_UP.class,"value.S", DIRECTION.UP.toString());
	}
}
