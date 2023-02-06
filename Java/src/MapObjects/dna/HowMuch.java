package MapObjects.dna;

import MapObjects.AliveCell;
/**
 * Функция, которая проверяет на допустимость, некоторый параметр клетки
 * @author Kerravitarr
 *
 */
public class HowMuch extends CommandExplore {
	public interface GetParam {
		public int getParam(AliveCell cell);
	}
	
	/**Мксимальное значение функции, если такое есть*/
	private final Integer MAX_VALUE;
	/**Функция, которая есть параметр*/
	private final GetParam PARAM;
	
	/**
	 * Проверить некоторый параметр без ограничения по максимальному значению
	 * @param name Имя этой функции
	 * @param iface функция, возвращающая значение параметра
	 */
	public HowMuch(String name, GetParam iface) {this(name, iface,null);};

	/**
	 * Проверить некоторый параметр с ограничением на максимальное значение
	 * @param name Имя этой функции
	 * @param iface функция, возвращающая значение параметра
	 * @param max_val максимальное значение, которое может принимать параметр
	 */
	public HowMuch(String name, GetParam iface, Integer max_val) {
		super(1, 2);
		MAX_VALUE = max_val;
		PARAM = iface;
	};

	@Override
	protected int explore(AliveCell cell) {
		int param = MAX_VALUE == null ? param(cell, 0) : param(cell, 0, MAX_VALUE);
		return PARAM.getParam(cell) >= param ? 0 : 1;
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(MAX_VALUE == null ? param(cell, 0) : param(cell, 0, MAX_VALUE));
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return branchMoreeLees(cell,numBranch,dna);
	};
}
