/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Utils.MyMessageFormat;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EventListener;

/**
 *Универсальный набор:
 * Подпись, ползунок, ресет и кнопка ввода числового значения
 * @author Kerravitarr
 */
public class SettingsNumber extends javax.swing.JPanel {
	/**Интерфейс, который срабатывает при обновлении значения*/
	public interface AdjustmentListener extends EventListener {
		public void adjustmentValueChanged(int nVal);
	}
	
	/**
	* Создаёт панельку настройки
	* @param nameO имя параметра (по нему берутся навазния)
	* @param minS минимальное значение слайдера
	* @param defVal значение по умолчанию
	* @param maxS максимальное значение слайдера
	* @param nowVal текущее значение
	* @param list слушатель, который сработает, когда значение изменится
	*/
   public SettingsNumber(String nameO, int minS, int defVal, int maxS, int nowVal, AdjustmentListener list) {
		this(minS, maxS);
		if (minS > maxS)
			throw new NumberFormatException("Значение минимума не может быть больше максимума");
		listener = e -> {};
		
		label.setText(Configurations.getHProperty(Settings.class, nameO + ".L"));
		label.setToolTipText(Configurations.getHProperty(Settings.class, nameO + ".T"));
        spinner.setModel(new javax.swing.SpinnerNumberModel(defVal, minS, maxS, 1));
		
		Configurations.setIcon(reset, "reset");
		reset.setPreferredSize(BUT_SIZE);
		reset.addActionListener(e -> setValue(defVal));
		reset.setToolTipText(Configurations.getHProperty(SettingsNumber.class, "resetSlider"));

		value = nowVal == 0 ? 1 : 0;
		setValue(nowVal);
		
		listener = list;
		spinner.addChangeListener((e) -> setValue(((Number)spinner.getValue()).intValue()));
	}
	/** Creates new form Slider */
	private SettingsNumber(Integer mi, Integer ma) {
		initComponents();
		final var jtf = ((javax.swing.JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
		jtf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String text = jtf.getText().replace(",", "");
				int oldCaretPos = jtf.getCaretPosition();
				try {
					Integer newValue = Integer.valueOf(text);
					spinner.setValue(newValue);
					jtf.setCaretPosition(oldCaretPos);
				} catch (NumberFormatException ex) {
					//Not a number in text field -> do nothing
				} catch (java.lang.IllegalArgumentException ex) { //Удалили цифру, каретка уехала
					jtf.setCaretPosition(oldCaretPos - 1);
				}
			}
		});
		min = mi;
		max = ma;
	}
	/**Сохранить значение слайдера
	 * @param val 
	 */
	public void setValue(int val) {
		if (val != value && (min == null || val >= min) && (max == null || val <= max)) {
			final var m = (javax.swing.SpinnerNumberModel) spinner.getModel();
			final var lmax = ((Number) m.getMaximum()).doubleValue();
			final var lmin = ((Number) m.getMinimum()).doubleValue();
			spinner.setToolTipText(valueFormat.format((val - lmin) / (lmax - lmin)));
			value = val;
			spinner.setValue(val);
			listener.adjustmentValueChanged(val);
		}
	}
	/**Получить текущее значение слайдера
	 * @return 
	 */
	public int getValue() {
		return value;
	}
	@Override
	public void setEnabled(boolean isEnabled){
		super.setEnabled(isEnabled);
		spinner.setEnabled(isEnabled);
		reset.setVisible(isEnabled);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();
        resetAndInsert = new javax.swing.JPanel();
        reset = new javax.swing.JButton();
        spinner = new javax.swing.JSpinner();

        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("name");
        add(label, java.awt.BorderLayout.WEST);

        resetAndInsert.setLayout(new java.awt.BorderLayout());

        reset.setText("reset");
        resetAndInsert.add(reset, java.awt.BorderLayout.WEST);

        add(resetAndInsert, java.awt.BorderLayout.EAST);

        spinner.setModel(new javax.swing.SpinnerNumberModel(100, 100, null, 1));
        add(spinner, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JButton reset;
    private javax.swing.JPanel resetAndInsert;
    private javax.swing.JSpinner spinner;
    // End of variables declaration//GEN-END:variables
	
	/**Размер кнопок*/
	private static final Dimension BUT_SIZE = new Dimension(20,15);
	/**Реальное значение*/
	private int value;
	/**Минимальное значение*/
	private final Integer min;
	/**Максимальное значение*/
	private final Integer max;
	/**Слушатель события, что значение в ячейке изменилось*/
	private AdjustmentListener listener;
	/**Форматирование высплывающего окна над значением*/
	private final MyMessageFormat valueFormat = new MyMessageFormat(Configurations.getProperty(Settings.class,"scrollTtext"));
}
