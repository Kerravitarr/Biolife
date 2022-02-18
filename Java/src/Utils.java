import java.awt.Graphics;

public class Utils {

	/**
	 * Метод получения псевдослучайного целого числа от min до max (включая max);
	 * @param min
	 * @param max
	 */
	public static int random(int min, int max) {
		max -= min;
		return (int) (Math.random() * ++max) + min;
	}

	/**
	 * Рисует круг
	 * @param g
	 * @param x
	 * @param y
	 * @param r
	 */
	public static void fillCircle(Graphics g, int x, int y, int r) {
		g.fillOval(x-r/2, y-r/2, r, r);
	}
}
