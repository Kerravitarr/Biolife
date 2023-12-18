package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import Calculations.Configurations;
import Calculations.Point;
/**
 * Функция, которая проверяет на допустимость, некоторый параметр клетки
 * @author Kerravitarr
 *
 */
public class HowMuch extends CommandExplore {
	public interface GetParam {
		public Number getParam(AliveCell cell);
	}
	
	/**Мксимальное значение функции, если такое есть*/
	private final Double MAX_VALUE;
	/**Функция, которая есть параметр*/
	private final GetParam PARAM;
	/**Функция, которая есть параметр*/
	private final boolean isTarget;
	
	/**
	 * Проверить некоторый параметр без ограничения по максимальному значению
	 * @param name Имя этой функции
	 * @param isT Функция будет рассматривать только себя или свою цель
	 * @param iface функция, возвращающая значение параметра
	 */
	public HowMuch(String name, boolean isT, GetParam iface) {this(name,isT, iface,null);};

	/**
	 * Проверить некоторый параметр с ограничением на максимальное значение
	 * @param name Имя этой функции
	 * @param isT Функция будет рассматривать только себя или свою цель
	 * @param iface функция, возвращающая значение параметра
	 * @param max_val максимальное значение, которое может принимать параметр
	 */
	public HowMuch(String name, boolean isT, GetParam iface, Number max_val) {
		super(1, isT ? 3 : 2, isT ? name + "T" : name);
		MAX_VALUE = max_val == null ? null : max_val.doubleValue();
		PARAM = iface;
		isTarget = isT;
	};

	@Override
	protected int explore(AliveCell cell) {
		int param = MAX_VALUE == null ? param(cell, 0) : param(cell, 0, MAX_VALUE);
		if(isTarget) {
			final var see = cell.see(cell.direction);
			if (see == OBJECT.FRIEND || see == OBJECT.ENEMY) {
				Point point = nextPoint(cell,cell.direction);
				return PARAM.getParam((AliveCell) Configurations.world.get(point)).doubleValue() >= param ? 0 : 1;
			} else {
				return 2;
			}
		} else {
			return PARAM.getParam(cell).doubleValue() >= param ? 0 : 1;
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(MAX_VALUE == null ? param(cell, 0) : param(cell, 0, MAX_VALUE));
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return isTarget ? branchMoreeLeesEmpty(cell,numBranch,dna) : branchMoreeLees(cell,numBranch,dna);
	};
}
