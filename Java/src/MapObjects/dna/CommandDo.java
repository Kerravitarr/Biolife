package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import main.Point;

/**
 * Абстрактный класс для всех команд действий 
 * @author Kerravitarr
 *
 */
public abstract class CommandDo extends CommandDNA {

	protected CommandDo() {this(0);};
	protected CommandDo(int countParams) {super(countParams,0);}
	protected CommandDo(boolean isAbsolute,int countParams) {super(isAbsolute, countParams,0);}
	@Override
	protected int perform(AliveCell cell) {
		doing(cell);
		return 0;
	}
	/**
	 * Непосредственно те действия, которые выполняет клетка
	 * @param cell - клетка, которая очень уж хочет походить
	 */
	protected abstract void doing(AliveCell cell);

	public boolean isDoing() {return true;};

	/**
	 * Условная затычка прерываниям
	 * @param cell - клетка, по которой сработало прерывание
	 * @param direction - направление на поглядеть
	 * @param targets - объекты, которые мы выискиваем
	 * @return 
	 */
	public int getInterrupt(AliveCell cell,Point.DIRECTION direction,CellObject.OBJECT ... targets){
		var see = cell.see(direction);
		for (CellObject.OBJECT target : targets) {
			if (see == target) return see.nextCMD;
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
	public int getInterruptA(AliveCell cell,DNA dna,int paramNum,CellObject.OBJECT ... targets){
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
	public int getInterruptR(AliveCell cell,DNA dna,int paramNum,CellObject.OBJECT ... targets){
		return getInterrupt(cell,relatively(cell,param(dna,paramNum, Point.DIRECTION.size())),targets);
	}
}
