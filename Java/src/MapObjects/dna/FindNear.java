package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.OBJECT;
import MapObjects.Poison;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;
import static MapObjects.CellObject.OBJECT.ALIVE;

/**
 * Крутится вокруг бота, выискивая свою цель
 * @author Kerravitarr
 *
 */
public class FindNear extends CommandExplore {
	final int COUNT_FIND = OBJECT.lenght - 1; //Кроме

	public FindNear() {super(1,2);}
	
	@Override
	protected int explore(AliveCell cell) {
		return search(cell,OBJECT.values[param(cell,0, COUNT_FIND)]);
	}
	/**
	 * Непосредственно ищет то, что нужно
	 * @param cell кто ищет
	 * @param type что ищет
	 * @return 0, если такого объекта нет рядом и 1, если такой объект есть
	 */
	protected int search(AliveCell cell, OBJECT type) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
			if(i == 0 || i == 4) {
				final var point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(i)));
				if (test(cell,point,type))
					return 1;
			} else {
				final var point1 = nextPoint(cell,relatively(cell,DIRECTION.toEnum(i)));
				if (test(cell,point1,type))
					return 1;
				final var point2 = nextPoint(cell,relatively(cell,DIRECTION.toEnum(-i)));
				if (test(cell,point2,type))
					return 1;
			}
		}
		return 0;
	}
	
	/**
	 * Сравнивает найденный объект с заданным типом
	 * @param cell кто спрашивает
	 * @param point какую точку исследуем
	 * @param type тип, который ожидаем там найти
	 * @return true, если ожидания совпали с реальностью
	 */
	public static boolean test(AliveCell cell, Point point, OBJECT type) {
		var wtype = cell.see(point);
		return wtype == type || wtype.groupLeader == type;
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return OBJECT.values[param(dna,0, COUNT_FIND)].toString();
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return numBranch == 0 ? "👎" : "👌";
	};
}
