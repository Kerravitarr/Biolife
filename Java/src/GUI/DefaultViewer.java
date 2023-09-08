/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import javax.swing.JPanel;

/**
 * Вид по умолчанию
 * Состоит из 5 панелей:
 *				Меню
 * Инфа о боте / МИР / Настройки
 *				Легенда
 * @author Kerravitarr
 */
public class DefaultViewer extends Viewers{
	/**Сюда отправляем бота, для его изучения*/
	private static BotInfo info = null;
	/**Настройки мира*/
	private static Settings settings = null;
	/**Отображение мира*/
	private static WorldView view = null;
	/**Меню мира со всеми его кнопочками*/
	private static Menu menu = null;
	/**Легенда мира, как его раскрашивать?*/
	private static Legend legend = null;
	
	public DefaultViewer(){
		if(settings == null)
			settings = new Settings();
		if(info == null)
			info = new BotInfo();
		if(menu == null)
			menu = new Menu();
		if(view == null)
			view = new WorldView();
		if(legend == null)
			legend = new Legend();
	}

	@Override
	public JPanel get(String panelName) {
		return switch (panelName) {
			case "Settings" -> settings;
			case "BotInfo" -> getBotInfo();
			case "Menu" -> getMenu();
			case "Legend" -> getLegend();
			case "World" -> getWorld();
			default -> throw new IllegalArgumentException("Панели" + panelName + " нет в наборе " + this);
		};
	}
	@Override
	public <T extends JPanel> T get(Class<T> cl) throws IllegalArgumentException{
		if (cl.equals(BotInfo.class)) return (T) getBotInfo();
		else if(cl.equals(Settings.class)) return (T) settings;
		else if(cl.equals(WorldView.class)) return (T) getWorld();
		else if(cl.equals(Menu.class)) return (T) getMenu();
		else if(cl.equals(Legend.class)) return (T) getLegend();
		else throw new IllegalArgumentException("Панели" + cl + " нет в наборе " + this);
	}
	public Menu getMenu(){return menu;}
	public WorldView getWorld(){return view;}
	public BotInfo getBotInfo(){return info;}
	public Legend getLegend(){return legend;}
	@Override
	public String toString(){
		return "Основная панель для отображения данных";
	}
}
