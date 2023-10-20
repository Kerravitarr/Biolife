/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MapObjects;

import Calculations.Configurations;
import Calculations.Point;
import GUI.Legend;
import Utils.JSON;
import Utils.Utils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Arrays;

/**
 * Это особая структура. Она позволяет многоклеточным двигаться.
 * Теперь как это работает.
 * Если живые клетки расходятся больше, чем на 2 клетки, то этот объект просто заполняет пространство,
 * как-бы забивая его для обоих клеток разом
 * @author Kerravitarr
 */
public class ConnectiveTissue extends CellObject {
	/**Список тех, кого мы увязываем крестом*/
	private final AliveCell[] _friends = new AliveCell[Point.DIRECTION.size()];
	/**Число этих самых "друзей"*/
	private int countFriends = 0;
	
	/**
	 * Создаёт соединительную ткань
	 * @param where место, где она будет создана
	 * @param perrent кто её создатель? 
	 * @param comrades где находятся все подельники текущей клетки
	 */
	public ConnectiveTissue(Point where, AliveCell perrent, Point [] comrades) {
		super(perrent.getStepCount(), LV_STATUS.LV_CONNECTIVE_TISSUE);
        setPos(where);
		for (final var cell : comrades) {
			if (cell == null) break;
			setCell((AliveCell) Configurations.world.get(cell));
		}
		assert countFriends > 0 : "Почему-то у нас маловато друзей оказалось... " + comrades[0];
		setCell(perrent);
	}
	
	/**Функция получения семьи. Тех клеток, с кем текущая в тестных отношениях
	 * @return список из клеток, коих ровно DIRECTION.size(). Может быть пропущенно сколько-то ячеек...
	 */
	private AliveCell[] getCells() {
		return _friends;
	}
	/**Сохраняет нам нового члена многоклеточной семьи
	 * @param friend 
	 */
    public void setCell(AliveCell friend) {
		assert friend != null : "Забыли друга!!!";
		assert friend.getPos().distance(this.getPos()).getHypotenuse() != 2 : "Слишком близко. " + Arrays.toString(_friends) + ". Этот явно лишний: " + friend + " для меня " + this;
        int emptyIndex = -1;
		for (int i = 0; i < _friends.length; i++) {
			final var _friend = _friends[i];
			if(_friend == friend){
				return;
			} else if(_friend == null){
				emptyIndex = i;
			} else if(_friend.getPos().equals(friend.getPos())){
				assert false;
			}
		}
		assert countFriends < Point.DIRECTION.size() : "Многовато у нас друзей, не находите? " + Arrays.toString(_friends) + ". Этот явно лишний: " + friend;
		_friends[emptyIndex] = friend;
		countFriends++;
		friend.setComrades(this);
    }
	/**Удаляет этого из наших товарищей
	 * @param remove кого надо удалить
	 * @throws CellObjectRemoveException выкидывает, если связь больше ни кого не связывает
	 */
	public void removeCell(AliveCell remove) throws CellObjectRemoveException{
		assert countFriends > 0 : "Ожидается, что у нас всё ещё есть друзья";
		countFriends--;
		for (int i = 0; i < _friends.length; i++) {
			final var _friend = _friends[i];
			if(_friend == remove){
				_friends[i] = null;
				if(countFriends < 2) destroy();
				return;
			}
		}
		assert false : "Недостижимая часть кода. Не нашли " + remove + " среди " + Arrays.toString(_friends);
	}

