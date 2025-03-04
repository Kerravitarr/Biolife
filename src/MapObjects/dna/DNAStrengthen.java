package MapObjects.dna;

import MapObjects.AliveCell;

/**Укрепляет ДНК клетки, защищая её от вирусных атак*/
public class DNAStrengthen extends CommandDo {
	/**Цена энергии на ход*/
	private final double HP_COST = DEF_COAST;
	
	protected DNAStrengthen() {super();}

	@Override
	protected void doing(AliveCell cell) {
		cell.addHealth(-HP_COST);
		cell.setDNA_wall(cell.getDNA_wall() + 1);
	}
}
