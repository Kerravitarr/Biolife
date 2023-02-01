package panels;

import static main.World.isActiv;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import Utils.JSON;
import Utils.JsonSave;
import main.Configurations;
import main.World;

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
		/**Показывает, что отсчёт ведётся в обратную сторону*/
		private final boolean isBack;
		/**Размер кнопок*/
		private static final Dimension BUT_SIZE = new Dimension(20,15);
		
		/**Фильтр вводимых значений*/
		class MyDocumentFilter extends DocumentFilter {
			private final Integer min;
			private final Integer max;
			MyDocumentFilter(Integer mi, Integer ma){
				min = mi; max = ma;
			}

			@Override
			public void insertString(DocumentFilter.FilterBypass fp, int offset, String string, AttributeSet aset)throws BadLocationException {
				if (isValid(string))
					super.insertString(fp, offset, string, aset);
				else
					java.awt.Toolkit.getDefaultToolkit().beep();
			}

			@Override
			public void replace(DocumentFilter.FilterBypass fp, int offset, int length, String string,AttributeSet aset) throws BadLocationException {
				if (isValid(string))
					super.replace(fp, offset, length, string, aset);
				else
					java.awt.Toolkit.getDefaultToolkit().beep();
			}
			
			private boolean isValid(String string) {
				int len = string.length();
				boolean isValidInteger = true;
				for (int i = 0; i < len; i++) {
					if (!Character.isDigit(string.charAt(i))) {
						isValidInteger = false;
						break;
					}
				}
				if (isValidInteger && (min != null || max != null)) {
					var val = Integer.parseInt(string);
					if (min != null && max != null)
						isValidInteger = min <= val && val <= max;
					else if (min != null)
						isValidInteger = min <= val;
					else
						isValidInteger = val <= max;
				}
				return isValidInteger;
			}
		}
		
		@SuppressWarnings("null")
		public Slider(String name,String toolTipText, int minS, int defVal, int maxS, Integer min, Integer max) {
			setLayout(new BorderLayout(0, 0));
			label = new JLabel(name);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setToolTipText(toolTipText);
			add(label, BorderLayout.NORTH);
			
			isBack = minS > maxS;
			
			scroll = new JScrollBar();
			scroll.setVisibleAmount (0); // Значение экстента равно 0
			if(isBack) {
				scroll.setMinimum(maxS);
				scroll.setMaximum(minS);
			}else {
				scroll.setMinimum(minS);
				scroll.setMaximum(maxS);
			}
			scroll.setOrientation(JScrollBar.HORIZONTAL);
			scroll.setToolTipText(toolTipText);
			add(scroll, BorderLayout.CENTER);
			
			reset = new JButton();
			Configurations.setIcon(reset, "reset");
			reset.setPreferredSize (BUT_SIZE);
			reset.setBorderPainted(false);
			reset.setFocusPainted(false);
			reset.setContentAreaFilled(false);
			reset.addActionListener( e -> setValue(defVal));
			reset.setToolTipText(Configurations.getProperty(Settings.class,"resetSlider"));
			reset.setFocusable(false);
			
			insert = new JButton();
			Configurations.setIcon(insert, "insert");
			insert.setPreferredSize (BUT_SIZE);
			insert.setBorderPainted(false);
			insert.setFocusPainted(false);
			insert.setContentAreaFilled(false);
			insert.setToolTipText(Configurations.getProperty(Settings.class,"insertSlider"));
			insert.setFocusable(false);
			
			JPanel resetAndInsert = new JPanel();
			resetAndInsert.setLayout(new BorderLayout(0, 0));
			resetAndInsert.add(insert, BorderLayout.EAST);
			resetAndInsert.add(reset, BorderLayout.WEST);
			add(resetAndInsert, BorderLayout.EAST);
			
			JPanel insertValue = new JPanel();
	        insertValue.setLayout(new BorderLayout(0, 0));
	        
	        String labelInsertText = Configurations.getProperty(Settings.class,"insertLabel");
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
	        var tField = new JTextField(10);
			((AbstractDocument) tField.getDocument()).setDocumentFilter(new MyDocumentFilter(min,max));
	        insertValue.add(tField, BorderLayout.CENTER);
			insert.addActionListener( e -> {
				JOptionPane.showMessageDialog(this, insertValue,Configurations.getProperty(Settings.class,"insertSlider"), JOptionPane.QUESTION_MESSAGE);
				System.out.println(tField.getText());
			});
			
			setValue(defVal);
		}
		
		public void setValue(int val) {
			if(isBack)
				val = scroll.getMaximum() - val + scroll.getMinimum();
			setToolTipText("Значение: "  + String.valueOf(100*(val-scroll.getMinimum())/(scroll.getMaximum()-scroll.getMinimum()))+"%");
			scroll.setValue(val);
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
	
	private final ScrollPanel scrollBar_7;
	private final ScrollPanel scrollBar_6;
	private final ScrollPanel scrollBar_5;
	private final ScrollPanel scrollBar_4;
	private final ScrollPanel scrollBar_2;
	private final ScrollPanel scrollBar_1;
	private final ScrollPanel const_SP;
	private final ScrollPanel scale;
	JComponent listener = null;
	private final ScrollPanel sun_speed;
	private final ScrollPanel scroll_SP;
	private final JButton step_button;
	private final ScrollPanel sun_size;
	
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
		
		JLabel lblNewLabel = new JLabel("Настройки");
		add(lblNewLabel, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		//add(makeParamsPanel());
		
		const_SP = new ScrollPanel("Постоянная освещённость",1,30);
		scroll_SP = new ScrollPanel("Движущаяся освещённость",1,30);
		scrollBar_1 = new ScrollPanel("Загрязнённость воды",1,50);
		scrollBar_2 = new ScrollPanel("Мутагенность",0,100);
		scrollBar_4 = new ScrollPanel("Высота минерализации",0,100);
		scrollBar_5 = new ScrollPanel("Концентрация минералов",0,40);
		scrollBar_5.setBlockIncrement(5);
		scrollBar_6 = new ScrollPanel("Скорость разложения",100,1);
		scrollBar_7 = new ScrollPanel("Частота кадров",100,0);
		scrollBar_7.setBlockIncrement(20);
		scale = new ScrollPanel("Масштаб",10,100);
		scale.setValue(10);
		sun_speed = new ScrollPanel("Скорость солнца",200,1);
		sun_speed.setValue(Configurations.SUN_SPEED);
		sun_size = new ScrollPanel("Размер солнца",1,Configurations.SUN_PARTS*2);
		
		step_button = new JButton("Шаг");
		step_button.setToolTipText("Для простоты можно нажать S на клавиатуре");
		step_button.addActionListener(e-> Configurations.world.step());
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addComponent(const_SP, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(step_button, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(scrollBar_7, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(scrollBar_6, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(scrollBar_2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(scrollBar_4, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(scrollBar_5, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(sun_size, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(sun_speed, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(scrollBar_1, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(scroll_SP, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(scale, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(const_SP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scroll_SP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollBar_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sun_speed, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sun_size, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollBar_5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollBar_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollBar_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollBar_6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollBar_7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
					.addComponent(step_button))
		);
		
		panel.setLayout(gl_panel);
		setListeners();
	}
	
	public void updateScrols() {
		sun_speed.setValue(Configurations.SUN_SPEED );
		scroll_SP.setValue(Configurations.ADD_SUN_POWER);
		sun_size.setMaximum(Configurations.SUN_PARTS*2);
		if(Configurations.SUN_LENGHT <= Configurations.SUN_PARTS)
			sun_size.setValue(Configurations.SUN_LENGHT);
		else
			sun_size.setValue((int) Math.round(Math.pow(Configurations.SUN_LENGHT-Configurations.SUN_PARTS, 0.5)+Configurations.SUN_PARTS));
		scrollBar_7.setValue(World.msTimeout);
		scrollBar_6.setValue(Configurations.TIK_TO_EXIT);
		scrollBar_5.setValue(Configurations.CONCENTRATION_MINERAL*10);
		scrollBar_4.setValue((1-Configurations.LEVEL_MINERAL)*100);
		scrollBar_2.setValue(Configurations.AGGRESSIVE_ENVIRONMENT*100);
		scrollBar_1.setValue(Configurations.DIRTY_WATER);
		const_SP.setValue(Configurations.BASE_SUN_POWER);
	}
	
	public final void setListeners() {
		scroll_SP.addAdjustmentListener(e->{
			Configurations.ADD_SUN_POWER =  e.getValue();
			Configurations.world.recalculate();
		});
		sun_size.addAdjustmentListener(e->{
			if(e.getValue() <= Configurations.SUN_PARTS)
				Configurations.SUN_LENGHT = e.getValue();
			else
				Configurations.SUN_LENGHT = (int) Math.round(Configurations.SUN_PARTS + Math.pow(e.getValue() - Configurations.SUN_PARTS,2));
			Configurations.sun.repaint();
		});
		sun_speed.addAdjustmentListener(e->{
			Configurations.SUN_SPEED =  e.getValue();
		});
		const_SP.addAdjustmentListener(e->{
			Configurations.BASE_SUN_POWER = e.getValue();
			Configurations.world.recalculate();
		});
		scrollBar_1.addAdjustmentListener(e->{
			Configurations.DIRTY_WATER = e.getValue();
			Configurations.world.recalculate();
		});
		scrollBar_2.addAdjustmentListener(e->{
			Configurations.AGGRESSIVE_ENVIRONMENT = e.getValue()/100.0;
		});
		scrollBar_4.addAdjustmentListener(e->{
			Configurations.LEVEL_MINERAL = 1-e.getValue()/100.0;
			Configurations.world.recalculate();
		});
		scrollBar_5.addAdjustmentListener(e->{
			Configurations.CONCENTRATION_MINERAL = e.getValue()/10.0;
			Configurations.world.recalculate();
		});
		scrollBar_6.addAdjustmentListener(e->{
			Configurations.TIK_TO_EXIT = e.getValue();
		});
		scrollBar_7.addAdjustmentListener(e->{
			World.msTimeout = e.getValue();
		});
		scale.addAdjustmentListener(e->{
			if(listener != null) {
				listener.dispatchEvent(new ComponentEvent(listener, ComponentEvent.COMPONENT_RESIZED));
				Configurations.world.dispatchEvent(new ComponentEvent(Configurations.world, ComponentEvent.COMPONENT_RESIZED));
			}
		});
	}
	
	private JPanel makeParamsPanel() {
		JPanel panelConstant = new JPanel();
		panelConstant.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), Configurations.getHProperty(Settings.class,"mainPanel"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		listFields = new ArrayList<>();

		listFields.add(new Slider(Configurations.getHProperty(Settings.class,"constSunL"), Configurations.getHProperty(Settings.class,"constSunT"), 1,Configurations.BASE_SUN_POWER,30, 1 , null));
		

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
		return panelConstant;
	}

	/**
	 * Функция загрузки - предложит окно и всё сама сделает
	 */
	public void load() {
		isActiv = false;
		var js = new JsonSave("BioLife", "map");
		var obj = new JSON();
		if(!js.load(obj)) return;
		try{
			Configurations.world.update(obj);
			lastSaveCount = Configurations.world.step;
		} catch (java.lang.RuntimeException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null,	"<html>Ошибка загрузки!<br>" + e1.getMessage(),	"BioLife", JOptionPane.ERROR_MESSAGE);
		} 
	}
	/**
	 * Функция сохранения - предложит окно и всё сама сделает
	 */
	public void save() {
		boolean oldStateWorld = isActiv;				
		isActiv = false;
		
		if(lastSaveCount != Configurations.world.step) {
			var js = new JsonSave("BioLife", "map");
			if(js.save(Configurations.world.serelization(), true))
				lastSaveCount = Configurations.world.step;
		}
		isActiv = oldStateWorld;
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
