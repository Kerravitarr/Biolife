package MapObjects;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.List;

import MapObjects.AliveCellProtorype.Specialization;
import MapObjects.dna.DNA;
import Utils.JSON;
import Utils.Utils;
import Calculations.Configurations;
import Calculations.EvolutionTree.Node;
import Calculations.Point;
import Calculations.Point.DIRECTION;
import java.util.Arrays;

/**
 * Прототип живой клетки. Тут все константы, переменные и вложенные классы.
 * @author Kerravitarr
 *
 */
public abstract class AliveCellProtorype extends CellObject{
	public interface AliveCellI{
		/**@return Возваращет сколько ХП у объекта */
		public double getHealth();
		/**@return Возваращет сколько минералов у объекта */
		public long getMineral();
		/**@param h Сколько добавить энергии объекту*/
		public void addHealth(double h);
		/** @param mineral новый кусочек минералов (отрицательный, если отнимаем)*/
		public void addMineral(long mineral);
		/**@return количество слизи. Если слизь есть - это затрудняет присасывание к клетке*/
		public int getMucosa();
	}

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
	/**Столько здоровья требуется клетке для жизни на ход*/
	public static final long HP_PER_STEP = 4;
	/**Для изменения цвета*/
	public enum ACTION {
		/**Съесть органику - красный*/
		EAT_ORG(255,0,0,0.1), 
		/**Съесть минералы - синий*/
		EAT_MIN(0,0,255,0.1), 
		/**Фотосинтез - зелёный*/
		EAT_SUN(0,255,0,0.1), 
		/**Поделиться - оливковый, грязно-жёлтый*/
		GIVE(128,128,0,0.1), 
		/**Принять подачку - морской волны*/
		RECEIVE(0,128,128,0.1), 
		/**Сломать мою ДНК - чёрный*/
		BREAK_DNA(0,0,0,0.1), 
		/**Ничего не делать - серый*/
		NOTHING(128,128,128,0.01);
		public static final ACTION[] staticValues = ACTION.values();
		public static int size() {return staticValues.length;}
		/**Описание цвета действия
		 * @param rc красный компонент
		 * @param gc зелёный компонент
		 * @param bc синий компонент
		 * @param power сколько +1hp прибавляет к цвету
		 */
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
	public class Specialization{
		public enum TYPE{
			PHOTOSYNTHESIS(		118),
			DIGESTION(			316),
			MINERALIZATION(		240),
			MINERAL_PROCESSING(	188),
			FERMENTATION(		63),
			ASSASSINATION(		6),
			ACCUMULATION(		271), 
			;
			
			public static final TYPE[] values = TYPE.values();
			public static int size() {return values.length;}
			
			TYPE(float colorByDegree) {
				lname = Configurations.getProperty(getClass(), MessageFormat.format("{0}.Long", super.name()));
				sname = Configurations.getProperty(getClass(), MessageFormat.format("{0}.Shot", super.name()));
				color = colorByDegree / 360f;
			}
			
			@Override
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
		private final int[] map = new int[Specialization.TYPE.size()];
		
		/**Максимальная специализация*/
		public static final int MAX_SPECIALIZATION = 100;
		/**Ведущая специализация*/
		private TYPE main = TYPE.PHOTOSYNTHESIS;
		
		Specialization() {
			for(var i : TYPE.values)
				put(i, MAX_SPECIALIZATION / TYPE.size());
			var summ = 0;
			for(var i : map)
				summ += i;
			put(TYPE.PHOTOSYNTHESIS, get(TYPE.PHOTOSYNTHESIS) + (MAX_SPECIALIZATION - summ));
			set(TYPE.PHOTOSYNTHESIS,50);
			updateColor();
		}
		
		/**Копируем специализацию нашего предка*/
		public Specialization(AliveCell cell) {
			System.arraycopy(cell.getSpecialization().map, 0, map, 0, map.length);
			phenotype = cell.phenotype;
		}
		
		/**Копируем специализацию нашего предка*/
		public Specialization(JSON json, long v) {
	    	List<Integer> vals = json.getA("vals");
			
			var max = 0;
			if(v < 8){
				List<Integer> keys = json.getA("keys");
				for(int i = 0 ; i < keys.size() ; i++) {
					var key = TYPE.values[keys.get(i)];
					var val = vals.get(i);
					if (val >= max) {
						max = val;
						main = key;
					}
					put(key,vals.get(i));
				}
			} else {
				List<String> keys = json.getA("keys");
				for(int i = 0 ; i < keys.size() ; i++) {
					var key = TYPE.valueOf(keys.get(i));
					var val = vals.get(i);
					if (val >= max) {
						max = val;
						main = key;
					}
					put(key,vals.get(i));
				}
			}
			

			updateColor();
		}

