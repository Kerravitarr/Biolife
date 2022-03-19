package panels;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileNotFoundException;
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

import Utils.JSONmake;
import main.Configurations;
import main.World;

public class Settings extends JPanel{
	private JScrollBar scrollBar_7;
	private JScrollBar scrollBar_6;
	private JScrollBar scrollBar_5;
	private JScrollBar scrollBar_4;
	private JScrollBar scrollBar_2;
	private JScrollBar scrollBar_1;
	private JScrollBar const_SP;
	public JScrollBar scale;
	JComponent listener = null;
	private JButton play;
	private JScrollBar sun_speed;
	private JScrollBar scroll_SP;
	private JButton load_button;
	private JButton saveButton;
	private JButton step_button;
	private JScrollBar sun_size;
	
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
		
		JPanel panel_1 = new JPanel();
		
		JPanel panel_2 = new JPanel();
		
		JPanel panel_3 = new JPanel();
		
		play = new JButton();
		
		JPanel panel_5 = new JPanel();
		
		JPanel panel_6 = new JPanel();
		
		saveButton = new JButton("Сохранить мир");
		
		load_button = new JButton("Загрузить мир");
		
		step_button = new JButton("Шаг");
		step_button.addActionListener(e-> Configurations.world.step());
		
		JPanel panel_4_1 = new JPanel();
		panel_4_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_7 = new JPanel();
		
		JPanel panel_8 = new JPanel();
		
		JPanel panel_4 = new JPanel();
		
		JPanel panel_9 = new JPanel();
		
		JPanel panel_10 = new JPanel();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(saveButton, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(load_button, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(play, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(step_button, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_8, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_7, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_4_1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_5, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_6, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_10, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_4, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_9, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_9, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_10, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_4_1, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
					.addComponent(step_button)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(play)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(load_button)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(saveButton)
					.addContainerGap())
		);
		panel_10.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_11 = new JLabel("Размер солнца");
		lblNewLabel_11.setHorizontalAlignment(SwingConstants.CENTER);
		panel_10.add(lblNewLabel_11, BorderLayout.NORTH);
		
		sun_size = new JScrollBar();
		sun_size.setVisibleAmount (0); // Значение экстента равно 0
		sun_size.setMinimum(1);
		sun_size.setMaximum(Configurations.SUN_PARTS*2);
		sun_size.setOrientation(JScrollBar.HORIZONTAL);
		panel_10.add(sun_size, BorderLayout.SOUTH);
		panel_9.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_10 = new JLabel("Движущаяся освещённость");
		lblNewLabel_10.setHorizontalAlignment(SwingConstants.CENTER);
		panel_9.add(lblNewLabel_10, BorderLayout.NORTH);
		
		scroll_SP = new JScrollBar();
		scroll_SP.setVisibleAmount (0); // Значение экстента равно 0
		scroll_SP.setMinimum(1);
		scroll_SP.setMaximum(30);
		scroll_SP.setOrientation(JScrollBar.HORIZONTAL);
		panel_9.add(scroll_SP, BorderLayout.SOUTH);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_4 = new JLabel("Скорость солнца");
		lblNewLabel_4.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblNewLabel_4, BorderLayout.NORTH);
		
		sun_speed = new JScrollBar();
		sun_speed.setVisibleAmount (0);
		sun_speed.setMinimum(1);
		sun_speed.setOrientation(JScrollBar.HORIZONTAL);
		sun_speed.setValue(sun_speed.getMaximum() - Configurations.SUN_SPEED + 1);
		panel_4.add(sun_speed, BorderLayout.SOUTH);
		panel_8.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_9 = new JLabel("Масштаб");
		lblNewLabel_9.setHorizontalAlignment(SwingConstants.CENTER);
		panel_8.add(lblNewLabel_9, BorderLayout.NORTH);
		
		scale = new JScrollBar();
		scale.setValue(10);
		scale.setMinimum(10);
		scale.setOrientation(JScrollBar.HORIZONTAL);
		panel_8.add(scale, BorderLayout.SOUTH);
		panel_7.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_8 = new JLabel("Частота кадров");
		lblNewLabel_8.setHorizontalAlignment(SwingConstants.CENTER);
		panel_7.add(lblNewLabel_8, BorderLayout.NORTH);
		
		scrollBar_7 = new JScrollBar();
		scrollBar_7.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_7.setBlockIncrement(20);
		scrollBar_7.setMaximum(1000);
		scrollBar_7.setMinimum(1);
		scrollBar_7.setOrientation(JScrollBar.HORIZONTAL);
		panel_7.add(scrollBar_7, BorderLayout.SOUTH);
		
		JLabel lblNewLabel_7 = new JLabel("Скорость разложения");
		lblNewLabel_7.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4_1.add(lblNewLabel_7, BorderLayout.NORTH);
		
		scrollBar_6 = new JScrollBar();
		scrollBar_6.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_6.setMaximum(10);
		scrollBar_6.setOrientation(JScrollBar.HORIZONTAL);
		panel_4_1.add(scrollBar_6, BorderLayout.SOUTH);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_6 = new JLabel("Концентрация минералов");
		lblNewLabel_6.setHorizontalAlignment(SwingConstants.CENTER);
		panel_6.add(lblNewLabel_6, BorderLayout.NORTH);
		
		scrollBar_5 = new JScrollBar();
		scrollBar_5.setMaximum(40);
		scrollBar_5.setBlockIncrement(5);
		scrollBar_5.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_5.setOrientation(JScrollBar.HORIZONTAL);
		panel_6.add(scrollBar_5, BorderLayout.SOUTH);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_5 = new JLabel("Высота минерализации");
		lblNewLabel_5.setHorizontalAlignment(SwingConstants.CENTER);
		panel_5.add(lblNewLabel_5, BorderLayout.NORTH);
		
		scrollBar_4 = new JScrollBar();
		scrollBar_4.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_4.setOrientation(JScrollBar.HORIZONTAL);
		panel_5.add(scrollBar_4, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_3 = new JLabel("Мутагенность");
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(lblNewLabel_3, BorderLayout.NORTH);
		
		scrollBar_2 = new JScrollBar();
		scrollBar_2.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_2.setOrientation(JScrollBar.HORIZONTAL);
		panel_3.add(scrollBar_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_2 = new JLabel("Загрязнённость воды");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(lblNewLabel_2, BorderLayout.NORTH);
		
		scrollBar_1 = new JScrollBar();
		scrollBar_1.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_1.setMinimum(1);
		scrollBar_1.setMaximum(50);
		scrollBar_1.setOrientation(JScrollBar.HORIZONTAL);
		panel_2.add(scrollBar_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_1 = new JLabel("Постоянная освещённость");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblNewLabel_1, BorderLayout.NORTH);
		
		const_SP = new JScrollBar();
		const_SP.setVisibleAmount (0); // Значение экстента равно 0
		const_SP.setMinimum(1);
		const_SP.setMaximum(30);
		const_SP.setOrientation(JScrollBar.HORIZONTAL);
		panel_1.add(const_SP, BorderLayout.SOUTH);
		panel.setLayout(gl_panel);
		setListeners();
	}
	
	public void updateScrols() {
		if(World.isActiv)
			play.setText("Пауза");
		else
			play.setText("Пуск");
		sun_speed.setValue(sun_speed.getMaximum() - Configurations.SUN_SPEED + 1);
		scroll_SP.setValue(Configurations.ADD_SUN_POWER);
		sun_size.setMaximum(Configurations.SUN_PARTS*2);
		if(Configurations.SUN_LENGHT <= Configurations.SUN_PARTS)
			sun_size.setValue(Configurations.SUN_LENGHT);
		else
			sun_size.setValue((int) Math.round(Math.pow(Configurations.SUN_LENGHT-Configurations.SUN_PARTS, 0.5)+Configurations.SUN_PARTS));
		scrollBar_7.setValue(scrollBar_7.getMaximum() - World.msTimeout);
		scrollBar_6.setValue(scrollBar_6.getMaximum() - Configurations.TIK_TO_EXIT + 1);
		scrollBar_5.setValue((int) Math.round(Configurations.CONCENTRATION_MINERAL*10));
		scrollBar_4.setValue((int)  Math.round((1-Configurations.LEVEL_MINERAL)*100));
		scrollBar_2.setValue((int) Math.round(Configurations.AGGRESSIVE_ENVIRONMENT*100));
		scrollBar_1.setValue((int) Math.round(Configurations.DIRTY_WATER));
		const_SP.setValue((int) Math.round(Configurations.BASE_SUN_POWER));
	}
	
	public void setListeners() {

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
			Configurations.SUN_SPEED =  (int) Math.pow(sun_speed.getMaximum() - e.getValue() + 1,1.5);
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
			Configurations.TIK_TO_EXIT = scrollBar_6.getMaximum() - e.getValue() + 1;
		});
		scrollBar_7.addAdjustmentListener(e->{
			World.msTimeout = scrollBar_7.getMaximum() - e.getValue();
		});
		scale.addAdjustmentListener(e->{
			if(listener != null) {
				listener.dispatchEvent(new ComponentEvent(listener, ComponentEvent.COMPONENT_RESIZED));
				Configurations.world.dispatchEvent(new ComponentEvent(Configurations.world, ComponentEvent.COMPONENT_RESIZED));
			}
		});
		play.addActionListener(e->{
			if(play.getText().equals("Пауза")) {
				World.isActiv = false;
			} else {
				World.isActiv = true;
			}
			updateScrols();
		});
		load_button.addActionListener(e->load());
		saveButton.addActionListener(e->save());
	}

