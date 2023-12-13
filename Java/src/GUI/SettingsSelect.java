/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.EventListener;

/**
 *Универсальный набор:
 * Подпись, ползунок, ресет и кнопка ввода числового значения
 * @author Kerravitarr
 */
public class SettingsSelect<T> extends javax.swing.JPanel {
	/**Интерфейс, который срабатывает при обновлении значения*/
	public interface AdjustmentListener<T> extends EventListener {
		public void adjustmentValueChanged(T nVal);
	}
	
	/**
	* Создаёт панельку настройки
	 * @param nameCl класс параметра, тоже для формирования локализованных подписей
	* @param nameS имя параметра (по нему берутся навазния)
	* @param values все доступные значения
	* @param defVal значение по умолчанию
	* @param nowVal текущее значение
	* @param list слушатель, который сработает, когда значение изменится
	*/
   public SettingsSelect(Class<?> nameCl, String nameS, T[] values, T defVal, T nowVal, AdjustmentListener<T> list) {
		initComponents();
		if (!Arrays.stream(values).anyMatch( v -> v == defVal || v != null && v.equals(defVal)))
			throw new NumberFormatException("Значение defVal обязано принадлежать массиву values!");
		if (!Arrays.stream(values).anyMatch( v -> v == nowVal || v != null &&v.equals(nowVal)))
			throw new NumberFormatException("Значение nowVal обязано принадлежать массиву values!");
		if (Arrays.stream(values).filter(v -> v == null).count() > 1)
			throw new NumberFormatException("Массив values может содержать не более одного null!");
		listener = e -> {};

		label.setText(Configurations.getHProperty(nameCl, nameS + ".L"));
		label.setToolTipText(Configurations.getHProperty(nameCl, nameS + ".T"));
		
		this.values = new javax.swing.DefaultComboBoxModel(values);
		select.setModel(this.values);
		
		Configurations.setIcon(reset, "reset");
		reset.setPreferredSize(BUT_SIZE);
		reset.addActionListener(e -> setValue(defVal));
		reset.setToolTipText(Configurations.getHProperty(SettingsSelect.class, "resetSlider"));

		value = nowVal == null ? Arrays.stream(values).filter( v -> v != null).findFirst().orElse(null) : null;
		setValue(nowVal);
		listener = list;
		select.addActionListener((e) -> setValue((T)select.getSelectedItem()));
	}
	/**Сохранить значение слайдера
	 * @param val 
	 */
	public void setValue(T val) {
		if ((val == null && val != value) || (val != null && !val.equals(value))) {
			for (int i = 0; i < values.getSize(); i++) {
				final var get = values.getElementAt(i);
				if((get == null && get == val) || (get != null && get.equals(val))){
					value = val;
					select.setSelectedIndex(i);
					listener.adjustmentValueChanged(val);
					break;
				}
			}
		}
	}
	/**Получить текущее значение слайдера
	 * @return 
	 */
	public T getValue() {
		return value;
	}
	@Override
	public void setEnabled(boolean isEnabled){
		super.setEnabled(isEnabled);
		select.setEnabled(isEnabled);
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
        select = new javax.swing.JComboBox<>();

        setMaximumSize(new java.awt.Dimension(2147483647, 40));
        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("name");
        add(label, java.awt.BorderLayout.NORTH);

        resetAndInsert.setLayout(new java.awt.BorderLayout());

        reset.setText("reset");
        resetAndInsert.add(reset, java.awt.BorderLayout.WEST);

        add(resetAndInsert, java.awt.BorderLayout.EAST);

        select.setFocusable(false);
        select.setMaximumSize(new java.awt.Dimension(32767, 20));
        select.setMinimumSize(new java.awt.Dimension(72, 20));
        select.setPreferredSize(new java.awt.Dimension(72, 20));
        add(select, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JButton reset;
    private javax.swing.JPanel resetAndInsert;
    private javax.swing.JComboBox<T> select;
    // End of variables declaration//GEN-END:variables
	
	/**Размер кнопок*/
	private static final Dimension BUT_SIZE = new Dimension(20,15);
	/**Реальное текущее значение*/
	private T value;
	/**Все доступные значения */
	private javax.swing.DefaultComboBoxModel<T> values;
	/**Слушатель события, что значение в ячейке изменилось*/
	private AdjustmentListener listener;
}
