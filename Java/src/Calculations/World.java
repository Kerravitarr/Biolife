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
import Utils.SaveAndLoad;
import Utils.Utils;
import java.awt.Dimension;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**Класс, отвечающий за математическую обработку мира
 * @author 
 */
public class World implements Runnable,SaveAndLoad.Serialization{
	/**Сам мир, каждая его клеточка*/
	private final CellObject [][] _WORLD_MAP;
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
	/**Указывает, что программа находится в цикле шага*/
	private boolean isStepCucle = false;
	/**Как часто производить расчёты*/
	private long timeout = 0;
	
	
	/**Возвожное состояние мира*/
	private enum STATUS {STOP,ACTIV_ALL,ERROR};
	/**Один блок, состоящий из двух вертекалей, карты*/
	class WorldTask implements Callable<int[]>{
		/**Один вертикальный столбик карты, который обсчитвыает этот поток*/
		/**Первая вертикаль блока*/
		private final Point [] first;
		/**Вторая вертикаль блока*/
		private final Point [] second;
		WorldTask(Point [] cellsFirst, Point[] cellsSecond){first = cellsFirst;second = cellsSecond;}
		@Override
		public int[] call(){
			final var points = (isFirst ? first : second);
			final var count_step = new int[CellObject.LV_STATUS.length];
			for (int i = points.length - 1; i >= 0 && _status != STATUS.ERROR; i--) {
				final var t = points[i];
				if(!t.valid() || get(t) == null) continue; //Чего мы будем пустые клетки мешать?
				final var j = Configurations.rnd.nextInt(i+1); // случайный индекс от 0 до i
				//Меняем местами клетки, чтобы каждый раз вызывать их в разной последовательности
				final var p = points[i] = points[j];
				points[j] = t;
				final var cell = get(p);
				if(cell == null) continue; //Аай, и тут пусто. Ну и ладно
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
			//cell.addHealth(100 - cell.getHealth());
			if(cell.canStep(step)) {
				try {
					cell.step(step);					
				} catch (Throwable e) {
					_status = STATUS.ERROR;
					final var errMsg = Configurations.getHProperty(World.class,"error.exception", cell, point, e.getMessage());
					Logger.getLogger(World.class.getName()).log(Level.WARNING, errMsg, e);
					JOptionPane.showMessageDialog(null,	errMsg,	"BioLife", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	/**Создаёт мир заданных размеров
	 * @param MAP_CELLS размер мира в высоту и ширину
	 */
	public World(Dimension MAP_CELLS) {
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
		maxExecutor = new ForkJoinPool(Math.max(1, Runtime.getRuntime().availableProcessors()*2), factory, null, true); // Один поток нужен системе для отрисовки
		worldGenerate();
		worldThread = new Thread(this);
		worldThread.start();
	}
	/**Создание мира на основе JSON
	 * @param json
	 * @param version 
	 * @param MAP_CELLS размер мира в высоту и ширину
	 */
	public World(JSON json, long version, Dimension MAP_CELLS) {
		this(MAP_CELLS);
		step = json.getL("step");
		List<JSON> cells = json.getAJ("Cells");
		for (JSON cell : cells) {
			try {
				var t = LV_STATUS.values[cell.getI("alive")];
				switch (t) {
					case LV_ALIVE -> {
						//if(loadOneCell(cell,31,31))
							add(new AliveCell(cell, Configurations.tree, version));
					}
					case LV_ORGANIC -> add(new Organic(cell, version));
					case LV_POISON -> add(new Poison(cell, version));
					case LV_WALL -> add(new Fossil(cell, version));
					default -> System.err.println("Ошибка загрузки строки: \n" + cell);
				}
				_all_live_cell[t.ordinal()]++;
			} catch (java.lang.RuntimeException e1) {
				Logger.getLogger(World.class.getName()).log(Level.WARNING, e1.getLocalizedMessage(), e1);
				JOptionPane.showMessageDialog(null, "<html>Ошибка загрузки!<br>" + e1.getMessage() + "<br>Для объекта<br>" + cell.toJSONString(), "BioLife", JOptionPane.ERROR_MESSAGE);
				throw e1;
			}
		}

		//Когда все сохранены, обновялем список друзей
		for (JSON cell : cells) {
			if (!cell.containsKey("friends")) continue; // Мы не клетка
			if (cell.getAJ("friends").isEmpty()) continue; // У нас нет друзей
			Point pos = Point.create(cell.getJ("pos"));
			CellObject realCell = get(pos);
			if (realCell == null || !(realCell instanceof AliveCell))
				continue;
			List<JSON> mindL = cell.getAJ("friends");
			AliveCell new_name = (AliveCell) realCell;
			for (JSON pointFriend : mindL) {
				final var posFriend = Point.create(pointFriend);
				if (get(posFriend) instanceof AliveCell aliveCell) {
					new_name.setComrades(aliveCell);
				} else {
					Logger.getLogger(World.class.getName()).log(Level.WARNING, cell.toString());
				}
			}
		}
		Configurations.tree.updatre();
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName("World thread");
 		while (_status != STATUS.ERROR) {
			switch (_status) {
				case ACTIV_ALL -> step();
				default -> Utils.pause(1);
			}
		}
	}
	
	/**Функция, уничтожающая мир и освобождающая все ресурсы*/
	public void destroy(){
		awaitStop();
		maxExecutor.shutdown();
		_status = STATUS.ERROR;
		pps.close();
	}
	/**Создаёт стартовую клетку на поле*/
	public void makeAdam(){
		AliveCell adam = new AliveCell();
		switch (Configurations.confoguration.world_type) {
			case LINE_H -> adam.setPos(Point.create(Configurations.getWidth()/2, 0));
			case LINE_V -> adam.setPos(Point.create(Configurations.getWidth()*3/4, Configurations.getHeight()/2));
			default -> throw new AssertionError();
		}
		add(adam);
		_all_live_cell[LV_ALIVE.ordinal()] += 1;
	}
	/**Генерирует карту - добавляет солнце, гейзеры, обновляет константы мира, разбивает мир на потоки процессора */
	private void worldGenerate() {
		var vaxX = Configurations.getWidth();
		//Мы будем считать клетки одновременно.
		//При этом, нам надо, чтобы две соседние клетки не пошли одновременно
		//Иначе они могут съесть друг друга... Парадокс!
		//Значит каждая активная клетка * должна быть окружена пассивными Х
		//В тоже время конструкция Х*Х*Х*Х быть не может. Две сосдение клетки могут съесть
		//Одну и туже клетку. Значит получается, что должно быть Х*Х|Х*Х|Х*Х. Другими словами. Каждая
		//Клетка должна быть окружена с двух сторон не ходящими клетками. Но
		//Лучше потоки представить по другому: ХХ|**| ХХ|**| - тогда у нас получается, что в одном потоке будет 2 столба
		//И тогда будут ходить:
		//		Этап 1				Этап 2
		// |ХХ|**|ХХ|**|		|**|ХХ|**|ХХ|
		// |ХХ|**|ХХ|**|	->	|**|ХХ|**|ХХ|
		// |ХХ|**|ХХ|**|		|**|ХХ|**|ХХ|
		
		//Гипотетически, такую штуку можно провернуть в обоих плоскостях. Но тогда получится, что на каждый поток будет по 4 клетки:
		//		Этап 1						Этап 2				Этап 3					Этап 4
		// *_|_ХХ_|_**_|_ХХ_|_*		X_|_**_|_XX_|_**_|_X	X_|_ХХ_|_XX_|_ХХ_|_X	X_|_ХХ_|_XX_|_ХХ_|_X	1_|_22_|_11_|_22_|_1
		// Х | ХХ | ХХ | ХХ | Х		Х | ХХ | ХХ | ХХ | Х	* | ХХ | ** | ХХ | *	Х | ** | ХХ | ** | Х	3 | 44 | 33 | 44 | 3
		// Х_|_ХХ_|_ХХ_|_ХХ_|_Х		Х_|_ХХ_|_ХХ_|_ХХ_|_Х	*_|_ХХ_|_**_|_ХХ_|_*	Х_|_**_|_ХХ_|_**_|_Х	3_|_44_|_33_|_44_|_3
		// * | ХХ | ** | ХХ | *		X | ** | XX | ** | X	X | ХХ | XX | ХХ | X	X | ХХ | XX | ХХ | X	1 | 22 | 11 | 22 | 1
		// *_|_ХХ_|_**_|_ХХ_|_* ->	Х_|_**_|_XX_|_**_|_X ->	Х_|_ХХ_|_XX_|_ХХ_|_X ->	Х_|_ХХ_|_XX_|_ХХ_|_X ==	1_|_22_|_11_|_22_|_1
		// Х | ХХ | ХХ | ХХ | Х		Х | ХХ | ХХ | ХХ | Х	* | ХХ | ** | ХХ | *	Х | ** | ХХ | ** | Х	3 | 44 | 33 | 44 | 3		
		// Х_|_ХХ_|_ХХ_|_ХХ_|_Х		Х_|_ХХ_|_ХХ_|_ХХ_|_Х	*_|_ХХ_|_**_|_ХХ_|_*	Х_|_**_|_ХХ_|_**_|_Х	3_|_44_|_33_|_44_|_3
		// * | ХХ | ** | ХХ | *		X | ** | XX | ** | X	X | ХХ | XX | ХХ | X	X | ХХ | XX | ХХ | X	1 | 22 | 11 | 22 | 1		
		//Однако, если в одном потоке будет только 4 клетки, то мы потратим чёртову кучу времени на переключение потоков!
		//А потому это не более, чем научная гипотеза. Нужны опыты. Сколько по хорошему должно быть клеток на поток. Тогда
		//Можно весь мир разбить не на столбы, а на блоки... Но исследований нет - поэтому пока это лишь теория
		//И, естественно, этапы должны идти в случайном порядке. Главное, чтобы они были независимы
		
		//А теперь вся магия!
		//Дело в том, что клетка может походить в сторону на 1 клетку.
		//А ещё на 1 клетку она может походить из-за течения!
		//Что до объёмного разбиения - клетка может походить вверх на ещё одну бонусную клетку из-за плавучести.
		//Итого. В горизонтале нам нужно не |ХХ|**|ХХ|**|, а |ХХХХ|****|ХХХХ|****|. Так как клетке нужна дополнительная "защитная" линия в случае сдувания потоком.
		//Для вертикали ещё больше - там понадобится 2 защитные клетки
		//И да... Если течения наложатся... То всё пойдёт по звезде
		//Конечно. Клетка будет толкаться течением и плавучестью после своего хода
		//Но всё равно - как только она подойдёт слишком близко к краю другого потока - ошибка - это вопрос времени
		final var columnPerPc = 4;
		final var columnPerPc2 = columnPerPc * 2;
		//Сколько нужно дать каждой клетке, чтобы сойтись по итогу
		double insert = (vaxX - (vaxX/columnPerPc2)*columnPerPc2) / ((double)vaxX);
		cellsTask = new ArrayList<>(vaxX / columnPerPc2 + columnPerPc);
		//Собственно сами точки для клетки
		ArrayList<Point> firstList = new ArrayList<>();
		List<Point> secondList = new ArrayList<>();
		for (int x = 0; x < vaxX; x++) {
			var difX = Math.round(x  - insert*x);
			if(x != 0 && difX % columnPerPc2 == 0 && !secondList.isEmpty()) {
				cellsTask.add(new WorldTask(firstList.toArray(Point[]::new),secondList.toArray(Point[]::new)));
				firstList.clear();
				secondList.clear();
			}
			boolean isF = difX % columnPerPc2 < columnPerPc;
			for (int y = 0; y < Configurations.getHeight(); y++) {
				Point point = Point.create(x, y);
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
		if(isStepCucle) throw new SecurityException("Попытка запустить уже запущенный поток провалена!");
		else			isStepCucle = true;
		preStep();
		try {
			var f = new int[CellObject.LV_STATUS.length];
			for (int st = 0; st < 2; st++) {
				if(timeout == 0){
					f = maxExecutor.invokeAll(cellsTask)
							.stream()
							.map(a -> {try{return a.get();}catch(InterruptedException | ExecutionException e){ return new int[CellObject.LV_STATUS.length];}})
							.reduce(f, (a,b) -> {for (int i = 0; i < b.length; i++) {a[i] += b[i];}return a;});
				} else {
					for (var t : cellsTask) {
						final var b = t.call();
						for (int i = 0; i < b.length; i++) {f[i] += b[i];}
					}
					if(timeout > 1)
						Utils.pause_ms(timeout);
				}
				isFirst = !isFirst;
			}
			_all_live_cell = f;
		} catch (InterruptedException e) {
			Logger.getLogger(World.class.getName()).log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		postStep();
		
		if(!isStepCucle)	throw new SecurityException("Попытка остановить не запущенный поток!");
		else				isStepCucle = false;
	}
	/**Операции, которые должны быть выполнены после шага*/
	private void postStep(){
		Configurations.suns.forEach( s -> s.step(step));
		Configurations.minerals.forEach( s -> s.step(step));
		Configurations.streams.forEach( s -> s.step(step));
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
		if(!point.valid())
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
		assert point.valid() : "Точка " + point + " находится за пределами поля! Её невозможно получить!";
		return _WORLD_MAP[point.getX()][point.getY()];
	}
	/**Добавляет определённый объект на карту в то место, куда он хочет
	 * дополнительно проверяя, что данное поле свободно
	 * @param cell объект, который надо добавить
	 */
	public void add(CellObject cell) {
		assert get(cell.getPos()) == null : String.format("Объект %s(%d) решил вступть на %s,но тут занято %s(%d)!!!", cell,cell.hashCode(), cell.getPos(),get(cell.getPos()),get(cell.getPos()).hashCode());
		assert !cell.aliveStatus(LV_STATUS.GHOST) : "Требуется добавить " + cell + " только вот он уже мёртв!!! ";
		_WORLD_MAP[cell.getPos().getX()][cell.getPos().getY()] = cell;	
	}
	/**Удаляет объект с карты, с места, которое он занимает
	 * дополнительно проверяя, что данное поле действительно занято этим объектом
	 * @param cell объект, которому больше нет места на поле
	 */
	public void clean(CellObject cell) {
		assert get(cell.getPos()) == cell : String.format("Объект %s(%d) решил удалиться со своих координат(%s), но тут занято %s(%d) - это не он!!!!!!", cell,cell.hashCode(), cell.getPos(),get(cell.getPos()),get(cell.getPos()).hashCode());
		assert !cell.aliveStatus(LV_STATUS.GHOST) : "Объект нужно удалить, но " + cell + " уже мёртв!!!";
		_WORLD_MAP[cell.getPos().getX()][cell.getPos().getY()] = null;
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
		var cellSwap = get(target);
		if(cellSwap == null){
			move(cell,target);
		} else {
			//Если на месте, куда хочет cell, что-то есть
			var d = Point.direction(target, cell.getPos());
			clean(cell);
			if(cellSwap.move(d)){
				//Если объект смог занять нашу позицию. 
				clean(cellSwap);
				add(cell);
				if(cell.move(d.inversion())){
					//Тогда мы занимаем позицию объекта и выходим
					add(cellSwap);
				} else {
					//Если мы не смогли занять позцию объекта...
					clean(cell);
					add(cellSwap);
					cellSwap.move(d.inversion()); //Откатываем цель ((клетку удаляем, потому что цель на месте клетки сейчас))
					add(cell);	//И возваращем клетку на свободное место
				}
			} else {
				add(cell);
			}
		}
	}
	/**
	 * Показывает состояние работы мира 
	 * @return true, если включён автоматических ход мира
	 */
	public boolean isActiv(){
		return _status == STATUS.ACTIV_ALL;
	}
	/**Останавливает работу мира*/
	public void stop(){
		if(_status == STATUS.ACTIV_ALL)
			_status = STATUS.STOP;
	}
	/**Останавливает работу мира и не возвращает управление, пока мир действительно не остановится*/
	public void awaitStop(){
		if(!isActiv()) return;
		stop();
		Utils.pause_ms(100);
		while(isStepCucle){
			Utils.pause_ms(100);
		}
	}
	/**Запускает работу мира*/
	public void start(){
		if(_status != STATUS.ERROR)
			_status = STATUS.ACTIV_ALL;
	}
	/**Сохраняет скорость моделирвоания.
	 * Если 0, то мир моделируется максимально быстро.
	 * Если 1, то моделирвоание происходит на одном процессоре
	 * Числа больше 1 показывают задержку в мс после каждого шага
	 * @param speed новая скорость
	 */
	public void setSpeed(long speed){
		timeout = speed;
	}
	/**Возвращает скорость моделирвоания.
	 * Если 0, то мир моделируется максимально быстро.
	 * Если 1, то моделирвоание происходит на одном процессоре
	 * Числа больше 1 показывают задержку в мс после каждого шага
	 * @return скорость моделирования
	 */
	public long getSpeed(){return timeout;}
	/**Возвращает количество существующих объектов того или иного типа
	 * 
	 * @param type тип интересующего объекта
	 * @return количество объектов такого типа
	 */
	public int getCount(CellObject.LV_STATUS type){
		return _all_live_cell[type.ordinal()];
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
		Point pos = Point.create(cell.getJ("pos"));
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
		Point pos = Point.create(cell.getJ("pos"));
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
		Point pos = Point.create(cell.getJ("pos"));
		final var tarPos = Point.create(x, y);
		return pos.distance(tarPos).getHypotenuse() <= r;
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
}
