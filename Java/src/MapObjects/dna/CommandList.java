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
	static {
		for(var i = 0 ; i < list.length ; i++)
			list[i] = get(i);
		
		//Небольшие тестики, после создания новой функции
		//Они не влияют на логику, но нужны для отоброжения
		var adam = new AliveCell();
		for (CommandDNA cmd : list) {
			try{
				if(cmd.isInterrupt())
					cmd.getInterrupt(adam, adam.getDna());
				String param = null;
				for (int j = 0; j < cmd.getCountParams(); j++) {
					String param2 = cmd.getParam(adam, j, adam.getDna());
					if(param2.equals(param))
						throw new RuntimeException("Параметры совпали для " + cmd);
					param = param2;
				}
			}catch(RuntimeException e){
				System.out.println(e);
			}
		}		
	}
	/**
	 * Функция преобразует порядковый номер команды в объект.
	 * Да, она обзательна. switch помогает отлавливать ситуацию, когда 
	 * blocki+k >=block(i+1)!!!!
	 * @param key порядковый номер команды
	 * @return
	 */
	private static CommandDNA get(int key) {
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

			case BLOCK_3_1 -> new HowMuch("HP", cell -> cell.getHealth(), false, AliveCellProtorype.MAX_HP);
			case BLOCK_3_1 + 1 -> new HowMuch("MP", cell -> cell.getMineral(), false, AliveCellProtorype.MAX_MP);
			case BLOCK_3_1 + 2 -> new HowMuch("Mucosa", cell -> cell.getMucosa(), false);
			case BLOCK_3_1 + 3 -> new HowMuch("FoodTank", cell -> cell.getFoodTank(), false, TankFood.TANK_SIZE);
			case BLOCK_3_1 + 4 -> new HowMuch("DW", cell -> cell.getDNA_wall(), false);
			case BLOCK_3_1 + 5 -> new HowMuch("Old", cell -> (cell.getAge() / 100), false);
			case BLOCK_3_1 + 6 -> new HowMuchSun();
			case BLOCK_3_1 + 7 -> new HowMuchMinerals();
			case BLOCK_3_1 + 8 -> new IAmMulticellular();
			case BLOCK_3_1 + 9 -> new HowHigh();
			case BLOCK_3_1 + 10 -> new IAmSurrounded();

			case BLOCK_3_2 + 0 -> new HowMuch("HP", cell -> cell.getHealth(), true, AliveCellProtorype.MAX_HP);
			case BLOCK_3_2 + 1 -> new HowMuch("MP", cell -> cell.getMineral(), true, AliveCellProtorype.MAX_MP);
			case BLOCK_3_2 + 2 -> new HowMuch("Mucosa", cell -> cell.getMucosa(), true);
			case BLOCK_3_2 + 3 -> new HowMuch("MineralTank", cell -> cell.getMineralTank(), false, TankMineral.TANK_SIZE);
			case BLOCK_3_2 + 4 -> new HowMuch("DW", cell -> cell.getDNA_wall(), true);
			case BLOCK_3_2 + 5 -> new FindNear();
			case BLOCK_3_2 + 6 -> new WhatMyPosion();
			case BLOCK_3_2 + 7 -> new See(true);
			case BLOCK_3_2 + 8 -> new See(false);
			case BLOCK_3_2 + 9 -> new WhoIsNearby();

			case BLOCK_4 -> new Eat(true);
			case BLOCK_4 + 1 -> new Eat(false);
			case BLOCK_4 + 2 -> new Bite(true);
			case BLOCK_4 + 3 -> new Bite(false);
			case BLOCK_4 + 4 -> new Care(true);
			case BLOCK_4 + 5 -> new Care(false);
			case BLOCK_4 + 6 -> new Give(true);
			case BLOCK_4 + 7 -> new Give(false);
			case BLOCK_4 + 8 -> new Pull(true);
			case BLOCK_4 + 9 -> new Pull(false);

			case BLOCK_5 -> new DNABreakNext();
			case BLOCK_5 + 1 -> new DNABreakNow();
			case BLOCK_5 + 2 -> new DNAInsert();
			case BLOCK_5 + 3 -> new DNACopy();
			case BLOCK_5 + 4 -> new DNAStrengthen();
			case BLOCK_5 + 5 -> new DNAWallBreak();
			case BLOCK_5 + 6 -> new Loop();
			case BLOCK_5 + 7 -> new ChangeSpecialization();
			case BLOCK_5 + 8 -> new DNABreakMy();

			case BLOCK_6 -> new Cling(true);
			case BLOCK_6 + 1 -> new Cling(false);
			case BLOCK_6 + 2 -> new Clone(true);
			case BLOCK_6 + 3 -> new Clone(false);
			case BLOCK_6 + 4 -> new TankFood(true);
			case BLOCK_6 + 5 -> new TankFood(false);
			case BLOCK_6 + 6 -> new TankMineral(true);
			case BLOCK_6 + 7 -> new TankMineral(false);
			case BLOCK_6 + 8 -> new Mucosa(true);
			case BLOCK_6 + 9 -> new Mucosa(false);

			default -> new Jump(); // Любая остальная команда - прыжок
		};
	}
}
