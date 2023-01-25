package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.Poison;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnTo extends CommandDo {

	public TurnTo() {super(1,"♲ → ",Configurations.bundle.getString("DNA.TurnTo"));};
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = search(cell,OBJECT.get(param(cell,0, OBJECT.size() - 1)));
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
				var rDir = relatively(cell,DIRECTION.toEnum(i));
				if (test(cell,nextPoint(cell,rDir),type))
					return rDir;
			} else {
				int dir = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
				var rDir = relatively(cell,DIRECTION.toEnum(dir));
				if (test(cell,nextPoint(cell, rDir),type))
					return rDir;
				rDir = relatively(cell,DIRECTION.toEnum(-dir));
				if (test(cell,nextPoint(cell, rDir),type))
					return rDir;
			}
		}
		return cell.direction;
	}
	/**
	 * Сравнивает найденный объект с заданным типом
	 * @param cell кто спрашивает
	 * @param point какую точку исследуем
	 * @param type тип, который ожидаем там найти
	 * @return true, если ожидания совпали с реальностью
	 */
	private boolean test(AliveCell cell, Point point, OBJECT type) {
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
}
