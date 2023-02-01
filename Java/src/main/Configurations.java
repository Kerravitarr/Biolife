package main;

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
import MapObjects.Geyser;
import MapObjects.Sun;
import Utils.JSON;
import panels.BotInfo;
import panels.Menu;
import panels.Settings;

/**
 * Так как некоторые переменные мира используются повсеместно
 * то они вынесены сюда!
 * @author Илья
 *
 */
public class Configurations {
	/**Версия приложения. Нужна на тот случай, если вдруг будет загружаться старое приложение*/
	public static final String VERSION = "2.0";
	
	//Карта
	/**Количиство ячеек карты*/
	//public static Dimension MAP_CELLS = new Dimension(500/4,200/4);
	public static Dimension MAP_CELLS = new Dimension(500,200);
	/**Сам мир*/
	public static CellObject [][] worldMap = new CellObject[MAP_CELLS.width][MAP_CELLS.height];
	/**Базовая освещённость карты, то есть сколько света падает постоянно*/
	public static int BASE_SUN_POWER = 8;
	/**Освещённость карты*/
	public static int ADD_SUN_POWER = 8;
	//Скорость движения солнца в тиках мира
	public static int SUN_SPEED = 15;
	//Положение солнца в частях экрана
	public static int SUN_POSITION = 0;
	/**"Ширина" солнечного света в частях экрана*/
	public static int SUN_LENGHT = 30;
	/**На сколько частей нужно поделить экран для солнца*/
	public static final int SUN_PARTS = 60;
	/**Уровень загрязнения воды*/
	public static double DIRTY_WATER = 19;
	/**Степень мутагенности воды*/
	public static double AGGRESSIVE_ENVIRONMENT = 0.25;
	/**Как глубоко лежат минералы. При этом 1.0 - ни где, а 0.0 - везде... Ну да, так получилось :)*/
	public static double LEVEL_MINERAL = 0.30;
	/**Концентрация минералов. Другими словами - количество всасываемых минералов в секунду при максимальной специализации*/
	public static double CONCENTRATION_MINERAL = 1;
	/**Сколько ходов до разложения органики*/
	public static int TIK_TO_EXIT = 4;
	
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
	/**Солнце нашего мира*/
	public static Sun sun = null;
	/**Гейзер, некая область где вода поднимается снизу вверх*/
	public static Geyser[] geysers = null;
	/**Эволюция ботов нашего мира*/
	public static EvolutionTree tree = new EvolutionTree();

	//Вспомогательные панели
	/**Сюда отправляем бота, для его изучения*/
	public static BotInfo info = null;
	/**Настройки мира*/
	public static Settings settings = null;
	/**Меню мира со всеми его кнопочками*/
	public static Menu menu = null;
	
	//Общие классы для программы
	/**ГСЧ для симуляции*/
	public static SplittableRandom rnd = new SplittableRandom();
	/**Основной потоковый пулл для всяких задач которым нужно выполняться периодически*/
	public static final ScheduledThreadPoolExecutor TIME_OUT_POOL = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1,new ThreadFactory(){
		@Override
		public Thread newThread(Runnable task) {return new Thread(task, "TIME_OUT_TASK");}
	});
	/**Переводчик для всех названий. В теории*/
	public static ResourceBundle bundle = ResourceBundle.getBundle("locales/locale", Locale.getDefault());
	
	
	/**Сохраняет конфигурацию мира*/
	public static JSON toJSON() {
		JSON configWorld = new JSON();
		configWorld.add("BASE_SUN_POWER", BASE_SUN_POWER);
		configWorld.add("ADD_SUN_POWER", ADD_SUN_POWER);
		configWorld.add("MAP_CELLS", new int[] {MAP_CELLS.width,MAP_CELLS.height});
		configWorld.add("DIRTY_WATER", DIRTY_WATER);
		configWorld.add("AGGRESSIVE_ENVIRONMENT", AGGRESSIVE_ENVIRONMENT);
		configWorld.add("LEVEL_MINERAL", LEVEL_MINERAL);
		configWorld.add("CONCENTRATION_MINERAL", CONCENTRATION_MINERAL);
		configWorld.add("TIK_TO_EXIT", TIK_TO_EXIT);
		configWorld.add("SUN_SPEED", SUN_SPEED);
		configWorld.add("SUN_LENGHT", SUN_LENGHT);
		configWorld.add("SUN_POSITION", SUN_POSITION);
		return configWorld;
	}
	/**Загрузка конфигурации мира*/
	public static void load(JSON configWorld) {
		List<Integer> map = configWorld.getA("MAP_CELLS");
		setSize(map.get(0),map.get(1));
		DIRTY_WATER = configWorld.get("DIRTY_WATER");
		AGGRESSIVE_ENVIRONMENT = configWorld.get("AGGRESSIVE_ENVIRONMENT");
		LEVEL_MINERAL = configWorld.get("LEVEL_MINERAL");
		CONCENTRATION_MINERAL = configWorld.get("CONCENTRATION_MINERAL");
		TIK_TO_EXIT = configWorld.get("TIK_TO_EXIT");
		SUN_SPEED = configWorld.get("SUN_SPEED");
		SUN_LENGHT = configWorld.get("SUN_LENGHT");
		SUN_POSITION = configWorld.get("SUN_POSITION");
		BASE_SUN_POWER = configWorld.get("BASE_SUN_POWER");
		ADD_SUN_POWER = configWorld.get("ADD_SUN_POWER");
	}
	
	/**
	 * Сохраняет новые размеры мира
	 * @param width ширина мира, в кубиках
	 * @param height высота мира, тоже в кубиках
	 */
	public static void setSize(int width, int height) {
		if(width != MAP_CELLS.width || height != MAP_CELLS.height) {
			MAP_CELLS.width = width;
			MAP_CELLS.height = height;
			worldMap = new CellObject[MAP_CELLS.width][MAP_CELLS.height];
		}
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
			System.err.println(MessageFormat.format("Не найдено свойство {0}.{1}", cls.getTypeName(),name));
			throw e;
		}
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
	}
}