	@Override
	void step() {
		
		//
		//  R-O-R
		//  |\|/|
		//  O-X-O
		//  |/|\|
		//  R-O-R
		// Нас интересуют только связи между клеткой O и клеткой R в противположном углу
		for (int i = 0; i < _friends.length - 1; i++) {
			final var f1 = _friends[i];
			if(f1 == null) continue;
			final var f1x = f1.getPos().x == this.getPos().x;
			final var f1y = f1.getPos().y == this.getPos().y;
			final var isR1 = !f1x && !f1y;
			for (int j = i + 1; j < _friends.length; j++) {
				final var f2 = _friends[j];
				if(f2 == null) continue;
				if(f1.getPos().x == f2.getPos().x || f2.getPos().y == this.getPos().y) continue; //Они на одной прямой - не интересуют
				final var f2x = f2.getPos().x == this.getPos().x;
				final var f2y = f2.getPos().y == this.getPos().y;
				final var isR2 = !f2x && !f2y;
				if(isR1 == isR2) continue; //Обе угловые и обе не угловые нас не интересуют!
				//И так. Теперь мы точно знаем, что у нас две клетки не на одной прямой и они разного типа... Надо проверить возможность создания связи
				
				
			}
		}
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public JSON toJSON(JSON make) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	public double getHealth() {
		return Arrays.stream(_friends).mapToDouble(f -> f == null ? 0 : f.getHealth()).sum();
	}

	@Override
	void setHealth(double h) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	boolean isRelative(CellObject bot0) {
		if (bot0 instanceof AliveCell) {
			for(var c : _friends)
				if(c == bot0)
					return true;
		}
		return false;
	}

	@Override
	public boolean toxinDamage(Poison.TYPE type, int damag) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	
	private static Color blend(int size, Color... c) {
		final float ratio = 255f / ((float) size);

		float a = 0;
		float r = 0;
		float g = 0;
		float b = 0;

		for (int i = 0; i < size; i++) {
			a += c[i].getAlpha() * ratio;
			r += c[i].getRed() * ratio;
			g += c[i].getGreen() * ratio;
			b += c[i].getBlue() * ratio;
		}

		return new Color(r, g, b, a);
	}
	@Override
	public void paint(Graphics g, Legend legend, int cx, int cy, int r) {
		final var colors = new Color[_friends.length];
		int cz = 0;
		for (AliveCell f : _friends) {
			if(f != null){
				colors[cz++] = f.getPaintColor(legend);
			}
		}
		//Цвет как средний по всем клеткам
		g.setColor(blend(cz,colors));
		final var points = new int[Point.DIRECTION.size()][2];
		var values = getCells();
		try {
			//Друзья
			for (int index = 0; index < values.length; index++) {
				final var i = values[index];
				if(i == null) {
					points[index][0] = Integer.MAX_VALUE;
					continue;
				}
				final var v = getPos().distance(i.getPos());
				int rxf = cx + v.x * r;
				int ryf = cy + v.y * r;
				final var lx = Math.abs(rxf - cx);
				final var ly = Math.abs(ryf - cy);
				if(lx > 2*r){
					//у нас расстояние больше радиуса - такого быть не может, так что мы должны нарисовать линию в другую сторону
					rxf = cx + (rxf < cx ? +r : -r);
				}
				if(ly > 2*r)
					ryf = cy + (ryf < cy ? +r : -r);
				points[index][0] = rxf;
				points[index][1] = ryf;
			}
			//Приходится рисовать в два этапа, иначе получается ужас страшный.
			//Этап первый - основные связи

			Graphics2D g2 = (Graphics2D) g;
			Stroke oldStr = g2.getStroke();
			g2.setStroke(new BasicStroke(r / 3));
			for (final int[] point : points) {
				if(point[0] == Integer.MAX_VALUE) continue;
				int delx = point[0] - cx;
				int dely = point[1] - cy;
				g.drawLine(cx, cy, cx + delx / 3, cy + dely / 3);
			}
			g2.setStroke(oldStr);
			g.setColor(Color.BLACK);
			//Этап второй, всё тоже самое, но теперь лишь тонкие линии
			for (final int[] point : points) {
				if(point[0] == Integer.MAX_VALUE) continue;
				g.drawLine(cx, cy, point[0], point[1]);
			}
		} catch (java.util.ConcurrentModificationException e) {/* Выскакивает, если кто-то из наших друзей погиб*/                }
		
	}
	
}
