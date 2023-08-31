package GUI;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import Utils.JSON;
import Utils.JsonSave;
import Utils.MyMessageFormat;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import javax.swing.JScrollPane;
import main.Configurations;
import main.World;
import start.BioLife;

public class Settings extends JPanel{
	
	private static class Slider extends JPanel{
		/**Описание ползунка*/
		private final JLabel label;
		/**Ползунок*/
		private final JScrollBar scroll;
		/**Сброс по умолчанию*/
		private final JButton reset;
		/**Ввести значение*/
		private final JButton insert;
		/**Размер кнопок*/
		private static final Dimension BUT_SIZE = new Dimension(20,15);
		/**Реальное значение*/
		private int value;
		/**Минимальное значение*/
		private final Integer min;
		/**Максимальное значение*/
		private final Integer max;
		/**Максимальное значение*/
		private final AdjustmentListener listener;
		/**Специальный флаг, чтобы можно было сохранять значения сильно за гранью допустимых*/
		private boolean isSetValue = false;
		/**Форматирование высплывающего окна над значением*/
		private final MyMessageFormat valueFormat = new MyMessageFormat(Configurations.getProperty(Settings.class,"scrollTtext"));
		
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
		 * @param nameO имя параметра (по нему берутся навазния)
		 * @param minS минимальное значение слайдера
		 * @param defVal значение по умолчанию
		 * @param maxS максимальное значение слайдера
		 * @param mi манимально возможное значение
		 * @param nowVal текущее значение
		 * @param ma максимально возможное значение
		 * @param list слушатель, который сработает, когда значение изменится
		 */
		public Slider(String nameO, int minS, int defVal, int maxS, Integer mi,int nowVal, Integer ma, AdjustmentListener list) {
			setLayout(new BorderLayout(0, 0));
			label = new JLabel(Configurations.getHProperty(Settings.class,nameO + "L"));
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setToolTipText(Configurations.getHProperty(Settings.class,nameO + "T"));
			add(label, BorderLayout.NORTH);
			
			if(minS > maxS)
				throw new NumberFormatException("Значение минимума не может быть больше максимума");
			
			scroll = new JScrollBar();
			scroll.setVisibleAmount (0); // Значение экстента равно 0
			scroll.setMinimum(minS);
			scroll.setMaximum(maxS);
			scroll.setOrientation(JScrollBar.HORIZONTAL);
			scroll.addAdjustmentListener(e->setValue(e.getValue()));
			add(scroll, BorderLayout.CENTER);
			
			reset = Configurations.makeIconButton("reset");
			reset.setPreferredSize (BUT_SIZE);
			reset.addActionListener( e -> setValue(defVal));
			reset.setToolTipText(Configurations.getHProperty(Settings.class,"resetSlider"));
			
			insert = Configurations.makeIconButton("insert");
			insert.setPreferredSize (BUT_SIZE);
			insert.setToolTipText(Configurations.getHProperty(Settings.class,"insertSlider"));
			
			JPanel resetAndInsert = new JPanel();
			resetAndInsert.setLayout(new BorderLayout(0, 0));
			resetAndInsert.add(insert, BorderLayout.EAST);
			resetAndInsert.add(reset, BorderLayout.WEST);
			add(resetAndInsert, BorderLayout.EAST);
			insert.addActionListener( e -> showConfirmDialog());
			
			listener = list;
			min = mi;
			max = ma;
			value = nowVal == 0 ? 1 : 0;
			setValue(nowVal);
		}
		
		public void setValue(int val) {
			if(isSetValue) return;
			isSetValue = true;
			if(val != value && (min == null || val >= min) && (max == null || val <= max)) { 
				scroll.setToolTipText(valueFormat.format(((double)val-scroll.getMinimum())/(scroll.getMaximum()-scroll.getMinimum())));
				value = val;
				scroll.setValue(val);
				listener.adjustmentValueChanged(val);
			}
			isSetValue = false;
		}
		
		public int getValue() {
			return value;
		}
		
