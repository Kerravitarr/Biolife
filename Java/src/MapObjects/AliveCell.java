package MapObjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import MapObjects.Poison.TYPE;
import MapObjects.dna.AddTankFood;
import MapObjects.dna.AddTankMineral;
import MapObjects.dna.Birth;
import MapObjects.dna.CommandList;
import MapObjects.dna.CreatePoison;
import MapObjects.dna.DNA;
import MapObjects.dna.SubTankFood;
import MapObjects.dna.SubTankMineral;
import Utils.JSON;
import Utils.Utils;
import main.Configurations;
import main.EvolutionTree;
import main.Point;
import main.Point.DIRECTION;
import panels.Legend;
public class AliveCell extends AliveCellProtorype{	
	
    
    /**
     * Создание клетки без рода и племени
     */
    public AliveCell(){
    	super(-1, LV_STATUS.LV_ALIVE);
    	setPos(new Point(Utils.random(0, Configurations.MAP_CELLS.width-1),Utils.random(0, Configurations.MAP_CELLS.height-1)));
    	dna = new DNA(DEF_MINDE_SIZE);
    	color_DO = new Color(255,255,255);
		evolutionNode = EvolutionTree.root;
		specialization = new Specialization();
    }

    /**
     * Загрузка клетки
     * @param cell - JSON объект, который содержит всю информацюи о клетке
     * @param tree - Дерево эволюции 
     */
    public AliveCell(JSON cell, EvolutionTree tree) {
    	super(cell);
    	dna = new DNA(cell.getJ("DNA"));
    	health = cell.get("health");
    	mineral = cell.getL("mineral");
		buoyancy = cell.getI("buoyancy");
    	direction = DIRECTION.toEnum(cell.getI("direction"));
    	DNA_wall = cell.getI("DNA_wall");
    	poisonType =  Poison.TYPE.toEnum(cell.getI("posionType"));
    	poisonPower = cell.getI("posionPower");
    	tolerance = cell.getI("tolerance");
    	foodTank = cell.get("foodTank");
    	mineralTank = cell.get("mineralTank");
    	mucosa = cell.get("mucosa");
    	
    	Generation = cell.getI("Generation");
    	specialization = new Specialization(cell.getJ("Specialization"));
    	
    	evolutionNode = tree.getNode(cell.get("GenerationTree"));
    	
    	color_DO = new Color(255,255,255);
	}

    /**
     * Копирование клетки
     * @param cell - её родитель
     */
	public AliveCell(AliveCell cell, Point newPos) {
    	super(cell.getStepCount(), LV_STATUS.LV_ALIVE);
		setPos(newPos);
	    evolutionNode = cell.evolutionNode.clone();
	    
	    setHealth(cell.getHealth() / 2);   // забирается половина здоровья у предка
	    cell.setHealth(cell.getHealth() / 2);
	    setMineral(cell.getMineral() / 2); // забирается половина минералов у предка
	    cell.setMineral(cell.getMineral() / 2);
	    DNA_wall = cell.DNA_wall /2;
	    cell.DNA_wall = cell.DNA_wall / 2; //Забирается половина защиты ДНК
	    poisonType = cell.getPosionType();
		poisonPower = cell.getPosionPower(); // Тип и степень защищённости у клеток сохраняются
		mucosa = (cell.mucosa = (int) (cell.mucosa / 2.1)); //Делится слизистой оболочкой
	
		specialization = new Specialization(cell);
	    direction = DIRECTION.toEnum(Utils.random(0, DIRECTION.size()-1));   // направление, куда повернут новорожденный, генерируется случайно
	
	    dna = new DNA(cell.dna);
	    
	    setGeneration(cell.Generation);
	    color_DO = new Color(255,255,255,evolutionNode.getAlpha());
	    repaint();
	    
	    //Мы на столько хорошо скопировали нашего родителя, что есть небольшой шанс накосячить - мутации
	    if (Utils.random(0, 100) < Configurations.AGGRESSIVE_ENVIRONMENT) 
	        mutation();
	}    
	
