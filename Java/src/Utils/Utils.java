package Utils;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

public class Utils {

	/**
	 * Метод получения псевдослучайного целого числа от min до max (включая max);
	 * @param min
	 * @param max
	 */
	public static int random(int min, int max) {
		max -= min;
		return (int) (Math.random() * ++max) + min;
	}

	/**
	 * Рисует круг
	 * @param g
	 * @param x
	 * @param y
	 * @param r
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
     * format used by the method {@link #getRGB() getRGB}.
     * This integer can be supplied as an argument to the
     * {@code Color} constructor that takes a single integer argument.
     * @param     h   the hue component of the color - цвет
     * @param     s   the saturation of the color - насыщенность (бледнее-ярче)
     * @param     b   the brightness of the color - яркость (от яркого до чёрного)
     * @param 	  a the alpha component
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
	 * @param sec
	 */
	public static void pause(long sec) {
		pause_ms(sec * 1_000);
	}
	/**
	 * Пауза
	 * @param msec
	 */
	public static void pause_ms(long msec) {
		pause_ns(msec * 1_000_000);
	}
	/**
	 * Пауза
	 * @param msec
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
	
	public static int betwin(int min, int val, int max) {
		if (val > max)
			return max;
		else if (val < min)
			return min;
		else
			return val;
	}
	public static double betwin(double min, double val, double max) {
		if (val > max)
			return max;
		else if (val < min)
			return min;
		else
			return val;
	}
}
