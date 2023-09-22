/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Calculations.Point;
import Utils.MyMessageFormat;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import javax.swing.WindowConstants;

/**
 *Универсальный набор:
 * Подпись, ползунок, ресет и две ячейки для ввода точки на экране
 * @author Kerravitarr
 */
public class SettingsPoint extends javax.swing.JPanel {
	/**Интерфейс, который срабатывает при обновлении значения*/
	public interface AdjustmentListener extends EventListener {
		public void adjustmentValueChanged(Point.Vector nVal);
	}
	
	/**
	* Создаёт панельку настройки для ввода двух чисел
	* @param nameO имя параметра (по нему берутся навазния)
	 * @param minX минимальное значение параметра Х
	 * @param defX значение параметра Х по умолчанию
	 * @param maxX максимамльное значение параметра Х
	 * @param nowX текущее значение параметра Х
	 * @param minY минимальное значение параметра Y
	 * @param defY значение параметра Y по умолчанию
	 * @param maxY максимамльное значение параметра Y
	 * @param nowY текущее значение параметра Y
	* @param list слушатель, который сработает, когда значение изменится
	*/
   public SettingsPoint(String nameO, int minX, int defX, int maxX, int nowX, int minY, int defY, int maxY, int nowY, AdjustmentListener list) {
		this();
		listener = e -> {};
		
		label.setText(Configurations.getHProperty(Settings.class, nameO + ".L"));
		labelX.setText(Configurations.getHProperty(Settings.class, nameO + ".X"));
		labelY.setText(Configurations.getHProperty(Settings.class, nameO + ".Y"));
		label.setToolTipText(Configurations.getHProperty(Settings.class, nameO + ".T"));
        spinnerX.setModel(new javax.swing.SpinnerNumberModel(nowX, minX, maxX, 1));
        spinnerY.setModel(new javax.swing.SpinnerNumberModel(nowY, minY, maxY, 1));
		
		Configurations.setIcon(reset, "reset");
		reset.addActionListener(e -> setValue(Point.Vector.create(defX,defY)));
		reset.setToolTipText(Configurations.getHProperty(SettingsPoint.class, "resetSlider"));
		
		Configurations.setIcon(select, "selectPoint");
		select.addActionListener(e -> {
			final var dialog = new javax.swing.JDialog((Frame)null, "", false);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			final var panel = new javax.swing.JPanel();
			final var pointLabel = new javax.swing.JLabel(Configurations.getHProperty(SettingsPoint.class, "emptySelectLabel"));
			final var world = Configurations.getViewer().get(WorldView.class);
			final var transform = world.getTransform();
			panel.add(pointLabel);
			dialog.add(panel);
			dialog.setBounds(SettingsPoint.this.getLocationOnScreen().x, SettingsPoint.this.getLocationOnScreen().y, 200, 50);
			final var clickListener = new java.awt.event.MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON1){
						setValue(transform.toWorldPoint(e));
					}
					dialog.dispose();
				}
			};
			final var movedListener = new java.awt.event.MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					final var point = transform.toWorldPoint(e);
					pointLabel.setText(Configurations.getHProperty(SettingsPoint.class, "selectLabel",point.getX(),point.getY()));
				}
			};
			
			world.addMouseListener(clickListener);
			world.addMouseMotionListener(movedListener);
			dialog.addComponentListener(new java.awt.event.ComponentAdapter() {
				@Override
				public void componentHidden(ComponentEvent e) {
					world.removeMouseMotionListener(movedListener);
					world.removeMouseListener(clickListener);
				}
			});
			dialog.setVisible(true);
		});
		select.setToolTipText(Configurations.getHProperty(SettingsPoint.class, "selectButton"));

		value = null;
		setValue(Point.Vector.create(nowX,nowY));
		
		listener = list;
		spinnerX.addChangeListener((e) -> setValue(Point.Vector.create(((Number)spinnerX.getValue()).intValue(),((Number)spinnerY.getValue()).intValue())));
		spinnerY.addChangeListener((e) -> setValue(Point.Vector.create(((Number)spinnerX.getValue()).intValue(),((Number)spinnerY.getValue()).intValue())));
	}
	/** Creates new form Slider */
	private SettingsPoint() {
		initComponents();
		for (int i = 0; i < 2; i++) {
			final var spinner = i == 0 ? spinnerX : spinnerY;
			
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
		}
	}
	
	/**Сохранить значение слайдера
	 * @param val 
	 */
	public void setValue(Point val) {
		setValue(Point.Vector.create(val.getX(), val.getY()));
	}
	/**Сохранить значение слайдера
	 * @param val 
	 */
	public void setValue(Point.Vector val) {
		if (!val.equals(value)) {
			value = val;
			spinnerX.setValue(val.x);
			spinnerY.setValue(val.y);
			listener.adjustmentValueChanged(val);
		}
	}
	/**Получить текущее значение слайдера
	 * @return 
	 */
	public Point.Vector getValue() {
		return value;
	}
	@Override
	public void setEnabled(boolean isEnabled){
		super.setEnabled(isEnabled);
		spinnerX.setEnabled(isEnabled);
		spinnerY.setEnabled(isEnabled);
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
        labelX = new javax.swing.JLabel();
        spinnerX = new javax.swing.JSpinner();
        labelY = new javax.swing.JLabel();
        spinnerY = new javax.swing.JSpinner();
        reset = new javax.swing.JButton();
        select = new javax.swing.JButton();

        setAlignmentX(0.0F);
        setAlignmentY(0.0F);
        setMaximumSize(new java.awt.Dimension(2147483647, 40));
        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("name");
        label.setAlignmentY(0.0F);
        add(label, java.awt.BorderLayout.NORTH);

        resetAndInsert.setLayout(new javax.swing.BoxLayout(resetAndInsert, javax.swing.BoxLayout.LINE_AXIS));

        labelX.setText("X:");
        labelX.setAlignmentY(0.0F);
        labelX.setPreferredSize(new java.awt.Dimension(15, 20));
        resetAndInsert.add(labelX);

        spinnerX.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spinnerX.setAlignmentY(0.0F);
        spinnerX.setMaximumSize(new java.awt.Dimension(32767, 20));
        spinnerX.setMinimumSize(new java.awt.Dimension(64, 20));
        spinnerX.setPreferredSize(new java.awt.Dimension(64, 20));
        resetAndInsert.add(spinnerX);

        labelY.setText("Y:");
        labelY.setAlignmentY(0.0F);
        labelY.setPreferredSize(new java.awt.Dimension(15, 20));
        resetAndInsert.add(labelY);

        spinnerY.setModel(new javax.swing.SpinnerNumberModel(100, 0, null, 1));
        spinnerY.setAlignmentX(0.0F);
        spinnerY.setAlignmentY(0.0F);
        spinnerY.setMaximumSize(new java.awt.Dimension(32767, 20));
        spinnerY.setMinimumSize(new java.awt.Dimension(68, 20));
        spinnerY.setPreferredSize(new java.awt.Dimension(68, 20));
        resetAndInsert.add(spinnerY);

        reset.setText("reset");
        reset.setAlignmentY(0.0F);
        resetAndInsert.add(reset);

        select.setText("jButton1");
        select.setAlignmentY(0.0F);
        resetAndInsert.add(select);

        add(resetAndInsert, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JLabel labelX;
    private javax.swing.JLabel labelY;
    private javax.swing.JButton reset;
    private javax.swing.JPanel resetAndInsert;
    private javax.swing.JButton select;
    private javax.swing.JSpinner spinnerX;
    private javax.swing.JSpinner spinnerY;
    // End of variables declaration//GEN-END:variables
	
	/**Реальное значение*/
	private Point.Vector value;
	/**Слушатель события, что значение в ячейке изменилось*/
	private AdjustmentListener listener;
	/**Форматирование высплывающего окна над значением*/
	private final MyMessageFormat valueFormat = new MyMessageFormat(Configurations.getProperty(Settings.class,"scrollTtext"));
}
