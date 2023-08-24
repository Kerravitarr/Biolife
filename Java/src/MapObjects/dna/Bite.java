package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import static MapObjects.AliveCellProtorype.Specialization.TYPE.ASSASSINATION;
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
	/**Бонус, от использования стратегии*/
	private final int BONUS = 2;
	
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
				final var point = nextPoint(cell,direction);
				final var target = (Organic) Configurations.world.get(point);
				if(target.getPoison() != Poison.TYPE.UNEQUIPPED) { //Если еда ядовита - то мы получаем урон
					cell.toxinDamage(target.getPoison(),target.getPoisonCount());
				}
				var hpInOrg = Math.min(target.getHealth() / 4, ((AliveCell.MAX_HP - cell.getHealth())) / 2); //Сколько можем съесть
				target.addHealth(-hpInOrg); //Укусили
				if(cell.getMainSpec() == AliveCellProtorype.Specialization.TYPE.DIGESTION){
					//Мы умеем переваривать органику - мы перевариваем органику!
					if(target.getPoison() != Poison.TYPE.YELLOW)
						hpInOrg /= 2; //Если яд не жёлтый, то пища не такая вкусная
					hpInOrg = cell.specMaxVal(hpInOrg, AliveCellProtorype.Specialization.TYPE.DIGESTION); //Сколько из съеденного получили энергии
					cell.addHealth(hpInOrg);    //здоровье увеличилось
					cell.color(AliveCell.ACTION.EAT_ORG,hpInOrg);
				} else {
					//Мы плохо перевариваем органику, а значит мы ей травимся
					hpInOrg = hpInOrg - cell.specMaxVal(hpInOrg, AliveCellProtorype.Specialization.TYPE.DIGESTION); //Какая часть из съеденного пошла не в то горло?
					cell.addHealth(-hpInOrg);
				}
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
					//Раунд 1, когда боты мерюются минералами
					var ourF = cell.specMaxVal(ourMP * BONUS, ASSASSINATION);		//Определим силу укуса нашими зубиками. То есть - сколько мы можем пробить минералов панциря
					var tarF = target.specMaxVal(tarMP / BONUS, ASSASSINATION);	//Определим на сколько другой противник умелый и сможет себя защитить
					if(ourF >= tarF){
						//Мы прокусили панцирь, теперь можем попить жизенных соков
						if(ourMP > tarF)
							cell.addMineral((long) -tarF);		 // количество минералов у бота уменьшается на количество минералов у жертвы. Стесали зубики
						else
							cell.setMineral(0);
						//А теперь выпьем часть жизненных соков
						tarHP /= 4;
						cell.addHealth(tarHP);
						cell.color(AliveCell.ACTION.EAT_ORG,tarHP);
						target.addHealth( -tarHP);
					} else {
						//Не смогли прокусить панцирь. Животинка сама нас победила
						cell.setMineral(0);
						target.addMineral(-(long)ourF); //И только зубки сточила
						return;
					}
				} 
				if(ourMP <= 0){
					//Мы без зубиков. Будем пробовать свою мускульную силу
					var maxF = cell.specMaxVal(ourHP * BONUS, ASSASSINATION);//Определим силу укуса нашими зубиками. То есть - сколько мы можем пробить минералов панциря
					var tarF = target.specMaxVal((tarMP * 2 + target.getHealth()) / BONUS, ASSASSINATION);	//Определим на сколько другой противник умелый и сможет себя защитить
					if(maxF >= tarF){
						//Ну хоть так мы победили! Но какой ценой?
						if(tarMP > 0){
							if(ourHP > tarF)	cell.addHealth((long) -tarF);
							else				cell.setHealth(1);
						}
						//А теперь выпьем часть жизненных соков
						tarHP /= 4;
						cell.addHealth(tarHP);
						cell.color(AliveCell.ACTION.EAT_ORG,tarHP);
						target.addHealth( -tarHP);
					} else {
						//У нас достойный противник
						if(ourHP > tarF)	cell.addHealth((long) -tarF);
						else				cell.setHealth(1);
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