	public void load() {
		World.isActiv = false;
		String pathToRoot = System.getProperty("user.dir");
		JFileChooser fileopen = new JFileChooser(pathToRoot);
		fileopen.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
		int ret = fileopen.showDialog(null, "Выбрать файл");
		if (ret == JFileChooser.APPROVE_OPTION) {
			try(FileReader reader = new FileReader(fileopen.getSelectedFile().getPath())){
				Configurations.world.update(new JSONmake(reader));
			} catch (IOException | java.lang.RuntimeException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null,	"<html>Ошибка загрузки!<br>" + e1.getMessage(),	"BioLife", JOptionPane.ERROR_MESSAGE);
			} 
		}
	}
	
	public void save() {
		boolean oldStateWorld = World.isActiv;				
		World.isActiv = false;
		Date date = new Date();
		SimpleDateFormat formater = new SimpleDateFormat("yyyy_MM_dd HHч mmм ssс");
		String name = "World_" + formater.format(date) + ".json";
		try(FileWriter writer = new FileWriter(name, true)){
			Configurations.world.serelization().writeToFormatJSONString(writer);
			writer.flush();
			JOptionPane.showMessageDialog(null,	"Сохранение заверешно",	"BioLife", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException | java.lang.RuntimeException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null,	"Ошибка сохранения!\n" + e1.getMessage(),	"BioLife", JOptionPane.ERROR_MESSAGE);
		}
		World.isActiv = oldStateWorld;
	}

	public void setListener(JComponent scrollPane) {
		listener = scrollPane;
	}
}
