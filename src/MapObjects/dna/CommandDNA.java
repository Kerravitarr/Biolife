package MapObjects.dna;

import MapObjects.AliveCell;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;
import MapObjects.CellObject;

/**
 * Абстрактный класс, он является основой для любого гена ДНК
 * @author Kerravitarr
 *
 */
public abstract class CommandDNA {	
	/**Возможность функции уйти в прерывание*/
	protected boolean isInterrupt = false;
	/**Количество параметров у этой функции (сколько следующий кодонов будут кодировать параметры)*/
	private final int countParams;
	/**Количество ветвей (сколько следующих кодонов будут содержать адреса перехода)*/
	private final int countBranch;
	/**Короткое имя*/
	private final String shotName;
	/**Полное имя*/
	private final String longName;
	/**Показывает, что мы должны отображать всё в кратком виде*/
	private static boolean isFullMod = true;
	/**Больше параметра*/
	private static String parametrMoreOrEqual = "≥" + Configurations.getProperty(CommandDNA.class, "parameter");
	/**Меньше параметра*/
	private static String parametrLess = "<" + Configurations.getProperty(CommandDNA.class, "parameter");
	/**Абсолютные координаты или относительные. Для комнад, для которых может быть разночтение*/
	protected final boolean isAbolute;

	/**
	 * Констурктор класса
	 * @param countParams - число параметров у функции
	 * @param countBranch - число ветвей у функции
	 */
	protected CommandDNA(int countParams,int countBranch) {
		this(null, countParams,countBranch, null);
	}
	/**
	 * Констурктор класса для тех команд, которые могут быть как абсолютные, так и относительные
	 * @param countParams - число параметров у функции
	 * @param countBranch - число ветвей у функции
	 * @param propName - дополнительное имя функции
	 */
	protected CommandDNA(int countParams,int countBranch, String propName) {
		this(null, countParams,countBranch, propName);
	}
	/**
	 * Констурктор класса для тех команд, которые могут быть как абсолютные, так и относительные
	 * @param isAbsolute - команда выполняется в абсолютном или относительном выражении
	 * @param countParams - число параметров у функции
	 * @param countBranch - число ветвей у функции
	 */
	protected CommandDNA(Boolean isAbsolute, int countParams,int countBranch) {
		this(isAbsolute, countParams,countBranch, null);
	}
	/**
	 * Констурктор класса для тех команд, которые могут быть как абсолютные, так и относительные
	 * @param isAbsolute - команда выполняется в абсолютном или относительном выражении
	 * @param countParams - число параметров у функции
	 * @param countBranch - число ветвей у функции
	 * @param propName - дополнительное имя функции
	 */
	protected CommandDNA(Boolean isAbsolute, int countParams,int countBranch, String propName) {
		this.countParams = countParams;
		this.countBranch = countBranch;
		String nameS = Configurations.getProperty(this.getClass(), propName == null ? "Shot" : (propName + ".Shot"));
		String nameL = Configurations.getProperty(this.getClass(), propName == null ? "Long" : (propName + ".Long"));
		if(isAbsolute != null) {
			isAbolute = isAbsolute;
			nameS += " " + Configurations.getProperty(CommandDNA.class, isAbolute ?"absolute.S":"relative.S");
			nameL += " " + Configurations.getProperty(CommandDNA.class, isAbolute ?"absolute.L":"relative.L");
		} else {
			isAbolute = false;
		}
		shotName = nameS;
		longName = nameL;
	}

	/** Выполняет текущую инструкцию клетки
	 * @param cell клетка, которая жаждет выполнить эту инструкцию
	 * @return true, если это последнее действие. То есть клетка походила, пофотосинтезировала, размножилась и т.д.
	 *		неполные действия предусматривают возможсность запустить следующее действие
	 */
	public boolean execute(AliveCell cell) {
		cell.getDna().next(perform(cell));
		return isDoing();
	}
	/**
	 * Выполняет действие над клеткой
	 * @param cell - клетка, над которой надо выполнить действие
	 * @return на сколько нужно сдвинуть PC в ДНК
	 */
	protected abstract int perform(AliveCell cell);
	
