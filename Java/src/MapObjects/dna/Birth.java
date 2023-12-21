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
	/**Сколько ХП стоит скопировать каждый кадон ДНК*/
	private static final double HP_FOR_KADON = 1;
	/**Сколько ХП стоит поделиться в целом*/
	private static final double HP_FOR_DIV = 150;

	public Birth() {this(1);};
	protected Birth(int countParams) {super(countParams);};
	@Override
	protected void doing(AliveCell cell) {
		final var childCMD = param(cell,0);	// Откуда будет выполняться команда ребёнка	
        final var n = cell.findEmptyDirection();	// проверим, окружен ли бот
        if (n == null) cell.bot2Organic();			// если бот окружен, то он в муках погибает
        birth(cell,n,childCMD);
	}
	/**
	 * Специальная функция, заставляет клетку поделиться безусловно.
	 * То есть будто клетка делится от переизбытка энергии
	 * @param cell - клетка, которая должна поделиться
	 */
	public static void birth(AliveCell cell) {	       
        final var n = cell.findEmptyDirection();// проверим, окружен ли бот
        if (n == null) cell.bot2Organic();      // если бот окружен, то он в муках погибает
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
		//Энергия на копирование
		cell.addHealth(-(dna.size*HP_FOR_KADON + HP_FOR_DIV));
        boolean isBirth;
		dna.next(nextCmd); // Чтобы у потомка выполнилась следующая команда
        if(cell.see(pos).groupLeader == OBJECT.BANE) {
			Poison posion = (Poison) Configurations.world.get(pos);
			posion.remove_NE(); //Не беспокойтесь. Всё нормально. Мы временно
            AliveCell newbot = new AliveCell(cell,pos);
			Configurations.world.add(newbot);
			if(Poison.createPoison(pos, posion.getType(), posion.getStepCount(), posion.getHealth(), posion.getStream())) { //А теперь на созданную клетку воздействуем ядом
				isBirth = cell.see(pos).groupLeader == OBJECT.ALIVE; //Удачное деление - это когда у нас бот на выходе
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
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		final var param = param(cell, 0);
		return Configurations.getProperty(Birth.class,isFullMod() ? "param.L" : "param.S",(dna.getPC() + param) % dna.size);
	}
	@Override
	public String value(AliveCell cell, DNA dna) {
		final var p = cell.findEmptyDirection();
		if(p == null){
			return Configurations.getProperty(Birth.class,isFullMod() ? "value.die.L" : "value.die.S");
		} else {
			return Configurations.getProperty(Birth.class,isFullMod() ? "value.L" : "value.S",(dna.size*HP_FOR_KADON + HP_FOR_DIV), p,param(dna, 0));
		}
	}
}
