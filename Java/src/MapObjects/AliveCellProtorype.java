package MapObjects;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import MapObjects.dna.DNA;
import Utils.JSON;
import Utils.Utils;
import main.Configurations;
import main.EvolutionTree.Node;
import main.Point;
import main.Point.DIRECTION;
import panels.Legend;

/**
 * Прототип живой клетки. Тут все константы, переменные и вложенные классы.
 * @author Kerravitarr
 *
 */

public abstract class AliveCellProtorype extends CellObject{	

	//КОНСТАНТЫ
	/**Размер мозга изначальный*/
	public static final int DEF_MINDE_SIZE = 64;
	/**Размер мозга максимальный, чтобы небыло взрывного роста и поедания памяти*/
	public static final int MAX_MINDE_SIZE = 1024;
	/**Начальный уровень здоровья клеток*/
	protected static final int START_HP = 5;
	/**Начальный уровень минералов клеток*/
	protected static final int START_MP = 5;
	/**Сколько нужно жизней для размножения, по умолчанию*/
	public static final int MAX_HP = 999;
	/**Сколько можно сохранить минералов*/
	public static final int MAX_MP = 999;
	/**На сколько организм тяготеет к фотосинтезу (0-4)*/
	protected static final double DEF_PHOTOSIN = 2;
	/**Столько здоровья требуется клетке для жизни на ход*/
	protected static final long HP_PER_STEP = 4;
	/**Для изменения цвета*/
	public enum ACTION {
		/**Съесть органику - красный*/
		EAT_ORG(Configurations.getHProperty(ACTION.class,"EAT_ORG"),255,0,0,1), 
		/**Съесть минералы - синий*/
		EAT_MIN(Configurations.getHProperty(ACTION.class,"EAT_MIN"),0,0,255,1), 
		/**Фотосинтез - зелёный*/
		EAT_SUN(Configurations.getHProperty(ACTION.class,"EAT_SUN"),0,255,0,1), 
		/**Поделиться - оливковый, грязно-жёлтый*/
		GIVE(Configurations.getHProperty(ACTION.class,"GIVE"),128,128,0,0.5), 
		/**Принять подачку - морской волны*/
		RECEIVE(Configurations.getHProperty(ACTION.class,"RECEIVE"),0,128,128,0.5), 
		/**Сломать мою ДНК - чёрный*/
		BREAK_DNA(Configurations.getHProperty(ACTION.class,"BREAK_DNA"),0,0,0,1), 
		/**Ничего не делать - серый*/
		NOTHING(Configurations.getHProperty(ACTION.class,"NOTHING"),128,128,128,0.04);
		public static final ACTION[] staticValues = ACTION.values();
		public static int size() {return staticValues.length;}
		
		ACTION(String des, int rc, int gc, int bc, double power) {r=rc;g=gc;b=bc;p=power;description=des;}
		public final int r;
		public final int g;
		public final int b;
		protected final double p;
		public final String description;
	};
    /**Цвет с большим числом значений*/
    protected class DColor{
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
    
	/**
	 * Специализиация клетки
	 * Представляет собой карту, - пары специализации и её выраженности [0,100]. В сумме все специализации дают тоже 100
	 * @author Kerravitarr
	 *
	 */
	public class Specialization extends HashMap<Specialization.TYPE,Integer>{
		public enum TYPE{
			PHOTOSYNTHESIS(		Configurations.getHProperty(TYPE.class,"PHOTOSYNTHESIS"),	360f*2/7),
			DIGESTION(			Configurations.getHProperty(TYPE.class,"DIGESTION"),			360f*3/7),
			MINERALIZATION(		Configurations.getHProperty(TYPE.class,"MINERALIZATION"),	360f*5/7),
			MINERAL_PROCESSING(	Configurations.getHProperty(TYPE.class,"MINERAL_PROCESSING"),360f*4/7),
			FERMENTATION(		Configurations.getHProperty(TYPE.class,"FERMENTATION"),		360f*1/7),
			ASSASSINATION(		Configurations.getHProperty(TYPE.class,"ASSASSINATION"),		360f*0/7),
			ACCUMULATION(		Configurations.getHProperty(TYPE.class,"ACCUMULATION"),		360f*6/7),
			;
			
			public static final TYPE[] staticValues = TYPE.values();
			public static int size() {return staticValues.length;}
			
			TYPE(String n, float c) {name = n; color = c/360f;}
			
			public String toString() {return name;}
			
			private String name;
			/**Цвет специализации [0,1]*/
			private float color;
		}
		
