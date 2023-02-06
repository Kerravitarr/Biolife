package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;
/**
 * Смотрит, у цели ХП больше параметра или нет
 * @author Kerravitarr
 *
 */
public class HowMuchTarget extends CommandExplore {
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
	public HowMuchTarget(String name, GetParam iface) {this(name, iface,null);};

	/**
	 * Проверить некоторый параметр с ограничением на максимальное значение
	 * @param name Имя этой функции
	 * @param iface функция, возвращающая значение параметра
	 * @param max_val максимальное значение, которое может принимать параметр
	 */
	public HowMuchTarget(String name, GetParam iface, Integer max_val) {
		super(1, 3);
		MAX_VALUE = max_val;
		PARAM = iface;
	};
	
	@Override
	protected int explore(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		if (see.isBot) {
			Point point = nextPoint(cell,cell.direction);
			AliveCell target = (AliveCell) Configurations.world.get(point);
			int param = MAX_VALUE == null ? param(cell, 0) : param(cell, 0, MAX_VALUE);
			return PARAM.getParam(target) >= param ? 0 : 1;
		} else {
			return 2;
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(MAX_VALUE == null ? param(cell, 0) : param(cell, 0, MAX_VALUE));
	}
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna) {
		return switch (numBranch) {
			case 0 -> "≥П";
			case 1 -> "<П";
			default -> "∅";
		};
	}
}
