package Calculations.Emitters;

import Calculations.Configurations;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import Utils.JSON;
import java.util.Arrays;

/**
 * Болванка любого излучателя - солнца или минералов
 * @author Илья
 *
 */
public abstract class DefaultEmitter implements Trajectory.HasTrajectory{
	/**Траектория движения*/
	private Trajectory move;
	/**Позиция ткущая. Это условная позиция, наследник может с ней делать что угодно*/
	protected Point position;
	/**Наибольшая энергия*/
	private double power;
	/**Название*/
	private String name;
	/**Если тут true, то у нас излучает только поверхность, а если false - то излучает вся площадь*/
	private boolean isLine;
	/**Матрица энергии излучателя*/
	private double[][] Energy = new double[0][0];
	/**"Нулевой" массив*/
	private double[] NullE = new double[0];
	/**Флаг необходимости пересчитать матрицу освещения*/
	private boolean isNeedRecalculateEnergy = true;
	
	protected DefaultEmitter(JSON j, long v){
		power = j.get("power");
		name = j.get("name");
		isLine = j.get("isLine");
		move = Trajectory.generation(j.getJ("move"),v);
		position = Point.create(j.getJ("position"));
		updateMatrix();
	}

	/**Создаёт изулучатель
	 * @param p максимальная энергия, будто и не было препятствий на пути
	 * @param move форма движения
	 * @param n название
	 * @param isLine если true, то у нас излучает только поверхность, а если false - то излучает вся площадь
	 */
	public DefaultEmitter(double p, Trajectory move, String n, boolean isLine){
		set(move);
		power = p;
		this.name = n;
		this.isLine = isLine;
		updateMatrix();
	}

	@Override
	public boolean step(long step) {
		if(move.isStep(step)){
			position = move.nextPosition();
			updateMatrix();
		}
		return isNeedRecalculateEnergy;
	}
	/**Пересчитывает всю сеть излучателеу*/
	public void recalculation(){
		if(isNeedRecalculateEnergy){
			isNeedRecalculateEnergy = false;
			for (int x = 0; x < Configurations.getWidth(); x++) {
				System.arraycopy(NullE	, 0, Energy[x], 0, NullE.length);
			}
		}
	}
	/**Возвращает количество излучения в этой точке
	 * @param where точка, где излучается
	 * @return суммарное излучение
	 */
	public double getE(Point where){
		if(Energy[where.getX()][where.getY()] == -1){
			Energy[where.getX()][where.getY()] = calculation(where);
		}
		return Energy[where.getX()][where.getY()];
	}
	protected abstract double calculation(Point where);
	
	/**Возвращает максимальную энергию излучателя
	 * @return сколько можно получить энергии от этого источника
	 */
	public double getPower(){return power;}
	/**Сохраняет новую максимальную энергию излучателя
	 * @param p сколкьо теперь эенргии будет в излучатеел
	 */
	public void setPower(double p) {
		if(p != power)
			updateMatrix();
		power = p;
	}
	/**Возвращает тип излучателя - линейный или объёмный
	 * @return Если тут true, то у нас излучает только поверхность, а если false - то излучает вся площадь
	 */
	public boolean getIsLine(){return isLine;}
	/**Сохраняет новую максимальную энергию излучателя
	 * @param isL если true, то у нас излучает только поверхность, а если false - то излучает вся площадь
	 */
	public void setIsLine(boolean isL) {
		if(isL != isLine)
			updateMatrix();
		isLine = isL;
	}
	/**Показывает, что нужно обновить матрицу энергии ребёка*/
	public synchronized void updateMatrix() {
		if(Energy.length != Configurations.getWidth() || Energy[0].length != Configurations.getHeight()){
			Energy = new double[Configurations.getWidth()][Configurations.getHeight()];
			NullE = new double[Configurations.getHeight()];
			Arrays.fill(NullE, -1d);
			for (int x = 0; x < Configurations.getWidth(); x++) {
				System.arraycopy(NullE	, 0, Energy[x], 0, NullE.length);
			}
		}
		isNeedRecalculateEnergy = true;
	}
	
	@Override
	public Trajectory getTrajectory(){return move;}
	@Override
	public final void set(Trajectory trajectory){
		move = trajectory;
		position = move.start();
		updateMatrix();
	}
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
		j.add("move", Trajectory.serialization(move));
		j.add("position", position.toJSON());
		return j;
	}
	
	/**Рисует объект на экране
	 * @param g холст, на котором надо начертить солнышко
	 * @param transform преобразователь размеров мировых в размеры экранные
	 */
	public void paint(java.awt.Graphics2D g, GUI.WorldView.Transforms transform){		
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
	protected abstract void paint(java.awt.Graphics2D g, GUI.WorldView.Transforms transform, int posX, int posY);
	
}
