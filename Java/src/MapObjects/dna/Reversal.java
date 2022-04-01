package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Разворачивает клетку на 180 градусов
 * Вообще для этого есть функция TurnAroundR(DOWN), но зато эта функция не требует доп параметров
 * @author Kerravitarr
 *
 */
public class Reversal extends CommandDo {

	protected Reversal() {super("↟","Ориентация вверх");}

	@Override
	protected void doing(AliveCell cell) {
		cell.direction = cell.direction.inversion();
	}
	
	/**Эта команда не занимает времени*/
	public boolean isDoing() {return false;};
}
