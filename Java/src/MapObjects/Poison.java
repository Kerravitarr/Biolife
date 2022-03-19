package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import Utils.JSONmake;
import Utils.Utils;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;
import panels.Legend;

public class Poison extends CellObject {
	/**Максимальная токсичность яда*/
	static final int MAX_TOXIC = 2000;
	
	public enum TYPE {
		NONE, YELLOW, PINK, MAGENTA;
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

	public Poison(JSONmake poison) {
		super(poison);
		setHealth((long) poison.getD("energy"));
		type =TYPE.toEnum(poison.getI("type"));
		nextDouble = getTimeToNextDouble();
	}

	public Poison(TYPE type, long stepCount, Point point, long newEnergy) {
		super(stepCount, LV_STATUS.LV_POISON);
		setPos(point);
		setHealth(newEnergy);
		this.type=type;
		repaint();
		nextDouble = getTimeToNextDouble();
	}

	@Override
	void step() {
		if ((getAge()) >= nextDouble) { // Вязкость яда
			DIRECTION dir = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));
			switch (seeA(dir)) {
				case WALL: return; //Ну что мы можем сделать со стеной? О_О
				case CLEAN:{
					energy /= 2.1; // 10% выветривается каждый раз, а половину своей энергии отдаём новой калпе
					if(getHealth() < 1) destroy();//Мы растартили всю нашу ядовитость, мы того - усё
					Point point = fromVektorA(dir);
					Poison newPoison = new Poison(type,stepCount,point,getHealth());
		            Configurations.world.add(newPoison);//Сделали новую каплю
				}break;
				case ORGANIC :
				case ENEMY:
				case FRIEND:
				case POISON:
				case NOT_POISON:{
					energy /= 2.1; // 10% выветривается каждый раз, а половину своей энергии отдаём новой калпе
					if(getHealth() < 1) destroy();//Мы растартили всю нашу ядовитость, мы того - усё
					Point point = fromVektorA(dir);
					CellObject cell = Configurations.world.get(point);
					if(cell.toxinDamage(type,(int) (getHealth()))) {
						cell.remove_NE();
						Poison newPoison = new Poison(type,stepCount,point,-cell.getHealth());
			            Configurations.world.add(newPoison);//Сделали новую каплю
					} // А иначе мы не создаём просто нашу копию, нас-же переварили
				}break;
				default:
				throw new IllegalArgumentException("Unexpected value: " + seeA(dir));
			}
			nextDouble = getTimeToNextDouble();
		}
	}
	
	private int getTimeToNextDouble() {
		return (int) Math.round(getAge() + 100 - 50 * energy / MAX_TOXIC);
	}

	protected boolean moveA(DIRECTION direction) {
		switch (seeA(direction)) {
			case WALL :
			case CLEAN : 
				return super.moveA(direction);
			case POISON :
			case NOT_POISON :{
				Point point = fromVektorA(direction);
				Poison cell = (Poison) Configurations.world.get(point);
				if(cell.toxinDamage(type,(int) getHealth())) {
					cell.type = type;
					cell.setHealth(Math.abs(cell.getHealth()));
				}
				destroy(); // Не важно что мы вернём - мы того
			}return true;
			case ORGANIC :
			case ENEMY :
			case FRIEND :{
				Point point = fromVektorA(direction);
				CellObject cell = Configurations.world.get(point);
				if(cell.toxinDamage(type,(int) getHealth())) {
					setHealth(Math.abs(cell.getHealth())); // Вот мы и покушали свежатинкой
					cell.remove_NE();
				} else { // Покушали нами
					destroy();
				}
			}return true;
			default :
				throw new IllegalArgumentException("Unexpected value: " + seeA(direction));
		}
	}
	
	protected boolean toxinDamage(TYPE type, int damag) {
		if (this.type == type) {
			addHealth(damag);
		} else {
			addHealth((long) -damag); // Мы компенсируем другие яды
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
	public JSONmake toJSON(JSONmake make) {
		make.add("energy", energy);
		make.add("type", type.ordinal());
		return make;
	}

	@Override
	public long getHealth() {
		return Math.round(energy);
	}

	@Override
	void setHealth(long h) {
		energy = h;
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
					case YELLOW -> color_DO = (Color.YELLOW);
					case PINK -> color_DO = (Color.PINK);
					case MAGENTA -> color_DO = (Color.MAGENTA);
					default -> color_DO = (Color.BLACK);
				}
			}
		}
	}


}
