package MapObjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import MapObjects.Poison.TYPE;
import MapObjects.dna.Birth;
import MapObjects.dna.CommandList;
import MapObjects.dna.CreatePoison;
import MapObjects.dna.DNA;
import MapObjects.dna.TankFood;
import MapObjects.dna.TankMineral;
import Utils.JSON;
import Utils.Utils;
import java.util.Iterator;
import main.Configurations;
import main.EvolutionTree;
import main.Point;
import main.Point.DIRECTION;
import panels.Legend;

public class AliveCell extends AliveCellProtorype {

    /**
     * Создание клетки без рода и племени
     */
    public AliveCell() {
        super(-1, LV_STATUS.LV_ALIVE);
        setPos(new Point(Utils.random(0, Configurations.MAP_CELLS.width - 1), Utils.random(0, Configurations.MAP_CELLS.height - 1)));
        dna = new DNA(DEF_MINDE_SIZE);
        color_DO = new Color(255, 255, 255);
        evolutionNode = EvolutionTree.root;
        specialization = new Specialization();
    }

    /**
     * Загрузка клетки
     *
     * @param cell - JSON объект, который содержит всю информацюи о клетке
     * @param tree - Дерево эволюции
     */
    public AliveCell(JSON cell, EvolutionTree tree, long version) {
        super(cell);
        dna = new DNA(cell.getJ("DNA"));
        health = cell.get("health");
        mineral = cell.getL("mineral");
        buoyancy = cell.getI("buoyancy");
        direction = DIRECTION.toEnum(cell.getI("direction"));
        DNA_wall = cell.getI("DNA_wall");
        poisonType = Poison.TYPE.toEnum(cell.getI("posionType"));
        setPosionPower(cell.getI("posionPower"));
        tolerance = cell.getI("tolerance");
        foodTank = cell.get("foodTank");
        mineralTank = cell.get("mineralTank");
        mucosa = cell.get("mucosa");
		if(version < 6)
			hp_by_div = cell.get("hp_by_div");

        Generation = cell.getI("Generation");
        specialization = new Specialization(cell.getJ("Specialization"));
		if(tree != null) 
			evolutionNode = tree.getNode(this, cell.get("GenerationTree"));

        color_DO = new Color(255, 255, 255);
    }

