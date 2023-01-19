package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.Poison.TYPE;
import main.Point.DIRECTION;

/**
 * Создаёт капельку яда, но уже относительно того места куда глядит клетка
 * @author Kerravitarr
 */
public class CreatePoisonR extends CreatePoisonA {

	public CreatePoisonR() {super("☣ О","Пукнуть О");};
	@Override
	protected void doing(AliveCell cell) {
		if (cell.getPosionType() != TYPE.UNEQUIPPED)
			addPosion(cell,relatively(cell,param(cell,0, DIRECTION.size())));
	}
	public String getParam(AliveCell cell, int numParam, int value) {return relativeDirection(cell, value);};
}
