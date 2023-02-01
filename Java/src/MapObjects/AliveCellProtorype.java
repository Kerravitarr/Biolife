package MapObjects;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MapObjects.AliveCellProtorype.Specialization;
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
	public static final int MAX_HP = 9999;
	/**Сколько можно сохранить минералов*/
	public static final int MAX_MP = 9999;
	/**На сколько организм тяготеет к фотосинтезу (0-4)*/
	protected static final double DEF_PHOTOSIN = 2;
	/**Столько здоровья требуется клетке для жизни на ход*/
	public static final long HP_PER_STEP = 4;
	/**Для изменения цвета*/
	public enum ACTION {
		/**Съесть органику - красный*/
		EAT_ORG(255,0,0,1), 
		/**Съесть минералы - синий*/
		EAT_MIN(0,0,255,1), 
		/**Фотосинтез - зелёный*/
		EAT_SUN(0,255,0,1), 
		/**Поделиться - оливковый, грязно-жёлтый*/
		GIVE(128,128,0,0.5), 
		/**Принять подачку - морской волны*/
		RECEIVE(0,128,128,0.5), 
		/**Сломать мою ДНК - чёрный*/
		BREAK_DNA(0,0,0,1), 
		/**Ничего не делать - серый*/
		NOTHING(128,128,128,0.04);
		public static final ACTION[] staticValues = ACTION.values();
		public static int size() {return staticValues.length;}
		
		ACTION(int rc, int gc, int bc, double power) {r=rc;g=gc;b=bc;p=power;description = Configurations.getProperty(getClass(), super.name());}
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
			PHOTOSYNTHESIS(		118),
			DIGESTION(			316),
			MINERALIZATION(		240),
			MINERAL_PROCESSING(	188),
			FERMENTATION(		63),
			ASSASSINATION(		6),
			ACCUMULATION(		271), 
			;
			
			public static final TYPE[] staticValues = TYPE.values();
			public static int size() {return staticValues.length;}
			
			TYPE(float colorByDegree) {
				lname = Configurations.getProperty(getClass(), MessageFormat.format("{0}.Long", super.name()));
				sname = Configurations.getProperty(getClass(), MessageFormat.format("{0}.Shot", super.name()));
				color = colorByDegree / 360f;
			}
			
			public String toString() {return lname;}
			/**Возвращает краткое описание типа*/
			public String toSString() {return sname;}
			/**Полное имя типа*/
			private final String lname;
			/**Краткое имя типа*/
			private final String sname;
			/**Цвет специализации [0,1]*/
			public final float color;
		}
		/**Максимальная специализация*/
		private static final int MAX_SPECIALIZATION = 100;
		/**Ведущая специализация*/
		private TYPE main = TYPE.PHOTOSYNTHESIS;
		
		Specialization() {
			super(TYPE.size()); 
			for(var i : TYPE.staticValues)
				put(i, MAX_SPECIALIZATION / TYPE.size());
			var summ = 0;
			for(var i : this.values())
				summ += i;
			put(TYPE.PHOTOSYNTHESIS, get(TYPE.PHOTOSYNTHESIS) + (MAX_SPECIALIZATION - summ));
			set(TYPE.PHOTOSYNTHESIS,MAX_SPECIALIZATION / 2);
			updateColor();
		}
		
		/**Копируем специализацию нашего предка*/
		public Specialization(AliveCell cell) {
			this.putAll(cell.getSpecialization());
			phenotype = cell.phenotype;
		}
		
		/**Копируем специализацию нашего предка*/
		public Specialization(JSON json) {
	    	List<Integer> keys = json.getA("keys");
	    	List<Integer> vals = json.getA("vals");
	    	
	    	for(int i = 0 ; i < keys.size() ; i++) {
	    		put(TYPE.staticValues[keys.get(i)],vals.get(i));
	    	}

			updateColor();
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
			float angle = (float) (Math.acos(x / lenght) / (2 * Math.PI));
			if(y < 0) angle = 1f - angle;
			phenotype = Utils.getHSBColor(angle, 1f, 0.5 + lenght / 200, 1f);
		}
		/**Сохраняет новое значение специализации*/
		public void set(TYPE type, int co) {
			if(get(type) == co) {
				return;
			} else {
				var del = co - get(type);
				var summ = MAX_SPECIALIZATION - get(type);
				var max = 0;
				for(var i : entrySet()) {
					if(i.getKey() == type) continue;
					var nVal = (int) Utils.betwin(0.0, Math.round(((double)(i.getValue() * summ - del * i.getValue())) / summ), MAX_SPECIALIZATION );
					if(nVal >= max) {
						max = nVal;
						main = i.getKey();
					}
					put(i.getKey(), nVal );
				}
				summ = 0;
				for(var i : entrySet()) {
					if(i.getKey() != type)
						summ += i.getValue();
				}
				var nVal = MAX_SPECIALIZATION - summ;
				if(nVal >= max)
					main = type;
				put(type, nVal);
			}
			updateColor();
		}

		public JSON toJSON() {
			JSON make = new JSON();
			int [] keys = new int[size()];
			int [] vals = new int[size()];
			var i = 0;
			for(var s : entrySet()) {
				keys[i] = s.getKey().ordinal();
				vals[i] = s.getValue();
				i++;
			}
			make.add("keys", keys);
			make.add("vals", vals);
			return make;
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
    /**Показывает сколько ошибок допускает бот, прежде чем сказать - наши ДНК разные!*/
    protected int tolerance = 2;
    
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
	 * @param mineral новый кусочек минералов (отрицательный, если отнимаем)
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
			case POISON -> {
				var rg = (int) Utils.betwin(0, getPosionPower() / Poison.MAX_TOXIC, 1.0) * 255;
				switch (getPosionType()) {
					case BLACK -> color_DO = new Color(255-rg, 255-rg, 255-rg);
					case PINK -> color_DO = new Color(rg, rg / 2, rg / 2);
					case YELLOW -> color_DO = new Color(rg, rg, 0);
					case UNEQUIPPED -> color_DO = Color.BLACK;
				}
			}
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
		return getSpecialization().get(type) / ((double) Specialization.MAX_SPECIALIZATION);
	}
	public Specialization getSpecialization() {
		return specialization;
	}
	public int getFoodTank() {
		return foodTank;
	}
	public void setFoodTank(int foodTank) {
		this.foodTank = foodTank;
	}

	/**Добавляет или уменьшает размер танка для еды*/
	public void addFoodTank(int add) {
		foodTank += add;
	}
	public int getMineralTank() {
		return mineralTank;
	}
	public void setMineralTank(int mineralTank) {
		this.mineralTank = foodTank;
	}

	/**Добавляет или уменьшает размер танка для минералов*/
	public void addMineralTank(int add) {
		mineralTank += add;
	}
	
	/**Возвращает количество минералов вокруг*/
	public double mineralAround() {
		double realLv = getPos().getY() - (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL);
		double dist = Configurations.MAP_CELLS.height * (1 - Configurations.LEVEL_MINERAL);
		return Configurations.CONCENTRATION_MINERAL * (realLv / dist) * 10 * get(Specialization.TYPE.MINERALIZATION);
	}
	/**Возвращает количество солнца вокруг*/
	public double sunAround() {
		//Эффективность
		var eff = get(AliveCellProtorype.Specialization.TYPE.PHOTOSYNTHESIS);
		//+5 бонусных частичек света при наличии миниралов
        double t = 5 * getMineral() / AliveCell.MAX_MP;	
        //Ну и энергию от солнца не забываем
        double hlt = Configurations.sun.getEnergy(getPos()) + t;
		return hlt * eff;
	}
}
