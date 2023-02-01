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
	
	private final MessageFormat valueFormat = new MessageFormat("A{0} PC += {1}");

	/**
	 * Cнициализирует класс исследования
	 * @param countBranch - число возможных ответов функции
	 */
	protected CommandExplore(int countBranch) {this(0,countBranch);}
	/**
	 * Инициализирует класс исследователя
	 * @param countParams - число параметров у функции
	 * @param countBranch - число возможных ветвей функции
	 */
	protected CommandExplore(int countParams, int countBranch) {super(countParams, countBranch);}
	protected CommandExplore(boolean isAbsolute,int countParams, int countBranch) {super(isAbsolute,countParams, countBranch);}

	
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
	public String value(AliveCell cell, DNA dna) {
		var ofset = explore(cell);
        return valueFormat.format(ofset,(dna.get(dna.getPC() + 1 + getCountParams(), ofset)) % dna.size);
	}
}
