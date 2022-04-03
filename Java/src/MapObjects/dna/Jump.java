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
		return param(cell, 0)-2; //+1-параметр, +1 - безусловный сдвиг 
	}
	@Override
	public boolean isDoing() {return false;};
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		StringBuilder sb = new StringBuilder();
		var value = param(dna, 0);
		sb.append(value);
		sb.append(" (");
		sb.append((value+dna.getIndex())%dna.size);
		sb.append(")");
		return sb.toString();
	};
}
