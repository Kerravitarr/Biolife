package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Общий класс для функций исследований.
 * Они отличаются тем, что по факту являются
 * ветвями работы программы.
 * Такие функции на выходе дают число. [Код команды][Параметр][На сколько сместить PC для выполнения]
 * @author Kerravitarr
 *
 */
public abstract class CommandExplore extends CommandDNA {

	protected CommandExplore(String shotName, String longName,int countBranch) {this(shotName, longName,1,countBranch);}
	/**
	 * Инициализирует класс исследователя
	 * @param shotName - короткое имя функции
	 * @param longName - полное имя функции
	 * @param countParams - число параметров у функции
	 * @param countBranch - число возможных ветвей функции
	 */
	protected CommandExplore(String shotName, String longName,int countParams, int countBranch) {super(countParams, countBranch, shotName, longName);}

	
	@Override
	protected int perform(AliveCell cell) {
		return explore(cell);
	}
	/**
	 * Собственно функция исследования
	 * @param cell - клетка, которая исследует
	 * @return какая ветвь выполняется
	 */
	protected abstract int explore(AliveCell cell);
	/**Это явно не функция действия, мы можем сколько угодно рассматривать окружающий мир*/
	public boolean isDoing() {return false;};
}
