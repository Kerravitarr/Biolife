/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import Calculations.Configurations;
import Calculations.Point;
import GUI.AllColors;
import GUI.WorldView;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kerravitarr
* @deprecated
* Сюда пришли все те функции, которые уже не актуальны для проекта, но всё ещё могут использоваться для отладки
 */
@Deprecated()
public class DeprecatedMetods {
	/**Пара объектов для рисования на экране*/
	private static class SunMinPair{
		/**Площадь, которую мы разукрашиваем*/
		private final java.awt.geom.Area area;
		/**Цвет этой площади*/
		private final Color color;
		SunMinPair(java.awt.geom.Area a, Color c){area=a;color=c;}
	}
	
	/**Шаг последнего обновления солнц*/
	private static long lastUpdateSuns = 10000;
	/**Шаг последнего обновления минералов*/
	private static long lastUpdateMinerals = 10000;
	/**Массив, показывающий, отрисовали мы уже эту клетку поля или нет?*/
	private static int[][] isPaint = new int[0][0];
	/**Массив непосредственного рисования на экране*/
	private static List<SunMinPair> paintEmitters = new ArrayList<>();
	/**Последний известный масштаб для рисования излучатеелй*/
	private static double lastscalse = 0;
	
	/**
	 * Рисует излучатели на поле
	 * @param g полотно, которое красим
	 * @param transform преобразователь размеров мировых в размеры экранные
	 */
	public static void paintEmitters(Graphics2D g, WorldView.Transforms transform){
		if(Configurations.suns.getLustUpdate() != lastUpdateSuns || Configurations.minerals.getLustUpdate() != lastUpdateMinerals || lastscalse != transform.toDScrin(1)){
			final var ls = Configurations.suns.getLustUpdate();
			final var lm = Configurations.minerals.getLustUpdate();
			final var lsc = transform.toDScrin(1);
			if(isPaint.length != Configurations.getWidth() || isPaint[0].length != Configurations.getHeight())
				isPaint = new int[Configurations.getWidth()][Configurations.getHeight()];
			final var oldIsMin = isPaint[0][0] < 0;
			int index = oldIsMin ? 1 : -1;
			int maxSP = 0, maxMP = 0;
			for (int x = 0; x < Configurations.getWidth(); x++) {
				for (int y = 0; y < Configurations.getHeight(); y++) {
					if(isPaint[x][y] < 0 == oldIsMin){
						isPaint[x][y] = index;
						final var pos = Point.create(x, y);
						final var sE = (int) Math.ceil(Configurations.suns.getE(pos));
						final var mE = (int) Math.ceil(Configurations.minerals.getE(pos));
						maxSP = Math.max(maxSP, sE);
						maxMP = Math.max(maxMP, mE);
						int size = 1;
						while (paint(pos, size, oldIsMin, sE, mE, index) != 0){ //Цикл по всем радиусам
							while (paint(pos, size, oldIsMin, sE, mE, index) != 0){}; //Цикл по одному радиусу, для надёжности. Бывают случаи, когда без него ни как
							size++;
						}
						if(oldIsMin)
							index++;
						else
							index--;
					}
				}
			}
			paintEmitters.clear();
			final var wh = (float) transform.toDScrin(1);
			for (int x = 0; x < Configurations.getWidth(); x++) {
				for (int y = 0; y < Configurations.getHeight(); y++) {
					final var i = Math.abs(isPaint[x][y]);
					final var rectangle = new java.awt.geom.Area(new java.awt.geom.Rectangle2D.Float((float) (transform.toDScrinX(x) - wh/2), (float) (transform.toDScrinY(y) - wh/2), wh, wh));
					if(paintEmitters.size() < i){
						//Нужно создать наш элемент
						final var pos = Point.create(x, y);
						final var sE = (int) Math.ceil(Configurations.suns.getE(pos));
						final var mE = (int) Math.ceil(Configurations.minerals.getE(pos));
						final var sC = AllColors.toDark(AllColors.SUN, AllColors.SUN.getAlpha() * sE / maxSP);
						final var mC = AllColors.toDark(AllColors.MINERALS, AllColors.MINERALS.getAlpha() * mE / maxMP);
						
						paintEmitters.add(new SunMinPair(rectangle,blend(sC,mC)));
					} else {
						paintEmitters.get(i-1).area.add(rectangle);
					}
				}
			}
			
			lastUpdateSuns = ls;
			lastUpdateMinerals = lm;
			lastscalse = lsc;
		}
		for(final var i : paintEmitters){
			g.setColor(i.color);
			g.fill(i.area);
		}
	}
	/**Смешивает цвета пропорционально их альфа каналу
	 * @param c0 первый цвет
	 * @param c1 второй цвет
	 * @return цвет, как сумма смешиваемых
	 */
	public static Color blend(Color c0, Color c1) {
		final double totalAlpha = c0.getAlpha() + c1.getAlpha();
		final double weight0 = totalAlpha == 0 ? 0.5d : c0.getAlpha() / totalAlpha;
		final double weight1 = totalAlpha == 0 ? 0.5d : c1.getAlpha() / totalAlpha;

		double r = weight0 * c0.getRed() + weight1 * c1.getRed();
		double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
		double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
		double a = Math.max(c0.getAlpha(), c1.getAlpha());

		return new Color((int) r, (int) g, (int) b, (int) a);
	}
	/**
	 * Этот монстр разносит в карте разные области за разными объектами.
	 * Его задача взять точку и обойти её по квадрату с радиусом size
	 * И все точки на этом квадрате, что имеют те-же значения sE и mE, что и точка
	 * start, а ещё что имеют с start прямой путь (без наискосок) связать с start
	 * одним index.
	 * @param start точка отсчёта, базовая точка
	 * @param size радиус квадрата, который обхдоим
	 * @param oldIsMin флаг, показываеющий, устаревшие значения меньше нуля?
	 * @param sE значение интерсоляции в этой точке
	 * @param mE значение плотности минералов в этой точке
	 * @param index порядковый индекс точки start
	 * @return количество точек, добавленных в коллекцию к start
	 */
	private static int paint(Point start, int size, boolean oldIsMin, int sE, int mE, int index){
		int countFind = 0;
		final var mix = start.getX() - size;
		final var miy = start.getY() - size;
		final var max = start.getX() + size;
		final var may = start.getY() + size;
		
		if(may < Configurations.getHeight()){
			final var sx = Math.max(0, mix);
			final var ex = Math.min(max + 1, Configurations.getWidth());  //+1, чтобы получился полный квадрат
			for(int x = sx ; x < ex ; x++){
				if(isPaint[x][may] < 0 == oldIsMin){
					final var pos = Point.create(x, may);
					if( (int) Math.ceil(Configurations.suns.getE(pos)) == sE && (int) Math.ceil(Configurations.minerals.getE(pos)) == mE){
						boolean addPoint = (may > 0 && isPaint[x][may - 1] == index) || ((x > 0) && (isPaint[x - 1][may] == index));
						if(addPoint || true){
							isPaint[x][may] = index;
							countFind++;
						}
					}
				}
			}
		}
		
		if(max < Configurations.getWidth()){
			final var sy = Math.max(0, miy);
			final var ey = Math.min(may+1, Configurations.getHeight());  //+1, чтобы получился полный квадрат
			for(int y = sy ; y < ey ; y++){
				if(isPaint[max][y] < 0 == oldIsMin){
					final var pos = Point.create(max, y);
					if( (int) Math.ceil(Configurations.suns.getE(pos)) == sE && (int) Math.ceil(Configurations.minerals.getE(pos)) == mE){
						boolean addPoint = (max > 0 && isPaint[max - 1][y] == index) || ((y > 0) && (isPaint[max][y] == index));
						if(addPoint || true){
							isPaint[max][y] = index;
							countFind++;
						}
					}
				}
			}
		}
		
		return countFind;
	}
	
	/**Вспомогательная, отладочная функция, рисования клеток поля
	 * @param g 
	 * @param transform преобразователь размеров мировых в размеры экранные
	 */
	public static void paintCells(Graphics2D g, WorldView.Transforms transform) {
		final var r = transform.toScrin(1);
		for (int x = 0; x < Configurations.getWidth(); x++) {
			for (int y = 0; y < Configurations.getHeight(); y++) {
				final var pos = Point.create(x, y);
				if(!pos.valid()) continue;
				if(x % 10 == 0)
					g.setColor(Color.RED);
				else if(y % 10 == 0)
					g.setColor(Color.YELLOW);
				else
					g.setColor(Color.BLACK);
				final var cx = transform.toScrinX(pos);
				final var cy = transform.toScrinY(pos);
				g.drawRect(cx-r/2, cy-r/2,r, r);
			}
		}
	}
	
	
}
