package Calculations;

import GUI.AllColors;
import GUI.WorldView.Transforms;
import Utils.JSON;
import java.awt.Graphics2D;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Болванка минералов.
 * И да... Полная копипаста. Минералы отличаются не внешним видом!
 * @author Илья
 *
 */
public abstract class MineralAbstract extends DefaultEmitter{
	/**Коээфициент "затухания" для минералов. Каждая минеральная нычка может иметь своё затухание*/
	protected double attenuation;

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
	protected MineralAbstract(JSON j, long v) throws GenerateClassException{
		super(j,v);
		attenuation = j.get("attenuation");
	}
	/**
	 * Возвращает концентрацию минералов в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии. Может быть отрицательным числом - это не поглащение минеравло, а удалённость от источника
	 */
	public abstract double getConcentration(Point pos);
	
	
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("attenuation", attenuation);
		j.add("_className", this.getClass().getName());
		return j;
	}
	
	/** * Создаёт реальную залеж на основе JSON файла.Это реальный генератор
	 * @param json объект, описывающий залеж
	 * @param version версия файла json в котором объект сохранён
	 * @return найденное солнце... Или null, если такой залежи не бывает не бывает
	 * @throws Calculations.GenerateClassException ошибка загрузки
	 */
	public static MineralAbstract generate(JSON json, long version) throws GenerateClassException{
		String className = json.get("_className");
		try {
			final var ac = Class.forName(className).asSubclass(MineralAbstract.class);
			var constructor = ac.getDeclaredConstructor(JSON.class, long.class);
			return constructor.newInstance(json,version);
		}  catch (ClassNotFoundException ex)	{throw new GenerateClassException(ex,className);}
		catch (NoSuchMethodException ex)		{throw new GenerateClassException(ex);}
		catch (InstantiationException ex)		{throw new GenerateClassException(ex);}
		catch (IllegalAccessException ex)		{throw new GenerateClassException(ex);} 
		catch (IllegalArgumentException ex)		{throw new GenerateClassException(ex);}
		catch (InvocationTargetException ex)	{throw new GenerateClassException(ex);}
	}
}
