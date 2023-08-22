package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.CellObject.OBJECT;
import static MapObjects.CellObject.OBJECT.FRIEND;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import MapObjects.Fossil;
import MapObjects.Organic;
import MapObjects.Poison;
import static MapObjects.dna.CommandDNA.nextPoint;
import static MapObjects.dna.CommandDNA.param;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Кусает клетку, которую считает своей жертвой.
 * В случае неудачи, просто остаётся ни с чем
 * @author Kerravitarr
 *
 */
public class Bite extends CommandDoInterupted {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;
	
	/**
	 * Инициализирует функцию куся
	 * @param isA абсолютная или относительная функция
	 */
	public Bite(boolean isA) {
		super(isA,1);
		setInterrupt(isA, OBJECT.CLEAN, OBJECT.NOT_POISON, OBJECT.POISON, OBJECT.WALL);
	}
	
	@Override
	protected void doing(AliveCell cell) {
		bite(cell,param(cell, 0, isAbolute));
	}
	/**
	 * Непосредственно укус
	 * @param cell - кто кусает
	 * @param direction - в каком направлении от него
	 */
	protected void bite(AliveCell cell,DIRECTION direction) {
		cell.addHealth(-HP_COST); // бот теряет на этом 1 энергию
		var see = cell.see(direction);
		switch (see) {
			case ORGANIC ->  {
				Point point = nextPoint(cell,direction);
				Organic target = (Organic) Configurations.world.get(point);
				if(target.getPoison() != Poison.TYPE.UNEQUIPPED) { //Если еда ядовита - то мы получаем урон
					cell.toxinDamage(target.getPoison(),target.getPoisonCount());
				}
				
				var hpInOrg = Math.min(target.getHealth() / 4, AliveCellProtorype.MAX_HP / 2); //Сколько можем съесть
				target.addHealth(-hpInOrg); //Укусили
				if(target.getPoison() != Poison.TYPE.YELLOW)
					hpInOrg /= 2; //Если яд не жёлтый, то пища не такая вкусная
				hpInOrg = cell.specMaxVal(hpInOrg, AliveCellProtorype.Specialization.TYPE.DIGESTION); //Сколько из съеденного получили энергии
				cell.addHealth(hpInOrg);    //здоровье увеличилось
				cell.color(AliveCell.ACTION.EAT_ORG,hpInOrg);
			}
			case ENEMY, FRIEND -> {
				//--------- дошли до сюда, значит впереди живой бот -------------------
				Point point = nextPoint(cell,direction);
				AliveCell target = (AliveCell) Configurations.world.get(point);
				
				var ourMP = cell.getMineral();  // определим количество минералов у нас
				var tarMP = target.getMineral();  // определим количество минералов у потенциального обеда
				final var ourHP = cell.getHealth();  // определим наше здоровье
				var tarHP = target.getHealth();  // определим размер обеда
				if(ourMP > 0){
					var maxF = cell.specMaxVal(ourMP, AliveCellProtorype.Specialization.TYPE.ASSASSINATION);//Определим силу укуса нашими зубиками. То есть - сколько мы можем пробить минералов панциря
					maxF /= 4; //Мы только попробуем, не в полную силу
					if(maxF >= tarMP){
						cell.addMineral(tarMP);		 // количество минералов у бота уменьшается на количество минералов у жертвы. Стесали зубики
						//Свежатинку не надо переваривать. Мы её едим прям так, как есть.
						tarHP /= 4; //Мы лишь кусаем, так что берём только 1/4 всех жизней
						cell.addHealth(tarHP);
						cell.color(AliveCell.ACTION.EAT_ORG,tarHP);
						target.addHealth( -tarHP);
					} else {
						//Не смогли прокусить панцирь. Животинка нас уделала.
						cell.addMineral(-(long)maxF);
						target.addMineral(-(long)maxF); //Но зубики-то постачивала
						//Но раз мы кусаем, то отваливаемся. Не пробуем физическую силу
						//tarMP = target.getMineral();
						//ourMP = 0;
					}
				} 
				if(ourMP <= 0){
					//Мы без зубиков. Будем пробовать свою мускульную силу
					var maxF = cell.specMaxVal(ourHP, AliveCellProtorype.Specialization.TYPE.ASSASSINATION);//Определим силу укуса нашими зубиками. То есть - сколько мы можем пробить минералов панциря
					maxF /= 4; //Мы только попробуем, не в полную силу
					if(maxF >= (tarMP * 2 + tarHP)){
						//Ну хоть так мы победили! Но какой ценой?
						if(tarMP > 0)
							cell.addHealth(-(tarMP * 2));
						
						tarHP /= 4; //Мы лишь кусаем, так что берём только 1/4 всех жизней
						cell.addHealth(tarHP);
						cell.color(AliveCell.ACTION.EAT_ORG,tarHP);
						target.addHealth( -tarHP);
					} else {
						//У нас достойный противник, но, раз мы только кусили, то и живы остались
						cell.addHealth(-(long)maxF); 
						return;
					}
				}
			}
			case OWALL -> {
				//Кусь за стену
				Point point = nextPoint(cell,direction);
				Fossil target = (Fossil) Configurations.world.get(point);
				var maxF = cell.specMaxVal(cell.getMineral() * 2 + cell.getHealth(), AliveCellProtorype.Specialization.TYPE.ASSASSINATION);//Сила укуса. Ооочень сильные могут прокусить даже хороший панцирь
				maxF /= 20;//Стена оооочень крепкая, а мы ещё и работаем в четверть силы. однако эффективность ниже только в половину
				if(maxF > target.getHealth())
					target.remove_NE();
				else
					target.addHealth(-maxF);
			}
			case CLEAN, NOT_POISON, POISON, WALL -> cell.getDna().interrupt(cell, see.nextCMD);
			case BOT -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		var dir = param(dna, cell, numParam, isAbolute);
		return isFullMod() ? dir.toString() : dir.toSString();
	}
}
