package Utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class drawsUtil {
	private static final AffineTransform affinetransform = new AffineTransform();
	private static final FontRenderContext frc = new FontRenderContext(affinetransform,true,true);  
	public static enum derect {
		Up, Down, 
		/**Стрелка смотреть будет в левую сторону - <*/
		Left, 
		/**Стрелка смотреть будет в левую сторону - >*/
		Right,
	}

	public static enum alignmentX {
		/** Текст будет упираться в левую границу, то есть сдвигаться вправо. Значение по умолчанию */
		left, 
		center,
		/** Текст будет упираться в правую границу, то есть сдвигаться влево */
		right
	}

	public static enum alignmentY {
		/** Текст будет упираться в нижнюю границу. То есть всегда леэать на оси y сверху */
		top, center,
		/** Текст будет упираться в врехнюю границу. То есть всегда под осью */
		bottom
	}

	/**
	 * Рисует стрелку
	 * @param g
	 * @param x0 - положение
	 * @param y0
	 * @param d - направление
	 * @param lenght - длина усов
	 * @param angl - угол между стрелками, в градусах
	 */
	public static void arrow(Graphics g, double x0, double y0, derect d, double lenght, double angl) {
		angl = Math.toRadians(angl);
		double move = 0;
		switch (d) {
		case Up:
			move = Math.PI / 2;
			break;
		case Left:
			move = 0;
			break;
		case Down:
			move = -Math.PI / 2;
			break;
		case Right:
			move = Math.PI;
			break;
		default:
			break;
		}
		double an = move + angl;
		g.drawLine(Utils.round(x0), Utils.round(y0), Utils.round(lenght * Math.cos(an) + x0), Utils.round(lenght * Math.sin(an) + y0));
		an = move - angl;
		g.drawLine(Utils.round(x0), Utils.round(y0), Utils.round(lenght * Math.cos(an) + x0), Utils.round(lenght * Math.sin(an) + y0));
	}

	/**Рисует текст на экране
	 * @param g холст
	 * @param x положение текста
	 * @param y положение текста
	 * @param size размер текста
	 * @param text текст
	 * @param alX выравнивание по x
	 * @return прямоугольник, который очерчивает написанный текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y,String text, alignmentX alX){
		return drawString(g, x, y, text, alX, alignmentY.center);
	}
	/**Рисует текст на экране
	 * @param g холст
	 * @param x положение текста
	 * @param y положение текста
	 * @param size размер текста
	 * @param text текст
	 * @param alX выравнивание по x
	 * @return прямоугольник, который очерчивает написанный текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y,float size, String text, alignmentX alX){
		return drawString(g, x, y, size, text, alX, alignmentY.center);
	}
	/**Рисует текст на экране
	 * @param g холст
	 * @param x положение текста
	 * @param y положение текста
	 * @param size размер текста
	 * @param text текст
	 * @param alX выравнивание по x
	 * @param alY выравнивание по y
	 * @return прямоугольник, который очерчивает написанный текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y,float size, String text, alignmentX alX, alignmentY alY){
		final var of = g.getFont();
		final var newFont = of.deriveFont(size);
		g.setFont(newFont);
		final var r = drawString(g, x, y, text, alX, alY);
		g.setFont(of);
		return r;
	}
	/**Рисует текст на экране
	 * @param g холст
	 * @param x положение текста
	 * @param y положение текста
	 * @param text текст
	 * @param alX выравнивание по x
	 * @param alY выравнивание по y
	 * @return прямоугольник, который очерчивает написанный текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y, String text, alignmentX alX, alignmentY alY){
		final var fm = g.getFontMetrics();
		final var rect = fm.getStringBounds(text, g);

		final var textHeight = rect.getHeight();
		final var textWidth = rect.getWidth();
		
		final var cornerX = switch(alX){
			case left -> x;
			case center ->  x - (textWidth / 2);
			case right -> x - textWidth;
		};
		final var cornerY = switch(alY){
			case top -> y - textHeight;
			case center -> y - (textHeight / 2);
			case bottom -> y;
		} + fm.getAscent();
		g.drawString(text, Utils.round(cornerX), Utils.round(cornerY));
		return rect;
	}
	
	
	@Deprecated
	public static void centeredText(Graphics g, double x, double y, double size, String text) {
		centeredText(g, Utils.round(x), Utils.round(y), (float)size, text);
	}
	@Deprecated
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
	
	@Deprecated
	public static void leftText(Graphics g, int x, int y, float size, String text) {
		// Create a new font with the desired size
		Font newFont = g.getFont().deriveFont(size);
		g.setFont(newFont);
		// Find the size of string s in font f in the current Graphics context g.
		FontMetrics fm = g.getFontMetrics();
		java.awt.geom.Rectangle2D rect = fm.getStringBounds(text, g);

		int textHeight = (int) (rect.getHeight());
		//int textWidth = (int) (rect.getWidth());

		// Find the top left and right corner
		int cornerX = x;// - (textWidth / 2);
		int cornerY = y - (textHeight / 2) + fm.getAscent();

		g.drawString(text, cornerX, cornerY); // Draw the string.
	}
	
	@Deprecated
	public static void rightText(Graphics g, int x, int y, float size, String text) {
		// Create a new font with the desired size
		Font newFont = g.getFont().deriveFont(size);
		g.setFont(newFont);
		// Find the size of string s in font f in the current Graphics context g.
		FontMetrics fm = g.getFontMetrics();
		java.awt.geom.Rectangle2D rect = fm.getStringBounds(text, g);

		int textHeight = (int) (rect.getHeight());
		int textWidth = (int) (rect.getWidth());

		// Find the top left and right corner
		int cornerX = x - textWidth;
		int cornerY = y - (textHeight / 2) + fm.getAscent();

		g.drawString(text, cornerX, cornerY); // Draw the string.
	}
	
	public static int getTextHeight(Font font, String text) {return (int)(font.getStringBounds(text, frc).getHeight());}
	public static int getTextWidth(Font font, String text) {return (int)(font.getStringBounds(text, frc).getWidth());}
	public static int getTextWidth(Graphics g, String text) {return getTextWidth(g.getFont(), text);}
	/**Рисует окружность
	 * @param g холст
	 * @param x центр 
	 * @param y центр
	 * @param r радиус
	 */
	public static void circle(Graphics g, double x, double y, double r) {
		final var d = Utils.round(r*2);
		g.drawOval(Utils.round(x - r), Utils.round(y - r), d, d);
	}
	public static void circle(Graphics g, int x, int y, int r) {
		g.drawOval(x - r, y - r, r*2, r*2);
	}
	/**Рисует окружность
	 * @param g холст
	 * @param x центр 
	 * @param y центр
	 * @param r радиус
	 */
	public static void fillCircle(Graphics g, double x, double y, double r) {
		final var d = Utils.round(r*2);
		g.fillOval(Utils.round(x - r), Utils.round(y - r), d, d);
	}
	public static void fillCircle(Graphics g, int x, int y, int r) {
		g.fillOval(x - r, y - r, r*2, r*2);
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
}
