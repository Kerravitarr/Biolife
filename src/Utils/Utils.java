package Utils;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.LockSupport;
import Calculations.Configurations;
import java.util.ArrayList;

public class Utils {

	/**
	 * Метод получения псевдослучайного целого числа [min,max];
	 * @param min минимальное значение, включительно
	 * @param max максимальное значение, включительно
	 * @return [min,max]
	 */
	public static int random(int min, int max) {
		max -= min;
		return min + Configurations.rnd.nextInt(max+1);
	}
	/**Превращает число в степенную форму.
	 * (-10_000,10_000) = x
	 * (-10_000_000,10_000_000) = xk
	 * (-10_000_000_000,10_000_000_000) = xM
	 * (-10_000_000_000_000,10_000_000_000_000) = xG
	 * @param number вводимое число
	 * @return форматированное число
	 */
	public static String degree(long number){
		StringBuilder sb = new StringBuilder();
		degree(sb,number);
		return sb.toString();
	}
	/** * Превращает число в степенную форму.(-10_000,10_000) = x
		(-10_000_000,10_000_000) = xk
		(-10_000_000_000,10_000_000_000) = xM
		(-10_000_000_000_000,10_000_000_000_000) = xG
	 * @param sb объект, куда занесут число
	 * @param number вводимое число
	 */
	public static void degree(StringBuilder sb, long number){
		final var abs = Math.abs(number);
		if(abs < 1_000l){
			sb.append(Long.toString(number));
			
		} else if(abs < 10_000l) {
			sb.append(Long.toString(number / 1_000l));
			sb.append(".");
			sb.append(Long.toString((abs % 1_000) / 100));
			sb.append("k");
		}  else if(abs < 1_000_000l) {
			sb.append(Long.toString(number / 1_000));
			sb.append("k");
			
		} else if(abs < 10_000_000l) {
			sb.append(Long.toString(number/ 1_000_000l));
			sb.append(".");
			sb.append(Long.toString((abs % 1_000_000l) / 100_000));
			sb.append("M");
		}  else if(abs < 1_000_000_000l) {
			sb.append(Long.toString(number / 1_000_000l));
			sb.append("M");
			
		} else if(abs < 10_000_000_000l) {
			sb.append(Long.toString(number/ 1_000_000_000l));
			sb.append(".");
			sb.append(Long.toString((abs % 1_000_000_000l) / 100_000_000l));
			sb.append("G");
		} else {
			sb.append(Long.toString(number / 1_000_000_000l));
			sb.append("G");
		} 
	}

	/**
	 * Рисует круг
	 * @param g
	 * @param x центр круга
	 * @param y центр круга
	 * @param r радиус круга
	 */
	public static void fillCircle(Graphics g, int x, int y, int r) {
		g.fillOval(x-r/2, y-r/2, r, r);
	}

	/**
	 * Рисует круг
	 * @param g
	 * @param x
	 * @param y
	 * @param r
	 */
	public static void drawCircle(Graphics g, int x, int y, int r) {
		g.drawOval(x-r/2, y-r/2, r, r);
	}

	/**
	 * Рисует квадрат
	 * @param g
	 * @param x - центр в х
	 * @param y - центр в у
	 * @param r - диаметр вписанной окружности (длинa стороны)
	 */
	public static void fillSquare(Graphics g, int x, int y, int r) {
		g.fillRect(x-r/2, y-r/2,r, r);
	}
	
	public static void centeredText(Graphics g, int x, int y, float size, String text) {
		// Create a new font with the desired size
		Font newFont = g.getFont().deriveFont(size);
		g.setFont(newFont);
		// Find the size of string s in font f in the current Graphics context g.
		FontMetrics fm = g.getFontMetrics();
		java.awt.geom.Rectangle2D rect = fm.getStringBounds(text, g);

		int textHeight = (int) (rect.getHeight());
		int textWidth = (int) (rect.getWidth());

		// Find the top left and right corner
		int cornerX = x - (textWidth / 2);
		int cornerY = y - (textHeight / 2) + fm.getAscent();

		g.drawString(text, cornerX, cornerY); // Draw the string.
	}

