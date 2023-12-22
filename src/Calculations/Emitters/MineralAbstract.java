package Calculations.Emitters;

import Calculations.Configurations;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import Utils.ClassBuilder;
import Utils.JSON;
import java.util.List;

/**
 * Болванка минералов.
 * И да... Полная копипаста. Минералы отличаются не внешним видом!
 * @author Илья
 *
 */
public abstract class MineralAbstract extends DefaultEmitter{
	/**Построитель для любых потомков текущего класса*/
	private final static ClassBuilder.StaticBuilder<MineralAbstract> BUILDER = new ClassBuilder.StaticBuilder<>();
	
	/**Коээфициент "затухания" для минералов. Каждая минеральная нычка может иметь своё затухание*/
	private double attenuation;

	/**Создаёт источник минералов
	 * @param p максимальная энергия солнца, будто и не было тени
	 * @param a затухание. На сколько единиц/клетку уменьшается количество минералов вдали от объекта
	 * @param move форма движения солнца. Доступны изначально LineMove и EllipseMove
	 * @param name название залежи
	 * @param isLine если true, то у нас излучает только поверхность, а если false - то излучает вся площадь
	 */
	public MineralAbstract(double p,double a, Trajectory move, String name, boolean isLine){
		super(p,move,name, isLine);
		attenuation = a;
	}	
	/**Обязательный конструктор для восстановления объекта
	 * @param j описание предка
	 * @param v версия файла
	 */
	protected MineralAbstract(JSON j, long v){
		super(j,v);
		attenuation = j.get("attenuation");
	}	

	/**Растворимость минералов, как быстро теряется сила
	 * @return сколько минералов теряется на 1 клетку поля
	 */
	public double getAttenuation() {
		return attenuation;
	}

	/** Сохраняет растворимость минералов
	 * @param attenuation сколько минералов теряется на 1 клетку поля
	 */
	public void setAttenuation(double attenuation) {
		if(attenuation != this.attenuation)
			updateMatrix();
		this.attenuation = attenuation;
	}
	
	
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("attenuation", getAttenuation());
		j.add("_className", this.getClass().getName());
		return j;
	}
	
	/** * Регистрирует наследника как одного из возможных дочерних классов.
	 * @param <T> класс наследника текущего класса
	 * @param builder фабрика по созданию наследников
	 */
	protected static <T extends MineralAbstract> void register(ClassBuilder<T> builder){BUILDER.register(builder);};
	/** * Создаёт реальный объект на основе JSON файла.
	 * Самое главное, чтобы этот объект был ранее зарегистрирован в этом классе
	 * @param json объект, описывающий объект подкласса CT
	 * @param version версия файла json в котором объект сохранён
	 * @return объект сериализации
	 * 
	 */
	public static MineralAbstract generation(JSON json, long version){return BUILDER.generation(json, version);}
	/**Укладывает текущий объект в объект сереализации для дальнейшего сохранения
	 * @param <T> тип объекта, который надо упаковать. Может быть любым наследником текущего класса,
	 *  зарегистрированного ранее в классе
	 * @param object объект, который надо упаковать
	 * @return JSON объект или null, если такой класс не зарегистрирован у нас
	 */
	public static <T extends MineralAbstract> JSON serialization(T object){return BUILDER.serialization(object);}
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
		if(attenuation == 0d){
			g.setColor(GUI.AllColors.MINERALS);
			g.fillRect(transform.toScrinX(0), transform.toScrinY(0),transform.toScrin(Configurations.getWidth()), transform.toScrin(Configurations.getHeight()));
		} else {
			super.paint(g, transform);
		}
	}
}