package MapObjects.dna;

import Calculations.Point;
import Calculations.Point.DIRECTION;
import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.OBJECT;
import static MapObjects.dna.CommandDNA.param;

/**
 * Абстрактный класс для всех команд действий c прерываниями
 * Это не обёртка над класом команд-действий, а просто набор вспомогательных функций
 * @author Kerravitarr
 *
 */
public abstract class CommandDoInterupted extends CommandDo {
	private OBJECT[] objectInter = null;
	private boolean isAbsolute;
	/**Номер параметра, откуда брать прерывание. Если по этому параметру встретится прерывание - то мы его исполним*/
	private int numParam;
	/**
	 * Создаёт команду ДНК с прерыванием. У команды только 1 параметр и прерывание относится именно к нему
	 * @param isAbsolute абсолютная команда или относительная?
	 * @param objects прерывания, которые мы будем обрабатывать
	 */
	protected CommandDoInterupted(boolean isAbsolute, OBJECT... objects) {
		this(isAbsolute,1,0,objects);
	}	
	/**
	 * Создаёт команду ДНК с прерыванием
	 * @param isAbsolute абсолютная команда или относительная?
	 * @param countParams количество параметров у команды
	 * @param paramNumber индекс параметра, на который мы поглядим и если там увидим один из объектов objects, то вызовем прерывание
	 * @param objects прерывания, которые мы будем обрабатывать
	 */
	protected CommandDoInterupted(boolean isAbsolute, int countParams,int paramNumber, OBJECT... objects) {
		super(isAbsolute, countParams);
		numParam = paramNumber;
		objectInter = objects;
		isInterrupt = true;
	}

	@Override
	public int getInterrupt(AliveCell cell, DNA dna) {
		if(isAbsolute) return getInterruptA(cell, dna, numParam,objectInter);
		else return getInterruptR(cell, dna, numParam,objectInter);
	}
	
	/**
	 * Условная затычка прерываниям
	 * @param cell - клетка, по которой сработало прерывание
	 * @param direction - направление на поглядеть
	 * @param targets - объекты, которые мы выискиваем
	 * @return 
	 */
	private int getInterrupt(AliveCell cell,Point.DIRECTION direction,CellObject.OBJECT ... targets){
		var see = cell.see(direction);
		for (CellObject.OBJECT target : targets) {
			if (see == target || see.groupLeader == target) return see.ordinal();
		}
		return -1;
	}
	/**
	 * Условная затычка прерываниям. Ищет по абсолютному направлению
	 * @param cell - клетка, по которой сработало прерывание
	 * @param dna - её ДНК
	 * @param paramNum - номер параметра, в котором записано направление
	 * @param targets - объекты, которые мы выискиваем
	 * @return 
	 */
	private int getInterruptA(AliveCell cell,DNA dna,int paramNum,CellObject.OBJECT ... targets){
		return getInterrupt(cell,Point.DIRECTION.toEnum(param(dna,paramNum, Point.DIRECTION.size())),targets);
	}
	/**
	 * Условная затычка прерываниям. Ищет по относительному направлению
	 * @param cell - клетка, по которой сработало прерывание
	 * @param dna - её ДНК
	 * @param paramNum - номер параметра, в котором записано направление
	 * @param targets - объекты, которые мы выискиваем
	 * @return 
	 */
	private int getInterruptR(AliveCell cell,DNA dna,int paramNum,CellObject.OBJECT ... targets){
		return getInterrupt(cell,relatively(cell,param(dna,paramNum, Point.DIRECTION.size())),targets);
	}
	/**
	 * Превращает относительное направление в абсолютное
	 * @param cell - клетка, с её напралвлением смотерния
	 * @param direction - направление, на которое нужно сдвинуть гляделку
	 * @return 
	 */
	protected static DIRECTION relatively(AliveCell cell,int direction) {
		return cell.direction.next(direction);
	}
}


