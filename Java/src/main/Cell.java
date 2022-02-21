package main;

import java.awt.Color;
import java.awt.Graphics;

import Utils.Utils;
import main.EvolutionTree.Node;
import main.Point.DIRECTION;
import panels.JSONmake;
import panels.Legend;
public class Cell {
	/**Размер мозга*/
	public static final int MINDE_SIZE = 128;
	/**Сколько вообще может быть команд*/
	private static final int COUNT_COMAND = 8*8; // 8 - DIRECTION.size()
	/**Начальный уровень здоровья клеток*/
	static final int StartHP = 5;
	/**Начальный уровень минералов клеток*/
	static final int StartMP = 5;
	/**Сколько нужно жизней для размножения, по умолчанию*/
	static final int maxHP = 999;
	/**Сколько можно сохранить минералов*/
	static final int MAX_MP = 999;
	/**На сколько организм тяготеет к фотосинтезу (0-4)*/
	static final double defFotosin = 2;
	/**Столько энергии тратит бот на размножение*/
	private static final long HP_FOR_DOUBLE = 150;
	/**Статус*/
	public enum LV_STATUS {LV_ALIVE,LV_ORGANIC_HOLD,LV_ORGANIC_SINK};
	/**Статус*/
	public enum OBJECT {WALL(1),CLEAN(0),ORGANIC(2),FRIEND(3),ENEMY(4), BOT(5);
		private static final OBJECT[] myEnumValues = OBJECT.values();
		/**На сколько нужно сдвинуть счётчик команда дополнительно, типо развилка*/
		int nextCMD;
		OBJECT(int nextCMD) {this.nextCMD=nextCMD;}
		public static int size() {return myEnumValues.length;}
	};
	
    /**Позиция бота в трёх координатах*/
	Point pos;
	/**Внутренний счётчик процессора*/
    int processorTik = 0;
    /**Сознание существа, его мозг*/
    int [] mind = new int[MINDE_SIZE];
    /**Состояние животного*/
    public LV_STATUS alive = LV_STATUS.LV_ALIVE;
    /**Жизни*/
    private long health = StartHP;
    //Минералы
    private long mineral = StartMP;
    //Жизни для размножения
    double hpForDiv = maxHP;
    //Максимально возможное хранение минералов
    double maxMP = MAX_MP;
    /**Направление движения*/
    public Point.DIRECTION direction = Point.DIRECTION.UP;
    //Счётчик, показывает ходил бот в этот ход или нет
    int stepCount = -1;
    
    //=================ПАРАМЕТРЫ БОТА============
    /**Цвет бота зависит от того, что он делает*/
    Color color_DO = new Color(255,255,255);
    /**Возраст бота*/
    private int years = 0;
    /**Поколение (мутационное)*/
    private int Generation = 0;
    
    //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
    /**Фенотип бота*/
    public Color phenotype = new Color(128,128,128);
    //Показывает на сколько организм тяготеет к фотосинтезу
    public double photosynthesisEffect = defFotosin;
    //TODO Сила укуса - мы больше тратим энергии на укус, но наш укус становится сильнее
    Node evolutionNode = null;
    
    Cell(){
    	pos = new Point(Utils.random(0, World.MAP_CELLS.width-1),Utils.random(0, World.MAP_CELLS.height-1));
    }

    public static final int block1 = COUNT_COMAND * 1 / 6;
    public static final int block2 = COUNT_COMAND * 2 / 6;
    public static final int block5 = (block1+block2)/2;
    public static final int block3 = COUNT_COMAND * 3 / 6;
    public static final int block4 = COUNT_COMAND * 4 / 6;
    public static final int block6 = COUNT_COMAND * 5 / 6;
    
