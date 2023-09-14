/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import Utils.JSON;
import java.lang.reflect.InvocationTargetException;

/**
 * болванка для таректории по которой могут двигаться объекты карты
 * @author Kerravitarr
 */
public class Trajectory {
	/**Скорость объекта. Единица измерения: секунд на 1 шаг*/
	private final long speed;
	/**Для неподвижных объектов, точка нахождения*/
	private final Point pos;
	/**Текущий шаг траектории*/
	private long step;
	
	private Trajectory(long speed, Point pos){
		if(speed < 0) throw new IllegalArgumentException("Скорость не может быть меньше 0!");
		this.speed = speed;
		this.pos = pos;
		step = 0;
	}
	/**Конструктор
	 * @param speed скорость движения. Как часто солнце будет шагать. При 1 - каждый раз, при 2 - каждые 2 хода и т.д. 
	 *			При 0 объект не движется
	 *			Отрицательной скорость не может
	 */
	protected Trajectory(long speed){
		this(speed, new Point(0, 0));
	}
	protected Trajectory(JSON json, long version){
		speed = json.getL("speed");
		step = json.getL("step");
		pos = new Point(json.getJ("pos"));
	}
	/**Конструктор неподвижного объекта
	 * @param pos его позиция
	 */
	public Trajectory(Point pos){
		this(0, pos);
	}

	/**Возвращает необходимость походить.
	 * @param step текущий шаг
	 * @return true, если требуется изменить позицию
	 */
	public final boolean isStep(long step) {
		return speed != 0 && step % speed == 0;
	}
	/**Возвращает следующую позицию для движения
	 * @return новая позиция объекта
	 */
	public final Point nextPosition() {
		return position(++step);
	}
	/**Возвращает предыдущую позицию для движения
	 * @return новая позиция объекта
	 */
	public final Point prefurPosition() {
		return position(--step);
	}
	
	/**Возвращает новую позицию для движения
	 * @param step шаг, для которого вычисляется позиция
	 * @return новая позиция объекта
	 */
	protected Point position(long step){
		return pos;
	}
	
	/**Получить стартовую позицию объекта
	 * @return позиция объекат во время начала движения
	 */
	public final Point start(){step--;return nextPosition();}
	
	/**Превращает излучатель в серелизуемый объект
	 * @return объект, который можно пересылать, засылать
	 */
	public JSON toJSON(){
		final var j = new JSON();
		j.add("speed", speed);
		j.add("step", step);
		j.add("pos", pos.toJSON());
		j.add("_className", this.getClass().getName());
		return j;
	}
	/** * Создаёт реальное солнце на основе JSON файла.Тут может быть любое из существующих солнц
	 * @param json объект, описывающий солнце
	 * @param version версия файла json в котором объект сохранён
	 * @return найденное солнце... Или null, если такого солнца не бывает
	 * 
	 * @throws Calculations.GenerateClassException
	 */
	public static Trajectory generate(JSON json, long version) throws GenerateClassException{
		String className = json.get("_className");
		try{
			Class<? extends Trajectory> ac = Class.forName(className).asSubclass(Trajectory.class);
			var constructor = ac.getDeclaredConstructor(JSON.class, long.class);
			return constructor.newInstance(json,version);
		} catch (ClassNotFoundException ex)		{throw new GenerateClassException(ex,className);}
		catch (NoSuchMethodException ex)		{throw new GenerateClassException(ex);}
		catch (InstantiationException ex)		{throw new GenerateClassException(ex);}
		catch (IllegalAccessException ex)		{throw new GenerateClassException(ex);} 
		catch (IllegalArgumentException ex)		{throw new GenerateClassException(ex);}
		catch (InvocationTargetException ex)	{throw new GenerateClassException(ex);}
	}
}
