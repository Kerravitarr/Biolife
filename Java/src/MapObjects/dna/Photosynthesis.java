package MapObjects.dna;

import java.text.MessageFormat;

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

	protected Photosynthesis() {super(Configurations.getProperty(Photosynthesis.class, "Shot"),Configurations.getProperty(Photosynthesis.class, "Long"));}

	@Override
	protected void doing(AliveCell cell) {
        //Показывает эффективность нашего фотосинтеза
        double t = 10 * cell.getMineral() / AliveCell.MAX_MP;
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
        double t = 10 * cell.getMineral() / AliveCell.MAX_MP;
        // формула вычисления энергии
        double hlt = Configurations.sun.getEnergy(cell.getPos()) + t;
        hlt *= cell.get(AliveCellProtorype.Specialization.TYPE.PHOTOSYNTHESIS);
		if (hlt > 0)
			return MessageFormat.format("HP += {0}", hlt);
		else
			return "";
	}
}
