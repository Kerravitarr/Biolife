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
	 * Параметр конструктора класса
	 * @param <P> тип этого параметра - Integer, Boolean...
	 */
	public interface ConstructorParametr<P, T> extends Parametr<P>{
		/**Позволяет сохранить значение в текущий объект
		 * @param value сохраняемое значение
		 */
		public abstract void setValue(P value);
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
	protected interface NumberParam<T> extends EditParametr<Integer, T>{
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
	/**Векторный параметр типа число. То ессть требуется задать ряд чисел, от 1 до бесконечности*/
	protected interface NumberVectorParam<T> extends EditParametr<Integer[], T>{
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
	/**Параметр типа точка на карте*/
	public interface MapPointParam<T> extends EditParametr<Point, T>{}
	/**Векторный параметр типа точка на карте. То ессть требуется задать ряд точек, от 1 до бесконечности*/
	public interface MapPointVectorParam<T> extends EditParametr<Point[], T>{}
	/**Параметр типа абстрактные два значения. Х - первое значение, Y - второе*/
	public interface Abstract2Param<T> extends EditParametr<Point.Vector, T>{
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
	/**Векторный параметр типа абстрактные два значения. Х - первое значение, Y - второе. То ессть требуется задать ряд точек, от 1 до бесконечности*/
	public interface Abstract2VectorParam<T> extends EditParametr<Point.Vector, T>{
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
	
	/**Параметр конструктора объекта
	 * @param <P> тип параметра
	 * @param <T> тип объекта, чей это параметр
	 */
	public static abstract class ConstructParam<P, T> implements ConstructorParametr<P,T>{
		//Текущее значение параметра конструктора
		protected P paramValue;
		protected ConstructParam(){
			paramValue = getDefault();
		}
		/**Позволяет сохранить значение в текущий объект
		 * @param value сохраняемое значение
		 */
		@Override
		public void setValue(P value){paramValue = value;}
	}
	/**Изменяемый векторный параметр конструктора. То ессть требуется задать целый рад параметров, от 1 до бесконечности*/
	private static abstract class ConstructVectorParam <P, T> extends ConstructParam<P[], T>{}	
	
	/**Конструктор класса*/
	public static abstract class Constructor<T>{
		/**Параметры конструктора, которые можно менять по своему желанию*/
		protected final List<ConstructParam> _params = new ArrayList<>();
		/**Строит и возвращает текущий объект по конструктору
		 * @return объект, который надо сконструировать
		 */
		public T build(){
			return build(_params);
		}
		/**Задача наследника - реально сконструировать текущий класс через параметры
		 * @param _params список заданных изначально параметров с уже установленными значениями
		 * @return объект, который сконструирован
		 */
		protected abstract T build(List<ConstructParam> _params);
	}
	
	/**Название класса. Оно постоянное, по нему будут искать последователей этого класса*/
	private final String _name;
	/**Параметры объекта, которые можно менять по своему желанию*/
	private final List<EditParametr> _params = new ArrayList<>();
	/**Конструкторы объекта из которых можно создать текущий объект*/
	private final List<Constructor> _constructors = new ArrayList<>();
	
	protected ClassBuilder(String n) {
		_name = n;
	}
	
	/**Имя класса, который может создавать этот строитель. То есть название самого класса
	 * может измениться, а имя его останется
	 * @return постоянное имя. По нему будет искаться объект
	 */
	public String name(){return _name;}
	
	/**
	 * Добавляет параметр объекта.Этот параметр можно менять по своему желанию
	 * @param <K> один из классов наследников, представленный в текущем классе 
	 * @param param параметр, который может дать своё значение, значение по умолчанию и обрабатывать изменение значения
	 */
	public <K extends EditParametr<?, T>> void addParam(K param){_params.add(param);}
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
