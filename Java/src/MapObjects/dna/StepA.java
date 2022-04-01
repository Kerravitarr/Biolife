package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Клетка должна сделать шаг
 * @author Kerravitarr
 *
 */
public class StepA extends CommandDo {
	/**Цена энергии на ход*/
	protected final int HP_COST = 1;

	public StepA() {this("🐾 A","Шаг A");};
	protected StepA(String shotName,String longName) {super(1, shotName, longName); isInterrupt = true;}
	@Override
	protected void doing(AliveCell cell) {
		step(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
	}
	
	protected void step(AliveCell cell,DIRECTION dir) {
		if (cell.moveA(dir))
			cell.addHealth(-HP_COST); // бот теряет на этом 1 энергию
		else
			cell.getDna().interrupt(cell,cell.see(dir).nextCMD);
	}

	public String getParam(AliveCell cellObject, int numParam, int value) {return absoluteDirection(value);};
}
