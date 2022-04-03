package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import static MapObjects.dna.CommandDNA.nextPoint;
import static MapObjects.dna.CommandDNA.relatively;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnToEnemy extends CommandDo {

	public TurnToEnemy() {this("♲ ]:|","Повер. к врагу");};
	protected TurnToEnemy(String shotName,String longName) {super(shotName, longName);}
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = search(cell,CellObject.OBJECT.ENEMY);
	}
	
	/**
	 * Показывает, затрачивает эта команда время или нет
	 * @return false, эта команда не требует времени
	 */
	@Override
	public boolean isDoing() {return false;};

	/**
	 * Возвращает направление на ближайшую цель по усмотрению
	 * @param cell - кого крутим
	 * @param type - что ищем
	 * @return новое направление или то направление, какое и было у клетки до вращения
	 */
	protected DIRECTION search(AliveCell cell, CellObject.OBJECT type) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
			if(i == 0 || i == 4) {
				Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(i)));
				if (Configurations.world.test(point) == type)
					return relatively(cell,DIRECTION.toEnum(i));
			} else {
				int dir = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
				Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(dir)));
				if (Configurations.world.test(point) == type)
					return relatively(cell,DIRECTION.toEnum(dir));
				dir = -dir;
				point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(dir)));
				if (Configurations.world.test(point) == type)
					return relatively(cell,DIRECTION.toEnum(dir));
			}
		}
		return cell.direction;
	}
}
