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

	public FindNear() {super(1,2);}
	
	@Override
	protected int explore(AliveCell cell) {
		return search(cell,OBJECT.values[param(cell,0, OBJECT.lenght - 1)]);
	}

	protected int search(AliveCell cell, OBJECT type) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
			if(i == 0 || i == 4) {
				Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(i)));
				if (test(cell,point,type))
					return 1;
			} else {
				int dir = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
				Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(dir)));
				if (test(cell,point,type))
					return 1;
				point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(-dir)));
				if (test(cell,point,type))
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
		switch (wtype) {
			case FRIEND, ENEMY -> {
				return type == OBJECT.ALIVE || wtype == type;
			}
			case POISON,NOT_POISON -> {
				return type == OBJECT.BANE || wtype == type;
			}
			default -> {return wtype == type;}
		}
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return OBJECT.values[param(dna,0, OBJECT.lenght - 1)].toString();
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return numBranch == 0 ? "👎" : "👌";
	};
}
