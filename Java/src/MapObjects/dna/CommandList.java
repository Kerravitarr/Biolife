package MapObjects.dna;

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
	public static final int block1 = COUNT_COMAND * 1 / 7;
	/**Команды движения*/
    public static final int block2 = COUNT_COMAND * 2 / 7;
	/**Команды исследования*/
    public static final int block3 = COUNT_COMAND * 3 / 7;
	/**Команды взимодействия*/
    public static final int block4 = COUNT_COMAND * 4 / 7;
	/**Команды программирования*/
    public static final int block5 = COUNT_COMAND * 5 / 7;
	/**Команды многоклеточности*/
    public static final int block6 = COUNT_COMAND * 6 / 7;
    
    public static final CommandDNA[] list = new CommandDNA[COUNT_COMAND+1];
	static {
		for(var i = 0 ; i < list.length ; i++)
			list[i] = get(i);
	}
	/**
	 * Функция преобразует порядковый номер команды в объект.
	 * Да, она обзательна. switch помогает отлавливать ситуацию, когда 
	 * blocki+k >=block(i+1)!!!!
	 * @param key порядковый номер команды
	 * @return
	 */
	private static CommandDNA get(int key) {
		switch (key) {
		case block1: return new Photosynthesis();
		case block1+1: return new Minerals2Energy();
		case block1+2: return new Birth();
		case block1+3: return new Destroy();
		case block1+4: return new CreatePoisonR();
		case block1+5: return new CreatePoisonA();
		case block1+6: return new Sleep();
		case block1+7: return new Buoyancy_UP();
		case block1+8: return new Buoyancy_DOWN();

		case block2: return new TurnAroundA();
		case block2+1: return new TurnAroundR();
		case block2+2: return new StepA();
		case block2+3: return new StepR();
		case block2+4: return new Align_UP();
		//TODO развернуться на 180 градусов
		
		case block3: return new SeeA();
		case block3+1: return new SeeR();
		case block3+2: return new HowHigh();
		case block3+3: return new HowMuchHP();
		case block3+4: return new HowMuchMP();
		case block3+5: return new IAmSurrounded();
		case block3+6: return new HowMuchSun();
		case block3+7: return new HowMuchMinerals();
		case block3+8: return new HowMuchHPTarget();
		case block3+9: return new HowMuchMPTarget();
		case block3+10: return new IAmMulticellular();
		case block3+11: return new HowOldIAm();
		case block3+12: return new HowMuchDW();
		//TODO Проверить - кто вокруг меня? Друзья, враги, кто?
		
		case block4: return new EatA();
		case block4+1: return new EatR();
		case block4+2: return new BiteA();
		case block4+3: return new BiteR();
		case block4+4: return new CareA();
		case block4+5: return new CareR();
		case block4+6: return new GiveA();
		case block4+7: return new GiveR();
		case block4+8: return new PullA();
		case block4+9: return new PullR();
		
		case block5: return new DNABreakNext();
		case block5+1: return new DNABreakNow();
		case block5+2: return new DNAInsert();
		case block5+3: return new DNACopy();
		case block5+4: return new DNAStrengthen();
		case block5+5: return new DNAWallBreak();
		case block5+6: return new DNALoop();
		
		case block6: return new ClingA();
		case block6+1: return new ClingR();
		case block6+2: return new CloneA();
		case block6+3: return new CloneR();
		
		default: return new Jump(); // Все неиспользуемые параметры заполняем пустотой
		}
	}
}
