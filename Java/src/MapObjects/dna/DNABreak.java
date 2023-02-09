package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.NOT_POISON;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.CellObject;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.FRIEND;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.OWALL;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;
import static MapObjects.dna.CommandDNA.nextPoint;
import static MapObjects.dna.CommandDNA.param;
import Utils.MyMessageFormat;
import main.Configurations;
import main.Point;

/**
 * TODO
 * Сравнить ген
 * Родить потомка с фрагментом ДНК
 * 
 */
/**
 * Базовая основа команды, которая изменяет ДНК.
 * Все эти команды "бесплатны", так как вирсная клетка умирает после своих телодвижений
 * @author Kerravitarr
 *
 */
public class DNABreak extends CommandDo {
	/**Цена энергии на ход, только для вставки одного значения*/
	private final int HP_COST = 100;
	private final MyMessageFormat oneParam0ormat = new MyMessageFormat("🔍CMD = {0}");
	private final MyMessageFormat oneParam1ormat = new MyMessageFormat("CMD = {0}");
	private final MyMessageFormat manyParam1ormat = new MyMessageFormat("L = {0}");
	private final MyMessageFormat manyParam2ormat = new MyMessageFormat("PC -= {0}");
	
	private final String manyValueFormatS = Configurations.getProperty(Destroy.class,  "Shot");
	private final String manyValueFormatL = Configurations.getProperty(Destroy.class,  "Long");
	private final MyMessageFormat manyValueFormat = new MyMessageFormat("HP -= {0}");
	
	/**Операция вставки? Или обновления*/
	private final boolean isInsert;
	/**Одиночная команда? Или для ряда чисел*/
	private final boolean isOne;
	/**
	 * Констурктор класса
	 * @param isO - одиноная команда или множественная
	 * @param isIns - вставить или обновить
	 */
	public DNABreak(boolean isO, boolean isIns) {
		super(isO ? 2 : 3, isO && isIns ? "OneInsert" : (!isO && isIns ? "ManyInsert" : (isO && !isIns ? "OneUpdate" : "ManyUpdate")));
		isInterrupt = true;
		isOne = isO;
		isInsert = isIns;
	}
	
	@Override
	protected void doing(AliveCell cell) {
		CellObject.OBJECT see = cell.see(cell.direction);
		switch (see) {
			case ENEMY, FRIEND -> {
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				if(isOne){
					int cmdStart = param(cell,0); // После какого гена мы устраиваем подлянку
					int mc = param(cell,1); //Какой ген меняем
					if(isInsert)
						insertCmd(cell,bot, true,cmdStart,mc);
					else
						updateCmd(cell,bot, true,cmdStart,mc);
					//Теряет бот на этом колосальное количество энергии
					cell.addHealth( - HP_COST);
				} else {
					int cmdStart = param(cell,0); // После какого гена мы устраиваем подлянку
					int length_DNA = param(cell,1, cell.getDna().size - 1) + 1; // Сколько вставляем
					int pref = param(cell,2); //Сколько генов отступаем назад
					int[] cmds = new int[length_DNA];
					var dna = cell.getDna();
					for(int i = 0 ; i < length_DNA ; i++){
						cmds[i] = dna.get(0, dna.getPC() - pref + i);
					}
					if(isInsert)
						insertCmds(cell,bot, true,cmdStart,cmds);
					else
						updateCmds(cell,bot, true,cmdStart,cmds);
					//Мы сделали достаточно, закругляемся на этом
					cell.destroy();
				}
			}
			case NOT_POISON, ORGANIC, POISON, WALL, CLEAN, OWALL -> cell.getDna().interrupt(cell, see.nextCMD);
			case BOT -> throw new IllegalArgumentException("Unexpected value: " + see);
		}
	}
	
	/**
	 * Ищет позицию в ДНК следующую сразу за cmd
	 * @param dna ДНК в которой ищем
	 * @param cmd команда, которую ищем
	 * @return индекс команды в формате ДНК[dna.getPC() + find(cmd) + 1];
	 *			или -1, если команда не найдена
	 */
	public static int findPos(DNA dna, int cmd){
		for (var i = 0; i < dna.size; i++) {
			if (dna.get(dna.getPC(), i) == cmd) {
				return (dna.getPC() + i + 1) % dna.size;
			}
		}
		return -1;
	}
	/**Обновляет поколение target за счёт who*/
	private void updateGeneration(AliveCell who, AliveCell target){
			target.setGeneration(target.getGeneration() + 1);
			target.evolutionNode = target.evolutionNode.newNode(target, who.getStepCount());
	}
	
