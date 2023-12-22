package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import Calculations.Point.DIRECTION;
/**
 * Ищет всё, что угодно вокруг, кроме пустоты и стены. Возвращает индекс согласно найденому объекту
 * 
 * 
 * Кое что про функцию see:
* Только для живой клетки не возвращает ALIVE, а возвращает ENEMY или FRIEND
* Только для жиовй клетки не возвращает BANE, а возвращает POISON или NOT_POISON
* 
 * @author Kerravitarr
 *
 */
public class WhoIsNearby extends CommandExplore {
	/**Количество возможных найденных объектов*/
	static final int OBJECT_L = OBJECT.lenght - 2 - 2; //-2 так как не ищет CLEAN и WALL. -2 так как не может найти ALIVE и BANE.
	
	/**
	 * Ищет первый попавшийся объект
	 */
	public WhoIsNearby() {super(OBJECT_L);}
	
	@Override
	protected int explore(AliveCell cell) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
			if(i == 0 || i == 4) {
				var ret = see(cell,i);
				if(ret != -1)
					return ret;
			} else {
				int dirI = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
				var ret = see(cell,dirI);
				if(ret != -1)
					return ret;
				ret = see(cell,-dirI);
				if(ret != -1)
					return ret;
			}
		}
		return OBJECT_L - 1; //-1, так как у нас ветви считаются с 0
	}
	/**
	 * Ищет у клетки относительно её моськи нужный объект
	 * @param cell клетка
	 * @param dirNum направление, в какое смотрим
	 * @return -1, если такой объект не обнаружен или точное число, соответствующее обнаруженному объекту
	 */
	private int see(AliveCell cell, int dirNum) {
		var dir = relatively(cell, DIRECTION.toEnum(dirNum));
		var see = cell.see(dir);
		if (see != OBJECT.CLEAN && see != OBJECT.WALL) {
			var ret = see.ordinal();
			if (ret >= OBJECT.CLEAN.ordinal()) ret--;
			if (ret >= OBJECT.WALL.ordinal()) ret--;
			if (ret >= OBJECT.ALIVE.ordinal()) ret--;
			if (ret >= OBJECT.BANE.ordinal()) ret--;
			return ret;
		} else {
			return -1;
		}
	}
	
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		if (numBranch >= OBJECT.CLEAN.ordinal())numBranch++;
		if (numBranch >= OBJECT.WALL.ordinal()) numBranch++;
		if (numBranch >= OBJECT.ALIVE.ordinal())numBranch++;
		if (numBranch >= OBJECT.BANE.ordinal())	numBranch++;
		if (numBranch == OBJECT_L-1) {
			return "∅";
		} else {
			for (var o : OBJECT.values) {
				if (o.ordinal() == numBranch) {
					return o.toString();
				}
			}
			throw new AssertionError("Не смогли найти ветвь " + numBranch);
		}
	}
}
