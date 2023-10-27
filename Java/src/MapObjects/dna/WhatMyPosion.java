package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.Poison;
/**
 * @author Kerravitarr
 *
 */
public class WhatMyPosion extends CommandExplore {
	/**Какой у меня яд?*/
	public WhatMyPosion() {super(0,Poison.TYPE.size() - 1);};

	@Override
	protected int explore(AliveCell cell) {
		return cell.getPosionType().ordinal();
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return Poison.TYPE.vals[numBranch].toString();
	}
}
