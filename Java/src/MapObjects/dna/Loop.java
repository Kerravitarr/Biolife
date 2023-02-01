package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Сдвигает программный счётчик не вперёд, как обычно,
 * а назад на заданное число
 * @author Kerravitarr
 *
 */
public class Loop extends CommandDNA {
	
	private final MessageFormat paramFormat = new MessageFormat("{0} ({1})");

	public Loop() {super(1,0);};
	@Override
	protected int perform(AliveCell cell) {
		int count = param(cell,0);
		return -count;
	}
	
	@Override
	public boolean isDoing() {return false;};
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		var val = param(dna, 0);
		var npc = (dna.getPC() + 2 - val) % dna.size;
		if(npc < 0) 
			npc += dna.size;
		return paramFormat.format(val,npc);
	};
	public String value(AliveCell cell, DNA dna) {
		var npc = (dna.getPC() + 2 - param(dna, 0)) % dna.size;
		if (npc < 0)
			npc += dna.size;
		return Integer.toString(npc);
	}
}
