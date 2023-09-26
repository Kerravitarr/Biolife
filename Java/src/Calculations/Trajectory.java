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
	private final static Map<String, ClassBuilder<Trajectory>> TRAJECTORIES = new HashMap<>();
	static{
		final var builder = new ClassBuilder<Trajectory>("Точка"){
			@Override
			public Trajectory build(JSON json, long version){
				return new Trajectory(json, version);
			}
		};
		builder.addParam(new ClassBuilder.MapPointParam<Trajectory>() {
			@Override public Point get(Trajectory who) {return who.pos;}
			@Override public Point getDefault() { return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
			@Override public void setValue(Trajectory who, Point value) {who.pos = value;}
			@Override public String name() {return "position";}
		});
		builder.addConstructor(new ClassBuilder.Constructor<Trajectory>(){
			@Override
			protected Trajectory build(List<ClassBuilder.ConstructParam> _params) {
				throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
			}
		});
		Trajectory.register(builder);
	}
	/**Регистрирует наследника как одного из возможных дочерних классов.
	 * То есть мы можем создать траекторию такого типа
	 * @param trajectory фабрика по созданию наследников
	 */
	protected static void register(ClassBuilder<Trajectory> trajectory){TRAJECTORIES.put(trajectory.name(), trajectory);};
	
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
	public JSON toJSON(){
		final var j = new JSON();
		j.add("speed", speed);
		j.add("step", step);
		j.add("pos", pos.toJSON());
		j.add("_className", this.getClass().getName());
		return j;
	}
	/**
	 * Возвращает все переменные, необходимые для создания объекта
	 * @return список из всех доступных параметров
	 */
	protected static ParamConstructor.GenerateVariants<Trajectory> generate(){
		final var ret = new ParamConstructor.GenerateVariants<Trajectory>(Trajectory.class);
		final var constructor1 = new ParamConstructor.GenerateObject<Trajectory>(Configurations.getProperty(Trajectory.class, "constructor.name"));
		constructor1.add(new ParamConstructor(Configurations.getProperty(Trajectory.class, "parametr.name"), Point.create(0, 0)));
		ret.add(constructor1);
		return ret;
	}
	
	
	/**
	 * Возвращает списко объекто, которые позволяют создать абсолютно любую траекторию
	 * @return каждый пункт списка - возможность создать объект
	 */
	public static List<ParamConstructor.GenerateVariants<Trajectory>> getAllGenerators(){
		final var allTrajectories = new ArrayList<ParamConstructor.GenerateVariants<Trajectory>>();
	
		
        /*for(final var url : urlCl.getDefinedPackages()) {
			final var name = url.getName();
			if(name.endsWith(".class")){
				try {
					final var cl = Class.forName(name.replace('/', '.')).asSubclass(Trajectory.class);
					final var m = cl.getMethod("generate");
					allTrajectories.add((ParamConstructor.GenerateVariants<Trajectory>) m.invoke(null));
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
					Logger.getLogger(Trajectory.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
        }*/
		return allTrajectories;
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
