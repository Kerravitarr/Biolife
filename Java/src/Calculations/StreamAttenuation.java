/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import Utils.JSON;
import java.lang.reflect.InvocationTargetException;

/**
 *Способ затухания энергии от максимальной к минимальной
 * @author Kerravitarr
 */
public abstract class StreamAttenuation {
	/**Нет затухания*/
	public static class NoneStreamAttenuation extends StreamAttenuation {
		protected NoneStreamAttenuation(JSON j, long v){super(j,v);}
		/**Поток без затухания
		* Немного о мощности потока:
		*	Она не может быть 0
		*	Если она 1, то объект толкает каждый шаг. Если 2, то каждые 2 шага и т.д.
		*  Может быть отрицательной.
		* Как правило, положительная энергия толкает клетку или в центр, или навосток (вправо), или вверх
		* @param power Энергия потока
		*/
		public NoneStreamAttenuation(int power){
			super(power,power);
		}
		@Override
		public double transform(double dist) {return 0d;}

		@Override
		public String getName() {
			return Configurations.getProperty(this.getClass(), "name");
		}
	}
	/**Линейное затухание*/
	public static class LinealStreamAttenuation extends StreamAttenuation {
		protected LinealStreamAttenuation(JSON j, long v){super(j,v);}
		/**Линейное затухание мощности потока
		* Немного о мощности потока:
		*	Она не может быть 0
		*	Если она 1, то объект толкает каждый шаг. Если 2, то каждые 2 шага и т.д.
		*  Может быть отрицательной.
		* Как правило, положительная энергия толкает клетку или в центр, или навосток (вправо), или вверх
		* @param minimum Минимальная энергия
		* @param maximum Максимальная энетргия 
		*/
		public LinealStreamAttenuation(int minimum, int maximum){
			super(minimum,maximum);
		}
		@Override
		public double transform(double dist) {return dist;}

		@Override
		public String getName() {
			return Configurations.getProperty(this.getClass(), "name");
		}
	}
	/**Степенное затухание*/
	public static class PowerFunctionStreamAttenuation extends StreamAttenuation {
		/**Степень функции*/
		private final double power;
		/** Создание степенного затухания - функции вида y=a^power
		* Немного о мощности потока:
		*	Она не может быть 0
		*	Если она 1, то объект толкает каждый шаг. Если 2, то каждые 2 шага и т.д.
		*  Может быть отрицательной.
		* Как правило, положительная энергия толкает клетку или в центр, или навосток (вправо), или вверх
		* @param minimum Минимальная энергия
		* @param maximum Максимальная энетргия 
		 * @param power Степень функции.Берётся отрезок [0,1]
		*/
		public PowerFunctionStreamAttenuation(int minimum, int maximum, double power){
			super(minimum,maximum);
			this.power = power;
		}
		protected PowerFunctionStreamAttenuation(JSON j, long v){
			super(j,v);
			power = j.get("power");
		}
		@Override
		public double transform(double dist) {return Math.pow(dist, power);	}
		@Override
		public JSON toJSON(){
			final var j = super.toJSON();
			j.add("power", power);
			return j;
		}

