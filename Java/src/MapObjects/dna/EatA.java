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
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Кушает клетку, которую выбирает своей жертвой
 * @author Kerravitarr
 *
 */
public class EatA extends CommandDoInterupted {
	/**Цена энергии на ход*/
	private final int HP_COST = 4;

	public EatA() {this(true);};

	protected EatA(boolean isAbsolute) {
		super(1);
		setInterrupt(isAbsolute, CLEAN, NOT_POISON, POISON, WALL,OWALL);
	}
	
	@Override
	protected void doing(AliveCell cell) {
		eat(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
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
				var F = cell.get(AliveCellProtorype.Specialization.TYPE.DIGESTION);//Эффективность пищеварения - поглащения отработанной пищи
				var hpInOrg = F * target.getHealth();	
				if(target.getPoison() != Poison.TYPE.YELLOW)	//Обработанная жёлтым ядом пища - сытнее
					hpInOrg /= 2;
				cell.addHealth(hpInOrg);    //здоровье увеличилось
				cell.color(AliveCell.ACTION.EAT_ORG,hpInOrg);
				target.remove_NE();
			}
			case ENEMY, FRIEND -> {
				//--------- дошли до сюда, значит впереди живой бот -------------------
				Point point = nextPoint(cell,direction);
				AliveCell target = (AliveCell) Configurations.world.get(point);
				
				var min0 = cell.getMineral();  // определим количество минералов у нас
				var min1 = target.getMineral();  // определим количество минералов у потенциального обеда
				var hl = target.getHealth();  // определим энергию у потенциального обеда
				var F = cell.get(AliveCellProtorype.Specialization.TYPE.ASSASSINATION);//Сила укуса. Ооочень сильные могут прокусить даже хороший панцирь
				// если у бота минералов больше
				if (min0 * F * 2 >= min1) {
					cell.addMineral(-min1);		 // количество минералов у бота уменьшается на количество минералов у жертвы
					// типа, стесал свои зубы о панцирь жертвы
					target.remove_NE(); 		// удаляем жертву из списков
					double cl = F * hl;         // количество энергии у бота прибавляется
					cell.addHealth(cl);
					cell.color(AliveCell.ACTION.EAT_ORG,cl);
					return;
				} else {
					//если у жертвы минералов больше ----------------------
					cell.setMineral(0);  // то бот израсходовал все свои минералы на преодоление защиты
					min1 = (long) (min1 - min0 * F * 2);       // у жертвы количество минералов тоже уменьшилось
					target.setMineral(min1);       // перезаписали минералы жертве
					//------ если здоровья в 2 раза больше, чем минералов у жертвы  ------
					//------ то здоровьем проламываем минералы ---------------------------
					if (cell.getHealth() * F * 2 >= 2 * min1) {
						target.remove_NE(); // удаляем жертву из списков
						double cl = Math.max(0,(F * hl) - 2 * min1); // вычисляем, сколько энергии смог получить бот
						cell.addHealth(cl);
						cell.color(AliveCell.ACTION.EAT_ORG,cl);
						return;                             // возвращаем 5
					} else {
						//--- если здоровья меньше, чем (минералов у жертвы)*2, то бот погибает от жертвы
						target.setMineral(min1 - Math.round(F * 2 * cell.getHealth() / 2));  // у жертвы минералы истраченны
						cell.setHealth(0);  // здоровье уходит в ноль
						return;
					}
				}
			}
			case OWALL -> {
				//Кусь за стену
				Point point = nextPoint(cell,direction);
				Fossil target = (Fossil) Configurations.world.get(point);
				var F = cell.get(AliveCellProtorype.Specialization.TYPE.ASSASSINATION) * 2;//Сила укуса. Ооочень сильные могут прокусить даже хороший панцирь
				target.addHealth(- F * cell.getHealth() / 10);	//Стена оооочень крепкая
				if(target.getHealth() < 0) {
					target.remove_NE();
				}
			}
			case CLEAN, NOT_POISON, POISON, WALL -> cell.getDna().interrupt(cell, see.nextCMD);
			case BOT -> throw new UnsupportedOperationException("Unimplemented case: " + see);
		}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return absoluteDirection(param(dna,0, DIRECTION.size()));};
}
