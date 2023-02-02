package MapObjects;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import Utils.ColorRec;
import Utils.Utils;
import main.Configurations;
import main.Point;

/**
 * Солнышко, которое нас освещает
 * @author Илья
 *
 */
public class Sun {
	
	private int w;
	private int h;
	/**Цвет ясного неба*/
	private final Color SunColor = Utils.getHSBColor(180d / 360, 1, 1, 0.7);
	/**Цвет солнца*/
	private final Color MoveColor = Utils.getHSBColor(180d / 360, 1, 1, 0.01);
	/**Цвет полного мрака*/
	private final Color DirColor = Utils.getHSBColor(240d / 360, 1, 1, 0.7);
	
	public Sun(int width, int height){
		Configurations.sun = this;
		resize(width, height);
	}

	public void resize(int width, int height) {
		w = width;
		h = height;
	}

	public double getEnergy(Point pos) {
		double E = Configurations.BASE_SUN_POWER;
		if(Configurations.DIRTY_WATER != Configurations.MAP_CELLS.height)
			E -= ((double) pos.getY()) / (Configurations.MAP_CELLS.height - Configurations.DIRTY_WATER);
		double delX = Math.abs(Point.subtractionX(Configurations.SUN_POSITION, pos.getX()));
		if(delX <= Configurations.SUN_LENGHT) {
			E += Configurations.ADD_SUN_POWER * (1d - delX / Configurations.SUN_LENGHT);
		}
		return E;
	}

	/**
	 * Отрисовывает себя кому угодно
	 * @param g
	 */
	public void paint(Graphics2D g2d) {
		var minH = Point.getRy(0);
		var minW = Point.getRx(0);
		
		//Рисуем основной фон воды
		g2d.setColor(DirColor);
		g2d.fillRect(minW, minH, w - minW * 2, h - minH * 2);
		
		//Настоящее солнце
		var PS1 = Configurations.SUN_POSITION;
		// Его копия
		var PS2 = PS1 + (PS1 < Configurations.MAP_CELLS.width / 2 ? (Configurations.MAP_CELLS.width) : (-Configurations.MAP_CELLS.width));
		//Рисуем солнце
		int x[]={Point.getRx(PS1 - Configurations.SUN_LENGHT),Point.getRx(PS1),Point.getRx(PS1 + Configurations.SUN_LENGHT)};
		int y[]={Point.getRy(0),Point.getRy((Configurations.BASE_SUN_POWER + Configurations.ADD_SUN_POWER) * (Configurations.MAP_CELLS.height - Configurations.DIRTY_WATER)),Point.getRy(0)};
		GradientPaint gp = new GradientPaint(x[1], y[0], SunColor, x[1], y[1], DirColor);
		g2d.setPaint(gp);
		g2d.fillPolygon(x,y,3);
		//Рисуем его копию
		int x2[]={Point.getRx(PS2 - Configurations.SUN_LENGHT),Point.getRx(PS2),Point.getRx(PS2 + Configurations.SUN_LENGHT)};
		gp = new GradientPaint(x2[1], y[0], SunColor, x2[1], y[1], DirColor);
		g2d.setPaint(gp);
		g2d.fillPolygon(x2,y,3);
		
		
		//Рисуем основное освещение
		var maxh = Point.getRy(Configurations.BASE_SUN_POWER * (Configurations.MAP_CELLS.height - Configurations.DIRTY_WATER));
		//Освещённая часть мира
		gp = new GradientPaint(w, minH, SunColor, w, maxh, DirColor);
		g2d.setPaint(gp);
		g2d.fillRect(minW, minH, w - minW * 2, maxh - minH);
		
		
	}

	/**Шаг мира для пересчёта*/
	public void step(long step) {
		if(step % Configurations.SUN_SPEED == 0) {
			Configurations.SUN_POSITION++;
			if(Configurations.SUN_POSITION >= Configurations.MAP_CELLS.width)
				Configurations.SUN_POSITION -= Configurations.MAP_CELLS.width;
		}
	}

}
