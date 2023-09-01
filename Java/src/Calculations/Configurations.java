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
import Utils.JsonSave;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import GUI.BotInfo;
import GUI.EvolTreeDialog;
import GUI.Legend;
import GUI.Menu;
import GUI.Settings;
import MapObjects.CellObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Так как некоторые переменные мира используются повсеместно
 * то они вынесены сюда!
 * @author Илья
 *
 */
public class Configurations extends JsonSave.JSONSerialization{
	/**Версия приложения. Нужна на тот случай, если вдруг будет загружаться старое приложение*/
	public static final long VERSION = 7;
	/**Количиство ячеек карты*/
	public static Dimension MAP_CELLS = null;
	/**Тип созданного мира, в котором живут живики*/
	public static WORLD_TYPE world_type;
	/**Гравитация в созданном мире.
	 * Может быть 0 или больше. При 0 - гравитации, ясное дело, нет.
	 * При 1 все тела, что должны падать, будут стремиться падать каждый ход
	 * При 2 - раз в 2 хода и т.д.
	 */
	public static Map<CellObject.LV_STATUS, Integer> gravitation;
	
	
	/**Степень мутагенности воды [0,100]*/
	public static int AGGRESSIVE_ENVIRONMENT = 0;
	/**Как часто органика теряет своё ХП. Если 1 - на каждый ход. Если 2 - каждые 2 хода и т.д.*/
	public static int TIK_TO_EXIT;
	
	//Те-же переменные, только их значения по умолчанию.
	//Значения по умолчанию рассчитываются исходя из размеров мира
	//И не могут меняться пока мир неизменен
	public static int DAGGRESSIVE_ENVIRONMENT = Configurations.AGGRESSIVE_ENVIRONMENT;
	public static int DTIK_TO_EXIT = Configurations.TIK_TO_EXIT;
	
	//Отображение карты на экране
	/**Масштаб*/
	public static double scale = 1;
	/**Высота" неба в процентах"*/
	public static double UP_border = 0.01;
	/**Высота" земли в процентах"*/
	public static double DOWN_border = 0.01;
	/**Дополнительный край из-за не совершенства арены*/
	public static Dimension border = new Dimension();
	
	//Разные глобальные объекты, отвечающие за мир
	/**Глобальный мир!*/
	public static World world = null;
	/**Звёзды нашего мира*/
	public static List<Sun> suns = null;
	/**Минералы нашего мира*/
	public static List<Object> minerals = null;
	/**Потоки воды, которые заставлют клетки двигаться*/
	public static List<Stream> streams = null;
	/**Эволюционное дерево мира*/
	public static EvolutionTree tree = null;

	//Вспомогательные панели
	/**Сюда отправляем бота, для его изучения*/
	public static BotInfo info = null;
	/**Настройки мира*/
	public static Settings settings = null;
	/**Меню мира со всеми его кнопочками*/
	public static Menu menu = null;
	/**Легенда мира, как его раскрашивать?*/
	public static Legend legend = null;
	/**Дерево эволюции*/
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
	
