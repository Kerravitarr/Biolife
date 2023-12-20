package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import Calculations.Point;

/**
 * Абстрактный класс для всех команд действий 
 * @author Kerravitarr
 *
 */
public abstract class CommandDo extends CommandDNA {

	protected CommandDo() {this(0);};
	protected CommandDo(String propName) {this(0,propName);};
	protected CommandDo(int countParams) {this(null, countParams,null);}
	protected CommandDo(int countParams, String propName) {this(null, countParams, propName);}
	protected CommandDo(Boolean isAbsolute,int countParams) {this(isAbsolute, countParams,null);}
	protected CommandDo(Boolean isAbsolute,int countParams, String propName) {super(isAbsolute, countParams,0,propName);}
	@Override
	protected int perform(AliveCell cell) {
		doing(cell);
		return size();
	}
	/**
	 * Непосредственно те действия, которые выполняет клетка
	 * @param cell - клетка, которая очень уж хочет походить
	 */
	protected abstract void doing(AliveCell cell);

	@Override
	public boolean isDoing() {return true;};
}
