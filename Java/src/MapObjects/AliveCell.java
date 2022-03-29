package MapObjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import MapObjects.Poison.TYPE;
import Utils.JSON;
import Utils.Utils;
import main.Configurations;
import main.EvolutionTree;
import main.EvolutionTree.Node;
import main.Point;
import main.Point.DIRECTION;
import panels.Legend;
public class AliveCell extends CellObject{
	//КОНСТАНТЫ
	/**Размер мозга изначальный*/
	public static final int DEF_MINDE_SIZE = 64;
	/**Размер мозга максимальный, чтобы небыло взрывного роста и поедания памяти*/
	public static final int MAX_MINDE_SIZE = 1024;
	/**Сколько вообще может быть команд*/
	public static final int COUNT_COMAND = 11*8; // 8 - DIRECTION.size()
	/**Начальный уровень здоровья клеток*/
	private static final int StartHP = 5;
	/**Начальный уровень минералов клеток*/
	private static final int StartMP = 5;
	/**Сколько нужно жизней для размножения, по умолчанию*/
	public static final int maxHP = 999;
	/**Сколько можно сохранить минералов*/
	public static final int MAX_MP = 999;
	/**На сколько организм тяготеет к фотосинтезу (0-4)*/
	private static final double defFotosin = 2;
	/**Столько энергии тратит бот на размножение*/
	private static final long HP_FOR_DOUBLE = 150;
	/**Столько ХП забирается на укрепление связей ДНК*/
	private static final long HP_TO_DNA_WALL = 1;
	/**Столько максимум может быть укреплений у ДНК*/
	public static final int MAX_DNA_WALL = 30;
	/**Столько энергии тратит бот на выделение яда, причём 2/3 этого числа идут яду, 1/3 сгорает*/
	private static final long HP_FOR_POISON = 20;
	/**Столько здоровья требуется клетке для жизни на ход*/
	private static final long HP_PER_STEP = 4;
	
	/**ДНК бота*/
	public class DNA {
		/**Размер мозга*/
		public final int size;
		/**Мозг*/
		public final int [] mind;
		/**Номер выполняемой инструкции*/
		private int instruction;
		
		/**
		 * Вектор прерываний:
		 * 0-(OBJECT.size()-1) по зрению. То есть если клетка не может совершить действие, потому что там стена (WALL(1)),
		 * то выполнится инструкция, адрес который записан в этом массиве на месте [1]
		 */
		public final int [] interrupts = new int[(OBJECT.size()-1)]; 
		/**Флаг нахождения в прерывании*/
		private boolean activInterrupt = false;
		
		private DNA(int size){
			this.size=size;
			mind = new int[this.size];
			for (int i = 0; i < mind.length; i++) {
				mind[i] = block1; // У клетки по базе только одна функция - фотосинтез
			}
			instruction = 0;
			for (int i = 0; i < interrupts.length; i++)
				interrupts[i] = 0;
		}
		/**ДНК у нас неизменяемая, поэтому при копировании мы можем сослаться на старую версию*/
		private DNA(DNA dna){
			this.size=dna.size;
			this.mind=dna.mind;
			this.instruction=dna.instruction;
			for (int i = 0; i < this.interrupts.length; i++)
				this.interrupts[i] = dna.interrupts[i];
		}
		private DNA(JSON dna) {
			this.size=dna.getI("size");
	    	List<Integer> mindL = dna.getA("mind");
	    	mind = new int[size];
	    	for (int i = 0; i < size; i++) 
				mind[i] = mindL.get(i);
			this.instruction=dna.getI("instruction");
	    	List<Integer> interruptsL = dna.getA("interrupts");
	    	for (int i = 0; i < interrupts.length; i++) 
	    		interrupts[i] = interruptsL.get(i);
		}
		/**А вот теперь ссылаться поздно - нам нужно поменять данные*/
		public DNA(DNA dna, int index, int value) {
			this.size=dna.size;
			this.mind = new int[dna.size];
	    	System.arraycopy(dna.mind, 0, mind, 0, size);
			this.instruction=dna.instruction;
	    	for (int i = 0; i < interrupts.length; i++) 
				this.interrupts[i] = dna.interrupts[i];
			//Изменение гена
			this.mind[getIndex(index)] = value;
		}
		
		/**
		 * Прерывание
		 * @param num - его номер
		 */
		private void interrupt(int num) {
			if(activInterrupt) return;
			activInterrupt = true;
			int stackInstr = instruction; // Кладём в стек PC
			instruction = interrupts[num] % size;
			for (int cyc = 0; (cyc < 15); cyc++)
				if(execute()) break; // Выполняем программу прерывания
			instruction = stackInstr; // Возвращаем из стека PC
			activInterrupt = false;
		}
		
		private int getIndex(int offset) {
			int ret = (instruction + offset) % size;
	        if(ret < 0)
	        	ret += size;
	        return ret;
		};
		/**Возвращает текущую инстуркцию*/
		private int get() {
			return mind[instruction];
		}
		/**Возвращает инстуркцию по смещению*/
		private int get(int index) {
			return mind[getIndex(index)];
		}
		/**
		 * Получает параметр функции
		 * @param i номер параметра
		 * @return
		 */
		private int param(int index) {
			return get(index + 1); //Потому что нулевой параметр идёт сразу за командой
		}
		private int param(int index, double maxVal) {
			return (int) Math.round(maxVal * param(index) / COUNT_COMAND);
		}
		/**
		 * Передвигает указатель на команду вперёд
		 * @param offset - смещение
		 */
		private void next(int offset) {
	        instruction = getIndex(offset);
		}
		/**
		 * Передвигает указатель на команду вперёд
		 * @param absoluteAdr - адрес в памяти, где лежит число на которое надо сдвинуть указатель
		 */
		private void nextFromAdr(int absoluteAdr) {
			next(get(absoluteAdr));
		}
		/**
		 * Обновляет значение гена в геноме
		 * @param index индекс в гене, причём 0 - под процессором
		 * @param value - значение, на которое надо заменить
		 * @return ДНК с обнавлёнными значениями
		 */
		private DNA update(int index, int value) {
			return new DNA(this,index,value);
		}
		private JSON toJSON() {
			JSON make = new JSON();
			make.add("size", size);
			make.add("mind", mind);
			make.add("instruction", instruction);
			make.add("interrupts", interrupts);
			return make;
		}
		/**
		 * Создаёт новую ДНК с удвоенным геном по адресу
		 * @param index - удваиваемый индекс, абсолютный
		 * @return
		 */
		private DNA doubling(int index) {
			DNA ret = new DNA(size+1);
        	System.arraycopy(mind, 0, ret.mind, 0, index+1);
        	ret.mind[index+1] = ret.mind[index]; //Дублирование гена
        	System.arraycopy(mind, index+1, ret.mind, index+2, size - 1 - index);
        	if(ret.instruction > index) {
        		ret.next(1); // Наш тактовый счётчик идёт дальше
        		for (int i = 0; i < interrupts.length; i++) {
					if(interrupts[i] > index) interrupts[i] = (interrupts[i]+1)%ret.size; // И все прерывания тоже сдвигаются
				}
        	}
			return ret;
		}
		/**
		 * Сжимает ДНК, удаляя из неё один ген
		 * @param index - удаляемый индекс, абсолютный
		 * @return
		 */
		private DNA compression(int index) {
			DNA ret = new DNA(size-1);
        	System.arraycopy(mind, 0, ret.mind, 0, index);
        	System.arraycopy(mind, index+1, ret.mind, index, size - 1 - index);
        	if(ret.instruction >= index) {
        		ret.next(-1);
        		for (int i = 0; i < interrupts.length; i++) {
					if(interrupts[i] >= index) interrupts[i] = Math.max(0,interrupts[i]-1); // И все прерывания тоже сдвигаются
				}
        	}
			return ret;
		}
		public int getIndex() {
			return instruction;
		}
		public int get(int instruction, int index) {
			int pos = (instruction+index) % size;
			if(pos < 0)
				pos += size;
			return mind[pos];
		}
	}
	
