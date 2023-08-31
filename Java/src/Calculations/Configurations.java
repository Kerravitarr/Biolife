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

import MapObjects.CellObject;
import Utils.JSON;
import Utils.JsonSave;
import java.awt.Font;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import GUI.BotInfo;
import GUI.EvolTreeDialog;
import GUI.Legend;
import GUI.Menu;
import GUI.Settings;
import java.util.ArrayList;

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
	/**Уровень загрязнения воды. Процент, где заканчивается Солнце. В норме от 0 до 200*/
	public static int DIRTY_WATER = 0;
	/**Степень мутагенности воды [0,100]*/
	public static int AGGRESSIVE_ENVIRONMENT = 0;
	/**Как часто органика теряет своё ХП. Если 1 - на каждый ход. Если 2 - каждые 2 хода и т.д.*/
	public static int TIK_TO_EXIT;
	
	//Те-же переменные, только их значения по умолчанию.
	//Значения по умолчанию рассчитываются исходя из размеров мира
	//И не могут меняться пока мир неизменен
	public static int DDIRTY_WATER = Configurations.DIRTY_WATER;
	
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
	
	@Override
	public String getName() {
		return "CONFIG_WORLD";
	}
	/**Сохраняет конфигурацию мира*/
	public JSON getJSON() {
		JSON configWorld = new JSON();
		configWorld.add("MAP_CELLS", new int[] {MAP_CELLS.width,MAP_CELLS.height});
		configWorld.add("DIRTY_WATER", DIRTY_WATER);
		configWorld.add("AGGRESSIVE_ENVIRONMENT", AGGRESSIVE_ENVIRONMENT);
		configWorld.add("TIK_TO_EXIT", TIK_TO_EXIT);
		return configWorld;
	}
	/**Загрузка конфигурации мира*/
	public void setJSON(JSON configWorld, long version) {
		List<Integer> map = configWorld.getA("MAP_CELLS");
		makeWorld(map.get(0),map.get(1));
		DIRTY_WATER = configWorld.get("DIRTY_WATER");
		AGGRESSIVE_ENVIRONMENT = configWorld.get("AGGRESSIVE_ENVIRONMENT");
		TIK_TO_EXIT = configWorld.get("TIK_TO_EXIT");
		if(version < 7){
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
	 * Создаёт новый мир.
	 * Если длина и высота мира изменяются - все объекты мира удаляются!
	 * @param width ширина мира, в кубиках
	 * @param height высота мира, тоже в кубиках
	 */
	public static void makeWorld(int width, int height) {
		//Создаём мир
		MAP_CELLS = new Dimension(width,height);
		world = new World(MAP_CELLS);
		
		//Создаём солнце. Одно неподвижное, одно движущееся
		suns = new ArrayList<>(2);
		suns.add(new Sun(20,null,null,0, Integer.MIN_VALUE));
		suns.add(new Sun(20,width / 5,25,width / 2, -3));
		//И грязь воды
		setDIRTY_WATER(DDIRTY_WATER = 33); //33% карты сверху - освщеено
		//Создаём минералы. Один тип, покачивающийся вверх/вниз
		minerals = new ArrayList<>(1);
		//suns.add(new Sun(20,width / 5,25,width / 2, -3));
		
		DLEVEL_MINERAL = LEVEL_MINERAL = 1 - 0.33;	//33% снизу в минералах
		DCONCENTRATION_MINERAL = CONCENTRATION_MINERAL = 20;
		DAGGRESSIVE_ENVIRONMENT = AGGRESSIVE_ENVIRONMENT = 25;
		TIK_TO_EXIT = DTIK_TO_EXIT = 1000; //1 единица энергии уходит за 1000 шагов!
		
		world.makeAdam();
		streams = new Stream[0];
		sun = new Sun(getWidth(),getHeight());
	}
	
	public static void setDIRTY_WATER(int val) {
		Configurations.DIRTY_WATER =  val;
		if(sun != null)
			sun.updateScrin();
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
