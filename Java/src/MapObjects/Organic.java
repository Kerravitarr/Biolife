package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import MapObjects.Poison.TYPE;
import Utils.JSON;
import Utils.Utils;
import main.Configurations;
import main.Point.DIRECTION;
import panels.Legend;

public class Organic extends CellObject {
	/**Цвет орагиники*/
    private static Color ORGANIC_COLOR = new Color(139,69,19,200);
    /**Сколько в ораганизме остальсь еды*/
    private double energy;
    /**Каким ядом данная органика заражена*/
    private Poison.TYPE poison = Poison.TYPE.UNEQUIPPED;
    /**Как много в ней яда*/
    private double poisonCount = 0;
	/**Когда следующее деление*/
	public int nextDouble;
    

	public Organic(AliveCell cell) {
		super(cell.getStepCount(), LV_STATUS.LV_ORGANIC);
		setPos(cell.getPos());
		energy = Math.abs(cell.getHealth()) + cell.getFoodTank() + (cell.getMineral() + cell.getMineralTank()) * 10; //Превращается в органику всё, что только может
	    super.color_DO = ORGANIC_COLOR;
	    nextDouble = getTimeToNextDouble();
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
    	super.color_DO = ORGANIC_COLOR;
    	nextDouble = getTimeToNextDouble();
	}


	@Override
	public void step() {
		if((getAge()) % 2 == 0) { //  Скорость падения
			moveD(DIRECTION.DOWN);
		}
		energy -= 1.0/Configurations.TIK_TO_EXIT;
		if(poison != Poison.TYPE.UNEQUIPPED) {
			if (getAge() >= nextDouble) { // Вязкость яда
				DIRECTION dir = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));
				var nen = poisonCount /= 2.1; // 10% выветривается каждый раз, а половину своей энергии отдаём новой калпе
				if(Poison.createPoison(getPos().next(dir),poison,getStepCount(), nen, getStream())) {
					poisonCount = nen;
				}
	 			nextDouble = getTimeToNextDouble();
	 			if(poisonCount <= 1)
	 				poison = Poison.TYPE.UNEQUIPPED;
			}
		}
		if(energy <= 0){
			try {
				destroy();
			}catch (CellObjectRemoveException e) {
				if(poison != Poison.TYPE.UNEQUIPPED)
					Poison.createPoison(getPos(),poison,getStepCount(), poisonCount, getStream());
				throw e;
			}
		}
	}
	/**
	 * Вычисляет сколько шагов нужно для следующего разделения
	 * @return
	 */
	private int getTimeToNextDouble() {
		return (int) Math.round(getAge() + getStream() * (2 - energy / Poison.MAX_TOXIC));
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
		g.setColor(ORGANIC_COLOR);
		
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

	public int getStream() {
		return (int) (Poison.MAX_STREAM * getHealth() / AliveCell.MAX_HP);
	}


	@Override
	public void repaint() {
		switch (Legend.Graph.getMode()) {
			case POISON -> {
				var rg = (int) Utils.betwin(0, getHealth() / Poison.MAX_TOXIC, 1.0) * 255;
				switch (poison) {
					case BLACK -> color_DO = new Color(255-rg,255- rg,255- rg);
					case PINK -> color_DO = new Color(rg, rg / 2, rg / 2);
					case YELLOW -> color_DO = new Color(rg, rg, 0);
					case UNEQUIPPED ->  color_DO = ORGANIC_COLOR;
				}
				
			}
			default -> color_DO = ORGANIC_COLOR;
		}
	}

	/**Возвращает тип яда, которым пропитанна органика*/
	public Poison.TYPE getPoison() {
		return poison;
	}
	/**Возвращает степень ядовитости органики*/
	public int getPoisonCount() {
		return (int) poisonCount;
	}

}
