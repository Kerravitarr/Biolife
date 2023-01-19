package MapObjects.dna;

import MapObjects.AliveCell;

/**
 * Преобразует мениралы в энергию
 * Тоже зависит от специального числа - photosynthesisEffect, но теперь чем оно ближе к 0, тем больше придёт минералов
 */
public class Minerals2Energy extends CommandDo {

	protected Minerals2Energy() {super("-МП","Ням мин");}

	@Override
	protected void doing(AliveCell cell) {
		double maxMin = 20 * (1 + (4-cell.photosynthesisEffect));
        if (cell.getMineral() > maxMin) {   // максимальное количество минералов, которые можно преобразовать в энергию = 100
        	cell.setMineral(Math.round(cell.getMineral() - maxMin));
        	var add_hp = Math.round((4-cell.photosynthesisEffect) * maxMin);
        	cell.addHealth(add_hp); // Максимум 1 минрал преобразуется в 4 хп
        	cell.goBlue((int)add_hp);  // бот от этого синеет
        } else {  // если минералов меньше, то все минералы переходят в энергию
        	cell.goBlue((int) cell.getMineral());
        	cell.addHealth(Math.round((4-cell.photosynthesisEffect) * cell.getMineral()));
        	cell.setMineral(0);
        }
	}
}
