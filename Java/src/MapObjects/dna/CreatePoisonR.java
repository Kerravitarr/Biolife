package MapObjects.dna;

import MapObjects.AliveCell;
import MapObjects.Poison.TYPE;
import main.Point.DIRECTION;

/**
 * Преобразует мениралы в энергию
 * Тоже зависит от специального числа - photosynthesisEffect, но теперь чем оно ближе к 0, тем больше придёт минералов
 */
public class CreatePoisonR extends CreatePoisonA {

	@Override
	protected void doing(AliveCell cell) {
		if (cell.getPosionType() != TYPE.НЕТ)
			addPosion(cell,relatively(cell,param(cell,0, DIRECTION.size())));
	}
}
