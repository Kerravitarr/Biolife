package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import Utils.MyMessageFormat;
import main.Configurations;
import main.Point;

/**
 * Вставляет свою программу в код другой клетки
 * @author Kerravitarr
 *
 */
public class DNAInsert extends DNABreak {
	private final MyMessageFormat paramFormat = new MyMessageFormat("L = {0}");
	private final MyMessageFormat valueFormat = new MyMessageFormat("PC = {0}");

	public DNAInsert() {super(1);};
	@Override
	protected int perform(AliveCell cell) {
		int length_DNA = param(cell,0, cell.getDna().size);
		doing(cell);
		return length_DNA; // Но этот код не наш, мы его не выполняем!
	}
	

	@Override
	protected void doing(AliveCell cell) {
		OBJECT see = cell.see(cell.direction);
		switch (see) {
			case ENEMY, FRIEND -> {
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				int length_DNA = param(cell,0, cell.getDna().size); // Сколько копируем
				breakDNAMany(cell,bot,cell.getDna().getPC(),length_DNA);
			}
			case NOT_POISON, ORGANIC, POISON, WALL, CLEAN, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			case BOT -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	};
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){
		return paramFormat.format(param(dna,0, dna.size));
	}

	@Override
	public String value(AliveCell cell, DNA dna) {
		return valueFormat.format((param(dna, 0, dna.size) + dna.getPC()) % dna.size);
	}
}
