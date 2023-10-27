package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Проверяет, окружён бот или есть хотя-бы одно сволодное поле
 * @author Kerravitarr
 *
 */
public class IAmSurrounded extends CommandExplore {

	protected IAmSurrounded() {super(2);}

	@Override
	protected int explore(AliveCell cell) {
		return findEmptyDirection(cell) == null ? 0 : 1;
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return numBranch == 0 ? "◯" : "◉";
	};
}
