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
import Utils.JsonSave;
import java.awt.Font;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import panels.BotInfo;
import panels.EvolTreeDialog;
import panels.Menu;
import panels.Settings;

/**
 * Так как некоторые переменные мира используются повсеместно
 * то они вынесены сюда!
 * @author Илья
 *
 */
public class Configurations extends JsonSave.JSONSerialization{
	/**Версия приложения. Нужна на тот случай, если вдруг будет загружаться старое приложение*/
	public static final long VERSION = 6;
	
	//Карта
	/**Количиство ячеек карты*/
	public static Dimension MAP_CELLS = new Dimension(0,0);
	/**Сам мир*/
	public static CellObject [][] worldMap = new CellObject[MAP_CELLS.width][MAP_CELLS.height];
	/**Базовая освещённость карты, то есть сколько света падает постоянно*/
	public static int BASE_SUN_POWER = 0;
	/**Освещённость карты*/
	public static int ADD_SUN_POWER = 0;
	/**Скорость движения солнца в тиках мира*/
	public static int SUN_SPEED = 15;
	/**Положение солнца в частях экрана*/
	public static int SUN_POSITION = 0;
	/**"Ширина" солнечного света в частях экрана*/
	public static int SUN_LENGHT = 0;
	/**Форма солнца*/
	public static int SUN_FORM = 0;
	/**Уровень загрязнения воды. Процент, где заканчивается Солнце. В норме от 0 до 200*/
	public static int DIRTY_WATER = 0;
	/**Как глубоко лежат минералы. При этом 1.0 - ни где, а 0.0 - везде... Ну да, так получилось :)*/
	public static double LEVEL_MINERAL = 0;
	/**Концентрация минералов. Другими словами - количество всасываемых минералов в секунду при максимальной специализации*/
	public static int CONCENTRATION_MINERAL = 0;
	/**Степень мутагенности воды [0,100]*/
	public static int AGGRESSIVE_ENVIRONMENT = 0;
	/**Сколько ходов до разложения органики*/
	public static int TIK_TO_EXIT;
	
	//Те-же переменные, только их значения по умолчанию.
	//Значения по умолчанию рассчитываются исходя из размеров мира
	//И не могут меняться пока мир неизменен
	public static int DBASE_SUN_POWER = Configurations.BASE_SUN_POWER;
	public static int DADD_SUN_POWER = Configurations.ADD_SUN_POWER;
	public static int DSUN_LENGHT = Configurations.SUN_LENGHT;
	public static int DSUN_SPEED = Configurations.SUN_SPEED;
	public static int DSUN_FORM = Configurations.SUN_FORM;
	public static int DDIRTY_WATER = Configurations.DIRTY_WATER;
	
	public static double DLEVEL_MINERAL = Configurations.LEVEL_MINERAL;
	public static int DCONCENTRATION_MINERAL = Configurations.CONCENTRATION_MINERAL;
	
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
	/**Дерево эволюции*/
	public static EvolTreeDialog evolTreeDialog = null;
	
	
	//Общие классы для программы
	/**ГСЧ для симуляции*/
	public static SplittableRandom rnd = new SplittableRandom();
	/**Основной потоковый пулл для всяких задач которым нужно выполняться периодически*/
	public static final ScheduledThreadPoolExecutor TIME_OUT_POOL = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1,new ThreadFactory(){
		@Override
		public Thread newThread(Runnable task) {return new Thread(task, "TIME_OUT_TASK");}
	});
	
	//Общее форматирование
	/**Фон по умолчанию для всех компонентов*/
	public static java.awt.Font defaultFont = new java.awt.Font("Verdana", Font.BOLD, 12); 
	/**Уменьешнный размер шрифта для необходимых элементов*/
	public static java.awt.Font smalFont = new java.awt.Font("Verdana", Font.PLAIN, 10); 
	
	/**Переводчик для всех названий. В теории*/
	private static ResourceBundle bundle = ResourceBundle.getBundle("locales/locale", Locale.getDefault());
	
	
	@Override
	public String getName() {
		return "CONFIG_WORLD";
	}
	/**Сохраняет конфигурацию мира*/
	public JSON getJSON() {
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
		configWorld.add("SUN_FORM", SUN_FORM);
		return configWorld;
	}
	/**Загрузка конфигурации мира*/
	public void setJSON(JSON configWorld, long version) {
		List<Integer> map = configWorld.getA("MAP_CELLS");
		makeWorld(map.get(0),map.get(1));
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
		SUN_FORM = configWorld.get("SUN_FORM");
	}
	
	/**
	 * Создаёт новый мир.
	 * Если длина и высота мира изменяются - все объекты мира удаляются!
	 * @param width ширина мира, в кубиках
	 * @param height высота мира, тоже в кубиках
	 */
	public static void makeWorld(int width, int height) {
		if(width != MAP_CELLS.width || height != MAP_CELLS.height) {
			MAP_CELLS.width = width;
			MAP_CELLS.height = height;
			worldMap = new CellObject[MAP_CELLS.width][MAP_CELLS.height];
		}
		//Освещение
		setBASE_SUN_POWER(DBASE_SUN_POWER = 20);
		setADD_SUN_POWER(DADD_SUN_POWER = DBASE_SUN_POWER);
		setSUN_LENGHT(DSUN_LENGHT = width / 5);
		setSUN_SPEED(DSUN_SPEED = 25); //Раз в 25 шагов сдвигается
		setSUN_FORM(DSUN_FORM = -3);
		SUN_POSITION = width / 2;
		setDIRTY_WATER(DDIRTY_WATER = 33); //33% карты сверху - освщеено
		
		DLEVEL_MINERAL = LEVEL_MINERAL = 1 - 0.33;	//33% снизу в минералах
		DCONCENTRATION_MINERAL = CONCENTRATION_MINERAL = 20;
		DAGGRESSIVE_ENVIRONMENT = AGGRESSIVE_ENVIRONMENT = 25;
		TIK_TO_EXIT = DTIK_TO_EXIT = 1000; //1 единица энергии уходит за 1000 шагов!
	}
	
	public static void setBASE_SUN_POWER(int val) {
		Configurations.BASE_SUN_POWER =  val;
		if(sun != null)
			sun.updateScrin();
	}
	public static void setADD_SUN_POWER(int val) {
		Configurations.ADD_SUN_POWER =  val;
		if(sun != null)
			sun.updateScrin();
	}
	public static void setSUN_LENGHT(int val) {
		Configurations.SUN_LENGHT =  val;
		if(sun != null)
			sun.updateScrin();
	}
	public static void setSUN_SPEED(int val) {
		Configurations.SUN_SPEED =  val;
		if(sun != null)
			sun.updateScrin();
	}
	public static void setDIRTY_WATER(int val) {
		Configurations.DIRTY_WATER =  val;
		if(sun != null)
			sun.updateScrin();
	}
	public static void setCONCENTRATION_MINERAL(int val) {
		Configurations.CONCENTRATION_MINERAL =  val;
		if(sun != null)
			sun.updateScrin();
	}
	public static void setLEVEL_MINERAL(double val) {
		Configurations.LEVEL_MINERAL =  val;
		if(sun != null)
			sun.updateScrin();
	}
	public static void setSUN_FORM(int val) {
		Configurations.SUN_FORM =  val;
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
}
