package MapObjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import MapObjects.Poison.TYPE;
import MapObjects.dna.Birth;
import MapObjects.dna.CommandList;
import MapObjects.dna.CreatePoisonA;
import MapObjects.dna.DNA;
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
	/**Начальный уровень здоровья клеток*/
	private static final int START_HP = 5;
	/**Начальный уровень минералов клеток*/
	private static final int START_MP = 5;
	/**Сколько нужно жизней для размножения, по умолчанию*/
	public static final int MAX_HP = 999;
	/**Сколько можно сохранить минералов*/
	public static final int MAX_MP = 999;
	/**На сколько организм тяготеет к фотосинтезу (0-4)*/
	private static final double DEF_PHOTOSIN = 2;
	/**Столько здоровья требуется клетке для жизни на ход*/
	private static final long HP_PER_STEP = 4;
	/**Для изменения цвета*/
	public enum ACTION {
		/**Съесть органику - красный*/
		EAT_ORG("Есть органику",255,0,0,1), 
		/**Съесть минералы - синий*/
		EAT_MIN("Есть минералы",0,0,255,1), 
		/**Фотосинтез - зелёный*/
		EAT_SUN("Фотосинтез",0,255,0,1), 
		/**Поделиться - оливковый, грязно-жёлтый*/
		GIVE("Отдавать ресурсы",128,128,0,0.5), 
		/**Принять подачку - морской волны*/
		RECEIVE("Получать ресурсы",0,128,128,0.5), 
		/**Сломать мою ДНК - чёрный*/
		BREAK_DNA("Ломаю ДНК",0,0,0,1), 
		/**Ничего не делать - серый*/
		NOTHING("Ничего не делаю",128,128,128,0.04);
		public static final ACTION[] staticValues = ACTION.values();
		public static int size() {return staticValues.length;}
		
		ACTION(String des, int rc, int gc, int bc, double power) {r=rc;g=gc;b=bc;p=power;description=des;}
		public final int r;
		public final int g;
		public final int b;
		private final double p;
		public final String description;
	};
	
	//=================Внутреннее состояние бота
	/**Мозг*/
	private DNA dna;
    /**Жизни*/
    private double health = START_HP;
    //Минералы
    private long mineral = START_MP;
    /**Направление движения*/
    public Point.DIRECTION direction = Point.DIRECTION.UP;
    //Защитный покров ДНК, он мешает изменить Нашу ДНК
    private int DNA_wall = 0;
    /**Тип яда к которому клетка устойчива*/
    private Poison.TYPE poisonType = Poison.TYPE.UNEQUIPPED;
    /**Сила устойчивости к яду*/
    private int poisonPower = 0;
    /**Плавучесть. Меняется от -10 до 10 Где -10 - тонуть каждый ход, 10 - всплывать каждый ход, 1 - тонуть каждые 10 ходов*/
    private int buoyancy = 0;
    /**Специальный флаг, показывает, что бот на этом ходу спит*/
    private boolean isSleep = false;
    /**Цвет с большим числом значений*/
    private class DColor{
    	double r, g, b, a; 
    	DColor(double r,double g, double b, double a){this.r=r;this.g=g;this.b=b;this.a=a;}
    	DColor(double r,double g, double b){this(r,g,b,255);}
    	DColor(){this(255,255,255);}
    	void addR(double add) {r = Utils.betwin(0.0, r+add, 255.0);}
    	void addG(double add) {g = Utils.betwin(0.0, g+add, 255.0);}
    	void addB(double add) {b = Utils.betwin(0.0, b+add, 255.0);}
    	int getR() {return (int) Utils.betwin(0.0, r, 255.0);}
    	int getG() {return (int) Utils.betwin(0.0, g, 255.0);}
    	int getB() {return (int) Utils.betwin(0.0, b, 255.0);}
    	int getA() {return (int) Utils.betwin(0.0, a, 255.0);}
		public Color getC() {return new Color(getR(),getG(),getB(),getA());}
    };
    private DColor color_cell = new DColor();
    
    //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
    /**Поколение (мутационное). Другими словами - как далеко клетка ушла от изначальной*/
    private int Generation = 0;
    /**Фенотип бота*/
    public Color phenotype = new Color(128,128,128);
    //Показывает на сколько организм тяготеет к фотосинтезу
    public double photosynthesisEffect = DEF_PHOTOSIN;
    /**Дерево эволюции*/
    public Node evolutionNode = null;
    
    //===============Параметры братства, многоклеточность=======
    private final Map<Point,AliveCell> friends = new HashMap<>(DIRECTION.size());
    
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
    	super(cell.getStepCount(), LV_STATUS.LV_ALIVE);
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
	
	@Override
	public void step() {
		for (int cyc = 0; (cyc < 15); cyc++)
			if(dna.get().execute(this))
				break;
		if(getBuoyancy() < 0 && getAge() % (getBuoyancy()+101) == 0)
			moveD(DIRECTION.DOWN);
		else if(getBuoyancy() > 0 && getAge() % (101-getBuoyancy()) == 0)
			moveD(DIRECTION.UP);
        
		if(isSleep) {
			setSleep(false);
	        addHealth(-1);          //Спать куда эффективнее
		} else {
	        addHealth(- HP_PER_STEP); //Пожили - устали
		}
        if (this.getHealth() > MAX_HP)
        	Birth.birth(this);
    	if(!getFriends().isEmpty())
			clingFriends();
        // если бот находится на глубине ниже половины
        // то он автоматом накапливает минералы, но не более MAX_MP
        if (this.getPos().getY() >= (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL)) {
        	double realLv = this.getPos().getY() - (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL);
        	double dist = Configurations.MAP_CELLS.height * (1 - Configurations.LEVEL_MINERAL);
            this.setMineral(Math.round(this.getMineral() + Configurations.CONCENTRATION_MINERAL * (realLv/dist) * (5 - this.photosynthesisEffect))); //Эффективный фотосинтез мешает нам переваривать пищу
        }
        if(getAge() % 50 == 0) {
            color(ACTION.NOTHING, color_cell.r+color_cell.g+color_cell.b);
        }
	}

	/**Поделиться с друзьями всем, что имеем*/
	private void clingFriends() {
		// Колония безвозмездно делится всем, что имеет
		double allHp = getHealth();
		long allMin = getMineral();
		int allDNA_wall = DNA_wall;
		int friendCount = getFriends().size() + 1;
		int maxToxic = poisonPower;
		Point delP = null;
		for (Entry<Point, AliveCell> cell_e : getFriends().entrySet()) {
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
			getFriends().remove(delP);
		allHp /= friendCount;
		allMin /= friendCount;
		allDNA_wall /= friendCount;
		if (allMin > getMineral())
			color(ACTION.RECEIVE, allMin - getMineral());
		else
			color(ACTION.GIVE, getMineral() - allMin);
		setHealth(allHp);
		setMineral(allMin);
		DNA_wall = allDNA_wall;
		for (AliveCell friendCell : getFriends().values()) {
			if (getAge() % 100 == 0) {
				if (allHp > friendCell.getHealth())
					friendCell.color(ACTION.RECEIVE, allHp - friendCell.getHealth());
				else
					friendCell.color(ACTION.GIVE, friendCell.getHealth() - allHp);
				if (allMin > friendCell.getMineral())
					friendCell.color(ACTION.RECEIVE, allMin - friendCell.getMineral());
				else
					friendCell.color(ACTION.GIVE, friendCell.getMineral() - allMin);
			}
			friendCell.setHealth(allHp);
			friendCell.setMineral(allMin);
			friendCell.DNA_wall = allDNA_wall;
			if(friendCell.poisonType == poisonType)
				friendCell.poisonPower += friendCell.poisonPower < maxToxic ? 1 : 0;
		}
	}

    
	/**
	 * Убирает бота с карты и проводит все необходимые процедуры при этом
	 */
    @Override
    public void destroy() {
		evolutionNode.remove();
		super.destroy();
    }

	/**
	 * Перемещает бота в абсолютном направлении
	 * @param direction
	 * @return
	 */
	@Override
	public boolean move(DIRECTION direction) {
		if(getFriends().isEmpty()) {
			return super.move(direction);
		} else {
			//Многоклеточный. Тут логика куда интереснее!
			OBJECT see = super.see(direction);
			if(see.isEmptyPlase){
				//Туда двинуться можно, уже хорошо.
				Point point = getPos().next(direction);

				/**
				 * Правило!
				 * Мы можем двигаться в любую сторону,
				 * 	если от нас до любого из наших друзей будет ровно 1 клетка
				 * То есть если delX или delY > 1.
				 * 	При этом следует учесть, что delX для клеток с х = 0 и х = край экрана - будет ширина экрана - 1
				 * 	Можно поглядеть код точки. В таком случае они всё равно будут рядом по х.
				 */
		    	for (AliveCell cell : getFriends().values() ) {
		    		int delx = Math.abs(point.getX() - cell.getPos().getX());
		    		int dely = Math.abs(point.getY() - cell.getPos().getY());
		    		if(dely > 1 || (delx > 1 && delx != Configurations.MAP_CELLS.width-1))
		    			return false;
		    	}
		    	//Все условия проверены, можно выдвигаться!
				return super.move(direction);
			}
			return false;
		}
	}
	/**
	 * Создаёт одну из мутаций
	 */
	protected void mutation() {
		setGeneration(getGeneration() + 1);
		/**Дельта, гуляет от -1 до 1*/
		double del = (0.5 - Math.random())*2;
        switch (Utils.random(2, 10)) { // К сожалению 0 и 1 вырезаны.
            case 2 -> //Мутирует эффективность фотосинтеза
                this.photosynthesisEffect = Math.max(0, Math.min(this.photosynthesisEffect * (1+del*0.1), 4));
            case 3 -> { //Мутирует геном
                int ma = Utils.random(0, dna.size-1); //Индекс гена
                int mc = Utils.random(0, CommandList.COUNT_COMAND); //Его значение
                dna = dna.update(ma, mc);
            	}
            case 4 -> {
				//Мутирует красный цвет
				int red = phenotype.getRed();
				red = (int) Math.max(0, Math.min(red + del * 10, 255));
				phenotype = new Color(red,phenotype.getGreen(), phenotype.getBlue(), phenotype.getAlpha());
			}
            case 5 -> {
				//Мутирует зелёный цвет
				int green = phenotype.getGreen();
				green = (int) Math.max(0, Math.min(green + del * 10, 255));
				phenotype = new Color(phenotype.getRed(),green, phenotype.getBlue(), phenotype.getAlpha());
			}
            case 6 -> {
				//Мутирует синий цвет
				int blue = phenotype.getGreen();
				blue = (int) Math.max(0, Math.min(blue + del * 10, 255));
				phenotype = new Color(phenotype.getRed(),phenotype.getGreen(), blue, phenotype.getAlpha());
			}
            case 7 -> { //Один геном дублируется
            	if(dna.size + 1 <= MAX_MINDE_SIZE) {
                	int mc = Utils.random(0, dna.size - 1); //Ген, который будет дублироваться
            		dna = dna.doubling(mc);
            	}
            }
            case 8 -> { //Геном укорачивается на один ген
            	int mc = Utils.random(0, dna.size - 1); //Ген, который будет удалён
            	dna = dna.compression(mc);
            }
            case 9 -> { // Смена типа яда на который мы отзываемся
            	poisonType = TYPE.toEnum(Utils.random(0, TYPE.size()));
            	if(poisonType != TYPE.UNEQUIPPED)
            		poisonPower = Utils.random(1, (int) (CreatePoisonA.HP_FOR_POISON * 2 / 3));
            	else
            		poisonPower = 0; //К этому у нас защищённости ни какой
            }
            case 10 -> { //Мутирует вектор прерываний
                int ma = Utils.random(0, dna.interrupts.length-1); //Индекс в векторе
                int mc = Utils.random(0, dna.size-1); //Его значение
                dna.interrupts[ma] = mc;
            }
        }
		evolutionNode = evolutionNode.newNode(this,getStepCount());
	}

	/**
	 * Превращает бота в органику
	 */
	public void bot2Organic() {
		try {
			destroy(); // Удаляем себя
		} catch (CellObjectRemoveException e) {
	    	Configurations.world.add(new Organic(this)); //Мы просто заменяем себя
	    	throw e;
		}
	}
	/**
	 * Превращает бота в стену
	 */
	public void bot2Wall() {
		try {
			destroy(); // Удаляем себя
		} catch (CellObjectRemoveException e) {
	    	Configurations.world.add(new Fossil(this)); //Мы просто заменяем себя
	    	throw e;
		}
	}

	/**
	 * Подглядывает за бота в абсолютном направлении
	 * @param direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	@Override
	public OBJECT see(DIRECTION direction) {
		OBJECT see = super.see(direction);
		if(see == OBJECT.POISON){
			Point point = getPos().next(direction);
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
	 * @param cell0 - клетка, с коротой сравнивается
	 * @return
	 */
	@Override
	protected boolean isRelative(CellObject cell0) {
		if (cell0 instanceof AliveCell bot0) {
			return bot0.dna.equals(this.dna);
		} else {
			return false;
		}
	}

	@Override
	public boolean toxinDamage(TYPE type,int damag) {
		if(type == getPosionType() && getPosionPower() >= damag) {
			poisonPower = getPosionPower() + 1;
			return false;
		} else {
			switch (type) {
				case PINK-> {return false;}
				case YELLOW->{
					if(type == getPosionType())	//Родной яд действует слабже
						damag /= 2;
					var isLife = damag < getHealth();
					if (isLife)
						addHealth(-damag);
					return !isLife;
				}
				case BLACK->{
					mutation();
					return false;
				}
				case UNEQUIPPED -> throw new UnsupportedOperationException("Unimplemented case: " + type);
			}
		}
		return true;
	}
	
	/**
	 * Раскрашивает бота в зависимости от действия
	 * @param act
	 * @param num
	 */
	public void color(ACTION act, double num) {
		if(Legend.Graph.getMode() != Legend.Graph.MODE.DOING)
			return;
		if(act.p != 1)
			num *= act.p;
		color_cell.addR(color_cell.r>act.r?-num:num);color_cell.addG(color_cell.g>act.g?-num:num);color_cell.addB(color_cell.b>act.b?-num:num);
		color_DO = color_cell.getC();
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(color_DO);
		
		int r = getPos().getRr();
		int rx = getPos().getRx();
		int ry = getPos().getRy();
		if(getFriends().isEmpty())
			Utils.fillCircle(g,rx,ry,r);
		else if(r < 5) 
			Utils.fillSquare(g,rx,ry,r);
		else {
			Utils.fillCircle(g,rx,ry,r);
			synchronized (getFriends()) {
				try {
					//Приходится рисовать в два этапа, иначе получается ужас страшный.
					//Этап первый - основные связи
					
				Graphics2D g2 = (Graphics2D) g;
			    Stroke oldStr = g2.getStroke();
				g.setColor(color_DO);
				g2.setStroke(new BasicStroke(r/2));
				for(AliveCell i : getFriends().values()) {
					int rxc = i.getPos().getRx();
					if(getPos().getX() == 0 && i.getPos().getX() == Configurations.MAP_CELLS.width -1)
						rxc = rx - r;
					else if(i.getPos().getX() == 0 && getPos().getX() == Configurations.MAP_CELLS.width -1)
							rxc = rx + r;
					int ryc = i.getPos().getRy();
					
					int delx = rxc - rx;
					int dely = ryc - ry;
					g.drawLine(rx,ry, rx+delx/3,ry+dely/3);
				}
				g2.setStroke(oldStr);
				g.setColor(Color.BLACK);
				//Этап второй, всё тоже самое, но теперь лишь тонкие линии
				for(AliveCell i : getFriends().values()) {
					int rxc = i.getPos().getRx();
					if(getPos().getX() == 0 && i.getPos().getX() == Configurations.MAP_CELLS.width -1)
						rxc = rx - r;
					else if(i.getPos().getX() == 0 && getPos().getX() == Configurations.MAP_CELLS.width -1)
							rxc = rx + r;
					int ryc = i.getPos().getRy();
					//А теперь рисуем тонкую линию, чтобы видно было как они выглядят
					g.drawLine(rx,ry, rxc,ryc);
				}
				}catch (java.util.ConcurrentModificationException e) {/* Выскакивает, если кто-то из наших друзей погиб*/	}
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
	@Override
	public double getHealth() {
		return health;
	}
	
	@Override
	public void setHealth(double health) {
		this.health=health;
		if(this.health < 0)
			bot2Organic();
	}

	/**
	 * @param years the years to set
	 */
	public void setAge(int years) {
		super.setAge(years);
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
		this.mineral = Math.min(mineral, MAX_MP);
	}
	
	@Override
	public void repaint() {
		switch (Legend.Graph.getMode()) {
			case MINERALS -> color_DO = new Color(0,0,(int) Utils.betwin(0, (255.0*mineral/MAX_MP),255),evolutionNode.getAlpha());
			case GENER -> color_DO =Legend.Graph.generationToColor(Generation,evolutionNode.getAlpha()/255.0);
			case YEAR -> color_DO = Legend.Graph.AgeToColor(getAge(),evolutionNode.getAlpha()/255.0);
			case HP -> color_DO = new Color((int) Math.min(255, (255.0*Math.max(0,health)/MAX_HP)),0,0,evolutionNode.getAlpha());
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
		if (friend == null || getFriends().get(friend.getPos()) != null)
			return;
		getFriends().put(friend.getPos(), friend);
		friend.getFriends().put(getPos(), this);
	}

	@Override
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
		JSON[] fr = new JSON[getFriends().size()];
		Object[] points = getFriends().keySet().toArray();
		for (int i = 0; i < fr.length; i++)
			fr[i] = ((Point)points[i]).toJSON();
		make.add("friends",fr);
		return make;
	}

	/**
	 * @return тип яда, к которому устойчива клетка
	 */
	public Poison.TYPE getPosionType() {
		return poisonType;
	}

	/**
	 * @return на сколько много очков урона клетка может игнорировать
	 */
	public int getPosionPower() {
		return poisonPower;
	}
	public void setPosionPower(int poisonPower) {
		this.poisonPower = poisonPower;
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

	public void setSleep(boolean isSleep) {
		this.isSleep = isSleep;
	}

	public void setBuoyancy(int buoyancy) {
		this.buoyancy = Utils.betwin(-100, buoyancy, 100);
	}

	public Map<Point,AliveCell> getFriends() {
		return friends;
	}
	public int getDNA_wall() {
		return DNA_wall;
	}
	public void setDNA_wall(int DNA_wall) {
		this.DNA_wall=DNA_wall;
	}

	public void DNAupdate(int ma, int mc) {
		dna = dna.update(ma, mc);
	}
}
