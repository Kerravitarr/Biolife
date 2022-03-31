package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Сдвигает программный счётчик не вперёд, как обычно,
 * а назад на заданное число
 * @author Kerravitarr
 *
 */
public class DNALoop extends CommandDNA {

	public DNALoop() {super(1,0,"⮍","Цикл"); isInterrupt = true;};
	@Override
	protected int perform(AliveCell cell) {
		int count = param(cell,0);
		return -count;
	}
	
	public boolean isDoing() {return true;};
}
