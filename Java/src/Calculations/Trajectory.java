/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import Utils.ParamConstructor;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Graphics2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * болванка для таректории по которой могут двигаться объекты карты
 * @author Kerravitarr
 */
public class Trajectory{
	/**Скорость объекта. Единица измерения: секунд на 1 шаг*/
	private long speed;
	/**Для неподвижных объектов, точка нахождения*/
	private Point pos;
	/**Текущий шаг траектории*/
	private long step;
	
	/**Все возможные траектории, которые мы можем создать*/
	private final static ClassBuilder.StaticBuilder<Trajectory> TRAJECTORIES = new ClassBuilder.StaticBuilder<>();
	static{
		final var builder = new ClassBuilder<Trajectory>(){
			@Override public Trajectory generation(JSON json, long version){return new Trajectory(json, version);}
			@Override public JSON serialization(Trajectory object) { return object.toJSON();}

			@Override public String serializerName() {return "Точка";}
			@Override public Class printName() {return Trajectory.class;}

		};
		builder.addParam(new ClassBuilder.MapPointParam<Trajectory>() {
			@Override public Point get(Trajectory who) {return who.pos;}
			@Override public Point getDefault() { return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
			@Override public void setValue(Trajectory who, Point value) {who.pos = value;}
			@Override public String name() {return "position";}
		});
		builder.addConstructor(new ClassBuilder.Constructor<Trajectory>(){
			{
				addParam(new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "center";}
				});
			}
			@Override public Trajectory build() {return new Trajectory(getParam(Point.class));}
			@Override public String name() {return "";}
		});
		Trajectory.register(builder);
	}
	
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
		this(speed, Point.create(0, 0));
	}
	protected Trajectory(JSON json, long version){
		speed = json.getL("speed");
		step = json.getL("step");
		pos = Point.create(json.getJ("pos"));
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
	/**Возвращает скорость движения по траектории
	 * @return каждые сколько шагов происходит шаг (если 0, то не двигается)
	 */
	public long getSpeed(){return speed;}
	/**Сохраняет скорость движения по траектории
	 * @param s каждые сколько шагов происходит шаг (если 0, то не двигается)
	 */
	public void setSpeed(long s){speed = s;}
	/**Превращает излучатель в серелизуемый объект
	 * @return объект, который можно пересылать, засылать
	 */
	protected JSON toJSON(){
		final var j = new JSON();
		j.add("speed", speed);
		j.add("step", step);
		j.add("pos", pos.toJSON());
		return j;
	}
	/** * Регистрирует наследника как одного из возможных дочерних классов.То есть мы можем создать траекторию такого типа
	 * @param <T> класс наследника текущего класса
	 * @param trajectory фабрика по созданию наследников
	 */
	protected static <T extends Trajectory> void register(ClassBuilder<T> trajectory){TRAJECTORIES.register(trajectory);};
	/** * Создаёт реальный объект на основе JSON файла.
	 * Самое главное, чтобы этот объект был ранее зарегистрирован в этом классе
	 * @param json объект, описывающий объект подкласса CT
	 * @param version версия файла json в котором объект сохранён
	 * @return объект сериализации
	 * 
	 */
	public static Trajectory generation(JSON json, long version){return TRAJECTORIES.generation(json, version);}
	/**Укладывает текущий объект в объект сереализации для дальнейшего сохранения
	 * @param <T> тип объекта, который надо упаковать. Может быть любым наследником текущего класса,
	 *  зарегистрированного ранее в классе
	 * @param object объект, который надо упаковать
	 * @return JSON объект или null, если такой класс не зарегистрирован у нас
	 */
	public static <T extends Trajectory> JSON serialization(T object){return TRAJECTORIES.serialization(object);}
	/** * Возвращает всех возможных подклассов текущего
	 * Самое главное, чтобы эти объекты были ранее зарегистрированы в этом классе
	 * @return список из которого можно можно создать всех деток
	 */
	public static List<ClassBuilder> getChildrens(){return TRAJECTORIES.getChildrens();}

	/**Рисует объект на экране
	 * @param g холст, на котором надо начертить солнышко
	 * @param transform преобразователь размеров мировых в размеры экранные
	 */
	public void paint(Graphics2D g, WorldView.Transforms transform) {
		int r = transform.toScrin(1);
		int cx = transform.toScrinX(pos);
		int cy = transform.toScrinY(pos);
		g.setColor(AllColors.TRAJECTORY_POINT);
		Utils.Utils.fillCircle(g, cx, cy, r);
	}
}