		@Override
		public String getName() {
			return Configurations.getProperty(this.getClass(), "name",power);
		}
	}
	/**Синусоидальное затухание. То есть минимальное значение будет при dist = 0 и 1, а максимальное при dist = 0.5*/
	public static class SinStreamAttenuation extends StreamAttenuation {
		/**Степень функции*/
		private final double power;
		/** Создание степенного затухания - функции вида y=sin(x*pi)^power
		* Немного о мощности потока:
		*	Она не может быть 0
		*	Если она 1, то объект толкает каждый шаг. Если 2, то каждые 2 шага и т.д.
		*  Может быть отрицательной.
		* Как правило, положительная энергия толкает клетку или в центр, или навосток (вправо), или вверх
		* @param minimum Минимальная энергия
		* @param maximum Максимальная энетргия 
		 * @param power Степень функции 
		*/
		public SinStreamAttenuation(int minimum, int maximum,double power){
			super(minimum,maximum);
			this.power = power;
		}
		protected SinStreamAttenuation(JSON j, long v){
			super(j,v);
			power = j.get("power");
		}
		@Override
		public double transform(double dist) {return Math.pow(Math.sin(dist * Math.PI), power);	}
		@Override
		public JSON toJSON(){
			final var j = super.toJSON();
			j.add("power", power);
			return j;
		}
		@Override
		public String getName() {
			return Configurations.getProperty(this.getClass(), "name",power);
		}
	}
	/**Сила потока на самом краешке*/
	private final int minPower;
	/**Сила потока прям в самом центре*/
	protected final int maxPower;
	/**Разница в силе потока, собственно функцией от этой величины и будет ребёнок*/
	private final int dp;
	/**Базовая заготовка для любой формы потока
	 * Немного о мощности потока:
	 *	Она не может быть 0
	 *	Если она 1, то объект толкает каждый шаг. Если 2, то каждые 2 шага и т.д.
	 *  Может быть отрицательной.
	 * Как правило, положительная энергия толкает клетку или в центр, или навосток (вправо), или вверх
	 * @param minimum Минимальная энергия
	 * @param maximum Максимальная энетргия 
	 */
	protected StreamAttenuation(int minimum, int maximum) {
		if(maximum == 0) throw new IllegalArgumentException("Мощность потока не может быть равна 0!");
		else if(maximum > 0 && minimum < 0 || maximum < 0 && minimum > 0 ) throw new IllegalArgumentException("Мощность потока не может менять направление от центра к краям!");
		minPower = minimum;
		maxPower = maximum;
		dp = maxPower - minPower;
	}
	
	protected StreamAttenuation(JSON j, long v){
		minPower = j.get("minPower");
		maxPower = j.get("maxPower");
		dp = maxPower - minPower;
	}
	
	/**Возвращает реальную мощность в определённой точки в зависимости от удалённости от центра потока
	 * @param dist расстояние от центра в процетнах [0,1]. При dist = 1 вернёт минимальное значение мощности, при dist = 0, то есть в центре, вернёт max
	 * @return мощность потока [min,max]
	 */
	public int power(double dist){
		assert 0 <= dist && dist <= 1 : "Проблемка - вышли за границу диапазона: " + dist;
		return (int) (maxPower - dp * transform(dist));
	}
	/**Возвращает реальную мощность в указанных пределах
	 * @param min минимальное значение энергии потока. При dist = 1
	 * @param max максимальное значение энергии потока. При dist = 0
	 * @param dist расстояние от центра в процетнах [0,1]
	 * @return мощность потока [min,max]
	 */
	public int power(int min, int max, double dist){
		assert 0 <= dist && dist <= 1 : "Проблемка - вышли за границу диапазона: " + dist;
		return (int) (max - (max - min) * transform(dist));
	}
	/**Функция для ребёнка.
	 * Должна преобразовать расстояние в процентах в силу потока
	 * @param dist расстояние от центра в процетнах [0,1]. 
	 * При dist = 1 вернёт 0, так как там силы нет
	 * при dist = 0, то есть в центре, вернёт 1, так как там вся сила
	 * @return мощность потока [1,0]
	 */
	protected abstract double transform(double dist);
	
	/**Имя функции снижения мощности
	 * @return строка с именем
	 */
	public abstract String getName();
	
	@Override
	public String toString(){
		return getName();
	}
	
	/**Превращает текущий объект в объект его описания
	 * @return объект описания. По нему можно гарантированно восстановить исходник
	 */
	public JSON toJSON(){
		final var j = new JSON();
		j.add("minPower", minPower);
		j.add("maxPower", maxPower);
		j.add("_className", this.getClass().getName());
		return j;
	}
	
	/** * Создаёт объект на основе JSON файла. Тут может быть любой из наследников этого класса
	 * @param json объект, описывающий искомый объект
	 * @param version версия файла json в котором объект сохранён
	 * @return найденный потомок
	 * @throws GenerateClassException исключение, вызываемое ошибкой
	 */
	public static StreamAttenuation generate(JSON json, long version) throws GenerateClassException{
		String className = json.get("_className");
		try{
			final var ac = Class.forName(className).asSubclass(StreamAttenuation.class);
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
