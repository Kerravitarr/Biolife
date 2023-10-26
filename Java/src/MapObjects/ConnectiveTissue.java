/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MapObjects;

import Calculations.Configurations;
import Calculations.Point;
import GUI.AllColors;
import GUI.Legend;
import MapObjects.AliveCellProtorype.ACTION;
import Utils.JSON;
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
public class ConnectiveTissue extends CellObject implements AliveCellProtorype.AliveCellI{
	/**Список тех, кого мы увязываем крестом*/
	private final CellObject[] _friends = new CellObject[Point.DIRECTION.size()];
	/**Число этих самых "друзей"*/
	private int countFriends = 0;
	/**Число клеток среди наших друзей*/
	private int countAliveCell = 0;
	/**Загружает связь из файла сохранения
	 * @param cell описание связи
	 * @param version версия файла JSON
	 */
    public ConnectiveTissue(JSON cell, long version) {
		super(cell);
	}
	/**
	 * Создаёт соединительную ткань
	 * @param where место, где она будет создана
	 * @param perrent кто её создатель? 
	 * @param comrades где находятся все подельники текущей клетки
	 */
	public ConnectiveTissue(Point where, AliveCell perrent, Point [] comrades) {
		super(perrent.getStepCount(), LV_STATUS.LV_CONNECTIVE_TISSUE);
		setImpuls(perrent.getImpuls());
		init(where,comrades);
		setConnect(perrent);
	}
	/**
	 * Создаёт соединительную ткань
	 * @param where место, где она будет создана
	 * @param perrent кто её создатель? 
	 * @param comrades где находятся все подельники текущей клетки
	 */
	public ConnectiveTissue(Point where, AliveCell perrent, CellObject [] comrades) {
		super(perrent.getStepCount(), LV_STATUS.LV_CONNECTIVE_TISSUE);
		setImpuls(perrent.getImpuls());
        setPos(where);
		for (final var o : comrades) {
			if (o == null) break;
			if(o instanceof AliveCell ac)
				setConnect(ac);
			else if(o instanceof ConnectiveTissue ct)
				ConnectiveTissue.this.setConnect(ct);
			else
				assert false : "А тут у нас не известный объект..." + o;
		}
		assert countFriends > 0 : "Почему-то у нас маловато друзей оказалось... " + comrades[0];
		setConnect(perrent);
	}
	/**
	 * Создаёт соединительную ткань
	 * @param where место, где она будет создана
	 * @param perrent кто её создатель? 
	 * @param comrades где находятся все подельники текущей клетки
	 */
	public ConnectiveTissue(Point where, ConnectiveTissue perrent, Point [] comrades) {
		super(perrent.getStepCount(), LV_STATUS.LV_CONNECTIVE_TISSUE);
		setImpuls(perrent.getImpuls());
		init(where,comrades);
		ConnectiveTissue.this.setConnect(perrent);
	}
	/**
	 * Инициализирует связь
	 * @param where где эта связь будет находиться, её позиция
	 * @param comrades все связи этой связи
	 */
	private void init(Point where, Point [] comrades){
        setPos(where);
		for (final var cell : comrades) {
			if (cell == null) break;
			final var o = Configurations.world.get(cell);
			if(o instanceof AliveCell ac)
				setConnect(ac);
			else if(o instanceof ConnectiveTissue ct)
				ConnectiveTissue.this.setConnect(ct);
			else
				assert false : "А тут у нас не известный объект..." + o;
		}
		assert countFriends > 0 : "Почему-то у нас маловато друзей оказалось... " + comrades[0];
	}
	
	/**Функция получения семьи. Тех клеток, с кем текущая в тестных отношениях
	 * @return список из клеток, коих ровно DIRECTION.size(). Может быть пропущенно сколько-то ячеек...
	 */
	public CellObject[] getCells() {
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
		for (final var _friend : _friends) {
			if(_friend == ac){
				return true;
			}
		}
		return false;
	}
	
