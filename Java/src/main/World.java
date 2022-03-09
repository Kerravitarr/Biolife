package main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.Organic;
import MapObjects.Sun;
import MapObjects.CellObject.LV_STATUS;
import MapObjects.CellObject.OBJECT;
import MapObjects.Geyser;
import Utils.FPScounter;
import Utils.JSONmake;
import Utils.Utils;
import main.Point.DIRECTION;
import panels.BotInfo;
import panels.Settings;

public class World extends JPanel {	
	/**Количиство ячеек карты*/
	public static final Dimension MAP_CELLS = new Dimension(500,200);
	/**Освещённость карты*/
	public static int SUN_POWER = 10;
	/**Уровень загрязнения воды*/
	public static double DIRTY_WATER = 17;
	/**Степень мутагенности воды*/
	public static double AGGRESSIVE_ENVIRONMENT = 0.25;
	/**Как глубоко лежат минералы*/
	public static double LEVEL_MINERAL = 0.50;
	/**Концентрация минералов*/
	public static double CONCENTRATION_MINERAL = 1;
	//Как долго разлагается органика
	public static int TIK_TO_EXIT = 2;
	/**Симуляция запущена?*/
	public static boolean isActiv = true;
	/**Сколько милисекунд делать перед ходами*/
	public static int msTimeout = 0;
	
	/**Глобальный мир!*/
	public static World world = null;
	/**Солнце нашего мира*/
	public static Sun sun;
	/**Гейзер, некая область где вода поднимается снизу вверх*/
	public static Geyser[] geysers;
	/**Эволюция ботов нашего мира*/
	static EvolutionTree tree = new EvolutionTree();
	
	class AutostartThread extends Thread{AutostartThread(Runnable target){super(target);start();}}
	
	class WorldTask implements Runnable{
		Point [] cellsFirst;
		Point [] cellsSecond;
		Point [] activCell = null;
		WorldTask(Point [] cellsFirst, Point[] cellsSecond){
			this.cellsFirst= cellsFirst;
			this.cellsSecond= cellsSecond;
		}
		public WorldTask start(boolean isFirst) {
			activCell = isFirst ? cellsFirst : cellsSecond;
			return this;
		}
		public void run() {
			/**Перетосовываем, чтобы у всех были равные шансы на ход*/
			shuffle(activCell);
			for (Point point : activCell) {
				CellObject cell= get(point);
				if(cell != null && cell.canStep(step)) {
					try {
						cell.step(step);
					} catch (Exception e) {
						e.printStackTrace();
						isActiv = false;
						System.out.println(cell);
					}
				}
			}
		}
	}
	
