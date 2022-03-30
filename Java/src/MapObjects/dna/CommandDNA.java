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
	private int countParams;
	
	protected CommandDNA(int countParams) {
		this.countParams = countParams;
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
	
	

	protected static int param(AliveCell cell, int numParam) {
		return param(cell.getDna(),numParam);
	}
	protected static int param(DNA dna, int numParam) {
		return dna.get(dna.getIndex(),  1 +numParam);
	}
	protected static int param(AliveCell cell, int index, double maxVal) {
		return param(cell.getDna(),index,maxVal);
	}
	protected static int param(DNA dna, int index, double maxVal) {
		return (int) Math.round(maxVal * param(dna,index) / CommandList.COUNT_COMAND);
	}

	protected static Point findEmptyDirection(AliveCell cell) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
	    	if(i == 0 || i == 4) {
		        Point point = fromVektor(cell,relatively(cell,DIRECTION.toEnum(i)));
		        if (Configurations.world.test(point).isEmptyPlase)
		            return point;
	    	} else {
	    		int dir = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
	    		Point point = fromVektor(cell,relatively(cell,DIRECTION.toEnum(dir)));
		        if (Configurations.world.test(point).isEmptyPlase)
		            return point;
		        dir = -dir;
	    		point = fromVektor(cell,relatively(cell,DIRECTION.toEnum(dir)));
		        if (Configurations.world.test(point).isEmptyPlase)
		            return point;
	    	}
	    }
	    return null;
	}
	protected static Point fromVektor(AliveCell cell,DIRECTION direction) {
	    return cell.getPos().next(direction);
	}
	protected static DIRECTION relatively(AliveCell cell,DIRECTION direction) {
		return direction.next(cell.direction);
	}
	protected static DIRECTION relatively(AliveCell cell,int direction) {
		return cell.direction.next(direction);
	}
}
