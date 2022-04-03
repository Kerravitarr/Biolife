package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnAroundA extends CommandDo {

	public TurnAroundA() {this("♲ A","Повернуться A");};
	protected TurnAroundA(String shotName,String longName) {super(1, shotName, longName);}
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = DIRECTION.toEnum(param(cell,0, DIRECTION.size()));
	}
	
	/**Эта команда не занимает времени*/
	public boolean isDoing() {return false;};
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return absoluteDirection(param(dna,0, DIRECTION.size()));}
}
