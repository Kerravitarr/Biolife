package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype.Specialization;

/**
 * Смена специализации. Функция доступна только до сотого года жизни!
 * @author Kerravitarr
 *
 */
public class ChangeSpecialization extends CommandDo {

	public ChangeSpecialization() {super(2);};
	
	@Override
	protected void doing(AliveCell cell) {
		if(cell.getAge() <= 100) {
        	int co = param(cell, 0, 100); //Новое значение специализации
        	int tp = param(cell, 1, Specialization.TYPE.size() - 1); //Какая специализация
			cell.getSpecialization().set(Specialization.TYPE.staticValues[tp],co);
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(cell.getAge() <= 100) {
			switch (numParam) {
				case 0 -> {return Integer.toString(param(dna, 0, 100));	}
				case 1 -> {
					var sp = Specialization.TYPE.staticValues[param(dna, 1, Specialization.TYPE.size() - 1)];
					return isFullMod() ? sp.toString() : sp.toSString();
				}
				default -> {return null;}
			}
		} else {
			return "";
		}
	}
}
