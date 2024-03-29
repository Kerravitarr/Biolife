package MapObjects.dna;

import MapObjects.AliveCell;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Делится с окружающими.
 * Если у нашего соседа здоровья меньше, то мы ему отдаём всё, чтобы свести разницу к нулю.
 * Если у нашего соседа минералов меньше, то идея та-же
 * @author Kerravitarr
 *
 */
public class Care extends CommandDoInterupted {
	/**Абсолютные координаты или относительные*/
	private final boolean isAbolute;
	
	public Care(boolean isA) {
		super(1);
		isAbolute = isA;
		setInterrupt(isA, NOT_POISON, ORGANIC, POISON, WALL, CLEAN);
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
		switch (see) {
			case ENEMY, FRIEND -> {
				Point point = nextPoint(cell,direction);
				AliveCell target = (AliveCell) Configurations.world.get(point);
				var hlt0 = cell.getHealth();         // определим количество энергии и минералов
				var hlt1 = target.getHealth();  // у бота и его соседа
				var min0 = cell.getMineral();
				var min1 = target.getMineral();
				if (hlt0 > hlt1) {              // если у бота больше энергии, чем у соседа
					double hlt = (hlt0 - hlt1) / 2;   // то распределяем энергию поровну
					cell.color(AliveCell.ACTION.GIVE,hlt);
					cell.addHealth(-hlt);
					target.color(AliveCell.ACTION.RECEIVE,hlt);
					target.addHealth(hlt);
				}
				if (min0 > min1) {              // если у бота больше минералов, чем у соседа
					long min = (min0 - min1) / 2;   // то распределяем их поровну
					cell.color(AliveCell.ACTION.GIVE,min);
					cell.setMineral(min0 - min);
					target.setMineral(min1 + min);
					target.color(AliveCell.ACTION.RECEIVE,min);
				}
			}
			case NOT_POISON, ORGANIC, POISON, WALL, CLEAN, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			case BOT -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
}
