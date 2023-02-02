package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import Utils.MyMessageFormat;
import main.Configurations;
import main.Point;

/**
 * Команда ломает заменяяет ген, который будет выполняться в ход цели
 * На тот, что хочет.
 * Для ограничения введено важное условие
 * Работает только против того, на кого смотришь!
 * @author Kerravitarr
 *
 */
public class DNABreakNow extends DNABreak {
	private final MyMessageFormat paramFormat = new MyMessageFormat("CMD = {0}");
	/**Подменяет команду у того, на кого смотрит на параметр*/
	public DNABreakNow() {super(1);};
	@Override
	protected void doing(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		switch (see) {
			case ENEMY, FRIEND -> {
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				int mc = param(cell,0); //Значение гена
				breakDNAOne(cell,bot,0,mc);
			}
			case CLEAN, NOT_POISON, ORGANIC, POISON, WALL, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return paramFormat.format(CommandList.list[param(dna,numParam)]);
	}
}
