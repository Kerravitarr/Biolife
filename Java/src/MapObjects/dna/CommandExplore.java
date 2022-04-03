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

	/**
	 * Cнициализирует класс исследования
	 * @param shotName - короткое имя функции
	 * @param longName - полное имя функции
	 * @param countBranch - число возможных ответов функции
	 */
	protected CommandExplore(String shotName, String longName,int countBranch) {this(shotName, longName,0,countBranch);}
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
	@Override
	public boolean isDoing() {return false;};
	/**
	 * Для всех команд открытия разрешено так делать. А знаете почему? Потому что команды открытия не меняют внутренее состояние!
	 * Команда действие может сдвинуть клетку или заставить её что ни будь сделать, а это лишь изучит окружающий мир
	 */
	@Override
	public String toString(AliveCell cell, DNA dna) {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString(cell,dna));
		sb.append(" = A");
		sb.append(explore(cell));
		return sb.toString();
	}
}
