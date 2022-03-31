package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Ориентирует клетку вверх.
 * Вообще для этого есть функция TurnAroundA(UP), но зато эта функция не требует доп параметров
 * @author Kerravitarr
 *
 */
public class Align_UP extends CommandDo {

	protected Align_UP() {super("↟","Ориентация вверх");}

	@Override
	protected void doing(AliveCell cell) {
		cell.direction = DIRECTION.UP;
	}
	
	/**Эта команда не занимает времени*/
	public boolean isDoing() {return false;};
}
