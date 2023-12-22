package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.OBJECT;
import Utils.MyMessageFormat;
import Calculations.Point.DIRECTION;

/**
 * Повернуться в сторону к определённому объекту
 * @author Kerravitarr
 *
 */
public class TurnTo extends CommandDo {

	public TurnTo() {super(1);};
	@Override
	protected void doing(AliveCell cell) {
		cell.direction = search(cell,OBJECT.values[param(cell,0, OBJECT.lenght - 1)]);
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
	protected static DIRECTION search(AliveCell cell, CellObject.OBJECT type) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
			if(i == 0 || i == 4) {
				var rDir = relatively(cell,DIRECTION.toEnum(i));
				if (FindNear.test(cell,nextPoint(cell,rDir),type))
					return rDir;
			} else {
				int dir = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
				var rDir = relatively(cell,DIRECTION.toEnum(dir));
				if (FindNear.test(cell,nextPoint(cell, rDir),type))
					return rDir;
				rDir = relatively(cell,DIRECTION.toEnum(-dir));
				if (FindNear.test(cell,nextPoint(cell, rDir),type))
					return rDir;
			}
		}
		return cell.direction;
	}

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return OBJECT.values[param(dna,0, OBJECT.lenght - 1)].toString();
	}
	
	@Override
	public String value(AliveCell cell, DNA dna) {
		final var d = search(cell,OBJECT.values[param(dna,0, OBJECT.lenght - 1)]);
        return isFullMod() ? 
				Configurations.getProperty(Align_UP.class,"value.L", d.toSString()) 
				: Configurations.getProperty(Align_UP.class,"value.S", d.toString());
	}
}
