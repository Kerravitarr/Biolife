package Utils;
//Версия 2.1 от 07 августа 2023 года!



import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import Utils.JsonSave.SerializationOld;

/**
 * Класс, который отвечает за стиль JSON
 * @author Илья
 *
 */
public class JSON implements SerializationOld{
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
		public JSON_TOKEN type;
		public Object value;
		@Override
		public String toString() {return type + " " + value;}
	}
	
	/**Чтец токенов*/
	private static class TokenReader {
		/**Поток, из которого читаем*/
		Reader stream;
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
			switch (ch) { // Не пробел, а что?
				case '{' -> {return new Token(JSON_TOKEN.BEGIN_OBJECT, "{");}
				case '}' -> {return new Token(JSON_TOKEN.END_OBJECT, "}");}
				case '[' -> {return new Token(JSON_TOKEN.BEGIN_ARRAY, "[");}
				case ']' -> {return new Token(JSON_TOKEN.END_ARRAY, "]");}
				case ',' -> {return new Token(JSON_TOKEN.SEP_COMMA, ",");}
				case ':' -> {return new Token(JSON_TOKEN.SEP_COLON, ":");}
				case 'n' -> {return readNull();}
				case 't', 'f' -> {return readBoolean(ch);}
				case '"' -> {return readString();}
				//case '-' -> {return readNumber(ch);} - входит в def
				default -> {return readNumber(ch);}
			}
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
			StringBuilder sb = new StringBuilder();
			
			if(ch == '0') { 
				ch = read();
				if(ch == '.') { //Десятичное число 0.ххх
					if(isNegativ)
						return new Token(JSON_TOKEN.NUMBER, -readFracAndExp(ch));
					else
						return new Token(JSON_TOKEN.NUMBER, readFracAndExp(ch));
				}else { //Это просто нуль и ни чего более
					back();
					return new Token(JSON_TOKEN.NUMBER, 0);	
				}
			} else if (isDigit(ch)) {
				do {
					sb.append(ch);
					ch = read();
				} while (isDigit(ch));
				Long long_ = Long.parseLong(sb.toString());
				if(ch == '.') { // Если это не число, то может точка?
					Double val = long_.doubleValue() + readFracAndExp(ch);
					return new Token(JSON_TOKEN.NUMBER, isNegativ ? -val: val);
				} else {
					back();
					if(long_ < Integer.MAX_VALUE)
						return new Token(JSON_TOKEN.NUMBER, isNegativ ? -long_.intValue() : long_.intValue());
					else
						return new Token(JSON_TOKEN.NUMBER, isNegativ ? -long_ : long_);
				}
			} else {
		        throw new ParseException(pos,ERROR.UNEXPECTED_CHAR,ch);
			}
		}
		/**
		 * Вычитывает число с плавающей точкой. Но только дробную часть!
		 * @return
		 */
		private Double readFracAndExp(char ch) throws IOException, JSON.ParseException{
			StringBuilder sb = new StringBuilder();
			sb.append('0');
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
			return Long.parseLong(sb.toString());
		}
		/**
		 * Вычитывает строку из потока
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
					return new Token(JSON_TOKEN.BOOLEAN, Boolean.valueOf(true));
			} else {
				char[] buf = new char[4];
				pos += stream.read(buf);
				if (!(buf[0] == 'a' && buf[1] == 'l' && buf[2] == 's' && buf[3] == 'e'))
			        throw new ParseException(pos,ERROR.UNEXPECTED_VALUE,"f" + buf[0] + buf[1] + buf[2] + buf[3]);
				else
					return new Token(JSON_TOKEN.BOOLEAN, Boolean.valueOf(false));
			}
		}
		/**
		 * Вычитывает значение null
		 * @return токен, который вычитает - токен null
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
		private boolean isDigit(char ch) {
			return ch >= '0' && ch <= '9';
		}
		/**
		 * Проверяет, что символ относится к экспоненциальной записи числа
		 * @param ch
		 * @return
		 * @throws IOException
		 */
	    private boolean isExp(char ch) {
	        return ch == 'e' || ch == 'E';
	    }
		/**
		 * Проверяет, что символ относится к хексам
		 * @param ch
		 * @return
		 */
		private boolean isHex(char ch) {
	        return ((ch >= '0' && ch <= '9') || ('a' <= ch && ch <= 'f') || ('A' <= ch && ch <= 'F'));
		}
		/**
		 * Показывает, является-ли введённый символ пробелом (таблом, ентером и т.д.)
		 * @param ch
		 * @return
		 */
		private boolean isWhiteSpace(char ch) {
			return (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n');
		}
		/**
		 * Показывает, можно-ли ещё вычитать из буфера что либо
		 * @return
		 */
		public boolean hasNext() {
			try {
				return stream.ready();
			} catch (IOException e) {
				return false;
			}
		}
		
		/**
		 * Читает символ из потока. Запоминает прочитанный символ во временный буфер
		 * @return
		 * @throws IOException
		 */
		private char read() throws IOException {
			if(!isBack)
				lastChar = (char) stream.read();
			else
				isBack = false;
			pos++;
			return lastChar;
		}
		/**Сдвинуть курсор чтения на одну позицию назад*/
		private void back() {isBack = true;pos--;};
		
	}

	/**
	 * Разные ошибки, возникающие при парсинге файла
	 *
	 */
	public static class ParseException extends RuntimeException {
		private ERROR errorType;
		private Object unexpectedObject;
		private long position;

		public ParseException(long position, ERROR errorType, Object unexpectedObject) {
			this.position = position;
			this.errorType = errorType;
			this.unexpectedObject = unexpectedObject;
		}

		public ERROR getErrorType() {
			return errorType;
		}

		public long getPosition() {
			return position;
		}
		
		public Object getUnexpectedObject() {
			return unexpectedObject;
		}

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
	private interface JSON_par{
		public void write(Writer writer, String tabs) throws IOException;
	}

	/**Специальный класс, который хранит только значение*/
	private class JSON_O<T> implements JSON_par{
		/**Вариант, когда значение - простой тип*/
		T value_o = null;
		/**
		 * Создаёт параметр
		 * @param value - значение
		 */
		public JSON_O(T value) {
			this.value_o = value;
		}
		/**
		 * Записывает красиво форматированный объект в поток.
		 * @param writer - цель, куда записывается объект
		 * @param tabs - специальная переменная, позволяет сделать красивое форматирование.
		 * 				Если она null, то форматирования не будет
		 * @throws IOException - следует учитывать возможность выброса исключения при работе с файлом
		 */
		public void write(Writer writer, String tabs) throws IOException {
			if (value_o != null && value_o instanceof JSON) {
				if (tabs != null) {
					writer.write("\n");
					((JSON) value_o).toBeautifulJSONString(writer, tabs);
					writer.write("\n" + tabs);
				} else {
					((JSON) value_o).toBeautifulJSONString(writer, null);
				}
			} else if(value_o != null) {
				writer.write(getVal(value_o));
			} else {
				writer.write("null");
			}
		}
		private String getVal(T value_o) {
			if(value_o instanceof String) {
				var ret = value_o.toString().replace("\\", "\\\\")
						.replace("\t", "\\t")
						.replace("\b", "\\b")
						.replace("\n", "\\n")
						.replace("\r", "\\r")
						.replace("\f", "\\f")
						.replace("\"", "\\\"");
				return "\"" + ret + "\"";
			}else {
				return value_o.toString();
			}
		}
		
		public String toString() {
			return  value_o != null ? value_o.toString() : "null";
		}
	}
	/**Специальный класс, который хранит только список значений*/
	private class JSON_A<T> implements JSON_par{
		/**Массив простых типов*/
		List<T> value_mo = null;
		/**
		 * Создаёт параметр
		 * @param value - значение
		 */
		public JSON_A(List<T> value) {
			this.value_mo = value;
		}
		public JSON_A(T[] value) {
			this(Arrays.asList(value));
		}

		/**
		 * Создаёт параметр из массива примитивов
		 * Не проивзодится проверка на null,
		 * 	тот факт, что T - примитивный тип
		 * 	Всё это может вызвать ошибки!
		 * @param value - значение
		 */
		@SuppressWarnings("unchecked")
		public JSON_A(T value) {
			final int length = java.lang.reflect.Array.getLength(value);
			if(length == 0) {
				this.value_mo = new ArrayList<>();
			}else {
				final Object first = java.lang.reflect.Array.get(value, 0);
				Object[] arr = (Object[]) java.lang.reflect.Array.newInstance(first.getClass(), length);
				arr[0] = first;
				for (int i = 1; i < length; i++)
					arr[i] = java.lang.reflect.Array.get(value, i);
				this.value_mo = (List<T>) Arrays.asList(arr);
			}
		}

		/**
		 * Записывает красиво форматированный объект в поток.
		 * @param writer - цель, куда записывается объект
		 * @param tabs - специальная переменная, позволяет сделать красивое форматирование.
		 * 				Если она null, то форматирования не будет
		 * @throws IOException - следует учитывать возможность выброса исключения при работе с файлом
		 */
		public void write(Writer writer, String tabs) throws IOException {
			if(value_mo != null && !value_mo.isEmpty() && value_mo.get(0) instanceof JSON) {
				writer.write("[");
				if (tabs != null)
					writer.write("\n");
				boolean isFirst = true;
				for(T i : value_mo) {
					if(isFirst)
						isFirst = false;
					else if (tabs != null)
						writer.write(",\n");
					else
						writer.write(",");
					if (tabs != null) {
						writer.write(tabs + "\t");
						((JSON) i).toBeautifulJSONString(writer, tabs + "\t");
					} else {
						((JSON) i).toBeautifulJSONString(writer, null);
					}
				}
				if (tabs != null)
					writer.write("\n" + tabs + "]");
				else
					writer.write("]");
			} else if(value_mo != null) {
				StringBuilder vals = new StringBuilder();
				for (T i : value_mo) {
					if (!vals.isEmpty())
						vals.append(",");
					vals.append(getVal(i));
				}
				writer.write("[" + vals + "]");
			} else {
				writer.write("[]");
			}
		}
		private String getVal(T value_o) {
			if(value_o instanceof String) {
				return "\"" + value_o.toString().replaceAll("\"", "\\\\\"") + "\"";
			}else {
				return value_o.toString();
			}
		}
		
		public String toString() {
			return  value_mo != null ? value_mo.toString() : "null";
		}
	}
	
	
	/**
	 * Создаёт объект JSON в который будут заносится значения для серелизации
	 */
	public JSON(){
		parametrs = new LinkedHashMap<>();
	}
	/**Парсинг JSON строки и заполнение соответствующих объектов
	 * @throws JSON.ParseException 
	 * @throws IOException */
	public JSON(String parseStr) throws JSON.ParseException, IOException {
		this(new StringReader(parseStr));
	}
	/**Парсинг JSON потока и заполнение соответствующих объектов
	 * @throws JSON.ParseException 
	 * @throws IOException */
	public JSON(Reader parseStr) throws JSON.ParseException, IOException {
		parse(parseStr);
	}
	/**Парсинг JSON строки и заполнение соответствующих объектов
	 * @throws JSON.ParseException 
	 * @throws IOException */
	public static List<Object> JSONA(String parseStr) throws JSON.ParseException, IOException {
		return JSONA(new StringReader(parseStr));
	}
	/**Парсинг JSON строки и заполнение соответствующих объектов
	 * @throws JSON.ParseException 
	 * @throws IOException */
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
	 * Разбирает поток в формат JSON
	 * @param in
	 * @throws JSON.ParseException 
	 * @throws IOException 
	 */
	public void parse(Reader in) throws IOException, JSON.ParseException{
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
	 * @throws JSON.ParseException
	 * @throws IOException
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
		Object sample = null;
		while (reader.hasNext()) {
			Token token = reader.next();
			if ((expectToken & token.type.value) == 0)
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.value);
			switch (token.type) {
				case BEGIN_ARRAY -> {
					if(sample == null || sample instanceof List) {
						sample = parseA(reader);
						array.add(sample);
					} else {
						throw new ParseException(reader.pos, ERROR.UNKNOW, "Массив содержит значения разных типов");
					}
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case BEGIN_OBJECT -> {
					if(sample == null || sample instanceof JSON) {
						sample = parseO(reader);
						array.add(sample);
					} else {
						throw new ParseException(reader.pos, ERROR.UNKNOW, "Массив содержит значения разных типов");
					}
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
					if(sample == null || sample.getClass() == token.value.getClass()) {
						sample = token.value;
						array.add(sample);
					} else {
						throw new ParseException(reader.pos, ERROR.UNKNOW, "Массив содержит значения разных типов");
					}
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case SEP_COMMA -> expectToken = JSON_TOKEN.NULL.value | JSON_TOKEN.NUMBER.value | JSON_TOKEN.BOOLEAN.value
						| JSON_TOKEN.STRING.value | JSON_TOKEN.BEGIN_OBJECT.value | JSON_TOKEN.BEGIN_ARRAY.value; // А дальше значение ждём!
			}
		}
		throw new ParseException(reader.pos, ERROR.UNEXPECTED_EXCEPTION, "Неожиданный конец документа");
	}
	/**
	 * Добавить новую пару ключ-значение в объект.
	 * Поддерживает не только добавление простых значений,
	 * но и векторов простых типов!
	 * @param key - ключ
	 * @param value - значение
	 */
	public <T> void add(String key, T value) {
		if(value.getClass().isArray()) // Массивы
			parametrs.put(key, new JSON_A<>(value));
		else
			parametrs.put(key, new JSON_O<>(value));
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public <T> void add(String key, T[] value) {
		parametrs.put(key, new JSON_A<>(value));
	}
	/**
	 * Добавить новую пару ключ-значение в объект
	 * @param key - ключ
	 * @param value - значение
	 */
	public <T> void add(String key, List<T> value) {
		parametrs.put(key, new JSON_A<>(value));
	}
	/**
	 * Получает любые векторные значения по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getA(String key) {
		var par = parametrs.get(key);
		if (par instanceof JSON_A)
			return ((JSON_A<T>) par).value_mo;
		else
			return null;
	}
	/**
	 * Возвращает массив состоящий из лонгов
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getAL(String key) {
		var par = parametrs.get(key);
		if (par instanceof JSON_A) {
			var ret = new ArrayList<Long>(((JSON_A<Number>) par).value_mo.size());
			for(var l : ((JSON_A<Number>) par).value_mo)
				ret.add(l.longValue());
			return ret;
		} else {
			return null;
		}
	}
	/**
	 * Получает любые векторные значения по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public List<JSON> getAJ(String key) {
		return getA(key);
	}
	/**
	 * Получает значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public JSON getJ(String key) {
		return get(key);
	}
	/**
	 * Получает значение по ключу. Заглушка, потому что во
	 * 	время исполнения не определить запрашиваемый тип
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public Integer getI(String key) {
		Number val = get(key);
		if(val == null) return null;
		else if(val instanceof Double) throw new java.lang.ClassCastException("Невозможно тип Double преобразовать к Integer");
		return val.intValue();
	}
	/**
	 * Получает значение по ключу. Заглушка, потому что во
	 * 	время исполнения не определить запрашиваемый тип
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public Long getL(String key) {
		Number val = get(key);
		if(val == null) return null;
		else if(val instanceof Double) throw new java.lang.ClassCastException("Невозможно тип Double преобразовать к Integer");
		return val.longValue();
	}
	/**
	 * Получает значение по ключу. Заглушка, потому что во
	 * 	время исполнения не определить запрашиваемый тип
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public Double getD(String key) {
		Number val = get(key);
		if(val == null) return null;
		else if(val instanceof Long) throw new java.lang.ClassCastException("Невозможно тип Long преобразовать к Double");
		return val.doubleValue();
	}
	/**
	 * Получает значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		var par = parametrs.get(key);
		if (par instanceof JSON_O) {
			return ((JSON_O<T>) par).value_o;
		}else {
			return null;
		}
	}

	/**
	 * Приводит JSON объект к строке
	 * @return Одна простая и длинная строка без форматирвоания
	 */
	public String toJSONString() {
		StringWriter sw = new StringWriter();
		try {
			toJSONString(sw);
		} catch (IOException e) {} // Быть такого не может! Не должен SW давать ошибки IO
		return sw.toString();
	}
	/**
	 * Приводит JSON объект к строке - простой и длинной строке без форматирвоания.
	 * И дописывает её в конец
	 * @throws IOException 
	 */
	public void toJSONString(Writer writer) throws IOException {
		toBeautifulJSONString(writer,null);
	}
	/**
	 * Приводит JSON объект к строке
	 * @return строка, форматированная согласно правилам составления JSON объектов, с табами и подобным
	 */
	public String toBeautifulJSONString() {
		StringWriter sw = new StringWriter();
		try {
			toBeautifulJSONString(sw);
		} catch (IOException e) {}
		return sw.toString();
	}
	/**
	 * Приводит JSON объект к строке, форматированной согласно правилам составления JSON объектов, с табами и подобным.
	 * Дописывает в конец документа
	 * @throws IOException 
	 */
	public void toBeautifulJSONString(Writer writer) throws IOException {
		toBeautifulJSONString(writer,"");
		writer.flush();
	}
	/**Внутренний метод для печати объекта. Объект состоит из открывающей табы ну и дальше по тексту*/
	private void toBeautifulJSONString(Writer writer,String tabs) throws IOException {
		writer.write("{");
		if(tabs != null)
			writer.write("\n");
		boolean isFirst = true;
		for (Entry<String, JSON.JSON_par> param : parametrs.entrySet()) {
			if(isFirst)
				isFirst = false;
			else if(tabs != null)
				writer.write(",\n");
			else
				writer.write(",");
			if(tabs != null) {
				writer.write(tabs + "\t");
				writer.write("\"" + param.getKey() + "\": ");
				param.getValue().write(writer, tabs + "\t");
			} else {
				writer.write("\"" + param.getKey() + "\":");
				param.getValue().write(writer, null);
			}
		}
		if(tabs != null)
			writer.write("\n" + tabs);
		writer.write("}");
	}
	
	public String toString() {
		return toJSONString();
	}
	public boolean containsKey(String key) {
		return parametrs.containsKey(key);
	}

	/**Очищает все элементы объекта*/
	public void clear() {
		parametrs.clear();
	}
	/**Возвращает список всех ключей объекта
	 * @return список со всеми ключами
	 */
	public Set<String> getKeys(){
		return parametrs.keySet();
	}
	
	
	/**Это список всех параметров объекта. Используется лист пар потому что было важное условие - сохранить порядок данных*/
	private LinkedHashMap<String,JSON_par> parametrs;
}
