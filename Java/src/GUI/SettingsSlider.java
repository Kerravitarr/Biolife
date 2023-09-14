/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Utils.MyMessageFormat;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.EventListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *Универсальный набор:
 * Подпись, ползунок, ресет и кнопка ввода числового значения
 * @author Kerravitarr
 */
public class SettingsSlider extends javax.swing.JPanel {
	/**Класс для ввода в поле только чисел в интервале*/
	private class RangeFilter extends PlainDocument {			
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			try {
				StringBuilder newString = new StringBuilder();
				newString.append(this.getText(0, offs));
				newString.append(str);
				newString.append(this.getText(offs, this.getLength() - offs));
				int value = Integer.parseInt(newString.toString());					
				if (min != null && value < min) {
					this.remove(0, this.getLength());
					super.insertString(0, min.toString(), a);
					java.awt.Toolkit.getDefaultToolkit().beep();
				} else if (max != null && value > max) {
					this.remove(0, this.getLength());
					super.insertString(0, max.toString(), a);
					java.awt.Toolkit.getDefaultToolkit().beep();
				} else {
					super.insertString(offs, str, a);
				}
			} catch (Exception e) {
				java.awt.Toolkit.getDefaultToolkit().beep();
				return;
			}
		}
	}
	/**Интерфейс, который срабатывает при обновлении значения*/
	public interface AdjustmentListener extends EventListener {
		public void adjustmentValueChanged(int nVal);
	}
	
	/**
	* Создаёт панельку настройки
	* @param nameO полное имя параметра (по нему берутся навазния)
	* @param minS минимальное значение слайдера
	* @param maxS максимальное значение слайдера
	* @param mi манимально возможное значение
	* @param nowVal текущее значение
	* @param ma максимально возможное значение
	* @param list слушатель, который сработает, когда значение изменится
	*/
   public SettingsSlider(String nameO, int minS, int maxS, Integer mi, int nowVal, Integer ma, AdjustmentListener list) {
		this(mi, ma);
		if (minS > maxS)
			throw new NumberFormatException("Значение минимума не может быть больше максимума");
		label.setText(Configurations.getHProperty(nameO + ".L"));
		label.setToolTipText(Configurations.getHProperty(nameO + ".T"));
		scroll.setMinimum(minS);
		scroll.setMaximum(maxS);
		reset.setVisible(false);

		Configurations.setIcon(insert, "insert");
		insert.setPreferredSize(BUT_SIZE);
		insert.setToolTipText(Configurations.getHProperty(SettingsSlider.class, "insertSlider"));
		insert.addActionListener( e -> showConfirmDialog());

		value = nowVal == 0 ? 1 : 0;
		setValue(nowVal);
		listener = list;
		scroll.addAdjustmentListener(e -> setValue(e.getValue()));
   }
	/**
	* Создаёт панельку настройки
	* @param nameO имя параметра (по нему берутся навазния)
	* @param minS минимальное значение слайдера
	* @param defVal значение по умолчанию
	* @param maxS максимальное значение слайдера
	* @param mi манимально возможное значение
	* @param nowVal текущее значение
	* @param ma максимально возможное значение
	* @param list слушатель, который сработает, когда значение изменится
	*/
   public SettingsSlider(String nameO, int minS, int defVal, int maxS, Integer mi, int nowVal, Integer ma, AdjustmentListener list) {
		this(mi, ma);
		if (minS > maxS)
			throw new NumberFormatException("Значение минимума не может быть больше максимума");

		label.setText(Configurations.getHProperty(Settings.class, nameO + ".L"));
		label.setToolTipText(Configurations.getHProperty(Settings.class, nameO + ".T"));
		scroll.setMinimum(minS);
		scroll.setMaximum(maxS);
		Configurations.setIcon(reset, "reset");
		reset.setPreferredSize(BUT_SIZE);
		reset.addActionListener(e -> setValue(defVal));
		reset.setToolTipText(Configurations.getHProperty(SettingsSlider.class, "resetSlider"));

		Configurations.setIcon(insert, "insert");
		insert.setPreferredSize(BUT_SIZE);
		insert.setToolTipText(Configurations.getHProperty(SettingsSlider.class, "insertSlider"));
		insert.addActionListener( e -> showConfirmDialog());

		value = nowVal == 0 ? 1 : 0;
		setValue(nowVal);
		listener = list;
		scroll.addAdjustmentListener(e -> setValue(e.getValue()));
	}
	/** Creates new form Slider */
	private SettingsSlider(Integer mi, Integer ma) {
		initComponents();
		
		listener = e -> {};
		min = mi;
		max = ma;
	}
	/**Сохранить значение слайдера
	 * @param val 
	 */
	public void setValue(int val) {
		if (val != value && (min == null || val >= min) && (max == null || val <= max)) {
			scroll.setToolTipText(valueFormat.format(((double) val - scroll.getMinimum()) / (scroll.getMaximum() - scroll.getMinimum())));
			value = val;
			scroll.setValue(val);
			listener.adjustmentValueChanged(val);
		}
	}
	/**Получить текущее значение слайдера
	 * @return 
	 */
	public int getValue() {
		return value;
	}
	/**Отобразить диалог ввода параметров */
	private void showConfirmDialog() {
		JPanel insertValue = new JPanel();
		insertValue.setLayout(new BorderLayout(0, 0));

		String labelInsertText = Configurations.getHProperty(SettingsSlider.class, "insertLabel");
		if (min == null && max == null)
			labelInsertText += " N∈R";
		else if (min != null && max == null)
			labelInsertText += " N≥" + min.toString();
		else if (min == null && max != null)
			labelInsertText += " N≤" + max.toString();
		else
			labelInsertText += " N∈[" + min.toString() + "," + max.toString() + "]";
		var tlabel = new JLabel(labelInsertText);
		insertValue.add(tlabel, BorderLayout.NORTH);
		var tText = new JTextField(new RangeFilter(), Integer.toString(value), 10);
		insertValue.add(tText, BorderLayout.CENTER);

		var resetToLast = Configurations.makeIconButton("reset");
		resetToLast.setPreferredSize(BUT_SIZE);
		resetToLast.addActionListener(e -> tText.setText(Integer.toString(value)));
		resetToLast.setToolTipText(Configurations.getHProperty(SettingsSlider.class, "resetSlider"));
		insertValue.add(resetToLast, BorderLayout.EAST);

		var ret = JOptionPane.showConfirmDialog(this, insertValue, Configurations.getProperty(SettingsSlider.class, "insertSlider"), JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {
			setValue(Integer.parseInt(tText.getText()));
		}
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
        scroll = new javax.swing.JScrollBar();
        resetAndInsert = new javax.swing.JPanel();
        reset = new javax.swing.JButton();
        insert = new javax.swing.JButton();

        setAlignmentX(0.0F);
        setAlignmentY(0.0F);
        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("name");
        add(label, java.awt.BorderLayout.NORTH);

        scroll.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scroll.setValue(50);
        scroll.setVisibleAmount(0);
        add(scroll, java.awt.BorderLayout.CENTER);

        resetAndInsert.setLayout(new java.awt.BorderLayout());

        reset.setText("reset");
        resetAndInsert.add(reset, java.awt.BorderLayout.WEST);

        insert.setText("insert");
        resetAndInsert.add(insert, java.awt.BorderLayout.EAST);

        add(resetAndInsert, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton insert;
    private javax.swing.JLabel label;
    private javax.swing.JButton reset;
    private javax.swing.JPanel resetAndInsert;
    private javax.swing.JScrollBar scroll;
    // End of variables declaration//GEN-END:variables
	
	/**Размер кнопок*/
	private static final Dimension BUT_SIZE = new Dimension(20,15);
	/**Реальное значение*/
	private int value;
	/**Минимальное значение*/
	private final Integer min;
	/**Максимальное значение*/
	private final Integer max;
	/**Максимальное значение*/
	private AdjustmentListener listener;
	/**Форматирование высплывающего окна над значением*/
	private final MyMessageFormat valueFormat = new MyMessageFormat(Configurations.getProperty(Settings.class,"scrollTtext"));
}
