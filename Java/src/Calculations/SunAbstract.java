package Calculations;

import GUI.AllColors;
import GUI.WorldView.Transforms;
import java.awt.Graphics2D;

/**
 * Болванка солнышка. Любое солнце должно быть похоже на это!
 * @author Илья
 *
 */
public abstract class SunAbstract {
	/**Траектория движения солнца*/
	private final Trajectory move;
	/**Позиция солнца. Это условная позиция, наследник может с ней делать что угодно*/
	protected Point position;
	/**Наибольшая энергия солнца*/
	protected double power;

	/**Создаёт солнце
	 * @param p максимальная энергия солнца, будто и не было тени
	 * @param move форма движения солнца. Доступны изначально LineMove и EllipseMove
	 */
	public SunAbstract(double p, Trajectory move){
		this.move = move;
		position = move.start();
		power = p;
	}
	/**
	 * Возвращает количество солнечной энергии в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии. Может быть отрицательным числом - это не поглащение света, а удалённость от солнца
	 */
	public abstract double getPoint(Point pos);
	/**Этот метод будет вызываться каждый раз, когда изменится местоположение объекта*/
	protected abstract void move();

	/**Шаг мира для пересчёт
	 * @param step номер шага мира
	 */
	public void step(long step) {
		if(move != null && move.isStep(step)){
			position = move.step();
			move();
		}
	}
	/**Рисует солнце на экране
	 * @param g холст, на котором надо начертить солнышко
	 * @param transform преобразователь размеров мировых в размеры экранные
	 */
	public void paint(Graphics2D g, Transforms transform){
		if(Configurations.DIRTY_WATER == 0){
			//Если у нас чистая вода, то солнце осветит собой всё, что можно
			g.setColor(AllColors.SUN);
			g.drawRect(transform.toScrinX(0), transform.toScrinY(0),transform.toScrin(Configurations.MAP_CELLS.width), transform.toScrin(Configurations.MAP_CELLS.height));
		} else {
			//Мы нарусем не одно солнце, а сразу все 4!
			//i = 0 Солнце
			//i = 1 Его-же справа (слева)
			//i = 2 Его-же сверху(снизу)
			//i = 3 И его правую (левую) тень сверху (снизу)
			
			//Мы нарусем не одно солнце, а сразу все 4!
			//i = 0 Солнце
			//i = 1 Его-же справа (слева)
			//i = 2 Его-же сверху(снизу)
			//i = 3 И его правую (левую) тень сверху (снизу)
			for (int i = 0; i < 4; i++) {
				if(i > 0){
					switch (Configurations.world_type) {
						case LINE_H -> {if(i == 2 || i == 3) continue;}
						case LINE_V -> {if(i == 1 || i == 3) continue;}
						case FIELD_R -> {}
						case CIRCLE,RECTANGLE -> {continue;}
						default -> throw new AssertionError();
					}
				}
				final var posX = switch(i){
					case 0,2 -> position.getX();
					case 1,3 -> position.getX() + (position.getX() > Configurations.MAP_CELLS.width/2 ?  - Configurations.MAP_CELLS.width: Configurations.MAP_CELLS.width);
					default -> throw new AssertionError();
				};
				final var posY = switch(i){
					case 0,1 -> position.getY();
					case 2,3 -> position.getY() + (position.getY() > Configurations.MAP_CELLS.height/2 ?  -Configurations.MAP_CELLS.height : Configurations.MAP_CELLS.height);
					default -> throw new AssertionError();
				};
				paint(g,transform, posX, posY);
			}
		}
	}
	/**
	 * Функция непосредственного рисования звезды в указанных координатах.
	 * Звезда должна отрисовать себя так, будто она находится где ей сказанно
	 * @param g холст, на котором надо начертить солнышко
	 * @param transform преобразователь размеров мировых в размеры экранные
	 * @param posX текущаяя координата звезды
	 * @param posY текущаяя координата звезды
	 */
	protected abstract void paint(Graphics2D g, Transforms transform, int posX, int posY);
}
