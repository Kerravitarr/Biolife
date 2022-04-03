package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
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
		if (cell.move(dir))
			cell.addHealth(-HP_COST); // бот теряет на этом 1 энергию
		else
			cell.getDna().interrupt(cell,cell.see(dir).nextCMD);
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return absoluteDirection(param(dna,0, DIRECTION.size()));}
	
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){return getInterrupt(cell, dna, DIRECTION.toEnum(param(dna,0, DIRECTION.size())));}
	public int getInterrupt(AliveCell cell, DNA dna,DIRECTION direction){
		var see = cell.see(direction);
		if (see == CellObject.OBJECT.CLEAN || see == CellObject.OBJECT.NOT_POISON || see == CellObject.OBJECT.POISON)
			return -1; //Только сюда можно ступнуть
		else
			return see.nextCMD;
	}
}
