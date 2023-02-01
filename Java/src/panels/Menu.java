package panels;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.CellObjectRemoveException;
import main.Configurations;
import main.World;

public class Menu extends JPanel {
	
	enum MENU_SELECT{
		NONE,
		REMOVE
	}
	/**Поток автоматического обновления*/
	private class WorkTask implements Runnable{
		@Override
		public void run() {
			if(isVisible()) {
				update_per_second();
			} 
		}
	}
	
	/**Какая из кнопок выбрана*/
	private MENU_SELECT select = MENU_SELECT.NONE;
	private final JButton start;
	private boolean startButtonIsStart = true;

	/**
	 * Create the panel.
	 */
	public Menu() {
		Configurations.menu = this;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		//Конфигурация мира
		//Рестарт
		add(makeButton("save", e-> Configurations.settings.save()));
		add(makeButton("load", e-> Configurations.settings.load()));
		add(makeButton("search", e-> System.out.println(e)));
		add(start = makeButton("play", e->{World.isActiv = !World.isActiv;} ));
		add(makeButton("record", e-> System.out.println(e)));
		add(makeButton("graph", e-> System.out.println(e)));
		add(makeButton("cursor", e-> toDefault()));
		add(makeButton("kill", e-> remove()));
		

		Configurations.TIME_OUT_POOL.scheduleWithFixedDelay(new WorkTask(), 100, 100, TimeUnit.MILLISECONDS);
	}

	private JButton makeButton(String name, ActionListener al) {
		
		JButton button = new JButton();
		Configurations.setIcon(button,name);
		button.setMaximumSize (new Dimension(40,20));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(al);
        button.setToolTipText(Configurations.getHProperty(Menu.class,name));
        button.setFocusable(false);
		return button;
	}

	private void remove() {
		select = MENU_SELECT.REMOVE;
		Configurations.world.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}
	private void toDefault() {
		select = MENU_SELECT.NONE;
		Configurations.world.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	/**Функция, вызываемая каждую секунду автоматически*/
	private void update_per_second() {
		var isA = World.isActiv;
		if(isA != startButtonIsStart) {
			startButtonIsStart = isA;
			Configurations.setIcon(start,isA ? "play" : "pause");
		}
	}

	public void setCell(CellObject cellObject) {
		if(cellObject == null) return;
		switch (select) {
			case NONE -> {}
			case REMOVE -> {
				if (cellObject instanceof AliveCell acell) {
					try {
						acell.bot2Organic();
					}catch (CellObjectRemoveException e) {}
				} else {
					cellObject.remove_NE();
				}
			}
		}
	}

}