	//=================Внутреннее состояние бота
	/**Мозг*/
	private DNA dna;
    /**Жизни*/
    private double health = StartHP;
    //Минералы
    private long mineral = StartMP;
    /**Направление движения*/
    public Point.DIRECTION direction = Point.DIRECTION.UP;
    //Защитный покров ДНК, он мешает изменить Нашу ДНК
    private int DNA_wall = 0;
    /**Тип яда к которому клетка устойчива*/
    private Poison.TYPE poisonType = Poison.TYPE.НЕТ;
    /**Сила устойчивости к яду*/
    private int poisonPower = 0;
    /**Плавучесть. Меняется от -10 до 10 Где -10 - тонуть каждый ход, 10 - всплывать каждый ход, 1 - тонуть каждые 10 ходов*/
    private int buoyancy = 0;
    /**Специальный флаг, показывает, что бот на этом ходу спит*/
    private boolean isSleep = false;
    
    //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
    /**Поколение (мутационное)*/
    private int Generation = 0;
    /**Фенотип бота*/
    public Color phenotype = new Color(128,128,128);
    //Показывает на сколько организм тяготеет к фотосинтезу
    public double photosynthesisEffect = defFotosin;
    /**Дерево эволюции*/
    public Node evolutionNode = null;
    
    //===============Параметры братства, многоклеточность=======
    private Map<Point,AliveCell> friends = new HashMap<>();
    
    /**
     * Создание клетки без рода и племени
     */
    public AliveCell(){
    	super(-1, LV_STATUS.LV_ALIVE);
    	setPos(new Point(Utils.random(0, Configurations.MAP_CELLS.width-1),Utils.random(0, Configurations.MAP_CELLS.height-1)));
    	dna = new DNA(DEF_MINDE_SIZE);
    	color_DO = new Color(255,255,255);
		evolutionNode = EvolutionTree.root;
		
    }

    /**
     * Загрузка клетки
     * @param cell - JSON объект, который содержит всю информацюи о клетке
     * @param tree - Дерево эволюции 
     */
    public AliveCell(JSON cell, EvolutionTree tree) {
    	super(cell);
    	dna = new DNA(cell.getJ("DNA"));
    	health = cell.get("health");
    	mineral = cell.getL("mineral");
    	direction = DIRECTION.toEnum(cell.getI("direction"));
    	DNA_wall = cell.getI("DNA_wall");
    	poisonType =  Poison.TYPE.toEnum(cell.getI("posionType"));
    	poisonPower = cell.getI("posionPower");
    	
    	Generation = cell.getI("Generation");
    	phenotype = new Color((Long.decode("0x"+cell.get("phenotype"))).intValue(),true);
    	photosynthesisEffect = cell.get("photosynthesisEffect");
    	
    	evolutionNode = tree.getNode(cell.get("GenerationTree"));
    	
    	color_DO = new Color(255,255,255);
	}

