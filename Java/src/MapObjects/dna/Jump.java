package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Является безусловным переходом на следующую команду
 * @author Kerravitarr
 *
 */
public class Jump extends CommandDNA {
	/**
	 * Следите за кривостью моих рук!
	 * Я не придумал как ещё адекватно сделать то, что я хочу. Поэтому сделал всё через одно место.
	 * Параметром у этой функции является следующая команда. Но я хз какая она. Просить передавать в 
	 * получение параметра ещё и PC, ну это вообще зашквар. Нужно только тут.
	 * Поэтому идём дорогой труддной, дорогой не прямой.
	 * Вызывается сначала команда написать название параметра. То есть вызывается toString
	 * И после этого для всех и каждого вот в это поле устанавливается указатель на текущую клетку.
	 * Следом вызывается getParam
	 * И теперь мы знаем где находится PC у клетки.
	 * Нет. Ни разу. В режиме симуляции проходит по 300 шагов в минуту, какое знаем?!
	 * Но там и не видно, что мы лажаем. А когда видно? Когда скорость маленькая. А когда скорость маленкая, тогда
	 * шанс, что сдвинется PC у клетки - минимальный. И мы успеем отрисовать как надо.
	 * Тупо? Криво? Ну да, я такой ^_^
	 */
	private static AliveCell nowActiv = null;

	public Jump() {super(1,0,"PC+=","Сл. Команда");}

	@Override
	protected int perform(AliveCell cell) {
		return param(cell, 0);
	}
	@Override
	public boolean isDoing() {return false;};
	
	public String getParam(AliveCell cellObject, int numParam, int value){
		StringBuffer sb = new StringBuffer();
		sb.append(value);
		sb.append(" (");
		sb.append((value+nowActiv.getDna().getIndex())%cellObject.getDna().size);
		sb.append(")");
		return sb.toString();
	};
	
	public String toString(AliveCell cell) {
		nowActiv = cell;
		return super.toString(cell);
	}
}