	@Override
    public boolean setConnect(AliveCell.AliveCellI other) {
		if(other instanceof AliveCell friend){
			if(_setCell(friend)){
				friend.setConnect(this);
				countAliveCell++;
				return true;
			} else {
				return false;
			}
		} else if(other instanceof ConnectiveTissue friend){
			if(_setCell(friend)){
				friend.setConnect(this);
				return true;
			} else {
				return false;
			}
		} else {
			assert !(other instanceof AliveCell.AliveCellI): "Недостижимая часть кода, когда пришёл " + other;
			return false;
		}
	}
	/**Сохраняет нам нового члена многоклеточной семьи
	 * @param friend 
	 */
    private boolean _setCell(CellObject friend) {
		assert friend != null : "Забыли друга!!!";
		assert friend != this : "Вам не кажется это странным?";
		assert friend.getPos().distance(this.getPos()).getHypotenuse() != 2 : "Слишком близко. " + 
				Arrays.toString(_friends) + ". Этот явно лишний: " + friend + " для меня " + this;
        int emptyIndex = -1;
		for (int i = 0; i < _friends.length; i++) {
			final var _friend = _friends[i];
			if(_friend == friend){
				return false;
			} else if(_friend == null){
				emptyIndex = i;
			} else {
				if(_friend.getPos().equals(friend.getPos()))
				assert !_friend.getPos().equals(friend.getPos()) : "У нас точки совпали, а объекты - нет... Магия!. Объекты: " + _friend + " и " + friend;
			}
		}
		assert countFriends < Point.DIRECTION.size() : "Многовато у нас друзей, не находите? " + Arrays.toString(_friends) + ". Этот явно лишний: " + friend;
		_friends[emptyIndex] = friend;
		countFriends++;
		return true;
    }
	/**Удаляет этого из наших товарищей
	 * @param remove кого надо удалить
	 * @throws CellObjectRemoveException выкидывает, если связь больше ни кого не связывает
	 */
	public void removeCell(ConnectiveTissue remove) throws CellObjectRemoveException{
		if(_removeCell(remove))
			remove.removeCell(this);
	}
		
	/**Удаляет этого из наших товарищей
	 * @param remove кого надо удалить
	 * @throws CellObjectRemoveException выкидывает, если связь больше ни кого не связывает
	 */
	public void removeCell(AliveCell remove) throws CellObjectRemoveException{
		if(_removeCell(remove)){
			remove.removeComrades(this);
			countAliveCell--;
		}
	}
	/**Удаляет этого из наших товарищей
	 * @param remove кого надо удалить
	 * @throws CellObjectRemoveException выкидывает, если связь больше ни кого не связывает
	 */
	private boolean _removeCell(CellObject remove) throws CellObjectRemoveException{
		for (int i = 0; i < _friends.length; i++) {
			final var _friend = _friends[i];
			if(_friend == remove){
				_friends[i] = null;
				assert countFriends > 0 : "Ожидается, что у нас всё ещё есть друзья";
				countFriends--;
				return true;
			}
		}
		return false;
		//assert false : "Недостижимая часть кода. Не нашли " + remove + " среди " + Arrays.toString(_friends);
	}
	
    @Override
    public void destroy() {
		for (final var _friend : _friends) {
			if (_friend instanceof AliveCell ac) {
				removeCell(ac);
			} else if(_friend instanceof ConnectiveTissue ct) {
				removeCell(ct);
			}
		}
        super.destroy();
    }

