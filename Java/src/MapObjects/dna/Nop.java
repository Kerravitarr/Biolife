package MapObjects.dna;

import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Пустая инструкция, ни чего не делающая
 * @author Kerravitarr
 *
 */
public class Nop extends CommandDNA {

	public Nop() {super(0,0);}

	@Override
	protected int perform(AliveCell cell) {
		return 0;
	}
	@Override
	public boolean isDoing() {return false;};
}
