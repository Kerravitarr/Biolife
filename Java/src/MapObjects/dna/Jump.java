package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Является безусловным переходом на следующую команду
 * @author Kerravitarr
 *
 */
public class Jump extends CommandDNA {
	/**На сколько прыгать будем*/
	public final int JAMP;

	public Jump(int j) {super(0,0);JAMP = j;}

	@Override
	protected int perform(AliveCell cell) {
		return JAMP;
	}
	@Override
	public boolean isDoing() {return false;};	
	@Override
	public String value(AliveCell cell, DNA dna) {
        return Configurations.getProperty(Jump.class,isFullMod() ? "value.L" : "value.S",dna.normalization(dna.getPC() + JAMP));
	}
}
