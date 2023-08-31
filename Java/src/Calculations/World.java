package Calculations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.LV_STATUS;
import static MapObjects.CellObject.LV_STATUS.LV_ALIVE;
import static MapObjects.CellObject.LV_STATUS.LV_ORGANIC;
import static MapObjects.CellObject.LV_STATUS.LV_POISON;
import static MapObjects.CellObject.LV_STATUS.LV_WALL;
import MapObjects.CellObject.OBJECT;
import MapObjects.Fossil;
import MapObjects.Organic;
import MapObjects.Poison;
import Utils.FPScounter;
import Utils.JSON;
import Utils.JsonSave;
import Utils.StreamProgressBar;
import Utils.Utils;
import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**Класс, отвечающий за математическую обработку мира
 * @author 
 */
public class World implements Runnable{
	/**Сам мир, каждая его клеточка*/
	private /*final*/ CellObject [][] _WORLD_MAP;
	/**Симуляция запущена?*/
	private STATUS _status = STATUS.STOP;
	/**Фабрика создания потоков обсчёта мира*/
	private final ForkJoinWorkerThreadFactory factory;
	/**Шаги мира*/
	public long step = 0;
	/**Всего точек по процессорам - сколько процессоров, в каждом ряду есть у*/
	private List<WorldTask> cellsTask;
	/**Флаг, показывает какую часть поля обрабатываем (первую или вторую)*/
	private boolean isFirst = false;
	/**Очередь потоков, которая будет обсчитывать мир*/
	private final ExecutorService maxExecutor;
	/**Счётчик шагов. Puls Per Second*/
	public final FPScounter pps = new FPScounter();
	/**Это мы, наш поток, в нём мы и считаем всё, что должны*/
	private final Thread worldThread;
	/**Сумма всех живых объектов на начало текущего шага*/
	private int[] _all_live_cell = new int[CellObject.LV_STATUS.length];
	
	
	/**Возвожное состояние мира*/
	private enum STATUS {STOP,ACTIV_ALL,ACTIV_SLOW,ERROR};
	/**Один блок, состоящий из двух вертекалей, карты*/
	class WorldTask implements Callable<int[]>{
		/**Один вертикальный столбик карты, который обсчитвыает этот поток*/
		class WorkThread{
			/**Точки потока*/
			final Point [] points;
			public WorkThread(Point[] cells) {points = cells;}
		}
		/**Первая вертикаль блока*/
		private final WorkThread first;
		/**Вторая вертикаль блока*/
		private final WorkThread second;
		WorldTask(Point [] cellsFirst, Point[] cellsSecond){first = new WorkThread(cellsFirst);second = new WorkThread(cellsSecond);}
		@Override
		public int[] call(){
			final Point[] points = (isFirst ? first : second).points;
			var count_step = new int[CellObject.LV_STATUS.length];
			for (int i = points.length - 1; i >= 0 && _status != STATUS.ERROR; i--) {
				Point t = points[i];
				if(get(t) == null) continue; //Чего мы будем пустые клетки мешать?
				final var j = Configurations.rnd.nextInt(i+1); // случайный индекс от 0 до i
				//Меняем местами клетки, чтобы каждый раз вызывать их в разной последовательности
				final var p = points[i] = points[j];
				points[j] = t;
				final var cell = get(p);
				action(cell,p);
				count_step[cell.getAlive().ordinal()]++;
			}
			return count_step;
		}
		/** Выполняет обработку шага клетки
		 * @param cell объект, который делает шаг
		 * @param point место, где он находился до начала хода
		 */
		private void action(CellObject cell, Point point){
			if(cell.canStep(step)) {
				try {
					cell.step(step);					
				} catch (Exception e) {
					_status = STATUS.ERROR;
					Logger.getLogger(World.class.getName()).log(Level.WARNING, null, e);
					System.out.println(cell);
					System.out.println(point);
					JOptionPane.showMessageDialog(null,	MessageFormat.format(Configurations.getHProperty(World.class,"error.exception"), cell, point, e.getMessage()),	"BioLife", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	/**Создаёт мир заданных размеров
	 * @param MAP_CELLS размер мира в высоту и ширину
	 */
	public World(Dimension MAP_CELLS) {
		super();
		_WORLD_MAP = new CellObject[MAP_CELLS.width][MAP_CELLS.height];
		factory = new ForkJoinWorkerThreadFactory() {
			private final AtomicInteger branshCount = new AtomicInteger(0);
			@Override
			public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
				final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
				worker.setName("World map thread " + branshCount.incrementAndGet());
				return worker;
			}
		};
		maxExecutor = new ForkJoinPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1), factory, null, true); // Один поток нужен системе для отрисовки
		worldGenerate();
		worldThread = new Thread(this);
		worldThread.start();
	}
	@Override
	public void run() {
		Thread.currentThread().setName("World thread");
 		while (_status != STATUS.ERROR) {
			switch (_status) {
				case ACTIV_ALL -> step();
				case ACTIV_SLOW -> stepSlow();
				default -> Utils.pause(1);
			}
		}
	}
	
	/**Функция, уничтожающая мир и освобождающая все ресурсы*/
	public void destroy(){
		maxExecutor.shutdown();
	}
	/**Создаёт стартовую клетку на поле*/
	public void makeAdam(){
		AliveCell adam = new AliveCell();
		adam.setPos(new Point(Configurations.MAP_CELLS.width/2,0));
		Configurations.tree.setAdam(adam);
		add(adam);
	}
	/**Генерирует карту - добавляет солнце, гейзеры, обновляет константы мира, разбивает мир на потоки процессора */
	private void worldGenerate() {
		var vaxX = Configurations.MAP_CELLS.width;
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
			boolean isF = difX % (2 * columnPerPc_2) < columnPerPc_2;
			for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
				Point point = new Point(x,y);
				if (isF)firstList.add(point);
				else 	secondList.add(point);
			}
		}
		cellsTask.add(new WorldTask(firstList.toArray(Point[]::new),secondList.toArray(Point[]::new)));
	}
	/**Оперции, которые должны быть выполнены до совершения шага мира*/
	private void preStep(){
		isFirst = Configurations.rnd.nextBoolean();
	}
	/**
	 * Один маленький шажок для мира и один огронмый шаг для клеток
	 * Метод синхронизирован, так что за раз сможет походить только один поток
	 * В этом потоке обсчитываются все переменые и прочие нужные вещи, так что
	 * достаточно просто его вызывать и быть уверенным, что походят все и всяк
	 */
	public synchronized void step() {
		preStep();
		try {
			var f = new int[CellObject.LV_STATUS.length];
			for (int st = 0; st < 2; st++) {
				f = maxExecutor.invokeAll(cellsTask)
						.stream()
						.map(a -> {try{return a.get();}catch(InterruptedException | ExecutionException e){ return new int[CellObject.LV_STATUS.length];}})
						.reduce(f, (a,b) -> {for (int i = 0; i < b.length; i++) {a[i] += b[i];}return a;});
				isFirst = !isFirst;
			}
			_all_live_cell = f;
		} catch (InterruptedException e) {
			Logger.getLogger(World.class.getName()).log(Level.WARNING, null, e);
		}
		postStep();
	}
	/**Шажок без использования потоков мира, а за счёт ресурсов вызвавшего его потока */
	public synchronized void stepSlow() {
		preStep();
		var f = new int[CellObject.LV_STATUS.length];
		for (int st = 0; st < 2; st++) {
			for (var t : cellsTask) {
				final var b = t.call();
				for (int i = 0; i < b.length; i++) {f[i] += b[i];}
			}
			isFirst = !isFirst;
		}
		_all_live_cell = f;
		postStep();
	}
	/**Операции, которые должны быть выполнены после шага*/
	private void postStep(){
		Configurations.sun.step(step);
		Configurations.tree.step();

		pps.interapt();
		step++;
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
	/**Получить объект в определённом месте поля
	 * @param point откуда брать объект?
	 * @return объект, который там находится. Или null, если там ни чего нет
	 */
	public CellObject get(Point point) {
		return _WORLD_MAP[point.getX()][point.getY()];
	}
	/**Добавляет определённый объект на карту в то место, куда он хочет
	 * дополнительно проверяя, что данное поле свободно
	 * @param cell объект, который надо добавить
	 */
	public void add(CellObject cell) {
		if(get(cell.getPos()) != null) {
			throw new IllegalArgumentException("Объект " + cell + " решил вступть на " + cell.getPos() + ",но тут занято " + get(cell.getPos()) + "!!!");
		} else if(cell.aliveStatus(LV_STATUS.GHOST)) {
			throw new IllegalArgumentException("Требуется добавить " + cell + " только вот он уже мёртв!!! ");
		} else {
			_WORLD_MAP[cell.getPos().getX()][cell.getPos().getY()] = cell;	
		}
	}
	/**Удаляет объект с карты, с места, которое он занимает
	 * дополнительно проверяя, что данное поле действительно занято этим объектом
	 * @param cell объект, которому больше нет места на поле
	 */
	public void clean(CellObject cell) {
		if(get(cell.getPos()) == null) {
			throw new IllegalArgumentException("Объект нужно удалить с " + cell.getPos() + ", да тут свободо, вот в чём проблема!!!");
		}else if(cell.aliveStatus(LV_STATUS.GHOST)){
			throw new IllegalArgumentException("Объект нужно удалить, но " + cell + " уже мёртв!!!");
		}else {
			_WORLD_MAP[cell.getPos().getX()][cell.getPos().getY()] = null;
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
		_WORLD_MAP = new CellObject[Configurations.MAP_CELLS.width][Configurations.MAP_CELLS.height];
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
				Logger.getLogger(World.class.getName()).log(Level.WARNING, null, e1);
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

	
	/**
	 * Показывает состояние работы мира 
	 * @return true, если включён автоматических ход мира
	 */
	public boolean isActiv(){
		return _status == STATUS.ACTIV_ALL ||  _status == STATUS.ACTIV_SLOW;
	}
	/**Останавливает работу мира*/
	public void stop(){
		if(_status == STATUS.ACTIV_ALL)
			_status = STATUS.STOP;
	}
	/**Запускает работу мира*/
	public void start(){
		if(_status != STATUS.ERROR)
			_status = STATUS.ACTIV_ALL;
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
			for (CellObject[] cell : _WORLD_MAP) {
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
			_WORLD_MAP = new CellObject[Configurations.MAP_CELLS.width][Configurations.MAP_CELLS.height];
			for (JSON cell : cells) {
				try {
					switch (LV_STATUS.values()[(int) cell.get("alive")]) {
						case LV_ALIVE -> {
							//if(loadOneCell(cell,31,31))
								add(new AliveCell(cell, Configurations.tree, version));
						}
						case LV_ORGANIC -> add(new Organic(cell, version));
						case LV_POISON -> add(new Poison(cell, version));
						case LV_WALL -> add(new Fossil(cell, version));
						default -> System.err.println("Ошибка загрузки строки: \n" + cell);
					}
				} catch (java.lang.RuntimeException e1) {
					Logger.getLogger(World.class.getName()).log(Level.WARNING, null, e1);
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
			worldGenerate();
		}
		
	}
	public JsonSave.JSONSerialization serelization(){
		return new JSONSerialization();
	}

}
