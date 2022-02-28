package Utils;


import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Класс, который отвечает за стиль JSON
 * @author Илья
 *
 */
public class JSONmake {
	/**
	 * Специальный класс, который хранит только ключ-значение
	 * @author Илья
	 *
	 */
	private class JSONParametr{
		/**Ключ всегда строковый*/
		String key = null;
		/**Вариант, когда значение - стока (причём число тоже нужно приводить к строке, важно, что это не другой JSON Объект и не массив*/
		String value_s = null;
		/**Массив строк*/
		String[] value_ms = null;
		/**JSON объект*/
		JSONmake value_j = null;
		/**Массив JSON объектов*/
		JSONmake[] value_js = null;
		
		/**
		 * Создаёт параметр
		 * @param key - ключ
		 * @param value - значение
		 */
		public JSONParametr(String key, String value) {
			this.key = key;
			this.value_s = value;
		}

		/**
		 * Создаёт параметр
		 * @param key - ключ
		 * @param value - значение
		 */
		public JSONParametr(String key, String[] value) {
			this.key = key;
			this.value_ms = value;
		}

		/**
		 * Создаёт параметр
		 * @param key - ключ
		 * @param value - значение
		 */
		public JSONParametr(String key, JSONmake value) {
			this.key = key;
			this.value_j = value;
		}

		/**
		 * Создаёт параметр
		 * @param key - ключ
		 * @param value - значение
		 */
		public JSONParametr(String key, JSONmake[] value) {
			this.key = key;
			this.value_js = value;
		}

		/**
		 * Серелизует JSON параметр, то есть просто переводит его в строку
		 */
		public String toString() {
			if(value_s != null) {
				return "\"" + key + "\":" + value_s;
			}else if(value_ms != null) {
				String vals = "";
				for(String i : value_ms) {
					if(!vals.isEmpty())
						vals+=",";
					vals += i;
				}
				return "\"" + key + "\":[" + vals + "]";
			}else if(value_j != null) {
				return "\"" + key + "\":" + value_j.toJSONString() + "";
			}else if(value_js != null) {
				String vals = "";
				for(JSONmake i : value_js) {
					if(!vals.isEmpty())
						vals+=",";
					vals += i.toJSONString();
				}
				return "\"" + key + "\":[" + vals + "]";
			} else {
				throw new RuntimeException("Не верный параметр");
			}
		}
		/**
		 * Записывает красиво форматированный объект в файл. 
		 * Считается, что один файл для одного объекта (максимум может быть список таких объектов)
		 * Выяснилось, что переводить объекты в строку - невероятно дорогостоящая операция
		 * Поэтому проще писать сразу в файл
		 * @param writer - цель, куда записывается объект
		 * @param tabs - специальная переменная, позволяет сделать красивое форматирование
		 * @throws IOException - следует учитывать возможность выброса исключения при работе с файлом
		 */
		public void write(Writer writer,String tabs) throws IOException {
			if(value_s != null) {
				writer.write(tabs + "\"" + key + "\":" + value_s);
			}else if(value_ms != null) {
				String vals = "";
				for(String i : value_ms) {
					if(!vals.isEmpty())
						vals+=",";
					vals += i;
				}
				writer.write(tabs + "\"" + key + "\":[" + vals + "]");
			}else if(value_j != null) {
				writer.write(tabs + "\"" + key + "\":{\n");
				value_j.writeToFormatJSONString(writer, tabs + '\t');
				writer.write("\n" + tabs + "}");
			}else if(value_js != null) {
				if(value_js.length == 0) {
					writer.write(tabs + "\"" + key + "\":[]");
				} else {
					writer.write(tabs + "\"" + key + "\":[\n");
					boolean isFirst = true;
					for(JSONmake i : value_js) {
						if(isFirst)
							isFirst = false;
						else
							writer.write("\n" + tabs + "\t},\n");
						writer.write(tabs + "\t{\n");
						i.writeToFormatJSONString(writer, tabs + "\t\t");
					}
					writer.write("\n" + tabs + "\t}\n" + tabs + "]");
				}
			} else {
				throw new RuntimeException("Не верный параметр");
			}
		}
	}

	
	/**В режиме работы на создание, это список всех параметров объекта*/
	ArrayList<JSONParametr> parametrs;
	/**В режиме парсинга из файла JSONа - это объект к которому идёт обращение*/
	JSONObject parseObj = null;
	
	/**
	 * Создаёт объект JSON в который будут заносится значения для серелизации
	 */
	public JSONmake(){
		parametrs = new ArrayList<>();
	}
	/**Парсинг JSON строки и заполнение соответствующих объектов*/
	public JSONmake(String parseStr) {
		JSONParser parser = new JSONParser();
		try {
			parseObj = (JSONObject) parser.parse(parseStr);
		} catch (ParseException e) {
			throw new RuntimeException(
					"Произошла ошибка при парсинге файла. Строка:" + parseStr
							+ " Ошибка: " + e.toString() + "(" + parseStr.substring(e.getPosition() - 5,e.getPosition()) + "→"  + parseStr.substring(e.getPosition(),e.getPosition()+1)+ "←"+ parseStr.substring(e.getPosition()+1,e.getPosition()+5)+")");
		}
	}

