/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Базовый класс для всех анимаций
 * @author Kerravitarr
 */
public abstract class DefaultAnimation {
	/**Номер кадра для отрисовки. Нужен чтобы узнать - поменялся у нас кадр или нет*/
	private int frame = 0;
	/**Шаг мира. Нужен чтобы понять - поменялся у нас шаг мира или нет*/
	private long step = -1;
	
	/** Вырисовывает на холсте только воду, подложку игрового поля
	 * @param g холст, на котором будет отрисована вода
	 * @param frame номер кадра. Услоное число, которое может и не меняться при слишком большом FPS
	 */
	public void water(Graphics2D g, int frame){
		if(frame!=this.frame){
			this.frame = frame;
			final var s = Configurations.world.step;
			if(s != step){
				step = s;
				nextStep(s);
			}
			nextFrame();
		}
		water(g);
	}
	/** Вырисовывает на холсте только воду, подложку игрового поля
	 * @param g холст, на котором будет отрисована вода
	 */
	protected abstract void water(Graphics2D g);
	/** Вырисовывает на холсте мир - всё, что вокруг игрового поля
	 * @param g холст, на котором будет риосвание
	 * @param visible квадрат, описывающий видимую область. Где правда надо рисовать
	 */
	public abstract void world(Graphics2D g, Rectangle visible);
	/**Сменился шаг мира, надо пересчитать нас 
	 * @param step текущий шаг мира
	 */
	protected void nextStep(long step){}
	/**Сменился кадр на экране. Надо перечертить мир*/
	protected void nextFrame(){}
}
