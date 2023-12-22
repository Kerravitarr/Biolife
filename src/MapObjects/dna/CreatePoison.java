package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.Poison;
import MapObjects.Poison.TYPE;
import Calculations.Point.DIRECTION;
import MapObjects.CellObject;

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
		var energy = cell.specMaxVal(Math.min(Poison.MAX_TOXIC, cell.getPosionPower()), AliveCellProtorype.Specialization.TYPE.FERMENTATION);
		cell.addHealth(-HP_FOR_POISON); 
    	Poison.createPoison(nextPoint(cell,direction), cell.getPosionType(), cell.getStepCount(), energy, stream);
		if(cell.aliveStatus(CellObject.LV_STATUS.GHOST))
			throw new CellObject.CellObjectRemoveException(cell); //Если мы плюнули себе на связь и она нас убила, то мы генерируем исключение тут
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(numParam == 0) {
			var stream = (int) Math.round(Math.exp(param(cell,0, MAX_STREAM)));
			return Configurations.getProperty(CreatePoison.class,isFullMod() ? "param0.L" : "param0.S", stream);
		} else {
			return getDirectionParam(cell, numParam, dna);
		}
	}
	@Override
	public String value(AliveCell cell, DNA dna) {
		var stream = (int) Math.round(Math.exp(param(cell,0, MAX_STREAM)));
		var energy = cell.specMaxVal(Math.min(Poison.MAX_TOXIC, cell.getPosionPower()), AliveCellProtorype.Specialization.TYPE.FERMENTATION);
		var direction = param(dna,cell,1,isAbolute);
		return Configurations.getProperty(CreatePoison.class,isFullMod() ? "value.L" : "value.S",HP_FOR_POISON,stream,energy, nextPoint(cell,direction));
	}
}
