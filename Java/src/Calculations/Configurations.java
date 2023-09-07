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
import GUI.Viewers;
import MapObjects.CellObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
	
	/**Версия приложения. Нужна на тот случай, если вдруг будет загружаться старое приложение*/
	public static final long VERSION = 7;
	/**Количиство ячеек карты*/
	public static Dimension MAP_CELLS = null;
	/**Тип созданного мира, в котором живут живики*/
	public static WORLD_TYPE world_type;
	/**Гравитация в созданном мире. */
	public final static Gravitation[] gravitation = new Gravitation[CellObject.LV_STATUS.length];
	
	/**Период сохранения, шаги эволюции*/
	public static final long SAVE_PERIOD = 100_000;
	/**Сколько файлов автосохранения держать*/
	public static final int COUNT_SAVE = 3;
	
	/**Степень мутагенности воды [0,100]*/
	public static int AGGRESSIVE_ENVIRONMENT = 0;
	/**Как часто органика теряет своё ХП. Если 1 - на каждый ход. Если 2 - каждые 2 хода и т.д.*/
	public static int TIK_TO_EXIT;
	/**Степень загрязнённости воды. На сколько падает уровень освещения за каждую клетку от источника света*/
	public static double DIRTY_WATER;
	
	//Те-же переменные, только их значения по умолчанию.
	//Значения по умолчанию рассчитываются исходя из размеров мира
	//И не могут меняться пока мир неизменен
	public static int DAGGRESSIVE_ENVIRONMENT;
	public static int DTIK_TO_EXIT;
	public static double DDIRTY_WATER;

	//Разные глобальные объекты, отвечающие за мир
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
	
	//Графическая часть
	/**Указатель на глобальный объект отображения. Тут прячутся все наборы панелей, которые в настоящий момент показываются на экране*/
	private static Viewers _viewers = null;
	/**Отдельное окно с отображением дерева эволюции*/
	public static EvolTreeDialog evolTreeDialog = null;	
	
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
		/**И, наконец, круглое поле, но без стенок - просто кусок океана ,быть не может.
		 Тут вообще хорошо-бы расписать доказательство, но сводится оно к простой истине - 
		 длина стенки мира всегда меньше, чем длина стенки на 1 клетку дальше. Поэтому отображения
		 1 к 1 быть не может
		 Ещё можно обосновать через сумму углов, которая становится больше 360... В общем в декартовой системе
		 Это не реально
		 */
		//FIELD_C,
	}
	private Configurations(){super(null,0);}
	public Configurations(JSON configWorld, long version) {
		super(configWorld,version);
		List<Integer> map = configWorld.getA("MAP_CELLS");
		if(version < 7){
			makeDefaultWord(WORLD_TYPE.LINE_H, map.get(0),map.get(1));
			AGGRESSIVE_ENVIRONMENT = configWorld.get("AGGRESSIVE_ENVIRONMENT");
			TIK_TO_EXIT = configWorld.get("TIK_TO_EXIT");
		
			var DIRTY_WATER = configWorld.get("DIRTY_WATER");
			var SUN_SPEED = configWorld.get("SUN_SPEED");
			var SUN_LENGHT = configWorld.get("SUN_LENGHT");
			var SUN_POSITION = configWorld.get("SUN_POSITION");
			var BASE_SUN_POWER = configWorld.get("BASE_SUN_POWER");
			var ADD_SUN_POWER = configWorld.get("ADD_SUN_POWER");
			var SUN_FORM = configWorld.get("SUN_FORM");
			
			var LEVEL_MINERAL = configWorld.get("LEVEL_MINERAL");
			var CONCENTRATION_MINERAL = configWorld.get("CONCENTRATION_MINERAL");
		} else {
			makeDefaultWord(WORLD_TYPE.valueOf(configWorld.get("WORLD_TYPE")), map.get(0),map.get(1));
			AGGRESSIVE_ENVIRONMENT = configWorld.get("AGGRESSIVE_ENVIRONMENT");
			TIK_TO_EXIT = configWorld.get("TIK_TO_EXIT");
			DIRTY_WATER = configWorld.get("DIRTY_WATER");
			configWorld.getAJ("SUNS").forEach( j -> {
				try {
					suns.add(SunAbstract.generate(j, version));
				} catch (GenerateClassException ex) {
					Logger.getLogger(Configurations.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					JOptionPane.showMessageDialog(null,	Configurations.getFormatProperty(this.getClass(),"loadSerror",j.toBeautifulJSONString(),ex),	"BioLife", JOptionPane.ERROR_MESSAGE);
				}					
			});
			configWorld.getAJ("MINERALS").forEach( j -> {
				try {
					minerals.add(MineralAbstract.generate(j, version));
				} catch (GenerateClassException ex) {
					Logger.getLogger(Configurations.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					JOptionPane.showMessageDialog(null,	Configurations.getFormatProperty(this.getClass(),"loadMerror",j.toBeautifulJSONString(),ex),	"BioLife", JOptionPane.ERROR_MESSAGE);
				}
			});
			//configWorld.getAJ("STREAMS").forEach( j -> streams.add(Stream.generate(j, version)));
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
		//configWorld.add("STREAMS", streams.stream().map(s -> s.toJSON()).toList());
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
				buildMap(type, width, height, new HashMap<CellObject.LV_STATUS, Gravitation>(){{put(CellObject.LV_STATUS.LV_ORGANIC, new Gravitation(2, Gravitation.Direction.DOWN));}});
				suns.add(new SunRectangle(20, new Trajectory(new Point(width/2,0)), (int) (width* 0.77), 1, false,"Постоянное"));
				suns.add(new SunEllipse(
						20, 
						new TrajectoryLine(50, new Point(0, 0),new Point(width/2, 0),new Point(width-1, 0)), 
						width/8,height/2, 
						false,"Движущееся"));
				//Эти минералы будут занимать только 33% мира
				minerals.add(new MineralRectangle(20,20d / (height * 0.33), new Trajectory(new Point(0,height-1)),width/2, 1, false,"Постоянная"));
				//А эти будут иногда подниматься достаточно высоко
				minerals.add(new MineralEllipse(20,20d / (height * 0.33), 
						new TrajectoryEllipse(500,new Point(width / 2, height * 7 / 8), -Math.PI, 1, height * 3 / 8)
						,width * 1 / 4, height * 1 / 8, true,"Движущееся"));
			}
			default -> throw new AssertionError();
		}
		//И конечно создаём адама.
		world.makeAdam();
		
		
		
		//Создаём солнце. Одно неподвижное, одно движущееся
		//suns.add(new Sun(new Sun.Rectangle(new Point(0,0), MAP_CELLS.width, (int) (MAP_CELLS.height * 0.33), 20, Sun.SunForm.SHADOW.DOWN), null));
		//suns.add(new Sun(new Sun.SpecForm(width/2, width / 5, (int) (MAP_CELLS.height * 0.66), -3, 20, Sun.SunForm.SHADOW.DOWN), new Sun.LineMove(15, new Point(1,0))));
		
		
		//streams.add(new Stream.VerticalRectangle(new Point(MAP_CELLS.width / 8, 0), MAP_CELLS.width / 4, MAP_CELLS.height, 1, Stream.SHADOW.LINE, 100));
		//streams.add(new Stream.VerticalRectangle(new Point(MAP_CELLS.width / 2 + MAP_CELLS.width / 8, 0), MAP_CELLS.width / 4, MAP_CELLS.height, -10, Stream.SHADOW.PARABOLA, -100));
		//streams.add(new Stream.Ellipse(new Point(MAP_CELLS.width / 2, MAP_CELLS.height / 2), MAP_CELLS.width / 8, 1, Stream.SHADOW.PARABOLA, 10));
	}
	/**Создаёт поле мира. Только поле. Пустая карта, да дерево эволюции.
	 * @param type тип создаваемого мира
	 * @param width ширина мира, в кубиках.
	 * @param height высота мира, тоже в кубиках
	 * @param gravitation гравитация в созданном мире для каждого типа объектов. 
	 *				Если не указывать тип, гравитация на него действовать не будет
	 */
	public static void buildMap(WORLD_TYPE type, int width, int height, Map<CellObject.LV_STATUS, Gravitation> gravitation){
		//Создаём мир
		MAP_CELLS = new Dimension(width,height);
		world_type = type;
		world = new World(MAP_CELLS);
		//Солнца
		suns = new ArrayList<>();
		//Минералы
		minerals = new ArrayList<>(0);
		//Потоки
		streams = new ArrayList<>(0);
		//Мутагенность воды
		DAGGRESSIVE_ENVIRONMENT = AGGRESSIVE_ENVIRONMENT = 25;
		//Скорость разложения органики. За сколько шагов уходит 1 единица энергии
		TIK_TO_EXIT = DTIK_TO_EXIT = 1000;
		 //Чтобы освещалось только 33 % мира при силе света в 20 единиц
		DDIRTY_WATER = DIRTY_WATER =  20d / (height * 0.33);
		//Создаём магическое притяжение
		for(var i : CellObject.LV_STATUS.values){
			Configurations.gravitation[i.ordinal()] = (gravitation != null && gravitation.containsKey(i)) ? gravitation.get(i) : Gravitation.NONE;
		}
		
		//А теперь дерево эволюции
		tree = new EvolutionTree();
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

	/**Функция сохранения - сохранит мир в определённый файл
	 * @param filePatch - путь, куда сохранить мир
	 * @throws java.io.IOException так как мы работаем с файловой системой, мы всегда имеем шансы уйти с ошибкой
	 */
	public static void save(String filePatch) throws IOException {
		boolean oldStateWorld = Configurations.world.isActiv();		
		Configurations.world.awaitStop();

		var js = SaveAndLoad.save(filePatch, Configurations.VERSION);
		js.addActionListener( e-> System.out.println("Сохранение " + e.now + " из " + e.all + ". Осталось " + (e.getTime()/1000) + "c"));
		js.save(new Configurations(), Configurations.tree, Configurations.world);
		if (oldStateWorld)
			Configurations.world.start();
		else
			Configurations.world.stop();
	}
	/**Функция загрузки
	 * @param filePatch - путь, куда сохранить мир
	 * @throws java.io.IOException так как мы работаем с файловой системой, мы всегда имеем шансы уйти с ошибкой
	 */
	public static void load(String filePatch) throws IOException {
		boolean oldStateWorld = Configurations.world.isActiv();			
		Configurations.world.awaitStop();

		var js = SaveAndLoad.load(filePatch);
		js.addActionListener( e-> System.out.println("Загрузка " + e.now + " из " + e.all + ". Осталось " + (e.getTime()/1000) + "c"));
		js.load(new Configurations());
		Configurations.tree = js.load(Configurations.tree);
		Configurations.world = js.load((j,v) -> new World(j, v, MAP_CELLS), Configurations.world.getName());
		
		if (oldStateWorld)
			Configurations.world.start();
		else
			Configurations.world.stop();
	}
	/**
	 * Возвращает форматированную строку описания для определённого класса
	 * @param cls - класс, в котором эта строка находится
	 * @param name - ключ
	 * @param arguments аргументы, которые будут вставленны в строку свойств
	 * @return Строка в формате HTML
	 */
	public static String getFormatHProperty(Class<?> cls, String name, Object ... arguments) {
		return MessageFormat.format(Configurations.getHProperty(cls,name),arguments);
	}
	/**
	 * Возвращает форматированную строку описания для определённого класса
	 * @param cls - класс, в котором эта строка находится
	 * @param name - ключ
	 * @param arguments аргументы, которые будут вставленны в строку свойств
	 * @return Строка текста
	 */
	public static String getFormatProperty(Class<?> cls, String name, Object ... arguments) {
		return MessageFormat.format(Configurations.getProperty(cls,name),arguments);
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
			return Configurations.bundle.getString(name);
		} catch (MissingResourceException e) {
			var err = "Не найдено свойство " + name;
			Logger.getLogger(Configurations.class.getName()).log(Level.WARNING, err, e);
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
		Configurations.setIcon(button, name);
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
	public static void setIcon(JButton button, String name) {
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

		if(icon_const == null)
			button.setText(name);
		else
			button.setIcon(new ImageIcon(icon_const.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
		if(icon_select != null)
			button.setRolloverIcon(new ImageIcon(icon_select.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
		else if(icon_const != null)
			button.setRolloverIcon(new ImageIcon(icon_const.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH)));
		
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusable(false);
	}
	
	/**Тестирует шрифт на возможность вывода на экран всех ключей.
	 * @param font тестируемый шрифт
	 * @return список символов, которые невозможно вывыести на экран
	 */
	public static Set<String> testFont(java.awt.Font font){
		var ret = new HashSet<String>();
		for (var iterator = Configurations.bundle.getKeys(); iterator.hasMoreElements();) {
			String key = iterator.nextElement();
			for(var ch : Configurations.bundle.getString(key).split("")){
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
		Configurations.TIME_OUT_POOL.scheduleWithFixedDelay(() -> 
			{
				try {
					task.taskStep();
				} catch (Exception ex) {
					Logger.getLogger(Configurations.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				}
			}, ms, ms, TimeUnit.MILLISECONDS);
	}
}
