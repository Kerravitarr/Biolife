package main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Utils.FPScounter;
import Utils.JSONmake;
import Utils.Utils;
import main.Cell.LV_STATUS;
import main.Cell.OBJECT;
import panels.BotInfo;
import panels.Settings;

public class World extends JPanel {

	/**Количиство ячеек карты*/
	public static final Dimension MAP_CELLS = new Dimension(200,100);
	/**Освещённость карты*/
	public static double SUN_POWER = 10;
	/**Уровень загрязнения воды*/
	public static double DIRTY_WATER = 17;
	/**Степень мутагенности воды*/
	public static double AGGRESSIVE_ENVIRONMENT = 0.25;
	/**Как глубоко лежат минералы*/
	public static double LEVEL_MINERAL = 0.50;
	/**Концентрация минералов*/
	public static double CONCENTRATION_MINERAL = 1;
	//Сколько тиков подряд обновлять экран
	public static int FPS_TIC = 1;
	//Как быстро разлагается продукты
	public static int TIK_TO_EXIT = 1;
	/**Симуляция запущена?*/
	public static boolean isActiv = true;
	/**Сколько милисекунд делать перед ходами*/
	public static int msTimeout = 0;
	
	/**Глобальный мир!*/
	public static World world = null;
	
	class AutostartThread extends Thread{AutostartThread(Runnable target){super(target);start();}}
	
	class WorldTask implements Runnable{
		private Graphics g;
		Point [] cellsThread;
		WorldTask(Point [] cellsThread){
			this.cellsThread= cellsThread;
		}
		public WorldTask start(Graphics g) {
			this.g=g;
			return this;
		}
		public void run() {
			/**Перетосовываем, чтобы у всех были равные шансы на ход*/
			shuffle(cellsThread);
			for (Point point : cellsThread) {
				Cell cell= get(point);
				if(cell != null && cell.stepCount != step) {
					cell.step(step);
					if(g != null)
						cell.paint(g);
				}
			}
		}
	}
	
