package Calculations;

import java.awt.Dimension;
import java.awt.Image;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import Utils.JSON;
import Utils.SaveAndLoad;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import GUI.EvolTreeDialog;
import GUI.Settings;
import GUI.Viewers;
import MapObjects.CellObject;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JOptionPane;

/**
 * Так как некоторые переменные мира используются повсеместно
 * то они вынесены сюда!
 * @author Илья
 *
 */
public class Configurations extends SaveAndLoad.JSONSerialization<Configurations>{
	/**Переводчик для всех названий. В теории*/
	private static ResourceBundle bundle = ResourceBundle.getBundle("locales/locale", Locale.getDefault());
	/**Размер карты высчитывается на основе размера экрана. А эта переменная определяет, сколько пикселей будет каждая клетка*/
	public static final double PIXEL_PER_CELL = 10;
	
	/**Версия приложения. Нужна на тот случай, если вдруг будет загружаться старое приложение*/
	public static final long VERSION = 8;

	//Разные глобальные объекты, отвечающие за мир
	/**Текущая конфигруация мира*/
	public static Configurations confoguration = null;
	/**Гравитация в созданном мире. */
	public static Gravitation[] gravitation = new Gravitation[CellObject.LV_STATUS.length];
	/**Глобальный мир!*/
	public static World world = null;
	/**Звёзды нашего мира*/
	public static List<SunAbstract> suns = null;
	/**Минералы нашего мира*/
	public static List<MineralAbstract> minerals = null;
	/**Потоки воды, которые заставлют клетки двигаться*/
	public static List<StreamAbstract> streams = null;
	/**Эволюционное дерево мира*/
	public static EvolutionTree tree = null;
	
	/**Количиство ячеек карты*/
	public final Dimension MAP_CELLS;
	/**Тип созданного мира, в котором живут живики*/
	public final WORLD_TYPE world_type;
	/**Период сохранения, шаги эволюции*/
	public long SAVE_PERIOD;
	/**Сколько файлов автосохранения держать*/
	public int COUNT_SAVE;
	/**Дата последнего сохранения*/
	public long lastSaveCount;
	/**Степень мутагенности воды [0,100]*/
	public int AGGRESSIVE_ENVIRONMENT;
	/**Как часто органика теряет своё ХП. Если 1 - на каждый ход. Если 2 - каждые 2 хода и т.д.*/
	public int TIK_TO_EXIT;
	/**Степень загрязнённости воды. На сколько падает уровень освещения за каждую клетку от источника света*/
	public double DIRTY_WATER;
	
	//Графическая часть
	/**Указатель на глобальный объект отображения. Тут прячутся все наборы панелей, которые в настоящий момент показываются на экране*/
	private static Viewers _viewers = null;
	private static Logger logger = Logger.getLogger(Configurations.class.getName());
	
