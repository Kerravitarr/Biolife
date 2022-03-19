package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import MapObjects.CellObject.LV_STATUS;
import Utils.ColorRec;
import main.World;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Гейзер - представляет собой направленый поток воды
 * Тёплой, если ориентация вверх и 
 * Холодной, если ориентация вниз
 * Причём считается, что поток образовался естественным образом - нагревом или охлаждением
 * Поэтому в начале потока он засасывает материю в центр, а в конце наоборот - сбрасывает в стороны
 * @author Илья
 *
 */
public class Geyser {
	/**
	 * Кусок гейзера. Сам гейзер состоит из разных областей
	 * Вот это описание одной конкретной области
	 * @author Илья
	 *
	 */
	private static class Section extends ColorRec{
		public Section(int x0, int y0, int w, int h, Color c, int startL, int lenghtSection, int power) {
			super(x0, y0, w, h, c);
			startX = startL;
			endX = startX + lenghtSection;
			this.power=power;
		}
		/**Начало области*/
		int startX;
		/**Конец области*/
		int endX;
		/**Сила действия*/
		int power;
		public String toString() {
			return "sx: " + startX + " ex: " + endX + " p: " + power + super.toString();
		}
	}
	
	public Geyser(int startX, int endX,int w,int h,DIRECTION dir, int power) {
		center = startX + (endX - startX ) / 2;
		if(dir != DIRECTION.DOWN)
			dir = DIRECTION.UP;
		this.startX=startX;
		this.endX=endX;
		this.dir=dir;
		this.power = Math.max(1, power);
		updateScreen(w,h);
	}
	/**На сколько секций делится каждый гейзер, градиент скоростей*/
	private static int GRADIENT = 4;
	
	/**Начало гейзера*/
	public int startX;
	/**Конец гейзера*/
	public int endX;
	/**Середина гейзера*/
	public int center;
	/**Сила гейзера*/
	public int power;
	/**Нижняя граница, где поток имеет боковую составляющую*/
	private int downWall;
	/**Верхняя граница, где поток имеет боковую составляющую*/
	private int upWall;
	/**Направление гейзера*/
	DIRECTION dir;
	/**Секции, из которых состоит гейзер*/
	Section[] cr = new Section[GRADIENT*2];
	
	/**
	 * Пересчитать графическое отображение гейзера при изменении размеров экрана
	 * @param w - ширина экрана
	 * @param h - высота экрана
	 */
	public void updateScreen(int w,int h) {
		int lenghtSection = (int) Math.round((endX - startX)/(GRADIENT*2.0));
		int startL = startX;
		int endR = endX;
		for(int i = 0 ; i < GRADIENT ; i++) {
			int startXPxL = Point.getRx(startL);
			int lenghtPx = Point.getRr(lenghtSection);
			Color color;
			if(dir == DIRECTION.DOWN)
				color = new Color(0, 0, 205, 10 + 6*i/(GRADIENT-1));
			else
				color = new Color(220, 20, 60, 13 + 12*i/(GRADIENT-1));
			cr[i] 				   = new Section(startXPxL, Configurations.border.height, lenghtPx, h-Configurations.border.height*2, color,startL,lenghtSection, 1 + power * i / (GRADIENT-1));
			if(i == GRADIENT - 1) {
				lenghtSection = endR - (startL+lenghtSection);
				lenghtPx = Point.getRr(lenghtSection);
			}
			int startXPxR = Point.getRx(endR - lenghtSection);
			cr[GRADIENT*2 - i - 1] = new Section(startXPxR, Configurations.border.height, lenghtPx, h-Configurations.border.height*2, color,endR-lenghtSection,lenghtSection, 1 + power * i / (GRADIENT-1));
			startL += lenghtSection;
			endR -= lenghtSection;
		}
		upWall = (endX - startX)/2;
		downWall = Configurations.MAP_CELLS.height - upWall;
	}
	/**Нарисовать гейзер на экране*/
	public void paint(Graphics g) {
		for (ColorRec colorRec : cr)
			colorRec.paint(g);
	}
	
	/**Клетка проходящая через гейзер. Или не проходящая, как получится*/
	public void action(CellObject cell) {
		if(!(startX <= cell.getPos().x && cell.getPos().x <= endX))
			return;
		for (Section se : cr) {
			if(se.startX <= cell.getPos().x && cell.getPos().x <= se.endX) {
				if(cell.getAge() % (power - se.power + 2) == 0) { // +2 так как se.power меняется от 1 до power + 1, а на ноль делить нельзя
					cell.moveD(dir); // Некая сила
					if (cell.getPos().y < upWall) { // Сдувание клетки в верхней части гейзера (засасывание/выталкивание)
						boolean right = cell.getPos().x - center > 0;
						if (right && dir == DIRECTION.UP || !right  && dir == DIRECTION.DOWN)
							cell.moveD(DIRECTION.RIGHT);
						else if(right && dir == DIRECTION.DOWN || !right  && dir == DIRECTION.UP)
							cell.moveD(DIRECTION.LEFT);
						else if(Math.random() < 0.5) 
							cell.moveD(DIRECTION.RIGHT);
						else
							cell.moveD(DIRECTION.LEFT);
					}  else if (cell.getPos().y > downWall) { // Сдувание клетки в нижней части гейзера (выталкивание/засасывание)
						boolean right = cell.getPos().x - center > 0;
						if (right && dir == DIRECTION.UP || !right  && dir == DIRECTION.DOWN)
							cell.moveD(DIRECTION.LEFT);
						else if(right && dir == DIRECTION.DOWN || !right  && dir == DIRECTION.UP)
							cell.moveD(DIRECTION.RIGHT);
						else if(Math.random() < 0.5) 
							cell.moveD(DIRECTION.RIGHT);
						else
							cell.moveD(DIRECTION.LEFT);
					} 
				}
				return;
			}
		}
	}
}
