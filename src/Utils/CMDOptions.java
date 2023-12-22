/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
//Версия от 5 опреля 2023
package Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *Опции командной строки
 * 
 * @author zeus
 */
public class CMDOptions implements java.lang.Iterable{
	public static class Option{

		/**Тип опции*/
		public enum opt_type { 
			/**Заглушка, опция которой несуществует*/	_void,
			/**Число*/	_int,	//Число
			/**Логика, есть опция/нет*/	_bool,	//Логика, есть опция/нет
			/**Строка*/	_string,//Строка
			/**Символ*/	_char,	//Символ
			/**Вектор чисел из строки, разделённе особым символом*/	_v_int,	//Вектор чисел из строки, разделённе особым символом
			/**Вектор строк, разделённых  особым символом*/	_v_string,//Вектор строк, разделённых  особым символом
			/**Вектор символов из строки, разделённой  особым символом*/	_v_char,//Вектор символов из строки, разделённой  особым символом
		};
		/**Статус опции*/
		public enum state {
			/**Новая, добавленная*/	add,
			/**Обновлённая*/	update,
			/**Удалённая*/	remove,	
		};
		/**
		 * Создание числовой опции с ограничениями по значению и с определённым масштабом
		 * @param symbol символ опции
		 * @param minimum минимальное значение опции
		 * @param default_val значение по умолчанию
		 * @param maximum максимальное значение опции
		 * @param scale_ масштабируемость опции. При получении занчение опции будет умножено на масштаб
		 * @param description описание опции
		 */
		public Option(char symbol, Integer minimum, int default_val, Integer maximum, double scale_, String description){
			type = opt_type._int;
			real_value = default_val;
			def_value = default_val;
			min_val = minimum;
			max_val = maximum;
			scale = scale_;
			name = symbol;
			help = description;
		}
		/**
		 * Создание числовой опции с ограничениями по значению и с определённым масштабом
		 * @param symbol символ опции
		 * @param minimum минимальное значение опции
		 * @param default_val значение по умолчанию
		 * @param maximum максимальное значение опции
		 * @param scale_ масштабируемость опции. При получении занчение опции будет умножено на масштаб
		 */
		public Option(char symbol, Integer minimum, int default_val, Integer maximum, double scale_){
			this(symbol, minimum, default_val, maximum, scale_,null);
		}
		/**
		 * Создание числовой опции без ограничений по значениям, но с определённым масштабом
		 * @param symbol символ опции
		 * @param default_val значение по умолчанию
		 * @param scale_ масштабируемость опции. При получении занчение опции будет умножено на масштаб
		 * @param description описание опции
		 */
		public Option(char symbol, int default_val, double scale_, String description){
			this(symbol, null, default_val, null, scale_, description);
		}
		/**
		 * Создание числовой опции без ограничений по значениям, но с определённым масштабом
		 * @param symbol символ опции
		 * @param default_val значение по умолчанию
		 * @param scale_ масштабируемость опции. При получении занчение опции будет умножено на масштаб
		 */
		public Option(char symbol, int default_val, double scale_){
			this(symbol, null, default_val, null, scale_);
		}
		/**
		 * Создание числовой опции без ограничений по значениям
		 * @param symbol символ опции
		 * @param default_val значение по умолчанию
		 * @param description описание опции
		 */
		public Option(char symbol, int default_val, String description){
			this(symbol, null, default_val, null, 1.0, description);
		}
		/**
		 * Создание числовой опции без ограничений по значениям
		 * @param symbol символ опции
		 * @param default_val значение по умолчанию
		 */
		public Option(char symbol, int default_val){
			this(symbol, null, default_val, null, 1.0);
		}
		
		/**
		 * Создание логической опции - есть/нет
		 * @param symbol символ опции
		 * @param description описание опции
		 */
		public Option(char symbol, String description){
			type = opt_type._bool;
			real_value = false;
			def_value = false;
			min_val = null;
			max_val = null;
			scale = 0;
			name = symbol;
			help = description;
		}
		/**
		 * Создание логической опции - есть/нет
		 * @param symbol символ опции
		 */
		public Option(char symbol){
			this(symbol, null);
		}
		
