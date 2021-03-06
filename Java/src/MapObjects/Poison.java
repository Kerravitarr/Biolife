package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import Utils.JSON;
import Utils.Utils;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;
import panels.Legend;

public class Poison extends CellObject {
	/**Максимальная токсичность яда*/
	static final int MAX_TOXIC = 2000;
	
	public enum TYPE {
		НЕТ, ЖЁЛ, РОЗ, ЧЁР;
		private static TYPE[] vals = values();

		public static TYPE toEnum(int num) {
			while (num >= vals.length)
				num -= vals.length;
			while (num < 0)
				num += vals.length;
			return vals[num];
		}

		static int size() {
			return vals.length;
		}
	};
	/**Сколько у нас энергии*/
	private double energy = 0;
	/**Каков тип яда*/
	public TYPE type;
	/**Каков наш радиус в частях одной клетки!!!!*/
	public double radius = 1;
	/**Когда следующее деление*/
	public int nextDouble;

	public Poison(JSON poison) {
		super(poison);
		setHealth(Math.round((double)poison.get("energy")));
		type = TYPE.toEnum(poison.getI("type"));
		nextDouble = getTimeToNextDouble();
		repaint();
	}

	public Poison(TYPE type, long stepCount, Point point, double newEnergy) {
		super(stepCount, LV_STATUS.LV_POISON);
		setPos(point);
		setHealth(newEnergy);
		this.type=type;
		nextDouble = getTimeToNextDouble();
		repaint();
	}

	@Override
	void step() {
		if(Configurations.POISON_STREAM == 0) //При таком уровне разложения, яд сразу исчезает
			destroy();
		if ((getAge()) >= nextDouble) { // Вязкость яда
			DIRECTION dir = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));
			switch (see(dir)) {
				case WALL: return; //Ну что мы можем сделать со стеной? О_О
				case CLEAN:{
					energy /= 2.1; // 10% выветривается каждый раз, а половину своей энергии отдаём новой калпе
					if(getHealth() < 1) destroy();//Мы растартили всю нашу ядовитость, мы того - усё
					Point point = getPos().next(dir);
					Poison newPoison = new Poison(type,getStepCount(),point,getHealth());
		            Configurations.world.add(newPoison);//Сделали новую каплю
				}break;
				case ORGANIC :
				case ENEMY:
				case FRIEND:
				case POISON:
				case NOT_POISON:{
					energy /= 2.1; // 10% выветривается каждый раз, а половину своей энергии отдаём новой калпе
					if(getHealth() < 1) destroy();//Мы растартили всю нашу ядовитость, мы того - усё
					Point point = getPos().next(dir);
					CellObject cell = Configurations.world.get(point);
					if(cell.toxinDamage(type,(int) (getHealth()))) {
						cell.remove_NE();
						Poison newPoison = new Poison(type,getStepCount(),point,Math.abs(cell.getHealth()));
			            Configurations.world.add(newPoison);//Сделали новую каплю
					} // А иначе мы не создаём просто нашу копию, нас-же переварили
				}break;
				default:
				throw new IllegalArgumentException("Unexpected value: " + see(dir));
			}
			nextDouble = getTimeToNextDouble();
		}
	}
	
	private int getTimeToNextDouble() {
		return (int) Math.round(getAge() + Configurations.POISON_STREAM * (2 - energy / MAX_TOXIC));
	}

	public boolean move(DIRECTION direction) {
		switch (see(direction)) {
			case WALL :
			case CLEAN : 
				return super.move(direction);
			case POISON :
			case NOT_POISON :{
				Point point = getPos().next(direction);
				Poison cell = (Poison) Configurations.world.get(point);
				if(cell.toxinDamage(type,(int) getHealth())) {
					cell.type = type;
					cell.setHealth(Math.abs(cell.getHealth()));
					cell.repaint();
				}
				destroy(); // Не важно что мы вернём - мы того
			}return true;
			case ORGANIC :
			case ENEMY :
			case FRIEND :{
				Point point = getPos().next(direction);
				CellObject cell = Configurations.world.get(point);
				if(cell.toxinDamage(type,(int) getHealth())) {
					addHealth(Math.abs(cell.getHealth())); // Вот мы и покушали свежатинкой
					cell.remove_NE();
				} else { // Покушали нами
					destroy();
				}
			}return true;
			default :
				throw new IllegalArgumentException("Unexpected value: " + see(direction));
		}
	}
	
	public boolean toxinDamage(TYPE type, int damag) {
		if (this.type == type) {
			addHealth(damag);
		} else {
			damag = (int) Math.min(damag, getHealth()*2); // Мы не можем принять больше яда, чем в нас хп
			addHealth(-damag); // Мы компенсируем другие яды
		}
		return energy <= 1;
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(color_DO);
		
		int r = (int) Math.round(getPos().getRr()*radius);
		int rx = getPos().getRx();
		int ry = getPos().getRy();
		Utils.drawCircle(g,rx,ry,r);
	}

	@Override
	public JSON toJSON(JSON make) {
		make.add("energy", energy);
		make.add("type", type.ordinal());
		return make;
	}

	@Override
	public double getHealth() {
		return Math.round(energy);
	}

	@Override
	void setHealth(double h) {
		energy = h;//Math.min(h, MAX_TOXIC);
		radius = Math.min(1, 0.3 + 0.7 * energy/MAX_TOXIC);
		nextDouble = Math.min(nextDouble,getTimeToNextDouble());
	}

	@Override
	boolean isRelative(CellObject cell0) {
		if (cell0 instanceof Poison) {
			Poison poison = (Poison) cell0;
		    return poison.type == type;
		} else {
			return false;
		}
	}

	@Override
	public void repaint() {
		switch (Legend.Graph.getMode()) {
			case HP -> color_DO = new Color((int) Math.min(255, (255.0*Math.max(0,getHealth())/MAX_TOXIC)),0,0,255);
			default -> {
				switch (type) {
					case ЖЁЛ -> color_DO = (Color.YELLOW);
					case РОЗ -> color_DO = (Color.PINK);
					case ЧЁР -> color_DO = (Color.BLACK);
					default -> color_DO = (Color.BLACK);
				}
			}
		}
	}


}
