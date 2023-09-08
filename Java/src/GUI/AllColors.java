/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import Utils.Utils;
import java.awt.Color;

/**
 * Тут перечисленны все цвета, которые отображаются на экране
 * @author Kerravitarr
 */
public class AllColors {
	/**Цвет неба, открытого воздуха*/
	public static final Color SKY =  new Color(224, 255, 255, 255);
	/**Цвет водички*/
	public static final Color WATER = Utils.getHSBColor(240d / 360, 0.7, 1, 1);
	/**Цвет земли, грязи, низа*/
	public static final Color DRY = new Color(139, 69, 19, 255);
	/**Цвет солнечного света*/
	public static final Color SUN = Utils.getHSBColor(180d / 360, 1, 1, 1);
	/**Цвет солнцечного света при минимуме*/
	public static final Color SUN_DARK = toDark(AllColors.SUN,0);
	/**Цвет минералов*/
	public static final Color MINERALS = Utils.getHSBColor(300d / 360, 1, 1, 1);
	/**Цвет минералов при минимуме*/
	public static final Color MINERALS_DARK = toDark(AllColors.MINERALS,0);
	/**Цвет потока*/
	public static final Color STREAM = new Color(0, 0, 205, 64);
	
	
	/**Преобразует цвет в его более прозрачный вариант
	 * @param from какой цвет
	 * @param alfa какая у него теперь будет альфа
	 * @return цвет с установленной альфой
	 */
	public static Color toDark(Color from, int alfa){
		return new Color(from.getRed(), from.getGreen(), from.getBlue(), Utils.betwin(0, alfa, 255));
	}
}