		private void updateColor() {
			float x = 0f;
			float y = 0f;
			for(var i : TYPE.values){
				var ix = get(i) * Math.cos(2 * Math.PI * i.color);
				var iy = get(i) * Math.sin(2 * Math.PI * i.color);
				x += ix;
				y += iy;
			}
			float lenght = (float) Math.sqrt(x * x + y * y);
			float angle = (float) (Math.acos(x / lenght) / (2 * Math.PI));
			if(y < 0) angle = 1f - angle;
			phenotype = Utils.getHSBColor(angle, 1f, 0.5 + lenght / 200, 1f);
		}
		/**Сохраняет новое значение специализации
		 * @param type какая специализация
		 * @param co какое у неё будет новое значение
		 */
		public void set(TYPE type, int co) {
			if(get(type) == co) {
				return;
			} else {
				final var del = co - get(type);
				var summBefore = MAX_SPECIALIZATION - get(type);
				var max = 0;
				var sumAfter = MAX_SPECIALIZATION;
				for(var i : TYPE.values) {
					if(i == type) continue;
					var nVal = (int) Utils.betwin(0.0, Math.round(((double)(get(i) * (summBefore - del))) / summBefore), MAX_SPECIALIZATION );
					if(nVal >= max) {
						max = nVal;
						main = i;
					}
					sumAfter -= nVal;
					put(i, nVal );
				}
				if(sumAfter >= max)
					main = type;
				put(type, sumAfter);
			}
			updateColor();
		}

		public JSON toJSON() {
			JSON make = new JSON();
			String [] keys = new String[TYPE.size()];
			int [] vals = new int[TYPE.size()];
			var i = 0;
			for(var s : TYPE.values) {
				keys[i] = s.name();
				vals[i] = get(s);
				i++;
			}
			make.add("keys", keys);
			make.add("vals", vals);
			return make;
		}
		/**Установить новое значение специализации
		 * @param type какая специализация
		 * @param value какое значение у специализации теперь
		 */
		private void put(TYPE type, int value) {map[type.ordinal()] = value;	}
		/**Получить значение специализации по типу
		 * @param type тип специализации
		 * @return текущее значение специализации
		 */
		public int get(TYPE type) {return map[type.ordinal()]; }

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
    /**Плавучесть. Меняется от -100 до 100 Где -100 - тонуть каждый ход, 100 - всплывать каждый ход, 1 - тонуть каждые 100 ходов*/
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
    /**Слизистая оболочка клетки. Если клетка в слизи, то к неё не присосаться*/
    protected int mucosa = 0;
	/**Количество энергии для деления*/
	protected int hp_by_div = 999;
    /**Цвет бота зависит от того, что он делает*/
	public Color color_DO;
    
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
    /**Список всех наших друзей*/
	private final CellObject[] _friends = new CellObject[DIRECTION.size()];
	/**Число этих самых "друзей"*/
	private int countFriends = 0;
  
    
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
		this.poisonPower = Math.min(Poison.MAX_TOXIC * 100, poisonPower);
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
	/**Функция получения семьи. Тех клеток, с кем текущая в тестных отношениях
	 * Каждая ячейка это или AliveCell или ConnectveTissue
	 * @return список из клеток, коих ровно DIRECTION.size() * 2. Может быть пропущенно сколько-то ячеек...
	 */
	public CellObject[] getComrades() {
		return _friends;
	}
	
	/**Сохраняет нам специфический наполнитель для нашей многоклеточной семьи
	 * @param friend 
	 */
    public void setComrades(ConnectiveTissue friend) {
		if(_setComrades(friend))
			friend.setCell((AliveCell) this);
	}
	/**Сохраняет нам нового члена многоклеточной семьи
	 * @param friend 
	 */
    public void setComrades(AliveCell friend) {
		if(_setComrades(friend))
			friend.setComrades((AliveCell) this);
    }
    private boolean _setComrades(CellObject friend) {
		assert friend != null : "Забыли друга!!!";
		assert friend != this : "Вам не кажется это странным?";
		assert friend.getPos().distance(this.getPos()).getHypotenuse() < 2 : "Куда-то далековато друг забрался, не находите?\n" + Arrays.toString(_friends) + ".\nЭтот явно лишний: " + friend + " из за дистанции " + friend.getPos().distance(this.getPos());
        int emptyIndex = -1;
		for (int i = 0; i < _friends.length; i++) {
			final var _friend = _friends[i];
			if(_friend == friend){
				return false;
			} else if(_friend == null){
				emptyIndex = i;
			} else {
				if(_friend.getPos().equals(friend.getPos()))
				assert !_friend.getPos().equals(friend.getPos()) : "У нас точки совпали, а объекты - нет... Магия!. Объекты: " + _friend + " и " + friend;
			}
		}
		assert countFriends < Point.DIRECTION.size() : "Многовато у нас друзей, не находите? " + Arrays.toString(_friends) + ". Этот явно лишний: " + friend;
		_friends[emptyIndex] = friend;
		countFriends++;
		return true;
    }
	
