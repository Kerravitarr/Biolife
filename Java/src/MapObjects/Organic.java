package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import Utils.JSONmake;
import Utils.Utils;
import main.World;
import main.Point.DIRECTION;

public class Organic extends CellObject {
	/**Цвет орагиники*/
    private static Color color_DO = new Color(139,69,19,200);
    /**Сколько в ораганизме остальсь еды*/
    private double energy;

	public Organic(AliveCell cell) {
		super(cell.stepCount, LV_STATUS.LV_ORGANIC);
		setPos(cell.getPos());
		energy = Math.abs(cell.getHealth()) + cell.hpForDiv/10.0 + cell.getMineral()/10.0; //Превращается в органику всё, что только может
	    super.color_DO = color_DO;
	}
	/**
     * Загрузка клетки
     * @param cell - JSON объект, который содержит всю информацюи о клетке
     * @param tree - Дерево эволюции 
     */
    public Organic(JSONmake cell) {
    	super(cell);
    	energy = cell.getD("energy");
    	super.color_DO = color_DO;
	}


	@Override
	public void step() {
		if((getAge()) % 2 == 0) { //  Скорость падения
			moveD(DIRECTION.DOWN);
			energy -= 1.0/World.TIK_TO_EXIT;
			if(energy <= 0)
				remove();
		}
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
	public JSONmake toJSON(JSONmake make) {
		make.add("energy", energy);
		return make;
	}


	@Override
	public long getHealth() {
		return Math.round(energy);
	}


	@Override
	void setHealth(long h) {
		energy = h;
	}


	@Override
	boolean isRelative(CellObject bot0) {
		return false;
	}


	@Override
	public void repaint() {}

}