    /**
     * Копирование клетки
     * @param cell - её родитель
     */
	public AliveCell(AliveCell cell, Point newPos) {
    	super(cell.stepCount, LV_STATUS.LV_ALIVE);
		setPos(newPos);
	    evolutionNode = cell.evolutionNode.clone();
	    
	    setHealth(cell.getHealth() / 2);   // забирается половина здоровья у предка
	    cell.setHealth(cell.getHealth() / 2);
	    setMineral(cell.getMineral() / 2); // забирается половина минералов у предка
	    cell.setMineral(cell.getMineral() / 2);
	    DNA_wall = cell.DNA_wall /2;
	    cell.DNA_wall = cell.DNA_wall / 2; //Забирается половина защиты ДНК
	    poisonType = cell.getPosionType();
		poisonPower = cell.getPosionPower(); // Тип и степень защищённости у клеток сохраняются
	
	    phenotype = new Color(cell.phenotype.getRGB(),true);   // цвет такой же, как у предка
	    direction = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));   // направление, куда повернут новорожденный, генерируется случайно
	
	    dna = new DNA(cell.dna);
	    
	    photosynthesisEffect = cell.photosynthesisEffect;
	    setGeneration(cell.Generation);
	    color_DO = new Color(255,255,255,evolutionNode.getAlpha());
	    repaint();
	    
	    //Мы на столько хорошо скопировали нашего родителя, что есть небольшой шанс накосячить - мутации
	    if (Math.random() < Configurations.AGGRESSIVE_ENVIRONMENT) 
	        mutation();
	}

	public static final int block1 = COUNT_COMAND * 1 / 7;
    public static final int block2 = COUNT_COMAND * 2 / 7;
    public static final int block3 = COUNT_COMAND * 3 / 7;
    public static final int block4 = COUNT_COMAND * 4 / 7;
    public static final int block5 = COUNT_COMAND * 5 / 7;
    public static final int block6 = COUNT_COMAND * 6 / 7;
    
    /**
     * Один шаг из жизни бота
     * @param step
     */
	public void step() {
		for (int cyc = 0; (cyc < 15); cyc++)
			if(execute()) break;
		if(getBuoyancy() < 0 && getAge() % (getBuoyancy()+11) == 0)
			moveD(DIRECTION.DOWN);
		else if(getBuoyancy() > 0 && getAge() % (11-getBuoyancy()) == 0)
			moveD(DIRECTION.UP);
        
      //###########################################################################
        //.......  входит ли бот в        ........
        //.......  многоклеточную цепочку и если да, то нужно распределить  ........
        //.......  энергию и минералы с соседями                            ........
        //.......  также проверить, количество накопленой энергии, возможно ........
        //.......  пришло время подохнуть или породить потомка              ........
		if(isSleep) {
			isSleep = false;
	        addHealth(-1); //Спать куда эффективнее
		} else {
	        addHealth(- HP_PER_STEP); //Пожили - устали
		}
        if (this.getHealth() < 1) { //Очень жаль, но мы того - всё
            this.bot2Organic();
            return;
        }else if (this.getHealth() > maxHP) { //Или наоборот, мы ещё как того!
            this.botDouble();
        }
    	if(friends.size() != 0) { // Колония безвозмездно делится всем, что имеет
    		double allHp = getHealth();
    		long allMin = getMineral();
    		int allDNA_wall = DNA_wall;
    		int friend = friends.size() + 1;
    		int maxToxic = poisonPower;
    		Point delP = null;
			synchronized (friends) {
				for (Entry<Point, AliveCell> cell_e : friends.entrySet()) {
					AliveCell cell = cell_e.getValue();
					allHp+=cell.getHealth();
					allMin+=cell.getMineral();
					allDNA_wall+=cell.DNA_wall;
					if(cell.poisonType == poisonType)
						maxToxic = Math.max(maxToxic, cell.poisonPower);
					if(!cell.aliveStatus(LV_STATUS.LV_ALIVE))
						delP = cell_e.getKey();
				}
	    		if(delP != null)
	    			friends.remove(delP);
			}
    		allHp /= friend;
    		allMin /= friend;
    		allDNA_wall /= friend;
    		setHealth(allHp);
    		setMineral(allMin);
    		DNA_wall = allDNA_wall;
    		for (AliveCell cell : friends.values()) {
				cell.setHealth(allHp);
				cell.setMineral(allMin);
				cell.DNA_wall = allDNA_wall;
				if(cell.poisonType == poisonType)
					cell.poisonPower += cell.poisonPower < maxToxic ? 1 : 0;
			}
    	}
        // если бот находится на глубине ниже половины
        // то он автоматом накапливает минералы, но не более 999
        if (this.getPos().getY() >= (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL)) {
        	double realLv = this.getPos().getY() - (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL);
        	double dist = Configurations.MAP_CELLS.height * (1 - Configurations.LEVEL_MINERAL);
            this.setMineral(Math.round(this.getMineral() + Configurations.CONCENTRATION_MINERAL * (realLv/dist) * (5 - this.photosynthesisEffect))); //Эффективный фотосинтез мешает нам переваривать пищу
        }
	}
	
	/**
	 * Исполняет текущую команду
	 * @return true, если это команда полная (то есть заканчивающая ход)
	 */
	private boolean execute() {
		int command = dna.get(); // текущая команда
		switch (command) {
		// БАЗОВЫЕ ФУНКЦИИ
		case block1: // Фотосинтез
			this.photosynthesis();
			dna.next(1);
			return true;
		// .................. преобразовать минералы в энерию ...................
		case block1 + 1:
			this.mineral2Energy();
			dna.next(1);
			return true;
		// Клонирование
		case block1 + 2:{
			int childCMD = dna.param(0, dna.size); // Откуда будет выполняться команда ребёнка
			dna.next(1+childCMD); // Чтобы у потомка выполнилась следующая команда
			botDouble();
			dna.next(-childCMD); // А родитель выполняет свои инструкции далее
			}return true;
		// Самоубийство
		case block1 + 3:
			setHealth(-getHealth());
			return true;
		// Пукнуть - выделить немного яда относительно
		case block1 + 4:
			if (getPosionType() != TYPE.НЕТ)
				addPosionR(DIRECTION.toEnum(dna.param(0, DIRECTION.size())));
			dna.next(2);
			return true;
		// Пукнуть - выделить немного яда абсолютно
		case block1 + 5:
			if (getPosionType() != TYPE.НЕТ)
				addPosionA(DIRECTION.toEnum(dna.param(0, DIRECTION.size())));
			dna.next(2);
			return true;
		// Спать, сохранить энергию
		case block1 + 6:
			isSleep = true;
			dna.next(1);
			return true;
		//Увеличить плавучесть
		case block1 + 7:
			addHealth(-1);//Переводит 1 хп в 0.1 плавучести
			buoyancy = Math.min(10, getBuoyancy() + 1);
			dna.next(1);
			return true;
		//Уменьшить плавучесть
		case block1 + 8:
			addHealth(-1);//Переводит 1 хп в 0.1 плавучести
			buoyancy = Math.max(-10, getBuoyancy() - 1);
			dna.next(1);
			return true;

		// ======================================ФУНКЦИИ ДВИЖЕНИЯ=============================
		// ............... сменить направление относительно ....
		case block2:
			direction = DIRECTION.toEnum(DIRECTION.toNum(direction) + dna.param(0, DIRECTION.size()));
			dna.next(2);
			return false;
		// ............... сменить направление абсолютно ....
		case block2 + 1:
			direction = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			dna.next(2);
			return false;
		// ............... шаг в относительном напралении ................
		case block2 + 2: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (moveR(dir))
		        setHealth(getHealth() - 1); // бот теряет на этом 1 энергию
			else
				dna.interrupt(seeR(dir).nextCMD);
			dna.next(2);
		}return true;
		// ............... шаг в абсолютном напралении ................
		case block2 + 3: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (moveA(dir))
		        setHealth(getHealth() - 1); // бот теряет на этом 1 энергию
			else
				dna.interrupt(seeA(dir).nextCMD);
			dna.next(2);
		}return false;
		// ............... выровниться вверх ....
		case block2 + 4:
			direction = DIRECTION.UP;
			dna.next(1);
			return false;

		//==============================ФУНКЦИИ ИССЛЕДОВАНИЯ=====================================
		// ............. посмотреть в относительном напралении...................................
		case block3:
			dna.nextFromAdr(2 + seeR(DIRECTION.toEnum(dna.param(0, DIRECTION.size()))).nextCMD);
			return false;
		// ............. посмотреть в абсолютном напралении ...................................
		case block3 + 1:
			dna.nextFromAdr(2 + seeA(DIRECTION.toEnum(dna.param(0, DIRECTION.size()))).nextCMD);
			return false;
		// ................... на какой высоте бот .........
		case block3 + 2: {
			int param = dna.param(0, Configurations.MAP_CELLS.height);
			if (getPos().getY() < param)
				dna.nextFromAdr(2);
			else
				dna.nextFromAdr(3);
		}return false;
		// ................... какое моё здоровье ...............................
		case block3 + 3: {
			if (health < dna.param(0, maxHP))
				dna.nextFromAdr(2);
			else
				dna.nextFromAdr(3);
		}return false;
		// ...................сколько минералов ...............................
		case block3 + 4: {
			if (mineral < dna.param(0, MAX_MP))
				dna.nextFromAdr(2);
			else
				dna.nextFromAdr(3);
		}return false;
		// ............... окружен ли бот ................
		case block3 + 5: {
			if (findEmptyDirection() != null)
				dna.nextFromAdr(1);
			else
				dna.nextFromAdr(2);
		}return false;
		// ..............Мы можем заняться фотосинтезом?........................
		case block3 + 6: {
			// Показывает эффективность нашего фотосинтеза
			double t = (1 + this.photosynthesisEffect) * this.getMineral() / MAX_MP;
			if (Configurations.sun.getEnergy(getPos()) + t > 0)
				dna.nextFromAdr(1);
			else
				dna.nextFromAdr(2);
		}return false;
		// ..............Минералы прибавляются?........................
		case block3 + 7:
			if (this.getPos().getY() >= (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL))
				dna.nextFromAdr(1);
			else
				dna.nextFromAdr(2);
			return false;
		// ..............Сколкько здоровья у того, на кого смотрю...................
		case block3 + 8: {
			OBJECT see = seeA(direction);
			if (see.isBot) {
				Point point = fromVektorA(direction);
				AliveCell cell = (AliveCell) Configurations.world.get(point);
				if (cell.health < dna.param(0, maxHP))
					dna.nextFromAdr(2);
				else
					dna.nextFromAdr(3);
			} else {
				dna.interrupt(see.nextCMD);
				dna.nextFromAdr(4);
			}
		}return false;
		// ............Сколкько минералов у того, на кого смотрю......................
		case block3 + 9: {
			OBJECT see = seeA(direction);
			if (see.isBot) {
				Point point = fromVektorA(direction);
				AliveCell cell = (AliveCell) Configurations.world.get(point);
				if (cell.mineral < dna.param(0, MAX_MP))
					dna.nextFromAdr(2);
				else
					dna.nextFromAdr(3);
			} else {
				dna.interrupt(see.nextCMD);
				dna.nextFromAdr(4);
			}
		}return false;
		// Я многоклеточный?
		case block3 + 10:
			if (friends.size() == 0)
				dna.nextFromAdr(1);
			else
				dna.nextFromAdr(2);
			return false;
		// Сколько мне десятков лет?
		case block3 + 11:
			if (getAge() > dna.param(0) * 10)
				dna.nextFromAdr(2);
			else
				dna.nextFromAdr(3);
			return false;
		// Сколько у меня защиты ДНК?
		case block3 + 12:
			if (DNA_wall > dna.param(0, MAX_DNA_WALL))
				dna.nextFromAdr(2);
			else
				dna.nextFromAdr(3);
			return false;

		// =============================================ФУНКЦИИ ВЗАИМОДЕЙТСВИЯ===============================================
		// .............. съесть в относительном напралении ...............
		case block4: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (!eatR(dir))
				dna.interrupt(seeR(dir).nextCMD);
			dna.next(2);
		}return true;
		// .............. съесть в абсолютном напралении ...............
		case block4 + 1: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (!eatA(dir))
				dna.interrupt(seeA(dir).nextCMD);
			dna.next(2);
		}return true;
		// Не убить соседа, а лишь куисить в относительном напралении
		case block4 + 2: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (!biteR(dir))
				dna.interrupt(seeR(dir).nextCMD);
			dna.next(2);
		}return true;
		// Не убить соседа, а лишь куисить в абсолютном напралении
		case block4 + 3: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (!biteA(dir))
				dna.interrupt(seeA(dir).nextCMD);
			dna.next(2);
		}return true;

		// Поделиться, если у соседа меньше в относительном напралении
		case block4 + 4: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (!careR(dir))
				dna.interrupt(seeR(dir).nextCMD);
			dna.next(2);
		}return true;
		// Поделиться, если у соседа меньше в абсолютном напралении
		case block4 + 5: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (!careA(dir))
				dna.interrupt(seeA(dir).nextCMD);
			dna.next(2);
		}return true;
		// Отдать безвозмездно в относительном напралении
		case block4 + 6: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (!giveR(dir))
				dna.interrupt(seeR(dir).nextCMD);
			dna.next(2);
		}return true;
		// Отдать безвозмездно в абсолютном напралении
		case block4 + 7: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if (!giveA(dir))
				dna.interrupt(seeA(dir).nextCMD);
			dna.next(2);
		}return true;
		// Толкнуть отностиельно
		case block4 + 8: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			pullR(dir);
			dna.next(2);
		}return true;
		// Толкнуть абсолютно
		case block4 + 9: {
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			pullA(dir);
			dna.next(2);
		}return true;

		// ======================================ФУНКЦИИ ПРОГРАММИРОЫВАНИЯ==============================================
		// У соседа заменить будущую команду пороцессора на свою
		case block5: {
			OBJECT see = seeA(direction);
			if (see.isBot) {
				addHealth(-2); // бот теряет на этом 2 энергии в независимости от результата
				Point point = fromVektorA(direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				if (bot.DNA_wall > 0) {
					bot.DNA_wall--;
				} else {
					int ma = dna.param(0); // Индекс гена
					int mc = dna.param(1); // Его значение
					bot.dna = bot.dna.update(ma, mc);
					bot.setGeneration(bot.getGeneration() + 1);
					bot.evolutionNode = bot.evolutionNode.newNode(bot, stepCount);
				}
				if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
					color_DO = Color.BLACK;
			} else {
				dna.interrupt(see.nextCMD);
			}
			dna.next(3);
		}return true;
		// У соседа подложить команду под процессор свою
		case block5 + 1: {
			OBJECT see = seeA(direction);
			if (see.isBot) {
				addHealth(-2); // бот теряет на этом 2 энергии в независимости от результата
				Point point = fromVektorA(direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				if (bot.DNA_wall > 0) {
					bot.DNA_wall--;
				} else {
					int mc = dna.param(0); // Его значение
					bot.dna = bot.dna.update(0, mc);
					bot.setGeneration(bot.getGeneration() + 1);
					bot.evolutionNode = bot.evolutionNode.newNode(bot, stepCount);
				}
				if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
					color_DO = Color.BLACK;
			} else {
				dna.interrupt(see.nextCMD);
			}
			dna.next(2);
		}return true;
		// у соседа ДНК переписать на своё - встраивание. Встраивается кусок кода сразу
		// за командой
		case block5 + 2: {
			OBJECT see = seeA(direction);
			int length_DNA = dna.param(0, dna.size);
			if (see.isBot) {
				addHealth(-4); // бот теряет на этом 2 энергии в независимости от результата
				Point point = fromVektorA(direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				if (bot.DNA_wall > 0) {
					bot.DNA_wall--;
				} else {
					// Встраиваемая комбинация начинается сразу за командой и её параметром
					// Мы не можем встроить команду на встраивание. Вот главная особенность!
					for (int i = 0; i < length_DNA && (dna.get(2 + i)) != (block5 + 2); i++)
						bot.dna = bot.dna.update(i, dna.get(2 + i));
					bot.setGeneration(bot.getGeneration() + 1);
					bot.evolutionNode = bot.evolutionNode.newNode(bot, stepCount);
				}
				if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
					color_DO = Color.BLACK;
			} else {
				dna.interrupt(see.nextCMD);
			}
			dna.next(2 + length_DNA); // Но этот код не наш, мы его не выполняем!
		}return true;
		// ДНК соседа встроить в свой код, полностью!
		case block5 + 3: {
			OBJECT see = seeA(direction);
			if (see.isBot) {
				addHealth(-8); // бот теряет на этом 2 энергии в независимости от результата
				Point point = fromVektorA(direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				for (int i = 0; i < bot.dna.size; i++)
					dna = dna.update(i, bot.dna.get(i));
				setGeneration(getGeneration() + 1);
				evolutionNode = evolutionNode.newNode(bot, stepCount);
				if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
					color_DO = Color.GRAY;
				// Смены команды не будет, ведь мы эту команду перезаписали уже на нужную
			} else {
				dna.interrupt(see.nextCMD);
				dna.next(1); // Просто живём дальше, будто ни чего и не было
			}
		}return true;
		// Укрепить свою ДНК. На 1 единицу стройматериала требуется целых 4 жизни
		case block5 + 4:
			DNA_wall = (int) Math.min(MAX_DNA_WALL, DNA_wall + 1);
			setHealth(getHealth() - HP_TO_DNA_WALL);
			dna.next(1);
			return true;
		// Пробить ДНК у соседа
		case block5 + 5: {
			OBJECT see = seeA(direction);
			if (see.isBot) {
				Point point = fromVektorA(direction);
				AliveCell bot = (AliveCell) Configurations.world.get(point);
				bot.DNA_wall = Math.max(0, bot.DNA_wall - 2);
				setHealth(getHealth() - 1); // На это нужно усилие
			} else {
				dna.interrupt(see.nextCMD);
			}
			dna.next(1);
		}return true;
		// Цикл, переход не вперёд по ДНК, а назад!
		case block5 + 6:
			dna.next(-dna.param(0)); // Но тут всегда абсолютно! Ибо от изменения длины ДНК всё поплывёт
			return true;

		// =============================================================================ФУНКЦИИ
		// МНОГОКЛЕТОЧНЫХ=============================================================================
		// Присосаться относительно
		case block6:
			DIRECTION dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if(!clingR(dir))
				dna.interrupt(seeA(dir).nextCMD);
			dna.next(2);
			return true;
		// Присосаться абсолютно
		case block6 + 1:
			dir = DIRECTION.toEnum(dna.param(0, DIRECTION.size()));
			if(!clingA(dir))
				dna.interrupt(seeA(dir).nextCMD);
			dna.next(2);
			return true;
		// Родить присосавшегося потомка относительно
		case block6 + 2: {
			int par = dna.param(0, DIRECTION.size());
			int childCMD = dna.param(1, dna.size); // Откуда будет выполняться команда ребёнка
			dna.next(3+childCMD); // Чтобы у потомка выполнилась следующая команда
			cloneR(DIRECTION.toEnum(par));
			dna.next(-childCMD); // А родитель выполняет свои инструкции далее
		}return true;
		// Родить присосавшегося потомка абсолютно
		case block6 + 3: {
			int par = dna.param(0, DIRECTION.size());
			int childCMD = dna.param(1, dna.size); // Откуда будет выполняться команда ребёнка
			dna.next(3+childCMD); // Чтобы у потомка выполнилась следующая команда
			cloneA(DIRECTION.toEnum(par));
			dna.next(-childCMD); // А родитель выполняет свои инструкции далее
		}return true;

		default:
			this.dna.next(command);
			return true;
		}
	}

    
	/**
	 * Убирает бота с карты и проводит все необходимые процедуры при этом
	 */
    public void destroy() {
		evolutionNode.remove();
		synchronized (friends) {
	    	for (AliveCell cell : friends.values() ) 
				cell.friends.remove(this.getPos());
	    	friends.clear();
		}
		super.destroy();
    }
	
	/**
	 * Фотосинтез.
	 * Если бот близко к солнышку, то можно получить жизни.
	 * При этом, есть специальный флаг - photosynthesisEffect число, меняющееся от 0 до 4, показывает сколько дополнительно очков сможет получить ораганизм за фотосинтез
	 */
	private void photosynthesis() {
        //Показывает эффективность нашего фотосинтеза
        double t = (1+this.photosynthesisEffect) * this.getMineral() / AliveCell.MAX_MP;
        // формула вычисления энергии
        double hlt = Configurations.sun.getEnergy(getPos()) + t;
        if (hlt > 0) {
        	addHealth(Math.round(hlt));   // прибавляем полученную энергия к энергии бота
            this.goGreen((int) Math.round(hlt));                      // бот от этого зеленеет
        }
	}
    /**
     * Преобразует мениралы в энергию
     * Тоже зависит от специального числа - photosynthesisEffect, но теперь чем оно ближе к 0, тем больше придёт минералов
     */
    public void mineral2Energy() {
    	double maxMin = 20 * (1 + (4-this.photosynthesisEffect));
        if (getMineral() > maxMin) {   // максимальное количество минералов, которые можно преобразовать в энергию = 100
            setMineral(Math.round(getMineral() - maxMin));
            addHealth(Math.round((4-this.photosynthesisEffect) * maxMin)); // Максимум 1 минрал преобразуется в 4 хп
            goBlue((int) maxMin);  // бот от этого синеет
        } else {  // если минералов меньше, то все минералы переходят в энергию
            goBlue((int) getMineral());
            addHealth(Math.round((4-this.photosynthesisEffect) * getMineral()));
            setMineral(0);
        }
    }
	/**
	 * Отпочковать потомка
	 */
    private void botDouble() {
    	addHealth(- HP_FOR_DOUBLE);      // бот затрачивает 150 единиц энергии на создание копии
        if (this.getHealth() <= 0) {
            return;
        }   // если у него было меньше 150, то пора помирать

        Point n = findEmptyDirection();    // проверим, окружен ли бот
        if (n == null) {           	// если бот окружен, то он в муках погибает
        	setHealth(-this.getHealth());
            return;
        }
        if(Configurations.world.test(n).isPosion) {
			Poison posion = (Poison) Configurations.world.get(n);
            AliveCell newbot = new AliveCell(this,n);
            if(newbot.toxinDamage(posion.type, (int) posion.getHealth())) { //Нас убило
            	posion.addHealth(Math.abs(newbot.getHealth()));
            	newbot.evolutionNode.remove(); //Мы так и не родились, так что нам не нужен узел
            } else { // Мы сильнее яда! Так что удаляем яд и занимаем его место
            	posion.remove_NE();
            	Configurations.world.add(newbot);
            }
        } else {
            Configurations.world.add(new AliveCell(this,n));
        }
	}

	/**
	 * Перемещает бота в относительном направлении
	 * @param direction
	 * @return
	 */
	private boolean moveR(DIRECTION direction) {
	    return moveA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}

	/**
	 * Перемещает бота в абсолютном направлении
	 * @param direction
	 * @return
	 */
	protected boolean moveA(DIRECTION direction) {
		if(friends.size() == 0) {
			if(super.moveA(direction)) {
		        return true;
			} else {
				return false;
			}
		} else {
			//Многоклеточный. Тут логика куда интереснее!
			OBJECT see = super.seeA(direction);
			if(see.isEmptyPlase){
				//Туда двинуться можно, уже хорошо.
				Point point = fromVektorA(direction);

				/**
				 * Правило!
				 * Мы можем двигаться в любую сторону,
				 * 	если от нас до любого из наших друзей будет ровно 1 клетка
				 * То есть если delX или delY > 1.
				 * 	При этом следует учесть, что delX для клеток с х = 0 и х = край экрана - будет ширина экрана - 1
				 * 	Можно поглядеть код точки. В таком случае они всё равно будут рядом по х.
				 */
		    	for (AliveCell cell : friends.values() ) {
		    		int delx = Math.abs(point.getX() - cell.getPos().getX());
		    		int dely = Math.abs(point.getY() - cell.getPos().getY());
		    		if(dely > 1 || (delx > 1 && delx != Configurations.MAP_CELLS.width-1))
		    			return false;
		    	}
		    	//Все условия проверены, можно выдвигаться!
				return super.moveA(direction);
			}
			return false;
		}
	}
	/**
	 * Создаёт одну из мутаций
	 */
	private void mutation() {
		setGeneration(getGeneration() + 1);
		/**Дельта, гуляет от -1 до 1*/
		double del = (0.5 - Math.random())*2;
        switch (Utils.random(2, 10)) { // К сожалению 0 и 1 вырезаны.
            case 2: //Мутирует эффективность фотосинтеза
                this.photosynthesisEffect = Math.max(0, Math.min(this.photosynthesisEffect * (1+del*0.1), 4));
                break;
            case 3:{ //Мутирует геном
                int ma = Utils.random(0, dna.size-1); //Индекс гена
                int mc = Utils.random(0, COUNT_COMAND); //Его значение
                dna = dna.update(ma, mc);
            	}break;
            case 4: //Мутирует красный цвет
        		int red = phenotype.getRed();
        		red = (int) Math.max(0, Math.min(red + del * 10, 255));
        		phenotype = new Color(red,phenotype.getGreen(), phenotype.getBlue(), phenotype.getAlpha());
                break;
            case 5: //Мутирует зелёный цвет
        		int green = phenotype.getGreen();
        		green = (int) Math.max(0, Math.min(green + del * 10, 255));
        		phenotype = new Color(phenotype.getRed(),green, phenotype.getBlue(), phenotype.getAlpha());
                break;
            case 6: //Мутирует синий цвет
        		int blue = phenotype.getGreen();
        		blue = (int) Math.max(0, Math.min(blue + del * 10, 255));
        		phenotype = new Color(phenotype.getRed(),phenotype.getGreen(), blue, phenotype.getAlpha());
                break;
            case 7:{ //Геном удлиняется
            	if(dna.size + 1 <= MAX_MINDE_SIZE) {
                	int mc = Utils.random(0, dna.size - 1); //Ген, который будет дублироваться
            		dna = dna.doubling(mc);
            	}
            } break;
            case 8:{ //Геном укорачивается на последний ген
            	int mc = Utils.random(0, dna.size - 1); //Ген, который будет удалён
            	dna = dna.compression(mc);
            } break;
            case 9:{ // Смена типа яда на который мы отзываемся
            	poisonType = TYPE.toEnum(Utils.random(0, TYPE.size()));
            	if(poisonType != TYPE.НЕТ)
            		poisonPower = Utils.random(1, (int) (HP_FOR_POISON * 2 / 3));
            	else
            		poisonPower = 0; //К этому у нас защищённости ни какой
            }break;
            case 10:{ //Мутирует вектор прерываний
                int ma = Utils.random(0, dna.interrupts.length-1); //Индекс в векторе
                int mc = Utils.random(0, COUNT_COMAND); //Его значение
                dna.interrupts[ma] = mc;
            }break;
        }
		evolutionNode = evolutionNode.newNode(this,stepCount);
	}

	/**
	 * Превращает бота в органику
	 */
	private void bot2Organic() {
		try {
			destroy(); // Удаляем себя
		} catch (CellObjectRemoveException e) {
	    	Configurations.world.add(new Organic(this)); //Мы просто заменяем себя
	    	throw e;
		}
	}



	/**
	 * Подглядывает за бота в абсолютном направлении
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	private OBJECT seeR(DIRECTION direction) {
		return seeA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}
	/**
	 * Подглядывает за бота в абсолютном направлении
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	protected OBJECT seeA(DIRECTION direction) {
		OBJECT see = super.seeA(direction);
		if(see == OBJECT.POISON){
			Point point = fromVektorA(direction);
			Poison cell = (Poison) Configurations.world.get(point);
			if(cell.type == getPosionType())
				return OBJECT.NOT_POISON;
			else
				return OBJECT.POISON;
		} else {
			return see;
		}
	}

	/**
	 * Родственные-ли боты?
	 * Определеяет родственников по фенотипу, по тому как они выглядят.
	 * @param cell
	 * @param cell2
	 * @return
	 */
	/*protected boolean isRelative(CellObject cell0) {
		if (cell0 instanceof AliveCell) {
			AliveCell bot0 = (AliveCell) cell0;
		    int dif = 0;    // счетчик несовпадений в фенотипе
		    dif += Math.abs(bot0.phenotype.getRed() - this.phenotype.getRed());
		    dif += Math.abs(bot0.phenotype.getGreen() - this.phenotype.getGreen());
		    dif += Math.abs(bot0.phenotype.getBlue() - this.phenotype.getBlue());
		    return dif < 10;
		} else {
			return false;
		}
	}*/
	/**
	 * Родственные-ли боты?
	 * Определеяет родственников по генотипу, по тому различаются их ДНК на 2 и более признака
	 * @param cell
	 * @param cell2
	 * @return
	 */
	protected boolean isRelative(CellObject cell0) {
		if (cell0 instanceof AliveCell) {
			AliveCell bot0 = (AliveCell) cell0;
			if(this.dna.mind == bot0.dna.mind) //Они ссылаются на один финальный массив - они полностью равны
				return true;
			if(this.dna.size != bot0.dna.size) //У них разная длинна, это априорно даст разные ДНК
				return false;
			int dif = 0;
			for (int i = 0; i < this.dna.size && dif < 2; i++) {
				if(this.dna.mind[i] != bot0.dna.mind[i])
					dif++;
			}
			return dif >= 2;
		} else {
			return false;
		}
	}

	/**
	 * скушать другого бота или органику в абсолютном направлении
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	private boolean eatR(DIRECTION direction) {
		return eatA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}
	/**
	 * скушать другого бота или органику в абсолютном направлении
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	private boolean eatA(DIRECTION direction) {
        setHealth(health - 4); // бот теряет на этом 4 энергии в независимости от результата
        switch (seeA(direction)) {
			case ORGANIC: {
				Point point = fromVektorA(direction);
				CellObject cell = Configurations.world.get(point);
				setHealth(health + cell.getHealth());    //здоровье увеличилось на сколько осталось
	            goRed((int) cell.getHealth());           // бот покраснел
	            cell.remove_NE();
			} return true;
			case ENEMY:
			case FRIEND:{
				//--------- дошли до сюда, значит впереди живой бот -------------------
				Point point = fromVektorA(direction);
				AliveCell cell = (AliveCell) Configurations.world.get(point);
				
		        long min0 = mineral;  // определим количество минералов у нас
		        long min1 = cell.mineral;  // определим количество минералов у потенциального обеда
		        double hl = cell.health;  // определим энергию у потенциального обеда
		        // если у бота минералов больше
		        if (min0 >= min1) {
		        	setMineral( min0 - min1); // количество минералов у бота уменьшается на количество минералов у жертвы
		            // типа, стесал свои зубы о панцирь жертвы
		            cell.remove_NE(); // удаляем жертву из списков
		            double cl = hl / 2;           // количество энергии у бота прибавляется на (половину от энергии жертвы)
		            this.setHealth(health + cl);
		            goRed((int) cl);                    // бот краснеет
		            return true;                  // возвращаем 5
		        } else {
		        	//если у жертвы минералов больше ----------------------
		            setMineral(0);  // то бот израсходовал все свои минералы на преодоление защиты
		            min1 = min1 - min0;       // у жертвы количество минералов тоже уменьшилось
		            cell.setMineral(min1);       // перезаписали минералы жертве 
		            //------ если здоровья в 2 раза больше, чем минералов у жертвы  ------
		            //------ то здоровьем проламываем минералы ---------------------------
		            if (health >= 2 * min1) {
			            cell.remove_NE(); // удаляем жертву из списков
			            double cl = Math.max(0,(hl / 2) - 2 * min1); // вычисляем, сколько энергии смог получить бот
		            	this.setHealth(health + cl);
		                goRed((int) cl);                   // бот краснеет
		                return true;                             // возвращаем 5
		            } else {
			            //--- если здоровья меньше, чем (минералов у жертвы)*2, то бот погибает от жертвы
		            	cell.setMineral(min1 - Math.round(health / 2));  // у жертвы минералы истраченны
		            	setHealth(0);  // здоровье уходит в ноль
			            return true;
		            }
		        }
			}
			default: return false;
		}
	}
	

	/**
	 * Укусить другого бота или органику в абсолютном направлении
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	private boolean biteR(DIRECTION direction) {
		return biteA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}

	/**
	 * Укусить другого бота или органику в абсолютном направлении
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	private boolean biteA(DIRECTION direction) {
        addHealth(-2); // бот теряет на этом 2 энергии в независимости от результата
        switch (seeA(direction)) {
			case ORGANIC: {
				Point point = fromVektorA(direction);
				CellObject cell = Configurations.world.get(point);
				setHealth(health + cell.getHealth()/4);    //здоровье увеличилось
	            goRed((int) +cell.getHealth()/4);           // бот покраснел
	            cell.setHealth(cell.getHealth()*3/4); //Одну четверть отдали
			} return true;
			case ENEMY:
			case FRIEND:{
				Point point = fromVektorA(direction);
				AliveCell cell = (AliveCell) Configurations.world.get(point);
				
		        long min0 = mineral;  // определим количество минералов у нас
		        long min1 = cell.mineral / 2;  // определим количество минералов у цели,
		        							//но так как мы только кусаем - то и прорываться нам не через весь панцирь
		        double hl = cell.health;  // определим энергию у потенциального кусиха
		        //Если у цели минералов не слишком много, а у нас жизней сильно меньше - можем его кусить
		        if (min0 >= (min1/2) && (health/2 < hl)) {
		        	setMineral(min0 - min1/2); // количество минералов у бота уменьшается на количество минералов у жертвы
		            // типа, стесал свои зубы о панцирь жертвы
		        	double cl = hl / 4;           // количество энергии у бота прибавляется лишь чуть чуть, мы же кусили
		            this.setHealth(health + cl);
		            cell.setHealth(cell.health - cl);
		            goRed((int) cl);                    // бот краснеет
		            return true;
		        } else {
		        	//если у жертвы минералов больше, то нам его просто не прокусить
		            setMineral(mineral/2);  //Ну мы же попробовали
		            return true;
		        }
			}
			default: return false;
		}
	}
	/**
	 * Поделиться
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	private boolean careR(DIRECTION direction) {
		return careA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}

	/**
	 * Поделиться. если больше энергии или минералов, чем у соседа в заданном направлении, то подарим излишки
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	private boolean careA(DIRECTION direction) {
		if(seeA(direction).isBot) {
			Point point = fromVektorA(direction);
			AliveCell cell = (AliveCell) Configurations.world.get(point);
			double hlt0 = health;         // определим количество энергии и минералов
			double hlt1 = cell.health;  // у бота и его соседа
	        long min0 = mineral;
	        long min1 = cell.mineral;
	        if (hlt0 > hlt1) {              // если у бота больше энергии, чем у соседа
	        	double hlt = (hlt0 - hlt1) / 2;   // то распределяем энергию поровну
	            setHealth(health - hlt);
	            cell.setHealth(cell.health + hlt);
	        }
	        if (min0 > min1) {              // если у бота больше минералов, чем у соседа
	        	long min = (min0 - min1) / 2;   // то распределяем их поровну
	            setMineral(mineral - min);
	            cell.setMineral(cell.mineral + min);
	        }
	        return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Отдать
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	private boolean giveR(DIRECTION direction) {
		return giveA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}

	/**
	 * Отдать. если больше энергии или минералов, чем у соседа в заданном направлении, то подарим излишки
	 * @param {*} bot - бот 
	 * @param {*} direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	private boolean giveA(DIRECTION direction) {
		if(seeA(direction).isBot) {
			Point point = fromVektorA(direction);
			AliveCell cell = (AliveCell) Configurations.world.get(point);
			double hlt0 = health;  // бот отдает четверть своей энергии
			double hlt = hlt0 / 4;
            setHealth(health - hlt);
            cell.setHealth(cell.health + hlt);

            long min0 = mineral;  // бот отдает четверть своих минералов
	        if (min0 > 3) {                 // только если их у него не меньше 4
	        	long min = min0 / 4;
	            setMineral(min0 - min);
	            cell.setMineral(cell.mineral + min);
	        }
	        return true;
		} else {
			return false;
		}
	}
	

	private boolean clingR(DIRECTION direction) {
		return clingA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}
	/**
	 * Присосаться к другой клетке
	 * @param direction
	 * @return 
	 */
	private boolean clingA(DIRECTION direction) {
		OBJECT see = seeA(direction);
		if(!see.isBot) return false;
		Point point = fromVektorA(direction);
	    setFriend((AliveCell) Configurations.world.get(point));
	    return true;
	}
	
	private void cloneR(DIRECTION direction) {
		cloneA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}
	private void cloneA(DIRECTION direction) {
		OBJECT see = seeA(direction);
		if(!see.isEmptyPlase)
			return;
		addHealth(- HP_FOR_DOUBLE);      // бот затрачивает 150 единиц энергии на создание копии
        if (this.getHealth() <= 0)// если у него было меньше 150, то пора помирать
            return; 
        if(see == OBJECT.CLEAN) {
			Point point = fromVektorA(direction);
            Configurations.world.add(new AliveCell(this,point));//Сделали потомка
            clingA(direction); // Присосались друг к дуругу
        } else if(see.isPosion) {
			Point point = fromVektorA(direction);
			Poison posion = (Poison) Configurations.world.get(point);
            AliveCell newbot = new AliveCell(this,point);
            if(newbot.toxinDamage(posion.type, (int) posion.getHealth())) { //Нас убило
            	posion.addHealth(Math.abs(newbot.getHealth()));
            	newbot.evolutionNode.remove(); //Мы так и не родились, так что нам не нужен узел
			} else { // Мы сильнее яда! Так что удаляем яд и занимаем его место
				posion.remove_NE();
				Configurations.world.add(newbot);
				clingA(direction); // Присосались друг к дуругу
			}
        }
	}
	
	
	private void addPosionR(DIRECTION direction) {
		addPosionA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}
	
	private void addPosionA(DIRECTION enum1) {
		OBJECT see = seeA(direction);
		if(see == OBJECT.WALL) {
			poisonPower = getPosionPower() + 1;
			return; //Мы пукнули в стену, а попало в нас. Согласен, спорный момент
		}

    	addHealth(-HP_FOR_POISON);      // бот затрачивает энергию на это, причём только 2/3 идёт на токсин
    	if(this.getHealth() <= 0)
    		return;
    	if(see.isEmptyPlase) {
			Point point = fromVektorA(direction);
			if(see == OBJECT.CLEAN) {
				Poison newPoison = new Poison(getPosionType(),stepCount,point,Math.min(HP_FOR_POISON * 2/3, poisonPower));
	            Configurations.world.add(newPoison);//Сделали потомка
			} else {
				CellObject cell = Configurations.world.get(point);
				if(cell.toxinDamage(getPosionType(), (int) Math.min(HP_FOR_POISON * 2/3, poisonPower)))
					cell.remove_NE();
			}
    	}
	}


	private void pullR(DIRECTION direction) {
		pullA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}


	private void pullA(DIRECTION direction) {
		OBJECT see = seeA(direction);
		if(see == OBJECT.WALL || see == OBJECT.CLEAN) {
			return; //Ну что мы можем сделать со стеной или пустотой? О_О
		} else {
			setHealth(this.getHealth() - 2); // Но немного потратились на это
			Point point = fromVektorA(direction);
			CellObject cell = Configurations.world.get(point);
			try {
				cell.moveD(direction); // Вот мы и толкнули
			}catch (CellObjectRemoveException e) {
				// А она возьми да умри. Вот ржака!
			}
		}
	}


	/**
	 * Удар токсином по клетке
	 */
	public boolean toxinDamage(TYPE type,int damag) {
		if(type == getPosionType() && getPosionPower() >= damag) {
			if(getPosionPower() >= damag)
				poisonPower = getPosionPower() + 1;
			else {
				damag = (int) Math.min(damag, getHealth()*2); // Мы не можем принять больше яда, чем в нас хп
				addHealth(-damag);
			}
		} else {
			setHealth(-getHealth()); // Мы не можем жить в этом яду!
		}
		return getHealth() <= 0;
	}
	/**
	 *  * Ищет свободные ячейки вокруг бота.
	 * Сначала прямо, потом лево право, потом зад лево/право и наконец назад
	 * @param bot
	 * @return Найденые координаты
 	 */
	public Point findEmptyDirection() {
	    for (int i = 0; i < DIRECTION.size()/2+1; i++) {
	    	if(i == 0 || i == 4) {
		        Point point = fromVektorR(DIRECTION.toEnum(i));
		        if (Configurations.world.test(point).isEmptyPlase)
		            return point;
	    	} else {
	    		int dir = getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
	    		Point point = fromVektorR(DIRECTION.toEnum(dir));
		        if (Configurations.world.test(point).isEmptyPlase)
		            return point;
		        dir = -dir;
	    		point = fromVektorR(DIRECTION.toEnum(dir));
		        if (Configurations.world.test(point).isEmptyPlase)
		            return point;
	    	}
	    }
	    return null;
	}
	/**
	 * Отдаёт следующие координаты относительно текущего смотра бота
	 * @param {*} bot - кто в центре
	 * @param {*} n - на сколько повернуть голову (+ по часовой)
	 * @return 
	 * @returns Координаты следующей точки в этом направлении
	 */
	public Point fromVektorR(DIRECTION direction) {
		return fromVektorA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}

	/**
	 * Зеленение бота
	 * @param num
	 */
	private void goGreen(int num) {
		if(Legend.Graph.getMode() != Legend.Graph.MODE.DOING)
			return;
		num = Math.max(0, num);
		int red = color_DO.getRed();
		int green = color_DO.getGreen();
		int blue = color_DO.getBlue();
		green = Utils.betwin(0,green + num, 255);
        int nm = num / 2;
        // убавляем красноту
        red = red - nm;
        if (red < 0) {
            blue += red;
        }
        // убавляем синеву
        blue = blue - nm;
        if (blue < 0) {
            red += blue;
        }
        red = Utils.betwin(0,red,255);
        blue = Utils.betwin(0,blue,255);
        color_DO = new Color(red, green, blue , color_DO.getAlpha());
	}
	/**
	 * Синение бота
	 * @param num
	 */
	private void goBlue(int num) {
		if(Legend.Graph.getMode() != Legend.Graph.MODE.DOING)
			return;
		num = Math.max(0, num);
		int red = color_DO.getRed();
		int green = color_DO.getGreen();
		int blue = color_DO.getBlue();
		blue = Utils.betwin(0,blue + num, 255);
        int nm = num / 2;
        // убавляем зелену
        green = green - nm;
        if (green < 0) {
        	red += green;
        }
        // убавляем красноту
        red = red - nm;
        if (red < 0) {
        	green += red;
        }
        green = Utils.betwin(0,green,255);
        red = Utils.betwin(0,red,255);
        color_DO = new Color(red, green, blue , color_DO.getAlpha());
	}
	/**
	 * Краснение бота
	 * @param num
	 */
	private void goRed(int num) {
		if(Legend.Graph.getMode() != Legend.Graph.MODE.DOING)
			return;
		num = Math.max(0, num);
		int red = color_DO.getRed();
		int green = color_DO.getGreen();
		int blue = color_DO.getBlue();
		red = Utils.betwin(0,red + num,255);
        int nm = num / 2;
        // убавляем зелену
        green = green - nm;
        if (green < 0) {
        	blue += green;
        }
        // убавляем
        blue = blue - nm;
        if (blue < 0) {
        	green += blue;
        }
        green = Utils.betwin(0,green,255);
        blue = Utils.betwin(0,blue,255);
        color_DO = new Color(red, green, blue , color_DO.getAlpha());
	}
	
	public void paint(Graphics g) {
		g.setColor(color_DO);
		
		int r = getPos().getRr();
		int rx = getPos().getRx();
		int ry = getPos().getRy();
		//if(friends.size() == 0)
			Utils.fillCircle(g,rx,ry,r);
		//else
		//	Utils.fillSquare(g,rx,ry,r);
		if(r > 5 && friends.size() > 0) {
			synchronized (friends) {
				try {
				for(AliveCell i : friends.values()) {
					int rxc = i.getPos().getRx();
					if(getPos().getX() == 0 && i.getPos().getX() == Configurations.MAP_CELLS.width -1)
						rxc = rx - r;
					else if(i.getPos().getX() == 0 && getPos().getX() == Configurations.MAP_CELLS.width -1)
							rxc = rx + r;
					int ryc = i.getPos().getRy();
					
					int delx = rxc - rx;
					int dely = ryc - ry;
					
					//Рисуем толстую линию, физическую связь
					Graphics2D g2 = (Graphics2D) g;
				    Stroke oldStr = g2.getStroke();
					g2.setStroke(new BasicStroke(r/2));
					g.setColor(color_DO);
					g.drawLine(rx,ry, rx+delx/3,ry+dely/3);

					//А теперь рисуем тонкую линию, чтобы видно было как они выглядят
					g2.setStroke(oldStr);
					g.setColor(Color.BLACK);
					g.drawLine(rx,ry, rx+delx,ry+dely);
				}
				}catch (java.util.ConcurrentModificationException e) {
					// Я хз от почему, но выскакивает!
				}
			}
		}
		if(r > 10) {
			g.setColor(Color.PINK);
			g.drawLine(rx,ry, rx + direction.addX*r/2,ry + direction.addY*r/2);
		}
			
	}

	/**
	 * @return the health
	 */
	public double getHealth() {
		return health;
	}
	
	public void setHealth(double health) {
		this.health=health;
		if(health < 0) health = 0;
		if((Legend.Graph.getMode() == Legend.Graph.MODE.HP))
			repaint();
	}

	/**
	 * @param years the years to set
	 */
	public void setAge(int years) {
		super.setAge(years);
		if((Legend.Graph.getMode() == Legend.Graph.MODE.YEAR) && (years % 100 == 0))
			repaint();
	}

	/**
	 * @return the generation
	 */
	public int getGeneration() {
		return Generation;
	}

	/**
	 * @param generation the generation to set
	 */
	public void setGeneration(int generation) {
		Generation = generation;
		if(Legend.Graph.getMode() == Legend.Graph.MODE.GENER)
			repaint();
	}

	/**
	 * @return the mineral
	 */
	public long getMineral() {
		return mineral;
	}

	/**
	 * @param mineral the mineral to set
	 */
	public void setMineral(long mineral) {
		mineral = (long) Math.min(mineral, MAX_MP);
		this.mineral = mineral;
		if(mineral < 0) mineral = 0;
		if((Legend.Graph.getMode() == Legend.Graph.MODE.MINERALS))
			repaint();
	}
	
	public void repaint() {
		switch (Legend.Graph.getMode()) {
			case MINERALS -> color_DO = new Color(0,0,(int) Utils.betwin(0, (255.0*mineral/MAX_MP),255),evolutionNode.getAlpha());
			case GENER -> color_DO = Utils.getHSBColor(Utils.betwin(0, 0.5*Generation/Legend.Graph.getMaxGen(),1), 1, 1,evolutionNode.getAlpha()/255.0);
			case YEAR -> color_DO = Utils.getHSBColor(Math.max(0, (1.0*getAge()/Legend.Graph.getMaxAge())), 1, 1,evolutionNode.getAlpha()/255.0);
			case HP -> color_DO = new Color((int) Math.min(255, (255.0*Math.max(0,health)/maxHP)),0,0,evolutionNode.getAlpha());
			case PHEN -> color_DO = new Color(phenotype.getRed(), phenotype.getGreen(), phenotype.getBlue(),evolutionNode.getAlpha());
			case DOING -> color_DO = new Color(color_DO.getRed(), color_DO.getGreen(), color_DO.getBlue(),evolutionNode.getAlpha());
		}
	}

	/**
	 * @return Ветвь эволюции
	 */
	public String getBranch() {
		return evolutionNode.getBranch();
	}

	public void setFriend(AliveCell friend) {
		synchronized (friends) {
			if (friend == null || friends.get(friend.getPos()) != null)
				return;
			friends.put(friend.getPos(), friend);
			friend.friends.put(getPos(), this);
		}
	}
	public int getDNA_wall() {
		return DNA_wall;
	}

	public JSON toJSON(JSON make) {
		make.add("DNA",dna.toJSON());
		make.add("health",health);
		make.add("mineral",mineral);
		make.add("direction",DIRECTION.toNum(direction));
		make.add("DNA_wall",DNA_wall);
		make.add("posionType",getPosionType().ordinal());
		make.add("posionPower",getPosionPower());

	    //=================ПАРАМЕТРЫ БОТА============
		make.add("Generation",Generation);
		make.add("GenerationTree",evolutionNode.getBranch());
		

	    //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
		make.add("phenotype",Integer.toHexString(phenotype.getRGB()));
		make.add("photosynthesisEffect",photosynthesisEffect);
		
		//===============МНОГОКЛЕТОЧНОСТЬ===================
		JSON[] fr = new JSON[friends.size()];
		Object[] points = friends.keySet().toArray();
		for (int i = 0; i < fr.length; i++)
			fr[i] = ((Point)points[i]).toJSON();
		make.add("friends",fr);
		return make;
	}

	/**
	 * @return the posionType
	 */
	public Poison.TYPE getPosionType() {
		return poisonType;
	}

	/**
	 * @return the posionPower
	 */
	public int getPosionPower() {
		return poisonPower;
	}
	/**
	 * @return the dna
	 */
	public DNA getDna() {
		return dna;
	}

	public int getBuoyancy() {
		return buoyancy;
	}
}
