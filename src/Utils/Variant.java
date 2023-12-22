/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import java.util.Arrays;

/**
 * Класс для хранения и контроля нескольких объектов в одной переменной
 * @author Kerravitarr
 */
public class Variant {
	/**Хранимый объект*/
	private Object _value;
	/**Все доступные классы, которые мы можем хранить*/
	private final Class<?>[] _cls; 
	/**
	 * 
	 * @param cls 
	 */
	public Variant(Class<?> ... cls){
		_cls = cls;
		set(null);
	}
	/**Сохраняет значение объекта
	 * @param <T> тип объекта
	 * @param object сам объект
	 * @throws IllegalArgumentException если данный вариантный тип не может хранить такое значение
	 */
	public <T> void set(T object) throws IllegalArgumentException{
		if(object != null && !isValid(object.getClass())) throw new IllegalArgumentException("Недопустимо хранить " + object.getClass() + " среди " + this);
		else _value = object;
	}
	/**
	 * Проверяет, может ли такое значение тут храниться
	 * @param <T> тип значения
	 * @param cl класс описывающий объект, или null, для проверки на null
	 * @return true, если тут можно хранить такой тип
	 */
	public <T> boolean isValid(Class<T> cl){
		if(cl == null) return _value == null;
		else return Arrays.stream(_cls).filter(c -> c.isAssignableFrom(cl)).findFirst().orElse(null) != null;
	}
	/**
	 * Проверяет, теущий содержащийся объект соответствует переданному типу
	 * @param <T> тип, который мы ожидаем получить
	 * @param cl класс описывающий объект, или null, для проверки на null
	 * @return true, если тут искомый класс
	 */
	public <T> boolean isContains(Class<T> cl){
		if(cl == null) return _value == null;
		else return _value != null && cl.isAssignableFrom(_value.getClass());
	}
	/**
	 * Проверяет, на пустоту храняющееся значение
	 * @return true, если тут искомый класс
	 */
	public boolean isNull(){return _value == null;}
	/**
	 * Возврвщает текущее значение
	 * @param <T> тип, который нужен
	 * @param cl описание этого типа
	 * @return содержащееся значение
	 * @throws IllegalArgumentException если данный вариантный тип не может хранить такое значение
	 */
	public <T> T get(Class<T> cl) throws IllegalArgumentException{
		if(isContains(cl)) return (T) _value;
		else throw new IllegalArgumentException("Недопустимо получить " + cl + " среди " + this);
	}
	@Override
	public String toString(){
		return Arrays.toString(Arrays.stream(_cls).map( cl -> cl.getName()).toArray());
	}
}
