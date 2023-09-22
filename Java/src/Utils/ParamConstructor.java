/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import Calculations.GenerateClassException;
import Calculations.GenerateClassException;
import Calculations.Point;
import Calculations.Point;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**Функция для работы с неформализуемыми параметрами
 * По факту, этот класс позволяет имея доступ только к указателю на объект
 * не рзабирая, что это именно за объект
 * управлять его параметрами
 * @author Kerravitarr
 */
public class ParamConstructor {
	/**Все варианты создания объекта*/
	public static class GenerateVariants<T>{
		/**Сам объект, который будет создан*/
		private final Class<T> _class; 
		/**Создаёт генератор набора конструкторов
		 * @param cl класс, который нас интересует
		 */
		public GenerateVariants(Class<T> cl) {
			_class = cl;
		}
		
		/**Параметры этого объекта*/
		private final List<GenerateObject<T>> params = new ArrayList<>();
		/**Добавляет вариант создания в набор
		 * @param param параметр объекта
		 */
		public void add(GenerateObject<T> param) {
			param._class = _class;
			params.add(param);
		}
	}
	/**Класс для создания объекта*/
	public static class GenerateObject<T>{
		/**Имя создаваемого объекта*/
		public final String name;
		/**Параметры конструктора*/
		private final List<ParamConstructor> params = new ArrayList<>();
		/**Сам объект, который будет создан*/
		private Class<T> _class; 
		/**Создаёт генератор
		 * @param n имя этого класса и конкретного набора параметров
		 */
		public GenerateObject(String n) {
			name = n;
		}
		
		/**Добавляет параметр конструктора в набор
		 * @param param параметр объекта
		 */
		public void add(ParamConstructor param){params.add(param);}
		
		/**Создаёт объект из предложенных параметров, которые ранее были установленны
		 * @return готовый объект, который мы смогли создать
		 * @throws Calculations.GenerateClassException если не смогли создать - то вот
		 */
		public T get() throws GenerateClassException{
			final var constructorClass = params.stream().map(p -> p.value.getClass()).toArray(Class[]::new);
			final var values = params.stream().map(p -> p.value).toArray();
			/*if(params.length != this.params.size()) throw new GenerateClassException( new NoSuchMethodException("Количество параметров не совпало с ожидаемым!"));
			final var constructorClass = new Class<?>[params.length];
			for (int i = 0; i < params.length; i++) {
				constructorClass[i] = params[i].getClass();
			}*/
			try{
				var constructor = _class.getDeclaredConstructor(constructorClass);
				return constructor.newInstance(values);
			} catch (NoSuchMethodException ex)		{throw new GenerateClassException(ex);}
			catch (InstantiationException ex)		{throw new GenerateClassException(ex);}
			catch (IllegalAccessException ex)		{throw new GenerateClassException(ex);} 
			catch (IllegalArgumentException ex)		{throw new GenerateClassException(ex);}
			catch (InvocationTargetException ex)	{throw new GenerateClassException(ex);}
		}
	}
	
	
	/**Название параметра*/
	public final String name;
	/**Тип параметра*/
	public final Type type;
	/**Параметр в единичном варианте или таких может быть любое количество?*/
	public final boolean isOne;
	
	/**Текущее значение параметра*/
	private Object value;

	/**Создаёт параметр
	 * @param n название
	 * @param def значение по умолчанию
	 * @param isMany указатель на то, что это не один параметр, а целый ряд параметров
	 */
	public ParamConstructor(String n, Point def, boolean isMany) {
		name = n;
		type = Type.POINT;
		isOne = !isMany;
		value = def;
	}
	/**Создаёт параметр
	 * @param n название
	 * @param def значение по умолчанию
	 */
	public ParamConstructor(String n, Point def) {
		this(n,def,false);
	}

	public enum Type {
		INT, STRING, BOOLEAN, POINT
	}

	/**Позволяет сохранить значение в параметра
	 * @param value сохраняемое значение
	 * @throws IllegalArgumentException может возникать, если у нас множественный параметр, а нам передали только одно значение
	 */
	public void setValue(Object value) throws IllegalArgumentException{
		if(isOne)
			this.value = value;
		else
			throw new IllegalArgumentException("Не может быть один параметр для набора параметров");
	}
	/**Позволяет сохранить значение в параметра
	 * @param value сохраняемое значение
	 * @throws IllegalArgumentException может возникать, если у нас множественный параметр, а нам передали только одно значение
	 */
	public void setValue(Object[] value) throws IllegalArgumentException{
		if(!isOne)
			this.value = value;
		else
			throw new IllegalArgumentException("Не может быть набора параметров для одиночного параметра");
	}
}
