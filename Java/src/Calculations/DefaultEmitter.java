package Calculations;

import GUI.WorldView.Transforms;
import Utils.JSON;
import java.awt.Graphics2D;

/**
 * Болванка любого излучателя - солнца или минералов
 * @author Илья
 *
 */
public abstract class DefaultEmitter {
	/**Траектория движения*/
	private final Trajectory move;
	/**Позиция ткущая. Это условная позиция, наследник может с ней делать что угодно*/
	protected Point position;
	/**Наибольшая энергия*/
	protected double power;
	/**Название*/
	private String name;
	/**Если тут true, то у нас излучает только поверхность, а если false - то излучает вся площадь*/
	protected boolean isLine;
	
	//Отдельные переменные только для отрисовки!
	/**Флаг выбора излучателя для "мигания"*/
	private boolean isSelected = false;
	/**Флаг, показывающий, в текущем кадре звезда должна выглядеть как выбранная или нет*/
	private static boolean isSelectedFrame = false;
	/**Счётчик времени для подсвечивания и высвечивания объекта*/
	private static long nextSelected = 0;
	/**Сколько мс должно пройти чтобы объект изменил параметр выбора*/
	private static final long SELECT_PERIOD = 500;
	
	
	
	protected DefaultEmitter(JSON j, long v){
		power = j.get("power");
		name = j.get("name");
		isLine = j.get("isLine");
		move = Trajectory.generation(j.getJ("move"),v);
		position = Point.create(j.getJ("position"));
	}

	/**Создаёт изулучатель
	 * @param p максимальная энергия, будто и не было препятствий на пути
	 * @param move форма движения
	 * @param n название
	 * @param isLine если true, то у нас излучает только поверхность, а если false - то излучает вся площадь
	 */
	public DefaultEmitter(double p, Trajectory move, String n, boolean isLine){
		this.move = move;
		position = move.start();
		power = p;
		this.name = n;
		this.isLine = isLine;
	}

	/**Этот метод будет вызываться каждый раз, когда изменится местоположение объекта*/
	protected abstract void move();
	/**Шаг мира для пересчёта
	 * @param step номер шага мира
	 */
	public void step(long step) {
		if(move != null && move.isStep(step)){
			position = move.nextPosition();
			move();
		}
	}
	/**Рисует объект на экране
	 * @param g холст, на котором надо начертить солнышко
	 * @param transform преобразователь размеров мировых в размеры экранные
	 */
	public void paint(Graphics2D g, Transforms transform){
		//Мигалка выбора
		if(isSelected){
			final var mc = System.currentTimeMillis();
			if(nextSelected < mc){
				nextSelected = mc + SELECT_PERIOD;
				isSelectedFrame = !isSelectedFrame;
			}
		}
		
		//Мы нарусем не один излучатель, а сразу все 4!
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
			paint(g,transform, posX, posY, isSelected && isSelectedFrame);
		}
	}
	/**
	 * Функция непосредственного рисования объекта в указанных координатах.
	 * Объект должен отрисовать себя так, будто она находится где ему сказанно
	 * @param g холст, на котором надо начертить себя
	 * @param transform преобразователь размеров мировых в размеры экранные
	 * @param posX текущаяя координата
	 * @param posY текущаяя координата
	 * @param isSelected отрисовывать объект, как выбранный или нет?
	 */
	protected abstract void paint(Graphics2D g, Transforms transform, int posX, int posY, boolean isSelected);
	/**Возвращает максимальную энергию излучателя
	 * @return сколько можно получить энергии от этого источника
	 */
	public double getPower(){return power;}
	/**Сохраняет новую максимальную энергию излучателя
	 * @param p сколкьо теперь эенргии будет в излучатеел
	 */
	public void setPower(double p){power = p;}
	/**Возвращает тип излучателя - линейный или объёмный
	 * @return Если тут true, то у нас излучает только поверхность, а если false - то излучает вся площадь
	 */
	public boolean getIsLine(){return isLine;}
	/**Сохраняет новую максимальную энергию излучателя
	 * @param isL если true, то у нас излучает только поверхность, а если false - то излучает вся площадь
	 */
	public void setIsLine(boolean isL){isLine = isL;}
	/**Возвращает значение параметра отображения звездны - выделяется она на экране или нет
	 * @return Если тут true, то излучатель будет подмигивать прозрачностью
	 */
	public boolean getSelect(){return isSelected;}
	/**Сохраняет значение параметра отображения звездны - выделяется она на экране или нет
	 * @param isS если true, то излучатель будет подмигивать прозрачностью
	 */
	public void setSelect(boolean isS){isSelected = isS;}
	/**Возвращает траекторию движения излучателя
	 * @return закон, по которому движется излучаетль
	 */
	public Trajectory getTrajectory(){return move;}
	/**Сохраняет имя излучателя
	 * @param n как его теперь будут звать
	 */
	public void setName(String n){name = n;}
	@Override
	public String toString(){
		return name;
	}
	/**Превращает излучатель в серелизуемый объект
	 * @return объект, который можно пересылать, засылать
	 */
	public JSON toJSON(){
		final var j = new JSON();
		j.add("power", power);
		j.add("name", name);
		j.add("isLine", isLine);
		j.add("move", move.toJSON());
		j.add("position", position.toJSON());
		return j;
	}
}