	@Override
	public void step() {
		//Работа ДНК
		for (int cyc = 0; (cyc < 15); cyc++)
			if(dna.get().execute(this))
				break;
		//Всплытие/погружение
		if(getBuoyancy() != 0) {
			if(getBuoyancy() < 0 && getAge() % (getBuoyancy()+101) == 0)
				moveD(DIRECTION.DOWN);
			else if(getBuoyancy() > 0 && getAge() % (101-getBuoyancy()) == 0)
				moveD(DIRECTION.UP);
		}
        //Трата энергии на ход
		if(isSleep) {
			setSleep(false);
	        addHealth(-1);          //Спать куда эффективнее
		} else {
	        addHealth(- HP_PER_STEP); //Пожили - устали
		}
		//Излишки в желудок
		if(getHealth() > MAX_HP - 100) 
			AddTankFood.addFood(this, (int) (getHealth() - (MAX_HP - 100)));
		if(getMineral() > MAX_MP - 100)
			AddTankMineral.addMineral(this, (int) (getMineral() - (MAX_MP - 100)));
		
		
		//Если жизней много - делимся
        if (this.getHealth() > MAX_HP)
        	Birth.birth(this);
        //Если есть друзья - делимся с ними едой
    	if(!getFriends().isEmpty())
			clingFriends();
        //Если есть минералы - получаем их
        if (this.getPos().getY() >= (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL))
            this.addMineral(Math.round(mineralAround()));
        //Меняем цвет, если бездельничаем
        if(getAge() % 50 == 0)
            color(ACTION.NOTHING, color_cell.r+color_cell.g+color_cell.b);
        //Если мало жизней, достаём заначку!
        if(getHealth() < 100)
        	SubTankFood.sub(this, 100);
        //Если мало минералов, достаём заначку!
        if(getMineral() < 100)
        	SubTankMineral.sub(this, 100);
		//Слизь постепенно раствоярется
        if(mucosa > 0 && getAge() % 50 == 0)
			mucosa--;
	}

	/**Поделиться с друзьями всем, что имеем*/
	private void clingFriends() {
		// Колония безвозмездно делится всем, что имеет
		double allHp = getHealth();
		long allMin = getMineral();
		int allDNA_wall = DNA_wall;
		int friendCount = getFriends().size() + 1;
		int maxToxic = poisonPower;
		Point delP = null;
		for (var cell_e : getFriends().entrySet()) {
			var cell = cell_e.getValue();
			allHp+=cell.getHealth();
			allMin+=cell.getMineral();
			allDNA_wall+=cell.DNA_wall;
			if(cell.poisonType == poisonType)
				maxToxic = Math.max(maxToxic, cell.poisonPower);
			if(!cell.aliveStatus(LV_STATUS.LV_ALIVE))
				delP = cell_e.getKey();
		}
		if(delP != null)
			getFriends().remove(delP);
		allHp /= friendCount;
		allMin /= friendCount;
		allDNA_wall /= friendCount;
		if (allMin > getMineral())
			color(ACTION.RECEIVE, allMin - getMineral());
		else
			color(ACTION.GIVE, getMineral() - allMin);
		setHealth(allHp);
		setMineral(allMin);
		DNA_wall = allDNA_wall;
		for (var friendCell : getFriends().values()) {
			if (getAge() % 100 == 0) {
				if (allHp > friendCell.getHealth())
					friendCell.color(ACTION.RECEIVE, allHp - friendCell.getHealth());
				else
					friendCell.color(ACTION.GIVE, friendCell.getHealth() - allHp);
				if (allMin > friendCell.getMineral())
					friendCell.color(ACTION.RECEIVE, allMin - friendCell.getMineral());
				else
					friendCell.color(ACTION.GIVE, friendCell.getMineral() - allMin);
			}
			friendCell.setHealth(allHp);
			friendCell.setMineral(allMin);
			friendCell.DNA_wall = allDNA_wall;
			if(friendCell.poisonType == poisonType)
				friendCell.poisonPower += friendCell.poisonPower < maxToxic ? 1 : 0;
		}
	}

    
	/**
	 * Убирает бота с карты и проводит все необходимые процедуры при этом
	 */
    @Override
    public void destroy() {
		evolutionNode.remove();
		super.destroy();
    }

