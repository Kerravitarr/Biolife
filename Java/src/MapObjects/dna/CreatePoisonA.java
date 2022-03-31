package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.Poison;
import MapObjects.CellObject.OBJECT;
import MapObjects.Poison.TYPE;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Создаёт капельку яда
 * @author Kerravitarr
 */
public class CreatePoisonA extends CommandDo {
	/**Столько энергии тратит бот на выделение яда, причём 2/3 этого числа идут яду, 1/3 сгорает*/
	public static final long HP_FOR_POISON = 20;

	public CreatePoisonA() {this("☣ A","Пукнуть A");};
	protected CreatePoisonA(String shotName,String longName) {super(1,shotName,longName);}
	@Override
	protected void doing(AliveCell cell) {
		if (cell.getPosionType() != TYPE.НЕТ)
			addPosion(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
	}
	/**
	 * Непосредственно рождает капельку яда
	 * @param cell - клетка, которая рождает
	 * @param direction - направление, в котором капелька рождается
	 */
	protected void addPosion(AliveCell cell,DIRECTION direction) {
		cell.addHealth(-HP_FOR_POISON);      // бот затрачивает энергию на это, причём только 2/3 идёт на токсин
    	if(cell.getHealth() <= 0)
    		return;
    	
		OBJECT see = cell.see(direction);
		switch (see) {
			case WALL:
				cell.setPosionPower(cell.getPosionPower() + 1);
				return; //Мы пукнули в стену, а попало в нас. Согласен, спорный момент
			case CLEAN:
				Point point = nextPoint(cell,direction);
				Poison newPoison = new Poison(cell.getPosionType(),cell.getStepCount(),point,Math.min(HP_FOR_POISON * 2/3, cell.getPosionPower()));
	            Configurations.world.add(newPoison);//Сделали потомка
	            return;
			case ORGANIC:
			case FRIEND:
			case ENEMY:
			case POISON:
			case NOT_POISON:
				point = nextPoint(cell,direction);
				CellObject target = Configurations.world.get(point);
				if(target.toxinDamage(cell.getPosionType(), (int) Math.min(HP_FOR_POISON * 2/3, cell.getPosionPower())))
					target.remove_NE();
				return;
			default:
				throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
}
