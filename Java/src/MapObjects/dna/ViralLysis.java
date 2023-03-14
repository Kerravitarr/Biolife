package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.Poison;
import Utils.MyMessageFormat;
import main.Configurations;
import main.Point;

/**
 * Отпочкование от клетки дочерних клеток вирусов
 * @author Kerravitarr
 *
 */
public class ViralLysis extends CommandDo {
	/**Практически ничего бот не тратит*/
	private static final long HP_FOR_DOUBLE = 1;
	private final MyMessageFormat param0Format = new MyMessageFormat("L = {0}");
	private final MyMessageFormat param1Format = new MyMessageFormat("PCp -= {0}");
	private final MyMessageFormat param2Format = new MyMessageFormat("PCc = {0}");
	private final MyMessageFormat param3Format = new MyMessageFormat("HPc = {0}");
	private final MyMessageFormat valueFormat = new MyMessageFormat("HP -= {0}; PC += {1}");

	public ViralLysis() {super(4);};
	
	@Override
	protected void doing(AliveCell cell) {
		int length_DNA = param(cell,0, cell.getDna().size - 1) + 1; //Длина ДНК
		int pref = param(cell,1); //Сколько генов отступаем назад
		int PC = param(cell,2); //Какое положение занимает указатель в ДНК
		int HP = param(cell,3,AliveCellProtorype.MAX_HP); //Сколько ХП дать новой клетке
		HP = (int) Math.min(HP, cell.getHealth() - HP_FOR_DOUBLE);
        Point n = findEmptyDirection(cell);    // проверим, окружен ли бот
        if (n == null)          	// если бот окружен, то он в муках погибает
        	return ;//Ну что-ж, не в этот раз
		cell.addHealth(-HP_FOR_DOUBLE);      // бот затрачивает энергии на создание копии
		
		var dnaChild = new DNA(length_DNA);
		var dnaPerrent = cell.getDna();
		for(var i = 0 ; i < length_DNA ; i++){
			dnaChild.criticalUpdate(i, dnaPerrent.get(0, dnaPerrent.getPC() - pref + i));
		}
		dnaChild.next(PC);
		if (Configurations.world.test(n).isPosion) {
			Poison posion = (Poison) Configurations.world.get(n);
			posion.remove_NE(); //Не беспокойтесь. Всё нормально. Мы временно
			AliveCell newbot = new AliveCell(cell,n, HP, dnaChild);
			Configurations.world.add(newbot);
			if (Poison.createPoison(n, posion.getType(), posion.getStepCount(), posion.getHealth(), posion.getStream())) { //А теперь на созданную клетку воздействуем ядом
				//isBirth = Configurations.world.test(pos) == OBJECT.BOT; //Удачное деление - это когда у нас бот на выходе
			} else {
				//Как это не получилось провзаимодействовать с клеткой?!
				throw new RuntimeException("Не сработала функция создания ребёнка вот сюда: " + n);
			}
		} else {
			Configurations.world.add(new AliveCell(cell,n, HP, dnaChild));
		}
		dnaPerrent.next(Math.max(0, length_DNA - pref)); //Мы отматываем на дополнительные несколько команд
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
			return switch (numParam) {
				case 0 -> param0Format.format(param(dna,0, dna.size));
				case 1 -> param1Format.format(param(dna,0));
				case 2 -> param2Format.format(param(dna,0));
				case 3 -> param3Format.format(param(dna,0,AliveCellProtorype.MAX_HP));
				default-> super.getParam(cell, numParam, dna);
			};
	}
	
	@Override
	public String value(AliveCell cell, DNA dna) {
		int length_DNA = param(cell,0, cell.getDna().size - 1) + 1; //Длина ДНК
		int pref = param(cell,1); //Сколько генов отступаем назад
		int HP = param(cell,3,AliveCellProtorype.MAX_HP); //Сколько ХП дать новой клетке
		return valueFormat.format(HP_FOR_DOUBLE + HP, 1 + getCountParams() + Math.max(0, length_DNA - pref));
	}
}