	/**
	 * Перемещает бота в абсолютном направлении
	 * @param direction
	 * @return
	 */
	@Override
	public boolean move(DIRECTION direction) {
		if(getFriends().isEmpty()) {
			return super.move(direction);
		} else {
			//Многоклеточный. Тут логика куда интереснее!
			OBJECT see = super.see(direction);
			if(see.isEmptyPlase){
				//Туда двинуться можно, уже хорошо.
				Point point = getPos().next(direction);

				/**
				 * Правило!
				 * Мы можем двигаться в любую сторону,
				 * 	если от нас до любого из наших друзей будет ровно 1 клетка
				 * То есть если delX или delY > 1.
				 * 	При этом следует учесть, что delX для клеток с х = 0 и х = край экрана - будет ширина экрана - 1
				 * 	Можно поглядеть код точки. В таком случае они всё равно будут рядом по х.
				 */
		    	for (AliveCell cell : getFriends().values() ) {
		    		int delx = Math.abs(point.getX() - cell.getPos().getX());
		    		int dely = Math.abs(point.getY() - cell.getPos().getY());
		    		if(dely > 1 || (delx > 1 && delx != Configurations.MAP_CELLS.width-1))
		    			return false;
		    	}
		    	//Все условия проверены, можно выдвигаться!
				return super.move(direction);
			}
			return false;
		}
	}
	/**
	 * Создаёт одну из мутаций
	 */
	protected void mutation() {
		if(DNA_wall > 0) {	//Защита ДНК в действии!
			DNA_wall = 0;
			return;
		}
		setGeneration(getGeneration() + 1);
        switch (Utils.random(0, 9)) {
            case 0 ->{ //Мутирует специализация
            	int co = Utils.random(0, 100); //Новое значение специализации
            	int tp = Utils.random(0, Specialization.TYPE.size() - 1); //Какая специализация
            	getSpecialization().set(Specialization.TYPE.staticValues[tp],co);
                }
            case 1 -> { //Мутирует геном
                int ma = Utils.random(0, dna.size-1); //Индекс гена
                int mc = Utils.random(0, CommandList.COUNT_COMAND); //Его значение
                dna = dna.update(ma, mc);
            	}
            case 2 -> { //Дупликация - один ген удваивается
            	if(dna.size + 1 <= MAX_MINDE_SIZE) {
                	int mc = Utils.random(0, dna.size - 1); //Индекс гена, который будет дублироваться
            		dna = dna.doubling(mc);
            	}
            }
            case 3 -> { //Делеция - один ген удалился
            	int mc = Utils.random(0, dna.size - 1); //Индекс гена, который будет удалён
            	dna = dna.compression(mc);
            }
            case 4 -> { //Инверсия - два подряд идущих гена меняются местами
            	int mn = Utils.random(0, dna.size - 1); //Индекс гена, который будет обменян со следующим
            	var f = dna.get(dna.getPC(),mn);
            	var s = dna.get(dna.getPC(),mn + 1);
            	dna = dna.update(mn, s);
            	dna = dna.update(mn + 1, f);
            }
            case 5 -> { //Изометрия - отзеркаливание гена на следующий
            	int mn = Utils.random(0, dna.size - 1); //Индекс гена, c которым будем работать
            	dna = dna.update(mn + 1, CommandList.COUNT_COMAND - dna.get(dna.getPC(),mn));
            }
            case 6 -> { //Транслокация - смена местоприбывания гена
            	int iStart = Utils.random(0, dna.size - 1); //Индекс гена сейчас
            	int iStop = Utils.random(0, dna.size - 1); //Индекс гена который хочу
            	var f = dna.get(dna.getPC(),iStart);//Сам путешественник
            	if(dna.getIndex(iStart) > dna.getIndex(iStop)) {
                	dna = dna.doubling(dna.getIndex(iStop));	//Создали площадку
                	dna = dna.update(iStop, f); //Переместили сюда новый ген
            		dna = dna.compression(dna.getIndex(iStart + 1)); //Удалили его предыдущую форму
            	} else {
                	dna = dna.doubling(dna.getIndex(iStop + 1));	//Создали площадку
                	dna = dna.update(iStop + 1, f); //Переместили сюда новый ген
                	dna = dna.compression(dna.getIndex(iStart)); //Удалили его предыдущую форму
            	}
            }
            case 7 -> { // Смена типа яда на который мы отзываемся
            	poisonType = TYPE.toEnum(Utils.random(0, TYPE.size()));
            	if(poisonType != TYPE.UNEQUIPPED)
            		poisonPower = Utils.random(1, (int) (CreatePoison.HP_FOR_POISON * 2 / 3));
            	else
            		poisonPower = 0; //К этому у нас защищённости ни какой
            }
            case 8 -> { //Мутирует вектор прерываний
                int ma = Utils.random(0, dna.interrupts.length-1); //Индекс в векторе
                int mc = Utils.random(0, dna.size-1); //Его значение
                dna.interrupts[ma] = mc;
            }
            case 9 -> { //Мутирует наша невосприимчивость
            	tolerance = Utils.random(0, dna.size - 1);
            }
        }
		evolutionNode = evolutionNode.newNode(this,getStepCount());
	}

