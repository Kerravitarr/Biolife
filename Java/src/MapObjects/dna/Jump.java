package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Является безусловным переходом на следующую команду
 * @author Kerravitarr
 *
 */
public class Jump extends CommandDNA {

	private final MessageFormat paramFormat = new MessageFormat("{0} ({1})");

	public Jump() {super(1,0);}

	@Override
	protected int perform(AliveCell cell) {
		return param(cell, 0)-2; //+1-параметр, +1 - безусловный сдвиг 
	}
	@Override
	public boolean isDoing() {return false;};
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		var value = param(dna, numParam);
		return paramFormat.format(value,(value+dna.getPC())%dna.size);
	};
	
	public String value(AliveCell cell, DNA dna) {
        return Integer.toString((param(dna, 0) + dna.getPC()) % dna.size);
	}
}
