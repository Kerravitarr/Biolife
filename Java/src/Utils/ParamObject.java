/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**Функция для работы с неформализуемыми параметрами
 * По факту, этот класс позволяет имея доступ только к указателю на объект
 * не рзабирая, что это именно за объект
 * управлять его параметрами
 * @author Kerravitarr
 */
public abstract class ParamObject {
	/**Набор изменяемых параметров*/
	public static class EditParams {
		/**Параметры этого объекта*/
		private final List<ParamObject> params = new ArrayList<ParamObject>();
		/**Добавляет параметр в набор
		 * @param param параметр объекта
		 */
		public void add(ParamObject param){params.add(param);}
	}
	
	/**Название параметра*/
	public final String name;
	/**Тип параметра*/
	public final Type type;
	/**Минимальное значение параметра для ползунка*/
	public final Integer minD;
	/**Максимальное значение параметра для ползунка*/
	public final Integer maxD;
	/**Минимальное значение параметра в целом, физически*/
	public final Integer minA;
	/**Максимальное значение параметра в целом, физически*/
	public final Integer maxA;

	/**Создаёт строковую переменную
	 * @param n название
	 * @param lenght максимальная длина строки
	 */
	public ParamObject(String n, int lenght) {
		name = n;
		type = Type.STRING;
		minD = minA = null;
		maxD = maxA = lenght;
	}

	/**Создаёт числовую переменную
	 * @param n название
	 * @param minD минимальное значение для ползунка настройки
	 * @param minA минимальное значение для ползунка настройки
	 * @param maxD максимальное значение физически или null, если любое
	 * @param maxA максимальное значение физически или null, если любое
	 */
	public ParamObject(String n, int minD, int maxD, Integer minA, Integer maxA) {
		name = n;
		type = Type.INT;
		this.minD = minD;
		this.minA = minA;
		this.maxD = maxD;
		this.maxA = maxA;
	}

	/**Создаёт логическую переменную
	 * @param n название
	 */
	public ParamObject(String n) {
		name = n;
		type = Type.BOOLEAN;
		minD = minA = maxD = maxA = null;
	}

	public enum Type {
		INT, STRING, BOOLEAN
	}

	/**Позволяет сохранить значение в звезду
	 * @param value сохраняемое значение
	 * @throws ClassCastException может возникать, если тип value не совпадёт с нужным
	 */
	public abstract void setValue(Object value) throws ClassCastException;

	/**Возвращает текущее значение параметра
	 * @return текущее значение параметра
	 */
	public int getI() {
		final java.lang.Object o = get();
		assert (type == Type.INT && (o instanceof Integer)) : "Не совпали типы!";
		return (int) o;
	}

	/**Возвращает текущее значение параметра
	 * @return текущее значение параметра
	 */
	public String getS() {
		final java.lang.Object o = get();
		assert (type == Type.STRING && (o instanceof String)) : "Не совпали типы!";
		return (String) o;
	}

	/**Возвращает текущее значение параметра
	 * @return текущее значение параметра
	 */
	public boolean getB() {
		final java.lang.Object o = get();
		assert (type == Type.BOOLEAN && (o instanceof Boolean)) : "Не совпали типы!";
		return (boolean) o;
	}

	/**Возвращает текущее значение параметра
	 * @return текущее значение параметра. Его тип обязан совпадать с типом параметра!
	 */
	protected abstract Object get();
	
}