	@Override
	void step() {
		if(countFriends < 2 || countAliveCell < 2) destroy(); //Если у нас нет связей, то мы удаляемся
		if(Math.abs(getImpuls().x) > 70 || Math.abs(getImpuls().y) > 70) destroy(); //От перенапряжения связь тоже может лопнуть
		
		//
		//  R-O-R
		//  |х|х|
		//  O-X-O
		//  |х|х|
		//  R-O-R
		// 
		//А ещё для уменьшения матана, за один ход мы тестируем только одну ячейку
		final var f1 = _friends[(int)(getAge()) % _friends.length];
		if(f1 != null){
			final var aci1 = (AliveCell.AliveCellI) f1;
			final var f1x = f1.getPos().x == this.getPos().x;
			final var f1y = f1.getPos().y == this.getPos().y;
			final var isR1 = !f1x && !f1y;
			for (final var f2 : _friends) {
				if(f2 == null || f2 == f1) continue;
				final var aci2 = (AliveCell.AliveCellI) f2;
				if(f1.getPos().distance(f2.getPos()).getHypotenuse() < 2 && f1 instanceof AliveCell ac1 && f2 instanceof AliveCell ac2){
					//Это две клетки, которые стоят рядом!
					//Если между ними есть связи, то это больше не наши клиенты
					//А если связи ещё нет - то мы её создадим :)
					if(!aci1.setConnect(aci2)){
						removeCell(ac1);
						removeCell(ac2);
					}
					break;
				}
				final var f2x = f2.getPos().x == this.getPos().x;
				final var f2y = f2.getPos().y == this.getPos().y;
				final var isR2 = !f2x && !f2y;
				final Point pointNewConnect;
				if(isR1 && isR2){
					//Они обе угловые. Между ними образуется новая связь
					if(f1.getPos().x == f2.getPos().x || f1.getPos().y == f2.getPos().y)
						pointNewConnect = f1.getPos().next(f1.getPos().distance(f2.getPos()).direction());
					else
						continue; //Правда, если они накрест - то не станем-же мы добавлять себя себе
				} else if(!isR1 && !isR2){ //Это две промежуточные
					if(f1.getPos().x != f2.getPos().x && f1.getPos().y != f2.getPos().y){
						//Это две соседние клетки. Между ними надо создать связь
						aci1.setConnect(aci2);
					}
					continue; //А больше нас и не интересуют такие клетки
				} else if(f1.getPos().x == f2.getPos().x || f1.getPos().y == f2.getPos().y) { //Они типов разных, но стоят на одной прямой. Значит они соседние.
					//Между ними надо создать связь
					aci1.setConnect(aci2);
					continue; //Они типов разных, но стоят на одной прямой
				} else {//А вот тут интереснее - они разных типов и они не на одной прямой
					if(isR1){
						//У нас f1 - угловой, а значит f2 находится прям за нами
						pointNewConnect = f1.getPos().next(this.getPos().direction(f2.getPos()));
					} else {
						pointNewConnect = f2.getPos().next(this.getPos().direction(f1.getPos()));
					}
				}
				final var o = Configurations.world.get(pointNewConnect);
				if(o == null){
					//Создаём дублирующую связь
					if(f1 instanceof AliveCell ac) Configurations.world.add(new ConnectiveTissue(pointNewConnect, ac,new Point[]{f2.getPos(), this.getPos()}));
					else Configurations.world.add(new ConnectiveTissue(pointNewConnect, (ConnectiveTissue) f1,new Point[]{f2.getPos(), this.getPos()}));
				} else if(o instanceof ConnectiveTissue ct){
					//Добавляем связь, если такой там нет
					if(Arrays.stream(ct._friends).filter(f -> f == f1 || f == f2).findFirst().orElse(null) != null){
						//Один из двух товарищей уже связан с этой связью, значит можно через неё прокинуть ещё одну связь
						ct.setConnect(aci1);
						ct.setConnect(aci2);
						ct.setConnect(this);
					}
				}
			}
		}
		if(countFriends < 2 || countAliveCell < 2) destroy(); //Если у нас нет связей, то мы удаляемся
		
		//Тогда делимся всем, что имеем со всеми
		double allHp = getHealth() / countAliveCell;
        long allMin = getMineral() / countAliveCell;
        for (var comrad : _friends) {
			if(comrad instanceof AliveCell cell){
				if (allHp > cell.getHealth()) {
					cell.color(ACTION.RECEIVE, allHp - cell.getHealth());
				} else {
					cell.color(ACTION.GIVE, cell.getHealth() - allHp);
				}
				if (allMin > cell.getMineral()) {
					cell.color(ACTION.RECEIVE, allMin - cell.getMineral());
				} else {
					cell.color(ACTION.GIVE, cell.getMineral() - allMin);
				}
				cell.setHealth(allHp);
				cell.setMineral(allMin);
			}
        }
	}
	