	/**
	 * Превращает бота в органику
	 */
	public void bot2Organic() {
		try {
			destroy(); // Удаляем себя
		} catch (CellObjectRemoveException e) {
	    	Configurations.world.add(new Organic(this)); //Мы просто заменяем себя
	    	throw e;
		}
	}
	/**
	 * Превращает бота в стену
	 */
	public void bot2Wall() {
		try {
			destroy(); // Удаляем себя
		} catch (CellObjectRemoveException e) {
	    	Configurations.world.add(new Fossil(this)); //Мы просто заменяем себя
	    	throw e;
		}
	}

	/**
	 * Подглядывает за бота в абсолютном направлении
	 * @param direction направление, DIRECTION
	 * @returns параметры OBJECT
	 */
	@Override
	public OBJECT see(DIRECTION direction) {
		OBJECT see = super.see(direction);
		if(see == OBJECT.POISON){
			Point point = getPos().next(direction);
			Poison cell = (Poison) Configurations.world.get(point);
			if(cell.getType() == getPosionType())
				return OBJECT.NOT_POISON;
			else
				return OBJECT.POISON;
		} else {
			return see;
		}
	}

	/**
	 * Родственные-ли боты?
	 * Определеяет родственников по фенотипу, по тому как они выглядят.
	 * @param cell
	 * @param cell2
	 * @return
	 */
	/*protected boolean isRelative(CellObject cell0) {
		if (cell0 instanceof AliveCell) {
			AliveCell bot0 = (AliveCell) cell0;
		    int dif = 0;    // счетчик несовпадений в фенотипе
		    dif += Math.abs(bot0.phenotype.getRed() - this.phenotype.getRed());
		    dif += Math.abs(bot0.phenotype.getGreen() - this.phenotype.getGreen());
		    dif += Math.abs(bot0.phenotype.getBlue() - this.phenotype.getBlue());
		    return dif < 10;
		} else {
			return false;
		}
	}*/
	/**
	 * Родственные-ли боты?
	 * Определеяет родственников по генотипу, по тому различаются их ДНК на 2 и более признака
	 * @param cell0 - клетка, с коротой сравнивается
	 * @return
	 */
	@Override
	protected boolean isRelative(CellObject cell0) {
		if (cell0 instanceof AliveCell bot0) {
			return bot0.dna.equals(this.dna, this.tolerance);
		} else {
			return false;
		}
	}

	/**
	 * Если яд слишком сильный - бот просто не трогает своё здоровье
	 */
	@Override
	public boolean toxinDamage(TYPE type,int damag) {
		if(type == getPosionType() && getPosionPower() >= damag) {
			poisonPower = getPosionPower() + 1;
			return false;
		} else {
			switch (type) {
				case PINK-> {return false;}
				case YELLOW->{
					if(type == getPosionType())	//Родной яд действует слабже
						damag /= 2;
					var isLife = damag < getHealth();
					if (isLife) {
						addHealth(-damag);
					}
					return !isLife;
				}
				case BLACK->{
					mutation();
					return false;
				}
				case UNEQUIPPED -> throw new UnsupportedOperationException("Unimplemented case: " + type);
			}
		}
		return true;
	}
	