		/**
		 * Создание строковой опции
		 * @param symbol символ опции
		 * @param default_val значение по умолчанию
		 * @param description описание опции
		 */
		public Option(char symbol, String default_val, String description){
			type = opt_type._string;
			real_value = default_val;
			def_value = default_val;
			min_val = null;
			max_val = null;
			scale = 0;
			name = symbol;
			help = description;
		}
		
		/**
		 * Создание символьной опции с ограничениями по значению
		 * @param symbol символ опции
		 * @param minimum минимальное значение опции
		 * @param default_val значение по умолчанию
		 * @param maximum максимальное значение опции
		 * @param description описание опции
		 */
		public Option(char symbol, Character minimum, char default_val, Character maximum, String description){
			type = opt_type._char;
			real_value = default_val;
			def_value = default_val;
			min_val = minimum;
			max_val = maximum;
			scale = 0;
			name = symbol;
			help = description;
		}
		/**
		 * Создание символьной опции с ограничениями по значению
		 * @param symbol символ опции
		 * @param minimum минимальное значение опции
		 * @param default_val значение по умолчанию
		 * @param maximum максимальное значение опции
		 */
		public Option(char symbol, Character minimum, char default_val, Character maximum){
			this(symbol, minimum, default_val, maximum,null);
		}
		/**
		 * Создание символьной опции
		 * @param symbol символ опции
		 * @param default_val значение по умолчанию
		 * @param description описание опции
		 */
		public Option(char symbol, char default_val, String description){
			this(symbol,null, default_val,null, description);
		}
		/**
		 * Создание символьной опции
		 * @param symbol символ опции
		 * @param default_val значение по умолчанию
		 */
		public Option(char symbol, char default_val){
			this(symbol, default_val, null);
		}
		
