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
public class CreatePoisonA extends CommandDo {
	/**Столько энергии тратит бот на выделение яда, причём 2/3 этого числа идут яду, 1/3 сгорает*/
	public static final long HP_FOR_POISON = 20;
	/**Логорифмическая прогрессия колличества яда*/
	private static final int MAX_STREAM = (int) Math.round(Math.log(Poison.MAX_STREAM));
	
	/**Абсолютные координаты или относительные*/
	private final boolean isAbolute;
	
	/**Создаёт яд в МСК*/
	public CreatePoisonA() {this(true);}
	protected CreatePoisonA(boolean isAbsol) {super(2);isAbolute = isAbsol;}
	@Override
	protected void doing(AliveCell cell) {
		if (cell.getPosionType() != TYPE.UNEQUIPPED) {
			if(isAbolute)
				addPosion(cell,DIRECTION.toEnum(param(cell,1, DIRECTION.size())));
			else
				addPosion(cell,relatively(cell,param(cell,0, DIRECTION.size())));
		}
	}
	/**
	 * Непосредственно рождает капельку яда
	 * @param cell - клетка, которая рождает
	 * @param direction - направление, в котором капелька рождается
	 */
	protected void addPosion(AliveCell cell,DIRECTION direction) {
		cell.addHealth(-HP_FOR_POISON);      // бот затрачивает энергию на это, причём только 2/3 идёт на токсин
    	if(cell.getHealth() <= 0)
    		return;

		var stream = (int) Math.round(Math.exp(param(cell,0, MAX_STREAM)));
		var energy =  Math.min(HP_FOR_POISON, cell.getPosionPower()) * cell.get(AliveCellProtorype.Specialization.TYPE.FERMENTATION);
    	Poison.createPoison(nextPoint(cell,direction), cell.getPosionType(), cell.getStepCount(), energy, stream);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(numParam == 0)
			return Integer.toString(param(dna, numParam, MAX_STREAM));
		else
			return isAbolute ? absoluteDirection(param(dna, numParam, DIRECTION.size())) : relativeDirection(cell, param(dna, numParam, DIRECTION.size()));
	}
}
