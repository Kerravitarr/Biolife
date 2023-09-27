/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import Calculations.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Это основной класс-строитель для всех объектов карты.
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
	public interface NumberP{
		/**Возвращает минимальное значение для слайдера.
		 * Слайдером можно задавать значение этой переменной без ввода числа. Просто слайдер
		 * @return числовое минимальное значение
		 */
		public int getSliderMinimum();
		/**Возвращает максимальное значение для слайдера.
		 * Слайдером можно задавать значение этой переменной без ввода числа. Просто слайдер
		 * @return числовое максимальное значение
		 */
		public int getSliderMaximum();
		
		/**Возвращает минимальное значение для переменной.
		 * Так как слайдер ограничен - он короткий и на нём не всегда удобно вводить числа с маленьким шагом
		 * то есть возможсность ввода вручную
		 * Слайдером можно задавать значение этой переменной без ввода числа. Просто слайдер
		 * @return числовое минимальное значение или null, если снизу параметр не ограничен
		 */
		public Integer getRealMinimum();
		/**Возвращает максимальное значение для переменной.
		 * Так как слайдер ограничен - он короткий и на нём не всегда удобно вводить числа с маленьким шагом
		 * Слайдером можно задавать значение этой переменной без ввода числа. Просто слайдер
		 * @return числовое максимальное значение или null, если сверху параметр не ограничен
		 */
		public Integer getRealMaximum();
	}
	/**Параметр типа число*/
	public interface NumberParam<T> extends NumberP, EditParametr<Integer, T>{}
	/**Векторный параметр типа число. То ессть требуется задать ряд чисел, от 1 до бесконечности*/
	public interface NumberVectorParam<T> extends NumberP, EditParametr<Integer[], T>{}
	
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
	public static abstract class NumberConstructorParam<T> extends ConstructorParametr<Integer, T> implements NumberP{}
	/**Векторный параметр типа число. То ессть требуется задать ряд чисел, от 1 до бесконечности*/
	public static abstract class NumberVectorConstructorParam<T> extends ConstructorParametr<Integer[], T> implements NumberP{}
	
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
		public void addParam(NumberConstructorParam<CT> bp){addEditParam(bp);}
		/**
		 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
		 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
		 */
		public void addParam(NumberVectorConstructorParam<CT> bp){addEditParam(bp);}
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
		private <R extends ConstructorParametr<R, CT>> R getParam_(int index, Class<R> type){
			final var p = _params.get(index);
			if(p.value.getClass().equals(type))
				return (R) p.value;
			else
				throw new ClassCastException("Невозможно привести " + p.value.getClass() + " к классу " + type);
		}
		
		/**Возвращает параметр по типу
		 * @param index порядковый номер параметра
		 * @param type тип параметраметра
		 * @return числовое значение параметра
		 */
		protected boolean getParam(int index, Class<BooleanConstructorParam<CT>> type){ return getParam_(index, type);}
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
	public void addParam(NumberParam<T> bp){addEditParam(bp);}
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param bp параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public void addParam(NumberVectorParam<T> bp){addEditParam(bp);}
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
	 * Добавляет конструктор объекта. 
	 * @param constructor конструктор объекта, который может создать новый объект
	 */
	public void addConstructor(Constructor<T> constructor){_constructors.add(constructor);}
	
	/**Создаёт объект из предложенных параметров.
	 * Главное не забыть в конструкторе вызвать конструктор родителя с такими параметрами!
	 * @param json описание объекта в форме JSON
	 * @param version версия файла JSON
	 * @return объект заявленного типа
	 */
	public abstract T build(JSON json, long version);
}
