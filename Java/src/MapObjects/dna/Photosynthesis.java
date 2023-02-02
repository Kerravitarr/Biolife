package MapObjects.dna;

import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Фотосинтез.
 * Если бот близко к солнышку, то можно получить жизни.
 * При этом, есть специальный флаг - photosynthesisEffect число, меняющееся от 0 до 4, показывает сколько дополнительно очков сможет получить ораганизм за фотосинтез
 * используя минералы
 */
public class Photosynthesis extends CommandDo {
	
	private final MyMessageFormat valueFormat = new MyMessageFormat("HP += {0, number, #.#}");

	protected Photosynthesis() {super();}

	@Override
	protected void doing(AliveCell cell) {
        var hlt = cell.sunAround();
        if (hlt > 0) {
        	cell.addHealth(Math.round(hlt));   // прибавляем полученную энергия к энергии бота
        	cell.color(AliveCell.ACTION.EAT_SUN,hlt);
        }
	}
	
	protected String value(AliveCell cell) {
        double hlt = cell.sunAround();
		if (hlt > 0)
			return valueFormat.format(hlt);
		else
			return null;
	}
}
