package panels;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import main.Configurations;

public class Menu extends JPanel {

	/**
	 * Create the panel.
	 */
	public Menu() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		
		//Сохранить
		//Загрузить
		//Конфигурация мира
		//Рестарт
		//Уничтожить объект. Клетка умирает, остальное исчезает
		add(makeButton("save", e-> Configurations.settings.save()));
		add(makeButton("load", e-> Configurations.settings.load()));
		add(makeButton("kill", e-> remove()));
	}

	private JButton makeButton(String name, ActionListener al) {
		var icon_const = new ImageIcon(Menu.class.getClassLoader().getResource(MessageFormat.format("resources/{0}.png", name)));
		var icon_select = new ImageIcon(Menu.class.getClassLoader().getResource(MessageFormat.format("resources/{0}_active.png", name)));
		
		JButton button = new JButton();
		button.setMaximumSize (new Dimension(40,20));
		button.setIcon(new ImageIcon(icon_const.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
		button.setRolloverIcon(new ImageIcon(icon_select.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(al);
        button.setToolTipText(Configurations.getHProperty(Menu.class,name));
		return button;
	}

	private void remove() {
		Configurations.world.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

}
