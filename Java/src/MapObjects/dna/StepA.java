package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;


public class StepA extends CommandDo {
	/**Цена энергии на ход*/
	protected final int HP_COST = 1;

	public StepA() {super(1); isInterrupt = true;};
	@Override
	protected void doing(AliveCell cell) {
		step(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
	}
	
	protected void step(AliveCell cell,DIRECTION dir) {
		if (cell.moveA(dir))
			cell.addHealth(-HP_COST); // бот теряет на этом 1 энергию
		else
			cell.getDna().interrupt(cell,cell.seeA(dir).nextCMD);
	}
}
