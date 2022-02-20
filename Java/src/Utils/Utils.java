package Utils;
import java.awt.Color;
import java.awt.Graphics;

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
     * @param     h   the hue component of the color
     * @param     s   the saturation of the color
     * @param     b   the brightness of the color
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
}
