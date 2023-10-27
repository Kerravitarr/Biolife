package Calculations.Streams;

import Calculations.Configurations;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import static Calculations.Configurations.WORLD_TYPE.CIRCLE;
import static Calculations.Configurations.WORLD_TYPE.FIELD_R;
import static Calculations.Configurations.WORLD_TYPE.LINE_H;
import static Calculations.Configurations.WORLD_TYPE.LINE_V;
import static Calculations.Configurations.WORLD_TYPE.RECTANGLE;
import GUI.WorldView;
import MapObjects.CellObject;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

/**
 * Поток жидкости
 *
 */
public abstract class StreamAbstract implements Trajectory.HasTrajectory{
	/**Траектория движения*/
	private Trajectory move;
	/**Позиция центра потока*/
	protected Point position;
	/**способ уменьшения мощности потока от расстояния*/
	protected StreamAttenuation shadow;
	/**Имя этого потока*/
	private String name;
	/**Построитель для любых потомков текущего класса*/
	private final static ClassBuilder.StaticBuilder<StreamAbstract> BUILDER = new ClassBuilder.StaticBuilder<>();
	
	/**Матрица энергии потока*/
	private Action[][] Energy = new Action[0][0];
	/**"Нулевой" массив. В данном случае он реально нулевой - тут одни null*/
	private Action[] NullE = new Action[0];
	/**Нулевая активность*/
	private final Action nullAction = new Action(null, 0);
	/**Флаг необходимости пересчитать матрицу потока*/
	private boolean isNeedRecalculateEnergy = true;
	
	protected class Action{
		private final Point.DIRECTION _d;
		private final double _dist;
		/**
		 * Указывает как именно следует сдвинуться клетке
		 * @param d направление движения
		 * @param dist удалённость от центра потока
		 */
		public Action(Point.DIRECTION d, double dist){_d = d; _dist=dist;}
	}
	
	
	//Отдельные переменные только для отрисовки!
	/**Номер кадра для рисования*/
	protected static int frame = Integer.MAX_VALUE / 2;
	/**Время последнего обновления счётчика кадров*/
	private static long lastUpdate = 0;
	/**Флаг выбора излучателя для "мигания"*/
	private boolean isSelected = false;
	/**Флаг, показывающий, в текущем кадре звезда должна выглядеть как выбранная или нет*/
	private static boolean isSelectedFrame = false;
	/**Счётчик времени для подсвечивания и высвечивания объекта*/
	private static long nextSelected = 0;
	/**Сколько мс должно пройти чтобы объект изменил параметр выбора*/
	private static final long SELECT_PERIOD = 500;
	
	/**Создание гейзера
	 * @param move форма движения
	 * @param s способ рассеения мощности от расстояния.
	 * @param name имя этого поткоа, то, что его отличает от дргих
	 */
	protected StreamAbstract(Trajectory move, StreamAttenuation s,  String name) {
		set(move);
		shadow = s;
		this.name = name;
		updateMatrix();
	}
	/**Создание универсальной формы без снижения мощности потока
	 * @param move форма движения
	 * @param p максимальная энергия потока
	 * @param name имя этого поткоа, то, что его отличает от дргих
	 */
	protected StreamAbstract(Trajectory move, int p, String name) {
		this(move, new StreamAttenuation.NoneStreamAttenuation(p), name);
	}
	protected StreamAbstract(JSON j, long v){
		position = Point.create(j.getJ("position"));
		shadow = StreamAttenuation.generation(j.get("shadow"),v);
		name = j.get("name");
		move = Trajectory.generation(j.getJ("move"),v);
		updateMatrix();
	}

	@Override
	public boolean step(long step) {
		if(move != null && move.isStep(step)){
			position = move.nextPosition();
			updateMatrix();
		}
		return isNeedRecalculateEnergy;
	}
	/**Пересчитывает всю сеть потока*/
	public void recalculation(){
		isNeedRecalculateEnergy = false;
		for (int x = 0; x < Configurations.getWidth(); x++) {
			System.arraycopy(NullE	, 0, Energy[x], 0, NullE.length);
		}
	}
	/**Обрабатывает сдувание клетки в определённую сторону потоком
	 * @param cell клетка, на которую поток воздействует
	 */
	public void push(CellObject cell){
		final var where = cell.getPos();
		if(Energy[where.getX()][where.getY()] == null){
			final var a = action(where);
			Energy[where.getX()][where.getY()] = a != null ? a : nullAction;
		}
		final var dist = Energy[where.getX()][where.getY()];
		if(dist != nullAction){
			cell.move(dist._d, 1d / shadow.power(dist._dist));
		}
	}
	/**Возвращает значение параметра отображения звездны - выделяется она на экране или нет
	 * @return Если тут true, то излучатель будет подмигивать прозрачностью
	 */
	/**Обрабатывает сдувание клетки в определённую сторону потоком
	 * @param cell клетка, на которую поток воздействует
	 * @return расстояние от центра в процетнах [0,1]. При return = 1 вернёт минимальное значение мощности, при return = 0, то есть в центре, вернёт max
	 */
	protected abstract Action action(Point cell);
	/**Показывает, что нужно обновить матрицу энергии ребёка*/
	public synchronized void updateMatrix() {
		if(Energy.length != Configurations.getWidth() || Energy[0].length != Configurations.getHeight()){
			Energy = new Action[Configurations.getWidth()][Configurations.getHeight()];
			NullE = new Action[Configurations.getHeight()];
			for (int x = 0; x < Configurations.getWidth(); x++) {
				System.arraycopy(NullE	, 0, Energy[x], 0, NullE.length);
			}
		}
		isNeedRecalculateEnergy = true;
	}
	