		/**
		 * Вмещает текст в рамку, начиная с определённой позиции
		 * @param text Текст, который надо вместить
		 * @param startPos с какой позиции рамка ничинается
		 * @param row_width сколько в рамке символов
		 * @return 
		 */
		String addText(String text, int startPos, int row_width){
			String ret = "";
			while(true){
				if(startPos + text.length()< row_width && text.indexOf('\n') == -1){
					return ret + text;
				} else {
					var whid = text.indexOf('\n');
					if(whid > (row_width - startPos) || whid == -1){
						whid = text.lastIndexOf(' ', row_width - startPos);
						if(whid == 0 || whid == -1)
							whid = row_width - startPos;
						ret += text.substring(0, whid) + "\n\t" + " ".repeat(startPos);
						text = text.substring(whid);
					} else{
						whid++;
						ret += text.substring(0, whid) + "\t" + " ".repeat(startPos);
						text = text.substring(whid);
					}
				}
			}
		}
		/**
		 * Превращает опцию в строку "\t[имя опции] _ _ : [Описание...]"
		 * Превращает опцию в строку "\t                : [...Описание...]"
		 * Превращает опцию в строку "\t                : [...Описание] _____ [значение]"
		 * @param row_width количество символов в длинну
		 * @param is_def_val значение по умлочанию или установленное?
		 * @return строка с описанием опции
		 */
		public String print(int row_width, boolean is_def_val){
			String type_s;
			switch (type) {
				case _int:
					type_s = "int";
					break;
				case _bool:
					type_s = "_";
					break;
				case _string:
					type_s = "str";
					break;
				case _char:
					type_s = "chr";
					break;
				case _v_int:
					type_s = "i,…,i";
					break;
				case _v_string:
					type_s = "s,…,s";
					break;
				case _v_char:
					type_s = "c,…,c";
					break;
				case _void:
				default:
					type_s = "?!";
			}

			//Начало опции
			String str = String.format("\t-%c%5s: ",name,type_s);
			var lenght = str.length();
			//Её описание
			String diskr = help == null ? "" : help;
			//Лимиты опции
			switch (type) {
				case _int:
					if (min_val != null && max_val != null) diskr += String.format(" %c∈[%d,%d]", name, min_val, max_val);
					else if (min_val != null) diskr += String.format(" %c≥%d", name, min_val);
					else if (max_val != null) diskr += String.format(" %c≤%d", name, max_val);
					else diskr += String.format(" %c∈R", name);
				break;
				case _char:
					if (min_val != null && max_val != null) diskr += String.format(" %c∈[%c,%c]", name, min_val, max_val);
					else if (min_val != null) diskr += String.format(" %c≥%c", name, min_val);
					else if (max_val != null) diskr += String.format(" %c≤%c", name, max_val);
					else diskr += String.format(" %c∈[A,z]", name);
					break;
				case _v_int:
					if (min_val != null && max_val != null) diskr += String.format(" …%c∈[%d,%d]", name, min_val, max_val);
					else if (min_val != null) diskr += String.format(" …%c≥%d", name, min_val);
					else if (max_val != null) diskr += String.format(" …%c≤%d", name, max_val);
					else diskr += String.format(" …%c∈R", name);
					break;
				case _v_char:
					if (min_val != null && max_val != null) diskr += String.format(" …%c∈[%c,%c]", name, min_val, max_val);
					else if (min_val != null) diskr += String.format(" …%c≥%c", name, min_val);
					else if (max_val != null) diskr += String.format(" …%c≤%c", name, max_val);
					else diskr += String.format(" …%c∈[!,~]", name);
					break;
				case _v_string:
				case _string:
				case _bool:
				case _void:
				default:
			}
			str += addText(diskr,lenght,row_width);
			//Описания параметров. Каждый параметр с новой строки
			if(description_values != null && !description_values.isEmpty()){
				int i = 0;
				String v_param;
				for(var descr : description_values){
					i++;
					switch (type) {
						case _v_int:
							v_param = String.format("i%d-%s",i,descr);
							break;
						case _v_string:
							v_param = String.format("s%d-%s",i,descr);
							break;
						case _v_char:
							v_param = String.format("c%d-%s",i,descr);
							break;
						case _int:
						case _bool:
						case _string:
						case _char:
						case _void:
						default:
							v_param = "";
							break;
					}
					str += "\n\t" + " ".repeat(lenght/2) + addText(v_param,lenght/2,row_width);
				}
			}
			int last_row_l = str.length()- str.lastIndexOf('\n');

			//А теперь значение опции
			str += "_".repeat(row_width - last_row_l);
			if(is_def_val){
				switch (type) {
					case _int:
						str += (Integer)def_value;
						break;
					case _bool:
						str += "не включено";
						break;
					case _string:
						str += (String)def_value;
						break;
					case _char:
						str += (Character)def_value;
						break;
					case _v_string:
					case _v_int:
					case _v_char:
						str += (String)def_value;
						break;
					case _void:
					default:
						break;
				}
			} else {
				switch (type) {
					case _int:
						str += (Integer) real_value;
						break;
					case _bool:
						str += ((Boolean) real_value) ? "включено" : "не включено";
						break;
					case _string:
						str += (String) real_value;
						break;
					case _char:
						str += (Character) real_value;
						break;
					case _v_int:
						str += (List<Integer>) real_value;
						break;
					case _v_string:
						str += (List<String>) real_value;
						break;
					case _v_char:
						str += (List<Character>) real_value;
						break;
					case _void:
					default:
						break;
				}
			}
			return str;
		}
		/**
		 * Проверяет, опция уже установила своё значение или всё ещё в дефаулте
		 * @return Возвращает true, если опция была обновлена в соответствии со сторокой запуска, а не преобрела своё значение по умолчанию
		 */
		public boolean isSetValue() {return isUpdateValue;}
		/**
		 * Возвращает имя опции
		 * @return символ, который мы ищем
		 */
		public char getName() {return name;}
		
		
		/**Статус опции*/
		private state _status = state.add;		//
		/**Тип опции, строковая, числовая или другая*/
		private final opt_type type;					//
		/**Фактическое значение опции*/
		private Object real_value;			//
		/**Значение по умолчанию*/
		private final Object def_value; 			//
		/**Минимальное значение числа или минимальное значение для символа.*/
		private final Object min_val; //
		/**Максимальное значение числа или максимальное значение для символа.*/
		private final Object max_val; //
		/**Масштаб для числовой оси. То есть по умолчанию опция может быть в милисекунадх, а значение нужно получить в секундах, вот для этого и нужен этот параметр*/
		private final double scale;				//
		/**Имя опции*/
		private final char name;				//
		/**Краткое описание опции*/
		private String help;				//
		/**Краткое описание каждой возможной опции для векторной велечины*/
		private List<String> description_values;	//
		/**Показывает, уже сохраненно значение опции из параметров или нет*/
		private boolean isSetVal = false;			//
		/**Показывает, что значение попытались обновить согласно данным из конмадной строки*/
		private boolean isUpdateValue = false;		//

