package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.FRIEND;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.ENEMY;
import static MapObjects.CellObject.OBJECT.WALL;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import main.Point.DIRECTION;

/**
 * Клетка должна сделать шаг
 * @author Kerravitarr
 *
 */
public class StepA extends CommandDoInterupted {
	/**Цена энергии на ход*/
	protected final int HP_COST = 1;

	public StepA() {this("🐾 A","Шаг A",true);};

	protected StepA(String shotName, String longName, boolean isAbsolute) {
		super(1, shotName, longName);
		setInterrupt(isAbsolute, WALL,ORGANIC,FRIEND,ENEMY);
	}

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
}
