/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import java.awt.Graphics2D;

/**
 * Базовый класс для всех анимаций
 * @author Kerravitarr
 */
public abstract class DefaultAnimation {
	
	/** @param g холст, на котором будет отрисована вода*/
	public abstract void water(Graphics2D g);
	/** @param g холст, на котором будет отрисован холст*/
	public abstract void world(Graphics2D g);
}
