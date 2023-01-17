package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import Utils.JSON;
import Utils.Utils;
import main.Point.DIRECTION;

/**
 * Ороговевшая клетка. Она превратилась в стену и теперь защищает других от себя
 * @author Илья
 *
 */
public class Fossil extends CellObject {
	/**Цвет стены*/
    private static Color color_DO = new Color(64, 56, 56,200);
	/**Сколько у нас энергии*/
	private double energy = 0;

	public Fossil(JSON poison) {
		super(poison);
		setHealth(Math.round((double)poison.get("energy")));
		repaint();
	}


	public Fossil(AliveCell cell) {
		super(cell.getStepCount(), LV_STATUS.LV_WALL);
		setPos(cell.getPos());
		energy = Math.abs(cell.getHealth()) + AliveCell.MAX_HP/10.0 + cell.getMineral()/10.0; //Превращается в органику всё, что только может
	    super.color_DO = color_DO;
	}


	@Override
	void step() {
		if (energy <= 1) { // Наша сила
			destroy();
		} else {
		}
	}
	
	/**
	 * Неподвижный объект
	 */
	@Override
	public boolean move(DIRECTION direction) {
		return true;
	}

	@Override
	public boolean toxinDamage(Poison.TYPE type, int damag) {
		addHealth(-damag/10); // Мы компенсируем другие яды
		return energy <= 1;
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(color_DO);
		
		int r = (int) Math.round(getPos().getRr()*1);
		int rx = getPos().getRx();
		int ry = getPos().getRy();
		Utils.fillSquare(g,rx,ry,r);
	}

	@Override
	public JSON toJSON(JSON make) {
		make.add("energy", energy);
		return make;
	}

	@Override
	public double getHealth() {
		return Math.round(energy);
	}

	@Override
	void setHealth(double h) {
		energy = h;
	}

	@Override
	boolean isRelative(CellObject cell0) {
		if (cell0 instanceof Fossil) {
			Fossil poison = (Fossil) cell0;
		    return true;
		} else {
			return false;
		}
	}

	@Override
	public void repaint() {
		/*switch (Legend.Graph.getMode()) {
			case HP -> color_DO = Utils.getHSBColor(0, 0, 0.5, 1.0);
			default -> {
				color_DO = Utils.getHSBColor(0, 0, 0.5, 1.0);
			}
		}*/
	}
}
