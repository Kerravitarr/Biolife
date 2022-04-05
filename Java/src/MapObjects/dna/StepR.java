package MapObjects.dna;

import MapObjects.AliveCell;
import main.Point.DIRECTION;

/**
 * Клетка должна сделать шаг, но теперь направление вычисляется относительно
 * направления, куда клетка глядит
 * @author Kerravitarr
 *
 */
public class StepR extends StepA {
	
	public StepR() {super("🐾 O","Шаг O",false);};
	@Override
	protected void doing(AliveCell cell) {
		DIRECTION dir = relatively(cell, param(cell,0, DIRECTION.size()));
		step(cell,dir);
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return relativeDirection(cell,param(dna,0, DIRECTION.size()));}
}