	/**Удаляет этого из наших товарищей
	 * @param remove кого надо удалить
	 */
	public void removeComrades(ConnectiveTissue remove){
		if(_removeComrades(remove))
			remove.removeCell((AliveCell) this);
	}
	/**Удаляет этого из наших товарищей
	 * @param remove кого надо удалить
	 */
	public void removeComrades(AliveCell remove){
		if(_removeComrades(remove))
			remove.removeComrades((AliveCell) this);
	}
	private boolean _removeComrades(CellObject remove){
		for (int i = 0; i < _friends.length; i++) {
			final var _friend = _friends[i];
			if(_friend == remove){
				_friends[i] = null;
				assert countFriends > 0 : "Ожидается, что у нас всё ещё есть друзья";
				countFriends--;
				return true;
			}
		}
		return false;
		//assert false : "Недостижимая часть кода. Не нашли " + remove + " среди " + Arrays.toString(_friends);
	}
	/**Возвращает сколько у нас друзей
	 * @return число друзей
	 */
	public int getCountComrades(){return countFriends;}
	
	public int getDNA_wall() {
		return DNA_wall;
	}
	public void setDNA_wall(int DNA_wall) {
		this.DNA_wall=DNA_wall;
	}
	/**Возвращает основную специализацию клетки
	 * @return основная специализация
	 */
	public Specialization.TYPE getMainSpec(){
		return specialization.main;
	}
	/**Выдаёт фактическое значение специализации в %
	 * @param type что за специализация
	 * @return [0, 1.0] в зависимости от приспособления
	 */
	public double specMaxVal(Specialization.TYPE type) {
		double spec = specialization.get(type);
		if(type != getMainSpec())
			spec = Math.min(10, spec);
		return spec / Specialization.MAX_SPECIALIZATION;
	}
	/**
	 * Возвращает предельное значение параметра, в зависимости от специализации
	 * @param maxVal истиное максимальное значение
	 * @param type специализация
	 * @return [0,maxVal] в зависимости от специализации
	 */
	public double specMaxVal(double maxVal, Specialization.TYPE type) {
		if(maxVal == 0)	return 0d;
		else			return maxVal * specMaxVal(type);
	}
	/**
	 * Возвращает предельное значение параметра, в зависимости от специализации
	 * @param maxVal истиное максимальное значение
	 * @param type специализация
	 * @return [0,maxVal] в зависимости от специализации
	 */
	public int specMaxVal(int maxVal, Specialization.TYPE type) {
		return (int) specMaxVal((double) maxVal,type);
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
		this.mineralTank = mineralTank;
	}

	/**Добавляет или уменьшает размер танка для минералов
	 * @param add сколько в танк кладём (или достаём, если отрицательное значение
	 */
	public void addMineralTank(int add) {
		mineralTank += add;
	}
	/**
	 * Возвращает количество солнца вокруг
	 * @return количество солнца, которое может получить клетка. [0, Configurations.BASE_SUN_POWER + Configurations.ADD_SUN_POWER + 5]
	 */
	public double sunAround() {
		if(getMineral() > 0){
			//+100% света при наличии миниралов
			final var t = 100 + 100 * getMineral() / AliveCell.MAX_MP;	
			//Ну и энергию от солнца не забываем
			return specMaxVal(Configurations.getSunPower(getPos()) * t / 100, Specialization.TYPE.PHOTOSYNTHESIS);
		} else {
			return specMaxVal(Configurations.getSunPower(getPos()), Specialization.TYPE.PHOTOSYNTHESIS);
		}
	}
	/**
	 * Возвращает количество минералов вокруг
	 * @return количество солнца, которое может получить клетка. [0, Configurations.BASE_SUN_POWER + Configurations.ADD_SUN_POWER + 5]
	 */
	public double mineralAround() {
		return specMaxVal(Configurations.getConcentrationMinerals(getPos()), Specialization.TYPE.MINERALIZATION);
	}

	/**
	 * @return the mucosa
	 */
	public int getMucosa() {
		return mucosa;
	}

	/**
	 * @param mucosa the mucosa to set
	 */
	public void setMucosa(int mucosa) {
		this.mucosa = mucosa;
	}

	/**
	 * @param dna the dna to set
	 */
	public void setDna(DNA dna) {
		this.dna = dna;
	}
}
