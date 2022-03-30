package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.Poison;
import main.Configurations;
import main.Point;

public class Birth extends CommandDo {
	/**Столько энергии тратит бот на размножение*/
	private static final long HP_FOR_DOUBLE = 150;
	
	/**Общий класс для деления*/
	public static final Birth BIRTH = new Birth();

	public Birth() {super(1);};
	protected Birth(int countParams) {super(countParams);};
	@Override
	protected void doing(AliveCell cell) {
		int childCMD = 1 + 1 + param(cell,0); // Откуда будет выполняться команда ребёнка	
       
        Point n = findEmptyDirection(cell);    // проверим, окружен ли бот
        if (n == null) {           	// если бот окружен, то он в муках погибает
        	cell.setHealth(-cell.getHealth());
            return;
        }
        birth(cell,n,childCMD);
	}
	
	public static void birth(AliveCell cell) {	       
        Point n = findEmptyDirection(cell);    // проверим, окружен ли бот
        if (n == null) {           	// если бот окружен, то он в муках погибает
        	cell.setHealth(-cell.getHealth());
            return;
        }
        birth(cell,n,0);
	}
	
	protected static boolean birth(AliveCell cell, Point pos, int nextCmd) {   
		var dna = cell.getDna();     

		cell.addHealth(- HP_FOR_DOUBLE);      // бот затрачивает 150 единиц энергии на создание копии
        if (cell.getHealth() <= 0)
            return true; // Доделились, мы всё
        boolean isBirth;
		dna.next(nextCmd); // Чтобы у потомка выполнилась следующая команда	
        if(Configurations.world.test(pos).isPosion) {
			Poison posion = (Poison) Configurations.world.get(pos);
            AliveCell newbot = new AliveCell(cell,pos);
            if(newbot.toxinDamage(posion.type, (int) posion.getHealth())) { //Нас убило
            	posion.addHealth(Math.abs(newbot.getHealth()));
            	newbot.evolutionNode.remove(); //Мы так и не родились, так что нам не нужен узел
            	isBirth = false;
            } else { // Мы сильнее яда! Так что удаляем яд и занимаем его место
            	posion.remove_NE();
            	Configurations.world.add(newbot);
            	isBirth = true;
            }
        } else {
            Configurations.world.add(new AliveCell(cell,pos));
        	isBirth = true;
        }
		dna.next(-nextCmd); // А родитель выполняет свои инструкции далее
		return isBirth;
	}
}
