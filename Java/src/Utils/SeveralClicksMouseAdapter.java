/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

/**
 * Специальный класс, который умеет отслеживать множественные нажатия мыши
 * Для этого у него есть новая функция - mouseSeveralClick
 * @author rjhjk
 */
public class SeveralClicksMouseAdapter extends MouseAdapter {

	private final Timer timer;
	/** Задежка по умолчанию для нескольких нажатий */
	private final int DELAY = ((Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval"));
	private MouseEvent lastEevent = null;

	public SeveralClicksMouseAdapter() {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mouseSeveralClick(lastEevent);
			}
		};
		timer = new javax.swing.Timer(DELAY, al);
		timer.setRepeats(false);
	}

	@Override
	public final void mouseClicked(MouseEvent e) {
		checkClicks(e);
	}

	private void checkClicks(MouseEvent e) {
		lastEevent = e;
		timer.restart();
	}

	public void mouseSeveralClick(MouseEvent e){}
}
