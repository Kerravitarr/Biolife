package panels;

import static main.World.isActiv;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import Utils.JSON;
import Utils.JSON.ParseException;
import Utils.JsonSave;
import main.Configurations;
import main.World;

public class Settings extends JPanel{
	public static class ScrollPanel extends JPanel {
		/**Описание ползунка*/
		private JLabel label;
		/**Скролик*/
		private JScrollBar scroll;
		/**Показывает, что отсчёт ведётся в обратную сторону*/
		private boolean isBack;
		
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
	
	private ScrollPanel scrollBar_7;
	private ScrollPanel scrollBar_6;
	private ScrollPanel scrollBar_5;
	private ScrollPanel scrollBar_4;
	private ScrollPanel scrollBar_2;
	private ScrollPanel scrollBar_1;
	private ScrollPanel const_SP;
	public ScrollPanel scale;
	JComponent listener = null;
	private JButton play;
	private ScrollPanel sun_speed;
	private ScrollPanel scroll_SP;
	private JButton load_button;
	private JButton saveButton;
	private JButton step_button;
	private ScrollPanel sun_size;
	private ScrollPanel PoisonStreem;
	
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
		
		const_SP = new ScrollPanel("Постоянная освещённость",1,30);
		scroll_SP = new ScrollPanel("Движущаяся освещённость",1,30);
		scrollBar_1 = new ScrollPanel("Загрязнённость воды",1,50);
		scrollBar_2 = new ScrollPanel("Мутагенность",0,100);
		scrollBar_4 = new ScrollPanel("Высота минерализации",0,100);
		scrollBar_5 = new ScrollPanel("Концентрация минералов",0,40);
		scrollBar_5.setBlockIncrement(5);
		scrollBar_6 = new ScrollPanel("Скорость разложения",10,1);
		scrollBar_7 = new ScrollPanel("Частота кадров",100,0);
		scrollBar_7.setBlockIncrement(20);
		scale = new ScrollPanel("Масштаб",10,100);
		scale.setValue(10);
		sun_speed = new ScrollPanel("Скорость солнца",100,1);
		sun_speed.setValue(Configurations.SUN_SPEED);
		sun_size = new ScrollPanel("Размер солнца",1,Configurations.SUN_PARTS*2);
		PoisonStreem = new ScrollPanel("Вязкость яда",1,16);
		PoisonStreem.setBlockIncrement(3);
		PoisonStreem.setValue((int) Math.round(Math.log(Configurations.POISON_STREAM)));
		
		play = new JButton();
		play.setToolTipText("Для простоты можно нажать пробел на клавиатуре");
		saveButton = new JButton("Сохранить мир");
		load_button = new JButton("Загрузить мир");
		step_button = new JButton("Шаг");
		step_button.setToolTipText("Для простоты можно нажать S на клавиатуре");
		step_button.addActionListener(e-> Configurations.world.step());
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addComponent(PoisonStreem, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(const_SP, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(saveButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(load_button, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(play, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
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
					.addComponent(PoisonStreem, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollBar_7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
					.addComponent(step_button)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(play)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(load_button)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(saveButton)
					.addContainerGap())
		);
		
		panel.setLayout(gl_panel);
		setListeners();
	}
	
	public void updateScrols() {
		if(isActiv)
			play.setText("Пауза");
		else
			play.setText("Пуск");
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
	
	public void setListeners() {

		PoisonStreem.addAdjustmentListener(e->{
			Configurations.POISON_STREAM =  (int) Math.round(Math.exp(e.getValue()));
			System.out.println(Configurations.POISON_STREAM);
		});
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
		play.addActionListener(e->{
			if(play.getText().equals("Пауза")) {
				isActiv = false;
			} else {
				isActiv = true;
			}
			updateScrols();
		});
		load_button.addActionListener(e->load());
		saveButton.addActionListener(e->save());
	}

	public void load() {
		isActiv = false;
		var js = new JsonSave("BioLife", "map");
		var obj = new JSON();
		js.load(obj);
		try{
			Configurations.world.update(obj);
		} catch (java.lang.RuntimeException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null,	"<html>Ошибка загрузки!<br>" + e1.getMessage(),	"BioLife", JOptionPane.ERROR_MESSAGE);
		} 
	}
	
	public void save() {
		boolean oldStateWorld = isActiv;				
		isActiv = false;
		var js = new JsonSave("BioLife", "map");
		js.save(Configurations.world.serelization(), true);
		isActiv = oldStateWorld;
	}

	public void setListener(JComponent scrollPane) {
		listener = scrollPane;
	}
}