	/**Парсинг JSON из файла (или откуда там) и заполнение соответствующих объектов*/
	public JSONmake(Reader in) {
		JSONParser parser = new JSONParser();
		try {
			parseObj = (JSONObject) parser.parse(in);
		} catch (ParseException e) {
			throw new RuntimeException(
					"Произошла ошибка при парсинге файла.  Ошибка: " + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Создание нового JSON объекта, как потомка другого JSON объекта*/
	private JSONmake(JSONObject json) {
		parseObj = json;
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public void add(String key, String value) {
		parametrs.add(new JSONParametr(key,"\"" + value.replaceAll("\"", "\\\\\"") + "\""));
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public void add(String key, int value) {
		add(key,(long) value);
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public void add(String key, long value) {
		parametrs.add(new JSONParametr(key,value+""));
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public void add(String key, double value) {
		parametrs.add(new JSONParametr(key,value+""));
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public void add(String key, String[] value) {
		JSONParametr param = new JSONParametr(key,value);
		for(int i = 0 ; i < value.length ; i ++)
			param.value_ms[i] = "\"" + value[i].replaceAll("\"", "\\\\\"") + "\"";
		parametrs.add(param);
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public void add(String key, JSONmake value) {
		parametrs.add(new JSONParametr(key,value));
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public void add(String key, boolean value) {
		parametrs.add(new JSONParametr(key,value ? "true" : "false"));
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public void add(String key, int[] value) {
		String[] str = new String[value.length];
		for(int i = 0 ; i < value.length ; i ++)
			str[i] = value[i]+"";
		parametrs.add(new JSONParametr(key,str));
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public void add(String key, JSONmake[] value) {
		parametrs.add(new JSONParametr(key,value));
	}

	/**
	 * Обновить значение у пары ключ-значение
	 * @param key - ключ
	 * @param value - новое значение
	 */
	public void replace(String key, String value) {
		for(JSONParametr i : parametrs) {
			if(i.key.equals(key)) {
				i.value_s = "\"" + value.replaceAll("\"", "\\\\\"") + "\"";
				break;
			}
		}
	}
	
	/**
	 * Получить значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public String getS(String key) {
		return getT(key);
	}
	/**
	 * Получить значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public int getI(String key) {
		return (int) getL(key);
	}
	/**
	 * Получить значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public long getL(String key) {
		return getT(key);
	}
	/**
	 * Получить значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public double getD(String key) {
		return getT(key);
	}
	/**
	 * Получить значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public JSONmake getJ(String key) {
		if(parseObj == null)
			return null;
		else
			return new JSONmake((JSONObject) parseObj.get(key));
	}

	/**
	 * Получить значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public List<Long> getAL(String key) {
		return getAT(key);
	}

	/**
	 * Получить значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public List<JSONmake> getAJ(String key) {
		if (parseObj == null)
			return null;
		else {
			List<JSONmake> ret = new ArrayList<>();
			for (Object i : (JSONArray) parseObj.get(key))
				ret.add(new JSONmake((JSONObject)i));
			return ret;
		}
	}

	/**
	 * Спецфункция, универнсальная для получения любых значений по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> getAT(String key) {
		if (parseObj == null)
			return null;
		else {
			List<T> ret = new ArrayList<>();
			for (Object i : (JSONArray) parseObj.get(key))
				ret.add((T) i);
			return ret;
		}
	}

	/**
	 * Спецфункция, универнсальная для получения любых значений по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	@SuppressWarnings("unchecked")
	private <T> T getT(String key) {
		if(parseObj == null)
			return null;
		else
			return ((T) parseObj.get(key));
	}

	/**
	 * Приводит JSON объект к строке
	 * @return строка, форматированная согласно правилам составления JSON объектов
	 */
	public String toJSONString() {
		String jsonStr = "{";
		for(int i = 0 ; i < parametrs.size() ; i++) {
			if(i != 0) jsonStr +=",";
			jsonStr += parametrs.get(i).toString();
		}
		jsonStr += "}";
		return jsonStr;
	}

	/**
	 * Записывает JSON в поток, оформляя объект с табуляцией вместе.
	 * @param writer - поток, куда будет записан объект
	 * @throws IOException - При работе с такими потоаки всегда существует возможность выбросить исключение
	 */
	public void writeToFormatJSONString(Writer writer) throws IOException {
		writer.write("{\n");
		for(int i = 0 ; i < parametrs.size() ; i++) {
			if(i != 0) writer.write(",\n");
			parametrs.get(i).write(writer,"\t");
		}
		writer.write("\n}");
	}
	/**
	 * Записывает JSON в поток, оформляя объект с табуляцией вместе.
	 * Это специальная функция, которая нужна для ркурсивного обхода и создания правильных отступов по файлу
	 * @param writer - поток, куда будет записан объект
	 * @throws IOException - При работе с такими потоаки всегда существует возможность выбросить исключение
	 */
	private void writeToFormatJSONString(Writer writer,String tabs) throws IOException {
		for(int i = 0 ; i < parametrs.size() ; i++) {
			if(i != 0) writer.write(",\n");
			parametrs.get(i).write(writer,tabs);
		}
	}
	
	public String toString() {
		if(parseObj == null)
			return toJSONString();
		else
			return parseObj.toString();
	}
}