	/**
	 * Проверяет, эта команда что-то делает с клеткой или только исследует окружающий мир
	 * @return true, если команда выполняет действие. Такая команда завершает текущий ход и, по необходимости, выходит из прерывания
	 */
	public abstract boolean isDoing();
	/**
	 * Проверяет, имеет-ли команда прерывание
	 * @return true, если команда может вызвать прерывание
	 */
	public boolean isInterrupt() {return isInterrupt;};	
	
	
	/** Возвращает параметр ДНК
	 * @param cell - клетка, параметр который возвращаем
	 * @param numParam - номер параметра для текущей команды
	 * @return значение параметра, [0,CommandList.COUNT_COMAND]
	 */
	protected static int param(AliveCell cell, int numParam) {
		return param(cell.getDna(),numParam);
	}
	/** Возвращает параметр ДНК
	 * @param dna - ДНК, параметр который возвращаем
	 * @param numParam - номер параметра для текущей команды (на неё указывает PC в ДНК)
	 * @return значение параметра [0,CommandList.COUNT_COMAND]
	 */
	protected static int param(DNA dna, int numParam) {
		return dna.get(1 + numParam,false);
	}
	/** Возвращает параметр ДНК
	 * @param cell - клетка, параметр который возвращаем
	 * @param numParam - номер параметра для текущей команды
	 * @param maxVal - максимальное значение, которым ограничивается параметр
	 * @return значение параметра [0;maxVal]
	 */
	protected static int param(AliveCell cell, int numParam, double maxVal) {
		return param(cell.getDna(),numParam,maxVal);
	}
	/** Возвращает параметр ДНК как направление смотрения
	 * @param cell - клетка, параметр который возвращаем
	 * @param numParam - номер параметра для текущей команды
	 * @param isAbsolute - абсолютное-ли значение направления нам требуется?
	 * @return значение параметра, [0;maxVal]
	 */
	protected static DIRECTION param(AliveCell cell, int numParam, boolean isAbsolute) {
		var par = param(cell,numParam, DIRECTION.size() - 1);
		return isAbsolute ? DIRECTION.toEnum(par) : cell.direction.next(par);
	}
	/** Возвращает параметр ДНК как направление смотрения
	 * @param dna - днк, параметр который возвращаем
	 * @param cell - клетка, параметр который возвращаем
	 * @param numParam - номер параметра для текущей команды
	 * @param isAbsolute - абсолютное-ли значение направления нам требуется?
	 * @return значение направления
	 */
	protected static DIRECTION param(DNA dna, AliveCell cell, int numParam, boolean isAbsolute) {
		var par = param(dna,numParam, DIRECTION.size() - 1);
		return isAbsolute ? DIRECTION.toEnum(par) : cell.direction.next(par);
	}
	/** Возвращает параметр ДНК
	 * @param dna - днк, параметр который возвращаем
	 * @param numParam - номер параметра для текущей команды
	 * @param maxVal - максимальное значение, которым ограничивается параметр
	 * @return значение параметра [0, maxVal]
	 */
	protected static int param(DNA dna, int numParam, double maxVal) {
		return (int) Math.round(maxVal * param(dna,numParam) / CommandList.COUNT_COMAND);
	}
	/** Возвращает адрес, который получит PC, если перейдёт по этой ветви
	 * @param dna ДНК, которая смотрит на текущую команду
	 * @param numBranch номер ветви для текущей команды.
	 * @return значение PC [0,dna.size]
	 */
	protected int branch(DNA dna, int numBranch) {
		final var rAdr = 1 + getCountParams() + numBranch;
		final var val = dna.get(rAdr, false);
		return (val + dna.getPC()) % dna.size;
	}
	/** Проверяет на пустоту клетку поля
	 * @param cell клетка, которая интересуется
	 * @param point какая клетка проверяется
	 * @return true, если там пусто
	 * @deprecated Надо пользоваться встроенной функцией базового объекта
	 */
	@Deprecated
	private static boolean isEmpty(AliveCell cell, Point point){
		final var see = cell.see(point);
		return see == CellObject.OBJECT.CLEAN || see.groupLeader == CellObject.OBJECT.BANE;
	}
	/**
	 * Ищет пустое направление вокруг клетки
	 * @param cell - клетка
	 * @return ближайшая пустая точка или null
	 * @deprecated Надо пользоваться встроенной функцией базового объекта
	 */
	@Deprecated
	protected static Point findEmptyDirection(AliveCell cell) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
	    	if(i == 0 || i == 4) {
		        Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(i)));
		        if (isEmpty(cell,point))
		            return point;
	    	} else {
	    		int dir = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
	    		Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(dir)));
		        if (isEmpty(cell,point))
		            return point;
		        dir = -dir;
	    		point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(dir)));
		        if (isEmpty(cell,point))
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
	/** @return длинное название команды*/
	public String getLongName() {return longName;}
	/** @return короткое, символьное название команды*/
	public String getShotName() {return shotName;}
	/** @return количество параметров у команды. Это число кодонов за командой, который дают некоторые числа параметров*/
	public int getCountParams() {return countParams;}
	/** @return количество ветвей у команды. Это число кодонов за командой и её параметрами, которые кодируют адреса по которым перейдёт управление*/
	public int getCountBranch() {return countBranch;}
	/**Возвращает размер комады. Сколько кадонов она занимает.
	 * @return 1 (сама команда) + количество параметров + количество ветвей
	 */
	public int size(){return 1 + getCountParams() + getCountBranch();}

	@Override
	public String toString() {
		if (isFullMod())
			return getLongName();
		else
			return getShotName();
	}
	/**Некоторые функции могут вернуть описание того, как станет выглядеть клетка после её выполнения.
	 * Что в клетке уменьшится, что увеличится и т.д. Вот для этого и есть эта функция.
	 * ВНИМАНИЕ!!! Она может вернуть и null, если команде недостаточно данных или если команда не знает как точно
	 * клетка изменитсяпосле воздействия!
	 * @param cell клетка, функция которой исследуется
	 * @return Строковое описание функции или null, если ни какого описания нет
	 */
	protected String value(AliveCell cell) {return null;}
	
	/**Некоторые функции могут вернуть описание того, как станет выглядеть клетка после её выполнения.
	 * Что в клетке уменьшится, что увеличится и т.д. Вот для этого и есть эта функция.
	 * ВНИМАНИЕ!!! Она может вернуть и null, если команде недостаточно данных или если команда не знает как точно
	 * клетка изменитсяпосле воздействия!
	 * @param cell клетка, функция которой исследуется
	 * @param dna вспомогательная копия ДНК, которая указывает на текущую функцию ДНК, и которая будет использоваться
	 *			вместо ДНК из клетки. Так нужно, если, например, хочется исследовать какой-то участок ДНК,
	 *			но ДНК клетки трогать нельзя. Тогда можно передать вот эту копию ДНК и не переживать
	 * @return Строковое описание функции или null, если ни какого описания нет
	 */
	public String value(AliveCell cell, DNA dna) {
		return value(cell);
	}
	/** Возвращает описание параметра по переданному значению
	 * @param cell клетка, параметр который нам важен
	 * @param numParam номер этого параметра, считая от 0
	 * @param dna вспомогательная копия ДНК, которая указывает на текущую функцию ДНК, и которая будет использоваться
	 *			вместо ДНК из клетки. Так нужно, если, например, хочется исследовать какой-то участок ДНК,
	 *			но ДНК клетки трогать нельзя. Тогда можно передать вот эту копию ДНК и не переживать
	 * @return текстовое описание параметра
	 */
	public String getParam(AliveCell cell, int numParam, DNA dna){
		throw new UnsupportedOperationException("Забыл подписать параметр для " + toString() + " " + this.getClass());
	};
	/** Возвращает описание ветви, по которой может идти программа
	 * @param cell клетка, ветвь который нам важен
	 * @param numBranch номер этой ветви, считая от 0
	 * @param dna вспомогательная копия ДНК, которая указывает на текущую функцию ДНК, и которая будет использоваться
	 *			вместо ДНК из клетки. Так нужно, если, например, хочется исследовать какой-то участок ДНК,
	 *			но ДНК клетки трогать нельзя. Тогда можно передать вот эту копию ДНК и не переживать
	 * @return текстовое описание ветви
	 */
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		System.err.println("Забыл подписать ветвь для " + toString() + " " + this.getClass());
		StringBuilder sb = new StringBuilder();
		sb.append(" (");
		var atr = dna.get(1 + this.getCountParams() + numBranch, false);
		sb.append((dna.getPC() + atr) % dna.size);
		sb.append(")");
		return sb.toString();
	};
	/** Стандартное описание ветвей, если их две. И если у команды всего один параметр. 
	 * Ветви выбираются при этом в зависимости от параметра. Если он больше или равен некой константе, то ветв 1. Иначе ветвь 2.
	 * @param cell клетка
	 * @param numBranch номер ветви [0,1]
	 * @param dna ДНК клетки, указыющая на текущую команду
	 * @return текст с подписью, где первая ветвь - равен или больше параметра, а вторая ветвь - меньше параметра
	 *			V >= П ? 0 : 1;
	 */
	protected String branchMoreeLees(AliveCell cell, int numBranch, DNA dna) {
		return switch (numBranch) {
			case 0 -> parametrMoreOrEqual;
			case 1 -> parametrLess;
			default -> throw new IllegalArgumentException("Ветвь " + numBranch + " не может быть!");
		};
	}
	/** Стандартное описание ветвей, если их три. И если у команды всего один параметр. 
	 * Ветви выбираются при этом в зависимости от параметра. Если он больше или равен некой константе, то ветв 1.
	 * Если параметр не найден (например, у друга здоровья больше 5? Вот ветвь 3 - если друзей нет вообще)
	 * В остальных случаях - 2.
	 * @param cell клетка
	 * @param numBranch номер параметра [0,1,2]
	 * @param dna ДНК клетки, указыющая на текущую команду
	 * @return текст с подписью, где первая ветвь - равен или больше параметра, вторая ветвь - меньше параметра, а третья - параметр не определяется
	 * V == null ? 3 : (V >= П ? 0 : 1)
	 */
	protected String branchMoreeLeesEmpty(AliveCell cell, int numBranch, DNA dna) {
		return switch (numBranch) {
			case 0 -> parametrMoreOrEqual;
			case 1 -> parametrLess;
			case 2 -> "∅";
			default -> throw new IllegalArgumentException("Ветвь " + numBranch + " не может быть!");
		};
	}
	/** Возвращает стандартное описание для параметра, определяющего направление клетки.
	 * Уже учитывает - команда относительная или абсолютная
	 * @param cell клетка, чьё направление
	 * @param numParam номер параметра
	 * @param dna ДНК клетки, указыющая на текущую команду
	 * @return строка с описанием направления
	 */
	protected String getDirectionParam(AliveCell cell, int numParam, DNA dna){
		var dir = param(dna, cell, numParam, isAbolute);
        return isFullMod() ? 
				Configurations.getProperty(CommandDNA.class,"param.direction.L", dir.toSString()) 
				: Configurations.getProperty(CommandDNA.class,"param.direction.S", dir.toString());
	}
	
	/** @param isFullMod Сохраняет способ отображения параметров. true - полно, false - кратко*/
	public static void setFullMod(boolean isFullMod) {
		CommandDNA.isFullMod = isFullMod;
	}
	/** @return способ отображения информации. Если true, то будет печать полностью*/
	public static boolean isFullMod() {return isFullMod;}
	/** Возвращает номер сработавшего прерывания
	 * @param cell клетка, у которой срабатывает прерывание
	 * @param dna ДНК клетки, указыющая на текущую команду
	 * @return номер прерывания или -1. Ну а если прерываний нет, то выскочит ошибка - UnsupportedOperationException!
	 */
	public int getInterrupt(AliveCell cell, DNA dna) {
		throw new UnsupportedOperationException("Если у вас есть прерывание - будьте любезны его реализовать для " + toString() + " " + this.getClass());
	}
}
