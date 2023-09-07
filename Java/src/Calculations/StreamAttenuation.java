/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

/**
 *Способ затухания энергии от максимальной к минимальной
 * @author Kerravitarr
 */
public abstract class StreamAttenuation {
	/**Нет затухания*/
	public static class NoneStreamAttenuation extends StreamAttenuation {
		@Override
		public int power(int min, int max, double dist) {return max;}
	}
	/**Линейное затухание*/
	public static class LinealStreamAttenuation extends StreamAttenuation {
		@Override
		public int power(int min, int max, double dist) {
			return (int) (min + (max - min) * Math.abs(dist));
		}
	}
	/**Степенное затухание*/
	public static class PowerFunctionStreamAttenuation extends StreamAttenuation {
		/**Степень функции*/
		private final double power;
		/** Создание степенного затухания - функции вида y=a^power
		 * @param power Степень функции.Берётся отрезок [0,1]
		 */
		public PowerFunctionStreamAttenuation(double power){
			this.power = power;
		}
		@Override
		public int power(int min, int max, double dist) {
			return (int) (min + (max - min) * Math.pow(Math.abs(dist), power));
		}
	}
	/**Синусоидальное затухание. То есть минимальное значение будет при dist = 0 и 1, а максимальное при dist = 0.5*/
	public static class SinStreamAttenuation extends StreamAttenuation {
		/**Степень функции*/
		private final double power;
		/** Создание степенного затухания - функции вида y=sin(x*pi)^power
		 * @param power Степень функции 
		 */
		public SinStreamAttenuation(double power){
			this.power = power;
		}
		@Override
		public int power(int min, int max, double dist) {
			return (int) (min + (max - min) * Math.pow(Math.sin(Math.abs(dist) * Math.PI), power));
		}
	}
	
	
	
	/**Возвращает реальную мощность в определённой точки в зависимости от удалённости от центра потока
	 * @param min минимальное значение энергии потока
	 * @param max максимальное значение энергии потока
	 * @param dist расстояние от центра в процетнах [0,1]
	 * @return мощность потока [min,max]
	 */
	public abstract int power(int min, int max, double dist);
}
