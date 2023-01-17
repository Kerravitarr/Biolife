package MapObjects.dna;

import MapObjects.AliveCell;
/**
 * @author Kerravitarr
 *
 */
public class ToWall extends CommandDo {
	/**Удаляет клетку и на её месте устанавливает стену*/
	protected ToWall() {super("|||","Окаменеть");}

	@Override
	protected void doing(AliveCell cell) {
		cell.bot2Wall();
	}
}
