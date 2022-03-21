package main;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JOptionPane;

import MapObjects.CellObject;
import MapObjects.Geyser;
import MapObjects.Sun;
import Utils.JSONmake;
import panels.BotInfo;
import panels.Settings;

/**
 * Так как некоторые переменные мира используются повсеместно
 * то они вынесены сюда!
 * @author Илья
 *
 */
public class Configurations {
	//Карта
	/**Количиство ячеек карты*/
	public static Dimension MAP_CELLS = new Dimension(500,200);
	/**Сам мир*/
	public static CellObject [][] worldMap = new CellObject[MAP_CELLS.width][MAP_CELLS.height];
	/**Базовая освещённость карты, то есть сколько света падает постоянно*/
	public static int BASE_SUN_POWER = 8;
	/**Освещённость карты*/
	public static int ADD_SUN_POWER = 4;
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
	/**Как глубоко лежат минералы*/
	public static double LEVEL_MINERAL = 0.50;
	/**Концентрация минералов*/
	public static double CONCENTRATION_MINERAL = 1;
	//Как долго разлагается органика
	public static int TIK_TO_EXIT = 2;
	/**Вязкость яда, как быстро он растекается*/
	public static int POISON_STREAM = 500;
	
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

	/**Сюда отправляем бота, для его изучения*/
	public static BotInfo info = null;
	/**Настройки мира*/
	public static Settings settings = null;
	
	/**Сохраняет конфигурацию мира*/
	public static JSONmake toJSON() {
		JSONmake configWorld = new JSONmake();
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
		configWorld.add("POISON_STREAM", POISON_STREAM);
		return configWorld;
	}
	/**Загрузка конфигурации мира*/
	public static void load(JSONmake configWorld) {
		List<Long> map = configWorld.getAL("MAP_CELLS");
		if(map.get(0) != MAP_CELLS.width || map.get(1) != MAP_CELLS.height) {
			MAP_CELLS.width = map.get(0).intValue();
			MAP_CELLS.height = map.get(1).intValue();
			worldMap = new CellObject[MAP_CELLS.width][MAP_CELLS.height];
		}
		DIRTY_WATER = configWorld.getD("DIRTY_WATER");
		AGGRESSIVE_ENVIRONMENT = configWorld.getD("AGGRESSIVE_ENVIRONMENT");
		LEVEL_MINERAL = configWorld.getD("LEVEL_MINERAL");
		CONCENTRATION_MINERAL = configWorld.getD("CONCENTRATION_MINERAL");
		TIK_TO_EXIT = configWorld.getI("TIK_TO_EXIT");
		SUN_SPEED = configWorld.getI("SUN_SPEED");
		SUN_LENGHT = configWorld.getI("SUN_LENGHT");
		SUN_POSITION = configWorld.getI("SUN_POSITION");
		BASE_SUN_POWER = configWorld.getI("BASE_SUN_POWER");
		ADD_SUN_POWER = configWorld.getI("ADD_SUN_POWER");
		POISON_STREAM = configWorld.getI("POISON_STREAM");
	}
}
