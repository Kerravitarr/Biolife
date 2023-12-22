package Utils;

/**Класс форматирования сообщений*/
public class MyMessageFormat{
	private final java.text.MessageFormat localFormat;

	public MyMessageFormat(String pattern) {localFormat = new java.text.MessageFormat(pattern);}
	public String format(Object ... arguments) {return localFormat.format(arguments);}
}