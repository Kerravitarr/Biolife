package MapObjects.dna;


import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.CellObject;
import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.OWALL;
import static MapObjects.CellObject.OBJECT.WALL;
import static MapObjects.dna.CommandDNA.nextPoint;
import static MapObjects.dna.CommandDNA.param;
import Utils.MyMessageFormat;
import Calculations.Configurations;
import Calculations.Point;

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
			case FRIEND,ENEMY -> {
				Point point = nextPoint(cell,cell.direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				if(isOne){
					if(cell.getHealth() + cell.getFoodTank() < HP_COST){
						bot.addHealth(cell.getHealth() + cell.getFoodTank());
						cell.destroy();
					}
					int cmdStart = param(cell,0); // После какого гена мы устраиваем подлянку
					int mc = param(cell,1); //Какой ген меняем
					if(isInsert)
						insertCmd(cell,bot, true,cmdStart,mc);
					else
						updateCmd(cell,bot, true,cmdStart,mc);
					//Теряет бот на этом колосальное количество энергии
					bot.addHealth(HP_COST);
					cell.destroy();
				} else {
					int cmdStart = param(cell,0); // После какого гена мы устраиваем подлянку
					int length_DNA = param(cell,1, cell.getDna().size - 1) + 1; // Сколько вставляем
					int pref = param(cell,2); //Сколько генов отступаем назад
					var dna = cell.getDna();
					final var cmds = dna.subDNA(- pref, false, Math.min(length_DNA,bot.getDna().size));
					if(isInsert)
						insertCmds(cell,bot, true,cmdStart,cmds);
					else
						updateCmds(cell,bot, true,cmdStart,cmds);
					bot.addHealth(cell.getHealth() + cell.getFoodTank());
					//Мы сделали достаточно, закругляемся на этом
					cell.destroy();
				}
			}
			case POISON,NOT_POISON, ORGANIC, WALL, CLEAN, OWALL,CONNECTION,FILLING -> cell.getDna().interrupt(cell, see);
			default -> throw new IllegalArgumentException("Unexpected value: " + see);
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
			if (dna.get(i, false) == cmd) {
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
			target.setDna(dna.insert(index, true, comands));
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
			target.setDna(dna.update(index, true, comands));
			updateGeneration(target,who);
		}
	}

	
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){
		var see = cell.see(cell.direction);
		if (see == CLEAN || see.groupLeader == CellObject.OBJECT.BANE || see == ORGANIC || see == WALL || see == OWALL || see == CellObject.OBJECT.CONNECTION || see == CellObject.OBJECT.FILLING)
			return see.ordinal();
		else
			return -1;
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(isOne){
			return switch (numParam) {
				case 0 -> Configurations.getProperty(DNABreak.class,isFullMod() ? "param0.L" : "param0.S", CommandList.list[param(dna, numParam)]);
				case 1 -> Configurations.getProperty(DNABreak.class,isFullMod() ? "param1.O.L" : "param1.O.S", CommandList.list[param(dna, numParam)]);
				default-> super.getParam(cell, numParam, dna);
			};
		} else {
			return switch (numParam) {
				case 0 -> Configurations.getProperty(DNABreak.class,isFullMod() ? "param0.L" : "param0.S", CommandList.list[param(dna, numParam)]);
				case 1 -> Configurations.getProperty(DNABreak.class,isFullMod() ? "param1.M.L" : "param1.M.S", param(dna,numParam, dna.size - 1) + 1);
				case 2 -> Configurations.getProperty(DNABreak.class,isFullMod() ? "param2.M.L" : "param2.M.S", dna.normalization(dna.getPC() - param(dna,numParam, dna.size)));
				default-> super.getParam(cell, numParam, dna);
			};
		}
	}
	
	@Override
	public String value(AliveCell cell, DNA dna) {
		final var sym = Configurations.getProperty(DNABreak.class, "value" + (isInsert ? ".I" : ".U") +(isFullMod() ? ".L" : ".S"));
		if (isOne) {
			final var p0 = CommandList.list[param(dna, 0)];
			final var p1 = CommandList.list[param(dna, 1)];
			return Configurations.getProperty(DNABreak.class,isFullMod() ? "value.O.L" : "value.O.S", p0,p1,sym);
		} else {
			final var p0 = CommandList.list[param(dna, 0)];
			final var p1 = param(dna,1, dna.size - 1) + 1;
			final var p2 = dna.normalization(dna.getPC() - param(dna,2, dna.size));
			return Configurations.getProperty(DNABreak.class,isFullMod() ? "value.M.L" : "value.M.S", p0,p1,p2,sym);
		}
	}
}
