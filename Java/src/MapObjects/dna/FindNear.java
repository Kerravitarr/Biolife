package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.OBJECT;
import MapObjects.Poison;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Крутится вокруг бота, выискивая свою цель
 * @author Kerravitarr
 *
 */
public class FindNear extends CommandExplore {

	public FindNear() {super(1,2);}
	
	@Override
	protected int explore(AliveCell cell) {
		return search(cell,OBJECT.get(param(cell,0, OBJECT.size() - 1)));
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
		var wtype = Configurations.world.test(point);
		switch (wtype) {
			case BOT -> {	//Конфигурация мира не умеет отличать друзей от врагов
				return switch (type) {
					case BOT -> true;
					case FRIEND -> CellObject.isRelative(cell, Configurations.world.get(point));
					case ENEMY -> !CellObject.isRelative(cell, Configurations.world.get(point));
					case CLEAN, NOT_POISON, ORGANIC, OWALL, POISON, WALL -> false;
				};
			}
			case POISON -> {	//Конфигурация мира не умеет отличать яды от лекарств
				return switch (type) {
					case NOT_POISON->	((Poison) Configurations.world.get(point)).getType() == cell.getPosionType();
					case POISON->		((Poison) Configurations.world.get(point)).getType() != cell.getPosionType();
					case BOT, CLEAN, FRIEND, ORGANIC, OWALL, ENEMY, WALL -> false;
				};
			}
			default -> {return wtype == type;}
		}
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return OBJECT.get(param(cell,0, OBJECT.size() - 1)).toString();
	}
	
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return numBranch == 0 ? "👎" : "👌";
	};
}