	/**Возвращает значение параметра отображения звездны - выделяется она на экране или нет
	 * @return Если тут true, то излучатель будет подмигивать прозрачностью
	 */
	public boolean getSelect(){return isSelected;}
	/**Сохраняет значение параметра отображения звездны - выделяется она на экране или нет
	 * @param isS если true, то излучатель будет подмигивать прозрачностью
	 */
	public void setSelect(boolean isS){isSelected = isS;}
	@Override
	public Trajectory getTrajectory(){return move;}
	@Override
	public final void set(Trajectory trajectory){
		move = trajectory;
		position = move.start();
		updateMatrix();
	}
	
	/**Превращает текущий объект в объект его описания
	 * @return объект описания. По нему можно гарантированно восстановить исходник
	 */
	public JSON toJSON(){
		final var j = new JSON();
		j.add("position",position.toJSON());
		j.add("name",name);
		j.add("shadow",StreamAttenuation.serialization(shadow));
		j.add("move", Trajectory.serialization(move));
		return j;
	}
	
	/** * Регистрирует наследника как одного из возможных дочерних классов.То есть мы можем создать траекторию такого типа
	 * @param <T> класс наследника текущего класса
	 * @param trajectory фабрика по созданию наследников
	 */
	protected static <T extends StreamAbstract> void register(ClassBuilder<T> trajectory){BUILDER.register(trajectory);};
	/** * Создаёт реальный объект на основе JSON файла.
	 * Самое главное, чтобы этот объект был ранее зарегистрирован в этом классе
	 * @param json объект, описывающий объект подкласса CT
	 * @param version версия файла json в котором объект сохранён
	 * @return объект сериализации
	 * 
	 */
	public static StreamAbstract generation(JSON json, long version){return BUILDER.generation(json, version);}
	/**Укладывает текущий объект в объект сереализации для дальнейшего сохранения
	 * @param <T> тип объекта, который надо упаковать. Может быть любым наследником текущего класса,
	 *  зарегистрированного ранее в классе
	 * @param object объект, который надо упаковать
	 * @return JSON объект или null, если такой класс не зарегистрирован у нас
	 */
	public static <T extends StreamAbstract> JSON serialization(T object){return BUILDER.serialization(object);}
	/**
	 * Возвращает список всех параметров объекта, которые можно покрутить в живом эфире
	 * @return список параметров объекта
	 */
	public List<ClassBuilder.EditParametr> getParams(){return BUILDER.get(this.getClass()).getParams();}
	/** * Возвращает всех возможных подклассов текущего
	 * Самое главное, чтобы эти объекты были ранее зарегистрированы в этом классе
	 * @return список из которого можно можно создать всех деток
	 */
	public static List<ClassBuilder> getChildrens(){return BUILDER.getChildrens();}
	
	
	/**Рисует объект на экране
	 * @param g холст, на котором надо начертить солнышко
	 * @param transform преобразователь размеров мировых в размеры экранные
	 */
	public void paint(Graphics2D g, WorldView.Transforms transform){
		final var cms = System.currentTimeMillis();
		if(cms > lastUpdate){
			lastUpdate = cms;
			frame++;
		}
		//Мигалка выбора
		if(isSelected){
			final var mc = System.currentTimeMillis();
			if(nextSelected < mc){
				nextSelected = mc + SELECT_PERIOD;
				isSelectedFrame = !isSelectedFrame;
			}
		}
		//Мы нарусем не один объек, а сразу все 4!
		//i = 0 Главный
		//i = 1 Его-же справа (слева)
		//i = 2 Его-же сверху(снизу)
		//i = 3 И его правую (левую) тень сверху (снизу)

		for (int i = 0; i < 4; i++) {
			if(i > 0){
				switch (Configurations.confoguration.world_type) {
					case LINE_H -> {if(i == 2 || i == 3) continue;}
					case LINE_V -> {if(i == 1 || i == 3) continue;}
					case FIELD_R -> {}
					case CIRCLE,RECTANGLE -> {continue;}
					default -> throw new AssertionError();
				}
			}
			final var posX = switch(i){
				case 0,2 -> position.getX();
				case 1,3 -> position.getX() + (position.getX() > Configurations.confoguration.MAP_CELLS.width/2 ?  - Configurations.confoguration.MAP_CELLS.width: Configurations.confoguration.MAP_CELLS.width);
				default -> throw new AssertionError();
			};
			final var posY = switch(i){
				case 0,1 -> position.getY();
				case 2,3 -> position.getY() + (position.getY() > Configurations.confoguration.MAP_CELLS.height/2 ?  -Configurations.confoguration.MAP_CELLS.height : Configurations.confoguration.MAP_CELLS.height);
				default -> throw new AssertionError();
			};
			if(!isSelected || isSelectedFrame)
				paint(g,transform, posX, posY, frame);
		}
	}
	/**
	 * Функция непосредственного рисования объекта в указанных координатах.
	 * Объект должен отрисовать себя так, будто она находится где ему сказанно
	 * @param g холст, на котором надо начертить себя
	 * @param transform преобразователь размеров мировых в размеры экранные
	 * @param posX текущаяя координата
	 * @param posY текущаяя координата
	 * @param frame некоторый счётчик, который создаёт анимацию
	 */
	protected abstract void paint(Graphics2D g, WorldView.Transforms transform, int posX, int posY, int frame);
	
	@Override
	public String toString(){
		return name;
	}
	/**Сохраняет имя потока
	 * @param n как его теперь будут звать
	 */
	public void setName(String n){name = n;}
}