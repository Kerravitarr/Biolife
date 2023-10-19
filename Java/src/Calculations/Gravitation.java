/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import Utils.JSON;
import java.awt.Dimension;
/**
 * Гравитация, действующая на любой объект с карты
 * @author Kerravitarr
 */
public class Gravitation {
	/**Нулевая гравитация*/
	public static final Gravitation NONE = new Gravitation();
	
	/**Сила гравитации
	 * Если 0 - гравитация не действует
	 * Если 1 - гравитация действует каждый ход
	 * Если 2 - каждые 2 хода
	 * Меньше 0 быть не может
	 */
	private final int value;
	/**Направление движения гравитации*/
	private final Direction direction;
	/**Точка, на которую стремится гравитация. Опционально, может быть null*/
	private Point target;
	/**Размер мира, под который заточена эта гравитация. Для движения на центр мира*/
	private final Dimension ws = new Dimension();
	
	/**Матрица направлений гравитации*/
	private Point.DIRECTION[][] Energy = new Point.DIRECTION[0][0];
	/**"Нулевой" массив. Причём тут он реально нулевой - везде null*/
	private Point.DIRECTION[] NullE = new Point.DIRECTION[0];
	/**Флаг необходимости пересчитать матрицу*/
	private boolean isNeedRecalculateEnergy = true;
	
	/**Возможное направление действия гравитации*/
	public static enum Direction{
		/**Вверх поля*/
		UP, 
		/**В верхний правый угол поля*/
		UP_R,
		/**В парвую сторону*/
		RIGHT, 
		/**В нижний правый угол поля*/
		DOWN_R,
		/**Вниз поля*/
		DOWN, 
		/**В левый нижний угол поля*/
		DOWN_L, 
		/**В левую сторону*/
		LEFT, 
		/**В правый верхний гол поля*/
		UP_L,
		/**В центр поля*/
		CENTER, 
		/**К точке*/
		TO_POINT,
		/**Не будет гравитации*/
		NONE;
		
		/**Все значения мира*/
		public static final Direction[] values = Direction.values();
		/**Количество значений*/
		public static final int length = Direction.values.length;
		
		@Override
		public String toString(){
			return Configurations.getProperty(Direction.class,name());
		}
	}
	/**Сoздаёт гравитацию, направленную на точку
	 * @param power cила гравитации
	 *				Если 1 - гравитация действует каждый ход
	 *				Если 2 - каждые 2 хода
	 * @param target точка, к которой стремятся все клетки
	 */
	public Gravitation(int power, Point target){
		this(Direction.TO_POINT, target, power);
		if(target == null)
			throw new IllegalArgumentException("Не задана точка притяжения!");
		if(power < 1)
			throw new IllegalArgumentException("Сила гравитации не может быть меньше 1!");
	}
	/**Сoздаёт гравитацию, направленную в определённую сторону
	 * @param power cила гравитации
	 *				Если 1 - гравитация действует каждый ход
	 *				Если 2 - каждые 2 хода
	 * @param dir направление, куда будет стремиться объект
	 */
	public Gravitation(int power, Direction dir){
		this(dir, null, power);
		if(power < 1)
			throw new IllegalArgumentException("Сила гравитации не может быть меньше 1!");
		else if(dir == Direction.TO_POINT)
			throw new IllegalArgumentException("Не задана точка притяжения!");
		else if(dir == Direction.NONE)
			throw new IllegalArgumentException("Задана сила притяжения!");
	}
	
	public Gravitation(JSON j, long v){
		this( Direction.valueOf(j.get("direction")), j.containsKey("target") ? Point.create(j.getJ("target")) : null, j.get("value"));
	}
	/**Сoздаёт гравитацию, которая ни на что не действует*/
	public Gravitation(){
		this(Direction.NONE, null, 0);
	}
	private Gravitation(Direction d, Point t, int v){
		direction = d;
		this.target = t;
		this.value = v;
		updateMatrix();
	}
	/**Возвращает силу потока
	 * @return сила потока
	 */
	public int getValue(){return value;}
	/**Возвращает базовое направление потока
	 * @return куда поток сейчас дует
	 */
	public Direction getDirection(){return direction;}
	/**Возвращает точку к которой стремится гравитация
	 * @return точка на карте или null. 
	 */
	public Point getPoint(){return direction == Direction.TO_POINT ? target : null;}
	/**Возвращает силу движения под действием гравитации
	 * @param o кто спрашивает
	 * @return сила движения, в клетках/ход
	 */
	public double push() {
		return value == 0 ? 0 : 1d / value;
	}
	/**Пересчитывает всю сеть излучателеу*/
	public void recalculation(){
		isNeedRecalculateEnergy = false;
		for (int x = 0; x < Configurations.getWidth(); x++) {
			System.arraycopy(NullE	, 0, Energy[x], 0, NullE.length);
		}
	}
	/**Показывает, что нужно обновить матрицу энергии*/
	public synchronized void updateMatrix() {
		if(Energy.length != Configurations.getWidth() || Energy[0].length != Configurations.getHeight()){
			Energy = new Point.DIRECTION[Configurations.getWidth()][Configurations.getHeight()];
			NullE = new Point.DIRECTION[Configurations.getHeight()];
			for (int x = 0; x < Configurations.getWidth(); x++) {
				System.arraycopy(NullE	, 0, Energy[x], 0, NullE.length);
			}
		}
		isNeedRecalculateEnergy = true;
	}
	/**Указывает куда именно двигаться клетке
	 * @param where откуда
	 * @return направление, куда. Может быть null, если ни куда не нужно
	 */
	public Point.DIRECTION getDirection(Point where) {
		if(Energy[where.getX()][where.getY()] == null){
			Energy[where.getX()][where.getY()] = calculation(where);
		}
		return Energy[where.getX()][where.getY()];
	}
	private Point.DIRECTION calculation(Point where){
		switch (direction) {
			case UP -> {	return Point.DIRECTION.UP;}
			case UP_R -> {	return Point.DIRECTION.UP_R;}
			case RIGHT -> {	return Point.DIRECTION.RIGHT;}
			case DOWN_R -> {return Point.DIRECTION.DOWN_R;}
			case DOWN -> {	return Point.DIRECTION.DOWN;}
			case DOWN_L -> {return Point.DIRECTION.DOWN_L;}
			case LEFT -> {	return Point.DIRECTION.LEFT;}
			case UP_L -> {	return Point.DIRECTION.UP_L;}
			case CENTER -> {
				if(ws.width != Configurations.confoguration.MAP_CELLS.width || ws.height != Configurations.confoguration.MAP_CELLS.height){
					ws.width = Configurations.confoguration.MAP_CELLS.width;
					ws.height = Configurations.confoguration.MAP_CELLS.height;
					target = Point.create(Configurations.confoguration.MAP_CELLS.width / 2, Configurations.confoguration.MAP_CELLS.height / 2);
				}
				return where.distance(target).direction();
			}
			case TO_POINT -> {
				return where.distance(target).direction();
			}
			case NONE -> {return null;}
			default -> throw new AssertionError(direction.name());
		}
	}
	/**Сохраняет гравитацию в виде JSON объекта
	 * @return JSON из которого можно восстановить гравитацию
	 */
	public JSON toJSON() {
		final var j = new JSON();
		j.add("value", value);
		j.add("direction", direction);
		if(target != null)
			j.add("target", target.toJSON());
		return j;
	}
}
