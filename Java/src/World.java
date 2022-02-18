import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

@SuppressWarnings("serial")
public class World extends JPanel {
	static class Point{
		int x;
		int y;
		Point(int x, int y){
			while(x >= MAP_CELLS.width)
				x -= MAP_CELLS.width;
			while(x < 0)
				x += MAP_CELLS.width;
			this.x=x;
			this.y=Math.max(0, Math.min(y, MAP_CELLS.height -1));
		}
		public int getRx() {
			return 200;
		}
		public int getRy() {
			return 100;
		}
		public int getRr() {
			return MAP_R*scale;
		}
	}
	
	/**Шаги мира*/
	int step = 0;
	/**Количиство ячеек карты*/
	static final Dimension MAP_CELLS = new Dimension(10,10);
	/**Размер ячеек карты*/
	static final int MAP_R = 1;
	/**Перечисление всех возможных адресов поля*/
	Point [] cells = new Point[MAP_CELLS.width*MAP_CELLS.height];
	/**Масштаб*/
	static int scale = 1;
	/**Высота "неба"*/
	double UP_border = 0.05;
	/**Высота "земли"*/
	double DOWN_border = 0.05;
	/**Дополнительный край из-за не совершенства арены*/
	Dimension border = new Dimension();
	/**Сам мир*/
	Cell [][] world = new Cell[MAP_CELLS.width][MAP_CELLS.height];
	/**
	 * Create the panel.
	 */
	public World() {
		super();
		
		for (int x = 0; x < MAP_CELLS.width; x++) {
			for (int y = 0; y < MAP_CELLS.height; y++) {
				cells[y + x*MAP_CELLS.height] = new Point(x,y);
			}
		}
		
		//Начальные клетки
		for (int i = 0; i < 10; i++) {
			add(new Cell());
		}
		
		setBackground(new Color(0, 255, 255, 100));

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				double hDel = 1.0 * getHeight() * (1 - (UP_border + DOWN_border)) / (MAP_CELLS.height*MAP_R);
				double wDel = 1.0 * getWidth() / (MAP_CELLS.width*MAP_R);
				scale = (int) Math.min(hDel, wDel);
			}
		});
	}
	
	private void add(Cell cell) {
		world[cell.pos.x][cell.pos.y] = cell;
	}

	public void paintComponent(Graphics g) {
		repaint();
		
		paintField(g);
		
		/**Перетосовываем, чтобы у всех были равные шансы на ход*/
		shuffle(cells);
		
		for (Point point : cells) {
			Cell cell= get(point);
			if(cell != null) {
				cell.step();
				cell.paint(g);
			}
		}
		
		step++;
	}

	private void paintField(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		int width = MAP_CELLS.width*MAP_R*scale;
		int widthRight = (getWidth() - width)/2;
		int height = (getHeight() - MAP_CELLS.height*MAP_R*scale)/2;
		int heightDown = getHeight() - height;
		
		g.setColor(new Color(224, 255, 255, 100));
		g.fillRect(widthRight, 0, width , height);
		
		g.setColor(new Color(139, 69, 19, 150));
		g.fillRect(widthRight, heightDown, width , height);
		
	}

	private Cell get(Point point) {
		return world[point.x][point.y];
	}

	/**
	 * Тасует вектор в случайном порядке
	 * @param array
	 */
	private void shuffle(Object[] array) {
		for (int i = array.length - 1; i > 0; i--) {
			int j = (int) Math.floor(Math.random() * (i + 1)); // случайный индекс от 0 до i
			Object t = array[i];
			array[i] = array[j];
			array[j] = t;
		}
	}
}
