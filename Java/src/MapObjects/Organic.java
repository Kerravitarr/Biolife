package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import MapObjects.Poison.TYPE;
import Utils.JSON;
import Utils.Utils;
import main.Configurations;
import main.Point.DIRECTION;

public class Organic extends CellObject {
	/**Цвет орагиники*/
    private static Color color_DO = new Color(139,69,19,200);
    /**Сколько в ораганизме остальсь еды*/
    private double energy;

	public Organic(AliveCell cell) {
		super(cell.getStepCount(), LV_STATUS.LV_ORGANIC);
		setPos(cell.getPos());
		energy = Math.abs(cell.getHealth()) + AliveCell.MAX_HP/10.0 + cell.getMineral()/10.0; //Превращается в органику всё, что только может
	    super.color_DO = color_DO;
	}
	/**
     * Загрузка клетки
     * @param cell - JSON объект, который содержит всю информацюи о клетке
     * @param tree - Дерево эволюции 
     */
    public Organic(JSON cell) {
    	super(cell);
    	energy = cell.get("energy");
    	super.color_DO = color_DO;
	}


	@Override
	public void step() {
		if((getAge()) % 2 == 0) { //  Скорость падения
			moveD(DIRECTION.DOWN);
			energy -= 1.0/Configurations.TIK_TO_EXIT;
			if(energy <= 0)
				destroy();
		}
	}
	
	public boolean toxinDamage(TYPE type, int damag) {
		setHealth(-getHealth());//Мы становимся полностью ядовитыми
        return true;
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(color_DO);
		
		int r = getPos().getRr();
		int rx = getPos().getRx();
		int ry = getPos().getRy();
		Utils.fillCircle(g,rx,ry,r);
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
	boolean isRelative(CellObject bot0) {
		return false;
	}


	@Override
	public void repaint() {}

}
