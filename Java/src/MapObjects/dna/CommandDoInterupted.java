package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;

/**
 * Абстрактный класс для всех команд действий c прерываниями
 * @author Kerravitarr
 *
 */
public abstract class CommandDoInterupted extends CommandDo {
	private OBJECT[] objectInter = null;
	private boolean isAbsolute;
	private int numParam;

	protected CommandDoInterupted(String shotName,String longName) {this(0,shotName,longName);};
	protected CommandDoInterupted(int countParams,String shotName,String longName) {super(countParams,shotName,longName); isInterrupt = true;}	

	@Override
	public final int getInterrupt(AliveCell cell, DNA dna) {
		if(isAbsolute) return getInterruptA(cell, dna, numParam,objectInter);
		else return getInterruptR(cell, dna, numParam,objectInter);
	}
	/**
	 * Сохраняет параметры перерырвания
	 * @param numParam номер параметра, откуда брать направление
	 * @param isAbsolute абсолютно смотрим или относительно
	 * @param objects цели, которые вызовут прерывание
	 */
	protected void setInterrupt(int numParam, boolean isAbsolute, OBJECT... objects) {
		this.numParam = numParam;
		this.isAbsolute = isAbsolute;
		objectInter = objects;
	}
	/**
	 * Сохраняет параметры перерырвания, в которых параметр указывающий направление - нулевой
	 * @param isAbsolute абсолютно смотрим или относительно
	 * @param objects цели, которые вызовут прерывание
	 */
	protected void setInterrupt(boolean isAbsolute, OBJECT... objects) {setInterrupt(0,isAbsolute,objects);}
	/**
	 * Сохраняет параметры перерырвания, в которых параметр указывающий направление - нулевой
	 * @param isAbsolute абсолютно смотрим или относительно
	 */
	protected void setInterrupt(boolean isAbsolute) {setInterrupt(0,isAbsolute,objectInter);}
}


