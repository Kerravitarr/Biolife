/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Emitters;

import Calculations.Configurations;
import Calculations.Point;
import Utils.JSON;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Набор излучателей
 * @author Kerravitarr
 * @param <T> каких именно излучателей это набор
 */
public class EmitterSet<T extends DefaultEmitter> {
	/**Матрица энергии излучателя*/
	private double[][] Energy = new double[0][0];
	/**"Нулевой" массив*/
	private double[] NullE = new double[0];
	/**Набор излучателей*/
	private ArrayList<T> _emitters = new ArrayList<>();
	
	/**Возвращает количество излучения в этой точке
	 * @param where точка, где излучается
	 * @return суммарное излучение
	 */
	public double getE(Point where){
		if(Energy[where.getX()][where.getY()] == -1){
			Energy[where.getX()][where.getY()] = _emitters.stream().reduce(0d, (a,b) -> a + b.getE(where), Double::sum);
		}
		return Energy[where.getX()][where.getY()];
	}
	
	public void step(long step) {
		final var recalculation = new boolean[1];
		recalculation[0] = false;
		_emitters.parallelStream().forEach(e -> {
			if(e.step(step)) {
				e.recalculation();
				recalculation[0] = true;
			}
		});
		if(recalculation[0]){
			if (Energy.length != Configurations.getWidth() || Energy[0].length != Configurations.getHeight()) {
				Energy = new double[Configurations.getWidth()][Configurations.getHeight()];
				NullE = new double[Configurations.getHeight()];
				Arrays.fill(NullE, -1d);
			}
			for (int x = 0; x < Configurations.getWidth(); x++) {
				System.arraycopy(NullE	, 0, Energy[x], 0, NullE.length);
			}
		}
	}
	
	/**Показывает, что нужно обновить матрицу энергии*/
	public void updateMatrix() {
		_emitters.forEach(e -> e.updateMatrix());
	}
	/**Добавляет ещё один излучаетль в набор
	 * @param add добавляемый излучатель
	 */
	public void add(T add) {_emitters.add(add);}
	/**Удаляет излучатель из набора
	 * @param add удаляемый излучатель
	 */
	public void remove(T add) {_emitters.remove(add);}
	/**Возваращает излучатель по индексу
	 * @param index индекс излучателя
	 * @return сам излучатель
	 */
	public T get(int index) {return _emitters.get(index);}
	/**Возвращает количество излучателей
	 * @return количество излучателей
	 */
	public int size(){return _emitters.size();}
	/**Преобразует излучатель в JSON объект
	 * @param mapper функция преобразования из T в JSON
	 * @return список всех излучателей в формате JSON
	 */
	public List<JSON> serialization(Function<T, JSON> mapper) {
		return _emitters.stream().map(mapper).toList();
	}
}
