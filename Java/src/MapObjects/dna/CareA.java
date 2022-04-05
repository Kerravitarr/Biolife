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
public class CareA extends CommandDoInterupted {
	
	public CareA() {this("↹ A", "Поделиться A",true);};

	protected CareA(String shotName, String longName,boolean isAbsolute) {
		super(1, shotName, longName);
		setInterrupt(isAbsolute, NOT_POISON, ORGANIC, POISON, WALL, CLEAN);
	}
	
	@Override
	protected void doing(AliveCell cell) {
		care(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
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
					cell.addHealth(-hlt);
					target.addHealth(hlt);
				}
				if (min0 > min1) {              // если у бота больше минералов, чем у соседа
					long min = (min0 - min1) / 2;   // то распределяем их поровну
					cell.setMineral(min0 - min);
					target.setMineral(min1 + min);
				}
			}
			case NOT_POISON, ORGANIC, POISON, WALL, CLEAN -> cell.getDna().interrupt(cell, see.nextCMD);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return absoluteDirection(param(dna,0, DIRECTION.size()));};
}
