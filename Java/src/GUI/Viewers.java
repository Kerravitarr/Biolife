/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import javax.swing.JPanel;

/**
 *Класс, описывающий возможные наборы отображения в главном экране приложения
 * @author Kerravitarr
 */
public abstract class Viewers {
	/**Возвращает панель по её имени
	 * @param panelName имя панели
	 * @return панель, которую мы знаем
	 * 
	 * @throws IllegalArgumentException если такой панели нет в отображемых
	 */
	public abstract JPanel get(String panelName);
	
}
