package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Просто пропуск хода, при этом не тратя энергии лишней
 * @author Kerravitarr
 *
 */
public class Nop extends CommandDo {
	public Nop() {super();}

	@Override
	protected void doing(AliveCell cell) {}	
}
