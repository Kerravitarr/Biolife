package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.CellObject.OBJECT;
import MapObjects.Fossil;
import MapObjects.Organic;
import MapObjects.Poison;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Кусает клетку, которую считает своей жертвой.
 * В случае неудачи, просто остаётся ни с чем
 * @author Kerravitarr
 *
 */
public class BiteA extends CommandDoInterupted {
	/**Цена энергии на ход*/
	private final int HP_COST = 2;

	public BiteA() {this(true);};

	/**
	 * Инициализирует функцию куся
	 * @param shotName короткое имя функции
	 * @param longName полное имя функции
	 * @param isAbsolute абсолютная или относительная функция
	 */
	protected BiteA(boolean isAbsolute) {
		super(1);
		setInterrupt(isAbsolute, OBJECT.CLEAN, OBJECT.NOT_POISON, OBJECT.POISON, OBJECT.WALL);
	}
	
	@Override
	protected void doing(AliveCell cell) {
		bite(cell,DIRECTION.toEnum(param(cell,0, DIRECTION.size())));
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
			var F = cell.get(AliveCellProtorype.Specialization.TYPE.DIGESTION);//Эффективность пищеварения - поглащения отработанной пищи
			var hpInOrg = F * target.getHealth() / 4;	
			if(target.getPoison() != Poison.TYPE.YELLOW)	//Обработанная жёлтым ядом пища - сытнее
				hpInOrg /= 2;
			cell.addHealth(hpInOrg);    //здоровье увеличилось
			cell.color(AliveCell.ACTION.EAT_ORG,hpInOrg);
			target.addHealth(-hpInOrg); //Одну четверть отдали
		}
		case ENEMY, FRIEND -> {
			//--------- дошли до сюда, значит впереди живой бот -------------------
			Point point = nextPoint(cell,direction);
			AliveCell target = (AliveCell) Configurations.world.get(point);

			var min0 = cell.getMineral();  // определим количество минералов у нас
			var min1 = target.getMineral() / 2;  // определим количество минералов у цели,
			//но так как мы только кусаем - то и прорываться нам не через весь панцирь
			var hl = target.getHealth();  // определим энергию у потенциального кусиха
			var F = cell.get(AliveCellProtorype.Specialization.TYPE.ASSASSINATION);//Сила укуса. Ооочень сильные могут прокусить даже хороший панцирь
			//Если у цели минералов не слишком много, а у нас жизней сильно меньше - можем его кусить
			if (min0 * F * 2 >= (min1/2) && (cell.getHealth()/2 < hl)) {
				cell.setMineral(min0 - min1/2); // количество минералов у бота уменьшается на количество минералов у жертвы
				// типа, стесал свои зубы о панцирь жертвы
				var cl = F * hl / 2;           // количество энергии у бота прибавляется лишь чуть чуть, мы же кусили
				cell.addHealth(cl);
				target.addHealth(-cl);
				cell.color(AliveCell.ACTION.EAT_ORG,cl);
			} else {
				//если у жертвы минералов больше, то нам его просто не прокусить
				cell.setMineral(cell.getMineral()/2);  //Ну мы же попробовали

			}
		}
		case OWALL -> {
			//Кусь за стену
			Point point = nextPoint(cell,direction);
			Fossil target = (Fossil) Configurations.world.get(point);
			target.addHealth(-cell.getHealth() / 20);	//Стена оооочень крепкая
			if(target.getHealth() < 0) {
				target.remove_NE();
			}
		}
		case CLEAN, NOT_POISON, POISON, WALL -> cell.getDna().interrupt(cell, see.nextCMD);
		default -> throw new IllegalArgumentException("Unexpected value: " + see);
	}
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna){return absoluteDirection(param(dna,0, DIRECTION.size()));};
}
