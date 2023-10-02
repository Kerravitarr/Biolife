/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import Calculations.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Это основной класс-строитель для всех объектов карты.
 * 
 * 
 * @author Kerravitarr
 * @param <T> Тип строящегося объекта
 */
public abstract class ClassBuilder <T>{
	/**Параметр
	 * @param <P> тип этого параметра - Integer, Boolean... 
	 */
	public interface Parametr<P>{
		/**Возвращает значение по умолчанию для текущего параметра.
		 * @return Значение к которому вернётся параметр по желанию пользователя
		 */
		public P getDefault();
		/**Возвращает имя параметра. По этому имени будет искаться локализованное название параметра в файлах локали
		 * @return название ключа имени параметра
		 */
		public String name();
	}
	/**
	 * Изменяемый параметр реально существующего класса
	 * @param <P> тип этого параметра - Integer, Boolean...
	 * @param <T> собственно тот класс, чьим параметром будет являеться этот объект
	 */
	public interface EditParametr<P, T> extends Parametr<P>{
		/**Возвращает текущее значение параметра
		 * @param who чей именно параметр интересует нас очень
		 * @return текущее значение параметра. Его тип обязан совпадать с типом параметра!
		 */
		public abstract P get(T who);
		/**Позволяет сохранить значение в текущий объект
		 * @param who объект, в который сохраняется параметр
		 * @param value сохраняемое значение
		 */
		public abstract void setValue(T who, P value);
	}

	/**Параметр типа Да/Нет*/
	public interface BooleanParam<T> extends EditParametr<Boolean, T>{}
	/**Векторный параметр типа Да/Нет. То ессть требуется задать ряд значений типа boolean, от 1 до бесконечности*/
	public interface BooleanVectorParam<T> extends EditParametr<Boolean[], T>{}
	
	/**Параметр типа Строка*/
	public interface StringParam<T> extends EditParametr<String, T>{}
	/**Векторный параметр типа Строка. То ессть требуется задать ряд значений типа String, от 1 до бесконечности*/
	public interface StringVectorParam<T> extends EditParametr<String[], T>{}
	
	/**Параметр типа число*/
	public interface NumberP<T extends Number>{
		/**Возвращает минимальное значение для слайдера.
		 * Слайдером можно задавать значение этой переменной без ввода числа. Просто слайдер
		 * @return числовое минимальное значение
		 */
		public T getSliderMinimum();
		/**Возвращает максимальное значение для слайдера.
		 * Слайдером можно задавать значение этой переменной без ввода числа. Просто слайдер
		 * @return числовое максимальное значение
		 */
		public T getSliderMaximum();
		
		/**Возвращает минимальное значение для переменной.
		 * Так как слайдер ограничен - он короткий и на нём не всегда удобно вводить числа с маленьким шагом
		 * то есть возможсность ввода вручную
		 * Слайдером можно задавать значение этой переменной без ввода числа. Просто слайдер
		 * @return числовое минимальное значение или null, если снизу параметр не ограничен
		 */
		public T getRealMinimum();
		/**Возвращает максимальное значение для переменной.
		 * Так как слайдер ограничен - он короткий и на нём не всегда удобно вводить числа с маленьким шагом
		 * Слайдером можно задавать значение этой переменной без ввода числа. Просто слайдер
		 * @return числовое максимальное значение или null, если сверху параметр не ограничен
		 */
		public T getRealMaximum();
	}
	/**Параметр типа число*/
	public interface NumberParam<NT extends Number, T> extends NumberP<NT>, EditParametr<NT, T>{}
	/**Параметр типа число, адаптер*/
	public abstract static class NumberParamAdapter<TP extends Number,T> implements NumberParam<TP,T>{
		private final String name;
		private final TP smin;
		private final TP def;
		private final TP smax;
		private final TP min;
		private final TP max;
		/**
		 * Создаёт числовой параметр
		 * @param name его имя
		 * @param sliderMin минимальное значение графического слайдера
		 * @param def значение по умолчанию
		 * @param sliderMaximum макисмальное значение графического слайдера
		 * @param min миниманльно возможное значение
		 * @param max максимально возможное значение
		 */
		public NumberParamAdapter(String name, TP sliderMin, TP def, TP sliderMaximum, TP min, TP max){
			this.name = name; smin=sliderMin; smax = sliderMaximum; this.def= def; this.min = min; this.max = max;
		}
		@Override public TP getDefault() {return def;}
		@Override public String name() {return name;}
		@Override public TP getSliderMinimum() {return smin;}
		@Override public TP getSliderMaximum() {return smax;}
		@Override public TP getRealMinimum() { return min;}
		@Override public TP getRealMaximum() { return max;}
	}
	/**Векторный параметр типа число. То ессть требуется задать ряд чисел, от 1 до бесконечности*/
	public interface NumberVectorParam<NT extends Number, T> extends NumberP<NT>, EditParametr<Integer[], T>{}
	
