package MapObjects.dna;

import MapObjects.AliveCell;
/**
 * На сколько защищена моя ДНК от вирусного воздействия?
 * @author Kerravitarr
 *
 */
public class HowMuchDW extends CommandExplore {
	
	public HowMuchDW() {super("ДНК ▣","ДНК защищена?",1,2);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0);
		return cell.getDNA_wall() < param ? 0 : 1;
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return String.valueOf(param(dna, 0));}
}
