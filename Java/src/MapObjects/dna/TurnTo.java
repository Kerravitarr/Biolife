package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.OBJECT;
import main.Point.DIRECTION;

/**
 * Повернуться в определеённую сторону
 * @author Kerravitarr
 *
 */
public class TurnTo extends CommandDo {
	private final MessageFormat valueFormat = new MessageFormat("D = {0}");

	public TurnTo() {super(1);};
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
		return OBJECT.get(param(cell,0, OBJECT.size() - 1)).toString();
	}
	
	public String value(AliveCell cell, DNA dna) {
		var d = search(cell,OBJECT.get(param(dna,0, OBJECT.size() - 1)));
       return valueFormat.format(isFullMod() ? d.toSString() : d.toString());
	}
}
