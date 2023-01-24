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
    /**Каким ядом данная органика заражена*/
    private Poison.TYPE poison = Poison.TYPE.UNEQUIPPED;
    /**Как много в ней яда*/
    private double poisonCount = 0;
    

	public Organic(AliveCell cell) {
		super(cell.getStepCount(), LV_STATUS.LV_ORGANIC);
		setPos(cell.getPos());
		energy = Math.abs(cell.getHealth()) + AliveCell.MAX_HP/10.0 + cell.getMineral(); //Превращается в органику всё, что только может
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
    	poison = TYPE.toEnum(cell.getI("poison"));
    	poisonCount = cell.get("poisonCount");
    	super.color_DO = color_DO;
	}


	@Override
	public void step() {
		if((getAge()) % 2 == 0) { //  Скорость падения
			moveD(DIRECTION.DOWN);
		}
		energy -= 1.0/Configurations.TIK_TO_EXIT;
		poisonCount = getPoisonCount() * 0.99;	//1% выветривается постепенно. Тоже самое, как с нормальным ядом
		if(energy <= 0)
			destroy();
	}
	
	public boolean toxinDamage(TYPE type, int damag) {
		if (getPoison() == type) { // Наш яд, впитываем
			poisonCount = getPoisonCount() + damag;
		} else {
			if (getPoisonCount() >= damag) {
				poisonCount = getPoisonCount() - damag;
			} else {
				poison = type;
				poisonCount = damag - getPoisonCount();
			}
		}
        return false;
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
		make.add("poison", poison.ordinal());
		make.add("poisonCount", poisonCount);
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

	/**Возвращает тип яда, которым пропитанна органика*/
	public Poison.TYPE getPoison() {
		return poison;
	}
	/**Возвращает степень ядовитости органики*/
	public int getPoisonCount() {
		return (int) poisonCount;
	}

}