	   /**
     * Converts the components of a color, as specified by the HSB
     * model, to an equivalent set of values for the default RGB model.
     * <p>
     * The {@code saturation} and {@code brightness} components
     * should be floating-point values between zero and one
     * (numbers in the range 0.0-1.0).  The {@code hue} component
     * can be any floating-point number.  The floor of this number is
     * subtracted from it to create a fraction between 0 and 1.  This
     * fractional number is then multiplied by 360 to produce the hue
     * angle in the HSB color model.
     * <p>
     * The integer that is returned by {@code HSBtoRGB} encodes the
     * value of a color in bits 0-23 of an integer value that is the same
     * format used by the method
     * This integer can be supplied as an argument to the
     * {@code Color} constructor that takes a single integer argument.
     * @param     h   цветовая составляющая цвета
     * @param     s   насыщенность цвета
     * @param     b   яркость цвета
     * @param 	  a   альфа-компонент
     * @return    the RGB value of the color with the indicated hue,
     *                            saturation, and brightness.
     * @see       java.awt.Color#getRGB()
     * @see       java.awt.Color#Color(int)
     * @see       java.awt.image.ColorModel#getRGBdefault()
     * @since     1.0
     */
	public static Color getHSBColor(double h, double s, double b, double a) {
		int alpha = ( ((int)(255 * a))<<8*3);
		int RGB = Color.HSBtoRGB((float)h, (float)s, (float)b)&(~(0xFF<<(8*3)));
		return new Color(RGB|alpha, true);
	}
	
	/**
	 * Пауза
	 * @param sec секунды
	 */
	public static void pause(long sec) {
		pause_ms(sec * 1_000);
	}
	/**
	 * Пауза
	 * @param msec милисекунды
	 */
	public static void pause_ms(long msec) {
		pause_ns(msec * 1_000_000);
	}
	/**
	 * Пауза
	 * @param nsec наносекунды
	 */
	public static void pause_ns(long nsec) {
		LockSupport.parkNanos(nsec);
	}

	/**
	 * Объединяет два массива
	 * @param <T>
	 * @param first
	 * @param second
	 * @return
	 */
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	/**Приравнивает число к интервалу [min,max]
	 * @param min минимальное значение, меньше которого выходное число точно не будет
	 * @param val число, которое нужно ограничить
	 * @param max максимальное значение, больше которого выходное число точно не будет
	 * @return 
	 */
	public static int betwin(int min, int val, int max) {
		if (val > max)
			return max;
		else if (val < min)
			return min;
		else
			return val;
	}
	/**
	 * Приравнивает число к интервалу [min,max]
	 * @param min минимальное значение, меньше которого выходное число точно не будет
	 * @param val число, которое нужно ограничить
	 * @param max максимальное значение, больше которого выходное число точно не будет
	 * @return 
	 */
	public static double betwin(double min, double val, double max) {
		if (val > max)
			return max;
		else if (val < min)
			return min;
		else
			return val;
	}
	/**
	 * Приравнивает число к интервалу [min,max]
	 * @param min минимальное значение, меньше которого выходное число точно не будет
	 * @param val число, которое нужно ограничить
	 * @param max максимальное значение, больше которого выходное число точно не будет
	 * @return 
	 */
	public static float betwin(float min, float val, float max) {
		if (val > max)
			return max;
		else if (val < min)
			return min;
		else
			return val;
	}
	