	/**Параметр типа точка на карте*/
	public interface MapPointParam<T> extends EditParametr<Point, T>{}
	/**Векторный параметр типа точка на карте. То ессть требуется задать ряд точек, от 1 до бесконечности*/
	public interface MapPointVectorParam<T> extends EditParametr<Point[], T>{}
	
	
	/**Параметр типа абстрактные два значения. Х - первое значение, Y - второе*/
	public interface Abstract2P{
		/**Минимальное значение для первого параметра
		 * @return меньше него не получится задать
		 */
		public int get1Minimum();
		/**Значение по умолчанию для первого параметра
		 * @return оно будет выставленно при сбросе
		 */
		public int get1Default();
		/**Максимальное значение для первого параметра
		 * @return больше него нельзя будет задать
		 */
		public int get1Maximum();
		/**Минимальное значение для второго параметра
		 * @return меньше него не получится задать
		 */
		public int get2Minimum();
		/**Значение по умолчанию для второго параметра
		 * @return оно будет выставленно при сбросе
		 */
		public int get2Default();
		/**Максимальное значение для второго параметра
		 * @return больше него нельзя будет задать
		 */
		public int get2Maximum();
	}
	/**Параметр типа абстрактные два значения. Х - первое значение, Y - второе*/
	public interface Abstract2Param<T> extends Abstract2P, EditParametr<Point.Vector, T>{}
	/**Параметр типа абстрактные два значения. Х - первое значение, Y - второе*/
	public interface Abstract2VectorParam<T> extends Abstract2P, EditParametr<Point.Vector[], T>{}
	
	
	/**Параметр конструктора объекта
	 * @param <P> тип параметра
	 * @param <T> тип объекта, чей это параметр
	 */
	public static abstract class ConstructorParametr<P, T> implements Parametr<P>{
		/**Текущее значение параметра конструктора*/
		private P value;
		
		protected ConstructorParametr(){value = getDefault();}
		
		/**Позволяет сохранить значение в текущий объект
		 * @param value сохраняемое значение
		 */
		public void setValue(P value){this.value = value;}
	}

	/**Параметр типа Да/Нет*/
	public static abstract class BooleanConstructorParam<T> extends ConstructorParametr<Boolean, T>{}
	/**Векторный параметр типа Да/Нет. То ессть требуется задать ряд значений типа boolean, от 1 до бесконечности*/
	public static abstract class BooleanVectorConstructorParam<T> extends ConstructorParametr<Boolean[], T>{}
	
	/**Параметр типа Строка*/
	public static abstract class StringConstructorParam<T> extends ConstructorParametr<String, T>{}
	/**Векторный параметр типа Строка. То ессть требуется задать ряд значений типа String, от 1 до бесконечности*/
	public static abstract class StringVectorConstructorParam<T> extends ConstructorParametr<String[], T>{}
	
	/**Параметр типа число*/
	public static abstract class NumberConstructorParam<TP extends Number,T> extends ConstructorParametr<TP, T> implements NumberP<TP>{}
	/**Параметр типа число, адаптер*/
	public static class NumberConstructorParamAdapter<TP extends Number,T> extends NumberConstructorParam<TP,T>{
		private final String name;
		private final TP smin;
		private final TP def;
		private final TP smax;
		private final TP min;
		private final TP max;
		/**
		 * Создаёт числовой параметр
		 * @param name его имя
		 * @param sliderMin минимальное значение графического слайдера
		 * @param def значение по умолчанию
		 * @param sliderMaximum макисмальное значение графического слайдера
		 * @param min миниманльно возможное значение
		 * @param max максимально возможное значение
		 */
		public NumberConstructorParamAdapter(String name, TP sliderMin, TP def, TP sliderMaximum, TP min, TP max){
			this.name = name; smin=sliderMin; smax = sliderMaximum; this.def= def; this.min = min; this.max = max;
		}
		@Override public TP getDefault() {return def;}
		@Override public String name() {return name;}
		@Override public TP getSliderMinimum() {return smin;}
		@Override public TP getSliderMaximum() {return smax;}
		@Override public TP getRealMinimum() { return min;}
		@Override public TP getRealMaximum() { return max;}
	}
	/**Векторный параметр типа число. То ессть требуется задать ряд чисел, от 1 до бесконечности*/
	public static abstract class NumberVectorConstructorParam<TP extends Number,T> extends ConstructorParametr<TP[], T> implements NumberP<TP>{}
	