	/**Переводчик для всех названий. В теории*/
	private static ResourceBundle bundle = ResourceBundle.getBundle("locales/locale", Locale.getDefault());
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
		/**И, наконец, круглое поле, но без стенок - просто кусок океана*/
		FIELD_C,
	}
	
	@Override
	public String getName() {
		return "CONFIG_WORLD";
	}
	/**Сохраняет конфигурацию мира*/
	public JSON getJSON() {
		JSON configWorld = new JSON();
		configWorld.add("MAP_CELLS", new int[] {MAP_CELLS.width,MAP_CELLS.height});
		configWorld.add("AGGRESSIVE_ENVIRONMENT", AGGRESSIVE_ENVIRONMENT);
		configWorld.add("TIK_TO_EXIT", TIK_TO_EXIT);
		return configWorld;
	}
	/**Загрузка конфигурации мира*/
	public void setJSON(JSON configWorld, long version) {
		List<Integer> map = configWorld.getA("MAP_CELLS");
		if(version < 7){
			makeWorld(WORLD_TYPE.LINE_H, map.get(0),map.get(1), new HashMap<CellObject.LV_STATUS, Integer>(){{put(CellObject.LV_STATUS.LV_ORGANIC, 2);}});
			AGGRESSIVE_ENVIRONMENT = configWorld.get("AGGRESSIVE_ENVIRONMENT");
			TIK_TO_EXIT = configWorld.get("TIK_TO_EXIT");
		
			var DIRTY_WATER = configWorld.get("DIRTY_WATER");
			var LEVEL_MINERAL = configWorld.get("LEVEL_MINERAL");
			var CONCENTRATION_MINERAL = configWorld.get("CONCENTRATION_MINERAL");
			var SUN_SPEED = configWorld.get("SUN_SPEED");
			var SUN_LENGHT = configWorld.get("SUN_LENGHT");
			var SUN_POSITION = configWorld.get("SUN_POSITION");
			var BASE_SUN_POWER = configWorld.get("BASE_SUN_POWER");
			var ADD_SUN_POWER = configWorld.get("ADD_SUN_POWER");
			var SUN_FORM = configWorld.get("SUN_FORM");
		} else {
			
		}
	}
	
	/**
	 * Создаёт новый мир.Если длина и высота мира изменяются - все объекты мира удаляются!
	 * В круглых мирах ширина и высота одинаковы, берётся максимальное из двух чисел
	 *	означают-же они диаметр мира. Да, поле будет квадратным, но в некоторые точки попасть станет невозможно
	 * @param type тип создаваемого мира
	 * @param width ширина мира, в кубиках.
	 * @param height высота мира, тоже в кубиках
	 * @param gravitation гравитация в созданном мире для каждого типа объектов. 
	 *				Если не указывать тип, гравитация на него действовать не будет
	 */
	public static void makeWorld(WORLD_TYPE type, int width, int height, Map<CellObject.LV_STATUS, Integer> gravitation) {
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
		//Создаём магическое притяжение
		Configurations.gravitation = gravitation;
		
		//А теперь дерево эволюции
		tree = new EvolutionTree();
		//И конечно создаём адама.
		world.makeAdam();
		
		
		
		//Создаём солнце. Одно неподвижное, одно движущееся
		suns.add(new Sun(new Sun.Rectangle(new Point(0,0), MAP_CELLS.width, (int) (MAP_CELLS.height * 0.33), 20, Sun.SunForm.SHADOW.DOWN), null));
		suns.add(new Sun(new Sun.SpecForm(width/2, width / 5, (int) (MAP_CELLS.height * 0.66), -3, 20, Sun.SunForm.SHADOW.DOWN), new Sun.LineMove(15, new Point(1,0))));
		
		
		streams.add(new Stream.VerticalRectangle(new Point(MAP_CELLS.width / 8, 0), MAP_CELLS.width / 4, MAP_CELLS.height, 1, Stream.SHADOW.LINE, 100));
		streams.add(new Stream.VerticalRectangle(new Point(MAP_CELLS.width / 2 + MAP_CELLS.width / 8, 0), MAP_CELLS.width / 4, MAP_CELLS.height, -10, Stream.SHADOW.PARABOLA, -100));
		streams.add(new Stream.Ellipse(new Point(MAP_CELLS.width / 2, MAP_CELLS.height / 2), MAP_CELLS.width / 8, 1, Stream.SHADOW.PARABOLA, 10));
	}
	/**Возвращает количество солнечной энергии в данной точке пространства
	 * @param pos где интересует энергия
	 * @return сколько в единицах HP энергии тут
	 */
	public static double getSunPower(Point pos){
		return suns.stream().reduce(0d, (a,b) -> a + b.getEnergy(pos), Double::sum);
	}
	
	/**
	 * Возвращает строку описания для определённого класса
	 * @param cls - класс, в котором эта строка находится
	 * @param name - ключ
	 * @return Строка в формате HTML
	 */
	public static String getHProperty(Class<?> cls, String name) {
		return MessageFormat.format("<HTML>{0}",getProperty(cls,name));
	}
	/**
	 * Возвращает строку описания для определённого класса
	 * @param cls - класс, в котором эта строка находится
	 * @param name - ключ
	 * @return Строка в формате HTML
	 */
	public static String getProperty(Class<?> cls, String name) {
		try {
			return Configurations.bundle.getString(MessageFormat.format("{0}.{1}", cls.getTypeName(),name));
		} catch (MissingResourceException e) {
			var err = MessageFormat.format("Не найдено свойство {0}.{1}", cls.getTypeName(),name);
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
					System.err.println(ex);
					ex.printStackTrace(System.err);
				}
			}, ms, ms, TimeUnit.MILLISECONDS);
	}
}
