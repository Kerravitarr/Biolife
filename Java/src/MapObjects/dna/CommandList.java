package MapObjects.dna;

import MapObjects.AliveCell;

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
			case BLOCK_1+1 -> new Minerals2Energy();
			case BLOCK_1+2 -> new Birth();
			case BLOCK_1+3 -> new Destroy();
			case BLOCK_1+4 -> new CreatePoisonR();
			case BLOCK_1+5 -> new CreatePoisonA();
			case BLOCK_1+6 -> new Sleep();
			case BLOCK_1+7 -> new Buoyancy_UP();
			case BLOCK_1+8 -> new Buoyancy_DOWN();
			case BLOCK_1+9 -> new ToWall();
				
			case BLOCK_2 -> new TurnAroundA();
			case BLOCK_2+1 -> new TurnAroundR();
			case BLOCK_2+2 -> new StepA();
			case BLOCK_2+3 -> new StepR();
			case BLOCK_2+4 -> new Align_UP();
			case BLOCK_2+5 -> new Reversal();
			case BLOCK_2+6 -> new TurnToEnemy();
			case BLOCK_2+7 -> new TurnToFriend();
			case BLOCK_2+8 -> new TurnToPoison();
			case BLOCK_2+9 -> new TurnToMedicament();
				
			case BLOCK_3_1 -> new SeeA();
			case BLOCK_3_1+1 -> new SeeR();
			case BLOCK_3_1+2 -> new HowHigh();
			case BLOCK_3_1+3 -> new HowMuchHP();
			case BLOCK_3_1+4 -> new HowMuchMP();
			case BLOCK_3_1+5 -> new IAmSurrounded();
			case BLOCK_3_1+6 -> new HowMuchSun();
			case BLOCK_3_1+7 -> new HowMuchMinerals();
			case BLOCK_3_1+8 -> new HowMuchHPTarget();
				
			case BLOCK_3_2+0 -> new HowMuchMPTarget();
			case BLOCK_3_2+1 -> new IAmMulticellular();
			case BLOCK_3_2+2 -> new HowOldIAm();
			case BLOCK_3_2+3 -> new HowMuchDW();
			case BLOCK_3_2+4 -> new WhoIsNearby();
			case BLOCK_3_2+5 -> new EnemyNear();
			case BLOCK_3_2+6 -> new FriendNear();
			case BLOCK_3_2+7 -> new PosionNear();
			case BLOCK_3_2+8-> new MedicamentNear();
			case BLOCK_3_2+9-> new OrganicNear();
			case BLOCK_3_2+10-> new WhatMyPosion();
				
			case BLOCK_4 -> new EatA();
			case BLOCK_4+1 -> new EatR();
			case BLOCK_4+2 -> new BiteA();
			case BLOCK_4+3 -> new BiteR();
			case BLOCK_4+4 -> new CareA();
			case BLOCK_4+5 -> new CareR();
			case BLOCK_4+6 -> new GiveA();
			case BLOCK_4+7 -> new GiveR();
			case BLOCK_4+8 -> new PullA();
			case BLOCK_4+9 -> new PullR();
				
			case BLOCK_5 -> new DNABreakNext();
			case BLOCK_5+1 -> new DNABreakNow();
			case BLOCK_5+2 -> new DNAInsert();
			case BLOCK_5+3 -> new DNACopy();
			case BLOCK_5+4 -> new DNAStrengthen();
			case BLOCK_5+5 -> new DNAWallBreak();
			case BLOCK_5+6 -> new Loop();
				
			case BLOCK_6 -> new ClingA();
			case BLOCK_6+1 -> new ClingR();
			case BLOCK_6+2 -> new CloneA();
			case BLOCK_6+3 -> new CloneR();
			default -> new Jump();
		}; //TODO Проверить - кто вокруг меня? Друзья, враги, кто?
	}
}
