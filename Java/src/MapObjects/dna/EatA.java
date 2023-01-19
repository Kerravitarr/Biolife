package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import static MapObjects.CellObject.OBJECT.OWALL;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.Fossil;
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

	public EatA() {this("🍴 А","Съесть А",true);};

	protected EatA(String shotName, String longName, boolean isAbsolute) {
		super(1, shotName, longName);
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
				CellObject target = Configurations.world.get(point);
				cell.addHealth(Math.abs(target.getHealth()));    //здоровье увеличилось на сколько осталось
				cell.color(AliveCell.ACTION.EAT_ORG,target.getHealth());
				target.remove_NE();
			}
			case ENEMY, FRIEND -> {
				//--------- дошли до сюда, значит впереди живой бот -------------------
				Point point = nextPoint(cell,direction);
				AliveCell target = (AliveCell) Configurations.world.get(point);
				
				var min0 = cell.getMineral();  // определим количество минералов у нас
				var min1 = target.getMineral();  // определим количество минералов у потенциального обеда
				var hl = target.getHealth();  // определим энергию у потенциального обеда
				// если у бота минералов больше
				if (min0 >= min1) {
					cell.setMineral(min0 - min1); // количество минералов у бота уменьшается на количество минералов у жертвы
					// типа, стесал свои зубы о панцирь жертвы
					target.remove_NE(); // удаляем жертву из списков
					double cl = hl / 2;           // количество энергии у бота прибавляется на (половину от энергии жертвы)
					cell.addHealth(cl);
					cell.color(AliveCell.ACTION.EAT_ORG,cl);
					return;
				} else {
					//если у жертвы минералов больше ----------------------
					cell.setMineral(0);  // то бот израсходовал все свои минералы на преодоление защиты
					min1 = min1 - min0;       // у жертвы количество минералов тоже уменьшилось
					target.setMineral(min1);       // перезаписали минералы жертве
					//------ если здоровья в 2 раза больше, чем минералов у жертвы  ------
					//------ то здоровьем проламываем минералы ---------------------------
					if (cell.getHealth() >= 2 * min1) {
						target.remove_NE(); // удаляем жертву из списков
						double cl = Math.max(0,(hl / 2) - 2 * min1); // вычисляем, сколько энергии смог получить бот
						cell.addHealth(cl);
						cell.color(AliveCell.ACTION.EAT_ORG,cl);
						return;                             // возвращаем 5
					} else {
						//--- если здоровья меньше, чем (минералов у жертвы)*2, то бот погибает от жертвы
						target.setMineral(min1 - Math.round(cell.getHealth() / 2));  // у жертвы минералы истраченны
						cell.setHealth(0);  // здоровье уходит в ноль
						return;
					}
				}
			}
			case OWALL -> {
				//Кусь за стену
				Point point = nextPoint(cell,direction);
				Fossil target = (Fossil) Configurations.world.get(point);
				target.addHealth(-cell.getHealth() / 10);	//Стена оооочень крепкая
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
