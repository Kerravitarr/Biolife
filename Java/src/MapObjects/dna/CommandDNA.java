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
	private final int countParams;
	/**Количество переходов у клетки*/
	private final int countBranch;
	/**Короткое имя*/
	private final String shotName;
	/**Полное имя*/
	private final String longName;
	/**Показывает, что мы должны отображать всё в кратком виде*/
	private static boolean isFullMod = false;
	/**
	 * Констурктор класса
	 * @param countParams - число параметров у функции
	 * @param countBranch - число ветвей у функции
	 * @param shotName - краткое имя у функции
	 * @param longName - полное имя функции
	 */
	protected CommandDNA(int countParams,int countBranch,String shotName,String longName) {
		this.countParams = countParams;
		this.countBranch = countBranch;
		this.shotName = shotName;
		this.longName = longName;
	}

	/**
	 * Выполняет действие над клеткой
	 * @param cell - кто именно будет выполнять действие
	 * @return true, если это полное действие - то есть действие, которое будет последним
	 */
	public boolean execute(AliveCell cell) {
		/**Указывает какую ветвь выполнять функции*/
		int branch = perform(cell);
		if(getCountBranch() == 0) {
			cell.getDna().next(1 + getCountParams() + branch);
		}else {
			var dna = cell.getDna();
			var PC = dna.getIndex() + 1 + getCountParams();
			dna.next(dna.get(PC, branch)); // Сдвижка определеяется параметром из ДНК
		}
		return isDoing();
	}
	/**
	 * Выполняет действие над клеткой
	 * @param cell - клетка, над которой надо выполнить действие
	 * @return специальный параметр. Если у функции есть ветви, то 
	 * это число показывает какой из параметрв нужно использовать.
	 * Иначе, он просто показывает на сколько дополнительно нужно сдвинуть PC
	 */
	protected abstract int perform(AliveCell cell);
	
	/**
	 * Проверяет, эта команда что-то делает с клеткой или только исследует окружающий мир
	 * @return true, если команда выполняет действие (конечная)
	 */
	public abstract boolean isDoing();
	/**
	 * Проверяет, имеет-ли команда прерывание
	 * @return true, если команда может вызвать прерывание
	 */
	public boolean isInterrupt() {return isInterrupt;};	
	
	
	/**
	 * Возвращает параметр ДНК
	 * @param cell - клетка, параметр который возвращаем
	 * @param numParam - номер параметра
	 * @return значение параметра, от 0 до CommandList.COUNT_COMAND, включетльно
	 */
	protected static int param(AliveCell cell, int numParam) {
		return param(cell.getDna(),numParam);
	}
	/**
	 * Возвращает параметр ДНК
	 * @param dna - днк, параметр который возвращаем
	 * @param numParam - номер параметра
	 * @return значение параметра, от 0 до CommandList.COUNT_COMAND, включетльно
	 */
	protected static int param(DNA dna, int numParam) {
		return dna.get(dna.getIndex(),  1 +numParam);
	}
	/**
	 * Возвращает параметр ДНК
	 * @param cell - клетка, параметр который возвращаем
	 * @param index - номер параметра
	 * @param maxVal - максимальное значение, которым ограничивается параметр
	 * @return значение параметра, от 0 до maxVal, включетльно
	 */
	protected static int param(AliveCell cell, int index, double maxVal) {
		return param(cell.getDna(),index,maxVal);
	}
	/**
	 * Возвращает параметр ДНК
	 * @param dna - днк, параметр который возвращаем
	 * @param index - номер параметра
	 * @param maxVal - максимальное значение, которым ограничивается параметр
	 * @return значение параметра, от 0 до maxVal, включетльно
	 */
	protected static int param(DNA dna, int index, double maxVal) {
		return (int) Math.round(maxVal * param(dna,index) / CommandList.COUNT_COMAND);
	}

	/**
	 * Ищет пустое направление вокруг клетки
	 * @param cell - клетка
	 * @return ближайшая пустая точка или null
	 */
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
	/**
	 * Возвращает точку в заданном направлении
	 * @param cell - клетка, которая задаёт точку начала
	 * @param direction - напаравление, в котором нам нужна клетка
	 * @return 
	 */
	protected static Point nextPoint(AliveCell cell,DIRECTION direction) {
	    return cell.getPos().next(direction);
	}
	/**
	 * Превращает относительное направление в абсолютное
	 * @param cell - клетка, с её напралвлением смотерния
	 * @param direction - направление, на которое нужно сдвинуть гляделку
	 * @return 
	 */
	protected static DIRECTION relatively(AliveCell cell,DIRECTION direction) {
		return cell.direction.next(direction);
	}
	/**
	 * Превращает относительное направление в абсолютное
	 * @param cell - клетка, с её напралвлением смотерния
	 * @param direction - направление, на которое нужно сдвинуть гляделку
	 * @return 
	 */
	protected static DIRECTION relatively(AliveCell cell,int direction) {
		return cell.direction.next(direction);
	}

	protected String getLongName() {
		return longName;
	}

	protected String getShotName() {
		return shotName;
	}

	public int getCountParams() {
		return countParams;
	}

	public int getCountBranch() {
		return countBranch;
	}

	@Override
	public String toString() {
		if (isFullMod)
			return getLongName();
		else
			return getShotName();
	}
	/**
	 * Выдаёт имя функции
	 * @param cell - клетка, функция которой исследуется
	 * @param dna - "локальная" копия ДНК, именно она решает как будет выглядеть результат
	 * @return Строковое описание функции
	 */
	public String toString(AliveCell cell, DNA dna) {
		return toString();
	}
	/**
	 * Возвращает описание параметра по переданному значению
	 * @param cell - клетка, параметр который нам важен
	 * @param numParam - номер этого параметра, считая от 0
	 * @param dna - "локальная" копия ДНК, в которой и хранится параметр
	 * @return текстовое описание параметра
	 */
	public String getParam(AliveCell cell, int numParam, DNA dna){
		if(countParams != 0)
			throw new UnsupportedOperationException("Забыл подписать параметр для " + toString() + " " + this.getClass());
		else
			return nonParam();
	};
	/**
	 * Заглушка для функции без параметров
	 * @return ""
	 */
	protected String nonParam() {return "";};
	/**
	 * Переводит значение в абсолютное направление
	 * @param value - значение параметра
	 * @return Текстовое описание направления
	 */
	protected String absoluteDirection(int value) {
		if (isFullMod)
			return DIRECTION.toEnum(value).name();
		else
			return DIRECTION.toEnum(value).toString();
	};

	/**
	 * Переводит значение в относительное направление
	 * @param cell - клетка. Ну направление-же относительное, поэтомуо относительно того, куда глядит клетка
	 * @param value - значение параметра
	 * @return Текстовое описание направления
	 */
	protected String relativeDirection(AliveCell cell, int value) {
		if (isFullMod)
			return cell.direction.next(value).name();
		else
			return cell.direction.next(value).toString();
	};

	public static void setFullMod(boolean isFullMod) {
		CommandDNA.isFullMod = isFullMod;
	}
	/**
	 * Возвращает номер сработавшего прерывания
	 * @param cell - клетка, у которой срабатывает прерывание
	 * @param dna - "локальная" копия ДНК, в которой и хранится параметр
	 * @return номер прерывания или -1
	 */
	public int getInterrupt(AliveCell cell, DNA dna) {
		throw new UnsupportedOperationException("Если у вас есть прерывание - будьте любезны его реализовать для " + toString() + " " + this.getClass());
	}
}
