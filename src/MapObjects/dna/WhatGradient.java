package MapObjects.dna;

import MapObjects.AliveCell;
import Calculations.Point;

/** Проверяет градиент по клеткам
 * @author Kerravitarr
 *
 */
public class WhatGradient extends CommandExplore {
	public interface GetParam {
        /**Надо вернуть значение параметра в этой точке*/
		public double getParam(AliveCell cell, Point point);
	}
	/**Функция, которая возвращает градиент*/
	private final GetParam PARAM;
	/**
	 * Проверить некоторый параметр без ограничения по максимальному значению
	 * @param name Имя этой функции
     * @param isA Какое берём направление - абсолютное?
	 * @param iface функция, возвращающая значение параметра
	 */
	public WhatGradient(String name, boolean isA, GetParam iface) {
		super(isA, 1, 2, name);
		PARAM = iface;
    };
    
	@Override
	protected int explore(AliveCell cell) {
        var param = param(cell, 0, isAbolute);
        var point1 = cell.getPos();
        var point2 = point1.next(param);
        return PARAM.getParam(cell,point1) > PARAM.getParam(cell,point2) ? 0 : 1;
	}
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		return String.valueOf(param(cell, 0,isAbolute));
	}
	@Override
	public String getBranch(AliveCell cell, int numBranch, DNA dna){
		return branchMoreeLees(cell,numBranch,dna);
	};
}
