package MapObjects.dna;

import Calculations.Configurations;
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
        	int co = param(cell, 0, Specialization.MAX_SPECIALIZATION); //Новое значение специализации
        	int tp = param(cell, 1, Specialization.TYPE.size() - 1); //Какая специализация
			cell.getSpecialization().set(Specialization.TYPE.values[tp],co);
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(cell.getAge() <= MAX_AGE) {
			switch (numParam) {
				case 0 -> {
					final var param = param(cell, 0,Specialization.MAX_SPECIALIZATION);
					return Configurations.getProperty(ChangeSpecialization.class,isFullMod() ? "param0.L" : "param0.S", param);
				}
				case 1 -> {
					var sp = Specialization.TYPE.values[param(dna, 1, Specialization.TYPE.size() - 1)];
					return Configurations.getProperty(ChangeSpecialization.class,isFullMod() ? "param1.L" : "param1.S", isFullMod() ? sp.toString() : sp.toSString());
				}
				default -> {return null;}
			}
		} else {
			return Configurations.getProperty(ChangeSpecialization.class,isFullMod() ? "param.old.L" : "param.old.S");
		}
	}
	@Override
	public String value(AliveCell cell, DNA dna) {
		if (cell.getAge() <= MAX_AGE) {
        	int co = param(dna, 0, Specialization.MAX_SPECIALIZATION);
			var sp = Specialization.TYPE.values[param(dna, 1, Specialization.TYPE.size() - 1)];
			return Configurations.getProperty(ChangeSpecialization.class,isFullMod() ? "value.L" : "value.S",HP_COST,co,isFullMod() ? sp.toString() : sp.toSString());
		} else {
			return Configurations.getProperty(ChangeSpecialization.class,isFullMod() ? "value.old.L" : "value.old.S");
		}
	}
}
