package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import Utils.JSON;
import Utils.Utils;
import main.Point.DIRECTION;
import panels.Legend;

/**
 * Ороговевшая клетка. Она превратилась в стену и теперь защищает других от себя
 * @author Илья
 *
 */
public class Fossil extends CellObject {
	/**Цвет стены*/
    private static final Color COLOR_DO = new Color(64, 56, 56,200);
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
	    super.color_DO = COLOR_DO;
	}


	@Override
	void step() {
		if (energy <= 1) { // Наша энергия
			destroy();
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
		switch (type) {
			case PINK-> {addHealth(-damag);return getHealth() <= 1;}
			case YELLOW,BLACK-> {return false;}
			case UNEQUIPPED -> throw new UnsupportedOperationException("Unimplemented case: " + type);
		}
		return true;
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(super.color_DO);
		
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
		if(h > 0)
			energy = h;
		energy = h;
	}

	@Override
	boolean isRelative(CellObject cell0) {
		if (cell0 instanceof Fossil) {
			//Fossil poison = (Fossil) cell0;
		    return true;
		} else {
			return false;
		}
	}

	@Override
	public void repaint() {
		switch (Legend.Graph.getMode()) {
			case MINERALS -> super.color_DO = new Color(0,0,0);
			case GENER -> super.color_DO = new Color(0,0,0);
			case YEAR -> super.color_DO = Utils.getHSBColor(Math.max(0, (1.0*getAge()/Legend.Graph.getMaxAge())), 1, 1,1);
			case HP -> super.color_DO = new Color((int) (Utils.betwin(0.0, getHealth()/AliveCell.MAX_HP, 1.0)*255),0,0);
			case PHEN -> super.color_DO = COLOR_DO;
			case DOING -> super.color_DO = COLOR_DO;
		}
	}
}
