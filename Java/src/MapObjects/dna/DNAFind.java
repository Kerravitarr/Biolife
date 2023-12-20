package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import Calculations.Configurations;
import Calculations.Point;
/**
 * Ищет инструкцию ДНК у себя или у соседей
 * @author Kerravitarr
 *
 */
public class DNAFind extends CommandExplore {	
	/**Функция, которая есть параметр*/
	private final boolean isTarget;
	
	public DNAFind( boolean isT) {
		super(1, isT ? 3 : 2, isT ? "T" : "M");
		isTarget = isT;
	};

	@Override
	protected int explore(AliveCell cell) {
		int cmdFind = param(cell,0); // Какой ген мы ищем
		if(isTarget) {
			final var see = cell.see(cell.direction);
			if (see == OBJECT.FRIEND || see == OBJECT.ENEMY) {
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				return DNABreak.findPos(bot.getDna(), cmdFind) == -1 ? 0 : 1;
			} else {
				return 2;
			}
		} else {
			return DNABreak.findPos(cell.getDna(), cmdFind) == -1 ? 0 : 1;
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		 return Configurations.getProperty(DNAFind.class,isFullMod() ? "param.L" : "param.S", CommandList.list[param(dna, numParam)]);
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		if(isTarget) {
			return branchMoreeLeesEmpty(cell, numBranch, dna);
		} else {
			return branchMoreeLees(cell,numBranch,dna);
		}
	};
}
