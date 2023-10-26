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
	public static final Color SKY =  new Color(117, 187, 253, 255);
	/**Цвет водички пруда*/
	public static final Color WATER_POND = new Color(35,137,218, 255);
	/**Цвет водички реки*/
	public static final Color WATER_RIVER = new Color(116,204,244, 255);
	/**Цвет водички авкариума*/
	public static final Color WATER_AQUARIUM = new Color(116,204,244, 255);
	/**Цвет водички реки*/
	public static final Color WATER_OCEAN = new Color(90,188,216, 255);
	/**Цвет земли, грязи, низа*/
	public static final Color DRY = new Color(139, 69, 19, 255);
	/**Цвет солнечного света*/
	public static final Color SUN = new Color(249,220,38, 255);
	/**Цвет солнцечного света при минимуме*/
	public static final Color SUN_DARK = toDark(AllColors.SUN,0);
	/**Цвет минералов*/
	public static final Color MINERALS = Utils.getHSBColor(300d / 360, 1, 1, 1);
	/**Цвет минералов при минимуме*/
	public static final Color MINERALS_DARK = toDark(AllColors.MINERALS,0);
	/**Цвет потока*/
	public static final Color STREAM = new Color(0, 0, 205, 64);
	/**Цвет речного песка*/
	public static final Color SAND =  new Color(242,210,169, 255);
	/**Цвет дубого стола*/
	public static final Color OAK =  new Color(85, 52, 43, 255);
	/**Цвет стекла*/
	public static final Color GLASS =  new Color(0xc9dce2);
	
	/**Цвет точки траектории.*/
	public static final Color TRAJECTORY_POINT =  new Color(225, 0, 0, 255);
	/**Цвет линии траектории.*/
	public static final Color TRAJECTORY_LINE =  new Color(0, 255, 0, 255);
	
	/**Цвет ороговевшей стены*/
    public static final Color FOSSIL = Color.BLACK;
	/**Цвет орагиники*/
    public static Color ORGANIC = new Color(139,69,19,200);
	
	
	/**Преобразует цвет в его более прозрачный вариант
	 * @param from какой цвет
	 * @param alfa какая у него теперь будет альфа
	 * @return цвет с установленной альфой
	 */
	public static Color toDark(Color from, int alfa){
		return new Color(from.getRed(), from.getGreen(), from.getBlue(), Utils.betwin(0, alfa, 255));
	}
	
	/**
	 * Смешивает цвета в равной пропорции
	 * @param size количество цветов
	 * @param c массив цветов, может быть заполнен лишь частично
	 * @return итоговый цвет
	 */
	public static Color blend(int size, Color... c) {
		if(size == 1) return c[0];
		
		final float ratio = 1f / (size * 255f);

		float a = 0;
		float r = 0;
		float g = 0;
		float b = 0;

		for (int i = 0; i < size; i++) {
			a += c[i].getAlpha() * ratio;
			r += c[i].getRed() * ratio;
			g += c[i].getGreen() * ratio;
			b += c[i].getBlue() * ratio;
		}

		return new Color(r, g, b, a);
	}
	
}
