package main;
import static main.Configurations.geysers;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.LV_STATUS;
import static MapObjects.CellObject.LV_STATUS.LV_ALIVE;
import static MapObjects.CellObject.LV_STATUS.LV_ORGANIC;
import static MapObjects.CellObject.LV_STATUS.LV_POISON;
import static MapObjects.CellObject.LV_STATUS.LV_WALL;
import MapObjects.CellObject.OBJECT;
import MapObjects.Fossil;
import MapObjects.Geyser;
import MapObjects.Organic;
import MapObjects.Poison;
import MapObjects.Sun;
import Utils.ColorRec;
import Utils.FPScounter;
import Utils.JSON;
import Utils.JsonSave;
import Utils.StreamProgressBar;
import Utils.Utils;
import java.util.concurrent.TimeUnit;
import main.Point.DIRECTION;

public class World extends JPanel implements Runnable,ComponentListener,MouseListener{	
	/**Симуляция запущена?*/
	private boolean isActiv = true;
	/**Произошла авария?*/
	public static boolean isError = false;
	/**Сколько кадров отрисовывать перед ходом. Если = 0, то как можно меньше. Если = 1, то считать всё на одном процессоре, если >1, то ФПС будет во столько раз больше ППС*/
	public static int msTimeout = 0;
	class AutostartThread extends Thread{AutostartThread(Runnable target){super(target);start();}}
	final ForkJoinWorkerThreadFactory factory = new ForkJoinWorkerThreadFactory() {
		private final AtomicInteger branshCount = new AtomicInteger(0);
		@Override
		public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
			worker.setName("World map thread " + branshCount.incrementAndGet());
			return worker;
		}
	};
	class WorldTask implements Callable<Boolean>{
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
		WorldTask(Point [] cellsFirst, Point[] cellsSecond){
			first = new WorkThread(cellsFirst);
			second = new WorkThread(cellsSecond);
		}
		@Override
		public Boolean call(){
			WorkThread activ = isFirst ? first : second;
			Point[] points = activ.points;
			for (int i = points.length - 1; i >= 0 && !isError; i--) {
				Point t = points[i];
				if(get(t) == null) continue; //Чего мы будем пустые клетки мешать?
				int j = Configurations.rnd.nextInt(i+1); // случайный индекс от 0 до i
				points[i] = points[j];
				points[j] = t;
				action(points[i]);
			}
			return null;
		}

		private void action(Point point) throws HeadlessException {
			CellObject cell = get(point);
			if(cell != null && cell.canStep(step)) {
				try {
					cell.step(step);					
				} catch (Exception e) {
					isError = true;
					isActiv = false;
					e.printStackTrace();
					System.out.println(cell);
					System.out.println(point);
					JOptionPane.showMessageDialog(null,	"<html>Критическая ошибка!!!\n"
							+ "Вызвала клетка " + cell + " с координат" + point + "\n"
							+ "Описание: " + e.getMessage() + "\n"
							+ "К сожалению дальнейшее моделирование невозможно. \n"
							+ "Вы можете сохранить мир и перезагрузить программу.",	"BioLife", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	/**Задача выполняемая ежесекундно, для всяких там работ с картой*/
	private class UpdateScrinTask implements Runnable {
		/**Специальная переменная, которая следит, чтобы обновления клеток проходили только во время симуляции*/
		private long stepUpdate = 0;
		@Override
		public void run() {
			int localCl = 0,localCO = 0,localCP = 0, localCW = 0;
			var minH = Configurations.MAP_CELLS.height * (1d  - Configurations.LEVEL_MINERAL);//Высота минирализации
			boolean isUpdate = false;
			for (CellObject[] cellByY : Configurations.worldMap) {
				var coByH = 0;
				var coByHd = 0;
				for (CellObject cell2 : cellByY) {
					if(cell2 == null) continue;
					cell2.repaint();
					if(cell2.aliveStatus(LV_STATUS.LV_ALIVE))
						localCl++;
					else if(cell2.aliveStatus(LV_STATUS.LV_POISON))
						localCP++;
					else if(cell2.aliveStatus(LV_STATUS.LV_WALL))
						localCW++;
					else if(cell2.getPos().getY() > minH)
						coByHd++;
					else
						coByH++;
				}
				if(stepUpdate != step && coByHd > minH / 2){	//Если среди миниралов много органики - схлопываем её
					isUpdate = true;
					for(var i = 1 ; ;i++){
						if (cellByY[Configurations.MAP_CELLS.height - i] instanceof Organic org) {
							if(!org.getPermissionEat()){
								org.setPermissionEat();
								break;
							}
						}
					}
				}
				localCO += coByH + coByHd;
			}
			if(isUpdate)
				stepUpdate = step;
			countLife = localCl;
			countOrganic = localCO;
			countPoison = localCP;
			countWall = localCW;
		}
	}
	
	/**Шаги мира*/
	public long step = 0;
	/**Счётчик количества отрисованных кадров. А ещё это специальный таймер, который позволяет замедлить симуляцию*/
	public int timeoutStep = 0;
	/**Всего точек по процессорам - сколько процессоров, в каждом ряду есть у*/
	private List<WorldTask> cellsTask;
	/**Флаг, показывает какую часть экрана обрабатываем (первую или вторую)*/
	private boolean isFirst = false;
	/**Очередь потоков, которая будет обсчитывать мир*/
	private final ExecutorService maxExecutor;
	/**Счётчик ФПС*/
	public final FPScounter fps = new FPScounter();
	/**Счётчик шагов*/
	public final FPScounter sps = new FPScounter();
	/**Счётчик ораганики*/
	public int countOrganic = 0;
	/**Счётчик живых*/
	public int countLife = 1;
	/**Счётчик капель яда*/
	public int countPoison = 0;
	/**Счётчик стенок*/
	public int countWall = 0;
	/**Все цвета, которые мы должны отобразить на поле*/
	private final ColorRec [] colors = new ColorRec[2];
	/**Это мы, наш поток, в нём мы рисуем всё и всяк*/
	@SuppressWarnings("unused")
	private final AutostartThread worldThread;
	/**Показывает, что остановка была сделана специально для перерисовки*/
	private int repaint_stop = 0;
	/**
	 * Create the panel.
	 */
	public World() {
		super();
		Configurations.world = this;
		maxExecutor = new ForkJoinPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1), factory, null, true); // Один поток нужен системе для отрисовки
		
		worldGenerate();
		
		//Начальные клетки
		AliveCell adam = new AliveCell();
		adam.setPos(new Point(Configurations.MAP_CELLS.width/2,0));
		Configurations.tree.setAdam(adam);
		add(adam);
		
		setBackground(new Color(255, 255, 255, 255));

		addComponentListener(this);
		addMouseListener(this);
		worldThread = new AutostartThread(this);
		Configurations.TIME_OUT_POOL.scheduleWithFixedDelay(new UpdateScrinTask(), 1, 1, TimeUnit.SECONDS);
	}
	@Override
	public void run() {
		Thread.currentThread().setName("World thread");
		while (countLife > 0 || countOrganic > 0 || countPoison > 0 || countWall > 0) {
			if (isActiv && msTimeout < 2)
				step();
			else
				Utils.pause(1);
		}
	}
	
	public void worldGenerate() {
		var vaxX = Configurations.MAP_CELLS.width;
		geysers = new Geyser[2];
		geysers[0]  = new Geyser(vaxX * 1 / 4,vaxX * 1/5,getWidth(),getHeight(),DIRECTION.DOWN,100);
		geysers[1]  = new Geyser(vaxX * 3 / 4,vaxX * 1/10,getWidth(),getHeight(),DIRECTION.UP,2);
		Configurations.sun = new Sun(getWidth(),getHeight());
		Point.update();
		

		//Сколько рядов уместится в одной половине поля потока
		//Это гарантированное расстояние, которое по оси Х клетка не может пройти ни при каких условиях.
		//То есть выйдя с границы своей области, клетка не дойдёт до следующей. Иначе может быть гонка процессов!
		int columnPerPc_2 = Math.max(5, vaxX/100);
		//Сколько нужно дать каждой клетке, чтобы сойтись по итогу
		double insert = 1.0*(vaxX - (vaxX/(columnPerPc_2*2))*(columnPerPc_2*2)) / vaxX;
		cellsTask = new ArrayList<>(vaxX / (columnPerPc_2*2)+columnPerPc_2);
		//Собственно сами точки для клетки
		ArrayList<Point> firstList = new ArrayList<>();
		List<Point> secondList = new ArrayList<>();
		for (int x = 0; x < vaxX; x++) {
			var difX = Math.round(x  - insert*x);
			if(x != 0 && difX % (2 * columnPerPc_2) == 0 && !secondList.isEmpty()) {
				cellsTask.add(new WorldTask(firstList.toArray(Point[]::new),secondList.toArray(Point[]::new)));
				firstList.clear();
				secondList.clear();
			}
			boolean isFirst = difX % (2 * columnPerPc_2) < columnPerPc_2;
			for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
				Point point = new Point(x,y);
				if (isFirst) firstList.add(point);
				else 		 secondList.add(point);
			}
		}
		cellsTask.add(new WorldTask(firstList.toArray(Point[]::new),secondList.toArray(Point[]::new)));
		
		recalculate();
	}
	/**
	 * Один маленький шажок для мира и один огронмый шаг для клеток
	 * Метод синхронизирован, так что за раз сможет походить только один поток
	 * В этом потоке обсчитываются все переменые и прочие нужные вещи, так что
	 * достаточно просто его вызывать и быть уверенным, что походят все и всяк
	 */
	public synchronized void step() {
		try {
			isFirst = Configurations.rnd.nextBoolean();
			
			if(msTimeout == 1) {
				for(var t : cellsTask)
					t.call();
			} else {
				maxExecutor.invokeAll(cellsTask);
			}
			isFirst = !isFirst;
			if(msTimeout == 1) {
				for(var t : cellsTask)
					t.call();
			} else {
				maxExecutor.invokeAll(cellsTask);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Configurations.sun.step(step);
		Configurations.tree.step();

		step++;
		sps.interapt();
	}

    @Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		paintField(g);
		
		if(isActiv && (msTimeout > 1 && timeoutStep % (msTimeout - 1) == 0)) {
			step();
		}
		for (CellObject[] cellByY : Configurations.worldMap) {
			for (CellObject cell2 : cellByY) {
				if(cell2 == null) continue;
				cell2.paint(g);
			}
		}
		if(Configurations.info.getCell() != null) {
			g.setColor(Color.GRAY);
			g.drawLine(Configurations.info.getCell().getPos().getRx(), Configurations.border.height, Configurations.info.getCell().getPos().getRx(), getHeight()-Configurations.border.height);
			g.drawLine(Configurations.border.width, Configurations.info.getCell().getPos().getRy(), getWidth()-Configurations.border.width, Configurations.info.getCell().getPos().getRy());
		}
		
		timeoutStep++;
		fps.interapt();
		repaint();
		if(repaint_stop > 1) {
			repaint_stop--;
		} else if(repaint_stop == 1) {
			repaint_stop--;
			isActiv = true;
		}
	}

	private void paintField(Graphics g) {
		//Небо
		colors[0].paint((Graphics2D) g);
		//Вода
		Configurations.sun.paint((Graphics2D) g);
        //Минералы
		colors[1].paint((Graphics2D) g);
		//Гейзеры
		for(Geyser gz : Configurations.geysers)
			gz.paint(g);

		//Земля
		colors[1].paint((Graphics2D)g);
		//Вспомогательное построение
		//paintLine(g);
		//paintProc(g);
	}

	@SuppressWarnings("unused")
	private void paintProc(Graphics g) {
		for (int y = 0; y < cellsTask.size(); y++) {
			var ct = cellsTask.get(y);
			var color = Utils.getHSBColor(1.0*ct.first.startX / Configurations.MAP_CELLS.width, 1, 1, 0.5);
			g.setColor(color);
			for (var point : ct.first.points) {
				//if(point.getY() == 10 - y%2)
					Utils.fillSquare(g, point.getRx(), point.getRy(), point.getRr());
			}
			color = Utils.getHSBColor(1.0*ct.second.startX / Configurations.MAP_CELLS.width, 1, 1, 0.5);
			g.setColor(color);
			for (var point : ct.second.points) {
				//if(point.getY() == 20 + y%2)
					Utils.fillSquare(g, point.getRx(), point.getRy(), point.getRr());
			}
		}
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
	 * Возвращает тип клетки по указанным координатам
	 * @param point координаты, которые надо протестировать
	 * @return объект OBJECT. ВАЖНОЕ ЗАМЕЧАНИЕ!!!!
	 * 			Вместо OBJECT.FRIEND и OBJECT.ENEMY возвращается OBJECT.BOT
	 * 			Вместо OBJECT.NOT_POISON возвращается OBJECT.POISON
	 */
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
		else if(cell.aliveStatus(LV_STATUS.LV_WALL))
			return OBJECT.OWALL;
		else
			return OBJECT.BOT;
	}

	public CellObject get(Point point) {
		return Configurations.worldMap[point.getX()][point.getY()];
	}
	
	public void add(CellObject cell) {
		if(get(cell.getPos()) != null) {
			throw new RuntimeException("Объект " + cell + " решил вступть на " + cell.getPos() + ",\nно тут занято " + get(cell.getPos()) + "!!!");
		} else if(cell.aliveStatus(LV_STATUS.GHOST)) {
			throw new RuntimeException("Требуется добавить " + cell + " только вот он уже мёртв!!! ");
		} else {
			Configurations.worldMap[cell.getPos().getX()][cell.getPos().getY()] = cell;	
		}
	}
	public void clean(CellObject cell) {
		if(get(cell.getPos()) == null) {
			throw new RuntimeException("Объект нужно удалить с " + cell.getPos() + ", да тут свободо, вот в чём проблема!!!");
		}else if(cell.aliveStatus(LV_STATUS.GHOST)){
			throw new RuntimeException("Объект нужно удалить, но " + cell + " уже мёртв!!!");
		}else {
			Configurations.worldMap[cell.getPos().getX()][cell.getPos().getY()] = null;
		}
	}
	/**
	 * Перемещает клетку в новую позицию
	 * @param cell клетка
	 * @param target её новая позиция
	 */
	public void move(CellObject cell,Point target) {
		clean(cell);
		cell.setPos(target);
		add(cell);
	}
	/**
	 * Меняет текущее местоположение клетки и желаемую цель местами
	 * @param cell клетка
	 * @param target с какой позицией она хочет обменяться местами
	 */
	public void swap(CellObject cell,Point target) {
		var t = get(target);
		if(t == null){
			move(cell,target);
		} else {
			var d = Point.direction(target, cell.getPos());
			clean(cell);
			if(t.move(d)){	//Если перемещение удачное
				clean(t);
				add(cell);	//Тогда цель уже в нужной позиции, осталость клетку поменять
				if(cell.move(d.inversion())){
					add(t);	//Если клетка сдвинулась тоже - возвращаем цель и выходим
				} else {
					clean(cell); //Иначе удаляем клетку
					add(t);
					t.move(d.inversion()); //Откатываем цель ((клетку удаляем, потому что цель на месте клетки сейчас))
					add(cell);	//И возваращем клетку на свободное место
				}
			} else {
				add(cell);
			}
		}
		
	}

	public synchronized void recalculate() {
		var oActiv = isActiv;
		if(isActiv) {
			isActiv = false;
			repaint_stop = 5;
		}
		//Пересчёт размера мира
		double hDel = ((double) getHeight()) * (1 - (Configurations.UP_border + Configurations.DOWN_border)) / (Configurations.MAP_CELLS.height);
		double wDel = ((double) getWidth()) / (Configurations.MAP_CELLS.width);
		Configurations.scale = Math.min(hDel, wDel);
		Configurations.border.width = (int) Math.round((getWidth() - Configurations.MAP_CELLS.width*Configurations.scale)/2);
		Configurations.border.height = (int) Math.round((getHeight() - Configurations.MAP_CELLS.height*Configurations.scale)/2);
		Point.update();
		for(Geyser gz : Configurations.geysers)
			gz.updateScreen(getWidth(),getHeight());
		Configurations.sun.resize(getWidth(), getHeight());
		
		updateScrin();
		
		if(oActiv && !isActiv)
			isActiv = true;
	}

	/**Пересчитывает позиции всех объектов*/
	public void updateScrin() {
		//Верхнее небо
		int xs[] = new int[4];
		int ys[] = new int[4];
		//Минералы
		int xm[] = new int[4];
		int ym[] = new int[4];
		//Дно
		int xb[] = new int[4];
		int yb[] = new int[4];
		
		xs[0] = xs[1] = xm[0] = xm[1] = xb[0] = xb[1] = Point.getRx(0);
		xs[2] = xs[3] = xm[2] = xm[3] = xb[2] = xb[3] = Point.getRx(Configurations.MAP_CELLS.width);
		
		ys[0] = ys[3] = 0;
		ys[1] = ys[2] = Point.getRy(0);
		ym[0] = ym[3] = Point.getRy((int) (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL));
		ym[1] = ym[2] = yb[0] = yb[3] = Point.getRy(Configurations.MAP_CELLS.height - 1);
		yb[1] = yb[2] = getHeight();
		
		colors[0] = new ColorRec(xs,ys, new Color(224, 255, 255, 255));
		colors[1] = new ColorRec(xm,ym, new GradientPaint(getWidth(), ym[0], Utils.getHSBColor(240d / 360, 1, 1, 0.7), getWidth(), ym[1], Utils.getHSBColor(300.0 / 360, 1d, 1d, 0.7)));
		colors[1] = new ColorRec(xb,yb, new Color(139, 69, 19, 255));
	}


	public synchronized void update(JSON json) {
		StreamProgressBar sb = new StreamProgressBar();
		sb.addEvent("Загрузка началась");
		sb.addEvent("Конфигурация мира - загружено");
		sb.addEvent("Дерево эволюции - загружено");
		sb.addEvent("Объекты на поле - загружено");
		sb.addEvent("Друзья - загружено");
		sb.addEvent("Эволюционное дерево перестроено");
		sb.addEvent("Настройки обновлены");
		sb.addEvent("Загрузка заверешена");
		
		sb.event();
		
		var version = json.getL("VERSION");
		JSON configWorld = json.getJ("configWorld");
		new Configurations().setJSON(configWorld,version);
		step = configWorld.getL("step");
		sb.event();
		
		Configurations.tree.setJSON(json.getJ("EvoTree"),version);
		sb.event();
		
		List<JSON> cells = json.getAJ("Cells");		
		Configurations.worldMap = new CellObject[Configurations.MAP_CELLS.width][Configurations.MAP_CELLS.height];
		for (JSON cell : cells) {
			try {
				switch (LV_STATUS.values()[(int)cell.get("alive")]) {
					case LV_ALIVE -> {
						//if(loadR(cell,516,148,30))
							add(new AliveCell(cell,Configurations.tree,version));
					}
					case LV_ORGANIC -> add(new Organic(cell,version));
					case LV_POISON -> add(new Poison(cell,version));
					case LV_WALL -> add(new Fossil(cell,version));
					default -> System.err.println("Ошибка загрузки строки: \n" + cell);
				}
			} catch (java.lang.RuntimeException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null,	"<html>Ошибка загрузки!<br>" + e1.getMessage() + "<br>Для объекта<br>"+cell.toJSONString(),	"BioLife", JOptionPane.ERROR_MESSAGE);
				throw e1;
			}
		}
		sb.event();
		
		//Когда все сохранены, обновялем список друзей
		for (JSON cell : cells) {
			if(!cell.containsKey("friends")) continue; // Мы не клетка
			if(cell.getAJ("friends").isEmpty()) continue; // У нас нет друзей
	    	Point pos = new Point(cell.getJ("pos"));
	    	CellObject realCell = get(pos);
	    	if(realCell == null || !(realCell instanceof AliveCell))
	    		continue;
	    	List<JSON> mindL = cell.getAJ("friends");
			AliveCell new_name = (AliveCell) realCell;
	    	for (JSON pointFriend : mindL) {
	    		pos = new Point(pointFriend);
	    		if (get(pos) instanceof AliveCell aliveCell)
		    		new_name.setFriend(aliveCell);
			}
		}
		sb.event();
		
		Configurations.tree.updatre();
		sb.event();
		
		worldGenerate();
		sb.event();
		sb.event();
	}
	
	/**
	 * Загружает только одну клетку по координатам
	 * @param cell описание клетки
	 * @param x координата Х
	 * @param y координата У
	 * @return true только для подходящей клетки
	 */
	@SuppressWarnings("unused")
	private boolean loadOneCell(JSON cell, int x, int y) {
		Point pos = new Point(cell.getJ("pos"));
		return pos.getX() == x && pos.getY() == y;
	}
	/**
	 * Загружает только один столбец
	 * @param cell описание клетки
	 * @param x координата Х
	 * @return true только для подходящей клетки
	 */
	@SuppressWarnings("unused")
	private boolean loadColumn(JSON cell, int x) {
		Point pos = new Point(cell.getJ("pos"));
		return pos.getX() == x;
	}
	/**
	 * Загружает все клетки в радиусе
	 * @param cell описание клетки
	 * @param x координата Х
	 * @param y координата У
	 * @param r радиус
	 * @return true только для подходящей клетки
	 */
	@SuppressWarnings("unused")
	private boolean loadR(JSON cell,  int x, int y, int r) {
		Point pos = new Point(cell.getJ("pos"));
		int delx = Math.abs(pos.getX() - x);
		int dely = Math.abs(pos.getY() - y);
		return dely <= r && (delx <= r || delx >= (Configurations.MAP_CELLS.width-r));
	}

	/**
	 * Загружает клетку и её соседенй по координатам
	 * @param cell описание клетки
	 * @param x координата Х
	 * @param y координата У
	 * @return true только для подходящей клетки
	 */
	@SuppressWarnings("unused")
	private boolean loadNineCell(JSON cell, int x, int y) {
		return loadR(cell,x,y,1);
	}
	
	
	

	@Override
	public void componentMoved(ComponentEvent e) {}
	@Override
	public void componentShown(ComponentEvent e) {}
	@Override
	public void componentHidden(ComponentEvent e) {}
	@Override
	public void componentResized(ComponentEvent e) {
		recalculate();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	
	/**
	 * Пересчитыавет координаты мировые в пикселях в координаты ячейки
	 * @param x мировая координата х
	 * @param y мировая координата у
	 * @return точку в реальном пространстве или null, если эта точка за гранью
	 */
	private Point recalculation(int x, int y) {
		if(x < Configurations.border.width || x > getWidth() - Configurations.border.width)
			return null;
		if(y < Configurations.border.height || y > getHeight() - Configurations.border.height)
			return null;
		x -= Configurations.border.width;
		y -= Configurations.border.height;
		x = (int) Math.round(x/Configurations.scale-0.5);
		y = (int) Math.round(y/Configurations.scale-0.5);
		return new Point(x,y);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(Configurations.menu.isVisible()) {
			Point point = recalculation(e.getX(),e.getY());
			if(point != null)
				Configurations.menu.setCell(get(point));
		}
		if(Configurations.info.isVisible()) {
			Point point = recalculation(e.getX(),e.getY());
			if(point != null)
				Configurations.info.setCell(get(point));
		}
	}
	
	/**
	 * Показывает состояние работы мира 
	 * @return true, если включён автоматических ход мира
	 */
	public boolean isActiv(){
		return isActiv;
	}
	/**Останавливает работу мира*/
	public void stop(){
		isActiv = false;
	}
	/**Запускает работу мира*/
	public void start(){
		isActiv = true;
	}
	private class JSONSerialization extends JsonSave.JSONSerialization{

		@Override
		public String getName() {
			return "WORLD";
		}

		@Override
		public JSON getJSON() {
			JSON make = new JSON();
			var cells = new ArrayList<CellObject>();
			for (CellObject[] cell : Configurations.worldMap) {
				for (CellObject cell2 : cell) {
					if (cell2 != null)
						cells.add(cell2);
				}
			}
			JSON[] nodes = new JSON[cells.size()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = cells.get(i).toJSON();
			}
			make.add("step", step);
			make.add("Cells", nodes);
			return make;
		}

		@Override
		public void setJSON(JSON json, long version) {
			step = json.getL("step");
			List<JSON> cells = json.getAJ("Cells");
			Configurations.worldMap = new CellObject[Configurations.MAP_CELLS.width][Configurations.MAP_CELLS.height];
			for (JSON cell : cells) {
				try {
					switch (LV_STATUS.values()[(int) cell.get("alive")]) {
						case LV_ALIVE -> {
							//if(loadR(cell,516,148,30))
							add(new AliveCell(cell, Configurations.tree, version));
						}
						case LV_ORGANIC -> add(new Organic(cell, version));
						case LV_POISON -> add(new Poison(cell, version));
						case LV_WALL -> add(new Fossil(cell, version));
						default -> System.err.println("Ошибка загрузки строки: \n" + cell);
					}
				} catch (java.lang.RuntimeException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "<html>Ошибка загрузки!<br>" + e1.getMessage() + "<br>Для объекта<br>" + cell.toJSONString(), "BioLife", JOptionPane.ERROR_MESSAGE);
					throw e1;
				}
			}

			//Когда все сохранены, обновялем список друзей
			for (JSON cell : cells) {
				if (!cell.containsKey("friends")) continue; // Мы не клетка
				if (cell.getAJ("friends").isEmpty()) continue; // У нас нет друзей
				Point pos = new Point(cell.getJ("pos"));
				CellObject realCell = get(pos);
				if (realCell == null || !(realCell instanceof AliveCell))
					continue;
				List<JSON> mindL = cell.getAJ("friends");
				AliveCell new_name = (AliveCell) realCell;
				for (JSON pointFriend : mindL) {
					pos = new Point(pointFriend);
					if (get(pos) instanceof AliveCell aliveCell)
						new_name.setFriend(aliveCell);
				}
			}
			Configurations.tree.updatre();
		}
		
	}
	public JsonSave.JSONSerialization serelization(){
		return new JSONSerialization();
	}

}