    /**
     * Создание полной копии клетки. Без мутаций!
     * @param cell - на основе кого создаём клетку
     */
	protected AliveCell(AliveCell cell){
        super(cell.getStepCount(), LV_STATUS.LV_ALIVE);
		setPos(cell.getPos());
        evolutionNode = cell.evolutionNode;
        setHealth(cell.getHealth());
        setMineral(cell.getMineral());
        DNA_wall = cell.DNA_wall;
        poisonType = cell.getPosionType();
        poisonPower = cell.getPosionPower(); // Тип и степень защищённости у клеток сохраняются
        mucosa = cell.mucosa;
		setFoodTank(cell.getFoodTank());//Поделимся жирком и минералами
		setMineralTank(cell.getMineralTank());
		hp_by_div = cell.hp_by_div;					//ХП для деления остаётся тем-же

        specialization = new Specialization(cell);
        direction = cell.direction;   // направление, куда повернут новорожденный, генерируется случайно
        dna = new DNA(cell.getDna());
        setGeneration(cell.Generation);
	}
    /**
     * Деление клетки
     * @param cell - её родитель
     * @param newPos - где она окажется
     */
    public AliveCell(AliveCell cell, Point newPos) {
        super(cell.getStepCount(), LV_STATUS.LV_ALIVE);
        setPos(newPos);
        evolutionNode = cell.evolutionNode.clone();

        setHealth(cell.getHealth() / 2);   // забирается половина здоровья у предка
        cell.setHealth(cell.getHealth() / 2);
        setMineral(cell.getMineral() / 2); // забирается половина минералов у предка
        cell.setMineral(cell.getMineral() / 2);
        DNA_wall = cell.DNA_wall / 2;
        cell.DNA_wall = cell.DNA_wall / 2; //Забирается половина защиты ДНК
        poisonType = cell.getPosionType();
        poisonPower = cell.getPosionPower(); // Тип и степень защищённости у клеток сохраняются
        mucosa = (cell.mucosa = (int) (cell.mucosa / 2.1)); //Делится слизистой оболочкой
		setFoodTank(cell.getFoodTank() / 2);//Поделимся жирком и минералами 
		cell.setFoodTank(cell.getFoodTank() / 2);
		setMineralTank(cell.getMineralTank()/ 2);
		cell.setMineralTank(cell.getMineralTank() / 2);
		hp_by_div = cell.hp_by_div;					//ХП для деления остаётся тем-же

        specialization = new Specialization(cell);
        direction = DIRECTION.toEnum(Utils.random(0, DIRECTION.size() - 1));   // направление, куда повернут новорожденный, генерируется случайно

        dna = new DNA(cell.getDna());

        setGeneration(cell.Generation);
        repaint();

        //Мы на столько хорошо скопировали нашего родителя, что есть небольшой шанс накосячить - мутации
        if (Utils.random(0, 100) < Configurations.AGGRESSIVE_ENVIRONMENT) {
            mutation();
        }
    }
	/**
     * Создание вирусной клетки
     * @param cell - её родитель
     * @param newPos - где она окажется
     * @param HP - сколько у неё будет здоровья
     * @param ndna - какая у неё теперь ДНК
     */
    public AliveCell(AliveCell cell, Point newPos, double HP, DNA ndna) {
        super(cell.getStepCount(), LV_STATUS.LV_ALIVE);
        setPos(newPos);

        setHealth(HP);
        cell.addHealth(-HP);
        setMineral(cell.getMineral() / 2); // забирается половина минералов у предка
        cell.setMineral(cell.getMineral() / 2);
        DNA_wall = cell.DNA_wall / 2;
        cell.DNA_wall = cell.DNA_wall / 2; //Забирается половина защиты ДНК
        poisonType = cell.getPosionType();
        poisonPower = cell.getPosionPower(); // Тип и степень защищённости у клеток сохраняются
        mucosa = (cell.mucosa = (int) (cell.mucosa / 2.1)); //Делится слизистой оболочкой
		hp_by_div = cell.hp_by_div;					//ХП для деления остаётся тем-же

        specialization = new Specialization(cell);
        direction = DIRECTION.toEnum(Utils.random(0, DIRECTION.size() - 1));   // направление, куда повернут новорожденный, генерируется случайно

        dna = ndna;

        setGeneration(0); //Вирусные клетки имеют нулевое поколение
        evolutionNode = cell.evolutionNode.clone();
        evolutionNode = evolutionNode.newNode(this, getStepCount());
        repaint();

        //Мы на столько хорошо скопировали нашего родителя, что есть небольшой шанс накосячить - мутации
        if (Utils.random(0, 100) < Configurations.AGGRESSIVE_ENVIRONMENT) {
            mutation();
        }
    }

