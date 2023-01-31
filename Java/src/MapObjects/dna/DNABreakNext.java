package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import main.Configurations;
import main.Point;

/**
 * Команда ломает заменяяет один из будущих генов цели. Следующий. А может через один и т.д.
 * На тот, что хочет.
 * Для ограничения введено важное условие
 * Работает только против того, на кого смотришь!
 * @author Kerravitarr
 *
 */
public class DNABreakNext extends DNABreak {
	
	private final MessageFormat param1Format = new MessageFormat("PC += {0}");
	private final MessageFormat param2Format = new MessageFormat("CMD = {0}");
	/**Ломает ДНК того, на кого смотит на определённый*/
	public DNABreakNext() {super(2);};
	@Override
	protected void doing(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		switch (see) {
			case ENEMY, FRIEND -> {
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				int ma = param(cell,0); // Индекс гена
				int mc = param(cell,1); // Его значение
				breakDNAOne(cell,bot,ma,mc);
			}
			case CLEAN, NOT_POISON, ORGANIC, POISON, WALL, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(numParam == 0){
			return param1Format.format(param(dna,numParam));
		}else {
			return param2Format.format(CommandList.list[param(dna,numParam)]);
		}
	}
}