		/**
		 * Сохраняет значение опции, базовая функция, которая автоматом выбирает один из следующих вариантов
		 * @param str_value строковое представление значения
		 */
		private void setValue(String str_value) {
			isUpdateValue = true;
			switch (type) {
				case _int:
					setInt(str_value);
					return;
				case _bool:
					real_value = true;
					return;
				case _string:
					real_value = str_value;
					return;
				case _char:
					setChar(str_value);
					return;
				case _v_int:
					if(!description_values.isEmpty() && description_values.size() != (str_value.chars().filter(c -> c == ',').count() + 1))
						System.err.printf("Опция -%c должна иметь %d значений, установлены значения по умолчанию - %s\n",name,description_values.size(),(String)def_value);
					else
						setVInt(str_value);
					return;
				case _v_string:
					if(!description_values.isEmpty() && description_values.size() != (str_value.chars().filter(c -> c == ',').count() + 1))
						System.err.printf("Опция -%c должна иметь %d значений, установлены значения по умолчанию - %s\n",name,description_values.size(),(String)def_value);
					else
						real_value = Arrays.asList(str_value.split(","));
					return;
				case _v_char:
					if(!description_values.isEmpty() && description_values.size() != (str_value.chars().filter(c -> c == ',').count() + 1))
						System.err.printf("Опция -%c должна иметь %d значений, установлены значения по умолчанию - %s\n",name,description_values.size(),(String)def_value);
					else
						setVChar(str_value);
					return;
				case _void:
				default:
					System.err.printf("Опция -%c имеет неизвестный тип %d и не может быть выведена из строки - %s\n",name,type,str_value);
			}
		}

		/**
		 * Сохраняет конкретно строку как число
		 * @param str_value строковое предстваление опции
		 */
		private void setInt(String str_value) {
			if (str_value.isEmpty()) {
				System.err.printf("Опция -%c должна иметь параметр, установлено значение по умолчанию - %d\n", name, (Integer) def_value);
				real_value = def_value;
			} else {
				try {
					Integer value_l = Integer.valueOf(str_value);
					if (min_val != null && max_val != null) {
						if ((Integer) min_val <= value_l && value_l <= (Integer) max_val) {
							real_value = value_l;
						} else {
							System.err.printf("Опция -%c равная %d находится вне диапазона [%d,%d], установлено значение по умолчанию - %d\n", name, value_l, (Integer)min_val, (Integer)max_val, (Integer) def_value);
							real_value = def_value;
						}
					} else if (min_val != null) {
						if ((Integer) min_val <= value_l) {
							real_value = value_l;
						} else {
							System.err.printf("Опция -%c равная %d должна быть НЕ МЕНЬШЕ %d, установлено значение по умолчанию - %d\n", name, value_l, (Integer)min_val, (Integer) def_value);
							real_value = def_value;
						}
					} else if (max_val != null) {
						if (value_l <= (Integer)  max_val) {
							real_value = value_l;
						} else {
							System.err.printf("Опция -%c равная %d  должна быть НЕ БОЛЬШЕ %d, установлено значение по умолчанию - %d\n", name, value_l, (Integer)max_val, (Integer) def_value);
							real_value = def_value;
						}
					} else {
						real_value = value_l;
					}
				} catch (NumberFormatException e) {
					System.err.printf("Опция -%c должна иметь ЧИСЛОВОЙ параметр вместо '%s', установлено значение по умолчанию - %d\n", name, str_value, (Integer) def_value);
					real_value = def_value;
				}
			}
		}

