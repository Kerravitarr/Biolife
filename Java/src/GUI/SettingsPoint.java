/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Calculations.Point;
import Utils.MyMessageFormat;
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
	/**Интерфейс, который срабатывает при обновлении значении
	 * @param <P> тип слайдера может быть или Point или Point.Vector
	 */
	public interface AdjustmentListener<P> extends EventListener {
		public void adjustmentValueChanged(P nVal);
	}
	
	/**
	* Создаёт панельку настройки для ввода точки на карте
	 * @param nameCl класс для поиска локализованного имени
	* @param nameS имя параметра (по нему берутся навазния)
	 * @param def значение параметра Х по умолчанию
	 * @param nowValue текущее значение параметра Y
	* @param list слушатель, который сработает, когда значение изменится
	*/
   public SettingsPoint(Class<?> nameCl, String nameS, Point def, Point nowValue, AdjustmentListener<Point> list) {
		this(nameCl,nameS,Configurations.getHProperty(SettingsPoint.class, "X"),Configurations.getHProperty(SettingsPoint.class, "Y"),null,def.getX(),null,nowValue.getX(),null,def.getY(),null,nowValue.getY(), list, true);
   }
	/**
	* Создаёт панельку настройки для ввода двух чисел
	 * @param nameCl класс для поиска локализованного имени
	* @param nameS имя параметра (по нему берутся навазния)
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
   public SettingsPoint(Class<?> nameCl, String nameS, int minX, int defX, int maxX, int nowX, int minY, int defY, int maxY, int nowY, AdjustmentListener<Point.Vector> list) {
		this(nameCl,nameS,Configurations.getHProperty(nameCl, nameS + ".P1"),Configurations.getHProperty(nameCl, nameS + ".P2"), minX, defX, maxX, nowX, minY, defY, maxY, nowY, list, false);
	}
   
   
	private SettingsPoint(Class<?> nameCl, String nameS, String xLabel, String yLabel, Integer minX, Integer defX, Integer maxX, Integer nowX, Integer minY, Integer defY, Integer maxY, Integer nowY, AdjustmentListener<?> list, boolean isP) {
		initComponents();
		listener = e -> {};
		
		label.setText(Configurations.getHProperty(nameCl, nameS + ".L"));
		label.setToolTipText(Configurations.getHProperty(nameCl, nameS + ".T"));
		labelX.setText(xLabel);
		labelY.setText(yLabel);
		
        spinnerX.setModel(new javax.swing.SpinnerNumberModel(nowX, minX, maxX, Integer.valueOf(1)));
        spinnerY.setModel(new javax.swing.SpinnerNumberModel(nowY, minY, maxY, Integer.valueOf(1)));
		
		Configurations.setIcon(reset, "reset");
		reset.addActionListener(e -> setValue(Point.Vector.create(defX,defY)));
		reset.setToolTipText(Configurations.getHProperty(SettingsPoint.class, "resetSlider"));
		Configurations.setIcon(select, "selectPoint");
		select.addActionListener(e -> {
			final var dialog = new javax.swing.JDialog((Frame)null, "", false);
			dialog.setAlwaysOnTop(true);
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
				public void mousePressed(MouseEvent e) {
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
			dialog.addWindowFocusListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowLostFocus(java.awt.event.WindowEvent arg0) {
					dialog.setVisible(false);
					world.removeMouseMotionListener(movedListener);
					world.removeMouseListener(clickListener);
				}
			});
			dialog.setVisible(true);
		});
		select.setToolTipText(Configurations.getHProperty(SettingsPoint.class, "selectButton"));

		value = null;
		setValue(Point.Vector.create(nowX,nowY));
		
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
		
		listener = list;
		isPoint = isP;
		spinnerX.addChangeListener((e) -> setValue(Point.Vector.create(((Number)spinnerX.getValue()).intValue(),((Number)spinnerY.getValue()).intValue())));
		spinnerY.addChangeListener((e) -> setValue(Point.Vector.create(((Number)spinnerX.getValue()).intValue(),((Number)spinnerY.getValue()).intValue())));
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
			if (isPoint) {
				final var p = Point.create(val.x, val.y);
				spinnerX.setValue(p.getX());
				spinnerY.setValue(p.getY());
				listener.adjustmentValueChanged(p);
			} else {
				spinnerX.setValue(val.x);
				spinnerY.setValue(val.y);
				listener.adjustmentValueChanged(val);
			}
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

        setMaximumSize(new java.awt.Dimension(2147483647, 40));
        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("name");
        label.setAlignmentY(0.0F);
        add(label, java.awt.BorderLayout.NORTH);

        labelX.setText("X:");
        labelX.setAlignmentY(0.0F);
        labelX.setPreferredSize(new java.awt.Dimension(15, 20));

        spinnerX.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spinnerX.setAlignmentX(0.0F);
        spinnerX.setAlignmentY(0.0F);
        spinnerX.setMaximumSize(new java.awt.Dimension(32767, 20));
        spinnerX.setMinimumSize(new java.awt.Dimension(64, 20));
        spinnerX.setPreferredSize(new java.awt.Dimension(64, 20));

        labelY.setText("Y:");
        labelY.setAlignmentY(0.0F);
        labelY.setPreferredSize(new java.awt.Dimension(15, 20));

        spinnerY.setModel(new javax.swing.SpinnerNumberModel(100, 0, null, 1));
        spinnerY.setAlignmentX(0.0F);
        spinnerY.setAlignmentY(0.0F);
        spinnerY.setMaximumSize(new java.awt.Dimension(32767, 20));
        spinnerY.setMinimumSize(new java.awt.Dimension(68, 20));
        spinnerY.setPreferredSize(new java.awt.Dimension(68, 20));

        reset.setText("reset");
        reset.setAlignmentY(0.0F);

        select.setText("jButton1");
        select.setAlignmentY(0.0F);

        javax.swing.GroupLayout resetAndInsertLayout = new javax.swing.GroupLayout(resetAndInsert);
        resetAndInsert.setLayout(resetAndInsertLayout);
        resetAndInsertLayout.setHorizontalGroup(
            resetAndInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resetAndInsertLayout.createSequentialGroup()
                .addComponent(labelX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(spinnerX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(labelY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(spinnerY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(reset)
                .addGap(0, 0, 0)
                .addComponent(select))
        );
        resetAndInsertLayout.setVerticalGroup(
            resetAndInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelX, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(spinnerX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(labelY, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(spinnerY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(reset, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(select, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

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
	/**У нас точка на карте или абстрактная точка в любом месте? Влияет на то, как работает счётчик (может идит по кругу*/
	private final boolean isPoint;
	/**Слушатель события, что значение в ячейке изменилось*/
	private AdjustmentListener listener;
}