	/**Шаги мира*/
	public long step = 0;
	/**Всего точек по процессорам - сколько процессоров, в каждом ряду есть у*/
	WorldTask [] cells;
	Thread [] threads;
	/**А дополительные клетки - от каждого процессора по 2 ряда, ну и дополнительные*/
	WorldTask betweenCells;
	/**Масштаб*/
	static double scale = 1;
	/**Высота "неба"*/
	double UP_border = 0.05;
	/**Высота "земли"*/
	double DOWN_border = 0.05;
	/**Дополнительный край из-за не совершенства арены*/
	static Dimension border = new Dimension();
	/**Сам мир*/
	Cell [][] worldMap = new Cell[MAP_CELLS.width][MAP_CELLS.height];
	/**Счётчик ФПС*/
	public final FPScounter fps = new FPScounter();
	/**Счётчик шагов*/
	public final FPScounter sps = new FPScounter();
	/**Счётчик ораганики*/
	public int countOrganic = 0;
	/**Счётчик живых*/
	public int countLife = 0;
	/**Все цвета, которые мы должны отобразить на поле*/
	Color [] colors;
	/**Сюда отправляем бота, для его изучения*/
	BotInfo info = null;
	/**Настройки мира*/
	Settings settings = null;
	/**Эволюция ботов нашего мира*/
	EvolutionTree tree = new EvolutionTree();
	/**Все мегаклетки, чтобы они могли походить после всех*/
	List<MegaCell> megaCells = new ArrayList<>();
	/**
	 * Create the panel.
	 */
	public World(BotInfo botInfo,Settings settings) {
		super();
		world = this;
		info = botInfo;
		this.settings=settings;
		
		int countProc = Runtime.getRuntime().availableProcessors()*2/3;
		
		while(MAP_CELLS.width - 2*countProc < countProc)
			countProc /= 2;
		
		/**Сколько рядов уместится в одном процессоре*/
		int lenghtX = MAP_CELLS.width / countProc;
		/**Сколько рядов придётся ещё добавить*/
		int addX = MAP_CELLS.width - lenghtX*countProc;
		
		Point [][] cellsThread = new Point[countProc][(lenghtX-2) * MAP_CELLS.height];
		Point [] cellsMain = new Point[(countProc*2 + addX) * MAP_CELLS.height];
		
		int main = 0;
		for (int proc = 0; proc < countProc; proc++) {
			for (int x = 0; x < lenghtX; x++) {
				for (int y = 0; y < MAP_CELLS.height; y++) {
					if(x >= lenghtX -2 ) {
						cellsMain[main++] = new Point(x + proc * lenghtX,y);
					} else {
						cellsThread[proc][x*MAP_CELLS.height+y] = new Point(x + proc * lenghtX,y);
					}
				}
			}
		}
		
		for (int i = 0; i < addX; i++) {
			for (int y = 0; y < MAP_CELLS.height; y++) {
				cellsMain[main++] = new Point(countProc * lenghtX + i,y);
			}
		}
		
		cells = new WorldTask[cellsThread.length];
		for (int i = 0; i < cells.length; i++) {
			cells[i] = new WorldTask(cellsThread[i]);
		}
		threads = new Thread[cells.length];
		betweenCells = new WorldTask(cellsMain);
		
		//Начальные клетки
		Cell adam = new Cell();
		adam.pos = new Point(MAP_CELLS.width/2,0);
		EvolutionTree.root.countAliveCell = 1;
		adam.evolutionNode = EvolutionTree.root;
		add(adam);
		
		recalculate();

		this.settings.updateScrols();
		
		setBackground(new Color(255, 255, 255, 255));

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				double hDel = 1.0 * getHeight() * (1 - (UP_border + DOWN_border)) / (MAP_CELLS.height);
				double wDel = 1.0 * getWidth() / (MAP_CELLS.width);
				scale = Math.min(hDel, wDel);
				border.width = (int) Math.round((getWidth() -MAP_CELLS.width*scale)/2);
				border.height = (int) Math.round((getHeight() - MAP_CELLS.height*scale)/2);
			}
		});
		
		addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		    	if(info.isVisible()) {
		    		int realX = e.getX();
		    		if(realX < border.width || realX > getWidth() - border.width)
		    			return;
		    		int realY = e.getY();
		    		if(realY < border.height || realY > getHeight() - border.height)
		    			return;
		    		realX -= border.width;
		    		realY -= border.height;
		    		realX = (int) Math.round(realX/scale-0.5);
		    		realY = (int) Math.round(realY/scale-0.5);
		    		Point point = new Point(realX,realY);
		    		info.setCell(get(point));
		    	}
		    }
		});
		
		new AutostartThread(new Runnable() {
			public void run() {
				while(true) {
					if(isActiv) {
						step();
					}else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					if(msTimeout != 0) {
						Utils.pause_ms(msTimeout);
					}
				}
			}
		});
		
	}
	
	public void step() {
		try {
			if (Math.random() <= 0.5) {
				Thread thread = new AutostartThread(betweenCells.start(null));
				thread.join();
				for (int i = 0; i < threads.length; i++)
					threads[i] = new AutostartThread(cells[i].start(null));
				for (Thread i : threads)
					i.join();
			} else {
				for (int i = 0; i < threads.length; i++)
					threads[i] = new AutostartThread(cells[i].start(null));
				for (Thread i : threads)
					i.join();
				Thread thread = new AutostartThread(betweenCells.start(null));
				thread.join();
			}
			for (MegaCell megaCell : megaCells) {
				megaCell.step();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		step++;
		sps.interapt();
	}

	public void paintComponent(Graphics g) {
		repaint();
	
		if(!isActiv || (step % FPS_TIC == 0)) {
			super.paintComponent(g);
			
			paintField(g);
			
			countLife = countOrganic = 0;
			for (Cell[] cell : worldMap) {
				for (Cell cell2 : cell) {
					if(cell2 != null) {
						cell2.paint(g);
						if(cell2.alive == LV_STATUS.LV_ALIVE)
							countLife++;
						else
							countOrganic++;
					}
				}
			}
			if(info.getCell() != null) {
				g.setColor(Color.BLACK);
				//g.drawLine(border.width, border.height, getWidth()-border.width, getHeight()-border.height);
				g.drawLine(info.getCell().pos.getRx(), border.height, info.getCell().pos.getRx(), getHeight()-border.height);
				g.drawLine(border.width, info.getCell().pos.getRy(), getWidth()-border.width, info.getCell().pos.getRy());
			}
		}

		fps.interapt();
	}

	private void paintField(Graphics g) {
		g.setColor(colors[0]);
		g.fillRect(border.width, 0, getWidth()-border.width*2, border.height);

		int lenghtY = getHeight()-border.height*2;
        int heightSun = (int) Math.ceil(lenghtY/World.DIRTY_WATER);
        for (int i = 0; i < World.DIRTY_WATER; i++) {
    		g.setColor(colors[1+i]);
    		g.fillRect(border.width, border.height + i*heightSun, getWidth()-border.width*2, heightSun);
		}
  
		g.setColor( colors[(int) (1+World.DIRTY_WATER)]);
		g.fillRect(border.width, (int) (border.height + lenghtY*LEVEL_MINERAL), getWidth()-border.width*2, (int) (lenghtY*(1-LEVEL_MINERAL)/2));
		g.setColor( colors[(int) (1+World.DIRTY_WATER+1)]);
		g.fillRect(border.width, (int) (getHeight()-border.height-(lenghtY*(1-LEVEL_MINERAL)/2)), getWidth()-border.width*2,(int) (lenghtY*(1-LEVEL_MINERAL)/2));
		
		g.setColor(colors[(int) (1+World.DIRTY_WATER+2)]);
		g.fillRect(border.width, getHeight()-border.height, getWidth()-border.width*2, border.height);
				
		//paintLine(g);
	}

	@SuppressWarnings("unused")
	private void paintLine(Graphics g) {
		for (int y = 0; y < MAP_CELLS.height; y++) {
			Point from = new Point(0, y);
			Point to = new Point(MAP_CELLS.width - 1, y);
			g.drawLine(from.getRx(), from.getRy(), to.getRx(), to.getRy());
		}
		for (int x = 0; x < MAP_CELLS.width; x++) {
			Point from = new Point(x, 0);
			Point to = new Point(x, MAP_CELLS.height - 1);
			g.drawLine(from.getRx(), from.getRy(), to.getRx(), to.getRy());
		}
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

	public OBJECT test(Point point) {
		if(point.y < 0 || point.y >= MAP_CELLS.height)
			return OBJECT.WALL;
		Cell cell = get(point);
		if(cell == null)
			return OBJECT.CLEAN;
		else if(cell.alive == LV_STATUS.LV_ORGANIC_HOLD || cell.alive == LV_STATUS.LV_ORGANIC_SINK)
			return OBJECT.ORGANIC;
		else
			return OBJECT.BOT;
	}

	public Cell get(Point point) {
		return worldMap[point.x][point.y];
	}
	
	void add(Cell cell) {
		worldMap[cell.pos.x][cell.pos.y] = cell;
	}
	public void clean(Point point) {
		worldMap[point.x][point.y] = null;
	}

	public void recalculate() {
		colors = new Color[(int) (2 + World.DIRTY_WATER+2)];
		colors[0] = new Color(224, 255, 255, 255); //небо
		for (int i = 0; i < World.DIRTY_WATER; i++) {
			float sunPower = (float) ((240 - Math.max(0, (1.0*World.SUN_POWER - i)/World.SUN_POWER)*60)/360);
			colors[1+i] = Utils.getHSBColor(sunPower, 1, 1,0.5);
		}

		colors[(int) (1 + World.DIRTY_WATER)] = Utils.getHSBColor(270.0/360,0.5,1.0,0.5);
		colors[(int) (1 + World.DIRTY_WATER+1)] =  ( Utils.getHSBColor(300.0/360,0.5,1.0,0.5));
		colors[(int) (1 + World.DIRTY_WATER+2)] = new Color(139, 69, 19, 255);
		
	}

	public JSONmake serelization() {
		JSONmake make = new JSONmake();

		JSONmake configWorld = new JSONmake();
		configWorld.add("SUN_POWER", SUN_POWER);
		configWorld.add("MAP_CELLS", new int[] {World.MAP_CELLS.width,World.MAP_CELLS.height});
		configWorld.add("DIRTY_WATER", DIRTY_WATER);
		configWorld.add("AGGRESSIVE_ENVIRONMENT", AGGRESSIVE_ENVIRONMENT);
		configWorld.add("LEVEL_MINERAL", LEVEL_MINERAL);
		configWorld.add("CONCENTRATION_MINERAL", CONCENTRATION_MINERAL);
		configWorld.add("FPS_TIC", FPS_TIC);
		configWorld.add("TIK_TO_EXIT", TIK_TO_EXIT);
		configWorld.add("step", step);
		make.add("configWorld", configWorld);
		
		make.add("EvoTree", tree.toJSON());
		System.out.println("EvoTree готово");
		Vector<Cell> cells = new Vector<>();
		for (Cell[] cell : worldMap) {
			for (Cell cell2 : cell) {
				if(cell2 != null) {
					cells.add(cell2);
				}
			}
		}
		JSONmake[] nodes = new JSONmake[cells.size()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = cells.get(i).toJSON();
		}
		make.add("Cells", nodes);
		System.out.println("Клетки готовы");
		return make;
	}

	public void update(JSONmake jsoNmake) {
		JSONmake configWorld = jsoNmake.getJ("configWorld");
		List<Long> map = configWorld.getAL("MAP_CELLS");
		if(map.get(0) != MAP_CELLS.width || map.get(1) != MAP_CELLS.height) {
			JOptionPane.showMessageDialog(this,
					"<html><h2>Ошибка</h2><i>Загружаемый мир создан на карте другого размера!!!</i><br>Треуется карта " + map.get(0) + "х" + map.get(1),
					"BioLife", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Не верные размеры мира");
		}
		DIRTY_WATER = configWorld.getD("DIRTY_WATER");
		AGGRESSIVE_ENVIRONMENT = configWorld.getD("AGGRESSIVE_ENVIRONMENT");
		LEVEL_MINERAL = configWorld.getD("LEVEL_MINERAL");
		CONCENTRATION_MINERAL = configWorld.getD("CONCENTRATION_MINERAL");
		FPS_TIC = configWorld.getI("FPS_TIC");
		TIK_TO_EXIT = configWorld.getI("TIK_TO_EXIT");
		step = configWorld.getL("step");
		
		
		List<JSONmake> cells = jsoNmake.getAJ("Cells");		
		worldMap = new Cell[MAP_CELLS.width][MAP_CELLS.height];
		for (JSONmake cell : cells) {
			Cell realCell = new Cell(cell);
			add(realCell);
		}
		settings.updateScrols();
	}
}
