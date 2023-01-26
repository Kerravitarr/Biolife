package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import main.Configurations;

/**
 * Фотосинтез.
 * Если бот близко к солнышку, то можно получить жизни.
 * При этом, есть специальный флаг - photosynthesisEffect число, меняющееся от 0 до 4, показывает сколько дополнительно очков сможет получить ораганизм за фотосинтез
 * используя минералы
 */
public class Photosynthesis extends CommandDo {
	
	private final MessageFormat valueFormat = new MessageFormat("HP += {0, number, #.#}");

	protected Photosynthesis() {super();}

	@Override
	protected void doing(AliveCell cell) {
		var eff = cell.get(AliveCellProtorype.Specialization.TYPE.PHOTOSYNTHESIS);
        //Показывает эффективность нашего фотосинтеза
        double t = 5 * eff * cell.getMineral() / AliveCell.MAX_MP;
        // формула вычисления энергии
        double hlt = Configurations.sun.getEnergy(cell.getPos()) + t;
        hlt *= cell.get(AliveCellProtorype.Specialization.TYPE.PHOTOSYNTHESIS);
        if (hlt > 0) {
        	cell.addHealth(Math.round(hlt));   // прибавляем полученную энергия к энергии бота
        	cell.color(AliveCell.ACTION.EAT_SUN,hlt);
        }
	}
	
	protected String value(AliveCell cell) {
        //Показывает эффективность нашего фотосинтеза
        double t = 5 * cell.getMineral() / AliveCell.MAX_MP;
        // формула вычисления энергии
        double hlt = Configurations.sun.getEnergy(cell.getPos()) + t;
        hlt *= cell.get(AliveCellProtorype.Specialization.TYPE.PHOTOSYNTHESIS);
		if (hlt > 0)
			return valueFormat.format(hlt);
		else
			return null;
	}
}
