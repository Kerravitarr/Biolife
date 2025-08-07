package Utils;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Graphics;
import java.awt.Graphics2D;
/**Класс, представляющий собой объединение рисовательного объекта и цвета рисования
 * @author Kerravitarr
 */
public class ColorRec{
	/**Монотонный цвет*/
	private final Color color;
	/**Гаридаентный цвет*/
	private final Paint gradientColor;
	/**Откуда начиная рисовать в координатах экрана*/
	private final int x;
	/**Откуда начиная рисовать в координатах экрана*/
	private final int y;
	/**Ширина объекта*/
	private final int width;
	/**Высота объекта*/
	private final int height;
	/**Координаты для рисования полинома*/
	private final int xp[];
	/**Координаты для рисования полинома*/
	private final int yp[];
	
	protected ColorRec(){
		this(0,0,0,0,null);
	}
	public ColorRec(int x0, int y0, int w, int h, Color c) {
		x = x0;
		y = y0;
		width = w;
		height = h;
		color = c;
		gradientColor = null;
		xp = null;
		yp = null;
	}
	public ColorRec(int x0, int y0, int w, int h, Paint c) {
		this(createX(x0,w),createY(y0,h),c);
	}
	public ColorRec(int[] x0, int[] y0, Paint c) {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
		color = null;
		gradientColor = c;
		xp = x0;
		yp = y0;
		if(xp.length != yp.length)
			throw new IllegalArgumentException("Длина вектора х и у различны!");
	}
	public ColorRec(int[] x0, int[] y0, Color c) {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
		color = c;
		gradientColor = null;
		xp = x0;
		yp = y0;
		if(xp.length != yp.length)
			throw new IllegalArgumentException("Длина вектора х и у различны!");
	}
	/**Создаёт обычный квадрта, заданный координатами, а не размерами
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param color
	 * @return 
	 */
	public static ColorRec rectangle(int x0, int y0, int x1, int y1, Color color){
		final int xrb[] = new int[4];
		final int yrb[] = new int[4];
		xrb[0] = xrb[3] = x0;
		xrb[1] = xrb[2] = x1;
		yrb[0] = yrb[1] = y0;
		yrb[2] = yrb[3] = y1;
		return new ColorRec(xrb, yrb, color);
	}
	/**Рисует многоугольник на поле
	 * @param g холст
	 */
	public void paint(Graphics g) {
		if (g instanceof Graphics2D g2d) {
			paint(g2d);
		} else {
			g.setColor(color);
			g.fillRect(x, y, width, height);
		}
	}

	public void paint(Graphics2D g) {
		if (color != null)
			g.setColor(color);
		else
			g.setPaint(gradientColor);
		
		if (xp != null)
			g.fillPolygon(xp, yp, xp.length);
		else
			g.fillRect(x, y, width, height);
	}
	/**Позволяет нарисовать прямоугольник, вырезав из него часть
	 * @param g холст
	 * @param field поле, которое надо вырезать из заливки
	 */
	public void paint(Graphics2D g, java.awt.geom.Area field) {
		if(field == null){
			paint(g);
			return;
		}
		if (color != null)
			g.setColor(color);
		else
			g.setPaint(gradientColor);
		
		if (xp != null) {
			var poligon = new java.awt.Polygon(xp, yp, xp.length);
			var area = new java.awt.geom.Area(poligon);
			area.subtract(field);
			g.fill(area);
		} else {
			var rectangle = new java.awt.Rectangle.Double(x, y, width, height);
			var area = new java.awt.geom.Area(rectangle);
			area.subtract(field);
			g.fill(area);
		}
	}
	
	private static int[] createX(int x0, int w) {
		int[] r = { x0, x0, x0 + w, x0 + w };
		return r;
	}

	private static int[] createY(int y0, int h) {
		int[] r = { y0, y0 + h, y0 + h, y0 };
		return r;
	}
	
	@Override
	public String toString() {
		return "x0: " + x + " y0: " + y + " w: " + width + " h: " + height + " c: " + color;
	}
}
