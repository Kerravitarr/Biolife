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
	public AliveCell[] getCells() {
		return _friends;
	}
	/**Возвращает количество связей у этой... Связи
	 * @return 
	 */
	public int size(){return countFriends;}
	/**
	 * Проверяет наличие клетки среди связей
	 * @param ac клетка, связь с которой мы ищем
	 * @return true, если такая связь есть
	 */
	public boolean contains(AliveCell ac){
		for (AliveCell _friend : _friends) {
			if(_friend == ac){
				return true;
			}
		}
		return false;
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
				remove.removeComrades(this);
				_friends[i] = null;
				if(countFriends < 2) remove_NE(); //Нас тоже больше не будет. Мы тоже удаляемся
				return;
			}
		}
		assert false : "Недостижимая часть кода. Не нашли " + remove + " среди " + Arrays.toString(_friends);
	}
	
    @Override
    public void destroy() {
		for (AliveCell _friend : _friends) {
			if(_friend != null) _friend.removeComrades(this);
		}
        super.destroy();
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
				if(Math.abs(f1.getPos().x - f2.getPos().x) <= 1 && Math.abs(f1.getPos().y - f2.getPos().y) <= 1 ){
					//Это две клетки, которые стоят рядом! Они больше не наши
 					if(countFriends == 2){
						 destroy();
					} else {
						removeCell(f1);
						removeCell(f2);
					}
					j = _friends.length;
					continue;
				}
				if(f1.getPos().x == f2.getPos().x || f1.getPos().y == f2.getPos().y) continue;//Они на одной прямой. не наш случай
				final var f2x = f2.getPos().x == this.getPos().x;
				final var f2y = f2.getPos().y == this.getPos().y;
				final var isR2 = !f2x && !f2y;
				if(isR1 == isR2) continue; //Обе угловые и обе не угловые нас не интересуют!
				//И так. Теперь мы точно знаем, что у нас две клетки не на одной прямой и они разного типа... Надо проверить возможность создания связи
				final Point p1, p2;
				if(Math.abs(f1.getPos().x - f2.getPos().x) == 2){
					//У нас одна из точек на горизонтальной прямой
					final var x = (f1.getPos().x + f2.getPos().x) / 2;
					p1 = Point.create(x,f1.getPos().y);
					p2 = Point.create(x,f2.getPos().y);
				} else {
					//У нас одна из точек на вертикальной прямой
					final var y = (f1.getPos().y + f2.getPos().y) / 2;
					p1 = Point.create(f1.getPos().x,y);
					p2 = Point.create(f2.getPos().x,y);
				}
				final var p = p1.equals(this.getPos()) ? p2 : p1;
				final var o = Configurations.world.get(p);
				if(o == null){
					//Создаём дублирующую связь
					Configurations.world.add(new ConnectiveTissue(p, f1,new Point[]{f2.getPos()}));
				} else if(o instanceof ConnectiveTissue ct){
					//Добавляем связь, если такой там нет
					if(Arrays.stream(ct._friends).filter(f -> f == f1 || f == f2).findFirst().orElse(null) != null){
						//Один из двух товарищей уже связан с этой связью, значит можно через неё прокинуть ещё одну связь
						ct.setCell(f1);
						ct.setCell(f2);
					}
				}
			}
		}
	}
	
    @Override
    public boolean move(Point.DIRECTION direction) {
		final var target = getPos().next(direction);
		//Если у нас до какой-то из клеток будет больше 2 клеток - то мы не можем походить
		boolean isNot = Arrays.stream(_friends).filter(f -> f != null && (Math.abs(f.getPos().x - target.x) > 1 || Math.abs(f.getPos().y - target.y) > 1)).findFirst().orElse(null) != null;
		if(isNot){
			//Делимся импульсом со всеми дочерними клетками
			final var dx = getImpuls().x / countFriends;
			final var dy = getImpuls().y / countFriends;

			for (AliveCell _friend : _friends) {
				if(_friend != null){
					_friend.getImpuls().x += dx;
					_friend.getImpuls().y += dy;
				}
			}
			getImpuls().x = getImpuls().y = 0;
			return false;
		} else {
			return super.move(direction);
		}
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
		final float ratio = 1f / (size * 255f);

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
