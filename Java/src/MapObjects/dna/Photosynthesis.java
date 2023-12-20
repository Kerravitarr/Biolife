package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Фотосинтез.
 * Если бот близко к солнышку, то можно получить жизни.
 * При этом, есть специальный флаг - photosynthesisEffect число, меняющееся от 0 до 4, показывает сколько дополнительно очков сможет получить ораганизм за фотосинтез
 * используя минералы
 */
public class Photosynthesis extends CommandDo {
	/**Цена энергии на ход. Да, фотосинтез тоже требует энергии!*/
	private final int HP_COST = 2;

	protected Photosynthesis() {super();}

	@Override
	protected void doing(AliveCell cell) {
        var hlt = cell.sunAround();
		if (hlt > 0) {
			if (cell.getMineral() > 0)
				cell.addMineral(-1);
        	cell.addHealth(Math.round(hlt));   // прибавляем полученную энергия к энергии бота
        	cell.color(AliveCell.ACTION.EAT_SUN,hlt);
        }
		cell.addHealth(-HP_COST);
	}
	
	@Override
	protected String value(AliveCell cell) {
        double hlt = cell.sunAround();
        return Configurations.getProperty(Photosynthesis.class,isFullMod() ? "value.L" : "value.S",hlt-HP_COST);
	}
}