	/**
	 * Сортирует любую карту по значениям
	 * @param <K>
	 * @param <V>
	 * @param map карта, которую надо отсортировать
	 * @return List, в котором пары отсортированны по значениям в убывающем порядке
	 */
	public static <K, V extends Comparable<? super V>> List<Entry<K,V>> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		return list;
	}
	/** Создаёт новую коллекцию с дополнительным, нулевым элементом
	 * @param <T> тип коллекции
	 * @param collect сама коллекция
	 * @return новая коллекция в которой есть новый элемент - null
	 */
	public static <T> T[] addNull(T [] collect){
		return (T[]) new ArrayList<>(Arrays.asList(collect)){{add(null);}}.toArray(collect);
	}
	/**
	 * Вычисляет обратимый хэш для числа.
	 * Взято с ответов - https://stackoverflow.com/a/12996028/22441496
	 * @param x число
	 * @return обратимый хэш этого числа
	 */
	public static int hashCode(int x){
		x = ((x >>> 16) ^ x) * 0x45d9f3b;
		x = ((x >>> 16) ^ x) * 0x45d9f3b;
		x = (x >>> 16) ^ x;
		return x;
	}
	/**
	 * Вычисляет число из обратного хэша
	 * Взято с ответов - https://stackoverflow.com/a/12996028/22441496
	 * @param x хэш
	 * @return число
	 */
	public static int unhashCode(int x){
		x = ((x >>> 16) ^ x) * 0x119de1f3;
		x = ((x >>> 16) ^ x) * 0x119de1f3;
		x = (x >>> 16) ^ x;
		return x;
	}
	/**
	 * Метод получения псевдослучайного целого числа [0,max] из хэша base.
	 * Код позаимствован из jdk.internal.util.random.RandomSupport
	 * @param base число, из которого будет сгенерировано случайное число
	 * @param max максимальное значение, включительно
	 * @return [min,max]
	 */
	public static int randomByHash(int base, int max){
		final var m = max - 1;
        var r = hashCode(base);
        if ((max & m) == 0) {
            r &= m;
        } else {
            for (var u = r >>> 1;
                 u + m - (r = u % max) < 0;
                 u = hashCode(++base) >>> 1)
				{}
        }
        return r;
	}
	/**
	 * Метод получения псевдослучайного целого числа [min,max] из хэша base.
	 * Код позаимствован из jdk.internal.util.random.RandomSupport
	 * Если min > max, то генератор вернёт просто случайное число
	 * @param base число, из которого будет сгенерировано случайное число
	 * @param min минимальное значение, включительно
	 * @param max максимальное значение, включительно
	 * @return [min,max]
	 */
	public static int randomByHash(int base, int min, int max){		
        var r = hashCode(base);
		max++; //Для включения верхней границы
        if (min < max) {
            final var n = max - min;
            final var m = n - 1;
            if ((n & m) == 0) {//Для границы в степени двойки результат самый простой
                r = (r & m) + min;
            } else if (n > 0) {//Самый сложный случай, требующий уравновешинвания шансов представления всех чисел
                for (var u = r >>> 1;
                     u + m - (r = u % n) < 0;
                     u = hashCode(++base) >>> 1)
                    {}
                r += min;
            } else { //А вот этот случай я не очень понимаю. Но он есть :)
                while (r < min || r >= max) {
                    r = hashCode(++base);
                }
            }
        }
        return r;
	}
	/**
	 * Вычисляет обратимый хэш для числа.
	 * Взято с ответов - https://stackoverflow.com/a/12996028/22441496
	 * @param x число
	 * @return обратимый хэш этого числа
	 */
	public static long hashCode(long x){
		x = (x ^ (x >>> 30)) * (0xbf58476d1ce4e5b9L);
		x = (x ^ (x >>> 27)) * (0x94d049bb133111ebL);
		x = x ^ (x >>> 31);
		return x;
	}
	/**
	 * Вычисляет число из обратного хэша
	 * Взято с ответов - https://stackoverflow.com/a/12996028/22441496
	 * @param x хэш
	 * @return число
	 */
	public static long unhashCode(long x){
		x = (x ^ (x >>> 31) ^ (x >>> 62)) * (0x319642b2d24d8ec3L);
		x = (x ^ (x >>> 27) ^ (x >>> 54)) * (0x96de1b173f119089L);
		x = x ^ (x >>> 30) ^ (x >>> 60);
		return x;
	}
	/**
	 * Метод получения псевдослучайного целого числа [0,max] из хэша base.
	 * Код позаимствован из jdk.internal.util.random.RandomSupport
	 * @param base число, из которого будет сгенерировано случайное число
	 * @param max максимальное значение, включительно
	 * @return [min,max]
	 */
	public static long randomByHash(long base, long max){
		final var m = max - 1;
        var r = hashCode(base);
        if ((max & m) == 0L) {
            r &= m;
        } else {
            for (var u = r >>> 1;
                 u + m - (r = u % max) < 0L;
                 u = hashCode(++base) >>> 1)
                {}
        }
        return r;
	}
	/**
	 * Метод получения псевдослучайного целого числа [min,max] из хэша base.
	 * Код позаимствован из jdk.internal.util.random.RandomSupport
	 * Если min > max, то генератор вернёт просто случайное число
	 * @param base число, из которого будет сгенерировано случайное число
	 * @param min минимальное значение, включительно
	 * @param max максимальное значение, включительно
	 * @return [min,max]
	 */
	public static long randomByHash(long base, long min, long max){
        var r = hashCode(base);
		max++; //Для включения верхней границы
        if (min < max) {
            final var n = max - min;
            final var m = n - 1;
            if ((n & m) == 0L) {
                r = (r & m) + min;
            } else if (n > 0L) {
                /* Этот цикл принимает неприятную форму (но он работает): 
				 * поскольку первый кандидат уже доступен, нам нужна конструкция с разрывом посередине, 
				 * которая кратко, но загадочно выполняется внутри условия while цикла for без тела. */
                for (var u = r >>> 1;            // обеспечить неотрицательный
                     u + m - (r = u % n) < 0L;    // проверка отклонения
                     u = hashCode(++base) >>> 1) // повторить попытку
					{}
                r += min;
            } else { //длина диапазона не может быть представлена как long.
                while (r < min || r >= max)
                    r = hashCode(++base);
            }
        }
        return r;
	}
	
}
