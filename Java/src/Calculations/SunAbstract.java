package Calculations;

import GUI.AllColors;
import GUI.WorldView.Transforms;
import Utils.JSON;
import Utils.SaveAndLoad;
import java.awt.Graphics2D;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	/**Объект, описывающий как можно создать тот или иной объект и какие параметры у него можно менять*/
	public static class ConstructAndChanged{
		/**Имя типа, который будет создан или отредактирован*/
		public final String name;
		
		/**Некоторый параметр или для конструктора или для регулирования*/
		public static class Param{
			/**Название параметра*/
			public final String name;
			/**Тип параметра*/
			public final Type type;
			
			public enum Type {INT,BOOLEAN}
			
			public Param(String n, Type t){name = n;type = t;}
		}
		
		public ConstructAndChanged(String n){
			name = n;
		}
	}
	
	/**Создаёт солнце
	 * @param p максимальная энергия солнца, будто и не было тени
	 * @param move форма движения солнца. Доступны изначально LineMove и EllipseMove
	 * @param n название солнца
	 */
	public SunAbstract(double p, Trajectory move, String n){
		super(p,move,n);
	}
	/**Обязательный конструктор для восстановления объекта
	 * @param j описание предка
	 * @param v версия файла
	 * @throws Calculations.GenerateClassException может возникать при создании родителя из ошибочного файла JSON
	 */
	protected SunAbstract(JSON j, long v) throws GenerateClassException{
		super(j,v);
	}
	/**
	 * Возвращает количество солнечной энергии в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии.
	 */
	public abstract double getEnergy(Point pos);
	
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("_className", this.getClass().getName());
		return j;
	}
	
	/** * Создаёт реальное солнце на основе JSON файла.Тут может быть любое из существующих солнц
	 * @param json объект, описывающий солнце
	 * @param version версия файла json в котором объект сохранён
	 * @return найденное солнце... Или null, если такого солнца не бывает
	 * @throws GenerateClassException исключение, вызываемое ошибкой
	 */
	public static SunAbstract generate(JSON json, long version) throws GenerateClassException{
		String className = json.get("_className");
		try{
			final var ac = Class.forName(className).asSubclass(SunAbstract.class);
			var constructor = ac.getDeclaredConstructor(JSON.class, long.class);
			return constructor.newInstance(json,version);
		}catch (ClassNotFoundException ex)		{throw new GenerateClassException(ex,className);}
		catch (NoSuchMethodException ex)		{throw new GenerateClassException(ex);}
		catch (InstantiationException ex)		{throw new GenerateClassException(ex);}
		catch (IllegalAccessException ex)		{throw new GenerateClassException(ex);} 
		catch (IllegalArgumentException ex)		{throw new GenerateClassException(ex);}
		catch (InvocationTargetException ex)	{throw new GenerateClassException(ex);}
	}
}