		private void showConfirmDialog() {
			JPanel insertValue = new JPanel();
	        insertValue.setLayout(new BorderLayout(0, 0));
	        
	        String labelInsertText = Configurations.getHProperty(Settings.class,"insertLabel");
	        if(min == null && max == null) 
	        	labelInsertText += " N∈R";
	        else if(min != null && max == null)
	        	labelInsertText += " N≥" + min.toString();
	        else if(min == null && max != null)
	        	labelInsertText += " N≤" + max.toString();
	        else
	        	labelInsertText += " N∈[" + min.toString() + "," + max.toString() +"]";
	        var tlabel = new JLabel(labelInsertText);
	        insertValue.add(tlabel, BorderLayout.NORTH);
	        var tText = new JTextField(new RangeFilter(),Integer.toString(value),10);
	        insertValue.add(tText, BorderLayout.CENTER);
			
	        var reset = Configurations.makeIconButton("reset");
			reset.setPreferredSize (BUT_SIZE);
			reset.addActionListener( e -> tText.setText(Integer.toString(value)));
			reset.setToolTipText(Configurations.getHProperty(Settings.class,"resetSlider"));
	        insertValue.add(reset, BorderLayout.EAST);

			javax.swing.UIManager.put("OptionPane.okButtonText"   , Configurations.getProperty(Settings.class,"okButtonText")   );
			javax.swing.UIManager.put("OptionPane.cancelButtonText", Configurations.getProperty(Settings.class,"cancelButtonText"));
			
			var ret = JOptionPane.showConfirmDialog(this, insertValue,Configurations.getProperty(Settings.class,"insertSlider"), JOptionPane.OK_CANCEL_OPTION);
			if(ret == JOptionPane.OK_OPTION) {
				setValue(Integer.parseInt(tText.getText()));
			}
		}
	}
	
	public static class ScrollPanel extends JPanel {
		/**Описание ползунка*/
		private final JLabel label;
		/**Скролик*/
		private final JScrollBar scroll;
		/**Показывает, что отсчёт ведётся в обратную сторону*/
		private final boolean isBack;
		
		//TODO - сделать две кнопки - сбросить на деф и ввести значение
		public ScrollPanel(String text, int min, int max){
			setLayout(new BorderLayout(0, 0));
			label = new JLabel(text);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			add(label, BorderLayout.NORTH);
			
			isBack = min > max;
			
			scroll = new JScrollBar();
			scroll.setVisibleAmount (0); // Значение экстента равно 0
			if(isBack) {
				scroll.setMinimum(max);
				scroll.setMaximum(min);
			}else {
				scroll.setMinimum(min);
				scroll.setMaximum(max);
			}
			scroll.setOrientation(JScrollBar.HORIZONTAL);
			add(scroll, BorderLayout.SOUTH);
		}

		public void setValue(int val) {
			if(isBack)
				val = getMaximum() - val + scroll.getMinimum();
			setToolTipText("Значение: "  + String.valueOf(100*(val-scroll.getMinimum())/(scroll.getMaximum()-scroll.getMinimum()))+"%");
			scroll.setValue(val);
		}
		public void setValue(double val) {
			setValue((int)Math.round(val));
		}

		public void setMaximum(int max) {
			scroll.setMaximum(max);
		}

		public int getMaximum() {
			return scroll.getMaximum();
		}

		public void setBlockIncrement(int blockIncrement) {
			scroll.setBlockIncrement(blockIncrement);
		}

		public void addAdjustmentListener(AdjustmentListener listener) {
			scroll.addAdjustmentListener(e->{
				setToolTipText("Значение: "  + String.valueOf(100*(e.getValue()-scroll.getMinimum())/(scroll.getMaximum()-scroll.getMinimum()))+"%");
				if(isBack)
					e = new AdjustmentEvent((Adjustable) e.getSource(),e.getID(),e.getAdjustmentType(),(getMaximum() - e.getValue())+scroll.getMinimum());
				listener.adjustmentValueChanged(e);
			});
		}
		@Override
		 public void setToolTipText(String text) {
			 super.setToolTipText(text);
			 label.setToolTipText(text);
			 scroll.setToolTipText(text);
		 }

		public int getValue() {
			return scroll.getValue();
		}
	}
	JComponent listener = null;
	
	/**Масштаб*/
	private Slider scale;
	/**Лист всех настроек*/
	List<Slider> listFields;
	
	/**Счётчик, показывающий, когда было сделанно последнее сохранение. Нужен, чтобы два раза подряд не сохраняться*/
	private long lastSaveCount = 0;
	
	/**
	 * Create the panel.
	 */
	public Settings() {
		Configurations.settings = this;
		
		setLayout(new BorderLayout(0, 0));
		construct();
	}
	
