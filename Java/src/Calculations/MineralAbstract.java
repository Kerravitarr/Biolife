package Calculations;

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
	/**
	 * Возвращает концентрацию минералов в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии. Может быть отрицательным числом - это не поглащение минеравло, а удалённость от источника
	 */
	public abstract double getConcentration(Point pos);
	

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
		this.attenuation = attenuation;
	}
	
	/**Возвращает занчение альфа канала для цвета.
	 * @param isSelected жила выбранна или нет?
	 * @return значение альфа канала, где 0 - полная прозрачность, а 255 - полная светимость
	 */
	protected double getColorAlfa(boolean isSelected){
		final var mp = Configurations.getMaxConcentrationMinerals();
		if(mp == power){
			return isSelected ? 63 : 255;
		} else if(isSelected){
			return 255;
		} else {
			return (64 + 192 * power / mp);
		}
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
}