    @Override
    public void step() {
        //Работа ДНК
        for (int cyc = 0; (cyc < 15); cyc++) {
			var cmd = getDna().get();
            if (cmd.execute(this)) {
                break;
            }
        }
        //Всплытие/погружение
        if (getBuoyancy() != 0) {
            if (getBuoyancy() < 0 && getAge() % (getBuoyancy() + 101) == 0) {
                moveD(DIRECTION.DOWN);
            } else if (getBuoyancy() > 0 && getAge() % (101 - getBuoyancy()) == 0) {
                moveD(DIRECTION.UP);
            }
        }
        //Трата энергии на ход
        if (isSleep) {
            setSleep(false);
            addHealth(-HP_PER_STEP/10d);          //Спать куда эффективнее
        } else {
            addHealth(-HP_PER_STEP); //Пожили - устали
        }
        //Излишки в желудок
        if (getHealth() > hp_by_div - 100) {
            TankFood.add(this, (int) (getHealth() - (hp_by_div - 100)));
        }
        if (getMineral() > MAX_MP - 100) {
            TankMineral.add(this, (int) (getMineral() - (MAX_MP - 100)));
        }

        //Если жизней много - делимся
        if (this.getHealth() > hp_by_div) {
            Birth.birth(this);
        }
        //Если есть друзья - делимся с ними едой
        if (!getFriends().isEmpty()) {
            clingFriends();
        }
        //Если есть минералы - получаем их
        if (this.getPos().getY() >= (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL)) {
            this.addMineral(Math.round(mineralAround()));
        }
        //Меняем цвет, если бездельничаем
        if (getAge() % 50 == 0) {
            color(ACTION.NOTHING, color_cell.r + color_cell.g + color_cell.b);
        }
        //Если мало жизней, достаём заначку!
        if (getHealth() < 100) {
            TankFood.sub(this, 100);
        }
        //Если мало минералов, достаём заначку!
        if (getMineral() < 100) {
            TankMineral.sub(this, 100);
        }
        //Слизь постепенно раствоярется
        if (mucosa > 0 && getAge() % 50 == 0) {
            mucosa--;
        }
    }