	//Общие классы для программы
	/**ГСЧ для симуляции*/
	public static SplittableRandom rnd = new SplittableRandom();
	/**Основной потоковый пулл для всяких задач которым нужно выполняться периодически*/
	private static final ScheduledThreadPoolExecutor TIME_OUT_POOL = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1,new ThreadFactory(){
		@Override
		public Thread newThread(Runnable task) {return new Thread(task, "TIME_OUT_TASK");}
	});
	
	//Общее форматирование
	/**Фон по умолчанию для всех компонентов*/
	public static java.awt.Font defaultFont = null; 
	/**Уменьешнный размер шрифта для необходимых элементов*/
	public static java.awt.Font smalFont = null; 
	
	/**Задача, выплоняемая примерно раз в секунду, но без жёсткого ограничения*/
	public interface EvrySecondTask{
		/**Функция, вызываемая каждую секунду. Примерно*/
		public void taskStep();
	}
	/**Возможные типы мира*/
	public enum WORLD_TYPE{
		/**Линейный мир представляет собой бесконечную полосу, ограниченную сверху и снизу. Справа и слева мир зациклен на себя.
		 Это вертекальный срез бассейна*/
		LINE_H,
		/**Линейный мир, но на этот раз бесконечная полоса вертикальна. Это тип река, вид сверху, кусок течения*/
		LINE_V,
		/**Прямоугольный мир, ограниченный со всех сторох. Это аквариум*/
		RECTANGLE,
		/**Бесконечное прямоугольное поле - просто кусок океана*/
		FIELD_R,
		/**Круглый мир, представляющий собой чашку петри*/
		CIRCLE,
		/**И, наконец, круглое поле, но без стенок - просто кусок океана - быть не может.
		 Тут вообще хорошо-бы расписать доказательство, но сводится оно к простой истине - 
		 длина стенки мира всегда меньше, чем длина стенки на 1 клетку дальше. Поэтому отображения
		 1 к 1 быть не может
		 Ещё можно обосновать через сумму углов, которая становится больше 360... В общем в декартовой системе
		 Это не реально
		 */
		//FIELD_C,
		;
		/**Все значения мира*/
		public static final WORLD_TYPE[] values = WORLD_TYPE.values();
		/**Количество значений*/
		public static final int length = WORLD_TYPE.values.length;
		
		public String toString(){
			return Configurations.getProperty(WORLD_TYPE.class,name());
		}
		
	}
	
	/**Создаёт конфигурацию мира на основе заданных параметров
	 * @param type тип создаваемого мира
	 * @param width ширина мира, в кубиках.
	 * @param height высота мира, тоже в кубиках
	 */
	public Configurations(WORLD_TYPE type, int width, int height) {
		super(null, 0);
		//Создаём поле
		MAP_CELLS = new Dimension(width,height);
		world_type = type;
		//Мутагенность воды
		AGGRESSIVE_ENVIRONMENT = 20;
		//Скорость разложения органики. За сколько шагов уходит 1 единица энергии
		TIK_TO_EXIT = 1000;
		 //Чтобы освещалось только 33 % мира при силе света в 30 единиц
		 switch (type) {
			case LINE_H,LINE_V -> DIRTY_WATER =  30d / (height * 0.33);
			case RECTANGLE -> DIRTY_WATER =  30d / (Math.min(height, width) * 0.5); //Чтобы освещалась половина мира
			default -> throw new AssertionError();
		}
		
		
		SAVE_PERIOD = 100_000;
		COUNT_SAVE = 3;
		lastSaveCount = 0;
	}
	/**Создаёт конфигурацию мира на основе предыдущей конфигурации
	 * @param sourse предыдущая конфигруация
	 * @param type тип создаваемого мира
	 * @param width ширина мира, в кубиках.
	 * @param height высота мира, тоже в кубиках
	 */
	public Configurations(Configurations sourse, WORLD_TYPE type, int width, int height) {
		super(null, 0);
		MAP_CELLS = new Dimension(width,height);
		world_type = type;
		AGGRESSIVE_ENVIRONMENT = sourse.AGGRESSIVE_ENVIRONMENT;
		TIK_TO_EXIT = sourse.TIK_TO_EXIT;
		DIRTY_WATER = sourse.DIRTY_WATER;
		SAVE_PERIOD = sourse.SAVE_PERIOD;
		COUNT_SAVE = sourse.COUNT_SAVE;
		lastSaveCount = sourse.lastSaveCount;
	}
	public Configurations(JSON configWorld, long version) throws GenerateClassException{
		this(version < 7 ? WORLD_TYPE.LINE_H : WORLD_TYPE.valueOf(configWorld.get("WORLD_TYPE")),(int)configWorld.getA("MAP_CELLS").get(0),(int)configWorld.getA("MAP_CELLS").get(1));
		if(version < 7){
			buildMap(this,new HashMap<CellObject.LV_STATUS, Gravitation>(){{put(CellObject.LV_STATUS.LV_ORGANIC, new Gravitation(2, Gravitation.Direction.DOWN));}});
			AGGRESSIVE_ENVIRONMENT = configWorld.get("AGGRESSIVE_ENVIRONMENT");
			TIK_TO_EXIT = configWorld.get("TIK_TO_EXIT");
		
			final int DIRTY_WATER_old = configWorld.get("DIRTY_WATER");			
			final int SUN_SPEED = configWorld.get("SUN_SPEED");
			final int SUN_LENGHT = configWorld.get("SUN_LENGHT");
			final int SUN_POSITION = configWorld.get("SUN_POSITION");
			final int BASE_SUN_POWER = configWorld.get("BASE_SUN_POWER");
			final int ADD_SUN_POWER = configWorld.get("ADD_SUN_POWER");
			//final int SUN_FORM = configWorld.get("SUN_FORM");
			final var width = MAP_CELLS.width;
			final var height = MAP_CELLS.height;
			
			DIRTY_WATER = ((double)BASE_SUN_POWER * 100) / (height * DIRTY_WATER_old);
			suns.add(new SunRectangle(BASE_SUN_POWER, new Trajectory(Point.create(width/2, 0)), width, 1, false,"Постоянное"));
			suns.add(new SunEllipse(
						ADD_SUN_POWER, 
						new TrajectoryPolyLine(Math.abs(SUN_SPEED),false, Point.create(SUN_POSITION, 0), Point.create(SUN_POSITION + (SUN_SPEED > 0 ? width/3 : -width/3), 0), Point.create(SUN_POSITION + (SUN_SPEED > 0 ? width*2/3 : -width*2/3), 0)), 
						SUN_LENGHT * MAP_CELLS.width / 100, (int) (2 * BASE_SUN_POWER/DIRTY_WATER), 
						false,"Движущееся"));
			
			
			final double LEVEL_MINERAL = configWorld.get("LEVEL_MINERAL");
			final int CONCENTRATION_MINERAL = configWorld.get("CONCENTRATION_MINERAL");
			
			minerals.add(new MineralRectangle(CONCENTRATION_MINERAL,CONCENTRATION_MINERAL / ((1d-LEVEL_MINERAL)* MAP_CELLS.height), new Trajectory(Point.create(MAP_CELLS.width/2, MAP_CELLS.height-1)),MAP_CELLS.width, 1, false,"Постоянная"));
			//А теперь два потока воды - вверх и вниз
			streams.add(new StreamVertical(new Trajectory(Point.create(width*9/40, height/2)), width/5, height, new StreamAttenuation.LinealStreamAttenuation(-100,-200),"Левый"));
			streams.add(new StreamVertical(new Trajectory(Point.create(width*3/4, height/2)), width/10, height, new StreamAttenuation.LinealStreamAttenuation(2,4),"Прваый"));
			//А теперь ещё 4 шапочки, чтобы в верхней и нжней части сдвутать клетки
			streams.add(new StreamHorizontal(new Trajectory(Point.create(width*7/40, 0)), width/10, height, new StreamAttenuation.LinealStreamAttenuation(100,200),"Левый, верхний левый"));
			streams.add(new StreamHorizontal(new Trajectory(Point.create(width*11/40, 0)), width/10, height, new StreamAttenuation.LinealStreamAttenuation(-100,-200),"Левый, верхний правый"));
			streams.add(new StreamHorizontal(new Trajectory(Point.create(width*7/40, height)), width/10, height, new StreamAttenuation.LinealStreamAttenuation(-100,-200),"Левый, нижний левый"));
			streams.add(new StreamHorizontal(new Trajectory(Point.create(width*11/40, height)), width/10, height, new StreamAttenuation.LinealStreamAttenuation(100,200),"Левый, нижний правый"));
			
			streams.add(new StreamHorizontal(new Trajectory(Point.create(width*29/40, 0)), width/20, height, new StreamAttenuation.LinealStreamAttenuation(-2,-4), "Правый, верхний левый"));
			streams.add(new StreamHorizontal(new Trajectory(Point.create(width*31/40, 0)), width/20, height,new StreamAttenuation.LinealStreamAttenuation(2,4),"Правый, верхний правый"));
			streams.add(new StreamHorizontal(new Trajectory(Point.create(width*29/40, height)), width/20, height,new StreamAttenuation.LinealStreamAttenuation(2,4), "Правый, нижний левый"));
			streams.add(new StreamHorizontal(new Trajectory(Point.create(width*31/40, height)), width/20, height, new StreamAttenuation.LinealStreamAttenuation(-2,-4),"Правый, нижний правый"));
		} else {
			final var mapG = new HashMap<CellObject.LV_STATUS, Gravitation>();
			final var gj = configWorld.getJ("GRAVITATION");
			for(var type : gj.getKeys()){
				mapG.put(CellObject.LV_STATUS.valueOf(type), new Gravitation(gj.get(type), version));
			}
			buildMap(this, mapG);
			AGGRESSIVE_ENVIRONMENT = configWorld.get("AGGRESSIVE_ENVIRONMENT");
			TIK_TO_EXIT = configWorld.get("TIK_TO_EXIT");
			DIRTY_WATER = configWorld.get("DIRTY_WATER");
			for(final var j : configWorld.getAJ("SUNS")){
				try {
					suns.add(SunAbstract.generate(j, version));
				} catch (GenerateClassException ex) {
					logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					ex.addMsg(getProperty(this.getClass(),"loadSerror",j.toBeautifulJSONString()));
					throw ex;
				}					
			}
			for(final var j : configWorld.getAJ("MINERALS")){
				try {
					minerals.add(MineralAbstract.generate(j, version));
				} catch (GenerateClassException ex) {
					logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					ex.addMsg(getProperty(this.getClass(),"loadMerror",j.toBeautifulJSONString()));
					throw ex;
				}
			}
			for(final var j : configWorld.getAJ("STREAMS")){
				try {
					streams.add(StreamAbstract.generation(j, version));
				} catch (NullPointerException ex) {
					logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					throw ex;
				}
			}
		}
	}
	
	@Override
	public String getName() {
		return "CONFIG_WORLD";
	}
	@Override
	public JSON getJSON() {
		JSON configWorld = new JSON();
		configWorld.add("MAP_CELLS", new int[] {MAP_CELLS.width,MAP_CELLS.height});
		configWorld.add("AGGRESSIVE_ENVIRONMENT", AGGRESSIVE_ENVIRONMENT);
		configWorld.add("TIK_TO_EXIT", TIK_TO_EXIT);
		configWorld.add("DIRTY_WATER", DIRTY_WATER);
		configWorld.add("WORLD_TYPE", world_type);
		configWorld.add("SUNS", suns.stream().map(s -> s.toJSON()).toList());
		configWorld.add("MINERALS", minerals.stream().map(s -> s.toJSON()).toList());
		configWorld.add("STREAMS", streams.stream().map(s -> s.toJSON()).toList());
		final var gj = new JSON();
		for (int i = 0; i < gravitation.length; i++) {
			gj.add(CellObject.LV_STATUS.values[i].name(), gravitation[i].toJSON());
		}
		configWorld.add("GRAVITATION", gj);
		return configWorld;
	}
	/**
	 * Создаёт базовый мир заданных размеров
	 * @param type тип создаваемого мира
	 * @param width ширина мира, в кубиках.
	 * @param height высота мира, тоже в кубиках
	 */
	public static void makeDefaultWord(WORLD_TYPE type, int width, int height ) {
		switch (type) {
			case LINE_H -> {
				buildMap(new Configurations(type, width, height), new EnumMap<CellObject.LV_STATUS, Gravitation>(CellObject.LV_STATUS.class){{put(CellObject.LV_STATUS.LV_ORGANIC, new Gravitation(20, Gravitation.Direction.DOWN));}});
				suns.add(new SunRectangle(30, new Trajectory(Point.create(width/2, 0)), (int) (width* 0.77), 1, false,"Постоянное"));
				suns.add(new SunEllipse(
						30, 
						new TrajectoryPolyLine(50,false, Point.create(width/2, 0), Point.create(width-1, 0), Point.create(0, 0), Point.create(width/2-1, 0)), 
						width/8,height/2, 
						false,"Движущееся"));
				//Эти минералы будут занимать только 33% мира
				minerals.add(new MineralRectangle(20,confoguration.DIRTY_WATER, new Trajectory(Point.create(0, height-1)),width/2, 1, false,"Постоянная"));
				//А эти будут иногда подниматься достаточно высоко
				minerals.add(new MineralEllipse(20,confoguration.DIRTY_WATER, 
						new TrajectoryEllipse(2000, Point.create(width / 2, height * 7 / 8), -Math.PI, 1, height * 3 / 8)
						,width * 1 / 4, height * 1 / 8, true,"Движущееся"));
				//А теперь два потока воды - вверх и вниз
				streams.add(new StreamVertical(new Trajectory(Point.create(width/4, height/2)), width/4, height, new StreamAttenuation.LinealStreamAttenuation(-100,-1000),"Левый"));
				streams.add(new StreamVertical(new Trajectory(Point.create(width*3/4, height/2)), width/4, height,new StreamAttenuation.PowerFunctionStreamAttenuation(1,1000,2),"Прваый"));
				//А теперь ещё 4 шапочки, чтобы в верхней и нжней части сдвутать клетки
				streams.add(new StreamEllipse(new Trajectory(Point.create(width/4, 0)), width/4, new StreamAttenuation.LinealStreamAttenuation(-40,-100),"Левый верхний"));
				streams.add(new StreamEllipse(new Trajectory(Point.create(width/4, height-1)), width/4,  new StreamAttenuation.LinealStreamAttenuation(40,100),"Левый нижний"));
				
				streams.add(new StreamEllipse(new Trajectory(Point.create(width*3/4, 0)), width/4, new StreamAttenuation.LinealStreamAttenuation(10,100),"Правый верхний"));
				streams.add(new StreamEllipse(new Trajectory(Point.create(width*3/4, height-1)), width/4, new StreamAttenuation.LinealStreamAttenuation(-4,-100),"Правый нижний"));
			}
			case LINE_V->{
				buildMap(new Configurations(type, width, height), null);
				//Будет одно солнышко, которое будет двигаться сверху вниз линией
				suns.add(new SunRectangle(
						30, 
						new TrajectoryPolyLine(401, false, Point.create(width/2, (int) (height/2)), Point.create(width/2, (int) (height-1)), Point.create(width/2, (int) (1))), 
						width,1, 
						false,"Движущееся"));
				//Четыре куска минералов движущихся наискось
				minerals.add(new MineralEllipse(30,confoguration.DIRTY_WATER * 2, new TrajectoryPolyLine(199,false, Point.create(width/2, height/2), Point.create(0, 0), Point.create(0, height-1), Point.create(width/2, height/2), Point.create(width-1, 0), Point.create(width-1, height-1)),
						width/10, height/10, 
						true,"Путешествующий эллипс 1"));
				minerals.add(new MineralEllipse(30,confoguration.DIRTY_WATER * 2, new TrajectoryPolyLine(227,false, Point.create(width/2, height/2), Point.create(width-1, 0), Point.create(width-1, height-1), Point.create(width/2, height/2), Point.create(0, 0), Point.create(0, height-1)),
						width/10, height/10, 
						true,"Путешествующий эллипс 2"));
				minerals.add(new MineralEllipse(30,confoguration.DIRTY_WATER * 2, new TrajectoryPolyLine(193,false, Point.create(width/2, height/2), Point.create(width-1, height-1), Point.create(width-1, 0), Point.create(width/2, height/2), Point.create(0, height-1), Point.create(0, 0)),
						width/10, height/10, 
						true,"Путешествующий эллипс 3"));
				minerals.add(new MineralEllipse(30,confoguration.DIRTY_WATER * 2, new TrajectoryPolyLine(233,false, Point.create(width/2, height/2), Point.create(0, height-1), Point.create(0, 0), Point.create(width/2, height/2), Point.create(width-1, height-1), Point.create(width-1, 0)),
						width/10, height/10, 
						true,"Путешествующий эллипс 4"));
				//Ну и течении в реке
				streams.add(new StreamVertical(new Trajectory(Point.create(width/2, height/2)), width, height,new StreamAttenuation.PowerFunctionStreamAttenuation(-3, -4001,4),"Течение"));
			}
			case RECTANGLE->{
				buildMap(new Configurations(type, width, height), null);
				//Будет четыре солнца, которые будут то "закатываться" то "выкатываться"
				final var offset = Math.min(width, height) / 3;
				suns.add(new SunRectangle(
						30, 
						new TrajectoryPolyLine(1000, false, Point.create(width/2, 0), Point.create(width/2, -offset)), 
						width,1, 
						true,"Верхнее"));
				suns.add(new SunRectangle(
						30, 
						new TrajectoryPolyLine(1000, false, Point.create(width/2, height), Point.create(width/2, height+offset)), 
						width,1, 
						true,"Нижнее"));
				suns.add(new SunRectangle(
						30, 
						new TrajectoryPolyLine(1000, false, Point.create(0, height/2), Point.create(-offset, height/2)), 
						1,height, 
						true,"Левое"));
				suns.add(new SunRectangle(
						30, 
						new TrajectoryPolyLine(1000, false, Point.create(width, height/2), Point.create(width+offset, height/2)), 
						1,height, 
						true,"Правое"));
				
				
				//Четыре нычки минералов двигающихся в противофазе с солнцем
				minerals.add(new MineralRectangle(
						30,confoguration.DIRTY_WATER,
						new TrajectoryPolyLine(1000, false, Point.create(width/2, -offset), Point.create(width/2, 0)), 
						width,1, 
						true,"Верхняя"));
				minerals.add(new MineralRectangle(
						30,confoguration.DIRTY_WATER,
						new TrajectoryPolyLine(1000, false, Point.create(width/2, height+offset), Point.create(width/2, height)), 
						width,1, 
						true,"Нижняя"));
				minerals.add(new MineralRectangle(
						30,confoguration.DIRTY_WATER,
						new TrajectoryPolyLine(1000, false, Point.create(-offset, height/2), Point.create(0, height/2)), 
						1,height, 
						true,"Левая"));
				minerals.add(new MineralRectangle(
						30,confoguration.DIRTY_WATER,
						new TrajectoryPolyLine(1000, false, Point.create(width+offset, height/2), Point.create(width, height/2)), 
						1,height, 
						true,"Правая"));
				
				/*minerals.add(new MineralRectangle(30,confoguration.DIRTY_WATER * 4, 
						new TrajectoryPolyLine(1000,false,
								Point.create(0, 0), Point.create(width, 0), Point.create(width, height), Point.create(0, height), Point.create(0, 0),
								Point.create(width*1/3, height*1/3), Point.create(width*2/3, height*1/3), Point.create(width*2/3, height*2/3), Point.create(width*1/3, height*2/3), Point.create(width*1/3, height*1/3)
						),
						width/10, height/10, 
						true,"Путешествующий источник минералов"));
				*/
			}
			default -> throw new AssertionError();
		}
		//И конечно создаём адама.
		world.makeAdam();
	}
	/** * Создаёт поле мира.Только поле. Пустая карта, да дерево эволюции.
	 * @param confoguration конфигурация мира на основе которой мир и создаётся
	 * @param gravitations гравитация в созданном мире для каждого типа объектов. 
	 *				Если не указывать тип, гравитация на него действовать не будет
	 */
	public static void buildMap(Configurations confoguration, Map<CellObject.LV_STATUS, Gravitation> gravitations){
		rebuildMap(confoguration);
		//Солнца
		suns = new ArrayList<>();
		//Минералы
		minerals = new ArrayList<>();
		//Потоки
		streams = new ArrayList<>();
		//Создаём магическое притяжение
		gravitation = new Gravitation[CellObject.LV_STATUS.length];
		for(var i : CellObject.LV_STATUS.values){
			gravitation[i.ordinal()] = (gravitations != null && gravitations.containsKey(i)) ? gravitations.get(i) : Gravitation.NONE;
		}
		//А теперь дерево эволюции
		tree = new EvolutionTree();
	}
	/**Перестраивает карту мира на основе заданной конфигурации
	 * при этом остальные параметры мира не трогает
	 * @param confog новая конфигурация мира
	 */
	public static void rebuildMap(Configurations confog){
		confoguration = confog;
		//Мир
		if(world != null)
			world.destroy();
		world = new World(confoguration.MAP_CELLS);
	}
	/** * Возвращает размер мира по умолчанию для текущего разрешения экрана
	 * Понятное дело, что если экрана нет - то вернёт он лишь null.Впрочем, без экрана вызывать эту функцию в принципе не следует!
	 * @param type тип мира. Да, размеры зависят от типа мира!
	 * @return высоту и ширину мира
	 */
	public static Configurations getDefaultConfiguration(WORLD_TYPE type){
		Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
		final var ret =  switch (type) {
			case LINE_H -> new Configurations(type,(int) (sSize.getWidth() / PIXEL_PER_CELL), (int) ((sSize.getHeight() * 0.9) / PIXEL_PER_CELL));
			case LINE_V -> new Configurations(type,(int) (sSize.getWidth() / PIXEL_PER_CELL), (int) ((sSize.getHeight()) / PIXEL_PER_CELL));
			case RECTANGLE -> new Configurations(type,(int) (sSize.getWidth() / PIXEL_PER_CELL), (int) ((sSize.getHeight() * 0.9) / PIXEL_PER_CELL));
			default ->throw new AssertionError();
		};
		return ret;
	}
	
	/**Возвращает количество солнечной энергии в данной точке пространства
	 * @param pos где интересует энергия
	 * @return сколько в единицах HP энергии тут
	 */
	public static double getSunPower(Point pos){
		return suns.stream().reduce(0d, (a,b) -> a + b.getEnergy(pos), Double::sum);
	}
	/**Возвращает максимально возможное количество солнечной энергии в мире
	 * @return сколько в единицах HP энергии всего в мире
	 */
	public static double getMaxSunPower(){
		return suns.stream().reduce(0d, (a,b) -> a + b.power, Double::sum);
	}
	/**Возвращает концентрацию минералов вокруг клетки
	 * @param pos где смотрим параметр
	 * @return сколько в единицах MP энергии тут
	 */
	public static double getConcentrationMinerals(Point pos){
		return minerals.stream().reduce(0d, (a,b) -> a + b.getConcentration(pos), Double::sum);
	}
	/**Возвращает максимально возможное количество минералов в мире
	 * @return сколько в единицах MP энергии всего в мире
	 */
	public static double getMaxConcentrationMinerals(){
		return minerals.stream().reduce(0d, (a,b) -> a + b.power, Double::sum);
	}
	/**Сохраняет текущий вид графического отображения
	 * @param defaultViewer набор панелей, которые теперь будут на экране
	 */
	public static void setViewer(Viewers defaultViewer) {
		_viewers = defaultViewer;
	}
	/**Возвращает текущий комплект отображения
	 * @return defaultViewer набор панелей, которые теперь будут на экране
	 */
	public static Viewers getViewer() {
		return _viewers;
	}
	/**Возвращает ширину мира
	 * @return Ширина мира в клетках
	 */
	public static int getWidth(){return confoguration.MAP_CELLS.width;}
	/**Возвращает высоту мира
	 * @return Высота мира в клетках
	 */
	public static int getHeight(){return confoguration.MAP_CELLS.height;}

	/**Функция сохранения - сохранит мир в определённый файл
	 * @param filePatch - путь, куда сохранить мир
	 * @throws java.io.IOException так как мы работаем с файловой системой, мы всегда имеем шансы уйти с ошибкой
	 */
	public static void save(String filePatch) throws IOException {
		boolean oldStateWorld = world.isActiv();		
		world.awaitStop();

		var js = SaveAndLoad.save(filePatch, VERSION);
		js.addActionListener( e-> logger.log(Level.INFO, String.format("Сохранение %d из %d. Осталось %.2fc",e.now,e.all,e.getTime()/1000)));
		js.save(confoguration, tree, world);
		confoguration.lastSaveCount = world.step;
		if (oldStateWorld)
			world.start();
		else
			world.stop();
	}
	/**Функция загрузки
	 * @param filePatch - путь, куда сохранить мир
	 * @throws java.io.IOException так как мы работаем с файловой системой, мы всегда имеем шансы уйти с ошибкой
	 * @throws Calculations.GenerateClassException может вылететь, когда у нас ошибка разбора открытого файла
	 */
	public static void load(String filePatch) throws IOException, GenerateClassException {
		boolean oldStateWorld = world.isActiv();			
		world.awaitStop();

		var js = SaveAndLoad.load(filePatch);     
		js.addActionListener( e-> logger.log(Level.INFO, String.format("Загрузка %d из %d. Осталось %.2fc",e.now,e.all,e.getTime()/1000)));
		confoguration = js.load(confoguration);
		tree = js.load(tree);
		world.destroy();
		world = js.load((j,v) -> new World(j, v, confoguration.MAP_CELLS), world.getName());	
		confoguration.lastSaveCount = world.step;	
		
		if (oldStateWorld)
			world.start();
		else
			world.stop();
	}
	/**
	 * Возвращает форматированную строку описания для определённого класса
	 * @param cls - класс, в котором эта строка находится
	 * @param name - ключ
	 * @param arguments аргументы, которые будут вставленны в строку свойств
	 * @return Строка в формате HTML
	 */
	public static String getHProperty(Class<?> cls, String name, Object ... arguments) {
		return MessageFormat.format(getHProperty(cls,name),arguments);
	}
	/**
	 * Возвращает форматированную строку описания для определённого класса
	 * @param cls - класс, в котором эта строка находится
	 * @param name - ключ
	 * @param arguments аргументы, которые будут вставленны в строку свойств
	 * @return Строка текста
	 */
	public static String getProperty(Class<?> cls, String name, Object ... arguments) {
		return MessageFormat.format(getProperty(cls,name),arguments);
	}
	/**
	 * Возвращает строку описания для определённого класса
	 * @param cls - класс, в котором эта строка находится
	 * @param name - ключ
	 * @return Строка в формате HTML
	 */
	public static String getHProperty(Class<?> cls, String name) {
		return "<HTML>"+getProperty(cls,name).replace("\n", " <br> ");
	}
	/**
	 * Возвращает строку описания
	 * @param name - ключ
	 * @return Строка в формате HTML
	 */
	public static String getHProperty(String name) {
		return "<HTML>"+getProperty(name).replace("\n", " <br> ");
	}
	/**
	 * Возвращает строку описания для определённого класса
	 * @param cls - класс, в котором эта строка находится
	 * @param name - ключ
	 * @return Строка
	 */
	public static String getProperty(Class<?> cls, String name) {
		return getProperty(MessageFormat.format("{0}.{1}", cls.getTypeName(),name));
	}
	
	/**
	 * Возвращает строку описания
	 * @param name - ключ
	 * @return Строка для текущей локали
	 */
	public static String getProperty(String name) {
		try {
			return bundle.getString(name);
		} catch (MissingResourceException e) {
			var err = "Не найдено свойство " + name;
			logger.log(Level.WARNING, err, e);
			System.err.println(err);
			return err;
		}
	}
	
	/**
	 * Создаёт кнопку с иконкой. Кнопка 15х15
	 * @param name имя иконки
	 * @return кнопку, у которой две иконки и выключены основные флаги
	 */
	public static JButton makeIconButton(String name) {
		var button = new JButton();
		setIcon(button, name);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusable(false);
		return button;
	}

	/**
	 * Сохраняет иконки для кнопки.
	 * @param button кнопка, которой нужны иконки
	 * @param name название кнопок
	 */
	public static void setIcon(AbstractButton button, String name) {
		var name_const = MessageFormat.format("resources/{0}.png", name);
		var constResource = Configurations.class.getClassLoader().getResource(name_const);
		if(constResource == null) {
			System.err.println("Не смогли загрузить фотографию " + name_const);
		}
		var icon_const = constResource == null ? null : new ImageIcon(constResource);
		var name_select = MessageFormat.format("resources/{0}_active.png", name);
		var selectResource = Configurations.class.getClassLoader().getResource(name_select);
		if(selectResource == null) {
			System.err.println("Не смогли загрузить фотографию " + name_select);
		}
		var icon_select = selectResource == null ? null : new ImageIcon(selectResource);

		if (icon_const == null) {
			button.setText(name);
		} else {
			button.setText("");
			button.setIcon(new ImageIcon(icon_const.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
		}
		final var select_image = icon_select != null ? new ImageIcon(icon_select.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)) : new ImageIcon(icon_const.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH));
		button.setRolloverIcon(select_image);
		button.setSelectedIcon(select_image);

		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusable(false);
		button.setPreferredSize(new Dimension(15,15));
		button.setMinimumSize(new Dimension(15,15));
	}
	
	/**Тестирует шрифт на возможность вывода на экран всех ключей.
	 * @param font тестируемый шрифт
	 * @return список символов, которые невозможно вывыести на экран
	 */
	public static Set<String> testFont(java.awt.Font font){
		var ret = new HashSet<String>();
		for (var iterator = bundle.getKeys(); iterator.hasMoreElements();) {
			String key = iterator.nextElement();
			for(var ch : bundle.getString(key).split("")){
				var n = font.canDisplayUpTo(ch);
				if(n < 0){
					ret.add(ch);
				}
			}
		}
		return ret;
	}
	/**Добавить задачу на выполнение
	 * @param task задача, которая будет выполняться каждую секунду
	 */
	public static void addTask(EvrySecondTask task){
		addTask(task,1000);
	}
	/**Добавить задачу на выполнение
	 * @param task задача, которая будет выполняться с определённым интервалом
	 * @param ms время в мс, как часто задача будет выполняться
	 */
	public static void addTask(EvrySecondTask task, int ms){
		TIME_OUT_POOL.scheduleWithFixedDelay(() -> {
				try {task.taskStep();
				} catch (Exception ex) {logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);}
			}, ms, ms, TimeUnit.MILLISECONDS);
	}
}