	/**
	 * Раскрашивает бота в зависимости от действия
	 * @param act
	 * @param num
	 */
	public void color(ACTION act, double num) {
		if(Legend.Graph.getMode() != Legend.Graph.MODE.DOING)
			return;
		if(act.p != 1)
			num *= act.p;
		color_cell.addR(color_cell.r>act.r?-num:num);color_cell.addG(color_cell.g>act.g?-num:num);color_cell.addB(color_cell.b>act.b?-num:num);
		color_DO = color_cell.getC();
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(color_DO);
		
		int r = getPos().getRr();
		int rx = getPos().getRx();
		int ry = getPos().getRy();
		if(getFriends().isEmpty())
			Utils.fillCircle(g,rx,ry,r);
		else if(r < 5) 
			Utils.fillSquare(g,rx,ry,r);
		else {
			Utils.fillCircle(g,rx,ry,r);
			synchronized (getFriends()) {
				try {
					//Приходится рисовать в два этапа, иначе получается ужас страшный.
					//Этап первый - основные связи
					
				Graphics2D g2 = (Graphics2D) g;
			    Stroke oldStr = g2.getStroke();
				g.setColor(color_DO);
				g2.setStroke(new BasicStroke(r/2));
				for(AliveCell i : getFriends().values()) {
					int rxc = i.getPos().getRx();
					if(getPos().getX() == 0 && i.getPos().getX() == Configurations.MAP_CELLS.width -1)
						rxc = rx - r;
					else if(i.getPos().getX() == 0 && getPos().getX() == Configurations.MAP_CELLS.width -1)
							rxc = rx + r;
					int ryc = i.getPos().getRy();
					
					int delx = rxc - rx;
					int dely = ryc - ry;
					g.drawLine(rx,ry, rx+delx/3,ry+dely/3);
				}
				g2.setStroke(oldStr);
				g.setColor(Color.BLACK);
				//Этап второй, всё тоже самое, но теперь лишь тонкие линии
				for(AliveCell i : getFriends().values()) {
					int rxc = i.getPos().getRx();
					if(getPos().getX() == 0 && i.getPos().getX() == Configurations.MAP_CELLS.width -1)
						rxc = rx - r;
					else if(i.getPos().getX() == 0 && getPos().getX() == Configurations.MAP_CELLS.width -1)
							rxc = rx + r;
					int ryc = i.getPos().getRy();
					//А теперь рисуем тонкую линию, чтобы видно было как они выглядят
					g.drawLine(rx,ry, rxc,ryc);
				}
				}catch (java.util.ConcurrentModificationException e) {/* Выскакивает, если кто-то из наших друзей погиб*/	}
			}
		}
		if(r > 10) {
			g.setColor(Color.PINK);
			g.drawLine(rx,ry, rx + direction.addX*r/2,ry + direction.addY*r/2);
		}
			
	}
	
	@Override
	public void setHealth(double health) {
		this.health=health;
		if(this.health < 0)
			bot2Organic();
	}

	public void setFriend(AliveCell friend) {
		if (friend == null || getFriends().get(friend.getPos()) != null)
			return;
		getFriends().put(friend.getPos(), friend);
		friend.getFriends().put(getPos(), this);
	}

	@Override
	public JSON toJSON(JSON make) {
		make.add("DNA",dna.toJSON());
		make.add("health",health);
		make.add("mineral",mineral);
		make.add("buoyancy",buoyancy);
		make.add("direction",DIRECTION.toNum(direction));
		make.add("DNA_wall",DNA_wall);
		make.add("posionType",getPosionType().ordinal());
		make.add("posionPower",getPosionPower());
		make.add("tolerance",tolerance);
		make.add("foodTank",getFoodTank());
		make.add("mineralTank",mineralTank);
		make.add("mucosa", getMucosa());

	    //=================ПАРАМЕТРЫ БОТА============
		make.add("Generation",Generation);
		make.add("GenerationTree",evolutionNode.getBranch());
		

	    //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
		make.add("phenotype",Integer.toHexString(phenotype.getRGB()));
		
		//===============МНОГОКЛЕТОЧНОСТЬ===================
		JSON[] fr = new JSON[getFriends().size()];
		Object[] points = getFriends().keySet().toArray();
		for (int i = 0; i < fr.length; i++)
			fr[i] = ((Point)points[i]).toJSON();
		make.add("friends",fr);
		
		//===============СПЕЦИАЛИЗАЦИЯ===================
		make.add("Specialization",getSpecialization().toJSON());
		
		return make;
	}

}
