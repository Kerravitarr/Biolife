package MapObjects.dna;

import MapObjects.AliveCell;


public class DNALoop extends CommandDNA {

	public DNALoop() {super(1); isInterrupt = true;};
	@Override
	protected int perform(AliveCell cell) {
		int count = param(cell,0);
		return -count;
	}
	
	public boolean isDoing() {return true;};
}
