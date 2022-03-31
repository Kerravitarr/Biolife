package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Является безусловным переходом на следующую команду
 * @author Kerravitarr
 *
 */
public class Jump extends CommandDNA {

	public Jump() {super(1,0,"PC+=","Сл. Команда");}

	@Override
	protected int perform(AliveCell cell) {
		return param(cell, 0);
	}
	@Override
	public boolean isDoing() {return false;};
	
	public String getParam(int value){return String.valueOf(value);};
}
