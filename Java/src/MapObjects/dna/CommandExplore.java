package MapObjects.dna;

import MapObjects.AliveCell;

public abstract class CommandExplore extends CommandDNA {
	
	protected CommandExplore() {this(1);};
	protected CommandExplore(int countParams) {super(countParams);};
	
	@Override
	protected int perform(AliveCell cell) {
		return explore(cell);
	}
	protected abstract int explore(AliveCell cell);

	public boolean isDoing() {return false;};
}
