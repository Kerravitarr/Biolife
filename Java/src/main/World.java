package main;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.LV_STATUS;
import MapObjects.CellObject.OBJECT;
import MapObjects.Geyser;
import MapObjects.Organic;
import MapObjects.Poison;
import MapObjects.Sun;
import Utils.FPScounter;
import Utils.JSON;
import Utils.Utils;
import main.Point.DIRECTION;

public class World extends JPanel {	
	/**Симуляция запущена?*/
	public static boolean isActiv = true;
	/**Произошла авария?*/
	public static boolean isError = false;
	/**Сколько милисекунд делать перед ходами*/
	public static int msTimeout = 0;
	
	class AutostartThread extends Thread{AutostartThread(Runnable target){super(target);start();}}
	
	class WorldTask implements Runnable{
		class WorkThread{
			public WorkThread(Point[] cells) {
				points = cells;
				startX = points[0].getX();
			}
			/**Точки потока*/
			Point [] points;
			/**Начальный Х потока*/
			int startX;
		}
		
		WorkThread first;
		WorkThread second;
		WorkThread activ = null;
		/**Помогает отслеживать, когда все потоки завершатся*/
		private CountDownLatch mutex;
		WorldTask(Point [] cellsFirst, Point[] cellsSecond){
			first = new WorkThread(cellsFirst);
			second = new WorkThread(cellsSecond);
		}
		public WorldTask start(CountDownLatch barrier, boolean isFirst) {
			activ = isFirst ? first : second;
			this.mutex=barrier;
			return this;
		}
		public void run() {
			//long time = System.nanoTime();
			Thread.currentThread().setName("Row: " + activ.startX);
			/**Перетосовываем, чтобы у всех были равные шансы на ход*/
			shuffle(activ.points);
			for (Point point : activ.points) {
				if(isError)
					break;
				CellObject cell= get(point);
				if(cell != null && cell.canStep(step)) {
					try {
						cell.step(step);
					} catch (Exception e) {
						isError = true;
						isActiv = false;
						e.printStackTrace();
						System.out.println(cell);
						JOptionPane.showMessageDialog(null,	"<html>Критическая ошибка!!!<br>"
								+ "Вызвала клетка "+cell+" с координат" + point + "<br>"
								+ "Описание: " + e.getMessage() + "<br>"
								+ "К сожалению дальнейшее моделирование невозможно. <br>"
								+ "Вы можете сохранить мир и перезагрузить программу.",	"BioLife", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			mutex.countDown();
			//time = System.nanoTime() - time;
			//System.out.println("Строка " + activ.startX + " выполнялась " + df.format(time) + "нс");
		}
	}
	
	/**Шаги мира*/
	public long step = 0;
	/**Специальный таймер, который позволяет замедлить симуляцию*/
	public int timeoutStep = 0;
	/**Всего точек по процессорам - сколько процессоров, в каждом ряду есть у*/
	WorldTask [] cellsTask;
	/**Очередь потоков, которая будет обсчитывать мир*/
	private ExecutorService executor;
	/**Счётчик ФПС*/
	public final FPScounter fps = new FPScounter();
	/**Счётчик шагов*/
	public final FPScounter sps = new FPScounter();
	/**Счётчик ораганики*/
	public int countOrganic = 0;
	/**Счётчик живых*/
	public int countLife = 0;
	/**Счётчик капель яда*/
	public int countPoison = 0;
	/**Все цвета, которые мы должны отобразить на поле*/
	Color [] colors;
	//private static DecimalFormat df = new DecimalFormat( "###,###" );
	/**
	 * Create the panel.
	 */
	public World() {
		super();
		Configurations.world = this;
		executor = Executors.newWorkStealingPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 4));
		
		worldGenerate();
		
		//Начальные клетки
		AliveCell adam = new AliveCell();
		adam.setPos(new Point(Configurations.MAP_CELLS.width/2,0));
		Configurations.tree.setAdam(adam);
		add(adam);
		
		setBackground(new Color(255, 255, 255, 255));

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				double hDel = 1.0 * getHeight() * (1 - (Configurations.UP_border + Configurations.DOWN_border)) / (Configurations.MAP_CELLS.height);
				double wDel = 1.0 * getWidth() / (Configurations.MAP_CELLS.width);
				Configurations.scale = Math.min(hDel, wDel);
				Configurations.border.width = (int) Math.round((getWidth() -Configurations.MAP_CELLS.width*Configurations.scale)/2);
				Configurations.border.height = (int) Math.round((getHeight() - Configurations.MAP_CELLS.height*Configurations.scale)/2);
				Point.update();
				for(Geyser gz : Configurations.geysers)
					gz.updateScreen(getWidth(),getHeight());
				Configurations.sun.resize(getWidth(),getHeight());
			}
		});
		
		addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		    	if(Configurations.info.isVisible()) {
		    		int realX = e.getX();
		    		if(realX < Configurations.border.width || realX > getWidth() - Configurations.border.width)
		    			return;
		    		int realY = e.getY();
		    		if(realY < Configurations.border.height || realY > getHeight() - Configurations.border.height)
		    			return;
		    		realX -= Configurations.border.width;
		    		realY -= Configurations.border.height;
		    		realX = (int) Math.round(realX/Configurations.scale-0.5);
		    		realY = (int) Math.round(realY/Configurations.scale-0.5);
		    		Point point = new Point(realX,realY);
		    		Configurations.info.setCell(get(point));
		    	}
		    }
		});
		
		new AutostartThread(new Runnable() {
			public void run() {
				Thread.currentThread().setName("World thread");
				while(true) {
					if(isActiv && msTimeout == 0)
						step();
					else
						Utils.pause(1);
				}
			}
		});
		
	}
	
	public void worldGenerate() {
		Configurations.geysers = new Geyser[2];
		Configurations.geysers[0]  = new Geyser(Configurations.MAP_CELLS.width * 10 / 40,Configurations.MAP_CELLS.width * 13 / 40,getWidth(),getHeight(),DIRECTION.DOWN,10);
		Configurations.geysers[1]  = new Geyser(Configurations.MAP_CELLS.width * 30 / 40,Configurations.MAP_CELLS.width * 33 / 40,getWidth(),getHeight(),DIRECTION.UP,10);
		Configurations.sun = new Sun(getWidth(),getHeight());
		Point.update();

		/**Сколько рядов уместится в одном процессоре*/
		int lenghtX = 8;
		int countColumn = Configurations.MAP_CELLS.width / lenghtX;
		/**Сколько рядов придётся ещё добавить*/
		int addX = Configurations.MAP_CELLS.width - lenghtX*countColumn;
		
		Point [][] cellsFirst = new Point[countColumn][(lenghtX/2) * Configurations.MAP_CELLS.height];
		Point [][] cellsSecond = new Point[countColumn][(lenghtX/2) * Configurations.MAP_CELLS.height];
		Point [] cellsAdd = new Point[addX * Configurations.MAP_CELLS.height];
		
		for (int proc = 0; proc < countColumn; proc++) {
			for (int x = 0; x < lenghtX; x++) {
				for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
					Point point = new Point(x + proc * lenghtX,y);
					if(x >= lenghtX/2 ) {
						cellsSecond[proc][(x - lenghtX/2)*Configurations.MAP_CELLS.height+y] = point;
					} else {
						cellsFirst[proc][x*Configurations.MAP_CELLS.height+y] = point;
					}
				}
			}
		}
		
		for (int i = 0; i < addX; i++) {
			for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
				cellsAdd[i*Configurations.MAP_CELLS.height+y] = new Point(countColumn * lenghtX + i,y);
			}
		}
		
		
		WorldTask[] cellsTask_l = new WorldTask[cellsFirst.length];
		for (int i = 0; i < cellsTask_l.length; i++)
			cellsTask_l[i] = new WorldTask(cellsFirst[i],cellsSecond[i]);
		if(cellsAdd.length != 0)
			cellsTask_l[cellsTask_l.length-1].second.points = Utils.concat(cellsTask_l[cellsTask_l.length-1].second.points, cellsAdd);
		
		//threads = new Thread[cellsTask_l.length];
		cellsTask=cellsTask_l;
		
		recalculate();

		Configurations.settings.updateScrols();
	}

	public synchronized void step() {
		try {
			boolean flag = Math.random() <= 0.5;
			CountDownLatch barrier = new CountDownLatch(cellsTask.length);
			for (int i = cellsTask.length - 1; i >= 0; i--)
				executor.execute(cellsTask[i].start(barrier,flag));
			barrier.await();
			barrier = new CountDownLatch(cellsTask.length);
			flag = !flag;
			for (int i = cellsTask.length - 1; i >= 0; i--)
				executor.execute(cellsTask[i].start(barrier,flag));
			barrier.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Configurations.sun.step(step);
		Configurations.tree.step();
		
		if(step % 10_000 == 0) {
			executor.shutdownNow();
			executor = Executors.newWorkStealingPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 4));
		}

		step++;
		sps.interapt();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		paintField(g);
		
		if(isActiv && (msTimeout != 0 && timeoutStep % msTimeout == 0)) {
			step();
		}
		int localCl = 0,localCO = 0,localCP = 0;
		for (CellObject[] cell : Configurations.worldMap) {
			for (CellObject cell2 : cell) {
				if(cell2 != null) {
					cell2.paint(g);
					if(cell2.aliveStatus(LV_STATUS.LV_ALIVE))
						localCl++;
					else if(cell2.aliveStatus(LV_STATUS.LV_POISON))
						localCP++;
					else
						localCO++;
				}
			}
		}
		countLife = localCl;
		countOrganic = localCO;
		countPoison = localCP;
		if(Configurations.info.getCell() != null) {
			g.setColor(Color.GRAY);
			g.drawLine(Configurations.info.getCell().getPos().getRx(), Configurations.border.height, Configurations.info.getCell().getPos().getRx(), getHeight()-Configurations.border.height);
			g.drawLine(Configurations.border.width, Configurations.info.getCell().getPos().getRy(), getWidth()-Configurations.border.width, Configurations.info.getCell().getPos().getRy());
		}
		
		timeoutStep++;
		fps.interapt();
		repaint();
	}

	private void paintField(Graphics g) {
		//Небо
		g.setColor(colors[0]);
		g.fillRect(Configurations.border.width, 0, getWidth()-Configurations.border.width*2, Configurations.border.height);
		//Вода
		int lenghtY = getHeight()-Configurations.border.height*2;
		Configurations.sun.paint(g);
        //Минералы
		g.setColor( colors[1]);
		g.fillRect(Configurations.border.width, (int) (Configurations.border.height + lenghtY*Configurations.LEVEL_MINERAL), getWidth()-Configurations.border.width*2, (int) (lenghtY*(1-Configurations.LEVEL_MINERAL)/2));
		g.setColor( colors[1+1]);
		g.fillRect(Configurations.border.width, (int) (getHeight()-Configurations.border.height-(lenghtY*(1-Configurations.LEVEL_MINERAL)/2)), getWidth()-Configurations.border.width*2,(int) (lenghtY*(1-Configurations.LEVEL_MINERAL)/2));
		//Гейзеры
		for(Geyser gz : Configurations.geysers)
			gz.paint(g);

		//Земля
		g.setColor(colors[1+2]);
		g.fillRect(Configurations.border.width, getHeight()-Configurations.border.height, getWidth()-Configurations.border.width*2, Configurations.border.height);
		//paintLine(g);
	}

	@SuppressWarnings("unused")
	private void paintLine(Graphics g) {
		for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
			Point from = new Point(0, y);
			Point to = new Point(Configurations.MAP_CELLS.width - 1, y);
			g.drawLine(from.getRx(), from.getRy(), to.getRx(), to.getRy());
		}
		for (int x = 0; x < Configurations.MAP_CELLS.width; x++) {
			Point from = new Point(x, 0);
			Point to = new Point(x, Configurations.MAP_CELLS.height - 1);
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
		if(point.getY() < 0 || point.getY() >= Configurations.MAP_CELLS.height)
			return OBJECT.WALL;
		CellObject cell = get(point);
		if(cell == null || cell.aliveStatus(LV_STATUS.GHOST))
			return OBJECT.CLEAN;
		else if(cell.aliveStatus(LV_STATUS.LV_ORGANIC))
			return OBJECT.ORGANIC;
		else if(cell.aliveStatus(LV_STATUS.LV_POISON))
			return OBJECT.POISON;
		else
			return OBJECT.BOT;
	}

	public CellObject get(Point point) {
		return Configurations.worldMap[point.getX()][point.getY()];
	}
	
	public void add(CellObject cell) {
		if(get(cell.getPos()) != null) {
			throw new RuntimeException("Объект " + cell + " решил вступть на " + cell.getPos() + ", но тут занято " + get(cell.getPos()) + "!!!");
		} else if(cell.aliveStatus(LV_STATUS.GHOST)) {
			throw new RuntimeException("Требуется добавить " + cell + " только вот он уже мёртв!!! ");
		} else {
			Configurations.worldMap[cell.getPos().getX()][cell.getPos().getY()] = cell;	
		}
	}
	public void clean(Point point) {
		if(get(point) == null) {
			throw new RuntimeException("Объект нужно удалить с " + point + ", да тут свободо, вот в чём проблема!!!");
		}
		Configurations.worldMap[point.getX()][point.getY()] = null;
	}
	public void move(CellObject cell,Point target) {
		clean(cell.getPos());
		cell.setPos(target);
		add(cell);
	}

	public synchronized void recalculate() {
		colors = new Color[2 + 2];
		colors[0] = new Color(224, 255, 255, 255); // небо
		colors[1] = Utils.getHSBColor(270.0 / 360, 0.5, 1.0, 0.5);
		colors[1 + 1] = (Utils.getHSBColor(300.0 / 360, 0.5, 1.0, 0.5));
		colors[1 + 2] = new Color(139, 69, 19, 255);
		Configurations.sun.resize(getWidth(),getHeight());
	}

	public synchronized JSON serelization() {
		JSON make = new JSON();

		JSON configWorld = Configurations.toJSON();
		configWorld.add("step", step);
		make.add("configWorld", configWorld);
		System.out.println("Конфигурация мира - готово");
		
		make.add("EvoTree", Configurations.tree.toJSON());
		System.out.println("Дерево эволюции - готово");
		
		Vector<CellObject> cells = new Vector<>();
		for (CellObject[] cell : Configurations.worldMap) {
			for (CellObject cell2 : cell) {
				if(cell2 != null)
					cells.add(cell2);
			}
		}
		JSON[] nodes = new JSON[cells.size()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = cells.get(i).toJSON();
		}
		make.add("Cells", nodes);
		System.out.println("Объекты на поле - готовы");
		return make;
	}

	public synchronized void update(JSON JSON) {
		JSON configWorld = JSON.getJ("configWorld");
		Configurations.load(configWorld);
		step = configWorld.getL("step");
		System.out.println("Конфигурация мира - готово");
		
		Configurations.tree = new EvolutionTree(JSON.getJ("EvoTree"));
		System.out.println("Дерево эволюции - готово");
		
		List<JSON> cells = JSON.getAJ("Cells");		
		Configurations.worldMap = new CellObject[Configurations.MAP_CELLS.width][Configurations.MAP_CELLS.height];
		for (JSON cell : cells) {
			switch (LV_STATUS.values()[(int)cell.get("alive")]) {
				case LV_ALIVE : {
			    	//Point pos = new Point(cell.getJ("pos"));
					//if(pos.x == MAP_CELLS.width/2 && (pos.y > 100 && pos.y < 120))
						add(new AliveCell(cell,Configurations.tree));
				}break;
				case LV_ORGANIC:
					add(new Organic(cell));
					break;
				case LV_POISON:
					add(new Poison(cell));
					break;
				default:
					System.err.println(cell);
			}
			
			
		}
		System.out.println("Объекты на поле - готовы");
		
		//Когда все сохранены, обновялем список друзей
		for (JSON cell : cells) {
	    	Point pos = new Point(cell.getJ("pos"));
	    	CellObject realCell = get(pos);
	    	if(realCell == null || !(realCell instanceof AliveCell))
	    		continue;

	    	List<JSON> mindL = cell.getAJ("friends");
			AliveCell new_name = (AliveCell) realCell;
	    	for (JSON pointFriend : mindL) {
	    		pos = new Point(pointFriend);
	    		if (get(pos) instanceof AliveCell)
		    		new_name.setFriend((AliveCell)get(pos));
			}
		}
		System.out.println("Друзья - готовы");
		
		Configurations.tree.updatre();
		System.out.println("Эволюционное дерево перестроено");
		
		Configurations.settings.updateScrols();
		System.out.println("Настройки обновлены");
	}
}
