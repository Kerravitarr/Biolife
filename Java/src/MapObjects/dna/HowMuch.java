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
	public interface GetIParam {
		public int getParam(AliveCell cell);
	}
	public interface GetDParam {
		public double getParam(AliveCell cell);
	}
	
	/**Мксимальное значение функции, если такое есть*/
	private final Integer MAX_VALUE;
	/**Функция, которая есть параметр*/
	private final GetIParam PARAM_I;
	private final GetDParam PARAM_D;
	/**Функция, которая есть параметр*/
	private final boolean isTarget;
	
	/**
	 * Проверить некоторый параметр без ограничения по максимальному значению
	 * @param name Имя этой функции
	 * @param isT Функция будет рассматривать только себя или свою цель
	 * @param iface функция, возвращающая значение параметра
	 */
	public HowMuch(String name, boolean isT, GetIParam iface) {this(name,isT, iface,null,null);};
	public HowMuch(String name, GetDParam iface, boolean isT) {this(name,isT, null,iface,null);};

	/**
	 * Проверить некоторый параметр с ограничением на максимальное значение
	 * @param name Имя этой функции
	 * @param isT Функция будет рассматривать только себя или свою цель
	 * @param iface функция, возвращающая значение параметра
	 * @param max_val максимальное значение, которое может принимать параметр
	 */
	public HowMuch(String name, boolean isT, GetIParam iface, Integer max_val){this(name,isT, iface,null,max_val);};
	public HowMuch(String name, GetDParam iface, boolean isT, Integer max_val){this(name,isT, null,iface,max_val);};
	
	private HowMuch(String name, boolean isT, GetIParam iface, GetDParam dface, Integer max_val) {
		super(1, isT ? 3 : 2, isT ? name + "T" : name);
		MAX_VALUE = max_val;
		PARAM_I = iface;
		PARAM_D = dface;
		isTarget = isT;
	};

	@Override
	protected int explore(AliveCell cell) {
		int param = MAX_VALUE == null ? param(cell, 0) : param(cell, 0, MAX_VALUE);
		if(isTarget) {
			if (cell.see(cell.direction).groupLeader == OBJECT.ALIVE) {
				Point point = nextPoint(cell,cell.direction);
				if(PARAM_I == null)
					return PARAM_D.getParam((AliveCell) Configurations.world.get(point)) >= param ? 0 : 1;
				else
					return PARAM_I.getParam((AliveCell) Configurations.world.get(point)) >= param ? 0 : 1;
			} else {
				return 2;
			}
		} else {
			if(PARAM_I == null)
				return PARAM_D.getParam(cell) >= param ? 0 : 1;
			else
				return PARAM_I.getParam(cell) >= param ? 0 : 1;
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(MAX_VALUE == null ? param(cell, 0) : param(cell, 0, MAX_VALUE));
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		if(isTarget) {
			return switch (numBranch) {
				case 0 -> "≥П";
				case 1 -> "<П";
				default -> "∅";
			};
		} else {
			return branchMoreeLees(cell,numBranch,dna);
		}
	};
}