		/**
		 * Сохраняет конкретно строку как символ
		 * @param str_value строковое предстваление опции
		 */
		private void setChar(String str_value) {
			if (str_value.isEmpty()) {
				System.err.printf("Опция -%c должна иметь параметр, установлено значение по умолчанию - '%c'\n", name, (Character) def_value);
				real_value = def_value;
			} else if (str_value.length() > 1) {
				System.err.printf("Опция -%c должна иметь длину в один символ, вместо %s, установлено значение по умолчанию - '%c'\n", name, str_value, (Character) def_value);
				real_value = def_value;
			} else {
				char value_l = str_value.charAt(0);
				if (min_val != null && max_val != null) {
					if ((Character) min_val <= value_l && value_l <= (Character) max_val) {
						real_value = value_l;
					} else {
						System.err.printf("Опция -%c равная %c находится вне диапазона [%c,%c], установлено значение по умолчанию - %c\n", name, value_l, (Character) min_val, (Character) max_val, (Character) def_value);
						real_value = def_value;
					}
				} else if (min_val != null) {
					if ((Character) min_val <= value_l) {
						real_value = value_l;
					} else {
						System.err.printf("Опция -%c равная %c должна быть НЕ МЕНЬШЕ %c, установлено значение по умолчанию - %c\n", name, value_l, (Character) min_val, (Character) def_value);
						real_value = def_value;
					}
				} else if (max_val != null) {
					if (value_l <= (Character) max_val) {
						real_value = value_l;
					} else {
						System.err.printf("Опция -%c равная %c  должна быть НЕ БОЛЬШЕ %c, установлено значение по умолчанию - %c\n", name, value_l, (Character) max_val, (Character) def_value);
						real_value = def_value;
					}
				} else {
					real_value = value_l;
				}
			}
		}

		private void setVInt(String str_value) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		private void setVChar(String str_value) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
		
		public <T> T get(Class<T> aClass) {
			if(aClass == Boolean.class){
				if (type == opt_type._bool) return (T) real_value;
				else throw new IllegalArgumentException(String.format("Опция %c имеет тип %s, отличный от логического",name,type.toString()));
			} else if(aClass == state.class){
				return (T) _status;
			} else if(aClass == String.class){
				if (type == opt_type._string) return (T) real_value;
				else throw new IllegalArgumentException(String.format("Опция %c имеет тип %s, отличный от строчного",name,type.toString()));
			}  else if(aClass == java.lang.Integer.class){
				if (type == opt_type._int) return (T) real_value;
				else throw new IllegalArgumentException(String.format("Опция %c имеет тип %s, отличный от числового",name,type.toString()));
			} else {
				throw new IllegalArgumentException(String.format("Опция %c имеет тип %s, отличный от %s",name,type.toString(),aClass.toString()));
			}
		}
	}
	
	/**Все опции командной строки*/
	private Map<String,String> soptions; 
	/**Объекты опций*/
	private List<Option> opts; 	//Опции
	
	public CMDOptions(String[] args){
		soptions = new HashMap<>();
		var sb = new StringBuilder();
		for(var i : args)
			sb.append(i);
		for(var arg : sb.toString().split("-")){
			if(arg.isEmpty()) continue;
			if(arg.charAt(0) == '-'){//Длинный аргумент
				var iV = arg.indexOf('=');
				var key = arg.substring(1, iV);
				soptions.put(key	, arg.length() > (iV+1) ? arg.substring(iV+1) : null);
			} else {
				soptions.put(String.valueOf(arg.charAt(0))	, arg.length() > 1 ? arg.substring(1) : null);
			}
		}
		opts = new ArrayList<>();
	}

