package MapObjects;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Utils.JSONmake;
import Utils.Utils;
import main.EvolutionTree;
import main.Point;
import main.World;
import main.EvolutionTree.Node;
import main.Point.DIRECTION;
import panels.Legend;
public class AliveCell extends CellObject{
	/**Размер мозга*/
	public static final int MINDE_SIZE = 256;
	/**Сколько вообще может быть команд*/
	private static final int COUNT_COMAND = 9*8; // 8 - DIRECTION.size()
	/**Начальный уровень здоровья клеток*/
	static final int StartHP = 5;
	/**Начальный уровень минералов клеток*/
	static final int StartMP = 5;
	/**Сколько нужно жизней для размножения, по умолчанию*/
	public static final int maxHP = 999;
	/**Сколько можно сохранить минералов*/
	static final int MAX_MP = 999;
	/**На сколько организм тяготеет к фотосинтезу (0-4)*/
	static final double defFotosin = 2;
	/**Столько энергии тратит бот на размножение*/
	private static final long HP_FOR_DOUBLE = 150;
	/**Внутренний счётчик процессора*/
    int processorTik = 0;
    /**Сознание существа, его мозг*/
    int [] mind = new int[MINDE_SIZE];
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
    
    //=================ПАРАМЕТРЫ БОТА============
    /**Поколение (мутационное)*/
    private int Generation = 0;
    
    //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
    /**Фенотип бота*/
    public Color phenotype = new Color(128,128,128);
    //Показывает на сколько организм тяготеет к фотосинтезу
    public double photosynthesisEffect = defFotosin;
    //TODO Сила укуса - мы больше тратим энергии на укус, но наш укус становится сильнее
    public Node evolutionNode = null;
    
    //===============Параметры братства, многоклеточность=======
    private Map<Point,AliveCell> friends = new HashMap<>();
   // private MegaCell megaCell = null;
    
    /**
     * Создание клетки без рода и племени
     */
    public AliveCell(){
    	super(-1, LV_STATUS.LV_ALIVE);
    	setPos(new Point(Utils.random(0, World.MAP_CELLS.width-1),Utils.random(0, World.MAP_CELLS.height-1)));
    	for (int i = 0; i < mind.length; i++)
    		mind[i] = block1; // У клетки по базе только одна функция - фотосинтез
    	color_DO = new Color(255,255,255);
    }

