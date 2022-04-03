package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import static MapObjects.dna.CommandDNA.nextPoint;
import static MapObjects.dna.CommandDNA.relatively;
import main.Configurations;
import main.Point;
import main.Point.DIRECTION;

/**
 * Крутится вокруг бота, выискивая врага
 * @author Kerravitarr
 *
 */
public class EnemyNear extends CommandExplore {

	public EnemyNear() {this("🔍 ]:|","Враг рядом?");}
	protected EnemyNear(String shotName, String longName) {super(shotName,longName,2);}
	
	@Override
	protected int explore(AliveCell cell) {
		return search(cell,OBJECT.ENEMY);
	}

	protected int search(AliveCell cell, OBJECT type) {
		for (int i = 0; i < DIRECTION.size()/2+1; i++) {
			if(i == 0 || i == 4) {
				Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(i)));
				if (Configurations.world.test(point) == type)
					return 1;
			} else {
				int dir = cell.getAge() % 2 == 0 ? i : -i; //Хоть какой-то фактр рандомности появления потомка
				Point point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(dir)));
				if (Configurations.world.test(point) == type)
					return 1;
				dir = -dir;
				point = nextPoint(cell,relatively(cell,DIRECTION.toEnum(dir)));
				if (Configurations.world.test(point) == type)
					return 1;
			}
		}
		return 0;
	}
}
