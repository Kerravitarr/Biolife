package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Проверяет, окружён бот или есть хотя-бы одно сволодное поле
 * @author Kerravitarr
 *
 */
public class IAmSurrounded extends CommandExplore {

	protected IAmSurrounded() {super("∅","Я окружён?", 2);}

	@Override
	protected int explore(AliveCell cell) {
		return findEmptyDirection(cell) == null ? 0 : 1;
	}
}