    @Override
    public boolean move(Point.DIRECTION direction) {
		if(countFriends < 2 || countAliveCell < 2) return false;
		final var target = getPos().next(direction);
		//Если у нас до какой-то из клеток будет больше 2 клеток - то мы не можем походить
		boolean isNot = Arrays.stream(_friends).filter(f -> f != null && (Math.abs(f.getPos().x - target.x) > 1 || Math.abs(f.getPos().y - target.y) > 1)).findFirst().orElse(null) != null;
		if(isNot){
			//Перераспределяем импульс
			var px = getImpuls().x;
			var py = getImpuls().y;
			var m = 1d;
			for (final var _friend : _friends) {
				if(_friend == null) continue;
				if(_friend instanceof AliveCell){
					px += _friend.getImpuls().x * _friend.getHealth();
					py += _friend.getImpuls().y * _friend.getHealth();
					m += _friend.getHealth();
				} else if(_friend instanceof ConnectiveTissue){
					px += _friend.getImpuls().x;
					py += _friend.getImpuls().y;
					m += 1;
				}
			}
			px /= m;
			py /= m;

			for (final var _friend : _friends) {
				if(_friend != null){
					_friend.setImpuls(px, py);
				}
			}
			setImpuls(px, py);
			return false;
		} else {
			return super.move(direction);
		}
	}

	@Override
	public JSON toJSON(JSON make) {
        JSON[] fr = new JSON[countFriends];
        final var comrads = getCells();
        for (int ic = 0, ifr = 0; ic < comrads.length; ic++) {
			final var cell = comrads[ic];
			if(cell == null) continue;
            fr[ifr++] = (cell.getPos()).toJSON();
        }
        make.add("friends", fr);
        return make;
	}

	@Override
	public double getHealth() {return Arrays.stream(_friends).mapToDouble(f -> (f == null || f instanceof ConnectiveTissue) ? 0 : f.getHealth()).sum();}
	@Override
	public long getMineral() {return Arrays.stream(_friends).mapToLong(f -> (f == null || f instanceof ConnectiveTissue) ? 0 : ((AliveCell)f).getMineral()).sum();}
	@Override
	public int getMucosa() {return Arrays.stream(_friends).mapToInt(f -> (f == null || f instanceof ConnectiveTissue) ? 0 : ((AliveCell)f).getMucosa()).sum();}
	@Override
	public void addHealth(double h){
		if(countAliveCell > 0){
			final var addh = h / countAliveCell;
			for(final var f : _friends)
				if(f != null && f instanceof AliveCell ac){
					if(h < 0 && -h > ac.getHealth())
						try{ac.bot2Organic();} catch (CellObjectRemoveException e) {}//Увы, клетка того. Умирает
					else
						ac.addHealth(addh);
				}
		}
	}
	@Override
	public void addMineral(long m){
		if(countAliveCell > 0 && m > countAliveCell){
			final var addM = (long)(m / countAliveCell);
			for(final var f : _friends)
				if(f != null && f instanceof AliveCell ac)
					ac.addMineral(addM);
		}
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
		var isDamag = false;
		if(countAliveCell > 0){
			final var d = damag / countAliveCell;
			for(final var f : _friends)
				if(f != null && f instanceof AliveCell ac){
					if(isDamag |= ac.toxinDamage(type, d))
						try {ac.bot2Organic();} catch (CellObjectRemoveException e) {} //Увы, даже такую каплю не смогли выдержать
				}
		}
		return isDamag;
	}
	
	@Override
	public Color getPaintColor(Legend legend){
		final var colors = new Color[_friends.length];
		int cz = 0;
		for (final var f : _friends) {
			if(f instanceof AliveCell ac){
				colors[cz++] = ac.getPaintColor(legend);
			}
		}
		//Цвет как средний по всем клеткам
		return AllColors.blend(cz,colors);
	}
	@Override
	public void paint(Graphics2D g, int cx, int cy, int r) {
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