	/**
	 * Вставляет дополнительную инструкцию в ДНК жертвы
	 * @param who кто ломает
	 * @param target кому ломает
	 * @param breakWall сломать-ли стену перед этим
	 * @param cmdStart после какой команды вставить свою команду
	 * @param comand какую команду вставить
	 */
	protected void insertCmd(AliveCell who,AliveCell target, boolean breakWall, int cmdStart, int comand) {
		insertCmds(who,target,breakWall,cmdStart,new int[]{comand});
	}
	/**
	 * Вставляет несколько дополнительных инструкций в ДНК жертвы
	 * @param who кто ломает
	 * @param target кому ломает
	 * @param breakWall сломать-ли стену перед этим
	 * @param cmdStart после какой команды вставить свою команду
	 * @param comands список вставляеых команд
	 */
	protected void insertCmds(AliveCell who,AliveCell target, boolean breakWall, int cmdStart, int[] comands) {
		if (breakWall && target.getDNA_wall() > 0) {
			target.setDNA_wall(target.getDNA_wall()-1);
		} else {
			var dna = target.getDna();
			var index = findPos(dna,cmdStart);
			if(index == -1 || dna.size + comands.length >= AliveCellProtorype.MAX_MINDE_SIZE) return;
			dna = dna.doubling(index, comands.length);
			for(var iCmd = 0 ; iCmd < comands.length ; iCmd++)
				dna.criticalUpdate(index - dna.getPC() + iCmd, comands[iCmd]);
			target.setDna(dna);
			updateGeneration(target,who);
		}
	}
	/**
	 * Обновляет инструкцию в ДНК жертвы
	 * @param who кто ломает
	 * @param target кому ломает
	 * @param breakWall сломать-ли стену перед этим
	 * @param cmdStart после какой команды вставить свою команду
	 * @param comand какую команду вставить
	 */
	protected void updateCmd(AliveCell who,AliveCell target, boolean breakWall, int cmdStart, int comand) {
		updateCmds(who,target,breakWall,cmdStart,new int[]{comand});
	}
	/**
	 * Обновляет несколько инструкций в ДНК жертвы
	 * @param who кто ломает
	 * @param target кому ломает
	 * @param breakWall сломать-ли стену перед этим
	 * @param cmdStart после какой команды вставить свою команду
	 * @param comands последовательность команд
	 */
	protected void updateCmds(AliveCell who,AliveCell target, boolean breakWall, int cmdStart, int[] comands) {
		if (breakWall && target.getDNA_wall() > 0) {
			target.setDNA_wall(target.getDNA_wall()-1);
		} else {
			var dna = target.getDna();
			var index = findPos(dna,cmdStart);
			if(index == -1) return;
			dna = dna.update(index - dna.getPC(), comands[0]); //Создаёт копию ДНК
			for(var iCmd = 0 ; iCmd < comands.length ; iCmd++)
				dna.criticalUpdate(index - dna.getPC() + iCmd, comands[iCmd]);
			target.setDna(dna);
			updateGeneration(target,who);
		}
	}

	
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){
		var see = cell.see(cell.direction);
		if (see == CLEAN || see == NOT_POISON || see == ORGANIC || see == POISON || see == WALL || see == OWALL)
			return see.nextCMD;
		else
			return -1;
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(isOne){
			return switch (numParam) {
				case 0 -> oneParam0ormat.format(CommandList.list[param(dna, numParam)]);
				case 1 -> oneParam1ormat.format(CommandList.list[param(dna, numParam)]);
				default-> super.getParam(cell, numParam, dna);
			};
		} else {
			return switch (numParam) {
				case 0 -> oneParam0ormat.format(CommandList.list[param(dna, numParam)]);
				case 1 -> manyParam1ormat.format(param(dna,0, dna.size - 1) + 1);
				case 2 -> manyParam2ormat.format(param(dna,0, dna.size));
				default-> super.getParam(cell, numParam, dna);
			};
		}
	}
	
	@Override
	public String value(AliveCell cell, DNA dna) {
		if (isOne) {
			return manyValueFormat.format(HP_COST);
		} else {
			return isFullMod() ? manyValueFormatL : manyValueFormatS;
		}
	}
}
