package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;

/**
 * Делится с окружающими.
 * Если у нашего соседа здоровья меньше, то мы ему отдаём всё, чтобы свести разницу к нулю.
 * Если у нашего соседа минералов меньше, то идея та-же
 * @author Kerravitarr
 *
 */
public class Care extends CommandDoInterupted {
	
	public Care(boolean isA) {
		super(isA, NOT_POISON, ORGANIC, POISON, WALL, CLEAN);
	}
	
	@Override
	protected void doing(AliveCell cell) {
		care(cell,param(cell, 0, isAbolute));
	}
	/**
	 * Непосредственно подеться
	 * @param cell - от кого
	 * @param direction - в какую сторону
	 */
	protected void care(AliveCell cell,DIRECTION direction) {
		var see = cell.see(direction);
		switch (see.groupLeader) {
			case ALIVE -> {
				Point point = nextPoint(cell,direction);
				final var target = (AliveCell.AliveCellI) Configurations.world.get(point);
				var hlt0 = cell.getHealth();         // определим количество энергии и минералов
				var hlt1 = target.getHealth();		// у бота и его соседа
				var min0 = cell.getMineral();
				var min1 = target.getMineral();
				if (hlt0 > hlt1) {              // если у бота больше энергии, чем у соседа
					double hlt = (hlt0 - hlt1) / 2;   // то распределяем энергию поровну
					cell.color(AliveCell.ACTION.GIVE,hlt);
					cell.addHealth(-hlt);
					if(target instanceof AliveCell ac) ac.color(AliveCell.ACTION.RECEIVE,hlt);
					target.addHealth(hlt);
				}
				if (min0 > min1) {              // если у бота больше минералов, чем у соседа
					long min = (min0 - min1) / 2;   // то распределяем их поровну
					cell.color(AliveCell.ACTION.GIVE,min);
					cell.addMineral(- min);
					target.addMineral(+ min);
					if(target instanceof AliveCell ac) ac.color(AliveCell.ACTION.RECEIVE,min);
				}
			}
			case BANE, ORGANIC, WALL, CLEAN, OWALL -> cell.getDna().interrupt(cell, see);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return getDirectionParam(cell, numParam, dna);
	}
}
