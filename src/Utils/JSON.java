package Utils;
//Версия 3.0 от 31 мая 2024 года!



import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;


/**
 * Класс, который отвечает за стиль JSON
 * @author Kerravitarr
 *
 */
public final class JSON{
	/**Перечисление разных состояний парсинга файла*/
	private static enum JSON_TOKEN{
		BEGIN_OBJECT("{"), END_OBJECT("}"), BEGIN_ARRAY("["), END_ARRAY("]"), NULL("null"), NUMBER("number"),
		STRING("str"), BOOLEAN("true/false"), SEP_COLON(":"), SEP_COMMA(","), END_DOCUMENT("");
		/**Описание символа*/
		@SuppressWarnings("unused")
		private String help;
		/**Номер перечисления, уникальный бит*/
		int value;
		JSON_TOKEN(String help) {this.help=help;value = 1 << this.ordinal();}
	}
	/**Возможные ошибки*/
	public enum ERROR{
		UNEXPECTED_CHAR,UNEXPECTED_TOKEN,UNEXPECTED_EXCEPTION,UNEXPECTED_VALUE,UNKNOW
	}
	/**Один прочитанный токен из потока*/
	private static class Token{
		public Token(JSON.JSON_TOKEN type, Object value) {
			this.type=type;
			this.value=value;
		}
		public final JSON_TOKEN type;
		public final Object value;
		@Override
		public String toString() {return type + " " + value;}
	}
	/**Чтец токенов*/
	private static class TokenReader {
		/**Поток, из которого читаем*/
		private final Reader stream;
		/**Текущая позиция чтения*/
		long pos = 0;
		/**Последний символ, который прочитали из потока*/
		char lastChar;
		/**Сдвинули каретку назад, то есть в следующий раз получим предыдущий символ*/
		boolean isBack = false;
		public TokenReader(Reader in) {
			stream=in;
		}
		/**
		 * Вычитывает следующий токен из входного потока
		 * @return
		 * @throws IOException
		 * @throws JSON.ParseException
		 */
		public Token next() throws IOException, JSON.ParseException {
			char ch;
			do {
				if (!stream.ready())
					return new Token(JSON_TOKEN.END_DOCUMENT, null);
				ch = read();
			} while (isWhiteSpace(ch));
			return switch (ch) { // Не пробел, а что?
				case '{' -> new Token(JSON_TOKEN.BEGIN_OBJECT, "{");
				case '}' -> new Token(JSON_TOKEN.END_OBJECT, "}");
				case '[' -> new Token(JSON_TOKEN.BEGIN_ARRAY, "[");
				case ']' -> new Token(JSON_TOKEN.END_ARRAY, "]");
				case ',' -> new Token(JSON_TOKEN.SEP_COMMA, ",");
				case ':' -> new Token(JSON_TOKEN.SEP_COLON, ":");
				case 'n' -> readNull();
				case 't', 'f' -> readBoolean(ch);
				case '"' -> readString();
				//case '-' -> readNumber(ch);// - входит в def
				default -> readNumber(ch);
			};
		}
		/**
		 * Читает число из входного потока
		 * @param ch - первое число, может быть числом, а может быть -
		 * @return
		 * @throws IOException
		 * @throws JSON.ParseException
		 */
		private Token readNumber(char ch) throws IOException, JSON.ParseException{
			boolean isNegativ = ch == '-';
			if(isNegativ) ch = read();
			
			if(ch == '0') { 
				ch = read();
				if(ch == '.') { //Десятичное число 0.ххх
					if(isNegativ)
						return new Token(JSON_TOKEN.NUMBER, -readFracAndExp(new StringBuilder("0"),ch));
					else
						return new Token(JSON_TOKEN.NUMBER, readFracAndExp(new StringBuilder("0"),ch));
				}else { //Это просто нуль и ни чего более
					back();
					return new Token(JSON_TOKEN.NUMBER, 0);	
				}
			} else if (isDigit(ch)) {
				var sb = new StringBuilder();
				do {
					sb.append(ch);
					ch = read();
				} while (isDigit(ch));
				if(ch == '.') { // Если это не число, то может точка?
					double val = readFracAndExp(sb,ch);
					return new Token(JSON_TOKEN.NUMBER, isNegativ ? -val: val);
				} else {
					back();
					Long long_ = Long.valueOf(sb.toString());
					if(long_ < Integer.MAX_VALUE)
						return new Token(JSON_TOKEN.NUMBER, isNegativ ? -long_.intValue() : long_.intValue());
					else
						return new Token(JSON_TOKEN.NUMBER, isNegativ ? -long_ : long_);
				}
			} else if(ch == 'N') { //NaN
				if((ch = read()) == 'a' && (ch = read()) == 'N') {
					return new Token(JSON_TOKEN.NUMBER, Double.NaN);
				} else {
					throw new ParseException(pos,ERROR.UNEXPECTED_CHAR,ch);
				}
			} else if(ch == 'I') { //Infinity
				for(var c : "nfinity".toCharArray()){
					if((ch = read()) != c){
						throw new ParseException(pos,ERROR.UNEXPECTED_CHAR,ch);
					}
				}
				return new Token(JSON_TOKEN.NUMBER, isNegativ ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			} else {
		        throw new ParseException(pos,ERROR.UNEXPECTED_CHAR,ch);
			}
		}
		/** Вычитывает число с плавающей точкой. Но только дробную часть!
		 * @param sb буффер, в котором содержится первая часть числа без точки
		 * @return
		 */
		private double readFracAndExp(StringBuilder sb, char ch) throws IOException, JSON.ParseException{
			if (ch == '.') {
				sb.append(ch);
				ch = read();
				if (!isDigit(ch))
			        throw new ParseException(pos,ERROR.UNEXPECTED_CHAR,ch);
				do {
					sb.append(ch);
					ch = read();
				} while (isDigit(ch));

				if (isExp(ch)) { // А вдруг это экспонента?
					sb.append(ch);
					sb.append(readExp().toString());
				} else {
					back(); // А мы хз что это, не к нам
				}
			} else {
		        throw new ParseException(pos,ERROR.UNEXPECTED_CHAR,ch);
			}
			return Double.parseDouble(sb.toString());
		}		
		/**
		 * Читает из потока экспоненту
		 * @return Число, представляющще собой экспоненту
		 * @throws IOException
		 * @throws JSON.ParseException
		 */
		private Long readExp() throws IOException, JSON.ParseException {
			StringBuilder sb = new StringBuilder();
			char ch = read();
			if (ch == '+' || ch == '-') {
				sb.append(ch);
				ch = read();
				if (isDigit(ch)) {
					do {
						sb.append(ch);
						ch = read();
					} while (isDigit(ch));
					back(); //Это не число, дальше мы всё
				} else {
			        throw new ParseException(pos,ERROR.UNEXPECTED_CHAR,ch);
				}
			} else if(isDigit(ch)) {
				do {
					sb.append(ch);
					ch = read();
				} while (isDigit(ch));
				back(); //Это не число, дальше мы всё
			} else {
		        throw new ParseException(pos,ERROR.UNEXPECTED_CHAR,ch);
			}
			return Long.valueOf(sb.toString());
		}
		/** Вычитывает строку из потока
		 * @return
		 * @throws IOException
		 * @throws JSON.ParseException
		 */
		private Token readString() throws IOException, JSON.ParseException {
			StringBuilder sb = new StringBuilder();
			while (true) {
				char ch = read();
				switch (ch) {
					case '\\' -> {
						 switch (read()) {
							case '"' -> sb.append('\"');
							case '\\' -> sb.append('\\');
							case '/' -> sb.append('/');
							case 'b' -> sb.append('\b');
							case 'f' -> sb.append('\f');
							case 'n' -> sb.append('\n');
							case 'r' -> sb.append('\r');
							case 't' -> sb.append('\t');
							case 'u' -> {
								StringBuilder unix = new StringBuilder();
								for (int i = 0; i < 4; i++) {
									ch = read();
									if (isHex(ch))
										unix.append(ch);
									else
										throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
								}
								sb.append(Character.toChars(
										Integer.parseInt(unix.toString(), 16)));
							}
							default -> throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
						}
					}
					case '"' -> {return new Token(JSON_TOKEN.STRING, sb.toString());}
					case '\r', '\n' -> throw new ParseException(pos,ERROR.UNEXPECTED_CHAR,ch); //Не может быть тут таких символов
					default -> sb.append(ch);
				}
			}
		}
		/**
		 * Вычитывает значение true/false
		 * @param ch - первый символ слова true или false
		 * @return токен, который вычитает - Boolean
		 * @throws IOException
		 * @throws JSON.ParseException
		 */
		private Token readBoolean(char ch) throws IOException, JSON.ParseException {
			if (ch == 't') {
				char[] buf = new char[3];
				pos += stream.read(buf);
				if (!(buf[0] == 'r' && buf[1] == 'u' && buf[2] == 'e'))
			        throw new ParseException(pos,ERROR.UNEXPECTED_VALUE,"t" + buf[0] + buf[1] + buf[2]);
				else
					return new Token(JSON_TOKEN.BOOLEAN, true);
			} else {
				char[] buf = new char[4];
				pos += stream.read(buf);
				if (!(buf[0] == 'a' && buf[1] == 'l' && buf[2] == 's' && buf[3] == 'e'))
			        throw new ParseException(pos,ERROR.UNEXPECTED_VALUE,"f" + buf[0] + buf[1] + buf[2] + buf[3]);
				else
					return new Token(JSON_TOKEN.BOOLEAN, false);
			}
		}
		/**
		 * Вычитывает значение null
		 * @return токен, который вычитывает - токен null
		 * @throws IOException
		 * @throws JSON.ParseException
		 */
		private Token readNull() throws IOException, JSON.ParseException {
			char[] buf = new char[3];
			pos += stream.read(buf);
			if (!(buf[0] == 'u' && buf[1] == 'l' && buf[2] == 'l'))
		        throw new ParseException(pos,ERROR.UNEXPECTED_VALUE,"n" + buf[0] + buf[1] + buf[2]);
			else
				return new Token(JSON_TOKEN.NULL, "null");
		}
		/**Првоеряет, что символ является числом*/
		private boolean isDigit(char ch) {return ch >= '0' && ch <= '9';}
		/** Проверяет, что символ относится к экспоненциальной записи числа */
	    private boolean isExp(char ch) {return ch == 'e' || ch == 'E';}
		/**Проверяет, что символ относится к хексам*/
		private boolean isHex(char ch) {return ((ch >= '0' && ch <= '9') || ('a' <= ch && ch <= 'f') || ('A' <= ch && ch <= 'F'));}
		/**Показывает, является-ли введённый символ пробелом (таблом, ентером и т.д.)*/
		private boolean isWhiteSpace(char ch) {return (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n');}
		/**Показывает, можно-ли ещё вычитать из буфера что либо*/
		public boolean hasNext() {
			try {
				return stream.ready();
			} catch (IOException e) {
				return false;
			}
		}
		/**Читает символ из потока. Запоминает прочитанный символ во временный буфер*/
		private char read() throws IOException {
			if(!isBack) lastChar = (char) stream.read();
			else isBack = false;
			pos++;
			return lastChar;
		}
		/**Сдвинуть курсор чтения на одну позицию назад*/
		private void back() {isBack = true;pos--;};
	}
	/**Разные ошибки, возникающие при парсинге файла*/
	public static class ParseException extends RuntimeException {
		/**Что за ошибка?*/
		private ERROR errorType;
		/**Какой объект мы не ожидали?*/
		private Object unexpectedObject;
		/**Какая позиция в тексте*/
		private long position;
		
		public ParseException(long position, ERROR errorType, Object unexpectedObject) {
			this.position = position;
			this.errorType = errorType;
			this.unexpectedObject = unexpectedObject;
		}

		public ERROR getErrorType() {return errorType;}
		public long getPosition() {return position;}
		public Object getUnexpectedObject() {return unexpectedObject;}

		@Override
		public String getMessage() {
			StringBuilder sb = new StringBuilder();
			switch (errorType) {
				case UNEXPECTED_CHAR -> sb.append("Неожиданный символ '").append(unexpectedObject).append("' в позиции ").append(position).append(".");
				case UNEXPECTED_TOKEN -> sb.append("Неожиданный токен '").append(unexpectedObject).append("' в позиции ").append(position).append(".");
				case UNEXPECTED_EXCEPTION -> sb.append("Unexpected exception at position ").append(position).append(": ").append(unexpectedObject);
				case UNEXPECTED_VALUE -> sb.append("Неожиданное значение в позиции ").append(position).append(": ").append(unexpectedObject);
				default -> sb.append("Неизвестная ошибка в позиции ").append(position).append(":").append(unexpectedObject).append(".");
			}
			return sb.toString();
		}
	}	
	/**Интерфейс для любого параметра JSON*/
	private class Serializer{
		/**
		 * Записывает красиво форматированный объект в поток.
		 * @param value_o объект, который надо записать
		 * @param writer - цель, куда записывается объект
		 * @param tabs - специальная переменная, позволяет сделать красивое форматирование.
		 * 				Если она null, то форматирования не будет
		 * @throws IOException - следует учитывать возможность выброса исключения при работе с файлом
		 */
		public static <T> void write(T value_o, Writer writer, String tabs) throws IOException {
			if(value_o == null)
				writer.write("null");
			else if(value_o instanceof String s) {
				final var sb = new StringBuffer();
				sb.append("\"");
				final int len = s.length();
				for(int i=0;i<len;i++){
					char ch=s.charAt(i);
					switch(ch){
					case '"' -> sb.append("\\\"");
					case '\\' -> sb.append("\\\\");
					case '\b' -> sb.append("\\b");
					case '\f' -> sb.append("\\f");
					case '\n' -> sb.append("\\n");
					case '\r' -> sb.append("\\r");
					case '\t' -> sb.append("\\t");
					case '/' -> sb.append("\\/");
					default -> {
							//Reference: http://www.unicode.org/versions/Unicode5.1.0/
							if((ch>='\u0000' && ch<='\u001F') || (ch>='\u007F' && ch<='\u009F') || (ch>='\u2000' && ch<='\u20FF')){
								String ss=Integer.toHexString(ch);
								sb.append("\\u");
								for(int k=0;k<4-ss.length();k++){
									sb.append('0');
								}
								sb.append(ss.toUpperCase());
							} else{
								sb.append(ch);
							}
						}
					}
				}//for	
				sb.append("\"");			
				writer.write(sb.toString());
			} else if(value_o instanceof Enum e) {
				writer.write("\"" + e.name() + "\"");
			} else if(value_o.getClass().isPrimitive() || value_o instanceof Number || value_o instanceof Boolean) {
				writer.write(String.valueOf(value_o));
			} else if(value_o instanceof JSON json) {
				if (tabs != null) {
					json.toBeautifulJSONString(writer, tabs);
				} else {
					json.toBeautifulJSONString(writer, null);
				}
			} else if(value_o instanceof List list) {
				if(list.isEmpty()){
					writer.write("[]");
				} else {
					var fcl = list.get(0).getClass();
					if(!isBase(list.get(0)) || list.stream().filter(v -> !v.getClass().equals(fcl)).findAny().isPresent()){
						//У нас сложные или разноплановые объекты
						writer.write("[");
						if (tabs != null) writer.write("\n");
						boolean isFirst = true;
						for(var value : list) {
							if(isFirst) isFirst = false;
							else if (tabs != null) writer.write(",\n");
							else writer.write(",");

							if (tabs != null) {
								writer.write(tabs + "\t");
								write(value, writer, tabs + "\t");
							} else {
								write(value, writer, null);
							}
						}
						if (tabs != null) writer.write("\n" + tabs + "]");
						else writer.write("]");
					} else {
						//У нас однородные примитивы
						writer.write("[");
						boolean isFirst = true;
						for (var value : list) {
							if(isFirst) isFirst = false;
							else writer.write(",");
							write(value, writer, null);
						}
						writer.write("]");
					}
				}
			} else {
				throw new IllegalArgumentException("Невозможно вывести объект типа" + value_o.getClass() + " -> " + value_o);
			}
		}
		/**Проверяет входящий тип на допустимость
		 * @param <T>
		 * @param value_o 
		 * @throws IllegalArgumentException если такой тип недопустим
		 */
		public static <T> Object box(T value_o){
			if(isBase(value_o)
					|| value_o instanceof JSON){
				return value_o;
			}
			else if(value_o instanceof List list) {
				return list.stream().map(value -> box(value)).toList();
			} else if(value_o.getClass().isArray()){
				final int length = java.lang.reflect.Array.getLength(value_o);
				return java.util.stream.IntStream.range(0,length).boxed().map(i -> box(java.lang.reflect.Array.get(value_o, i))).toList();
			} else {
				throw new IllegalArgumentException("Невозможно превести к JSON объект типа [" + value_o.getClass() + "] = " + value_o);
			}
		}
		/**Осуществляет преобразование из класса в класс
		 * @param <T> итоговый класс
		 * @param cls класс, который описывает то, к чему мы стремимся
		 * @param o входной объект
		 * @return объект, нужного типа
		 * @throws ClassCastException когда не смогли преобразовать один тип к другому
		 */
		public static <T> List<T> unboxl(Class<T> cls, Object o) throws ClassCastException{
			if(o == null) return null;
			else if(o instanceof List list) return (List<T>) list.stream().map(v -> unbox(cls,v)).toList();
			else throw new IllegalArgumentException("Нельзя преобразовать значение к массиву");
		}
		/**Осуществляет преобразование из класса в класс
		 * @param <T> итоговый класс
		 * @param cls класс, который описывает то, к чему мы стремимся
		 * @return объект, нужного типа
		 * @throws ClassCastException когда не смогли преобразовать один тип к другому
		 */
		public static <T> T unbox(Class<T> cls, Object o) throws ClassCastException{
			if (o == null) {
				return null;
			} else if(cls.isAssignableFrom(o.getClass())){
				return (T) o;
			} else if(cls.isEnum() && o instanceof String rets){
				return (T) (Enum.valueOf((Class<Enum>) cls, rets));
			} else if(cls.equals(Byte.class) || cls.equals(byte.class)){
				return (T) Byte.valueOf(((Number)o).byteValue());
			} else if(cls.equals(Double.class) || cls.equals(double.class)){
				return (T) Double.valueOf(((Number)o).doubleValue());
			} else if(cls.equals(Float.class) || cls.equals(float.class)){
				return (T) Float.valueOf(((Number)o).floatValue());
			} else if(cls.equals(Integer.class) || cls.equals(int.class)){
				return (T) Integer.valueOf(((Number)o).intValue());
			} else if(cls.equals(Long.class) || cls.equals(long.class)){
				return (T) Long.valueOf(((Number)o).longValue());
			} else if(cls.equals(Short.class) || cls.equals(short.class)){
				return (T) Short.valueOf(((Number)o).shortValue());
			} else if(cls.equals(Boolean.class) || cls.equals(boolean.class)){
				return (T) Boolean.valueOf(((Boolean)o).booleanValue());
			} else {
				throw new ClassCastException("Невозможно привести " + o.getClass() + " к " + cls);
			}
		}
		private static <T> boolean isBase(T value_o){
			return value_o == null 
					|| value_o instanceof String
					|| value_o instanceof Enum
					|| value_o.getClass().isPrimitive() 
					|| value_o instanceof Number 
					|| value_o instanceof Boolean;
		}
	}	
	/** Создаёт пустой объект JSON */
	public JSON(){
		parametrs = new LinkedHashMap<>();
	}
	/**Парсинг JSON строки
	 * @param parseStr строка, которую разбираем
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 */
	public JSON(String parseStr) throws JSON.ParseException {
		try {
			parse(new StringReader(parseStr));
		} catch (IOException ex) { //Быть не может! Стркоу нельзя так прочитать!
			throw new RuntimeException(ex);
		}
	}
	/**Парсинг JSON потока
	 * @param in поток чтения
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 */
	public JSON(Reader in) throws JSON.ParseException, IOException {
		parse(in);
	}
	/**Парсинг JSON строки в массив
	 * @param <T>
	 * @param cls класс, который мы хотим получить
	 * @param parseStr строка, которую разбираем
	 * @return массив разобранных объектов. 
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public static <T> List<T> parse(Class<T> cls,String parseStr) throws JSON.ParseException {
		try {
			return parse(cls, new StringReader(parseStr));
		} catch (IOException ex) { //Быть не может! Стркоу нельзя так прочитать!
			throw new RuntimeException(ex);
		}
	}
	/**Парсинг JSON строки в массив
	 * @param <T>
	 * @param cls класс, который мы хотим получить
	 * @param in поток чтения
	 * @return массив разобранных объектов. 
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public static <T> List<T> parse(Class<T> cls,Reader in) throws JSON.ParseException, IOException {
		TokenReader reader = new TokenReader(in);
		if(!reader.hasNext()) { // Пустой файл
			return new ArrayList<>();
		} else {
			Token token = reader.next();
			if(token.type == JSON_TOKEN.BEGIN_ARRAY)
				return Serializer.unboxl(cls, parseA(reader));
			else
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.value);
		}
	}
	
	/** Добавить новую пару ключ-значение в объект
	 * @param <T>
	 * @param key ключ
	 * @param value значение
	 * @return текущий объект для возможности создания цепочек
	 */
	public <T> JSON add(String key, T value) {
		parametrs.put(key, Serializer.box(value));
		return this;
	}
	/**Получает значение по ключу
	 * @param <T>
	 * @param cls ожидаемый класс
	 * @param key ключ
	 * @return значение, или null, если значение не найдено
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public <T> T get(Class<T> cls, String key) throws IllegalArgumentException {
		return Serializer.unbox(cls,parametrs.get(key));
	}
	/**Получает значение массива по ключу
	 * @param <T>
	 * @param cls - ожидаемый класс элементов
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 * @throws IllegalArgumentException возникает, если элемент представляет единственное значение и вернуть как массив его нельзя
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public <T> List<T> getA(Class<T> cls, String key) {
		return Serializer.unboxl(cls,parametrs.get(key));
	}
	/**Получает массив JSON по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 * @throws IllegalArgumentException возникает, если массив не состоит только из JSON
	 */
	public List<JSON> getAJ(String key) {
		return getA(JSON.class, key);
	}
	/**Получает значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 * @throws IllegalArgumentException возникает, если элемент не состоит из JSON
	 */
	public JSON getJ(String key) {
		return get(JSON.class, key);
	}
	/**Приводит JSON объект к строке
	 * @return Одна простая и длинная строка без форматирвоания
	 */
	public String toJSONString() {
		try {
			StringWriter sw = new StringWriter();
			toJSONString(sw);		
			return sw.toString();
		} catch (IOException e) {throw new RuntimeException(e);} // Быть такого не может! Не должен SW давать ошибки IO
	}
	/** Приводит JSON объект к строке - простой и длинной строке без форматирвоания.И дописывает её в конец
	 * @param writer
	 * @throws IOException 
	 */
	public void toJSONString(Writer writer) throws IOException {
		toBeautifulJSONString(writer,null);
		writer.flush();
	}
	/** Приводит JSON объект к строке
	 * @return строка, форматированная согласно правилам составления JSON объектов, с табами и подобным
	 */
	public String toBeautifulJSONString() {
		try {
			StringWriter sw = new StringWriter();
			toBeautifulJSONString(sw);
			return sw.toString();
		} catch (IOException e) {throw new RuntimeException(e);} // Быть такого не может! Не должен SW давать ошибки IO
	}
	/** Приводит JSON объект к строке, форматированной согласно правилам составления JSON объектов, с табами и подобным.Дописывает в конец документа
	 * @param writer
	 * @throws IOException 
	 */
	public void toBeautifulJSONString(Writer writer) throws IOException {
		toBeautifulJSONString(writer,"");
		writer.flush();
	}
	/**Проверяет наличие ключа в объекте
	 * @param key ключ
	 * @return true, если ключ тут есть
	 */
	public boolean containsKey(String key) { return parametrs.containsKey(key);}

	/**Очищает все элементы объекта*/
	public void clear() { parametrs.clear(); }
	/**Возвращает список всех ключей объекта
	 * @return список со всеми ключами
	 */
	public Set<String> getKeys(){ return parametrs.keySet(); }
	@Override
	public String toString() { return toJSONString(); }
	
	
	
	
	/**Парсинг JSON строки
	 * @param parseStr строка, которую разбираем
	 * @return массив разобранных объектов. 
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 */
	@Deprecated
	public static List<Object> JSONA(String parseStr) throws JSON.ParseException {
		try {
			return JSONA(new StringReader(parseStr));
		} catch (IOException ex) { //Быть не может! Стркоу нельзя так прочитать!
			throw new RuntimeException(ex);
		}
	}
	/**Парсинг JSON строки и заполнение соответствующих объектов
	 * @param in поток чтения
	 * @return массив разобранных объектов. 
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 */
	@Deprecated
	public static List<Object> JSONA(Reader in) throws JSON.ParseException, IOException {
		TokenReader reader = new TokenReader(in);
		if(!reader.hasNext()) { // Пустой файл
			return new ArrayList<>();
		}else {
			Token token = reader.next();
			if(token.type == JSON_TOKEN.BEGIN_ARRAY)
				return parseA(reader);
			else
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.value);
		}
	}
	
	/**
	 * Возвращает массив состоящий из лонгов
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	@Deprecated
	public List<Long> getAL(String key) {
		return getA(Long.class, key);
	}
	/**
	 * Получает значение по ключу. Заглушка, потому что во
	 * 	время исполнения не определить запрашиваемый тип
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	@Deprecated
	public int getI(String key) {
		return get(int.class, key);
	}
	/**
	 * Получает значение по ключу. Заглушка, потому что во
	 * 	время исполнения не определить запрашиваемый тип
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	@Deprecated
	public long getL(String key) {
		return get(long.class, key);
	}
	/**
	 * Получает значение по ключу. Заглушка, потому что во
	 * 	время исполнения не определить запрашиваемый тип
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	@Deprecated
	public double getD(String key) {
		return get(double.class, key);
	}
	/**
	 * Получает значение по ключу
	 * @param <T>
	 * @param key - ключ
	 * @deprecated теперь надо пользоваться методами с указанием класса объекта
	 * @return - значение, или null, если значение не найдено или значение не является единственным, а, например, это массив
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public <T> T get(String key) {
		return (T) parametrs.get(key);
	}
	/**
	 * Получает любые векторные значения по ключу
	 * @param <T>
	 * @param key - ключ
	 * @deprecated теперь надо пользоваться фукнцией с указанием класса объектов
	 * @return - значение, или null, если значение не найдено
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public <T> List<T> getA(String key) {
		return (List<T>) parametrs.get(key);
	}

	/**Внутренний метод для печати объекта. Объект состоит из открывающей табы ну и дальше по тексту*/
	private void toBeautifulJSONString(Writer writer,String tabs) throws IOException {
		writer.write("{");
		if(tabs != null)
			writer.write("\n");
		boolean isFirst = true;
		for (var param : parametrs.entrySet()) {
			if(isFirst) isFirst = false;
			else if(tabs != null) writer.write(",\n");
			else writer.write(",");
			if(tabs != null) {
				writer.write(tabs + "\t");
				writer.write("\"" + param.getKey() + "\": ");
				Serializer.write(param.getValue(), writer, tabs + "\t");
			} else {
				writer.write("\"" + param.getKey() + "\":");
				Serializer.write(param.getValue(), writer, null);
			}
		}
		if(tabs != null)
			writer.write("\n" + tabs);
		writer.write("}");
	}
	
	/**
	 * Разбирает поток в формат JSON
	 * @param in поток чтения
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 */
	private void parse(Reader in) throws IOException, JSON.ParseException{
		TokenReader reader = new TokenReader(in);
		if(!reader.hasNext()) { // Пустой файл
			parametrs.clear();
		}else {
			Token token = reader.next();
			if(token.type == JSON_TOKEN.BEGIN_OBJECT)
				parametrs = parseO(reader).parametrs;
			else
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.value);
		}
	}
	/**
	 * Парсит объект JSON, первый символ { уже получили
	 * @param reader
	 * @return
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 */
	private static JSON parseO(JSON.TokenReader reader) throws JSON.ParseException, IOException {
		JSON json = new JSON();
		int expectToken = JSON_TOKEN.STRING.value | JSON_TOKEN.END_OBJECT.value; // Ключ или конец объекта
		String key = null;
		JSON_TOKEN lastToken = JSON_TOKEN.BEGIN_OBJECT;
		while (reader.hasNext()) {
			Token token = reader.next();
			if ((expectToken & token.type.value) == 0)
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.type);
			switch (token.type) {
				case BEGIN_ARRAY -> {
					json.add(key, parseA(reader));
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
				}
				case BEGIN_OBJECT -> {
					json.add(key, parseO(reader));
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
				}
				case END_ARRAY -> {
				}
				case END_DOCUMENT -> // Этого мы ни когда не ждём!
					throw new ParseException(reader.pos, ERROR.UNEXPECTED_EXCEPTION, "Неожиданный конец документа");
				case END_OBJECT -> {
					return json; //Мы всё!
				}
				case NULL -> {
					json.add(key, (Object) null);
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
				}
				case BOOLEAN, NUMBER -> {
					json.add(key, token.value);
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
				}
				case SEP_COLON -> expectToken = JSON_TOKEN.NULL.value | JSON_TOKEN.NUMBER.value | JSON_TOKEN.BOOLEAN.value
						| JSON_TOKEN.STRING.value | JSON_TOKEN.BEGIN_OBJECT.value | JSON_TOKEN.BEGIN_ARRAY.value; // А дальше значение ждём!
				case SEP_COMMA -> expectToken = JSON_TOKEN.STRING.value; // Теперь снова ключ
				case STRING -> {
					if(lastToken == JSON_TOKEN.SEP_COLON) { // Если у нас было :, то мы просто значение 
						json.add(key, token.value);
						expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
					} else { //А раз нет - то мы ключ
						key = (String) token.value;
						expectToken = JSON_TOKEN.SEP_COLON.value; // А дальше значение ждём!
					}
				}
			}
			lastToken = token.type;
		}
		throw new ParseException(reader.pos, ERROR.UNEXPECTED_EXCEPTION, "Неожиданный конец документа");
	}
	/**
	 * Парсит массив JSON, первый символ [ уже получили
	 * @param reader
	 * @return
	 * @throws JSON.ParseException
	 * @throws IOException
	 */
	private static List<Object> parseA(JSON.TokenReader reader) throws JSON.ParseException, IOException {
		List<Object> array = new ArrayList<>();
		int expectToken = JSON_TOKEN.BEGIN_ARRAY.value | JSON_TOKEN.END_ARRAY.value | JSON_TOKEN.BEGIN_OBJECT.value
				 | JSON_TOKEN.NUMBER.value | JSON_TOKEN.BOOLEAN.value | JSON_TOKEN.STRING.value | JSON_TOKEN.NULL.value; // Массив чего у нас там?
		while (reader.hasNext()) {
			Token token = reader.next();
			if ((expectToken & token.type.value) == 0)
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.value);
			switch (token.type) {
				case BEGIN_ARRAY -> {
					array.add(parseA(reader));
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case BEGIN_OBJECT -> {
					array.add(parseO(reader));
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case END_ARRAY -> {
					return array;
				}
				case END_OBJECT, SEP_COLON, END_DOCUMENT -> // Этого мы ни когда не ждём!
					throw new ParseException(reader.pos, ERROR.UNKNOW, "Ошибка библиотеки");
				case NULL -> {
					array.add(null);
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case BOOLEAN, NUMBER, STRING -> {
					array.add(token.value);
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case SEP_COMMA -> expectToken = JSON_TOKEN.NULL.value | JSON_TOKEN.NUMBER.value | JSON_TOKEN.BOOLEAN.value
						| JSON_TOKEN.STRING.value | JSON_TOKEN.BEGIN_OBJECT.value | JSON_TOKEN.BEGIN_ARRAY.value; // А дальше значение ждём!
			}
		}
		throw new ParseException(reader.pos, ERROR.UNEXPECTED_EXCEPTION, "Неожиданный конец документа");
	}
	
	
	
	/**Это список всех параметров объекта. Используется лист пар потому что было важное условие - сохранить порядок данных*/
	private LinkedHashMap<String,Object> parametrs;
}
