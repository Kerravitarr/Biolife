package MapObjects.dna;

import MapObjects.AliveCell;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Абстрактный класс, он является основой для любого гена ДНК
 * @author Kerravitarr
 *
 */
public abstract class CommandDNA {
	/**Возможность функции уйти в прерывание*/
	protected boolean isInterrupt = false;
	/**Количество параметров у клетки*/
	private int countParams;
	/**Количество переходов у клетки*/
	private int countBranch;
	/**Короткое имя*/
	private String shotName;
	/**Полное имя*/
	private String longName;
	
	protected CommandDNA(int countParams,int countBranch,String shotName,String longName) {
		this.countParams = countParams;
		this.countBranch = countBranch;
		this.shotName = shotName;
		this.longName = longName;
	}

	/**Выполняет действие над клеткой и возвращает truе, если это полное действие*/
	public boolean execute(AliveCell cell) {
		/**Указывает какую ветвь выполнять функции*/
		int branch = perform(cell);
		if(isDoing()) {
			cell.getDna().next(1 + countParams + branch);
		}else {
			var dna = cell.getDna();
			var PC = dna.getIndex() + 1 + countParams;
			dna.next(dna.get(PC, branch)); // Сдвижка определеяется параметром из ДНК
		}
		return isDoing();
	}
	/**Выполняет действие над клеткой*/
	protected abstract int perform(AliveCell cell);
	
	/**Возвращает true, если команда выполняет действие (конечная)*/
	public abstract boolean isDoing();
	/**Возвращает true, если команда может вызвать прерывание*/
	public boolean isInterrupt() {return isInterrupt;};	
	
	
	/**Возвращает параметр ДНК*/
	protected static int param(AliveCell cell, int numParam) {
		return param(cell.getDna(),numParam);
	}
	/**Возвращает параметр ДНК*/
	protected static int param(DNA dna, int numParam) {
		return dna.get(dna.getIndex(),  1 +numParam);
	}
	/**Возвращает параметр ДНК ограниченный максимальным значением*/
	protected static int param(AliveCell cell, int index, double maxVal) {
		return param(cell.getDna(),index,maxVal);
	}
	/**Возвращает параметр ДНК ограниченный максимальным значением*/
	protected static int param(DNA dna, int index, double maxVal) {
		return (int) Math.round(maxVal * param(dna,index) / CommandList.COUNT_COMAND);
	}

	/**Ищет пустое направление вокруг клетки*/
	protected static Point findEmptyDirection(AliveCell cell) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
	    	if(i == 0 || i == 4) {
		        Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(i)));
		        if (Configurations.world.test(point).isEmptyPlase)
		            return point;
	    	} else {
	    		int dir = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
	    		Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(dir)));
		        if (Configurations.world.test(point).isEmptyPlase)
		            return point;
		        dir = -dir;
	    		point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(dir)));
		        if (Configurations.world.test(point).isEmptyPlase)
		            return point;
	    	}
	    }
	    return null;
	}
	/**Возвращает точку в заданном направлении*/
	protected static Point nextPoint(AliveCell cell,DIRECTION direction) {
	    return cell.getPos().next(direction);
	}
	/**Превращает относительное направление в абсолютное*/
	protected static DIRECTION relatively(AliveCell cell,DIRECTION direction) {
		return direction.next(cell.direction);
	}
	/**Превращает относительное направление в абсолютное*/
	protected static DIRECTION relatively(AliveCell cell,int direction) {
		return cell.direction.next(direction);
	}
}