		Specialization() {
			super(TYPE.size()); 
			for(var i : TYPE.staticValues)
				this.put(i, 100 / TYPE.size());
			var summ = 0;
			for(var i : this.values())
				summ += i;
			this.put(TYPE.PHOTOSYNTHESIS, get(TYPE.PHOTOSYNTHESIS) + (100 - summ));
			set(TYPE.PHOTOSYNTHESIS,50);
			updateColor();
		}
		
		/**Копируем специализацию нашего предка*/
		public Specialization(AliveCell cell) {
			this.putAll(cell.specialization);
			phenotype = cell.phenotype;
		}

		private void updateColor() {
			float x = 0f;
			float y = 0f;
			for(var i : this.entrySet()){
				var ix = i.getValue() * Math.cos(2 * Math.PI * i.getKey().color);
				var iy = i.getValue() * Math.sin(2 * Math.PI * i.getKey().color);
				x += ix;
				y += iy;
			}
			float lenght = (float) Math.sqrt(x * x + y * y);
			float angle = (float) (Math.acos(x / lenght) / (Math.PI));
			if(y < 0) angle = 1f - angle;
			angle = (float) Math.asin(y / lenght);
			phenotype = Utils.getHSBColor(angle, 1f, lenght / 100, 1f);
		}
		/**Сохраняет новое значение специализации*/
		public void set(TYPE type, int co) {
			if(get(type) == co) {
				return;
			} else if(get(type) < co) {	//Улучшили специализацию
				var del = co - get(type);
				this.put(type, co);
				while(del > 0) {
					for(var i : entrySet()) {
						if(i.getKey() == type || i.getValue() == 0) continue;
						del--;
						this.put(i.getKey(), i.getValue() - 1);
						if(del == 0) break;
					}
				}
			} else { // Ухудшили спциализацию
				var del = get(type) - co;
				this.put(type, co);
				while(del > 0) {
					for(var i : entrySet()) {
						if(i.getKey() == type || i.getValue() == 100) continue;
						del--;
						this.put(i.getKey(), i.getValue() + 1);
						if(del == 0) break;
					}
				}
			}
		}

	}
	
	//=================Внутреннее состояние бота
	/**Мозг*/
	protected DNA dna;
    /**Жизни*/
	protected double health = START_HP;
    /**Минералы*/
	protected long mineral = START_MP;
    /**Направление движения*/
    public Point.DIRECTION direction = Point.DIRECTION.UP;
    /**Защитный покров ДНК, он мешает изменить Нашу ДНК*/
    protected int DNA_wall = 0;
    /**Тип яда к которому клетка устойчива*/
    protected Poison.TYPE poisonType = Poison.TYPE.UNEQUIPPED;
    /**Сила устойчивости к яду*/
    protected int poisonPower = 0;
    /**Плавучесть. Меняется от -100 до 10 Где -100 - тонуть каждый ход, 100 - всплывать каждый ход, 1 - тонуть каждые 100 ходов*/
    protected int buoyancy = 0;
    /**Специальный флаг, показывает, что бот на этом ходу спит*/
    protected boolean isSleep = false;
    /**Цвет бота по действиям*/
    protected DColor color_cell = new DColor();
    /**Внутреннее хранилище энергии*/
    protected int foodTank = 0;
    /**Внутреннее хранилище минералов*/
    protected int mineralTank = 0;
    /**Специализация бота*/
    protected Specialization specialization;
    
    //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
    /**Поколение (мутационное). Другими словами - как далеко клетка ушла от изначальной*/
    protected int Generation = 0;
    /**Фенотип бота. Зависит от специализации*/
    public Color phenotype;
    /**Дерево эволюции*/
    public Node evolutionNode = null;
    
    //===============Параметры братства, многоклеточность=======
    protected final Map<Point,AliveCell> friends = new HashMap<>(DIRECTION.size());

    
    
    
    
	public AliveCellProtorype(JSON cell) {
		super(cell);
	}
	public AliveCellProtorype(long stepCount,LV_STATUS alive){
		super(stepCount, alive);
	}
	
	/**
	 * @return the health
	 */
	@Override
	public double getHealth() {
		return health;
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
	 * Добавляет или отнимает минералы у клетки
	 * @param новый кусочек минералов (отрицательный, если отнимаем)
	 */
	public void addMineral(long mineral) {
		setMineral(getMineral() + mineral);
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
	
	/**
	 * Возвращает процент специализированности в данном типе
	 * @param type тип специализации
	 * @return число [0,1]
	 */
	public double get(Specialization.TYPE type) {
		return specialization.get(type) / 100d;
	}
}
