package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Сдвигает программный счётчик не вперёд, как обычно,
 * а назад на заданное число
 * @author Kerravitarr
 *
 */
public class Loop extends CommandDNA {

	public Loop() {super(1,0,"⮍","Цикл");};
	@Override
	protected int perform(AliveCell cell) {
		int count = param(cell,0);
		return -count;
	}
	
	@Override
	public boolean isDoing() {return false;};
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		StringBuilder sb = new StringBuilder();
		var val = param(dna,0);
		sb.append(val);
		sb.append(" (");
		val = (dna.getIndex()+2-val)%dna.size;
		if(val < 0) 
			val += dna.size;
		sb.append(val);
		sb.append(")");
		return sb.toString();
	};
}
