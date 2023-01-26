package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;

/**
 * Преобразует мениралы в энергию
 * Тоже зависит от специального числа - photosynthesisEffect, но теперь чем оно ближе к 0, тем больше придёт минералов
 */
public class Minerals2Energy extends CommandDo {

	protected Minerals2Energy() {super("-МП","Ням мин");}

	@Override
	protected void doing(AliveCell cell) {
		double maxMin = 10;
        if (cell.getMineral() > maxMin) {   // максимальное количество минералов, которые можно преобразовать в энергию = 10
        	cell.setMineral(Math.round(cell.getMineral() - maxMin));
        	var add_hp = Math.round(10 * cell.get(AliveCellProtorype.Specialization.TYPE.MINERAL_PROCESSING) * maxMin);
        	cell.addHealth(add_hp); // Максимум 1 минрал преобразуется в 4 хп
        	cell.color(AliveCell.ACTION.EAT_MIN,add_hp);
        	
        } else if(cell.getMineral() > 0) {  // если минералов меньше, то все минералы переходят в энергию
        	var add_hp = Math.round(10 * cell.get(AliveCellProtorype.Specialization.TYPE.MINERAL_PROCESSING) * cell.getMineral());
        	cell.color(AliveCell.ACTION.EAT_MIN,add_hp);
        	cell.addHealth(add_hp);
        	cell.setMineral(0);
        }
	}
}
