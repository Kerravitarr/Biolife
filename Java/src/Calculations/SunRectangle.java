/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import java.awt.Graphics;

/**
 *Самое обычное прямоугольное солнце.
 * Представляет собой стандартную длинную лампочку
 * от которой исходит полоска света, рассеивающаяся со временем
 * @author Kerravitarr
 */
public class SunRectangle extends SunAbstract {
	/**Ширина излучающей поверхности*/
	private final int width;
	/**Высота излучающей поверхности*/
	private final int height;
	
	/**Создаёт излучающую полоску света
	 * @param p сила излучения
	 * @param move движение ЦЕНТРА этой полоски
	 * @param width ширина полоски
	 * @param height высота полоски
	 */
	public SunRectangle(double p, Trajectory move, int width, int height) {
		super(p, move);
		this.width = width;
		this.height = height;
	}

	@Override
	public double getEnergy(Point pos) {
		//Расстояние от точки до центра нашей полосы
		final var d = pos.distance(position);
		final var absX = Math.abs(d.x);
		final var absY = Math.abs(d.y);
		if(absX <= width / 2 && absY <= height / 2 )
			return power;
		else
			return power - Configurations.DIRTY_WATER * Math.max(absX - width / 2, absY - height / 2);
	}

	@Override
	protected void move() {}

	
	
	@Override
	public void paint(Graphics g) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}
}