	/**
	 * Позволяет получить опции командной строки
	 * @return карта, где ключ - ключ, а значение - значение
	 */
	public Map<String,String> getCmdParams(){return soptions;}
	/**
	 * Добавляет опцию
	 * @param opt опция, которую надо добавить
	 * @throws IllegalArgumentException если такая опция уже существует
	 */
	public void add(Option opt){
		if(contain(opt.name)){
			throw new IllegalArgumentException(String.format("Добавить опцию '%c' невозможно, так как она уже находится в карте!",opt.name));
		}else{
			opt._status = Option.state.add;
			opts.add(opt);
		}
	}
	/**
	 * Удаляет опцию
	 * @param symbol опция, которую надо удалить
	 * @throws IllegalArgumentException если такая опция уже существует
	 */
	public void remove(char symbol){
		if (contain(symbol) && get(symbol)._status != Option.state.remove) {
			get(symbol)._status = Option.state.remove;
		} else {
			throw new IllegalArgumentException(String.format("Опция '%c' несуществует!", symbol));
		}
	}
	/**
	 * Обновляет существующую опцию
	 * Если у новой опции не задано описание и тот-же масштаб, то
	 * опция считается не обновлённой, так как по факту мы просто обновили минимум-максимум
	 * @param opt опция, которую надо обновить
	 * @throws IllegalArgumentException если такая опция не существует
	 */
	public void update(Option opt){
		if(!contain(opt.name) && get(opt.name)._status != Option.state.remove){
			throw new IllegalArgumentException(String.format("Опция '%c' несуществует!", opt.name));
		}else{
			var erase = opts.stream().filter(test -> (test.getName() == opt.name)).findFirst().get();
			var index = opts.indexOf(erase);
			String help = opt.help == null ? erase.help : opt.help;
			opts.set(index, opt);
			opt.help = help;
			if(opt.help != null) //Обновляется, по настоящему, опция только тогда, когда её описание меняется. Всё остальное - детали
				opt._status = Option.state.update;
		}
	}
	/**
	 * Сохраняет новое значение для опции.
	 * @param symbol опция
	 * @param val её значение
	 */
	public void setValue(char symbol, String val){
		for(var i : opts)
			i.isSetVal = false;
		soptions.put(String.valueOf(symbol), val);
	}
	/**
	 * Проверяет, существует-ли такая опция?
	 * @param symbol опция, которую ищем
	 * @return true, если такая опция нам известна
	 */
	public boolean contain(char symbol){
		return opts.stream().filter(test -> (test.getName() == symbol)).findFirst().orElse(null) != null;
	}
	/**
	 * Возвращает опцию по её символу
	 * @param symbol символ опции
	 * @return опция, которую ищем
	 * @throws IllegalArgumentException если такая опция уже существует
	 */
	public Option get(char symbol){
		var find = opts.stream().filter(test -> (test.getName() == symbol)).findFirst().orElse(null);
		if (find != null) {
			if(!find.isSetVal){
				if(soptions.containsKey(String.valueOf(symbol)))
					find.setValue(soptions.get(String.valueOf(symbol)));
				find.isSetVal = true;
			}
			return find;
		} else {
			throw new IllegalArgumentException(String.format("Опция '%c' несуществует!", symbol));
		}
	}
	/**
	 * Обновляет все значения всех опций и сохраняет их
	 */
	private void updateAll(){
		for(var i : opts){
			if(!i.isSetVal){
				if(soptions.containsKey(String.valueOf(i.name)))
					i.setValue(soptions.get(String.valueOf(i.name)));
				i.isSetVal = true;
			}
		}
	}

	@Override
	public Iterator<Option> iterator() {updateAll(); return opts.iterator();}
}
