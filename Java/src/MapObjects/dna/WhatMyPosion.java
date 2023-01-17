package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.Poison;
/**
 * @author Kerravitarr
 *
 */
public class WhatMyPosion extends CommandExplore {
	/**Какой у меня яд?*/
	public WhatMyPosion() {super("☣?","Какой яд?",0,Poison.TYPE.size());};

	@Override
	protected int explore(AliveCell cell) {
		return cell.getPosionType().ordinal();
	}
}
