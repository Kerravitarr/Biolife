/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import MapObjects.CellObject;
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
		NONE
	}
	/**Сoздаёт гравитацию, направленную на точку
	 * @param power cила гравитации
	 *				Если 1 - гравитация действует каждый ход
	 *				Если 2 - каждые 2 хода
	 * @param target точка, к которой стремятся все клетки
	 */
	public Gravitation(int power, Point target){
		if(target == null)
			throw new IllegalArgumentException("Не задана точка притяжения!");
		if(power < 1)
			throw new IllegalArgumentException("Сила гравитации не может быть меньше 1!");
		direction = Direction.TO_POINT;
		this.target=target;
		this.value = power;
	}
	/**Сoздаёт гравитацию, направленную в определённую сторону
	 * @param power cила гравитации
	 *				Если 1 - гравитация действует каждый ход
	 *				Если 2 - каждые 2 хода
	 * @param dir направление, куда будет стремиться объект
	 */
	public Gravitation(int power, Direction dir){
		direction = dir;
		if(power < 1)
			throw new IllegalArgumentException("Сила гравитации не может быть меньше 1!");
		else if(dir == Direction.TO_POINT)
			throw new IllegalArgumentException("Не задана точка притяжения!");
		else if(dir == Direction.NONE)
			throw new IllegalArgumentException("Задана сила притяжения!");
		this.target=null;
		this.value = power;
	}
	
	public Gravitation(JSON j, long v){
		direction = Direction.valueOf(j.get("direction"));
		value = j.get("value");
		if(j.containsKey("target"))
			target = new Point(j.getJ("target"));
	}
	/**Сoздаёт гравитацию, которая ни на что не действует*/
	public Gravitation(){
		direction = Direction.NONE;
		this.target=null;
		this.value = 0;
	}
	/**Возвращает необходимость движения под действием гравитации
	 * @param o кто спрашивает
	 * @return true, если нужно куда-то сдвинуться
	 */
	public boolean isStep(CellObject o) {
		return value != 0 && (o.getAge() % value) == 0;
	}
	/**Указывает куда именно двигаться клетке
	 * @param pos откуда
	 * @return направление, куда. Может быть null, если ни куда не нужно
	 */
	public Point.DIRECTION getDirection(Point pos) {
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
				if(ws.width != Configurations.MAP_CELLS.width || ws.height != Configurations.MAP_CELLS.height){
					ws.width = Configurations.MAP_CELLS.width;
					ws.height = Configurations.MAP_CELLS.height;
					target = new Point(Configurations.MAP_CELLS.height / 2,Configurations.MAP_CELLS.width / 2);
				}
				return pos.distance(target).direction();
			}
			case TO_POINT -> {
				return pos.distance(target).direction();
			}
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
