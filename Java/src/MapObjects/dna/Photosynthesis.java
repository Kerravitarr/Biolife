package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;

/**
 * Фотосинтез.
 * Если бот близко к солнышку, то можно получить жизни.
 * При этом, есть специальный флаг - photosynthesisEffect число, меняющееся от 0 до 4, показывает сколько дополнительно очков сможет получить ораганизм за фотосинтез
 * используя минералы
 */
public class Photosynthesis extends CommandDo {

	protected Photosynthesis() {super("☀","Фотосинтез");}

	@Override
	protected void doing(AliveCell cell) {
        //Показывает эффективность нашего фотосинтеза
        double t = (1+cell.photosynthesisEffect) * cell.getMineral() / AliveCell.MAX_MP;
        // формула вычисления энергии
        double hlt = Configurations.sun.getEnergy(cell.getPos()) + t;
        if (hlt > 0) {
        	cell.addHealth(Math.round(hlt));   // прибавляем полученную энергия к энергии бота
        	cell.color(AliveCell.ACTION.EAT_SUN,hlt);
        }
	}
}
