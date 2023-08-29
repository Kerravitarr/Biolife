package MapObjects;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;

import Utils.ColorRec;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Гейзер - представляет собой направленый поток воды Тёплой, если ориентация
 * вверх и Холодной, если ориентация вниз Причём считается, что поток
 * образовался естественным образом - нагревом или охлаждением Поэтому в начале
 * потока он засасывает материю в центр, а в конце наоборот - сбрасывает в
 * стороны
 *
 * @author Илья
 *
 */
public class Geyser {
    /**Ширина половины гейзера*/
    public final int width;
    /** Середина гейзера */
    public final int center;
    /**Каждые сколько тиков клетку в центре будет сдувать */
    public final int calm;
    /**Направление гейзера*/
    private final DIRECTION dir;
    /**Фоновое изображение Гейзера */
    private final ColorRec[] fon = new ColorRec[2];
    /**Холодный гейзер в центре*/
    private final static Color coldColor1 = new Color(0, 0, 205, 100);
    /**Холодный гейзер скраю */
    private final static Color coldColor2 = new Color(0, 0, 205, 0);
    /**Горячий гейзер в центре */
    private final static Color hotColor1 = new Color(220, 20, 60, 100);
    /**Горячий гейзер скраю */
    private final static Color hotColor2 = new Color(220, 20, 60, 0);

    /**
     * Создаёт гейзер
     *
     * @param center Позиция центра гейзера
     * @param width ширина гейзера
     * @param w ширина экрана в пикселях
     * @param h высота экрана в пикселях
     * @param dir направление движения воды в гейзере - вверх или вниз
     * @param calm показывает силу работы гейзера. Каждые сколько тиков клетку в
     * центре будет сдувать
     */
    public Geyser(int center, int width, int w, int h, DIRECTION dir, int calm) {
        this.center = center;
        if (dir != DIRECTION.DOWN) {
            dir = DIRECTION.UP;
        }
        this.width = width / 2;
        this.dir = dir;
        this.calm = Math.max(1, calm);
        updateScreen(w, h);
    }

    /**
     * Пересчитать графическое отображение гейзера при изменении размеров экрана
     *
     * @param w - ширина экрана
     * @param h - высота экрана
     */
    public void updateScreen(int w, int h) {
        int xl[] = new int[4];
        int yl[] = new int[4];
        int xr[] = new int[4];
        int yr[] = new int[4];

        xl[1] = xl[0] = Point.getRx(center - width);
        xl[2] = xl[3] = xr[1] = xr[0] = Point.getRx(center);
        xr[2] = xr[3] = Point.getRx(center + width);

        yl[0] = yl[3] = yr[0] = yr[3] = Point.getRy(0);
        yl[1] = yl[2] = yr[1] = yr[2] = Point.getRy(Configurations.MAP_CELLS.height);

        GradientPaint gp;
        if (dir == DIRECTION.DOWN) {
            gp = new GradientPaint(xl[0], yl[0], coldColor2, xl[3], yl[0], coldColor1);
        } else {
            gp = new GradientPaint(xl[0], yl[0], hotColor2, xl[3], yl[0], hotColor1);
        }
        fon[0] = new ColorRec(xl, yl, gp);

        if (dir == DIRECTION.DOWN) {
            gp = new GradientPaint(xr[0], yr[0], coldColor1, xr[3], yr[0], coldColor2);
        } else {
            gp = new GradientPaint(xr[0], yr[0], hotColor1, xr[3], yr[0], hotColor2);
        }
        fon[1] = new ColorRec(xr, yr, gp);

    }

    /**
     * Нарисовать гейзер на экране
     */
    public void paint(Graphics g) {
        for (ColorRec colorRec : fon) {
            colorRec.paint(g);
        }
    }

    /**
     * Клетка проходящая через гейзер. Или не проходящая, как получится
     */
    public void action(CellObject cell) {
        var distX = Point.subtractionX(center, cell.getPos().getX());
        if (Math.abs(distX) > width) {
            return;
        }
        //Сила затягивания клетки вниз. Линейный закон
        var F = calm + Math.abs(calm * distX / width);
        if (cell.getAge() % F == 0) {
            cell.moveD(dir); // Поехали по направлению!
        }		//Сила затягивания клетки к центру. Линейный закон
        var distY = ((double) cell.getPos().getY()) / Configurations.MAP_CELLS.height;
        F = (int) (calm + Configurations.MAP_CELLS.height * (Math.abs((distY - 0.5))));
        if (cell.getAge() % F == 0) { //Затягиваемся или выталкиваемся
            boolean right = distX > 0;
            if (distY < 0.5) {// Сдувание клетки в верхней части гейзера (засасывание/выталкивание)
                if (right && dir == DIRECTION.UP || !right && dir == DIRECTION.DOWN) {
                    cell.moveD(DIRECTION.RIGHT);
                } else if (right && dir == DIRECTION.DOWN || !right && dir == DIRECTION.UP) {
                    cell.moveD(DIRECTION.LEFT);
                } else if (Math.random() < 0.5) {
                    cell.moveD(DIRECTION.RIGHT);
                } else {
                    cell.moveD(DIRECTION.LEFT);
                }
            } else {
                if (right && dir == DIRECTION.UP || !right && dir == DIRECTION.DOWN) {
                    cell.moveD(DIRECTION.LEFT);
                } else if (right && dir == DIRECTION.DOWN || !right && dir == DIRECTION.UP) {
                    cell.moveD(DIRECTION.RIGHT);
                } else if (Math.random() < 0.5) {
                    cell.moveD(DIRECTION.RIGHT);
                } else {
                    cell.moveD(DIRECTION.LEFT);
                }
            }
        }
    }
}
