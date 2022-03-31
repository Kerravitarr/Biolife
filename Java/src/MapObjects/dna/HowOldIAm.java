package MapObjects.dna;

import MapObjects.AliveCell;
/**
 * Проверяет, на сколько бот старый. В сотнях лет!
 * @author Kerravitarr
 *
 */
public class HowOldIAm extends CommandExplore {

	public HowOldIAm() {super("♔","Сколько лет?",1,2);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0)*100;
		return cell.getAge() < param ? 0 : 1;
	}
}
