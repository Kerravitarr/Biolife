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
	public static final int MAX_TOXIC = 2000;
	/**Максимальная вязкость яда.*/
	public static final int MAX_STREAM = 20_000;
	
	/**Все возможные типы яда*/
	public enum TYPE {
		/**Без яда, используется для клеток*/
		UNEQUIPPED(),
		/**Химический активный, наносит урон*/
		YELLOW(), 
		/**Безвредный для клеток, но может разрушать стены*/
		PINK(), 
		/**Безвредный для здоровья, но вызывает мутацию в клетке*/
		BLACK();
		private static TYPE[] vals = values();
		
		private String name;

		TYPE() {name = Configurations.getProperty(getClass(), super.name());}

		public static TYPE toEnum(int num) {
			while (num >= vals.length)
				num -= vals.length;
			while (num < 0)
				num += vals.length;
			return vals[num];
		}

		public static int size() {
			return vals.length;
		}
		
		public String toString() {return name;}
	};
	/**Сколько у нас энергии*/
	private double energy = 0;
	/**Каков тип яда*/
	private TYPE type;
	/**Каков наш радиус в частях одной клетки!!!!*/
	public double radius = 1;
	/**Когда следующее деление*/
	public int nextDouble;
	/**Вязкость яда*/
	private int stream = 150;

	public Poison(JSON poison) {
		super(poison);
		setHealth(Math.round((double)poison.get("energy")));
		type = TYPE.toEnum(poison.getI("type"));
		nextDouble = getTimeToNextDouble();
		stream = poison.get("stream");
		repaint();
	}

	public Poison(TYPE type, long stepCount, Point point, double energy, int stream) {
		super(stepCount, LV_STATUS.LV_POISON);
		setPos(point);
		setHealth(energy);
		
		this.type=type;
		this.stream = stream;
		nextDouble = getTimeToNextDouble();
		repaint();
	}

	@Override
	void step() {
		if ((getAge()) >= nextDouble) { // Вязкость яда
			DIRECTION dir = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));
			var nen = energy /= 2.1; // 10% выветривается каждый раз, а половину своей энергии отдаём новой калпе
			if(createPoison(getPos().next(dir),getType(),getStepCount(), nen, getStream())) {
				energy = nen;
			}
 			nextDouble = getTimeToNextDouble();
			repaint();
		}
		if(getHealth() < 1) destroy();//Мы растартили всю нашу ядовитость, мы того - усё
	}
	/**
	 * Создаёт каплю яда в определённой позиции
	 * @param pos где создать
	 * @param type какого типа капля
	 * @param stepCount шаг моделирования. Нужно для того, чтобы дети не ходили в ход родителя
	 * @param energy сколько энергии у капли
	 * @param stream тягучесть капли
	 * @return true, если яд был истрачен и false, если по каким-то причинам яд не потратился
	 */
	public static boolean createPoison(Point pos, TYPE type, long stepCount, double energy, int stream) {
		switch (Configurations.world.test(pos)) {
			case WALL: return false; //Ну что мы можем сделать со стеной? О_О
			case CLEAN:
	            Configurations.world.add(new Poison(type,stepCount,pos,energy, stream));//Сделали новую каплю
			return true;
			case OWALL:{
				if(type != TYPE.PINK)
					return false;	//Кроме розового яда - все остальные для стен непрохдимы
				var cell = (Fossil) Configurations.world.get(pos);
				if(cell.toxinDamage(type,(int) (energy))) {
					energy = Math.abs(cell.getHealth());	//Сколько тут осталось?
					cell.remove_NE();
					if(energy > 1)
			            Configurations.world.add(new Poison(type,stepCount,pos,energy, stream));//Сделали новую каплю
				} // А иначе мы не создаём просто нашу копию, нас-же переварили
			}return true;
			case ORGANIC:
				Configurations.world.get(pos).toxinDamage(type,(int) (energy));
			return true;	//Органику потравили, да и всё
			case BOT:{
				AliveCell cell = (AliveCell) Configurations.world.get(pos);
				if(cell.toxinDamage(type,(int) (energy))) {	//Умерли, надо превратить живого в мёртвого
					try {cell.bot2Organic();} catch (CellObjectRemoveException e) {}	//Создаём органику
					var organic = (Organic)Configurations.world.get(pos);
					if(type == cell.getPosionType())	//Родной яд действует слабже
						energy /= 2;
					organic.toxinDamage(type,(int) (energy - organic.getHealth())); //И отравляем её. Умерли то от яда!
				}
			}return true;	//Потравили, да и всё
			case POISON:{
				var cell = (Poison) Configurations.world.get(pos);
				if(cell.toxinDamage(type,(int) (energy))) {
					energy = Math.abs(cell.getHealth());	//Сколько тут осталось?
					cell.remove_NE();
					if(energy > 1)
			            Configurations.world.add(new Poison(type,stepCount,pos,energy, stream));//Сделали новую каплю
				} // А иначе мы не создаём просто нашу копию, нас-же переварили
			}return true;	//Потравили, да и всё
			case ENEMY:
			case NOT_POISON:
			case FRIEND: throw new IllegalArgumentException("Unexpected value: " + Configurations.world.test(pos));
		}
		//Это вместо default. Зато позволяет не писать злополучное слово и лучше видеть подскзаки по коду!
		throw new IllegalArgumentException("Unexpected value: " + Configurations.world.test(pos));
	}
	
	private int getTimeToNextDouble() {
		return (int) Math.round(getAge() + getStream() * (2 - energy / MAX_TOXIC));
	}

	public boolean move(DIRECTION direction) {
		var pos = getPos().next(direction);
		switch (Configurations.world.test(pos)) {
			case WALL :
			case CLEAN : 
				return super.move(direction);
			case OWALL:{
				if(getType() != TYPE.PINK)
					return false;	//Кроме розового яда - все остальные для стен непрохдимы
				var cell = (Fossil) Configurations.world.get(pos);
				if(cell.toxinDamage(getType(),(int) (getHealth()))) {
					energy = Math.abs(cell.getHealth());	//Сколько теперь у нас энергии
					cell.remove_NE();
					if(energy > 1)
						return super.move(direction); //А теперь двигаемся на освободившуюся клетку
					else
						destroy();	//Упс, мы не смогли :/
				} else {
					destroy();	//Упс, мы не смогли :/
				}
			}case ORGANIC :
				Configurations.world.get(pos).toxinDamage(getType(),(int) (getHealth()));
			return false;
			case BOT :{
				AliveCell cell = (AliveCell) Configurations.world.get(pos);
				if(cell.toxinDamage(getType(),(int) getHealth())) {
					try {cell.bot2Organic();} catch (CellObjectRemoveException e) {}	//Создаём органику
					var organic = (Organic)Configurations.world.get(pos);
					var energy = getHealth();
					if(getType() == cell.getPosionType())	//Родной яд действует слабже
						energy /= 2;
					organic.toxinDamage(getType(),(int) (energy - organic.getHealth())); //И отравляем её. Умерли то от яда!
					destroy();	//А мы что? Мы всё, теперь там ядовитая плоть
				} else { // Покушали нами
					destroy();
				}
			}
			case POISON :{
				Poison cell = (Poison) Configurations.world.get(pos);
				if(cell.toxinDamage(getType(),(int) getHealth())) {
					energy = Math.abs(cell.getHealth());	//Сколько тут осталось?
					cell.remove_NE();
					if(energy > 1)
						return super.move(direction); //А теперь двигаемся на освободившуюся клетку
					else
						destroy();	//Упс, мы не смогли :/
				} else {
					destroy();	//Упс, мы не смогли :/
				}
			}return true;
			case ENEMY:
			case NOT_POISON:
			case FRIEND: throw new IllegalArgumentException("Unexpected value: " + Configurations.world.test(pos));
		}
		throw new IllegalArgumentException("Unexpected value: " + Configurations.world.test(getPos().next(direction)));
	}
	
	public boolean toxinDamage(TYPE type, int damag) {
		if (this.getType() == type) {
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
		make.add("stream", getStream());
		make.add("type", getType().ordinal());
		return make;
	}

	@Override
	public double getHealth() {
		return Math.round(energy);
	}

	@Override
	void setHealth(double h) {
		energy = h;
		radius = Math.min(1, 0.3 + 0.7 * energy/MAX_TOXIC);
		nextDouble = Math.min(nextDouble,getTimeToNextDouble());
	}

	@Override
	boolean isRelative(CellObject cell0) {
		if (cell0 instanceof Poison poison)
			return poison.getType() == getType();
		else
			return false;
	}

	@Override
	public void repaint() {
		switch (Legend.Graph.getMode()) {
			case HP -> color_DO = new Color((int) Math.min(255, (255.0*Math.max(0,getHealth())/MAX_TOXIC)),0,0,255);
			default -> {
				switch (getType()) {
					case YELLOW -> color_DO = (Color.YELLOW);
					case PINK -> color_DO = (Color.PINK);
					case BLACK -> color_DO = (Color.BLACK);
					case UNEQUIPPED -> throw new IllegalArgumentException("Unexpected value: " + getType());
				}
			}
			case POISON -> {
				var rg = (int) Utils.betwin(0, getHealth() / MAX_TOXIC, 1.0) * 255;
				switch (getType()) {
					case BLACK -> color_DO = new Color(255-rg,255- rg,255- rg);
					case PINK -> color_DO = new Color(rg, rg / 2, rg / 2);
					case YELLOW -> color_DO = new Color(rg, rg, 0);
					case UNEQUIPPED -> throw new IllegalArgumentException("Unexpected value: " + getType());
				}
			}
		}
	}

	public TYPE getType() {
		return type;
	}

	public int getStream() {
		return stream;
	}


}
