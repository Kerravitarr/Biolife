package Calculations.Emitters;

import Calculations.Configurations;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import Utils.ClassBuilder;
import Utils.JSON;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Болванка солнышка. Любое солнце должно быть похоже на это!
 * 
 *	Однако есть несколько нюансов с которыми я так и не смог совладать до сих пор.
 * К сожалению, я не такой хороший программист :(
 * 1! Каждый наследник обязан иметь конструктор, принмиюащий (JSON j, long v) и вызывающий super(j,v).
 *		Тут всё очевидно - для создания объекта из JSONа
 * 2. Каждый наследник обязан реализовать статический метод "". Нужен для создания настроек солнышка
 * @author Илья
 *
 */
public abstract class SunAbstract extends DefaultEmitter{
	/**Построитель для любых потомков текущего класса*/
	private final static ClassBuilder.StaticBuilder<SunAbstract> BUILDER = new ClassBuilder.StaticBuilder<>();
	
	/**Создаёт солнце
	 * @param p максимальная энергия солнца, будто и не было тени
	 * @param move форма движения солнца. Доступны изначально LineMove и EllipseMove
	 * @param n название солнца
	 * @param isLine если true, то у нас излучает только поверхность, а если false - то излучает вся площадь
	 */
	public SunAbstract(double p, Trajectory move, String n, boolean isLine){
		super(p,move,n, isLine);
	}
	/**Обязательный конструктор для восстановления объекта
	 * @param j описание предка
	 * @param v версия файла
	 * @throws Calculations.GenerateClassException может возникать при создании родителя из ошибочного файла JSON
	 */
	protected SunAbstract(JSON j, long v){
		super(j,v);
	}
	
	/** * Регистрирует наследника как одного из возможных дочерних классов.
	 * @param <T> класс наследника текущего класса
	 * @param builder фабрика по созданию наследников
	 */
	protected static <T extends SunAbstract> void register(ClassBuilder<T> builder){BUILDER.register(builder);};
	/** * Создаёт реальный объект на основе JSON файла.
	 * Самое главное, чтобы этот объект был ранее зарегистрирован в этом классе
	 * @param json объект, описывающий объект подкласса CT
	 * @param version версия файла json в котором объект сохранён
	 * @return объект сериализации
	 * 
	 */
	public static SunAbstract generation(JSON json, long version){return BUILDER.generation(json, version);}
	/**Укладывает текущий объект в объект сереализации для дальнейшего сохранения
	 * @param <T> тип объекта, который надо упаковать. Может быть любым наследником текущего класса,
	 *  зарегистрированного ранее в классе
	 * @param object объект, который надо упаковать
	 * @return JSON объект или null, если такой класс не зарегистрирован у нас
	 */
	public static <T extends SunAbstract> JSON serialization(T object){return BUILDER.serialization(object);}
	/**
	 * Возвращает список всех параметров объекта, которые можно покрутить в живом эфире
	 * @return список параметров объекта
	 */
	public List<ClassBuilder.EditParametr> getParams(){return BUILDER.get(this.getClass()).getParams();}
	/** * Возвращает всех возможных подклассов текущего
	 * Самое главное, чтобы эти объекты были ранее зарегистрированы в этом классе
	 * @return список из которого можно можно создать всех деток
	 */
	public static List<ClassBuilder> getChildrens(){return BUILDER.getChildrens();}
	
	
	/**Рисует объект на экране
	 * @param g холст, на котором надо начертить солнышко
	 * @param transform преобразователь размеров мировых в размеры экранные
	 */
	@Override
	public void paint(java.awt.Graphics2D g, GUI.WorldView.Transforms transform){	
		if(Configurations.confoguration.DIRTY_WATER == 0d){
			//Если у нас чистая вода, то солнце осветит собой всё, что можно
			g.setColor(GUI.AllColors.SUN);
			g.fillRect(transform.toScrinX(0), transform.toScrinY(0),transform.toScrin(Configurations.getWidth()), transform.toScrin(Configurations.getHeight()));
		} else {
			super.paint(g, transform);
		}
	}
}
