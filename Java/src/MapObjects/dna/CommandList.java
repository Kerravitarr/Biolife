package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;

/**
 * Специальный класс, по фатку представляющий доступ к массиву со всеми возможными функциями из ДНК
 * Ну и связь их с индексом, разумеется
 * @author Kerravitarr
 *
 */
public class CommandList {
	/**Сколько вообще может быть команд*/
	public static final int COUNT_COMAND = 11*8; // 8 - DIRECTION.size()
	/*Команды разделены на ранвые блоки*/
	/**Команды общего назначения*/
	public static final int BLOCK_1 = COUNT_COMAND * 1 / 8;
	/**Команды движения*/
    public static final int BLOCK_2 = COUNT_COMAND * 2 / 8;
	/**Команды исследования*/
    public static final int BLOCK_3_1 = COUNT_COMAND * 3 / 8;
	/**Команды исследования*/
    public static final int BLOCK_3_2 = COUNT_COMAND * 4 / 8;
	/**Команды взимодействия*/
    public static final int BLOCK_4 = COUNT_COMAND * 5 / 8;
	/**Команды программирования*/
    public static final int BLOCK_5 = COUNT_COMAND * 6 / 8;
	/**Команды многоклеточности*/
    public static final int BLOCK_6 = COUNT_COMAND * 7 / 8;
    
    public static final CommandDNA[] list = new CommandDNA[COUNT_COMAND+1];
	static{
		for(var i = 0 ; i < CommandList.list.length ; i++)
			CommandList.list[i] = CommandList.get(i);
	}
	/**
	 * Функция преобразует порядковый номер команды в объект.
	 * Да, она обзательна. switch помогает отлавливать ситуацию, когда 
	 * blocki+k >=block(i+1)!!!!
	 * @param key порядковый номер команды
	 * @return
	 */
	protected static CommandDNA get(int key) {
		return switch (key) {
			case BLOCK_1 -> new Photosynthesis();
			case BLOCK_1 + 1 -> new Minerals2Energy();
			case BLOCK_1 + 2 -> new Birth();
			case BLOCK_1 + 3 -> new Destroy();
			case BLOCK_1 + 4 -> new CreatePoison(true);
			case BLOCK_1 + 5 -> new CreatePoison(false);
			case BLOCK_1 + 6 -> new Sleep();
			case BLOCK_1 + 7 -> new Buoyancy(true);
			case BLOCK_1 + 8 -> new Buoyancy(false);
			case BLOCK_1 + 9 -> new ToWall();

			case BLOCK_2 -> new TurnAround(true);
			case BLOCK_2 + 1 -> new TurnAround(false);
			case BLOCK_2 + 2 -> new Step(true);
			case BLOCK_2 + 3 -> new Step(false);
			case BLOCK_2 + 4 -> new Align_UP();
			case BLOCK_2 + 5 -> new Reversal();
			case BLOCK_2 + 6 -> new TurnTo();
			case BLOCK_2 + 7 -> new Swap(true);
			case BLOCK_2 + 8 -> new Swap(false);

			case BLOCK_3_1 -> new HowMuch("HP", false,cell -> cell.getHealth(),  AliveCellProtorype.MAX_HP);
			case BLOCK_3_1 + 1 -> new HowMuch("MP", false, cell -> cell.getMineral(), AliveCellProtorype.MAX_MP);
			case BLOCK_3_1 + 2 -> new HowMuch("Mucosa", false, cell -> cell.getMucosa());
			case BLOCK_3_1 + 3 -> new HowMuch("FoodTank", false, cell -> cell.getFoodTank(), TankFood.TANK_SIZE);
			case BLOCK_3_1 + 4 -> new HowMuch("DW",  false,cell -> cell.getDNA_wall());
			case BLOCK_3_1 + 5 -> new HowMuch("Old", false, cell -> (cell.getAge() / 100));
			case BLOCK_3_1 + 6 -> new HowMuch("Sun",  false,cell -> cell.sunAround());
			case BLOCK_3_1 + 7 -> new HowMuch("MA",  false,cell -> cell.mineralAround());
			case BLOCK_3_1 + 8 -> new IAmMulticellular();
			case BLOCK_3_1 + 9 -> new HowHigh();
			case BLOCK_3_1 + 10 -> new IAmSurrounded();

			case BLOCK_3_2 + 0 -> new HowMuch("HP",true, cell -> cell.getHealth(),  AliveCellProtorype.MAX_HP);
			case BLOCK_3_2 + 1 -> new HowMuch("MP",true, cell -> cell.getMineral(), AliveCellProtorype.MAX_MP);
			case BLOCK_3_2 + 2 -> new HowMuch("Mucosa", true,cell -> cell.getMucosa());
			case BLOCK_3_2 + 3 -> new HowMuch("MineralTank",false,  cell -> cell.getMineralTank(), TankMineral.TANK_SIZE);
			case BLOCK_3_2 + 4 -> new HowMuch("DW", true,cell -> cell.getDNA_wall());
			case BLOCK_3_2 + 5 -> new FindNear();
			case BLOCK_3_2 + 6 -> new WhatMyPosion();
			case BLOCK_3_2 + 7 -> new See(true);
			case BLOCK_3_2 + 8 -> new See(false);
			case BLOCK_3_2 + 9 -> new WhoIsNearby();

			case BLOCK_4 + 0 -> new Eat(true);
			case BLOCK_4 + 1 -> new Eat(false);
			//case BLOCK_4 + 2 -> new Bite(true);
			//case BLOCK_4 + 3 -> new Bite(false);
			case BLOCK_4 + 4 -> new Care(true);
			case BLOCK_4 + 5 -> new Care(false);
			case BLOCK_4 + 6 -> new Give(true);
			case BLOCK_4 + 7 -> new Give(false);
			case BLOCK_4 + 8 -> new Pull(true);
			case BLOCK_4 + 9 -> new Pull(false);

			case BLOCK_5 + 0 -> new DNABreak(true, true);
			case BLOCK_5 + 1 -> new DNABreak(true, false);
			case BLOCK_5 + 2 -> new DNABreak(false, true);
			case BLOCK_5 + 3 -> new DNABreak(false, false);
			case BLOCK_5 + 4 -> new DNAStrengthen();
			case BLOCK_5 + 5 -> new DNAWallBreak();
			//case BLOCK_5 + 6 -> 
			case BLOCK_5 + 7 -> new ChangeSpecialization();
			case BLOCK_5 + 8 -> new DNAFind(false);
			case BLOCK_5 + 9 -> new DNAFind(true);
			case BLOCK_5 + 10 -> new ViralLysis();

			//case BLOCK_6 + 0 -> new Cling(true); Из за этого мир превращается в большой бессмысленный и беспощадный лес
			//case BLOCK_6 + 1 -> new Cling(false);
			case BLOCK_6 + 2 -> new Clone(true);
			case BLOCK_6 + 3 -> new Clone(false);
			case BLOCK_6 + 4 -> new TankFood(true);
			case BLOCK_6 + 5 -> new TankFood(false);
			case BLOCK_6 + 6 -> new TankMineral(true);
			case BLOCK_6 + 7 -> new TankMineral(false);
			case BLOCK_6 + 8 -> new Mucosa(true);
			case BLOCK_6 + 9 -> new Mucosa(false);

			default -> key % 2 == 0 ? new Jump(key) : new Loop(key); // Любая остальная команда - прыжок
		};
	}
}
