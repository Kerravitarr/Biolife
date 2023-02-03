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
	/**Цвет солнца*/
	private final Color sunColor = Utils.getHSBColor(180d / 360, 1, 1, 0.7);
	private final Color sunColorAlf = Utils.getHSBColor(180d / 360, 1, 1, 0.0);
	/**Цвет полного мрака*/
	private final Color dirColor = Utils.getHSBColor(240d / 360, 1, 1, 0.7);
	/**Цвет минералов*/
	private final Color minColor = Utils.getHSBColor(300d / 360, 1, 1, 0.7);
	private final Color minColorAlf = Utils.getHSBColor(300d / 360, 1, 1, 0.0);
	
	/**Фоновое изображение воды*/
	private ColorRec []fon;
	
	
	public Sun(int width, int height){
		Configurations.sun = this;
		resize(width, height);
	}

	public void resize(int width, int height) {
		w = width;
		updateScrin();
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
		for(var i : fon)
			i.paint(g2d);
	}

	/**Шаг мира для пересчёта*/
	public void step(long step) {
		if(step % Configurations.SUN_SPEED == 0) {
			Configurations.SUN_POSITION++;
			if(Configurations.SUN_POSITION >= Configurations.MAP_CELLS.width)
				Configurations.SUN_POSITION -= Configurations.MAP_CELLS.width;
			updateScrin();
		}
	}
	
	/**Пересчитывает позиции всех объектов*/
	public void updateScrin() {
		
		//Первое солнце
		var PS1 = Configurations.SUN_POSITION > Configurations.MAP_CELLS.width / 2 ? (Configurations.SUN_POSITION - Configurations.MAP_CELLS.width) : Configurations.SUN_POSITION;
		//Второе солнце
		var PS2 = PS1 + Configurations.MAP_CELLS.width;
		//Левая часть без солнца
		int xl[] = new int[4];
		int yl[] = new int[4];
		//Первое солнце
		int xs1[] = new int[3];
		int ys1[] = new int[3];
		//Центарльная часть
		int xc[] = new int[4];
		int yc[] = new int[4];
		//Второе солнце
		int xs2[] = new int[3];
		int ys2[] = new int[3];
		//Правая часть без солнца
		int xr[] = new int[4];
		int yr[] = new int[4];
		//Грязная вода
		int xw[] = new int[4];
		int yw[] = new int[4];
		//Минералы
		int xm[] = new int[4];
		int ym[] = new int[4];
		
		xw[0] = xw[1] = xl[0] = xl[1] = xm[0] = xm[1] = Point.getRx(0);
		xl[3] = xs1[0] = Point.getRx(PS1 - Configurations.SUN_LENGHT);
		xs1[1] = Point.getRx(PS1);
		xl[2] = Point.getRx(PS1 - Configurations.SUN_LENGHT * Configurations.ADD_SUN_POWER / (Configurations.BASE_SUN_POWER + Configurations.ADD_SUN_POWER));
		xs1[2] = xc[0] = Point.getRx(PS1 + Configurations.SUN_LENGHT);
		xc[1] = Point.getRx(PS1 + Configurations.SUN_LENGHT * Configurations.ADD_SUN_POWER / (Configurations.BASE_SUN_POWER + Configurations.ADD_SUN_POWER));
		xs2[0] = xc[3] = Point.getRx(PS2 - Configurations.SUN_LENGHT);
		xs2[1] = Point.getRx(PS2);
		xc[2] = Point.getRx(PS2 - Configurations.SUN_LENGHT * Configurations.ADD_SUN_POWER / (Configurations.BASE_SUN_POWER + Configurations.ADD_SUN_POWER));
		xs2[2] = xr[0] = Point.getRx(PS2 + Configurations.SUN_LENGHT);
		xr[1] = Point.getRx(PS2 + Configurations.SUN_LENGHT * Configurations.ADD_SUN_POWER / (Configurations.BASE_SUN_POWER + Configurations.ADD_SUN_POWER));
		xw[2] = xw[3] = xr[2] = xr[3] = xm[2] = xm[3] = Point.getRx(Configurations.MAP_CELLS.width);
		
		yw[0] = yw[3] = yl[0] = yl[3] = ys1[0] = ys1[2] = yc[0] = yc[3] = ys2[0] = ys2[2] = yr[0] = yr[3] = Point.getRy(0);
		yl[1] = yl[2] = yc[1] = yc[2] = yr[1] = yr[2] = Point.getRy(Configurations.BASE_SUN_POWER * (Configurations.MAP_CELLS.height - Configurations.DIRTY_WATER));
		ys1[1] = ys2[1] = Point.getRy((Configurations.BASE_SUN_POWER + Configurations.ADD_SUN_POWER) * (Configurations.MAP_CELLS.height - Configurations.DIRTY_WATER));
		ym[0] = ym[3] = Point.getRy((int) (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL));
		yw[1] = yw[2] = ym[1] = ym[2] = Point.getRy(Configurations.MAP_CELLS.height);
		
		//Части без солнца
		fon = new ColorRec[7];
		GradientPaint gp;
		//Заполнение
		fon[0] = new ColorRec(xw,yw, dirColor);
		//Солнышки
		gp = new GradientPaint(w, yl[0], sunColor, w, yl[1], sunColorAlf);
		fon[1] = new ColorRec(xl,yl, gp);
		fon[2] = new ColorRec(xc,yc, gp);
		fon[3] = new ColorRec(xr,yr, gp);
		//Cолнца
		gp = new GradientPaint(w, ys1[0], sunColor, w, ys1[1], sunColorAlf);
		fon[4] = new ColorRec(xs1,ys1, gp);
		fon[5] = new ColorRec(xs2,ys2, gp);
		
		//Минералы
		gp = new GradientPaint(w, ym[0], minColorAlf, w, ym[1], minColor);
			
		fon[6] = new ColorRec(xm,ym, gp);
	}

}
