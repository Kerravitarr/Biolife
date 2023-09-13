package Calculations;

import static Calculations.Configurations.WORLD_TYPE.CIRCLE;
import static Calculations.Configurations.WORLD_TYPE.FIELD_R;
import static Calculations.Configurations.WORLD_TYPE.LINE_H;
import static Calculations.Configurations.WORLD_TYPE.LINE_V;
import static Calculations.Configurations.WORLD_TYPE.RECTANGLE;
import Calculations.Point.DIRECTION;
import GUI.AllColors;
import GUI.WorldView;
import MapObjects.CellObject;
import Utils.JSON;
import Utils.SaveAndLoad;
import java.awt.Graphics2D;
import java.lang.reflect.InvocationTargetException;

/**
 * Поток жидкости
 *
 */
public abstract class StreamAbstract{
	/**Позиция центра потока*/
	protected Point position;
	/**способ уменьшения мощности потока от расстояния*/
	protected StreamAttenuation shadow;
	/**Имя этого потока*/
	private String name;
	
	/**Создание гейзера
	 * @param pos позиция центра потока при создании
	 * @param s способ рассеения мощности от расстояния.
	 * @param name имя этого поткоа, то, что его отличает от дргих
	 */
	protected StreamAbstract(Point pos, StreamAttenuation s,  String name) {
		position = pos;
		shadow = s;
		this.name = name;
	}
	/**Создание универсальной формы без снижения мощности потока
	 * @param pos позиция центра потока при создании
	 * @param p максимальная энергия потока
	 * @param name имя этого поткоа, то, что его отличает от дргих
	 */
	protected StreamAbstract(Point pos, int p, String name) {
		this(pos, new StreamAttenuation.NoneStreamAttenuation(p), name);
	}
	protected StreamAbstract(JSON j, long v)throws GenerateClassException{
		position = new Point(j.getJ("position"));
		shadow = StreamAttenuation.generate(j.get("shadow"),v);
		name = j.get("name");
	}

	/**Обрабатывает сдувание клетки в определённую сторону потоком
	 * @param cell клетка, на которую поток воздействует
	 */
	public abstract void action(CellObject cell);
	/**Превращает текущий объект в объект его описания
	 * @return объект описания. По нему можно гарантированно восстановить исходник
	 */
	public JSON toJSON(){
		final var j = new JSON();
		j.add("_className", this.getClass().getName());
		j.add("position",position.toJSON());
		j.add("name",name);
		j.add("shadow",shadow.toJSON());
		return j;
	}
	
	/** * Создаёт объект на основе JSON файла. Тут может быть любой из наследников этого класса
	 * @param json объект, описывающий искомый объект
	 * @param version версия файла json в котором объект сохранён
	 * @return найденный потомок
	 * @throws GenerateClassException исключение, вызываемое ошибкой
	 */
	public static StreamAbstract generate(JSON json, long version) throws GenerateClassException{
		String className = json.get("_className");
		try{
			final var ac = Class.forName(className).asSubclass(StreamAbstract.class);
			var constructor = ac.getDeclaredConstructor(JSON.class, long.class);
			return constructor.newInstance(json,version);
		}catch (ClassNotFoundException ex)		{throw new GenerateClassException(ex,className);}
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
	public void paint(Graphics2D g, WorldView.Transforms transform){
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
			paint(g,transform, posX, posY);
		}
	}
	/**
	 * Функция непосредственного рисования объекта в указанных координатах.
	 * Объект должен отрисовать себя так, будто она находится где ему сказанно
	 * @param g холст, на котором надо начертить себя
	 * @param transform преобразователь размеров мировых в размеры экранные
	 * @param posX текущаяя координата
	 * @param posY текущаяя координата
	 */
	protected abstract void paint(Graphics2D g, WorldView.Transforms transform, int posX, int posY);
	
	@Override
	public String toString(){
		return name;
	}
	/**Сохраняет имя потока
	 * @param n как его теперь будут звать
	 */
	public void setName(String n){name = n;}
}