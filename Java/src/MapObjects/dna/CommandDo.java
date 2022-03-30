package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Абстрактный класс для всех команд действий 
 * @author Kerravitarr
 *
 */
public abstract class CommandDo extends CommandDNA {

	protected CommandDo() {this(0);};
	protected CommandDo(int countParams) {super(countParams);}
	@Override
	protected int perform(AliveCell cell) {
		doing(cell);
		return 0;
	}

	protected abstract void doing(AliveCell cell);

	public boolean isDoing() {return true;};

}
