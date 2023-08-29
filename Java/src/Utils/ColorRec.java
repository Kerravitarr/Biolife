package Utils;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class ColorRec{
	/**Монотонный цвет*/
	private final Color color;
	/**Гаридаентный цвет*/
	private final GradientPaint gradientColor;
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
	public ColorRec(int x0, int y0, int w, int h, GradientPaint c) {
		this(createX(x0,w),createY(y0,h),c);
	}
	public ColorRec(int[] x0, int[] y0, GradientPaint c) {
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