	/**Создаёт панель настроек со всем необходимым*/
	private void construct() {
		removeAll();
		add(makeParamsPanel());
	}
	
	private JScrollPane makeParamsPanel() {
		final var panelConstant = new JPanel();
		panelConstant.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), Configurations.getHProperty(Settings.class,"mainPanel"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		listFields = new ArrayList<>();

		listFields.add(new Slider("constSun", 1, Configurations.DBASE_SUN_POWER, 200, 1,Configurations.BASE_SUN_POWER,  null, e -> Configurations.setBASE_SUN_POWER(e)));
		listFields.add(new Slider("scrollSun", 0, Configurations.DADD_SUN_POWER, 200, 0,Configurations.ADD_SUN_POWER, null, e -> Configurations.setADD_SUN_POWER(e)));
		listFields.add(new Slider("sunSize", 
				1, Configurations.DSUN_LENGHT, Configurations.MAP_CELLS.width / 2,
				1, Configurations.SUN_LENGHT, Configurations.MAP_CELLS.width / 2, e -> Configurations.setSUN_LENGHT(e)));
		listFields.add(new Slider("sunSpeed", -200, Configurations.DSUN_SPEED, 200, Integer.MIN_VALUE, Configurations.SUN_SPEED, Integer.MAX_VALUE, e -> Configurations.setSUN_SPEED(e)));
		listFields.add(new Slider("dirtiness",
				0, Configurations.DDIRTY_WATER, 100,
				0, Configurations.DIRTY_WATER, null, e -> Configurations.setDIRTY_WATER(e)));
		
		listFields.add(new Slider("mineralHeight",
				0, (int) ((1 - Configurations.DLEVEL_MINERAL) * 100), 100,
				0, (int) ((1 - Configurations.LEVEL_MINERAL) * 100), null, e -> Configurations.setLEVEL_MINERAL(1 - e/100d)));
		listFields.add(new Slider("mineralСoncentration", 0, Configurations.DCONCENTRATION_MINERAL, 400, 0,Configurations.CONCENTRATION_MINERAL, null, e -> Configurations.setCONCENTRATION_MINERAL(e)));
		
		listFields.add( new Slider("timeLifeOrg", 1, Configurations.DTIK_TO_EXIT, 100, 1, Configurations.TIK_TO_EXIT,null, e -> {
			Configurations.TIK_TO_EXIT = e;
		}));
		
		listFields.add(new Slider("mutagenicity",
				0, Configurations.DAGGRESSIVE_ENVIRONMENT, 100,
				0, Configurations.AGGRESSIVE_ENVIRONMENT, 100, e -> Configurations.AGGRESSIVE_ENVIRONMENT = e));
		
		listFields.add( new Slider("sleepFrame", 0, 0, 5, 0, World.msTimeout,null, e -> {
				World.msTimeout = e;
		}));
		
		listFields.add(scale = new Slider("scale", 1,1, 100, 1, 1,null, e -> {
			if(listener != null) {
				listener.dispatchEvent(new ComponentEvent(listener, ComponentEvent.COMPONENT_RESIZED));
				Configurations.world.dispatchEvent(new ComponentEvent(Configurations.world, ComponentEvent.COMPONENT_RESIZED));
			}
		}));
		
		listFields.add( new Slider("sunForm", -50, Configurations.DSUN_FORM, 50, null, Configurations.SUN_FORM,null, e -> Configurations.setSUN_FORM(e)));
		

		GroupLayout gl_panel_const = new GroupLayout(panelConstant);
		var hGroupe = gl_panel_const.createParallelGroup(Alignment.TRAILING);
		for(var i : listFields) {
			hGroupe.addComponent(i, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE);
		}
		gl_panel_const.setHorizontalGroup(
			gl_panel_const.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_const.createSequentialGroup()
					.addContainerGap()
					.addGroup(hGroupe)
					.addContainerGap())
		);
		var wGroupe = gl_panel_const.createSequentialGroup();
		for(var i : listFields) {
			wGroupe.addComponent(i, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addPreferredGap(ComponentPlacement.RELATED);
		}
		
		gl_panel_const.setVerticalGroup(
			gl_panel_const.createParallelGroup(Alignment.LEADING)
				.addGroup(wGroupe)
		);
		panelConstant.setLayout(gl_panel_const);
	
		final var scroll = new JScrollPane(panelConstant);
		scroll.setBorder(null);
		return scroll;
	}

