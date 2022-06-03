package Utils;

import java.awt.Color;
import java.awt.Graphics;

public class ColorRec{
	public ColorRec(int x0, int y0, int w, int h,Color c) {
		x=x0;
		y=y0;
		width=w;
		height=h;
		setColor(c);
	}
	Color color;
	int x;
	int y;
	int width;
	int height;
	public void paint(Graphics g) {
		g.setColor(color);
		g.fillRect(x,y,width,height);
	}
	
	@Override
	public String toString() {
		return "x0: " + x + " y0: " + y + " w: " + width + " h: " + height + " c: " + color;
	}

	/**
	 * @param color the color to set
	 */
	public final void setColor(Color color) {
		this.color = color;
	}
}
