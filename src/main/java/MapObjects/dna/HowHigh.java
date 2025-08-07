package MapObjects.dna;

import MapObjects.AliveCell;
import Calculations.Configurations;

/**
 * Проверяет, мы выше или ниже высоты из параметра
 * Странная форма. Возможно нужно создать или группу подобных генов или вообще убрать их
 * Ибо в реальности высота определяется давлением. А у нас давления нет. Зато есть круглые миры!
 * Можно сделать удалённость от центра гравитации, хоть какая-то высота. Правда тогда если гравитации нет - будет 0 всегда
 * @author Kerravitarr
 *
 */
public class HowHigh extends CommandExplore {

	public HowHigh() {super(1,2);};

	@Override
	protected int explore(AliveCell cell) {
		int param = param(cell, 0, Configurations.getHeight());
		return cell.getPos().getY() >= param ? 0 : 1;
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(cell, 0, Configurations.getHeight()));
	}
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna) {
		return branchMoreeLees(cell, numBranch, dna);
	}
}