    /**
     * Поделиться с друзьями всем, что имеем
     */
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
            allHp += cell.getHealth();
            allMin += cell.getMineral();
            allDNA_wall += cell.DNA_wall;
            if (cell.poisonType == poisonType) {
                maxToxic = Math.max(maxToxic, cell.poisonPower);
            }
            if (!cell.aliveStatus(LV_STATUS.LV_ALIVE)) {
                delP = cell_e.getKey();
            }
        }
        if (delP != null) {
            getFriends().remove(delP);
        }
        allHp /= friendCount;
        allMin /= friendCount;
        allDNA_wall /= friendCount;
        if (allMin > getMineral()) {
            color(ACTION.RECEIVE, allMin - getMineral());
        } else {
            color(ACTION.GIVE, getMineral() - allMin);
        }
        setHealth(allHp);
        setMineral(allMin);
        DNA_wall = allDNA_wall;
        for (var friendCell : getFriends().values()) {
            if (getAge() % 100 == 0) {
                if (allHp > friendCell.getHealth()) {
                    friendCell.color(ACTION.RECEIVE, allHp - friendCell.getHealth());
                } else {
                    friendCell.color(ACTION.GIVE, friendCell.getHealth() - allHp);
                }
                if (allMin > friendCell.getMineral()) {
                    friendCell.color(ACTION.RECEIVE, allMin - friendCell.getMineral());
                } else {
                    friendCell.color(ACTION.GIVE, friendCell.getMineral() - allMin);
                }
            }
            friendCell.setHealth(allHp);
            friendCell.setMineral(allMin);
            friendCell.DNA_wall = allDNA_wall;
            if (friendCell.poisonType == poisonType) {
                friendCell.poisonPower += friendCell.poisonPower < maxToxic ? 1 : 0;
            }
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
     *
     * @param direction
     * @return
     */
    @Override
    public boolean move(DIRECTION direction) {
        if (getFriends().isEmpty()) {
            return super.move(direction);
        } else {
            //Многоклеточный. Тут логика куда интереснее!
            OBJECT see = super.see(direction);
            if (see.isEmptyPlase) {
                //Туда двинуться можно, уже хорошо.
                Point point = getPos().next(direction);

                /**
                 * Правило! Мы можем двигаться в любую сторону, если от нас до
                 * любого из наших друзей будет ровно 1 клетка То есть если delX
                 * или delY > 1. При этом следует учесть, что delX для клеток с
                 * х = 0 и х = край экрана - будет ширина экрана - 1 Можно
                 * поглядеть код точки. В таком случае они всё равно будут рядом
                 * по х.
                 */
                for (AliveCell cell : getFriends().values()) {
                    int delx = Math.abs(point.getX() - cell.getPos().getX());
                    int dely = Math.abs(point.getY() - cell.getPos().getY());
                    if (dely > 1 || (delx > 1 && delx != Configurations.MAP_CELLS.width - 1)) {
                        return false;
                    }
                }
                //Все условия проверены, можно выдвигаться!
                return super.move(direction);
            }
            return false;
        }
    }

    /**Заставляет геном клетки или её параметры мутировать.
	 * Если у клетки защищена ДНК, то она не мутируте, а защита её ДНК сбрасывается в 0
     */
    protected void mutation() {
        if (DNA_wall > 0) {	//Защита ДНК в действии!
            DNA_wall = 0;
            return;
        }
        switch (Utils.random(0, 11)) {
            case 0 -> { //Мутирует специализация
                int co = Utils.random(0, 100); //Новое значение специализации
                int tp = Utils.random(0, Specialization.TYPE.size() - 1); //Какая специализация
                getSpecialization().set(Specialization.TYPE.staticValues[tp], co);
            }
            case 1 -> { //Мутирует геном
                int ma = Utils.random(0, getDna().size - 1); //Индекс гена
                int mc = Utils.random(0, CommandList.COUNT_COMAND); //Его значение
                setDna(getDna().update(ma, true,mc));
            }
            case 2 -> { //Дупликация - один ген удваивается
                if (getDna().size + 1 <= MAX_MINDE_SIZE) {
                    final int mc = Utils.random(0, getDna().size - 1); //Индекс гена, который будет дублироваться
					final var gen = getDna().get(mc);
					if(gen.size() <= getDna().size) //Чтобы не не умножать для вирусов
						setDna(getDna().doubling(mc,false,gen.size()));
                }
            }
            case 3 -> { //Делеция - один ген удалился
				if(getDna().size == 1) return ;	//Да, у виросов бывает, что и не бывает
				int mc = Utils.random(0, getDna().size - 1); //Индекс гена, который будет удалён
				final var gen = getDna().get(mc);
				if(gen.size() < getDna().size) //Чтобы последний не удалить ненароком
					setDna(getDna().compression(mc,false,gen.size()));
            }
            case 4 -> { //Инверсия - два подряд идущих гена меняются местами
                int mn = Utils.random(0, getDna().size - 1); //Индекс гена, который будет обменян со следующим
				final var f = getDna().get(mn);
				final var lf = f.size();
				final var s = getDna().get(mn + lf);
				final var ls = s.size();
				if(lf + ls <= getDna().size) { //У микроскопических ДНК не может быть такого
					final var ndna = new int[lf + ls];
					for(var i = 0 ; i < ls; i++)
						ndna[i] = getDna().get(mn + lf + i, false);
					for(var i = 0 ; i < lf; i++)
						ndna[i + ls] = getDna().get(mn + i, false);
					setDna(getDna().update(mn, false,ndna));
				}
            }
            case 5 -> { //Изометрия - отзеркаливание гена на обратный
                int mn = Utils.random(0, getDna().size - 1); //Индекс гена, c которым будем работать
                setDna(getDna().update(mn + 1,false, CommandList.COUNT_COMAND - getDna().get(mn, false)));
            }
            case 6 -> { //Транслокация - смена местоприбывания гена
                int iStart = Utils.random(0, getDna().size - 1); //Индекс гена сейчас
                int iStop = Utils.random(0, getDna().size - 1); //Индекс гена который хочу
				final var f = getDna().get(iStart);
				final var lf = f.size();
				if(lf < getDna().size) { //У микроскопических ДНК не может быть такого
					final var comad = getDna().subDNA(iStart, false, lf);
					//А теперь вырезаем ген, где он был и вставляем его в новое место
					setDna(getDna().compression(iStart, false,lf).insert(iStop, false, comad));
				}
            }
            case 7 -> { // Смена типа яда на который мы отзываемся
                poisonType = TYPE.toEnum(Utils.random(0, TYPE.size()));
                if (poisonType != TYPE.UNEQUIPPED) {
                    poisonPower = Utils.random(1, (int) (CreatePoison.HP_FOR_POISON * 2 / 3));
                } else {
                    poisonPower = 0; //К этому у нас защищённости ни какой
                }
            }
            case 8 -> { //Мутирует вектор прерываний
                int ma = Utils.random(0, getDna().interrupts.length - 1); //Индекс в векторе
                int mc = Utils.random(0, getDna().size - 1); //Его значение
                dna.interrupts[ma] = mc;
            }
            case 9 -> { //Мутирует наша невосприимчивость к ДНК других клеток
                tolerance = Utils.random(0, getDna().size - 1);
            }
			case 10 -> { //Мутирует скорость размножения, сколько нужно ХП для поделишек
				hp_by_div = Math.min(MAX_HP, hp_by_div * Utils.random(90, 110) / 100);
			}
			case 11 -> { //Мутирует программный счётчик ДНК
				dna.next(Utils.random(1, dna.size));
			}
        }
        setGeneration(getGeneration() + 1);
        evolutionNode = evolutionNode.newNode(this, getStepCount());
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
     *
     * @param direction направление, DIRECTION
     * @return параметры OBJECT
     */
    @Override
    public OBJECT see(DIRECTION direction) {
        OBJECT see = super.see(direction);
        if (see == OBJECT.POISON) {
            Point point = getPos().next(direction);
            Poison cell = (Poison) Configurations.world.get(point);
            if (cell.getType() == getPosionType()) {
                return OBJECT.NOT_POISON;
            } else {
                return OBJECT.POISON;
            }
        } else {
            return see;
        }
    }

    /**
     * Родственные-ли боты? Определеяет родственников по фенотипу, по тому как
     * они выглядят.
     *
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
     * Родственные-ли боты? Определеяет родственников по генотипу, по тому
     * различаются их ДНК на 2 и более признака
     *
     * @param cell0 - клетка, с коротой сравнивается
     * @return
     */
    @Override
    protected boolean isRelative(CellObject cell0) {
        if (cell0 instanceof AliveCell bot0) {
           return bot0.getDna().equals(this.getDna(), this.tolerance);
        } else {
            return false;
        }
    }

    /**
     * Если яд слишком сильный - бот просто не трогает своё здоровье
     */
    @Override
    public boolean toxinDamage(TYPE type, int damag) {
        if (type == getPosionType() && getPosionPower() >= damag) {
            setPosionPower(getPosionPower() + 1);
            return false;
        } else {
            switch (type) {
                case PINK -> {
                    return false;
                }
                case YELLOW -> {
                    if (type == getPosionType()) //Родной яд действует слабже
                    {
                        damag /= 2;
                    }
                    var isLife = damag < getHealth();
                    if (isLife) {
                        addHealth(-damag);
                    }
                    return !isLife;
                }
                case BLACK -> {
                    mutation();
                    return false;
                }
                case UNEQUIPPED ->
                    throw new UnsupportedOperationException("Unimplemented case: " + type);
            }
        }
        return true;
    }

    /**
     * Раскрашивает бота в зависимости от действия
     *
     * @param act
     * @param num
     */
    public void color(ACTION act, double num) {
        if (Legend.Graph.getMode() != Legend.Graph.MODE.DOING) {
            return;
        }
        if (act.p != 1) {
            num *= act.p;
        }
        color_cell.addR(color_cell.r > act.r ? -Math.min(num, color_cell.r - act.r) : Math.min(num, act.r - color_cell.r));
        color_cell.addG(color_cell.g > act.g ? -Math.min(num, color_cell.g - act.g) : Math.min(num, act.g - color_cell.g));
        color_cell.addB(color_cell.b > act.b ? -Math.min(num, color_cell.b - act.b) : Math.min(num, act.b - color_cell.b));
        color_DO = color_cell.getC();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(color_DO);

        int r = getPos().getRr();
        int rx = getPos().getRx();
        int ry = getPos().getRy();
        if (getFriends().isEmpty()) {
            Utils.fillCircle(g, rx, ry, r);
        } else if (r < 5) {
            Utils.fillSquare(g, rx, ry, r);
        } else {
            Utils.fillCircle(g, rx, ry, r);
			int[][] points = new int[DIRECTION.size()][2];
			var values = getFriends().values();
			try {
				//Посчитаем наших друзей
				int index = 0;
				for (Iterator<AliveCell> iterator = values.iterator(); iterator.hasNext(); index++) {
					AliveCell i = iterator.next();
					int rxc = i.getPos().getRx();
					if (getPos().getX() == 0 && i.getPos().getX() == Configurations.MAP_CELLS.width - 1) {
						rxc = rx - r;
					} else if (i.getPos().getX() == 0 && getPos().getX() == Configurations.MAP_CELLS.width - 1) {
						rxc = rx + r;
					}
					points[index][0] = rxc;
					points[index][1] = i.getPos().getRy();
				}
				//Приходится рисовать в два этапа, иначе получается ужас страшный.
				//Этап первый - основные связи

				Graphics2D g2 = (Graphics2D) g;
				Stroke oldStr = g2.getStroke();
				g.setColor(color_DO);
				g2.setStroke(new BasicStroke(r / 2));
				for (int i = 0; i < index; i++) {
					int delx = points[i][0] - rx;
					int dely = points[i][1] - ry;
					g.drawLine(rx, ry, rx + delx / 3, ry + dely / 3);
				}
				g2.setStroke(oldStr);
				g.setColor(Color.BLACK);
				//Этап второй, всё тоже самое, но теперь лишь тонкие линии
				for (int i = 0; i < index; i++) {
					g.drawLine(rx, ry, points[i][0], points[i][1]);
				}
			} catch (java.util.ConcurrentModificationException e) {/* Выскакивает, если кто-то из наших друзей погиб*/                }
        }
        if (r > 10) {
            g.setColor(Color.PINK);
            g.drawLine(rx, ry, rx + direction.addX * r / 2, ry + direction.addY * r / 2);
        }

    }

    @Override
    public void setHealth(double health) {
        this.health = health;
        if (this.health < 0) {
            bot2Organic();
        }
    }

    public void setFriend(AliveCell friend) {
        if (friend == null || getFriends().get(friend.getPos()) != null) {
            return;
        }
        getFriends().put(friend.getPos(), friend);
        friend.getFriends().put(getPos(), this);
    }

    @Override
    public JSON toJSON(JSON make) {
        make.add("DNA", getDna().toJSON());
        make.add("health", health);
        make.add("mineral", mineral);
        make.add("buoyancy", buoyancy);
        make.add("direction", DIRECTION.toNum(direction));
        make.add("DNA_wall", DNA_wall);
        make.add("posionType", getPosionType().ordinal());
        make.add("posionPower", getPosionPower());
        make.add("tolerance", tolerance);
        make.add("foodTank", getFoodTank());
        make.add("mineralTank", mineralTank);
        make.add("mucosa", getMucosa());
        make.add("hp_by_div", hp_by_div);

        //=================ПАРАМЕТРЫ БОТА============
        make.add("Generation", Generation);
		if(evolutionNode != null)	//Для клеток дерева эволюции.
			make.add("GenerationTree", evolutionNode.getBranch());
		else
			make.add("GenerationTree", "");

        //=================ЭВОЛЮЦИОНИРУЮЩИЕ ПАРАМЕТРЫ============
        make.add("phenotype", Integer.toHexString(phenotype.getRGB()));

        //===============МНОГОКЛЕТОЧНОСТЬ===================
        JSON[] fr = new JSON[getFriends().size()];
        Object[] points = getFriends().keySet().toArray();
        for (int i = 0; i < fr.length; i++) {
            fr[i] = ((Point) points[i]).toJSON();
        }
        make.add("friends", fr);

        //===============СПЕЦИАЛИЗАЦИЯ===================
        make.add("Specialization", getSpecialization().toJSON());

        return make;
    }

}
