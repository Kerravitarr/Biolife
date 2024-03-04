/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ColorRec;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Анимация для океана
 * Океан - поле, с двух сторон (неизвестно с каких) оно будет сшито, а две соатвшиеся стороны могут быть чем угодно
 * 
 * @author Kerravitarr
 */
public class Ocean extends DefaultAnimation{
	/**водичка*/
	private ColorRec water0;
	/**водичка*/
	private ColorRec water;
	/**водичка*/
	private ColorRec water1;
	
	public Ocean(WorldView.Transforms transform, int w, int h){
		//Поле, вода
		final int xw[] = new int[4];
		final int yw[] = new int[4];
		//Верхний прямоугольник
		final int xu[] = new int[4];
		final int yu[] = new int[4];
		//Нижний блок
		final int xd[] = new int[8];
		final int yd[] = new int[8];

		xu[0] = xu[3] = xd[0] = xd[7] = 0;
		xd[1] = xd[2] = xw[0] = xw[3] = transform.toScrinX(0);
		xd[3] = xd[4] = xw[1] = xw[2] = transform.toScrinX(Configurations.getWidth()-1);
		xd[5] = xd[6] = xu[1] = xu[2] = w;

		yu[0] = yu[1] = 0;
		yd[0] = yd[0] = yd[1] = yd[4] = yd[5] = yu[2] = yu[3] = yw[0] = yw[1] = transform.toScrinY(0);
		yd[2] = yd[3] = yw[2] = yw[3] = transform.toScrinY(Configurations.getHeight()-1);
		yd[6] = yd[7] = h;

		water0 = new ColorRec(xu,yu, AllColors.WATER_OCEAN);
		water = new ColorRec(xw,yw, AllColors.WATER_OCEAN);
		water1 = new ColorRec(xd,yd, AllColors.WATER_OCEAN);	
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g, Rectangle visible) {
		water0.paint(g);
		water1.paint(g);
	}
	
}
