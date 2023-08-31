package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.Poison;
import MapObjects.CellObject.OBJECT;
import Calculations.Configurations;
import Calculations.Point;

/**
 * Деление, то есть заставляет одну клетку превратиться в две
 * @author Kerravitarr
 *
 */
public class Birth extends CommandDo {
	/**Столько энергии тратит бот на размножение*/
	private static final long HP_FOR_DOUBLE = 150;

	public Birth() {this(1);};
	protected Birth(int countParams) {super(countParams);};
	@Override
	protected void doing(AliveCell cell) {
		int childCMD = 1 + 1 + param(cell,0); // Откуда будет выполняться команда ребёнка	
       
        Point n = findEmptyDirection(cell);    // проверим, окружен ли бот
        if (n == null)          	// если бот окружен, то он в муках погибает
        	cell.bot2Organic();
        birth(cell,n,childCMD);
	}
	/**
	 * Специальная функция, заставляет клетку поделиться безусловно.
	 * То есть будто клетка делится от переизбытка энергии
	 * @param cell - клетка, которая должна поделиться
	 */
	public static void birth(AliveCell cell) {	       
        Point n = findEmptyDirection(cell);    // проверим, окружен ли бот
        if (n == null)           	// если бот окружен, то он в муках погибает
        	cell.bot2Organic();
        birth(cell,n,0);
	}
	/**
	 * Сама функция деления
	 * @param cell - клетка, которая делится
	 * @param pos - позиция, куда она хочет положить потомка
	 * @param nextCmd - на сколько тактовый счётчик потомка уйдёт вперёд
	 * @return - true, если удачное деление
	 */
	protected static boolean birth(AliveCell cell, Point pos, int nextCmd) {   
		var dna = cell.getDna();
		cell.addHealth(-HP_FOR_DOUBLE);      // бот затрачивает 150 единиц энергии на создание копии
        boolean isBirth;
		dna.next(nextCmd); // Чтобы у потомка выполнилась следующая команда	
        if(Configurations.world.test(pos).isPosion) {
			Poison posion = (Poison) Configurations.world.get(pos);
			posion.remove_NE(); //Не беспокойтесь. Всё нормально. Мы временно
            AliveCell newbot = new AliveCell(cell,pos);
			Configurations.world.add(newbot);
			if(Poison.createPoison(pos, posion.getType(), posion.getStepCount(), posion.getHealth(), posion.getStream())) { //А теперь на созданную клетку воздействуем ядом
				isBirth = Configurations.world.test(pos) == OBJECT.BOT; //Удачное деление - это когда у нас бот на выходе
			} else {
						//Как это не получилось провзаимодействовать с клеткой?!
				throw new RuntimeException("Не сработала функция создания ребёнка вот сюда: " + pos);
			}
        } else {
            Configurations.world.add(new AliveCell(cell,pos));
        	isBirth = true;
        }
		dna.next(-nextCmd); // А родитель выполняет свои инструкции далее
		return isBirth;
	}
	

	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return String.valueOf(param(cell,0)+1+1);}
}