	/**Шаги мира*/
	public long step = 0;
	/**Специальный таймер, который позволяет замедлить симуляцию*/
	public int timeoutStep = 0;
	/**Всего точек по процессорам - сколько процессоров, в каждом ряду есть у*/
	WorldTask [] cellsTask;
	Thread [] threads;
	/**Масштаб*/
	public static double scale = 1;
	/**Высота "неба"*/
	double UP_border = 0.01;
	/**Высота "земли"*/
	double DOWN_border = 0.01;
	/**Дополнительный край из-за не совершенства арены*/
	public static Dimension border = new Dimension();
	/**Сам мир*/
	CellObject [][] worldMap = new CellObject[MAP_CELLS.width][MAP_CELLS.height];
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
	/**
	 * Create the panel.
	 */
	public World(BotInfo botInfo,Settings settings) {
		super();
		world = this;
		info = botInfo;
		this.settings=settings;
		
		worldGenerate();
		
		//Начальные клетки
		AliveCell adam = new AliveCell();
		adam.setPos(new Point(MAP_CELLS.width/2,0));
		EvolutionTree.root.countAliveCell = 1;
		adam.evolutionNode = EvolutionTree.root;
		add(adam);
		
		setBackground(new Color(255, 255, 255, 255));

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				double hDel = 1.0 * getHeight() * (1 - (UP_border + DOWN_border)) / (MAP_CELLS.height);
				double wDel = 1.0 * getWidth() / (MAP_CELLS.width);
				scale = Math.min(hDel, wDel);
				border.width = (int) Math.round((getWidth() -MAP_CELLS.width*scale)/2);
				border.height = (int) Math.round((getHeight() - MAP_CELLS.height*scale)/2);
				for(Geyser gz : geysers)
					gz.updateScreen(getWidth(),getHeight());
				sun.resize(getWidth(),getHeight());
				Point.update();
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
					if(isActiv && msTimeout == 0)
						step();
					else
						Utils.pause(1);
				}
			}
		});
		
	}
	
	private void worldGenerate() {
		geysers = new Geyser[3];
		geysers[0]  = new Geyser(0,World.MAP_CELLS.width * 3 / 40,getWidth(),getHeight(),DIRECTION.DOWN,10);
		geysers[1]  = new Geyser(World.MAP_CELLS.width * 3 / 40,World.MAP_CELLS.width * 6 / 40,getWidth(),getHeight(),DIRECTION.UP,10);
		geysers[2]  = new Geyser(World.MAP_CELLS.width * 6 / 40,World.MAP_CELLS.width * 9 / 40,getWidth(),getHeight(),DIRECTION.DOWN,10);
		sun = new Sun(getWidth(),getHeight());
		Point.update();
		
		int countProc = Runtime.getRuntime().availableProcessors()*2/3;
		while(MAP_CELLS.width - 2*countProc < countProc)
			countProc--;
		
		/**Сколько рядов уместится в одном процессоре*/
		int lenghtX = MAP_CELLS.width / countProc;
		/**Сколько рядов придётся ещё добавить*/
		int addX = MAP_CELLS.width - lenghtX*countProc;
		
		Point [][] cellsFirst = new Point[countProc][(lenghtX/2) * MAP_CELLS.height];
		Point [][] cellsSecond = new Point[countProc][(lenghtX/2 + (lenghtX%2!=0?1:0)) * MAP_CELLS.height];
		Point [] cellsAdd = new Point[addX * MAP_CELLS.height];
		
		for (int proc = 0; proc < countProc; proc++) {
			for (int x = 0; x < lenghtX; x++) {
				for (int y = 0; y < MAP_CELLS.height; y++) {
					Point point = new Point(x + proc * lenghtX,y);
					if(x >= lenghtX/2 ) {
						cellsSecond[proc][(x - lenghtX/2)*MAP_CELLS.height+y] = point;
					} else {
						cellsFirst[proc][x*MAP_CELLS.height+y] = point;
					}
				}
			}
		}
		
		for (int i = 0; i < addX; i++) {
			for (int y = 0; y < MAP_CELLS.height; y++) {
				cellsAdd[i*MAP_CELLS.height+y] = new Point(countProc * lenghtX + i,y);
			}
		}
		
		cellsTask = new WorldTask[cellsFirst.length];
		for (int i = 0; i < cellsTask.length; i++)
			cellsTask[i] = new WorldTask(cellsFirst[i],cellsSecond[i]);
		if(cellsAdd.length != 0)
			cellsTask[cellsTask.length-1].cellsSecond = Utils.concat(cellsTask[cellsTask.length-1].cellsSecond, cellsAdd);
		threads = new Thread[cellsTask.length];
		
		recalculate();

		this.settings.updateScrols();
	}

	public void step() {
		try {
			boolean flag = Math.random() <= 0.5;
			for (int i = 0; i < threads.length; i++)
				threads[i] = new AutostartThread(cellsTask[i].start(flag));
			for (Thread i : threads)
				i.join();
			flag = !flag;
			for (int i = 0; i < threads.length; i++)
				threads[i] = new AutostartThread(cellsTask[i].start(flag));
			for (Thread i : threads)
				i.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sun.step(step);

		step++;
		sps.interapt();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		paintField(g);
		
		if(isActiv && (msTimeout != 0 && timeoutStep % msTimeout == 0)) {
			step();
		}
		int localCl = 0,localCO = 0;
		for (CellObject[] cell : worldMap) {
			for (CellObject cell2 : cell) {
				if(cell2 != null) {
					cell2.paint(g);
					if(cell2.aliveStatus(LV_STATUS.LV_ALIVE))
						localCl++;
					else
						localCO++;
				}
			}
		}
		countLife = localCl;
		countOrganic = localCO;
		if(info.getCell() != null) {
			g.setColor(Color.GRAY);
			g.drawLine(info.getCell().getPos().getRx(), border.height, info.getCell().getPos().getRx(), getHeight()-border.height);
			g.drawLine(border.width, info.getCell().getPos().getRy(), getWidth()-border.width, info.getCell().getPos().getRy());
		}
		
		timeoutStep++;
		fps.interapt();
		repaint();
	}

	private void paintField(Graphics g) {
		//Небо
		g.setColor(colors[0]);
		g.fillRect(border.width, 0, getWidth()-border.width*2, border.height);
		//Вода
		int lenghtY = getHeight()-border.height*2;
		sun.paint(g);
        //Минералы
		g.setColor( colors[1]);
		g.fillRect(border.width, (int) (border.height + lenghtY*LEVEL_MINERAL), getWidth()-border.width*2, (int) (lenghtY*(1-LEVEL_MINERAL)/2));
		g.setColor( colors[1+1]);
		g.fillRect(border.width, (int) (getHeight()-border.height-(lenghtY*(1-LEVEL_MINERAL)/2)), getWidth()-border.width*2,(int) (lenghtY*(1-LEVEL_MINERAL)/2));
		//Гейзеры
		for(Geyser gz : geysers)
			gz.paint(g);

		//Земля
		g.setColor(colors[1+2]);
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
	private <T> void  shuffle(T[] array) {
		for (int i = array.length - 1; i > 0; i--) {
			int j = (int) Math.floor(Math.random() * (i + 1)); // случайный индекс от 0 до i
			T t = array[i];
			array[i] = array[j];
			array[j] = t;
		}
	}

	public OBJECT test(Point point) {
		if(point.y < 0 || point.y >= MAP_CELLS.height)
			return OBJECT.WALL;
		CellObject cell = get(point);
		if(cell == null || cell.aliveStatus(LV_STATUS.GHOST))
			return OBJECT.CLEAN;
		else if(cell.aliveStatus(LV_STATUS.LV_ORGANIC))
			return OBJECT.ORGANIC;
		else
			return OBJECT.BOT;
	}

	public CellObject get(Point point) {
		return worldMap[point.x][point.y];
	}
	
	public void add(CellObject cell) {
		worldMap[cell.getPos().x][cell.getPos().y] = cell;
	}
	public void clean(Point point) {
		worldMap[point.x][point.y] = null;
	}

	public void recalculate() {
		colors = new Color[2 + 2];
		colors[0] = new Color(224, 255, 255, 255); // небо
		colors[1] = Utils.getHSBColor(270.0 / 360, 0.5, 1.0, 0.5);
		colors[1 + 1] = (Utils.getHSBColor(300.0 / 360, 0.5, 1.0, 0.5));
		colors[1 + 2] = new Color(139, 69, 19, 255);
		sun.resize(getWidth(),getHeight());
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
		configWorld.add("TIK_TO_EXIT", TIK_TO_EXIT);
		configWorld.add("step", step);
		make.add("configWorld", configWorld);
		
		make.add("EvoTree", tree.toJSON());
		System.out.println("EvoTree готово");
		Vector<CellObject> cells = new Vector<>();
		for (CellObject[] cell : worldMap) {
			for (CellObject cell2 : cell) {
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
		TIK_TO_EXIT = configWorld.getI("TIK_TO_EXIT");
		step = configWorld.getL("step");
		
		tree = new EvolutionTree(jsoNmake.getJ("EvoTree"));
		
		List<JSONmake> cells = jsoNmake.getAJ("Cells");		
		worldMap = new CellObject[MAP_CELLS.width][MAP_CELLS.height];
		for (JSONmake cell : cells) {
			switch (LV_STATUS.values()[cell.getI("alive")]) {
				case LV_ALIVE : {
			    	//Point pos = new Point(cell.getJ("pos"));
					//if(pos.x == MAP_CELLS.width/2 && (pos.y > 100 && pos.y < 120))
						add(new AliveCell(cell,tree));
				}break;
				case LV_ORGANIC:
					add(new Organic(cell));
					break;
				default:
					System.err.println(cell);
			}
			
			
		}
		//Когда все сохранены, обновялем список друзей
		for (JSONmake cell : cells) {
	    	Point pos = new Point(cell.getJ("pos"));
	    	CellObject realCell = get(pos);
	    	if(realCell == null || !(realCell instanceof AliveCell))
	    		continue;

	    	List<JSONmake> mindL = cell.getAJ("friends");
			AliveCell new_name = (AliveCell) realCell;
	    	for (JSONmake pointFriend : mindL) {
	    		pos = new Point(pointFriend);
	    		if (get(pos) instanceof AliveCell)
		    		new_name.setFriend((AliveCell)get(pos));
			}
		}
		
		tree.updatre();
		
		settings.updateScrols();
	}
}
