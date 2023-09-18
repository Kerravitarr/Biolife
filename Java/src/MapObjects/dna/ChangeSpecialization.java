package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype.Specialization;
import Utils.MyMessageFormat;

/**
 * Смена специализации. Функция доступна только до сотого года жизни!
 * @author Kerravitarr
 *
 */
public class ChangeSpecialization extends CommandDo {
	/**Максимальный возраст для смены специализации*/
	private final int MAX_AGE = 100;
	/**Цена энергии на действие*/
	private final int HP_COST = 1;
	private final MyMessageFormat valueFormat = new MyMessageFormat("HP -= {0, number, #.#}");

	public ChangeSpecialization() {super(2);};
	
	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		if(cell.getAge() <= MAX_AGE) {
        	int co = param(cell, 0, 100); //Новое значение специализации
        	int tp = param(cell, 1, Specialization.TYPE.size() - 1); //Какая специализация
			cell.getSpecialization().set(Specialization.TYPE.values[tp],co);
		}
	}
	@Override
	protected String value(AliveCell cell) {
		if (cell.getAge() <= MAX_AGE) {
			return valueFormat.format(HP_COST);
		} else {
			return valueFormat.format(HP_COST);
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(cell.getAge() <= MAX_AGE) {
			switch (numParam) {
				case 0 -> {return Integer.toString(param(dna, 0, 100));	}
				case 1 -> {
					var sp = Specialization.TYPE.values[param(dna, 1, Specialization.TYPE.size() - 1)];
					return isFullMod() ? sp.toString() : sp.toSString();
				}
				default -> {return null;}
			}
		} else {
			return "";
		}
	}
}
