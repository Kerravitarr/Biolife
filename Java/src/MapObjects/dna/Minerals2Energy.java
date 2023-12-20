package MapObjects.dna;

import Calculations.Configurations;
import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import Utils.MyMessageFormat;

/**
 * Преобразует мениралы в энергию
 * Тоже зависит от специального числа - photosynthesisEffect, но теперь чем оно ближе к 0, тем больше придёт минералов
 */
public class Minerals2Energy extends CommandDo {
	/**Цена энергии на ход. Да, на периваривание минералов тоже нужна энергия!*/
	private final int HP_COST = 2;
	/**Сколько ХП дадут за 1 минерал*/
	private static final long MIN_PER_HP = 10;
	/**Сколько максимально минералов можно переработать. 1/4 от полных жизней*/
	private static final double MAX_MIN = (AliveCellProtorype.MAX_HP / MIN_PER_HP) / 4;
	
	private final MyMessageFormat valueFormat = new MyMessageFormat("MP -= {0, number, #.#} HP += {0, number, #.#}");

	protected Minerals2Energy() {super();}

	@Override
	protected void doing(AliveCell cell) {
		//Сколько мы можем съесть максимум
		final var maxEat = ((AliveCell.MAX_HP - cell.getHealth()) + TankFood.leftover(cell));
		//Сколько мы можем взять минералов
		var min = Math.min(cell.getMineral(), maxEat / MIN_PER_HP);
    	var add_hp = cell.specMaxVal(MIN_PER_HP * min, AliveCellProtorype.Specialization.TYPE.MINERAL_PROCESSING);
    	cell.color(AliveCell.ACTION.EAT_MIN,add_hp);
    	cell.addHealth(add_hp);
    	cell.addMineral((long) -min);
		cell.addHealth(-HP_COST);
	}
	

	@Override
	protected String value(AliveCell cell) {
         if(cell.getMineral() > 0) {  // если минералов меньше, то все минералы переходят в энергию
			var min = Math.min(cell.getMineral(), MAX_MIN);
			var add_hp = cell.specMaxVal(MIN_PER_HP * min, AliveCellProtorype.Specialization.TYPE.MINERAL_PROCESSING);
        	return Configurations.getProperty(Minerals2Energy.class,isFullMod() ? "value.L" : "value.S",min,add_hp - HP_COST);
        } else {
        	return Configurations.getProperty(Minerals2Energy.class,isFullMod() ? "value.non.L" : "value.non.S",HP_COST);
        }
	}
}
