package MapObjects.dna;

import static MapObjects.CellObject.OBJECT.CLEAN;
import static MapObjects.CellObject.OBJECT.NOT_POISON;
import static MapObjects.CellObject.OBJECT.ORGANIC;
import static MapObjects.CellObject.OBJECT.OWALL;
import static MapObjects.CellObject.OBJECT.POISON;
import static MapObjects.CellObject.OBJECT.WALL;

import MapObjects.AliveCell;
import panels.Legend;

/**
 * TODO
 * Команды ДНК:
 * Вставить ген
 * Заменить ген
 * Сравнить ген
 * Вставить кусок ДНК
 * Заменить кусок ДНК
 * Родить потомка с фрагментом ДНК
 * 
 */
/**
 * Базовая основа команды, которая изменяет ДНК.
 * Изменить можно только ДНК того, на кого смотришь
 * @author Kerravitarr
 *
 */
public abstract class DNABreak extends CommandDo {
	/**Цена энергии на ход*/
	protected final int HP_COST_ONE = 2;
	protected final int HP_COST_MANY = 8;

	/**
	 * Констурктор класса
	 * @param countParams - число параметров у функции
	 */
	public DNABreak(int countParams) {
		super(countParams);
		isInterrupt = true;
	}

	/**
	 * Изменить код своей ДНК
	 * @param who кто я
	 * @param index какой индекс
	 * @param comand на какую команду
	 */
	protected void breakDNA(AliveCell who, int index, int comand) {
		breakDNAOne(who,who,false,index,comand);
	}

	/**
	 * Ломает чью-то ДНК
	 * @param who кто ломает
	 * @param target кому ломает
	 * @param index какой индекс, начиная с PC, в ДНК подвергается изменению
	 * @param comand какая новая команда
	 */
	protected void breakDNAOne(AliveCell who,AliveCell target, int index, int comand) {
		breakDNAOne(who,target,false,index,comand);
	}
	/**
	 * Ломает чью-то ДНК
	 * @param who кто ломает
	 * @param target кому ломает
	 * @param breakWall сломать-ли стену перед этим
	 * @param index какой индекс, начиная с PC, в ДНК подвергается изменению
	 * @param comand какая новая команда
	 */
	protected void breakDNAOne(AliveCell who,AliveCell target, boolean breakWall, int index, int comand) {
		who.addHealth(-HP_COST_ONE); // бот теряет на этом 2 энергии в независимости от результата
		if (breakWall && target.getDNA_wall() > 0) {
			target.setDNA_wall(target.getDNA_wall()-1);
		} else {
			if(target.getDna().get(0,target.getDna().getIndex(index)) != comand) {
				target.DNAupdate(index, comand);
				target.setGeneration(target.getGeneration() + 1);
				target.evolutionNode = target.evolutionNode.newNode(target, who.getStepCount());
			}
		}
		if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
			who.color(AliveCell.ACTION.BREAK_DNA,10);
	}
	/**
	 * Ломает чью-то ДНК. Да не просто ломает, а прям огромный кусок заменяет!
	 * @param who кто ломает
	 * @param target кому ломает
	 * @param PC Начиная с какой команды у who вставлять ДНК
	 * @param lenght сколько подменять ДНК
	 */
	protected void breakDNAMany(AliveCell who,AliveCell target, int PC, int lenght) {
		breakDNAMany(who,target,false,PC,lenght);
	}
	
	/**
	 * Ломает чью-то ДНК. Да не просто ломает, а прям огромный кусок заменяет!
	 * @param who кто ломает
	 * @param target кому ломает
	 * @param breakWall сломать-ли стену перед этим?
	 * @param PC Начиная с какой команды у who вставлять ДНК
	 * @param lenght сколько подменять ДНК
	 */
	protected void breakDNAMany(AliveCell who,AliveCell target, boolean breakWall, int PC, int lenght) {
		who.addHealth(-HP_COST_MANY); // бот теряет на этом 2 энергии в независимости от результата
		if (breakWall && target.getDNA_wall() > 0) {
			target.setDNA_wall(target.getDNA_wall()-1);
		} else {
			var bot_dna = who.getDna();
			for (int i = 0; i < lenght; i++)
				target.DNAupdate(i, bot_dna.get(PC,2 + i));
			target.setGeneration(target.getGeneration() + 1);
			target.evolutionNode = target.evolutionNode.newNode(target, who.getStepCount());
		}
		if (Legend.Graph.getMode() == Legend.Graph.MODE.DOING)
			who.color(AliveCell.ACTION.BREAK_DNA,10);
	}
	
	@Override
	public int getInterrupt(AliveCell cell, DNA dna){
		var see = cell.see(cell.direction);
		if (see == CLEAN || see == NOT_POISON || see == ORGANIC || see == POISON || see == WALL || see == OWALL)
			return see.nextCMD;
		else
			return -1;
	}
}
