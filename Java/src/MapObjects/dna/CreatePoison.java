package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.Poison;
import MapObjects.Poison.TYPE;
import main.Point.DIRECTION;

/**
 * Создаёт капельку яда
 * @author Kerravitarr
 */
public class CreatePoison extends CommandDo {
	/**Столько энергии тратит бот на выделение яда*/
	public static final long HP_FOR_POISON = 20;
	/**Логорифмическая прогрессия тягучести яда*/
	private static final int MAX_STREAM = (int) Math.round(Math.log(Poison.MAX_STREAM));
	
	/**Создаёт яд в МСК*/
	public CreatePoison(boolean isAbsol) {super(isAbsol, 2);}
	@Override
	protected void doing(AliveCell cell) {
		if (cell.getPosionType() != TYPE.UNEQUIPPED) {
			addPosion(cell,param(cell,1,isAbolute));
		}
	}
	/**
	 * Непосредственно рождает капельку яда
	 * @param cell - клетка, которая рождает
	 * @param direction - направление, в котором капелька рождается
	 */
	protected void addPosion(AliveCell cell,DIRECTION direction) {
		var stream = (int) Math.round(Math.exp(param(cell,0, MAX_STREAM)));
		var max = cell.specMax(Poison.MAX_TOXIC, AliveCellProtorype.Specialization.TYPE.FERMENTATION);
		var energy = cell.specNormalize(Math.min(max, cell.getPosionPower()), AliveCellProtorype.Specialization.TYPE.FERMENTATION);
		cell.addHealth(-HP_FOR_POISON); 
    	Poison.createPoison(nextPoint(cell,direction), cell.getPosionType(), cell.getStepCount(), energy, stream);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(numParam == 0) {
			return Integer.toString(param(dna, numParam, MAX_STREAM));
		} else {
			var dir = param(dna,cell,numParam,isAbolute);
			return isFullMod() ? dir.toSString() : dir.toString();
		}
	}
}
