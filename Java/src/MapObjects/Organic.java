package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import MapObjects.Poison.TYPE;
import Utils.JSON;
import Utils.Utils;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;
import GUI.Legend;

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
	    nextDouble = getTimeToNextDouble();
	}
	/**
     * Загрузка клетки
     * @param cell - JSON объект, который содержит всю информацюи о клетке
	 * @param version версия протокола JSON
     */
    public Organic(JSON cell, long version) {
    	super(cell);
    	energy = cell.get("energy");
    	poison = TYPE.toEnum(cell.getI("poison"));
    	poisonCount = cell.get("poisonCount");
    	nextDouble = getTimeToNextDouble();
	}


	@Override
	public void step() {
		if(Configurations.confoguration.TIK_TO_EXIT != 0)
			energy -= 1.0/Configurations.confoguration.TIK_TO_EXIT;
		else
			energy = 0;
		if(poison != Poison.TYPE.UNEQUIPPED) {
			if (getAge() >= nextDouble) { // Вязкость яда
				DIRECTION dir = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));
				var nen = poisonCount / 2; // Половину своей энергии отдаём новой калпе
				if(Poison.createPoison(getPos().next(dir),poison,getStepCount(), nen, getStream())) {
					poisonCount = nen;	
				}
				//Оцениваем когда следующий раз поделимся
	 			nextDouble = getTimeToNextDouble();
	 			if(poisonCount <= 1)
	 				poison = Poison.TYPE.UNEQUIPPED;
			} else {
				//Так как мы ядовиты, то по чуть чуть растворяемся, то есть становимся ещё более ядовиты
				poisonCount += 1.0/Configurations.confoguration.TIK_TO_EXIT;
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
		return (int) Math.round(getAge() + getStream() * (2 - poisonCount / Poison.MAX_TOXIC));
	}
	
	@Override
	public boolean move(DIRECTION direction) {
		var pos = getPos().next(direction);
		switch (see(pos)) {
			case WALL, CLEAN, OWALL, ALIVE, BANE -> {
				return super.move(direction);
			}
			case ORGANIC -> {
				final var org = (Organic) Configurations.world.get(pos);
				final var yMin =  Math.min(getPos().getY(), org.getPos().getY());
				final var yMax =  Math.max(getPos().getY(), org.getPos().getY());
				final var hpMin =  Math.min(getHealth(), org.getHealth());
				final var hpMax =  Math.max(getHealth(), org.getHealth());
				//100_000_000 - если больше сделать, то 0.1 уже не помещается в double и органика становится бесконечной
				if(hpMin * yMax >= hpMax * yMin && hpMax < 100_000_000){ //Маленькие кусочки слипаются
					org.addHealth(getHealth());
					if(poison != Poison.TYPE.UNEQUIPPED)
						org.toxinDamage(poison, (int) poisonCount);
					destroy(); //Мы слепились со следующей и всё, пошли отсюда. Тут будет исключение
					return false;
				} else if(org.getHealth() < getHealth()){
					Configurations.world.swap(this, pos); //Мы поменялись с кусочком, упав ниже
					return true;
				} else {
					//Отдаём свой импульс этой органике, пущай теперь и выпутывается!
					Configurations.world.get(getPos().next(direction)).move(direction,1);
					return true;
				}
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + see(direction));
		}
	}

	@Override
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
		poisonCount = Math.min(poisonCount, Poison.MAX_TOXIC * 10);
		nextDouble = Math.min(nextDouble,getTimeToNextDouble());
        return false;
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
		return (int) Math.min(Poison.MAX_STREAM, Poison.MAX_STREAM * getHealth() / AliveCell.MAX_HP);
	}

	/**Возвращает тип яда, которым пропитанна органик
	 * @return тип яда, которым заражена органики
	 */
	public Poison.TYPE getPoison() {
		return poison;
	}
	/**Возвращает степень ядовитости органик
	 * @return количество очков яда, которые уже есть в клетке
	 */
	public int getPoisonCount() {
		return (int) poisonCount;
	}
	
	
	/** * Не смог я в этот раз уйти от рисования...Очень жаль :(
		Эта функция должна отобразить объект на холсте согласно установленному режиму
	 * @param g где рисуем
	 * @param legend легенда, по которой рисуем
	 * @param cx координата ЦЕНТРА на холсте, где клетка находится
	 * @param cy координата ЦЕНТРА на холсте, где клетка находится
	 * @param r размер в пк квадрата, которым клетка окружена
	 */
	@Override
	public void paint(Graphics g, Legend legend, int cx, int cy, int r){
		Color color_DO;
		switch (legend.getMode()) {
			case POISON -> {
				var rg = (int) Utils.betwin(0, getHealth() / Poison.MAX_TOXIC, 1.0) * 255;
				switch (getPoison()) {
					case BLACK -> color_DO = new Color(255-rg,255- rg,255- rg);
					case PINK -> color_DO = new Color(rg, rg / 2, rg / 2);
					case YELLOW -> color_DO = new Color(rg, rg, 0);
					case UNEQUIPPED ->  color_DO = ORGANIC_COLOR;
					default ->  throw new IllegalArgumentException("Unexpected value: " + getPoison());
				}

			}
			case HP -> color_DO = legend.HPtToColor(getHealth());
			case YEAR -> color_DO = legend.AgeToColor(getAge());
			default -> color_DO = ORGANIC_COLOR;
		}
		g.setColor(color_DO);

		if (g instanceof Graphics2D g2d) {
			Stroke old = g2d.getStroke();
			g2d.setStroke(new BasicStroke(r/3));
			Utils.drawCircle(g, cx, cy, r * 2 / 3);
			g2d.setStroke(old);
		} else {
			Utils.fillCircle(g,cx,cy,r);
		}
	}
}