	/**Параметр типа точка на карте*/
	public static abstract class MapPointConstructorParam<T> extends ConstructorParametr<Point, T>{}
	/**Векторный параметр типа точка на карте. То ессть требуется задать ряд точек, от 1 до бесконечности*/
	public static abstract class MapPointVectorConstructorParam<T> extends ConstructorParametr<Point[], T>{}
	
	/**Параметр типа абстрактные два значения. Х - первое значение, Y - второе*/
	public static abstract class Abstract2ConstructorParam<T> extends ConstructorParametr<Point.Vector, T> implements Abstract2P{}
	/**Параметр типа абстрактные два значения. Х - первое значение, Y - второе*/
	public static abstract class Abstract2VectorConstructorParam<T> extends ConstructorParametr<Point.Vector[], T> implements Abstract2P{}
	
	
	/**Конструктор класса*/
	public static abstract class Constructor<CT>{
		/**Параметры конструктора, которые можно менять по своему желанию*/
		private final List<ConstructorParametr<?,CT>> _params = new ArrayList<>();
		/**Строит и возвращает текущий объект по конструктору
		 * Для удобства нужно воспользоваться функцией getParam
		 * @return объект, который надо сконструировать
		 */
		public abstract CT build();
		/**Возвращает ключ к локализованному имени конструктора этого класса.
		 * Например, по двум точкам и углу, или по трём точкам или ещё как.
		 * @return ключ по которому в файле локализации можно найти имя этого конструктора
		 */
		public abstract String name();
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param <K> один из классов наследников, представленный в текущем классе 
		 * @param param параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		private <K extends ConstructorParametr<?, CT>> void addEditParam(K param){_params.add(param);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public void addParam(BooleanConstructorParam<CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public void addParam(BooleanVectorConstructorParam<CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public void addParam(StringConstructorParam<CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public void addParam(StringVectorConstructorParam<CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public <NT extends Number> void addParam(NumberConstructorParam<NT, CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public <NT extends Number> void addParam(NumberVectorConstructorParam<NT, CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public void addParam(MapPointConstructorParam<CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public void addParam(MapPointVectorConstructorParam<CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public void addParam(Abstract2ConstructorParam<CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public void addParam(Abstract2VectorConstructorParam<CT> bp){addEditParam(bp);}
		
		/**Возвращает параметр по типу
		 * @param <R> тип возвращаемого параметра
		 * @param index порядковый номер параметра
		 * @param type тип параметраметра
		 * @return числовое значение параметра
		 */
		private <T, R extends ConstructorParametr<T, ?>> T getParam_(int index, Class<R> type){
			final var p = _params.get(index);
			if(p.value.getClass().equals(type))
				return (T) p.value;
			else
				throw new ClassCastException("Невозможно привести " + p.value.getClass() + " к классу " + type);
		}
		/** * Возвращает параметр по по типу.Или первый, если параметр всего 1 или первый, если других таких параметров нет
		 * @param <T> тип возвращаемого параметра
		 * @param <R> объект класса параметра
		 * @param type тип параметраметра
		 * @return числовое значение параметра
		 */
		private <T, R extends ConstructorParametr<T, ?>> T getParam_(Class<R> type){
			if(_params.size() == 1) return getParam_(0,type);
			for (int index = 0; index < _params.size(); index++) {
				final var get = _params.get(index);
				if(get.getClass().equals(type))
					return getParam_(index,type);
			}
			throw new IllegalArgumentException("Отпустствует параметр " + type);
		}
		/**Возвращает параметр запрошенного класса.
		 * Доступны только Boolean, Boolean[]
		 *				   String, String[]
		 *					Integer,Integer[]
		 *					Point,Point[]
		 *					Point.Vector,Point.Vector[]
		 * @param <T> тип параметра, который нужен
		 * @param cls объект класса этого параметра
		 * @return Единственный параметр, если такой параметр правда единственный, или первый из параметров, если всего у класса много параметров
		 * @throws ClassCastException возникает при любом несоответствии классов
		 */
		protected <T> T getParam(Class<T> cls) {
			if(cls.equals(Boolean.class)) return (T) getParam_( ClassBuilder.BooleanConstructorParam.class);
			else if(cls.equals(Boolean[].class)) return (T) getParam_( ClassBuilder.BooleanVectorConstructorParam.class);
			else if(cls.equals(String.class)) return (T) getParam_( ClassBuilder.StringConstructorParam.class);
			else if(cls.equals(String[].class)) return (T) getParam_( ClassBuilder.StringVectorConstructorParam.class);
			else if(cls.equals(Integer.class)) return (T) getParam_( ClassBuilder.NumberConstructorParam.class);
			else if(cls.equals(Integer[].class)) return (T) getParam_( ClassBuilder.NumberVectorConstructorParam.class);
			else if(cls.equals(Point.class)) return (T) getParam_( ClassBuilder.MapPointConstructorParam.class);
			else if(cls.equals(Point[].class)) return (T) getParam_( ClassBuilder.MapPointVectorConstructorParam.class);
			else if(cls.equals(Point.Vector.class)) return (T) getParam_( ClassBuilder.Abstract2ConstructorParam.class);
			else if(cls.equals(Point.Vector[].class)) return (T) getParam_( ClassBuilder.Abstract2VectorConstructorParam.class);
			else throw new ClassCastException("Невозможно получить параметр типа " + cls);
		}
		
		/** * Возвращает параметр запрошенного класса.Доступны только Boolean, Boolean[]
				   String, String[]
					Integer,Integer[]
					Double,Double[]
					Point,Point[]
					Point.Vector,Point.Vector[]
		 * @param <T> тип параметра, который нужен
		 * @param index порядковый номер параметра
		 * @param cls объект класса этого параметра
		 * @return Единственный параметр, если такой параметр правда единственный, или первый из параметров, если всего у класса много параметров
		 * @throws ClassCastException возникает при любом несоответствии классов
		 */
		protected <T> T getParam(int index, Class<T> cls) {
			if(cls.equals(Boolean.class)) return (T) getParam_(index, ClassBuilder.BooleanConstructorParam.class);
			else if(cls.equals(Boolean[].class)) return (T) getParam_(index, ClassBuilder.BooleanVectorConstructorParam.class);
			else if(cls.equals(String.class)) return (T) getParam_( index,ClassBuilder.StringConstructorParam.class);
			else if(cls.equals(String[].class)) return (T) getParam_( index,ClassBuilder.StringVectorConstructorParam.class);
			else if(cls.equals(Integer.class)) return (T) getParam_( index,ClassBuilder.NumberConstructorParam.class);
			else if(cls.equals(Integer[].class)) return (T) getParam_( index,ClassBuilder.NumberVectorConstructorParam.class);
			else if(cls.equals(Double.class)) return (T) getParam_( index,ClassBuilder.NumberConstructorParam.class);
			else if(cls.equals(Double[].class)) return (T) getParam_( index,ClassBuilder.NumberVectorConstructorParam.class);
			else if(cls.equals(Point.class)) return (T) getParam_( index,ClassBuilder.MapPointConstructorParam.class);
			else if(cls.equals(Point[].class)) return (T) getParam_( index,ClassBuilder.MapPointVectorConstructorParam.class);
			else if(cls.equals(Point.Vector.class)) return (T) getParam_( index,ClassBuilder.Abstract2ConstructorParam.class);
			else if(cls.equals(Point.Vector[].class)) return (T) getParam_( index,ClassBuilder.Abstract2VectorConstructorParam.class);
			else throw new ClassCastException("Невозможно получить параметр типа " + cls);
		}
	}
	/**Класс для постройки объектов */
	public static class StaticBuilder<CT>{
		/**Все объекты сереализации по имени*/
		private final Map<String, ClassBuilder<? extends CT>> OBJECTS_BY_NAME = new HashMap<>();
		/**Все объекты сереализации по реальному классу*/
		private final Map<Class<?>, ClassBuilder<? extends CT>> OBJECTS_BY_CLASS = new HashMap<>();
		
		/** * Регистрирует наследника как одного из возможных дочерних классов.То есть регистрирует объект, через который можно создавать любых наследников
		 * @param <T> любой класс наследник текущего
		 * @param object фабрика по созданию наследников
		 */
		public <T extends CT> void register(ClassBuilder<T> object) {
			if(OBJECTS_BY_NAME.containsKey(object.serializerName()))
				throw new IllegalArgumentException("Объект с серилаизационным именем " + object.serializerName() + " уже зарегистрирован");
			else
				OBJECTS_BY_NAME.put(object.serializerName(), object);
			if(OBJECTS_BY_CLASS.containsKey(object.printName()))
				throw new IllegalArgumentException("Объект класса " + object.printName() + " уже зарегистрирован");
			else
				OBJECTS_BY_CLASS.put(object.printName(), object);
		}
		
		/** * Возвращает построитель объекта по его классу
		 * @param <T> любой класс наследник текущего
		 * @param object объект класса этого типа
		 * @return фабрика для построения этого объекта
		 */
		public <T extends CT> ClassBuilder<T> get(Class<T> object) {
			return (ClassBuilder<T>) OBJECTS_BY_CLASS.get(object.getClass());
		}
		
		
		/** * Создаёт реальный объект на основе JSON файла.
		 * Самое главное, чтобы этот объект был ранее зарегистрирован в этом классе
		 * @param json объект, описывающий объект подкласса CT
		 * @param version версия файла json в котором объект сохранён
		 * @return объект сериализации
		 * 
		 */
		public CT generation(JSON json, long version){
			return OBJECTS_BY_NAME.get((String)json.get("_serializerName")).generation(json, version);
		}
		/**Укладывает текущий объект в объект сереализации для дальнейшего сохранения
		 * @param <T> тип объекта, который надо упаковать. Может быть любым наследником текущего класса,
		 *  зарегистрированного ранее в классе
		 * @param object объект, который надо упаковать
		 * @return JSON объект или null, если такой класс не зарегистрирован у нас
		 */
		public <T extends CT> JSON serialization(T object){
			final var builder = get((Class<T>) object.getClass());
			final var json = builder.serialization(object);
			json.add("", builder.serializerName());
			return json;
		}
	}
	
	/**Параметры объекта, которые можно менять по своему желанию*/
	private final List<EditParametr> _params = new ArrayList<>();
	/**Конструкторы объекта из которых можно создать текущий объект*/
	private final List<Constructor> _constructors = new ArrayList<>();
	
	/**Имя класса, который может создавать этот строитель. То есть название самого класса
	 * может измениться, а имя его останется
	 * @return постоянное имя. По нему будет искаться объект
	 */
	public abstract String serializerName();
	/**Объект класса. Он нужен, потому что по нему будет получено
	 * локализованное имя создаваемого объекта
	 * @return класс по которому будет создано локализованное имя объекта
	 */
	public abstract Class<T> printName();
	
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param <K> один из классов наследников, представленный в текущем классе 
	 * @param param параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	private <K extends EditParametr<?, T>> void addEditParam(K param){_params.add(param);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public void addParam(BooleanParam<T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public void addParam(BooleanVectorParam<T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public void addParam(StringParam<T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public void addParam(StringVectorParam<T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public <NT extends Number> void addParam(NumberParam<NT,T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public <NT extends Number> void addParam(NumberVectorParam<NT, T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public void addParam(MapPointParam<T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public void addParam(MapPointVectorParam<T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public void addParam(Abstract2Param<T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public void addParam(Abstract2VectorParam<T> bp){addEditParam(bp);}
	
	
	/**
	 * Добавляет конструктор объекта. Таких конструкторов может быть сколько угодно
	 * @param constructor конструктор объекта, который может создать новый объект
	 */
	public void addConstructor(Constructor<T> constructor){_constructors.add(constructor);}
	
	/**Создаёт объект из предложенных параметров.
	 * Главное не забыть в конструкторе вызвать конструктор родителя с такими параметрами!
	 * @param json описание объекта в форме JSON
	 * @param version версия файла JSON
	 * @return объект заявленного типа
	 */
	public abstract T generation(JSON json, long version);
	
	/** * Преобразует объект в серилизованный объект, который можно теперь отправить в любое место
	 * @param object объект, который надо сереализовать
	 * @return JSON объект из которого можно восстановить текущий объект
	 */
	public abstract JSON serialization(T object);
	/**Возвращает список всех параметров объекта
	 * @return список со всеми праметрами
	 */
	public List<EditParametr> getParams(){return _params;};
}
