package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Сдвигает программный счётчик не вперёд, как обычно,
 * а назад на заданное число
 * @author Kerravitarr
 *
 */
public class Loop extends CommandDNA {
	/**На сколько прыгать будем*/
	public final int LOOP;

	public Loop(int l) {super(0,0);LOOP = l;};
	@Override
	protected int perform(AliveCell cell) {
		return -LOOP;
	}
	
	@Override
	public boolean isDoing() {return false;};
	
	@Override
	public String value(AliveCell cell, DNA dna) {
        return Configurations.getProperty(Jump.class,isFullMod() ? "value.L" : "value.S",dna.normalization(dna.getPC() - LOOP));
	}
}
