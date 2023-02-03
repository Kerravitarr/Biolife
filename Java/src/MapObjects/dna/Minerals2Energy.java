package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import Utils.MyMessageFormat;

/**
 * Преобразует мениралы в энергию
 * Тоже зависит от специального числа - photosynthesisEffect, но теперь чем оно ближе к 0, тем больше придёт минералов
 */
public class Minerals2Energy extends CommandDo {
	/**Сколько ХП дадут за 1 минерал*/
	private static final long MIN_PER_HP = 10;
	/**Сколько максимально минералов можно переработать*/
	private static final long MAX_MIN = (AliveCellProtorype.MAX_HP / MIN_PER_HP) / 4;
	
	private final MyMessageFormat valueFormat = new MyMessageFormat("MP -= {0, number, #.#} HP += {0, number, #.#}");

	protected Minerals2Energy() {super();}

	@Override
	protected void doing(AliveCell cell) {
		var min = Math.min(cell.getMineral(), MAX_MIN);
    	var add_hp = Math.round(MIN_PER_HP * cell.get(AliveCellProtorype.Specialization.TYPE.MINERAL_PROCESSING) * min);
    	cell.color(AliveCell.ACTION.EAT_MIN,add_hp);
    	cell.addHealth(add_hp);
    	cell.addMineral(-min);
	}
	

	protected String value(AliveCell cell) {
         if(cell.getMineral() > 0) {  // если минералов меньше, то все минералы переходят в энергию
        	double maxMin = Math.min(MAX_MIN, cell.getMineral());
        	var add_hp = Math.round(MIN_PER_HP * cell.get(AliveCellProtorype.Specialization.TYPE.MINERAL_PROCESSING) * maxMin);
        	return valueFormat.format(maxMin,add_hp);
        } else {
        	return null;
        }
	}
}
