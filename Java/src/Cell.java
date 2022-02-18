import java.awt.Color;
import java.awt.Graphics;

public class Cell {
	/**Размер мозга*/
	static final int MINDE_SIZE = 8 * 8;//*8 - так как вокруг каждой клетки 6 соседних клеток
	/**Начальный уровень здоровья клеток*/
	static final int StartHP = 5;
	/**Начальный уровень минералов клеток*/
	static final int StartMP = 5;
	/**Сколько нужно жизней для размножения, по умолчанию*/
	static final int maxHP = 999;
	/**Сколько можно сохранить минералов*/
	static final int MAX_MP = 999;
	/**На сколько организм тяготеет к фотосинтезу (0-4)*/
	static final double defFotosin = 2;
	/**Статус*/
	enum LV_STATUS {LV_ALIVE,LV_ORGANIC_HOLD,LV_ORGANIC_SINK};
	/**Статус*/
	enum DIRECTION {UP,UP_R,RIGHT,DOWN_R,DOWN,DOWN_L,LEFT,UP_L};
	
    /**Позиция бота в трёх координатах*/
	World.Point pos;
	/**Внутренний счётчик процессора*/
    int processorTik = 0;
    /**Сознание существа, его мозг*/
    int [] mind = new int[MINDE_SIZE];
    /**Состояние животного*/
    LV_STATUS alive = LV_STATUS.LV_ALIVE;
    /**Жизни*/
    int health = StartHP;
    //Минералы
    int mineral = StartMP;
    //Жизни для размножения
    double hpForDiv = maxHP;
    //Максимально возможное хранение минералов
    double maxMP = MAX_MP;
    //Показывает на сколько организм тяготеет к фотосинтезу
    double photosynthesisEffect = defFotosin;
    //Цвет бота
    Color color = new Color(255,255,255);
    /**Направление движения*/
    DIRECTION direction = DIRECTION.UP;
    //Счётчик, показывает ходил бот в этот ход или нет
    int stepCount = -1;
    
    Cell(){
    	pos = new World.Point(Utils.random(0, World.MAP_CELLS.width),Utils.random(0, World.MAP_CELLS.height));
    }

	public void step() {
		// TODO Auto-generated method stub
		
	}

	public void paint(Graphics g) {
		g.setColor(color);
		Utils.fillCircle(g,pos.getRx(),pos.getRy(),pos.getRr());
	}

}