	/**
	 * Функция загрузки - предложит окно и всё сама сделает
	 */
	public void load() {
		try{
			Configurations.world.stop();
			var js = new JsonSave("BioLife", "zbmap", Configurations.VERSION);
			js.addActionListener( e-> System.out.println("Загрузка " + e.now + " из " + e.all + ". Осталось " + (e.getTime()/1000) + "c"));
			var filename = js.load();
			while(Configurations.world.isActiv()){ //На всякий случай убеждаемся, что мир прям точно встал!
				Configurations.world.stop();
				Utils.Utils.pause(1);
			}
			if(filename == null) return;
			else if(filename.contains(".zbmap")){//Новый стандарт
				js.load(filename, new Configurations(), Configurations.tree, Configurations.world.serelization());
			} else { //Старый стандарт
				var obj = new JSON();
				if(!js.load(filename, obj)) return;
				Configurations.world.update(obj);
			}
			Configurations.evolTreeDialog.restart();
			lastSaveCount = Configurations.world.step;
			construct();
			repaint();
			revalidate();
		} catch (java.lang.Exception e1) {
			e1.printStackTrace();
			var error = MessageFormat.format(Configurations.getHProperty(Settings.class,"error.load"),e1.getMessage());
			JOptionPane.showMessageDialog(null,error,	"BioLife", JOptionPane.ERROR_MESSAGE);
		} 
	}
	/**
	 * Функция сохранения - предложит окно и всё сама сделает
	 */
	public void save() {
		try{
			boolean oldStateWorld = Configurations.world.isActiv();	
			while(Configurations.world.isActiv()){ //На всякий случай убеждаемся, что мир прям точно встал!
				Configurations.world.stop();
				Utils.Utils.pause(1);
			}

			if(lastSaveCount != Configurations.world.step) {
				var js = new JsonSave("BioLife", "zbmap", Configurations.VERSION);
				js.addActionListener( e-> System.out.println("Сохранение " + e.now + " из " + e.all + ". Осталось " + (e.getTime()/1000) + "c"));
				if(js.save(true, new Configurations(), Configurations.tree, Configurations.world.serelization()))
					lastSaveCount = Configurations.world.step;
			}
			if (oldStateWorld)
				Configurations.world.start();
			else
				Configurations.world.stop();
		} catch (java.lang.Exception e1) {
			e1.printStackTrace();
			var error = MessageFormat.format(Configurations.getHProperty(Settings.class,"error.save"),e1.getMessage());
			JOptionPane.showMessageDialog(null,error,	"BioLife", JOptionPane.ERROR_MESSAGE);
		} 
	}
	/**
	 * Функция сохранения - сохранит мир в определённый файл
	 * @param filePatch - путь, куда сохранить мир
	 */
	public void save(String filePatch) {
		try{
			boolean oldStateWorld = Configurations.world.isActiv();		
			while(Configurations.world.isActiv()){ //На всякий случай убеждаемся, что мир прям точно встал!
				Configurations.world.stop();
				Utils.Utils.pause(1);
			}

			if(lastSaveCount != Configurations.world.step) {
				var js = new JsonSave("BioLife", "zbmap", Configurations.VERSION);
				js.addActionListener( e-> System.out.println("Автосохранение " + e.now + " из " + e.all + ". Осталось " + (e.getTime()/1000) + "c"));
				if(js.save(filePatch,true, new Configurations(), Configurations.tree, Configurations.world.serelization()))
					lastSaveCount = Configurations.world.step;
			}
			if (oldStateWorld)
				Configurations.world.start();
			else
				Configurations.world.stop();
		} catch (java.lang.Exception e1) {
			e1.printStackTrace();
			var error = MessageFormat.format(Configurations.getHProperty(Settings.class,"error.autosave"),e1.getMessage());
			JOptionPane.showMessageDialog(null,error,	"BioLife", JOptionPane.ERROR_MESSAGE);
		} 
	}

	public void setListener(JComponent scrollPane) {
		listener = scrollPane;
	}
	/**
	 * Возвращает установленный настройками масштаб
	 * @return число в интервале от 10 до 100
	 */
	public int getScale() {
		return scale.getValue();
	}
	/**
	 * Изменяет масштаб на велечину переданного параметра
	 * @param val масштаб от 10 до 100
	 */
	public void addScale(int val) {
		scale.setValue(getScale() + val);
	}
}
