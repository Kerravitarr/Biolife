package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnAround extends CommandDo {

	public TurnAround(boolean isA) {super(isA,1);}
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = param(cell, 0, isAbolute);
	}
	
	/**Эта команда не занимает времени*/
	@Override
	public boolean isDoing() {return false;};

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return getDirectionParam(cell,numParam,dna);
	}
}
