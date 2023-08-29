package MapObjects;

import static MapObjects.AliveCellProtorype.MAX_MP;
import java.awt.Color;
import java.awt.Graphics;

import Utils.JSON;
import Utils.Utils;
import main.Configurations;
import main.Point.DIRECTION;
import panels.Legend;

/**
 * Ороговевшая клетка. Она превратилась в стену и теперь защищает других от себя
 * @author Илья
 *
 */
public class Fossil extends CellObject {
	/**Цвет стены*/
    private static final Color COLOR_DO = Color.BLACK;
	/**Сколько у нас энергии*/
	private double energy = 0;
	/**Стены не бессмертны, напротив, эроизия разлагает их*/
	private static final int MAX_AGE = 1_000_000;

	public Fossil(JSON poison, long version) {
		super(poison);
		energy = (double)poison.get("energy");
		super.color_DO = COLOR_DO;
		repaint();
	}


	public Fossil(AliveCell cell) {
		super(cell.getStepCount(), LV_STATUS.LV_WALL);
		setPos(cell.getPos());
		energy = Math.abs(cell.getHealth()) + cell.getFoodTank() + (cell.getMineral() + cell.getMineralTank()) * 10; //Превращается в органику всё, что только может
	    super.color_DO = COLOR_DO;
		repaint();
	}


	@Override
	void step() {
		if (energy <= 1) { // Наша энергия
			destroy();
		} else if(getAge() > MAX_AGE){
			energy--;
		}
	}
	
	/**
	 * Неподвижный объект
	 */
	@Override
	public boolean move(DIRECTION direction) {
		return false;
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
		int xCenter = getPos().getRx();
		int yCenter = getPos().getRy();
		
		g.fillRect(xCenter-r/2, yCenter-r/6,r, r/3);
		g.fillRect(xCenter-r/6, yCenter-r/2,r/3, r);
	}

	@Override
	public JSON toJSON(JSON make) {
		make.add("energy", energy);
		return make;
	}

	@Override
	public double getHealth() {
		return energy;
	}
	@Override
	void setHealth(double h) {
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
		final var legend = Configurations.legend;
		switch (legend.getMode()) {
			case HP -> color_DO = legend.HPtToColor(getHealth());
			case YEAR -> color_DO = legend.AgeToColor(((double)getAge())/MAX_AGE);
			default -> color_DO = COLOR_DO;
		}
	}
}
