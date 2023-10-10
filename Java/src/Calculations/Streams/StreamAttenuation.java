/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Streams;

import Calculations.Configurations;
import Utils.ClassBuilder;
import Utils.JSON;
import java.lang.reflect.InvocationTargetException;

/**
 *Способ затухания энергии от максимальной к минимальной
 * @author Kerravitarr
 */
public abstract class StreamAttenuation {
	/**Нет затухания*/
	public static class NoneStreamAttenuation extends StreamAttenuation {
		static{
			final var builder = new ClassBuilder<NoneStreamAttenuation>(){
				@Override public NoneStreamAttenuation generation(JSON json, long version){return new NoneStreamAttenuation(json, version);}
				@Override public JSON serialization(NoneStreamAttenuation object) { return object.toJSON();}

				@Override public String serializerName() {return "Без затухания";}
				@Override public Class printName() {return NoneStreamAttenuation.class;}

			};
			builder.addConstructor(new ClassBuilder.Constructor<NoneStreamAttenuation>(){
				{
					addParam(new ClassBuilder.NumberConstructorParamAdapter<>("power",-1000,0,1000,null,null));
				}
				@Override public NoneStreamAttenuation build() {return new NoneStreamAttenuation(getParam(Integer.class));}
				@Override public String name() {return "name";}
			});
			StreamAttenuation.register(builder);
		}
		
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
	}
	/**Линейное затухание*/
	public static class LinealStreamAttenuation extends StreamAttenuation {
		static{
			final var builder = new ClassBuilder<LinealStreamAttenuation>(){
				@Override public LinealStreamAttenuation generation(JSON json, long version){return new LinealStreamAttenuation(json, version);}
				@Override public JSON serialization(LinealStreamAttenuation object) { return object.toJSON();}

				@Override public String serializerName() {return "Линейная";}
				@Override public Class printName() {return LinealStreamAttenuation.class;}

			};
			builder.addConstructor(new ClassBuilder.Constructor<LinealStreamAttenuation>(){
				{
					addParam(new ClassBuilder.NumberConstructorParamAdapter<>("minimum",-1000,0,1000,null,null));
					addParam(new ClassBuilder.NumberConstructorParamAdapter<>("maximum",-1000,0,1000,null,null));
				}
				@Override public LinealStreamAttenuation build() {return new LinealStreamAttenuation(getParam(0,Integer.class),getParam(1,Integer.class));}
				@Override public String name() {return "name";}
			});
			StreamAttenuation.register(builder);
		}
		
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
		public double transform(double dist) {return 1 - dist;}

	}
	/**Степенное затухание*/
	public static class PowerFunctionStreamAttenuation extends StreamAttenuation {
		static{
			final var builder = new ClassBuilder<PowerFunctionStreamAttenuation>(){
				@Override public PowerFunctionStreamAttenuation generation(JSON json, long version){return new PowerFunctionStreamAttenuation(json, version);}
				@Override public JSON serialization(PowerFunctionStreamAttenuation object) { return object.toJSON();}

				@Override public String serializerName() {return "Степенное";}
				@Override public Class printName() {return PowerFunctionStreamAttenuation.class;}

			};
			builder.addConstructor(new ClassBuilder.Constructor<PowerFunctionStreamAttenuation>(){
				{
					addParam(new ClassBuilder.NumberConstructorParamAdapter("minimum",-1000,0,1000,null,null));
					addParam(new ClassBuilder.NumberConstructorParamAdapter("power",-10d,1d,10d,null,null));
					addParam(new ClassBuilder.NumberConstructorParamAdapter("maximum",-1000,0,1000,null,null));
				}
				@Override
				public PowerFunctionStreamAttenuation build() {
					return new PowerFunctionStreamAttenuation(getParam(0, Integer.class), getParam(2, Integer.class),getParam(1, Double.class));
				}
				@Override public String name() {return "name";}
			});
			StreamAttenuation.register(builder);
		}
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
		public double transform(double dist) {return 1d - Math.pow(dist, power);	}
		@Override
		public JSON toJSON(){
			final var j = super.toJSON();
			j.add("power", power);
			return j;
		}
	}
	/**Синусоидальное затухание. То есть минимальное значение будет при dist = 0 и 1, а максимальное при dist = 0.5*/
	public static class SinStreamAttenuation extends StreamAttenuation {
		static{
			final var builder = new ClassBuilder<SinStreamAttenuation>(){
				@Override public SinStreamAttenuation generation(JSON json, long version){return new SinStreamAttenuation(json, version);}
				@Override public JSON serialization(SinStreamAttenuation object) { return object.toJSON();}

				@Override public String serializerName() {return "Синусоидальное";}
				@Override public Class printName() {return SinStreamAttenuation.class;}

			};
			builder.addConstructor(new ClassBuilder.Constructor<SinStreamAttenuation>(){
				{
					addParam(new ClassBuilder.NumberConstructorParamAdapter("minimum",-1000,0,1000,null,null));
					addParam(new ClassBuilder.NumberConstructorParamAdapter("power",-10d,1d,10d,null,null));
					addParam(new ClassBuilder.NumberConstructorParamAdapter("maximum",-1000,0,1000,null,null));
				}
				@Override
				public SinStreamAttenuation build() {
					return new SinStreamAttenuation(getParam(0, Integer.class), getParam(2, Integer.class),getParam(1, Double.class));
				}
				@Override public String name() {return "name";}
			});
			StreamAttenuation.register(builder);
		}
		
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
	}
	
	/**Сила потока на самом краешке*/
	private final int minPower;
	/**Сила потока прям в самом центре*/
	protected final int maxPower;
	/**Разница в силе потока, собственно функцией от этой величины и будет ребёнок*/
	private final int dp;
	/**Построитель для любых потомков текущего класса*/
	private final static ClassBuilder.StaticBuilder<StreamAttenuation> BUILDER = new ClassBuilder.StaticBuilder<>();
	
	
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
	
	@Override
	public String toString(){
		return Configurations.getProperty(BUILDER.get(this.getClass()).printName(), "name");
	}
	
	/**Превращает текущий объект в объект его описания
	 * @return объект описания. По нему можно гарантированно восстановить исходник
	 */
	public JSON toJSON(){
		final var j = new JSON();
		j.add("minPower", minPower);
		j.add("maxPower", maxPower);
		return j;
	}
	
	
	/** * Регистрирует наследника как одного из возможных дочерних классов.То есть мы можем создать траекторию такого типа
	 * @param <T> класс наследника текущего класса
	 * @param trajectory фабрика по созданию наследников
	 */
	protected static <T extends StreamAttenuation> void register(ClassBuilder<T> trajectory){BUILDER.register(trajectory);};
	/** * Создаёт реальный объект на основе JSON файла.
	 * Самое главное, чтобы этот объект был ранее зарегистрирован в этом классе
	 * @param json объект, описывающий объект подкласса CT
	 * @param version версия файла json в котором объект сохранён
	 * @return объект сериализации
	 * 
	 */
	public static StreamAttenuation generation(JSON json, long version){return BUILDER.generation(json, version);}
	/**Укладывает текущий объект в объект сереализации для дальнейшего сохранения
	 * @param <T> тип объекта, который надо упаковать. Может быть любым наследником текущего класса,
	 *  зарегистрированного ранее в классе
	 * @param object объект, который надо упаковать
	 * @return JSON объект или null, если такой класс не зарегистрирован у нас
	 */
	public static <T extends StreamAttenuation> JSON serialization(T object){return BUILDER.serialization(object);}
}
