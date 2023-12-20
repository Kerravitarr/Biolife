package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Общий класс для функций исследований.
 * Они отличаются тем, что по факту являются
 * ветвями работы программы.
 * Такие функции на выходе дают число. [Код команды][Параметр][На сколько сместить PC для выполнения]
 * @author Kerravitarr
 *
 */
public abstract class CommandExplore extends CommandDNA {
	/**Формат текстового описания того, что команда сделает с клеткой*/
	private final MyMessageFormat shortValue = new MyMessageFormat(Configurations.getProperty(CommandExplore.class,"value.S"));
	/**Формат текстового описания того, что команда сделает с клеткой*/
	private final MyMessageFormat longValue = new MyMessageFormat(Configurations.getProperty(CommandExplore.class,"value.L"));

	/**
	 * Cпециализирует класс исследования
	 * @param countBranch - число возможных ответов функции
	 */
	protected CommandExplore(int countBranch) {this(0,countBranch);}
	/**
	 * Инициализирует класс исследователя
	 * @param countParams - число параметров у функции
	 * @param countBranch - число возможных ветвей функции
	 */
	protected CommandExplore(int countParams, int countBranch) {super(countParams, countBranch);}
	protected CommandExplore(int countParams, int countBranch, String propName) {super(countParams, countBranch,propName);}
	protected CommandExplore(boolean isAbsolute,int countParams, int countBranch) {super(isAbsolute,countParams, countBranch);}

	
	@Override
	protected int perform(AliveCell cell) {
		final var branch = explore(cell);
		assert branch >= 0 && branch <= getCountBranch() : "У нас доступно только " + getCountBranch() + " ветвей, тогда как мы решили вызвать ветвь " + branch + ". Мы - " + this;
		final var offset = cell.getDna().get(1 + getCountParams() + branch,false);
		return offset;
	}
	/** Фукнция исследования, которую обязана реализовать каждая команда исследования
	 * @param cell клетка, которая исследует
	 * @return номер ветви, которая будет выполнена
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
	public String value(AliveCell cell, DNA dna) {
		var ofset = explore(cell);
        return (isFullMod() ? longValue : shortValue).format(ofset,branch(dna,ofset));
	}
}
