/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Utils.MyMessageFormat;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.EventListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *Универсальный набор:
 * Подпись, ползунок, ресет и кнопка ввода числового значения
 * @author Kerravitarr
 */
public class SettingsSlider<NumberT extends Number & Comparable<NumberT>> extends javax.swing.JPanel {
	/**Все классы, которые описывают целочисленные значения*/
	@SuppressWarnings("unchecked")
	private static final Class[] integerClasses = new Class[] { Byte.class, Short.class, Integer.class, Long.class};
	
	
	private enum numType{LONG, DOUBLE};
	
	/**Класс для ввода в поле только чисел в интервале*/
	private class RangeFilter extends PlainDocument {			
		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			try {
				StringBuilder newString = new StringBuilder();
				newString.append(this.getText(0, offs));
				newString.append(str);
				newString.append(this.getText(offs, this.getLength() - offs));
				boolean isMin = false;
				boolean isMax = false;
				switch (typeValue) {
					case DOUBLE -> {
						final var value = Double.valueOf(newString.toString());
						isMin = min != null && compare(transform(value), min) == -1;
						isMax = max != null && compare(transform(value) , max) == 1;
					}
					case LONG -> {
						final var value = Long.valueOf(newString.toString());
						isMin = min != null && compare(transform(value), min) == -1;
						isMax = max != null && compare(transform(value) , max) == 1;
					}
				}
									
				if (isMin) {
					this.remove(0, this.getLength());
					super.insertString(0, min.toString(), a);
					java.awt.Toolkit.getDefaultToolkit().beep();
				} else if (isMax) {
					this.remove(0, this.getLength());
					super.insertString(0, max.toString(), a);
					java.awt.Toolkit.getDefaultToolkit().beep();
				} else {
					super.insertString(offs, str, a);
				}
			} catch (NumberFormatException | BadLocationException e) {
				java.awt.Toolkit.getDefaultToolkit().beep();
			}
		}
	}
	
	/**Интерфейс, который срабатывает при обновлении значения*/
	public interface AdjustmentListener<NumberT extends Number> extends EventListener {
		public void adjustmentValueChanged(NumberT nVal);
	}
	
	/**
	* Создаёт панельку настройки
	 * @param nameCl класс для поиска локализованного имени
	* @param nameS имя параметра (по нему берутся навазния)
	* @param minS минимальное значение слайдера
	* @param defVal значение по умолчанию
	* @param maxS максимальное значение слайдера
	* @param mi манимально возможное значение
	* @param nowVal текущее значение
	* @param ma максимально возможное значение
	* @param list слушатель, который сработает, когда значение изменится
	*/
   public SettingsSlider(Class<?> nameCl, String nameS, NumberT minS, NumberT defVal, NumberT maxS, NumberT mi, NumberT nowVal, NumberT ma, AdjustmentListener<NumberT> list) {
		initComponents();
		listener = e -> {};
		min = mi;
		max = ma;
		final var nclr = nowVal.getClass();
		if(Arrays.stream(integerClasses).filter( a->nclr.equals(a)).findFirst().orElse(null) != null)
			typeValue = numType.LONG;
		else
			typeValue = numType.DOUBLE;
		if (compare(minS,maxS) == 1)
			throw new NumberFormatException("Значение минимума не может быть больше максимума");

		label.setText(Configurations.getHProperty(nameCl, nameS + ".L"));
		label.setToolTipText(Configurations.getHProperty(nameCl, nameS + ".T"));
		minMaxSliderDelta = (minS != null && maxS != null) ? (maxS.doubleValue() - minS.doubleValue()) : 0d;
		minSlider = minS;
		if(minS == null || maxS == null){
			valueLabel.setText(nowVal.toString());
			scroll.setVisible(false);
		} else if(typeValue == numType.LONG){
			if(maxS.longValue() - minS.longValue() > 1){
				scroll.setMinimum(minS.intValue());
				scroll.setMaximum(maxS.intValue());
				valueLabel.setVisible(false);
			} else {
				valueLabel.setText(nowVal.toString());
				scroll.setVisible(false);
			}
		} else {
			valueLabel.setVisible(false);
			if(minMaxSliderDelta > 100){
				scroll.setMinimum(minS.intValue());
				scroll.setMaximum(maxS.intValue());
			} else {
				scroll.setMinimum(0);
				scroll.setMaximum(100);
			}
		}
		Configurations.setIcon(reset, "reset");
		reset.setPreferredSize(BUT_SIZE);
		reset.addActionListener(e -> setValue(defVal));
		reset.setToolTipText(Configurations.getHProperty(SettingsSlider.class, "resetSlider"));

		Configurations.setIcon(insert, "insert");
		insert.setPreferredSize(BUT_SIZE);
		insert.setToolTipText(Configurations.getHProperty(SettingsSlider.class, "insertSlider"));
		insert.addActionListener( e -> showConfirmDialog());

		value = null;
		setValue(nowVal);
		listener = list;
		scroll.addAdjustmentListener(e -> {
			if(typeValue == numType.LONG){
				setValue(transform(e.getValue()));
			} else {
				if(minMaxSliderDelta > 100)
					setValue(transform(e.getValue()));
				else if(minS != null)
					setValue(transform(minS.doubleValue() + e.getValue() * minMaxSliderDelta / (scroll.getMaximum() - scroll.getMinimum())));
			}
		});
	}
	/**Сохранить значение слайдера
	 * @param val 
	 */
	public void setValue(NumberT val) {
		if (val != value && (min == null || compare(val, min) >= 0) && (max == null || compare(val, max) <= 0)) {
			scroll.setToolTipText(valueFormat.format(val));
			value = val;
			if (typeValue == numType.LONG) {
				scroll.setValue(val.intValue());
			} else {
				if(minMaxSliderDelta > 100)
					scroll.setValue(val.intValue());
				else
					scroll.setValue((int) ((val.doubleValue() - minSlider.doubleValue()) * (scroll.getMaximum() - scroll.getMinimum()) / minMaxSliderDelta));
			}
			listener.adjustmentValueChanged(val);
		}
	}
	/**Получить текущее значение слайдера
	 * @return 
	 */
	public NumberT getValue() {
		return value;
	}
	/**
	 * Сравнивает два числа.
	 * @param a
	 * @param b
	 * @return a==b -> 0;
	 *			a > b -> 1
	 *			a < b -> -1
	 */
	private int compare(NumberT a, NumberT b) {
		if (a == b) {
			return 0;
		} else if (b == null) {
			return 1;
		} else if (a == null) {
			return -1;
		} else {
			return a.compareTo(b);
		}
	}
	/**
	 * Преобразует val в нужный тип
	 * @param val
	 * @return 
	 */
	private NumberT transform(Number val){
		final var cls = value.getClass();
		switch (typeValue) {
			case DOUBLE->{
				if(cls.equals(Float.class))
					return (NumberT) Float.valueOf(val.floatValue());
				else if(cls.equals(Double.class))
					return (NumberT) Double.valueOf(val.doubleValue());
				else 
					throw new ClassCastException("Невозможно преобразовать " + val.getClass() + " к " + cls);
			}
			case LONG -> {
				if(cls.equals(Byte.class))
					return (NumberT) Byte.valueOf(val.byteValue());
				else if(cls.equals(Short.class))
					return (NumberT) Short.valueOf(val.shortValue());
				else if(cls.equals(Integer.class))
					return (NumberT) Integer.valueOf(val.intValue());
				else if(cls.equals(Long.class))
					return (NumberT) Long.valueOf(val.longValue());
				else 
					throw new ClassCastException("Невозможно преобразовать " + val.getClass() + " к " + cls);
			}
			default ->{
				throw new ClassCastException("Невозможно преобразовать " + val.getClass() + " к " + cls);
			}
		}
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
		var tText = new JTextField(new RangeFilter(), String.valueOf(value), 10);
		insertValue.add(tText, BorderLayout.CENTER);

		var resetToLast = Configurations.makeIconButton("reset");
		resetToLast.setPreferredSize(BUT_SIZE);
		resetToLast.addActionListener(e -> tText.setText(String.valueOf(value)));
		resetToLast.setToolTipText(Configurations.getHProperty(SettingsSlider.class, "resetSlider"));
		insertValue.add(resetToLast, BorderLayout.EAST);

		var ret = JOptionPane.showConfirmDialog(this, insertValue, Configurations.getProperty(SettingsSlider.class, "insertSlider"), JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {
			switch (typeValue) {
				case DOUBLE -> setValue(transform(Double.valueOf(tText.getText())));
				case LONG -> setValue(transform(Long.valueOf(tText.getText())));
			}
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
        resetAndInsert = new javax.swing.JPanel();
        reset = new javax.swing.JButton();
        insert = new javax.swing.JButton();
        centralPanel = new javax.swing.JPanel();
        valueLabel = new javax.swing.JLabel();
        scroll = new javax.swing.JScrollBar();

        setMaximumSize(new java.awt.Dimension(2147483647, 40));
        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("name");
        add(label, java.awt.BorderLayout.NORTH);

        resetAndInsert.setLayout(new javax.swing.BoxLayout(resetAndInsert, javax.swing.BoxLayout.LINE_AXIS));

        reset.setText("reset");
        resetAndInsert.add(reset);

        insert.setText("insert");
        insert.setAlignmentX(0.5F);
        resetAndInsert.add(insert);

        add(resetAndInsert, java.awt.BorderLayout.EAST);

        centralPanel.setLayout(new javax.swing.BoxLayout(centralPanel, javax.swing.BoxLayout.LINE_AXIS));

        valueLabel.setText("jLabel1");
        centralPanel.add(valueLabel);

        scroll.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scroll.setValue(50);
        scroll.setVisibleAmount(0);
        scroll.setAlignmentX(0.0F);
        centralPanel.add(scroll);

        add(centralPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centralPanel;
    private javax.swing.JButton insert;
    private javax.swing.JLabel label;
    private javax.swing.JButton reset;
    private javax.swing.JPanel resetAndInsert;
    private javax.swing.JScrollBar scroll;
    private javax.swing.JLabel valueLabel;
    // End of variables declaration//GEN-END:variables
	
	/**Размер кнопок*/
	private static final Dimension BUT_SIZE = new Dimension(20,15);
	/**Реальное значение*/
	private NumberT value;
	/**Тип числа, которое используется в текущем слайдере*/
	private final numType typeValue;
	/**Минимальное значение*/
	private final NumberT min;
	/**Минимальное значение для слайдера*/
	private final NumberT minSlider;
	/**Максимальное значение*/
	private final NumberT max;
	/**Разница между минимальный и максимальный значением слайдера*/
	private final double minMaxSliderDelta;
	/**Максимальное значение*/
	private AdjustmentListener<NumberT> listener;
	/**Форматирование высплывающего окна над значением*/
	private final MyMessageFormat valueFormat = new MyMessageFormat(Configurations.getProperty(Settings.class,"scrollTtext"));
}
