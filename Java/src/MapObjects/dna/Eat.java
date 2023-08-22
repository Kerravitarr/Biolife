package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.OWALL;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.Fossil;
import MapObjects.Organic;
import MapObjects.Poison;
import MapObjects.CellObject.CellObjectRemoveException;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Кушает клетку, которую выбирает своей жертвой
 * @author Kerravitarr
 *
 */
public class Eat extends CommandDoInterupted {
	/**Цена энергии на ход*/
	private final int HP_COST = 4;
	/**Абсолютные координаты или относительные*/
	private final boolean isAbolute;

	public Eat(boolean isA) {
		super(isA,1);
		isAbolute = isA;
		setInterrupt(isA, CLEAN, NOT_POISON, POISON, WALL, OWALL);
	}
	
	@Override
	protected void doing(AliveCell cell) {
		eat(cell,param(cell, 0, isAbolute));
	}
	/**
	 * Непосредственно фукнция поедания
	 * @param cell - кто ест
	 * @param direction - в каком направлении кушает
	 */
	protected void eat(AliveCell cell,DIRECTION direction) {
		cell.addHealth(-HP_COST); // бот теряет на этом 1 энергию
		var see = cell.see(direction);
		switch (see) {
			case ORGANIC ->  {
				Point point = nextPoint(cell,direction);
				Organic target = (Organic) Configurations.world.get(point);
				if(target.getPoison() != Poison.TYPE.UNEQUIPPED) { //Если еда ядовита - то мы получаем урон
					cell.toxinDamage(target.getPoison(),target.getPoisonCount());
				}
				var hpInOrg = target.getHealth(); //Сколько можем съесть
				if(target.getPoison() != Poison.TYPE.YELLOW)
					hpInOrg /= 2; //Если яд не жёлтый, то пища не такая вкусная
				hpInOrg = cell.specMaxVal(hpInOrg, AliveCellProtorype.Specialization.TYPE.DIGESTION); //Сколько из съеденного получили энергии
				cell.addHealth(hpInOrg);    //здоровье увеличилось
				cell.color(AliveCell.ACTION.EAT_ORG,hpInOrg);
				target.remove_NE(); //А цель мы всю поглотили
			}
			case ENEMY, FRIEND -> {
				//--------- дошли до сюда, значит впереди живой бот -------------------
				final Point point = nextPoint(cell,direction);
				final AliveCell target = (AliveCell) Configurations.world.get(point);
				
				var ourMP = cell.getMineral();  // определим количество минералов у нас
				var tarMP = target.getMineral();  // определим количество минералов у потенциального обеда
				final var ourHP = cell.getHealth();  // определим наше здоровье
				final var tarHP = target.getHealth();  // определим размер обеда
				if(ourMP > 0){
					var maxF = cell.specMaxVal(ourMP, AliveCellProtorype.Specialization.TYPE.ASSASSINATION);//Определим силу укуса нашими зубиками. То есть - сколько мы можем пробить минералов панциря
					if(maxF >= tarMP){
						cell.addMineral(-tarMP);		 // количество минералов у бота уменьшается на количество минералов у жертвы. Стесали зубики
						//Свежатинку не надо переваривать. Мы её едим прям так, как есть.
						cell.addHealth(tarHP);
						cell.color(AliveCell.ACTION.EAT_ORG,tarHP);
						target.remove_NE();
					} else {
						//Не смогли прокусить панцирь. Животинка нас уделала
						cell.setMineral(0);
						target.addMineral(-(long)maxF); //Но зубики-то постачивала
						tarMP = target.getMineral();
						ourMP = 0;
					}
				} 
				if(ourMP <= 0){
					//Мы без зубиков. Будем пробовать свою мускульную силу
					var maxF = cell.specMaxVal(ourHP, AliveCellProtorype.Specialization.TYPE.ASSASSINATION);//Определим силу укуса нашими зубиками. То есть - сколько мы можем пробить минералов панциря
					if(maxF >= (tarMP * 2 + tarHP)){
						//Ну хоть так мы победили! Но какой ценой?
						if(tarMP > 0)
							cell.addHealth(-(tarMP * 2));

						//Свежатинку не надо переваривать. Мы её едим прям так, как есть.
						cell.addHealth(tarHP);
						cell.color(AliveCell.ACTION.EAT_ORG,tarHP);
						target.remove_NE();
					} else {
						//У нас достойный противник
						cell.setHealth(0);  // здоровье уходит в ноль
						return;
					}
				}
			}
			case OWALL -> {
				//Кусь за стену
				Point point = nextPoint(cell,direction);
				Fossil target = (Fossil) Configurations.world.get(point);
				var maxF = cell.specMaxVal(cell.getMineral() * 2 + cell.getHealth(), AliveCellProtorype.Specialization.TYPE.ASSASSINATION);//Сила укуса. Ооочень сильные могут прокусить даже хороший панцирь
				maxF /= 10;//Стена оооочень крепкая
				if(maxF > target.getHealth())
					target.remove_NE();
				else
					target.addHealth(-maxF);
			}
			case CLEAN, NOT_POISON, POISON, WALL -> cell.getDna().interrupt(cell, see.nextCMD);
			case BOT -> throw new UnsupportedOperationException("Unimplemented case: " + see);
		}
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
}