    /**
     * Один шаг из жизни бота
     * @param step
     */
	public void step(int step) {
		stepCount = step;
		if(alive == LV_STATUS.LV_ORGANIC_HOLD) {
			alive = LV_STATUS.LV_ORGANIC_SINK;
			return;
		}else if(alive == LV_STATUS.LV_ORGANIC_SINK) {
			alive = LV_STATUS.LV_ORGANIC_HOLD;
			if(!moveA(DIRECTION.DOWN) && !moveA(DIRECTION.DOWN_L))
				moveA(DIRECTION.DOWN_R);
			if((years--) % World.TIK_TO_EXIT == 0)				//Помогает орагнике дольше оставаться "свежатенкой"
				health = (this.getHealth() + 1);
			if(this.getHealth() >= 0) {
				evolutionNode.remove();
				World.world.clean(pos);
			}
			return;
		}
		setAge(getAge() + 1);
		loop : for (int cyc = 0; (cyc < 15); cyc++) {
			int command = this.mind[this.getProcessorTik()]; // текущая команда
			switch (command) {
				case 0 : // Фотосинтез
					this.photosynthesis();
					this.nextCommand(1);
					break loop;

					// БАЗОВЫЕ ФУНКЦИИ
					//.................. преобразовать минералы в энерию ...................
				case block1:
					this.mineral2Energy();
					this.nextCommand(1);
					break loop;

				//Клонирование
				case block1+1:
					botDouble();
					nextCommand(1);
				break loop;
				//TODO Клонирование но удержание потомка
				//case block1+2:
				//	break;
					
					//=============================================================================ФУНКЦИИ ПРОГРАММИРОЫВАНИЯ=============================================================================
				//Заменить ДНК из параметра 1 в месте параметр 2 Заменить
				case block5:{
					int ma = Math.min(param(0),MINDE_SIZE); //Индекс гена
	                int mc = Math.min(param(1),COUNT_COMAND);  //Его значение
	                this.mind[ma] = mc;
					nextCommand(3);
				}break loop;
				//у соседа ДНК из параметра 1 в месте параметр 2 Заменить
				case block5+1:{
					OBJECT see = seeA(direction);
					if(see == OBJECT.ENEMY || see == OBJECT.FRIEND){
					    Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
					    Cell bot = World.world.get(point);
						int ma = Math.min(param(0),MINDE_SIZE); //Индекс гена
		                int mc = Math.min(param(1),COUNT_COMAND);  //Его значение
		                bot.mind[ma] = mc;
					}
					nextCommand(3);
				}break loop;
				//TODO Присосаться к соседу
				//case block5+2:break;
					
					
					//=============================================================================ФУНКЦИИ ДВИЖЕНИЯ=============================================================================
					//...............  сменить направление относительно   ....
				case block2 :
	                direction = DIRECTION.toEnum(DIRECTION.toNum(direction) + param(0));
					this.nextCommand(2);
				break;
				//...............  сменить направление абсолютно   ....
				case block2 +1:
	                direction = DIRECTION.toEnum(param(0));
					this.nextCommand(2);
				break;
				//...............  шаг  в относительном напралении  ................
				case block2 +2:{
	                if(moveR(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else {
						this.nextCommandFromAdr(2+seeR(DIRECTION.toEnum(param(0))).nextCMD);
	                }
				}break loop;
				//...............  шаг  в абсолютном напралении  ................
				case block2 +3:{
	                if(moveA(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else {
						this.nextCommandFromAdr(2+seeA(DIRECTION.toEnum(param(0))).nextCMD);
	                }
				}break loop;
				
				
					//=============================================================================ФУНКЦИИ ИССЛЕДОВАНИЯ=============================================================================
					//.............   посмотреть  в относительном напралении ...................................
				case block3 :
					this.nextCommandFromAdr(1+seeR(DIRECTION.toEnum(param(0))).nextCMD);
	            break;
	        	//.............   посмотреть  в абсолютном напралении ...................................
				case block3+1 :
					this.nextCommandFromAdr(1+seeA(DIRECTION.toEnum(param(0))).nextCMD);
	            break;
	            //...................  какой мой уровень (на какой высоте бот)  .........
				case block3+2 :{
					 // байт в геноме может иметь значение от 0 до COUNT_COMAND, а нужно от 0 до World.MAP_CELLS.height
	                int param = param(0) * World.MAP_CELLS.height / COUNT_COMAND;
	                // если уровень бота ниже, чем полученное значение,
	                // то прибавляем к указатели текущей команды значение 2-го байта, после выполняемой команды
	                if (pos.y < param) {
						this.nextCommandFromAdr(2);
	                } else { // иначе прибавляем к указатели текущей команды значение 3-го байта, после выполняемой команды
						this.nextCommandFromAdr(3);
	                }
				}break;
					//...................  какое моё здоровье  ...............................
				case block3+3 :{
					 // байт в геноме может иметь значение от 0 до COUNT_COMAND, а нужно от 0 до maxHP
	                int param = (int) (param(0) * hpForDiv / COUNT_COMAND);
	                // если здоровье бота ниже, чем полученное значение,
	                // то прибавляем к указатели текущей команды значение 2-го байта, после выполняемой команды
	                if (health < param) {
						this.nextCommandFromAdr(2);
	                } else { // иначе прибавляем к указатели текущей команды значение 3-го байта, после выполняемой команды
						this.nextCommandFromAdr(3);
	                }
				}break;
					//...................сколько  минералов ...............................
				case block3+4 :{
		            int param = (int) (param(0) * maxMP / COUNT_COMAND);
		            if (mineral < param) {
						this.nextCommandFromAdr(2);
		            } else { // иначе прибавляем к указатели текущей команды значение 3-го байта, после выполняемой команды
						this.nextCommandFromAdr(3);
		            }
				}break;
					//...............  окружен ли бот    ................
				case block3+5 :{
					if(AllBotsCommand.findEmptyDirection(this) != null)
						this.nextCommandFromAdr(1);
					else
						this.nextCommandFromAdr(2);
				}break;
					//..............Мы можем заняться фотосинтезом?........................
				case block3+6:{
			        //Показывает эффективность нашего фотосинтеза
			        double t = this.getMineral() / this.maxMP * this.photosynthesisEffect;
			        // формула вычисления энергии ============================= SEZON!!!!!!!!!!
			        double hlt = World.SUN_POWER - (World.DIRTY_WATER * this.pos.y / World.MAP_CELLS.height) + t;
			        if (hlt > 0) 
						this.nextCommandFromAdr(1);
					else
						this.nextCommandFromAdr(2);
				}break;
					//..............Минералы прибавляются?........................
				case block3+7:
			        if (this.pos.y >= (World.MAP_CELLS.height  *World.LEVEL_MINERAL)) 
						this.nextCommandFromAdr(1);
					else
						this.nextCommandFromAdr(2);
				break;
				//..............Сколкько здоровья у того, на кого смотрю........................
				case block3+8:{
					 // байт в геноме может иметь значение от 0 до COUNT_COMAND, а нужно от 0 до maxHP
	                int param = (int) (param(0) * hpForDiv / COUNT_COMAND);
	                OBJECT see = seeA(direction);
					if(see == OBJECT.ENEMY || see == OBJECT.FRIEND) {
						Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
						Cell cell = World.world.get(point);
						// если уровень бота ниже, чем полученное значение,
		                // то прибавляем к указатели текущей команды значение 2-го байта, после выполняемой команды
		                if (cell.health < param) {
							this.nextCommandFromAdr(2);
		                } else { // иначе прибавляем к указатели текущей команды значение 3-го байта, после выполняемой команды
							this.nextCommandFromAdr(3);
		                }
					} else {
						this.nextCommandFromAdr(4+see.nextCMD);
					}
				}break;
				//..............Сколкько минералов у того, на кого смотрю........................
				case block3+9:{
					 // байт в геноме может иметь значение от 0 до COUNT_COMAND, а нужно от 0 до maxHP
	                int param = (int) (param(0) * hpForDiv / COUNT_COMAND);
	                OBJECT see = seeA(direction);
					if(see == OBJECT.ENEMY || see == OBJECT.FRIEND) {
						Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
						Cell cell = World.world.get(point);
						// если уровень бота ниже, чем полученное значение,
		                // то прибавляем к указатели текущей команды значение 2-го байта, после выполняемой команды
		                if (cell.mineral < param) {
							this.nextCommandFromAdr(2);
		                } else { // иначе прибавляем к указатели текущей команды значение 3-го байта, после выполняемой команды
							this.nextCommandFromAdr(3);
		                }
					} else {
						this.nextCommandFromAdr(4+see.nextCMD);
					}
				}break;
				//TODO Я многоклеточный?
				//case block3+8:break;
				
					//=============================================================================ФУНКЦИИ ВЗАИМОДЕЙТСВИЯ=============================================================================
	            	//..............   съесть в относительном напралении       ...............
				case block4 :
					if(eatR(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else
						this.nextCommandFromAdr(3+seeR(DIRECTION.toEnum(param(0))).nextCMD);
				break loop;
					//..............   съесть в абсолютном напралении       ...............
				case block4 +1:
					if(eatA(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else
						this.nextCommandFromAdr(3+seeA(DIRECTION.toEnum(param(0))).nextCMD);
				break loop;
				//Не убить соседа, а лишь куисить в относительном напралении
				case block4 + 2:
					if(biteR(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else
						this.nextCommandFromAdr(3+seeR(DIRECTION.toEnum(param(0))).nextCMD);
				break loop;
				//Не убить соседа, а лишь куисить в абсолютном напралении
				case block4 + 3:
					if(biteA(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else
						this.nextCommandFromAdr(3+seeR(DIRECTION.toEnum(param(0))).nextCMD);
				break loop;
					
				//Поделиться, если у соседа меньше в относительном напралении
				case block4 + 4:
					if(careR(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else
						this.nextCommandFromAdr(3+seeR(DIRECTION.toEnum(param(0))).nextCMD);
				break loop;
				//Поделиться, если у соседа меньше в абсолютном напралении
				case block4 + 5:
					if(careA(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else
						this.nextCommandFromAdr(3+seeR(DIRECTION.toEnum(param(0))).nextCMD);
				break loop;
					
				//Отдать безвозмездно в относительном напралении
				case block4 + 6:
					if(giveR(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else
						this.nextCommandFromAdr(3+seeR(DIRECTION.toEnum(param(0))).nextCMD);
				break loop;
				//Отдать безвозмездно в абсолютном напралении
				case block4 + 7:
					if(giveA(DIRECTION.toEnum(param(0))))
						this.nextCommandFromAdr(2);
	                else
						this.nextCommandFromAdr(3+seeR(DIRECTION.toEnum(param(0))).nextCMD);
				break loop;
				

				//=============================================================================ФУНКЦИИ МНОГОКЛЕТОЧНЫХ=============================================================================
				//case block6:
				
				default :
					this.nextCommand(command);
					break loop;
			}
		}
        
      //###########################################################################
        //.......  входит ли бот в        ........
        //.......  многоклеточную цепочку и если да, то нужно распределить  ........
        //.......  энергию и минералы с соседями                            ........
        //.......  также проверить, количество накопленой энергии, возможно ........
        //.......  пришло время подохнуть или породить потомка              ........
        
        if (this.alive == LV_STATUS.LV_ALIVE) {
            //let cells = this.isMulti();
            //Обработка многоклеточности..
            if (this.getHealth() > this.hpForDiv) {
                //Диление
                this.botDouble();
            }
            setHealth(this.getHealth() - 3); //Пожили - устали
            if (this.getHealth() < 1) { //Очень жаль, но мы того - всё
                this.bot2Organic();
                return;
            }
            // если бот находится на глубине ниже половины
            // то он автоматом накапливает минералы, но не более 999
            if (this.pos.y >= (World.MAP_CELLS.height * World.LEVEL_MINERAL)) {
            	double realLv = this.pos.y - (World.MAP_CELLS.height * World.LEVEL_MINERAL);
            	double dist = World.MAP_CELLS.height * (1 - World.LEVEL_MINERAL);
                this.setMineral((long) (this.getMineral() + World.CONCENTRATION_MINERAL * (realLv/dist) * (5 - this.photosynthesisEffect))); //Эффективный фотосинтез мешает нам переваривать пищу
            }
        }
	}
	

	/**
	 * Получает паарметр функции
	 * @param i номер параметра
	 * @return
	 */
	public int param(int i) {
		int num = this.getProcessorTik() + i + 1; //Потому что нулевой параметр идёт сразу за командой
		while (num >= mind.length) {
			num = num - mind.length;
        }
		return mind[num];
	}

	/**
	 * Отпочковать потомка
	 */
    private void botDouble() {
    	setHealth(this.getHealth() - HP_FOR_DOUBLE);      // бот затрачивает 150 единиц энергии на создание копии
        if (this.getHealth() <= 0) {
        	setHealth(getHealth() + (long) (-hpForDiv/2 + getMineral())); //Превращается в органику всё, что только может
            return;
        }   // если у него было меньше 150, то пора помирать

        Point n = AllBotsCommand.findEmptyDirection(this);    // проверим, окружен ли бот
        if (n == null) {           	// если бот окружен, то он в муках погибает
        	setHealth(-this.getHealth() - getMineral());
            return;
        }

        Cell newbot = new Cell();
        newbot.pos = n;

        newbot.setHealth(this.getHealth() / 2);   // забирается половина здоровья у предка
        setHealth(this.getHealth() / 2);
        newbot.setMineral(this.getMineral() / 2); // забирается половина минералов у предка
        this.setMineral(this.getMineral() / 2);

        newbot.phenotype = this.phenotype;   // цвет такой же, как у предка
        newbot.direction = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));   // направление, куда повернут новорожденный, генерируется случайно

        for (int i = 0; i < this.mind.length; i++) {  // копируем геном в нового бота
            newbot.mind[i] = this.mind[i];
        }

        newbot.hpForDiv = this.hpForDiv;
        newbot.maxMP = this.maxMP;
        newbot.photosynthesisEffect = this.photosynthesisEffect;
        newbot.stepCount = stepCount;
        newbot.setGeneration(Generation);
        newbot.evolutionNode = evolutionNode.clone();

        if (Math.random() < World.AGGRESSIVE_ENVIRONMENT) {
            newbot.mutation();
        }
        World.world.add(newbot);
	}

	private void mutation() {
		setGeneration(getGeneration() + 1);
		/**Дельта, гуляет от -1 до 1*/
		double del = (0.5 - Math.random())*2;
		//TODO К сожалению первые два слишком сильно убегают вперёд
        switch (Utils.random(2, 6)) { 
            case 0: //Мутирует количество энергии для деления
                this.hpForDiv *= del;
                break;
            case 1: //Мутирует максимально возможное хранилище минералов
                this.maxMP *= del;
                break;
            case 2: //Мутирует эффективность фотосинтеза
                this.photosynthesisEffect = Math.max(0, Math.min(this.photosynthesisEffect * (1+del*0.1), 4));
                evolutionNode = evolutionNode.newNode(stepCount, photosynthesisEffect);
                break;
            case 3: //Мутирует геном
                int ma = Utils.random(0, mind.length-1); //Индекс гена
                int mc = Utils.random(0, COUNT_COMAND-1); //Его значение
                evolutionNode = evolutionNode.newNode(stepCount, ma,mc);
                this.mind[ma] = mc;
                break;
            case 4: //Мутирует красный цвет
        		int red = phenotype.getRed();
        		red = (int) Math.max(0, Math.min(red + del * 10, 255));
        		phenotype = new Color(red,phenotype.getGreen(), phenotype.getBlue(), phenotype.getAlpha());
                evolutionNode = evolutionNode.newNode(stepCount, phenotype);
                break;
            case 5: //Мутирует зелёный цвет
        		int green = phenotype.getGreen();
        		green = (int) Math.max(0, Math.min(green + del * 10, 255));
        		phenotype = new Color(phenotype.getRed(),green, phenotype.getBlue(), phenotype.getAlpha());
                evolutionNode = evolutionNode.newNode(stepCount, phenotype);
                break;
            case 6: //Мутирует синий цвет
        		int blue = phenotype.getGreen();
        		blue = (int) Math.max(0, Math.min(blue + del * 10, 255));
        		phenotype = new Color(phenotype.getRed(),phenotype.getGreen(), blue, phenotype.getAlpha());
                evolutionNode = evolutionNode.newNode(stepCount, phenotype);
                break;
        }
	}

	private void bot2Organic() {
        this.alive = LV_STATUS.LV_ORGANIC_HOLD;       // Мы теперь орагника
        color_DO = new Color(139,69,19,100);
        if(getHealth() == 0)
        	setHealth((long) (-hpForDiv/10 + getMineral()/10)); //Превращается в органику всё, что только может
        else
        	setHealth(getHealth()); //Для разукрашивания
        setAge(0); //У нас больше нет возраста, зато мы его можем использовать в дело :)
	}

	// ...  фотосинтез, этой командой забит геном первого бота     ...............
    // ...  бот получает энергию солнца в зависимости от глубины   ...............
    // ...  и количества минералов, накопленных ботом              ...............
	private void photosynthesis() {
        //Показывает эффективность нашего фотосинтеза
        double t = this.getMineral() / this.maxMP * (1+this.photosynthesisEffect);
        // формула вычисления энергии ============================= SEZON!!!!!!!!!!
        double hlt = World.SUN_POWER - (World.DIRTY_WATER * this.pos.y / World.MAP_CELLS.height) + t;
        if (hlt > 0) {
        	setHealth(Math.round(this.getHealth() + hlt));   // прибавляем полученную энергия к энергии бота
            this.goGreen((int) Math.round(hlt));                      // бот от этого зеленеет
        }
	}
    // ...  преобразование минералов в энергию  ...............
    public void mineral2Energy() {
    	double maxMin = 10 * (1 + (4-this.photosynthesisEffect));
        if (getMineral() > maxMin) {   // максимальное количество минералов, которые можно преобразовать в энергию = 100
            setMineral(Math.round(getMineral() - maxMin));
            setHealth(Math.round(getHealth() + 2 * (4-this.photosynthesisEffect) * maxMin));
            goBlue((int) maxMin);  // бот от этого синеет
        } else {  // если минералов меньше 100, то все минералы переходят в энергию
            goBlue((int) getMineral());
            setHealth(Math.round(getHealth() + 2 * (4-this.photosynthesisEffect) * getMineral()));
            setMineral(0);
        }
    }


	//Передвигает счётчик команд на переданное число
	private void nextCommand(int absoluteAdr) {
		int paramadr = absoluteAdr + this.getProcessorTik();
        while (paramadr >= mind.length) {
            paramadr = paramadr - mind.length;
        }
        this.processorTik = paramadr;
	}
	//Передвигает счётчик команд на переданное число
	private void nextCommandFromAdr(int absoluteAdr) {
		//Вычисляем где лежит адрес
		int paramadr = absoluteAdr + this.getProcessorTik();
        while (paramadr >= mind.length) {
            paramadr = paramadr - mind.length;
        }
        //Получили этот адерс
        int bias = mind[paramadr];
        //А теперь смещаемся куда надо
        nextCommand(bias);
	}

	/**
	 * Перемещает бота в абсолютном направлении
	 * @param direction
	 * @return
	 */
	private boolean moveA(DIRECTION direction) {
		if(seeA(direction) == OBJECT.CLEAN){
			Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
			World.world.clean(pos);
	        pos = point;
	        World.world.add(this);
	        if(health > 0)
	        	setHealth(health - 1); // бот теряет на этом 1 энергию
	        return true;
	    } 
	    return false;
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
	private OBJECT seeA(DIRECTION direction) {
	    Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
	    OBJECT obj = World.world.test(point);
	    if (obj != OBJECT.BOT)
	        return obj;
	    else if (isRelative(this, World.world.get(point)))
	        return OBJECT.FRIEND;
	    else
	        return OBJECT.ENEMY;
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
			    Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
				Cell cell = World.world.get(point);
				setHealth(health - cell.getHealth());    //здоровье увеличилось на сколько осталось
	            goRed((int) -cell.getHealth());           // бот покраснел
	            cell.evolutionNode.remove();
	            World.world.clean(point);
			} return true;
			case ENEMY:
			case FRIEND:{
				//--------- дошли до сюда, значит впереди живой бот -------------------
			    Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
				Cell cell = World.world.get(point);
				
		        long min0 = mineral;  // определим количество минералов у бота
		        long min1 = cell.mineral;  // определим количество минералов у потенциального обеда
		        long hl = cell.health;  // определим энергию у потенциального обеда
		        // если у бота минералов больше
		        if (min0 >= min1) {
		        	setMineral( min0 - min1); // количество минералов у бота уменьшается на количество минералов у жертвы
		            // типа, стесал свои зубы о панцирь жертвы
		            World.world.clean(point);          // удаляем жертву из списков
		            cell.evolutionNode.remove();
		            long cl = hl / 2;           // количество энергии у бота прибавляется на (половину от энергии жертвы)
		            this.setHealth(health + cl);
		            goRed((int) cl);                    // бот краснеет
		            return true;                  // возвращаем 5
		        } else {
		        	//если у жертвы минералов больше ----------------------
		            setMineral(0);  // то бот израсходовал все свои минералы на преодоление защиты
		            min1 = min1 - min0;       // у жертвы количество минералов тоже уменьшилось
		            cell.setMineral(min1 - min0);       // перезаписали минералы жертве 
		            //------ если здоровья в 2 раза больше, чем минералов у жертвы  ------
		            //------ то здоровьем проламываем минералы ---------------------------
		            if (health >= 2 * min1) {
		            	World.world.clean(point);          // удаляем жертву из списков
			            cell.evolutionNode.remove();
		            	long cl = (hl / 2) - 2 * min1; // вычисляем, сколько энергии смог получить бот
		            	this.setHealth(health + cl);
		                goRed((int) cl);                   // бот краснеет
		                return true;                             // возвращаем 5
		            } else {
			            //--- если здоровья меньше, чем (минералов у жертвы)*2, то бот погибает от жертвы
		            	cell.setMineral(min1 - (health / 2));  // у жертвы минералы истраченны
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
        setHealth(health - 2); // бот теряет на этом 2 энергии в независимости от результата
        switch (seeA(direction)) {
			case ORGANIC: {
			    Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
				Cell cell = World.world.get(point);
				setHealth(health - cell.getHealth()/4);    //здоровье увеличилось
	            goRed((int) -cell.getHealth()/4);           // бот покраснел
	            cell.setHealth(cell.getHealth()*3/4);
			} return true;
			case ENEMY:
			case FRIEND:{
				//--------- дошли до сюда, значит впереди живой бот -------------------
			    Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
				Cell cell = World.world.get(point);
				
		        long min0 = mineral;  // определим количество минералов у бота
		        long min1 = cell.mineral / 2;  // определим количество минералов у цели,
		        							//но так как мы только кусаем - то и прорываться нам не через весь панцирь
		        long hl = cell.health;  // определим энергию у потенциального кусиха
		        // если у бота минералов больше
		        if (min0 >= min1 && (health < hl*2)) { //Если у нас жизней сильно меньше, чем у жертвы
		        	setMineral( min0 - min1); // количество минералов у бота уменьшается на количество минералов у жертвы
		            // типа, стесал свои зубы о панцирь жертвы
		            long cl = hl / 4;           // количество энергии у бота прибавляется лишь чуть чуть, мы же кусили
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
        switch (seeA(direction)) {
			case ENEMY:
			case FRIEND:{
		        //------- если мы здесь, то в данном направлении живой ----------
			    Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
				Cell cell = World.world.get(point);
		        long hlt0 = health;         // определим количество энергии и минералов
		        long hlt1 = cell.health;  // у бота и его соседа
		        long min0 = mineral;
		        long min1 = cell.mineral;
		        if (hlt0 > hlt1) {              // если у бота больше энергии, чем у соседа
		        	long hlt = (hlt0 - hlt1) / 2;   // то распределяем энергию поровну
		            setHealth(health - hlt);
		            cell.setHealth(cell.health + hlt);
		        }
		        if (min0 > min1) {              // если у бота больше минералов, чем у соседа
		        	long min = (min0 - min1) / 2;   // то распределяем их поровну
		            setMineral(mineral - min);
		            cell.setMineral(cell.mineral + min);
		        }
		        return true;
			}
			default: return false;
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
        switch (seeA(direction)) {
			case ENEMY:
			case FRIEND:{
		        //------- если мы здесь, то в данном направлении живой ----------
			    Point point = AllBotsCommand.fromVektorA(this,DIRECTION.toNum(direction));
				Cell cell = World.world.get(point);
				long hlt0 = health;  // бот отдает четверть своей энергии
				long hlt = hlt0 / 4;
	            setHealth(health - hlt);
	            cell.setHealth(cell.health + hlt);

	            long min0 = mineral;  // бот отдает четверть своих минералов
		        if (min0 > 3) {                 // только если их у него не меньше 4
		        	long min = min0 / 4;
		            setMineral(min0 - min);
		            cell.setMineral(cell.mineral + min);
		        }
		        return true;
			}
			default: return false;
		}
	}
	/**
	 * Родственные-ли боты?
	 * Вообще пока смотрит только по ДНК, но возможно в будущем нужно смотреть будет не на 
	 *      генотип, а на фенотип!
	 * @param cell
	 * @param cell2
	 * @return
	 */
	private boolean isRelative(Cell bot0, Cell bot1) {
	    if (bot0.alive != LV_STATUS.LV_ALIVE || bot1.alive != LV_STATUS.LV_ALIVE) {
	        return false;
	    }
	    int dif = 0;    // счетчик несовпадений в геноме
	    for (int i = 0; i < mind.length; i++) {
	        if (bot0.mind[i] != bot1.mind[i]) {
	            dif = dif + 1;
	            if (dif == 2) {
	                return false;
	            } // если несовпадений в генеме больше 1
	        }     // то боты не родственики
	    }
	    return true;
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
		green = Math.min(green + num, 255);
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
        red = Math.max(red, 0);
        blue = Math.max(blue, 0);
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
		blue = Math.min(blue + num, 255);
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
        green = Math.max(green, 0);
        red = Math.max(red, 0);
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
		red = Math.min(red + num, 255);
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
        green = Math.max(green, 0);
        blue = Math.max(blue, 0);
        color_DO = new Color(red, green, blue , color_DO.getAlpha());
	}
	
	public void paint(Graphics g) {
		if(Legend.Graph.getMode() == Legend.Graph.MODE.PHEN)
			g.setColor(phenotype);
		else
			g.setColor(color_DO);
		Utils.fillCircle(g,pos.getRx(),pos.getRy(),pos.getRr());
	}

	/**
	 * @return the health
	 */
	public long getHealth() {
		return health;
	}
	
	private void setHealth(long health) {
		this.health=health;
		if(health < 0) health = 0;
		if((Legend.Graph.getMode() == Legend.Graph.MODE.HP) && (health % 20 == 0))
			repaint();
	}

	/**
	 * @return the years
	 */
	public int getAge() {
		return years;
	}

	/**
	 * @param years the years to set
	 */
	public void setAge(int years) {
		this.years = years;
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
		mineral = (long) Math.min(mineral, this.maxMP);
		this.mineral = mineral;
		if(mineral < 0) mineral = 0;
		if((Legend.Graph.getMode() == Legend.Graph.MODE.MINERALS) && (mineral % 20 == 0))
			repaint();
	}
	
	public void repaint() {
		switch (Legend.Graph.getMode()) {
			case MINERALS -> color_DO = new Color(0,0,(int) Math.min(255, (255.0*mineral/maxMP)));
			case GENER -> color_DO = Color.getHSBColor((float)Math.max(0, (0.5*Generation/Legend.Graph.getMaxGen())), 1, 1);
			case YEAR -> color_DO = Color.getHSBColor((float)Math.max(0, (1.0*years/Legend.Graph.getMaxAge())), 1, 1);
			case HP -> color_DO = new Color((int) Math.min(255, (255.0*Math.max(0,health)/maxHP)),0,0);
			case PHEN -> color_DO = phenotype;
			case DOING -> color_DO = phenotype;
		}
	}


	/**
	 * @return the processorTik
	 */
	public int getProcessorTik() {
		return processorTik;
	}


	public int getCmdA(int num) {
        while (num >= mind.length)
        	num = num - mind.length;
		return mind[num];
	}


	public JSONmake toJSON() {
		JSONmake make = new JSONmake();
		make.add("pos", pos.toJSON());
		make.add("processorTik",processorTik);
		//make.add("mind",mind);
		make.add("alive",alive.ordinal());
		make.add("health",health);
		make.add("mineral",mineral);
		make.add("direction",DIRECTION.toNum(direction));
		make.add("stepCount",stepCount);

	    //=================ПАРАМЕТРЫ БОТА============
		make.add("years",years);
		//make.add("Generation",Generation);
		make.add("GenerationTree",evolutionNode.branch);
		

	    //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
		//make.add("phenotype",Integer.toHexString(phenotype.getRGB()));
		//make.add("photosynthesisEffect",photosynthesisEffect);
		
		//Убранные уже есть в эволюционном дереве!
		return make;
	}
}