    /**
     * Загрузка клетки
     * @param cell - JSON объект, который содержит всю информацюи о клетке
     * @param tree - Дерево эволюции 
     */
    public AliveCell(JSONmake cell, EvolutionTree tree) {
    	super(cell);
    	processorTik = cell.getI("processorTik");
    	health = cell.getL("health");
    	mineral = cell.getL("mineral");
    	direction = DIRECTION.toEnum(cell.getI("direction"));
    	Generation = cell.getI("Generation");
    	phenotype = new Color((Long.decode("0x"+cell.getS("phenotype"))).intValue(),true);
    	photosynthesisEffect = cell.getD("photosynthesisEffect");
    	
    	evolutionNode = tree.getNode(cell.getS("GenerationTree"));
    	
    	List<Long> mindL = cell.getAL("mind");
    	for (int i = 0; i < mind.length; i++) 
			mind[i] = mindL.get(i).intValue();
    	
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
	
	    phenotype = new Color(cell.phenotype.getRGB(),true);   // цвет такой же, как у предка
	    direction = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));   // направление, куда повернут новорожденный, генерируется случайно
	
	    for (int i = 0; i < this.mind.length; i++)  // копируем геном в нового бота
	    	mind[i] = cell.mind[i];
	
	    hpForDiv = cell.hpForDiv;
	    maxMP = cell.maxMP;
	    photosynthesisEffect = cell.photosynthesisEffect;
	    setGeneration(cell.Generation);
	    color_DO = new Color(255,255,255,evolutionNode.getAlpha());
	    repaint();
	    
	    //Мы на столько хорошо скопировали нашего родителя, что есть небольшой шанс накосячить - мутации
	    if (Math.random() < World.AGGRESSIVE_ENVIRONMENT) 
	        mutation();
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
	public void step() {
		loop : for (int cyc = 0; (cyc < 15); cyc++) {
			int command = this.mind[this.getProcessorTik()]; // текущая команда
			switch (command) {
				// БАЗОВЫЕ ФУНКЦИИ
				case block1 : // Фотосинтез
					this.photosynthesis();
					this.nextCommand(1);
					break loop;
					//.................. преобразовать минералы в энерию ...................
				case block1+1:
					this.mineral2Energy();
					this.nextCommand(1);
					break loop;
				//Клонирование
				case block1+2:
					botDouble();
					nextCommand(1);
				break loop;
				//Самоубийство
				case block1+3:
					setHealth(-getHealth());
				break loop;
					
					//=============================================================================ФУНКЦИИ ПРОГРАММИРОЫВАНИЯ=============================================================================
				//Заменить ДНК из параметра 1 в месте параметр 2 Заменить - самопрограммирование
				case block5:{
					int ma = Math.min(param(0),MINDE_SIZE); //Индекс гена
	                int mc = Math.min(param(1),COUNT_COMAND);  //Его значение
	                this.mind[ma] = mc;
	        		setGeneration(getGeneration() + 1);
	        		evolutionNode = evolutionNode.newNode(stepCount);
					nextCommand(3);
				}break loop;
				//у соседа ДНК из параметра 1 в месте параметр 2 Заменить - взлом кода
				case block5+1:{
					OBJECT see = seeA(direction);
					if(see == OBJECT.ENEMY || see == OBJECT.FRIEND){
					    Point point = fromVektorA(direction);
					    AliveCell bot = (AliveCell) World.world.get(point);
						int ma = Math.min(param(0),MINDE_SIZE); //Индекс гена
		                int mc = Math.min(param(1),COUNT_COMAND);  //Его значение
		                bot.mind[ma] = mc;
		                bot.setGeneration(bot.getGeneration() + 1);
		                bot.evolutionNode = bot.evolutionNode.newNode(bot.stepCount);
		        		if(Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
		        			color_DO = Color.BLACK;
					}
					nextCommand(3);
				}break loop;
				//у соседа ДНК переписать на своё - встраивание
				case block5+2:{
					OBJECT see = seeA(direction);
					if(see == OBJECT.ENEMY || see == OBJECT.FRIEND){
					    Point point = fromVektorA(direction);
					    AliveCell bot = (AliveCell) World.world.get(point);
					    int startPos = Math.min(param(0),MINDE_SIZE);
					    int count = param(1);
						for (int i = 0; i < count; i++) {
							int adr = bot.getProcessorTik() + i;
					        while (adr >= mind.length)
					        	adr = adr - mind.length;
							 bot.mind[adr] = getCmdA(startPos + i);
						}
		                bot.setGeneration(bot.getGeneration() + 1);
		                bot.evolutionNode = bot.evolutionNode.newNode(bot.stepCount);
		        		if(Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
		        			color_DO = Color.BLACK;
					}
					nextCommand(3);
				}break loop;
					
					
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
				//...............  выровниться вверх   ....
				case block2 +4:
	                direction = DIRECTION.UP;
					this.nextCommand(1);
				break;
				
				
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
	                if (getPos().y < param) {
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
					if(findEmptyDirection() != null)
						this.nextCommandFromAdr(1);
					else
						this.nextCommandFromAdr(2);
				}break;
					//..............Мы можем заняться фотосинтезом?........................
				case block3+6:{
			        //Показывает эффективность нашего фотосинтеза
			        double t = this.getMineral() / this.maxMP * (1+this.photosynthesisEffect);
			        if (World.sun.getEnergy(getPos()) + t > 0) 
						this.nextCommandFromAdr(1);
					else
						this.nextCommandFromAdr(2);
				}break;
					//..............Минералы прибавляются?........................
				case block3+7:
			        if (this.getPos().y >= (World.MAP_CELLS.height  *World.LEVEL_MINERAL)) 
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
						Point point = fromVektorA(direction);
						AliveCell cell = (AliveCell) World.world.get(point);
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
						Point point = fromVektorA(direction);
						AliveCell cell = (AliveCell) World.world.get(point);
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
				//Я многоклеточный?
				case block3+10:
					if(friends.size() == 0)
						this.nextCommandFromAdr(1);
					else
						this.nextCommandFromAdr(2);
				break;
				//Сколько мне десятков лет?
				case block3+11:
	                if(getAge() > param(0)*10)
						this.nextCommandFromAdr(2);
	                else
						this.nextCommandFromAdr(3);
				break;
				
				
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
				//Присосаться относительно
				case block6:
					clingR(DIRECTION.toEnum(param(0)));
					nextCommand(2);
				break loop;
				//Присосаться абсолютно
				case block6 +1:
					clingA(DIRECTION.toEnum(param(0)));
					nextCommand(2);
				break loop;
				//Родить присосавшегося потомка относительно
				case block6+2:
					cloneR(DIRECTION.toEnum(param(0)));
					nextCommand(2);
				break loop;
				//Родить присосавшегося потомка абсолютно
				case block6+3:
					cloneA(DIRECTION.toEnum(param(0)));
					nextCommand(2);
				break loop;
				
				
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
        setHealth(this.getHealth() - 3); //Пожили - устали
        if (this.getHealth() < 1) { //Очень жаль, но мы того - всё
            this.bot2Organic();
            return;
        }else if (this.getHealth() > this.hpForDiv) { //Или наоборот, мы ещё как того!
            this.botDouble();
        }
    	if(friends.size() != 0) { // Колония безвозмездно делится всем, что имеет
    		long allHp = getHealth();
    		long allMin = getMineral();
    		int friend = friends.size() + 1;
    		Point delP = null;
			synchronized (friends) {
				for (Entry<Point, AliveCell> cell : friends.entrySet()) {
					allHp+=cell.getValue().getHealth();
					allMin+=cell.getValue().getMineral();
					if(!cell.getValue().aliveStatus(LV_STATUS.LV_ALIVE))
						delP = cell.getKey();
				}
	    		if(delP != null)
	    			friends.remove(delP);
			}
    		allHp /= friend;
    		allMin /= friend;
    		setHealth(allHp);
    		setMineral(allMin);
    		for (AliveCell cell : friends.values()) {
				cell.setHealth(allHp);
				cell.setMineral(allMin);
			}
    	}
        // если бот находится на глубине ниже половины
        // то он автоматом накапливает минералы, но не более 999
        if (this.getPos().y >= (World.MAP_CELLS.height * World.LEVEL_MINERAL)) {
        	double realLv = this.getPos().y - (World.MAP_CELLS.height * World.LEVEL_MINERAL);
        	double dist = World.MAP_CELLS.height * (1 - World.LEVEL_MINERAL);
            this.setMineral((long) (this.getMineral() + World.CONCENTRATION_MINERAL * (realLv/dist) * (5 - this.photosynthesisEffect))); //Эффективный фотосинтез мешает нам переваривать пищу
        }
	}

	/**
	 * Получает паарметр функции
	 * @param i номер параметра
	 * @return
	 */
	public int param(int i) {
		return getCmdA(this.getProcessorTik() + i + 1); //Потому что нулевой параметр идёт сразу за командой
	}
	/**
	 * Возвращает команду по переданому адресу
	 * @param num
	 * @return
	 */
	public int getCmdA(int num) {
        while (num >= mind.length)
        	num = num - mind.length;
		return mind[num];
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
        nextCommand(getCmdA(absoluteAdr + this.getProcessorTik()));
	}
    
	/**
	 * Убирает бота с карты и проводит все необходимые процедуры при этом
	 */
    public void remove() {
		evolutionNode.remove();
		synchronized (friends) {
	    	for (AliveCell cell : friends.values() ) 
				cell.friends.remove(this.getPos());
	    	friends.clear();
		}
		super.remove();
    }
	
	/**
	 * Фотосинтез.
	 * Если бот близко к солнышку, то можно получить жизни.
	 * При этом, есть специальный флаг - photosynthesisEffect число, меняющееся от 0 до 4, показывает сколько дополнительно очков сможет получить ораганизм за фотосинтез
	 */
	private void photosynthesis() {
        //Показывает эффективность нашего фотосинтеза
        double t = this.getMineral() / this.maxMP * (1+this.photosynthesisEffect);
        // формула вычисления энергии
        double hlt = World.sun.getEnergy(getPos()) + t;
        if (hlt > 0) {
        	setHealth(Math.round(this.getHealth() + hlt));   // прибавляем полученную энергия к энергии бота
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
            setHealth(Math.round(getHealth() + (4-this.photosynthesisEffect) * maxMin)); // Максимум 1 минрал преобразуется в 4 хп
            goBlue((int) maxMin);  // бот от этого синеет
        } else {  // если минералов меньше, то все минералы переходят в энергию
            goBlue((int) getMineral());
            setHealth(Math.round(getHealth() + (4-this.photosynthesisEffect) * getMineral()));
            setMineral(0);
        }
    }
	/**
	 * Отпочковать потомка
	 */
    private void botDouble() {
    	setHealth(this.getHealth() - HP_FOR_DOUBLE);      // бот затрачивает 150 единиц энергии на создание копии
        if (this.getHealth() <= 0) {
            return;
        }   // если у него было меньше 150, то пора помирать

        Point n = findEmptyDirection();    // проверим, окружен ли бот
        if (n == null) {           	// если бот окружен, то он в муках погибает
        	setHealth(-this.getHealth() - getMineral());
            return;
        }
        World.world.add(new AliveCell(this,n));
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
		        setHealth(getHealth() - 1); // бот теряет на этом 1 энергию
		        return true;
			} else {
				return false;
			}
		} else {
			//Многоклеточный. Тут логика куда интереснее!
			if(seeA(direction) == OBJECT.CLEAN){
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
		    		int delx = Math.abs(point.x - cell.getPos().x);
		    		int dely = Math.abs(point.y - cell.getPos().y);
		    		if(dely > 1 || (delx > 1 && delx != World.MAP_CELLS.width-1))
		    			return false;
		    	}
		        setHealth(getHealth() - 1); // бот теряет на этом 1 энергию
		    	//Все условия проверены, можно выдвигаться!
				World.world.clean(getPos());
		        setPos(point);
		        World.world.add(this);

				return true;
			}
			return false;
		}
	}
	/**
	 * Создаёт одну из мутаций
	 */
	private void mutation() {
		setGeneration(getGeneration() + 1);
		evolutionNode = evolutionNode.newNode(stepCount);
		/**Дельта, гуляет от -1 до 1*/
		double del = (0.5 - Math.random())*2;
		//TODO К сожалению первые два слишком сильно убегают вперёд
        switch (Utils.random(2, 6)) { // К сожалению 0 и 1 вырезаны. Существа перерастают и эволюция заканчивается
            case 0: //Мутирует количество энергии для деления
                this.hpForDiv *= del;
                break;
            case 1: //Мутирует максимально возможное хранилище минералов
                this.maxMP *= del;
                break;
            case 2: //Мутирует эффективность фотосинтеза
                this.photosynthesisEffect = Math.max(0, Math.min(this.photosynthesisEffect * (1+del*0.1), 4));
                break;
            case 3: //Мутирует геном
                int ma = Utils.random(0, mind.length-1); //Индекс гена
                int mc = Utils.random(0, COUNT_COMAND-1); //Его значение
                this.mind[ma] = mc;
                break;
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
        }
	}

	/**
	 * Превращает бота в органику
	 */
	private void bot2Organic() {
    	remove(); //Удаляем себя
        World.world.add(new Organic(this)); //Мы просто заменяем себя
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
	 * Родственные-ли боты?
	 * TODO Вообще пока смотрит только по ДНК, но возможно в будущем нужно смотреть будет не на 
	 *      генотип, а на фенотип!
	 * @param cell
	 * @param cell2
	 * @return
	 */
	/*private boolean isRelative(Cell bot0, Cell bot1) {
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
	}*/
	/**
	 * Родственные-ли боты?
	 * Определеяет родственников по фенотипу, по тому как они выглядят
	 * @param cell
	 * @param cell2
	 * @return
	 */
	protected boolean isRelative(CellObject cell0) {
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
				CellObject cell = World.world.get(point);
				setHealth(health - cell.getHealth());    //здоровье увеличилось на сколько осталось
	            goRed((int) -cell.getHealth());           // бот покраснел
	            cell.remove();
			} return true;
			case ENEMY:
			case FRIEND:{
				//--------- дошли до сюда, значит впереди живой бот -------------------
				Point point = fromVektorA(direction);
				AliveCell cell = (AliveCell) World.world.get(point);
				
		        long min0 = mineral;  // определим количество минералов у нас
		        long min1 = cell.mineral;  // определим количество минералов у потенциального обеда
		        long hl = cell.health;  // определим энергию у потенциального обеда
		        // если у бота минералов больше
		        if (min0 >= min1) {
		        	setMineral( min0 - min1); // количество минералов у бота уменьшается на количество минералов у жертвы
		            // типа, стесал свои зубы о панцирь жертвы
		            cell.remove(); // удаляем жертву из списков
		            long cl = hl / 2;           // количество энергии у бота прибавляется на (половину от энергии жертвы)
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
			            cell.remove(); // удаляем жертву из списков
		            	long cl = Math.max(0,(hl / 2) - 2 * min1); // вычисляем, сколько энергии смог получить бот
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
				Point point = fromVektorA(direction);
				CellObject cell = World.world.get(point);
				setHealth(health - cell.getHealth()/4);    //здоровье увеличилось
	            goRed((int) -cell.getHealth()/4);           // бот покраснел
	            cell.setHealth(cell.getHealth()*3/4); //Одну четверть отдали
			} return true;
			case ENEMY:
			case FRIEND:{
				//--------- дошли до сюда, значит впереди живой бот -------------------
				Point point = fromVektorA(direction);
				AliveCell cell = (AliveCell) World.world.get(point);
				
		        long min0 = mineral;  // определим количество минералов у нас
		        long min1 = cell.mineral / 2;  // определим количество минералов у цели,
		        							//но так как мы только кусаем - то и прорываться нам не через весь панцирь
		        long hl = cell.health;  // определим энергию у потенциального кусиха
		        //Если у цели минералов не слишком много, а у нас жизней сильно меньше - можем его кусить
		        if (min0 >= (min1/2) && (health/2 < hl)) {
		        	setMineral(min0 - min1/2); // количество минералов у бота уменьшается на количество минералов у жертвы
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
				Point point = fromVektorA(direction);
				AliveCell cell = (AliveCell) World.world.get(point);
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
				Point point = fromVektorA(direction);
				AliveCell cell = (AliveCell) World.world.get(point);
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
	

	private void clingR(DIRECTION direction) {
		clingA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}
	/**
	 * Присосаться к другой клетке
	 * @param direction
	 */
	private void clingA(DIRECTION direction) {
		OBJECT see = seeA(direction);
		if(see == OBJECT.ENEMY || see == OBJECT.FRIEND) {
			Point point = fromVektorA(direction);
		    setFriend((AliveCell) World.world.get(point));
		}
	}
	
	private void cloneR(DIRECTION direction) {
		cloneA(DIRECTION.toEnum(DIRECTION.toNum(this.direction) + DIRECTION.toNum(direction)));
	}
	private void cloneA(DIRECTION direction) {
    	setHealth(this.getHealth() - HP_FOR_DOUBLE);      // бот затрачивает 150 единиц энергии на создание копии
        if (this.getHealth() <= 0) {
            return;
        }   // если у него было меньше 150, то пора помирать

        if(seeA(direction) == OBJECT.CLEAN) {
			Point point = fromVektorA(direction);
            AliveCell newbot = new AliveCell(this,point);
            World.world.add(newbot);//Сделали потомка
            clingA(direction); // Присосались друг к дуругу
        }
	}
	
	/**
	 *  * Ищет свободные ячейки вокруг бота.
	 * Сначала прямо, потом лево право, потом зад лево/право и наконец назад
	 * @param bot
	 * @return Найденые координаты
 	 */
	public Point findEmptyDirection() {
	    for (int i = 0; i < 5; i++) {
	        Point point = fromVektorR(DIRECTION.toEnum(i));
	        OBJECT obj = World.world.test(point);
	        if (obj == OBJECT.CLEAN)
	            return point;
	        if (i != 0 && i != 4) {
	            point = fromVektorR(DIRECTION.toEnum(-i));
		        obj = World.world.test(point);
		        if (obj == OBJECT.CLEAN)
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
		g.setColor(color_DO);
		
		int r = getPos().getRr();
		int rx = getPos().getRx();
		int ry = getPos().getRy();
		if(friends.size() == 0)
			Utils.fillCircle(g,rx,ry,r);
		else
			Utils.fillSquare(g,rx,ry,r);
		if(r > 5 && friends.size() > 0) {
			g.setColor(Color.BLACK);
			synchronized (friends) {
				try {
				for(AliveCell i : friends.values()) {
					int rxc = i.getPos().getRx();
					if(getPos().x == 0 && i.getPos().x == World.MAP_CELLS.width -1)
						rxc = rx - r;
					else if(i.getPos().x == 0 && getPos().x == World.MAP_CELLS.width -1)
							rxc = rx + r;
					int ryc = i.getPos().getRy();
					g.drawLine(rx,ry, rxc,ryc);
				}
				}catch (java.util.ConcurrentModificationException e) {
					// Я хз от почему, но выскакивает!
				}
			}
		}
		if(r > 10) {
			g.setColor(Color.PINK);
			g.drawLine(rx,ry, rx+ direction.addX*r/2,ry + direction.addY*r/2);
		}
			
	}

	/**
	 * @return the health
	 */
	public long getHealth() {
		return health;
	}
	
	public void setHealth(long health) {
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
		mineral = (long) Math.min(mineral, this.maxMP);
		this.mineral = mineral;
		if(mineral < 0) mineral = 0;
		if((Legend.Graph.getMode() == Legend.Graph.MODE.MINERALS))
			repaint();
	}
	
	public void repaint() {
		switch (Legend.Graph.getMode()) {
			case MINERALS -> color_DO = new Color(0,0,(int) Math.max(0,Math.min(255, (255.0*mineral/maxMP))),evolutionNode.getAlpha());
			case GENER -> color_DO = Utils.getHSBColor(Math.max(0, (0.5*Generation/Legend.Graph.getMaxGen())), 1, 1,evolutionNode.getAlpha()/255.0);
			case YEAR -> color_DO = Utils.getHSBColor(Math.max(0, (1.0*getAge()/Legend.Graph.getMaxAge())), 1, 1,evolutionNode.getAlpha()/255.0);
			case HP -> color_DO = new Color((int) Math.min(255, (255.0*Math.max(0,health)/maxHP)),0,0,evolutionNode.getAlpha());
			case PHEN -> color_DO = new Color(phenotype.getRed(), phenotype.getGreen(), phenotype.getBlue(),evolutionNode.getAlpha());
			case DOING -> color_DO = new Color(color_DO.getRed(), color_DO.getGreen(), color_DO.getBlue(),evolutionNode.getAlpha());
		}
	}


	/**
	 * @return the processorTik
	 */
	public int getProcessorTik() {
		return processorTik;
	}



	public JSONmake toJSON(JSONmake make) {
		make.add("processorTik",processorTik);
		make.add("mind",mind);
		make.add("health",health);
		make.add("mineral",mineral);
		make.add("direction",DIRECTION.toNum(direction));

	    //=================ПАРАМЕТРЫ БОТА============
		make.add("Generation",Generation);
		make.add("GenerationTree",evolutionNode.getBranch());
		

	    //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
		make.add("phenotype",Integer.toHexString(phenotype.getRGB()));
		make.add("photosynthesisEffect",photosynthesisEffect);
		
		//===============МНОГОКЛЕТОЧНОСТЬ===================
		JSONmake[] fr = new JSONmake[friends.size()];
		Object[] points = friends.keySet().toArray();
		for (int i = 0; i < fr.length; i++)
			fr[i] = ((Point)points[i]).toJSON();
		make.add("friends",fr);
		return make;
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
}
