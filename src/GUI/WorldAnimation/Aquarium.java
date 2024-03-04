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
 * Анимация для аквариума.
 * Тут сложнее. Аквариум. Это бак с водой стоящий на подоконнике. За ним - окно. За окном погода меняется. Можно сделать занавески
 * При изменении размеров открывается больше стены с обоями или больше комнаты - под аквариумом батарея. Но когда размеры переходят логику, то
 * у нас увеличивается окно, типа оооооочень маленький аквариум стоит на окне
 * 
 * @author Kerravitarr
 */
public class Aquarium extends DefaultAnimation{
	/**Воздух*/
	private ColorRec air;
	/**водичка*/
	private ColorRec water;
	/**стол*/
	private ColorRec table;
	
	public Aquarium(WorldView.Transforms transform, int w, int h){
		//Нижняя часть поля
		final int xd[] = new int[8];
		final int yd[] = new int[8];
		//Поле, вода
		final int xw[] = new int[4];
		final int yw[] = new int[4];
		//Верхняя часть поля
		final int yu[] = new int[8];

		xd[0] = xd[1] = 0;
		xd[6] = xd[7] = xw[0] = xw[3] = transform.toScrinX(0);
		xd[4] = xd[5] = xw[1] = xw[2] = transform.toScrinX(Configurations.getWidth()-1);
		xd[2] = xd[3] = w;

		yd[1] = yd[2] = 0;
		yw[0] = yw[1] = yd[5] = yd[6] = transform.toScrinY(0);
		yd[0] = yd[3] = yd[4] = yd[7] = yu[0] = yu[3] = yu[4] = yu[7] = transform.toScrinY(Configurations.getHeight()-3); //Место сшивания полей
		yw[2] = yw[3] = yu[5] = yu[6] = transform.toScrinY(Configurations.getHeight()-1);
		yu[1] = yu[2] = h;


		air = new ColorRec(xd,yd,AllColors.SKY);
		table = new ColorRec(xd,yu, AllColors.OAK);
		water = new ColorRec(xw,yw, AllColors.WATER_AQUARIUM);
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g, Rectangle visible) {
		air.paint(g);
		table.paint(g);
	}
	
}
