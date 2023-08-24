package MapObjects;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

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
	
	private final class PointsPair{
		private final int  []x;
		private final int  []y;
		PointsPair(int []x,int []y){this.x = x; this.y = y;}
	}
	
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
	
	/**Абсолютные координаты солнца. То есть к ним достаточно просто прибавить положение солнца. В каждой ячейке количество солнца на поверхности*/
	private double[] sunY;
	private int O_ADD_SUN_POWER = 0;
	private int O_BASE_SUN_POWER = 0;
	private int O_SUN_FORM = 0;
	
	
	public Sun(int width, int height){
		Configurations.sun = this;
		resize(width, height);
	}

	public void resize(int width, int height) {
		w = width;
		updateScrin();
	}
	/**
	 * Возвращает количество солнечной энергии в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии. Может быть отрицательным числом - это не поглащение света, а удалённость от солнца
	 */
	public double getEnergy(Point pos) {
		var lum = 100 - (10000 * pos.getY())/(Configurations.MAP_CELLS.height * Configurations.DIRTY_WATER);
		double delX = Math.abs(Point.subtractionX(Configurations.SUN_POSITION, pos.getX()));
		double E = Configurations.BASE_SUN_POWER * lum / 100;
		if(delX <= Configurations.SUN_LENGHT) {
			E += O_ADD_SUN_POWER * Math.pow(1 - delX / Configurations.SUN_LENGHT, O_SUN_FORM == 0 ? 1 : (O_SUN_FORM > 0 ? O_SUN_FORM + 1 : -(1d / (O_SUN_FORM - 1))));
		}
		return E;
	}

	/**
	 * Отрисовывает себя кому угодно
	 * @param g2d
	 */
	public void paint(Graphics2D g2d) {
		for(var i : fon)
			i.paint(g2d);
	}

	/**Шаг мира для пересчёта*/
	public void step(long step) {
		if(Configurations.SUN_SPEED == 0 || step % Configurations.SUN_SPEED == 0) {
			if(Configurations.SUN_SPEED < 0){
				Configurations.SUN_POSITION--;
				if(Configurations.SUN_POSITION < 0)
					Configurations.SUN_POSITION += Configurations.MAP_CELLS.width;
			}else{
				Configurations.SUN_POSITION++;
				if(Configurations.SUN_POSITION >= Configurations.MAP_CELLS.width)
					Configurations.SUN_POSITION -= Configurations.MAP_CELLS.width;
			}
			updateScrin();
		}
	}
	
	/**Пересчитывает позиции всех объектов*/
	public void updateScrin() {
		//Копирование параметров, чтобы они не поменялись во время расчётов
		var SUN_LENGHT = Configurations.SUN_LENGHT;
		var ADD_SUN_POWER = Configurations.ADD_SUN_POWER;
		var SUN_FORM = Configurations.SUN_FORM;
		var BASE_SUN_POWER = Configurations.BASE_SUN_POWER;
		if(sunY == null || sunY.length != SUN_LENGHT || O_ADD_SUN_POWER != ADD_SUN_POWER || O_SUN_FORM != SUN_FORM || O_BASE_SUN_POWER != BASE_SUN_POWER) {
			sunY = new double[SUN_LENGHT];
			O_ADD_SUN_POWER = ADD_SUN_POWER;
			O_SUN_FORM = SUN_FORM;
			O_BASE_SUN_POWER = BASE_SUN_POWER;
			
			for(var i = 0 ; i < SUN_LENGHT ; i++) {
				sunY[i] = Configurations.BASE_SUN_POWER + O_ADD_SUN_POWER * Math.pow(1 - i / ((double) SUN_LENGHT), O_SUN_FORM == 0 ? 1 : (O_SUN_FORM > 0 ? O_SUN_FORM + 1 : -(1d / (O_SUN_FORM - 1))));
			}
			//Теперь у нас есть зависимость Y(|x|) = f(ADD_SUN_POWER)
			//Это функция для половины солнца, от центра в сторону увелчиения Х
		}
		//Первое солнце
		var PS1 = Configurations.SUN_POSITION > Configurations.MAP_CELLS.width / 2 ? (Configurations.SUN_POSITION - Configurations.MAP_CELLS.width) : Configurations.SUN_POSITION;
		//Второе солнце
		var PS2 = PS1 + Configurations.MAP_CELLS.width;
		//Левая часть без солнца
		int xl[] = new int[4];
		int yl[] = new int[4];
		//Первое солнце
		var s1 = makeSun(PS1,SUN_LENGHT);
		//Центарльная часть
		int xc[] = new int[4];
		int yc[] = new int[4];
		//Второе солнце
		var s2 = makeSun(PS2,SUN_LENGHT);
		//Правая часть без солнца
		int xr[] = new int[4];
		int yr[] = new int[4];
		//Грязная вода
		int xw[] = new int[4];
		int yw[] = new int[4];
		//Минералы
		int xm[] = new int[4];
		int ym[] = new int[4];
		//Белые уши, для сокрытия рисовки
		int xwl[] = new int[4];
		int ywl[] = new int[4];
		int xwr[] = new int[4];
		int ywr[] = new int[4];
		
		xwl[0] = xwl[1] = 0;
		xwl[2] = xwl[3] = xm[0] = xm[1] = xw[0] = xw[1] = xl[0] = xl[1] = Point.getRx(0);
		xl[2] = s1.x[1];
		xl[3] = s1.x[0];
		xc[0] = s1.x[s1.x.length - 1];
		xc[1] = s1.x[s1.x.length - 2];
		xc[2] = s2.x[1];
		xc[3] = s2.x[0];
		xr[0] = s2.x[s2.x.length - 1];
		xr[1] = s2.x[s2.x.length - 2];
		xwr[0] = xwr[1] = xm[2] = xm[3] = xw[2] = xw[3] = xr[2] = xr[3] = Point.getRx(Configurations.MAP_CELLS.width);
		xwr[2] = xwr[3] = Configurations.world.getWidth();
		
		ywl[0] = ywl[3] = ywr[0] = ywr[3] = yw[0] = yw[3] = yl[0] = yl[3] = yr[0] = yr[3] = Point.getRy(0);
		yl[1] = yl[2] = yr[1] = yr[2] = Point.getRy((int) (Configurations.MAP_CELLS.height * Configurations.DIRTY_WATER / 100.0));
		yl[2] = s1.y[1];
		yl[3] = s1.y[0];
		yc[0] = s1.y[s1.y.length - 1];
		yc[1] = s1.y[s1.y.length - 2];
		yc[2] = s2.y[1];
		yc[3] = s2.y[0];
		yr[0] = s2.y[s2.y.length - 1];
		yr[1] = s2.y[s2.y.length - 2];
		ym[0] = ym[3] = Point.getRy((int) (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL));
		ywl[1] = ywl[2] = ywr[1] = ywr[2] = yw[1] = yw[2] = ym[1] = ym[2] = Point.getRy(Configurations.MAP_CELLS.height);
		
		
		
		//Части без солнца
		var lfon = new ColorRec[9];
		GradientPaint gp;
		//Заполнение
		lfon[0] = new ColorRec(xw,yw, dirColor);
		//Солнышки
		gp = new GradientPaint(w, yl[0], sunColor, w, yl[1], sunColorAlf);
		lfon[1] = new ColorRec(xl,yl, gp);
		lfon[2] = new ColorRec(xc,yc, gp);
		lfon[3] = new ColorRec(xr,yr, gp);
		//Cолнца
		gp = new GradientPaint(w, s1.y[0], sunColor, w, s1.y[s1.y.length/2], sunColorAlf);
		lfon[4] = new ColorRec(s1.x,s1.y, gp);
		lfon[5] = new ColorRec(s2.x,s2.y, gp);
		
		//Минералы
		gp = new GradientPaint(w, ym[0], minColorAlf, w, ym[1], minColor);
			
		lfon[6] = new ColorRec(xm,ym, gp);
		//Боковины, скроющие особенности рисования
		lfon[7] = new ColorRec(xwl,ywl, Color.WHITE);
		lfon[8] = new ColorRec(xwr,ywr, Color.WHITE);
		
		fon = lfon;
	}
	
	private PointsPair makeSun(int PS, int SUN_LENGHT) {
		int []x =  new int[SUN_LENGHT * 2 + 2];
		int []y =  new int[SUN_LENGHT * 2 + 2];
		int pos = 0;
		x[pos] = Point.getRx((PS - SUN_LENGHT));
		y[pos++] = Point.getRy(0);
		var dry = (1d / Configurations.BASE_SUN_POWER) * Configurations.MAP_CELLS.height * Configurations.DIRTY_WATER / 100.0;
		for(var i = 1 ; i < SUN_LENGHT + 1; i++) {
			x[pos] = Point.getRx(i + (PS - SUN_LENGHT));
			var yp = sunY[SUN_LENGHT - i] * dry;
			y[pos++] = Point.getRy((int) yp);
		}
		for(var i = 0 ; i < SUN_LENGHT; i++) {
			x[pos] = Point.getRx(i + (PS));
			var yp = sunY[i] * dry;
			y[pos++] = Point.getRy((int) yp);
		}
		x[pos] = Point.getRx((PS + SUN_LENGHT));
		y[pos++] = y[0];
		return new PointsPair(x,y);
	}

}
