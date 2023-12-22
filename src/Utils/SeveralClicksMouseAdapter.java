/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 * Версия 1.0. От 31 марта
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
		ActionListener al = (ActionEvent evt) -> mouseSeveralClick(lastEevent);
		timer = new javax.swing.Timer(DELAY, al);
		timer.setRepeats(false);
	}

	@Override
	public final void mouseClicked(MouseEvent e) {
		checkClicks(e);
	}
	/** Функция для подсчёта количества кликов
	 * @param e новый клик
	 */
	private void checkClicks(MouseEvent e) {
		lastEevent = e;
		timer.restart();
	}
	/** Событие клика по экрану
	 * @param e событие клика. При множественных кликах вернётся толкьо последний, с правильным числом кликов
	 */
	public void mouseSeveralClick(MouseEvent e){}
}