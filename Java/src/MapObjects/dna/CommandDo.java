package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Абстрактный класс для всех команд действий 
 * @author Kerravitarr
 *
 */
public abstract class CommandDo extends CommandDNA {

	protected CommandDo(String shotName,String longName) {this(0,shotName,longName);};
	protected CommandDo(int countParams,String shotName,String longName) {super(countParams,0,shotName,longName);}
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

}
