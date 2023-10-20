package MapObjects;

import java.awt.Color;
import java.awt.Graphics;

import Utils.JSON;
import Utils.Utils;
import Calculations.Configurations;
import Calculations.Point;
import Calculations.Point.DIRECTION;
import GUI.Legend;

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
		public static TYPE[] vals = values();
		
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
		
		@Override
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

	public Poison(JSON poison, long version) {
		super(poison);
		setHealth(Math.round((double)poison.get("energy")));
		type = TYPE.toEnum(poison.getI("type"));
		nextDouble = getTimeToNextDouble();
		stream = poison.get("stream");
	}

	public Poison(TYPE type, long stepCount, Point point, double energy, int stream) {
		super(stepCount, LV_STATUS.LV_POISON);
		setPos(point);
		setHealth(energy);
		
		this.type=type;
		this.stream = stream;
		nextDouble = getTimeToNextDouble();
	}

	@Override
	void step() {
		if(getHealth() < 1) destroy();//Мы растартили всю нашу ядовитость, мы того - усё
		if ((getAge()) >= nextDouble) { // Вязкость яда
			DIRECTION dir = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));
			energy *= 0.9;// 10% выветривается каждый раз
			var nen = energy / 2; // Половину своей энергии отдаём новой калпе
			if(createPoison(getPos().next(dir),getType(),getStepCount(), nen, getStream())) {
				energy = nen;
			}
 			nextDouble = getTimeToNextDouble();
			if(getHealth() < 1) destroy();//Мы растартили всю нашу ядовитость, мы того - усё
		}
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
		switch (test(pos)) {
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
			case ALIVE:{
				AliveCell cell = (AliveCell) Configurations.world.get(pos);
				if(cell.toxinDamage(type,(int) (energy))) {	//Умерли, надо превратить живого в мёртвого
					try {cell.bot2Organic();} catch (CellObjectRemoveException e) {}	//Создаём органику
					var organic = (Organic)Configurations.world.get(pos);
					if(type == cell.getPosionType())	//Родной яд действует слабже
						energy /= 2;
					organic.toxinDamage(type,(int) (energy - organic.getHealth())); //И отравляем её. Умерли то от яда!
				}
			}return true;	//Потравили, да и всё
			case BANE:{
				var cell = (Poison) Configurations.world.get(pos);
				if(cell.toxinDamage(type,(int) (energy))) {
					energy = Math.abs(cell.getHealth());	//Сколько тут осталось?
					cell.remove_NE();
					if(energy > 1)
			            Configurations.world.add(new Poison(type,stepCount,pos,energy, stream));//Сделали новую каплю
				} // А иначе мы не создаём просто нашу копию, нас-же переварили
			}return true;	//Потравили, да и всё
			default: throw new IllegalArgumentException("Unexpected value: " + test(pos));
		}
	}
	
	private int getTimeToNextDouble() {
		return (int) Math.round(getAge() + getStream() * (2 - energy / MAX_TOXIC));
	}

	@Override
	public boolean move(DIRECTION direction) {
		var pos = getPos().next(direction);
		switch (see(pos)) {
			case WALL :
			case CLEAN : 
				return super.move(direction);
			case OWALL:{
				if(getType() != TYPE.PINK){
					//Кроме розового яда - все остальные для стен непрохдимы
					Configurations.world.get(getPos().next(direction)).move(direction,1);
					return true;
				}
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
				destroy();	//Отдали всего себя органике
			case ALIVE :{
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
			case POISON:
			case NOT_POISON :{
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
			default: throw new IllegalArgumentException("Unexpected value: " + see(pos));
		}
	}
	
	@Override
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

	public TYPE getType() {
		return type;
	}

	public int getStream() {
		return stream;
	}

	@Override
	public void paint(Graphics g, Legend legend, int cx, int cy, int r){
		Color color_DO;
		switch (legend.getMode()) {
			case HP -> color_DO = legend.HPtToColor(getHealth()/Poison.MAX_TOXIC);
			case YEAR -> color_DO = legend.AgeToColor(getAge());
			default -> {
				switch (getType()) {
					case YELLOW -> color_DO = (Color.YELLOW);
					case PINK -> color_DO = (Color.PINK);
					case BLACK -> color_DO = (Color.BLACK);
					default -> throw new IllegalArgumentException("Unexpected value: " + getType());
				}
			}
			case POISON -> {
				var rg = (int) Utils.betwin(0, getHealth() / Poison.MAX_TOXIC, 1.0) * 255;
				switch (getType()) {
					case BLACK -> color_DO = new Color(255-rg,255- rg,255- rg);
					case PINK -> color_DO = new Color(rg, rg / 2, rg / 2);
					case YELLOW -> color_DO = new Color(rg, rg, 0);
					default -> throw new IllegalArgumentException("Unexpected value: " + getType());
				}
			}
		}
		g.setColor(color_DO);
		Utils.drawCircle(g,cx,cy,(int) Math.round(r*radius));
	}

}
