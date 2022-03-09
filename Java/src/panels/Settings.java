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
import main.World;

public class Settings extends JPanel {
	private JScrollBar scrollBar_7;
	private JScrollBar scrollBar_6;
	private JScrollBar scrollBar_3;
	private JScrollBar scrollBar_5;
	private JScrollBar scrollBar_4;
	private JScrollBar scrollBar_2;
	private JScrollBar scrollBar_1;
	private JScrollBar scrollBar;
	public JScrollBar scale;
	JComponent listener = null;
	private JButton play;
	
	/**
	 * Create the panel.
	 */
	public Settings() {
		setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("Настройки");
		add(lblNewLabel, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		
		JPanel panel_2 = new JPanel();
		
		JPanel panel_3 = new JPanel();
		
		play = new JButton();
		play.addActionListener(e->{
			if(play.getText().equals("Пауза")) {
				World.isActiv = false;
			} else {
				World.isActiv = true;
			}
			updateScrols();
		});
		
		JPanel panel_4 = new JPanel();
		
		JPanel panel_5 = new JPanel();
		
		JPanel panel_6 = new JPanel();
		
		JButton btnNewButton_1 = new JButton("Сохранить мир");
		btnNewButton_1.addActionListener(e->{
			boolean oldStateWorld = World.isActiv;				
			World.isActiv = false;
			Utils.Utils.pause(2);
			Date date = new Date();
			SimpleDateFormat formater = new SimpleDateFormat("yyyy_MM_dd HHч mmм ssс");
			String name = "World_" + formater.format(date) + ".json";
			try(FileWriter writer = new FileWriter(name, true)){
				World.world.serelization().writeToFormatJSONString(writer);
				writer.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			World.isActiv = oldStateWorld;

			JOptionPane.showMessageDialog(this,	"Сохранение заверешно",	"BioLife", JOptionPane.INFORMATION_MESSAGE);
		});
		
		JButton btnNewButton_2 = new JButton("Загрузить мир");
		btnNewButton_2.addActionListener(e->{			
			World.isActiv = false;
			String pathToRoot = System.getProperty("user.dir");
			JFileChooser fileopen = new JFileChooser(pathToRoot);
			fileopen.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
			int ret = fileopen.showDialog(null, "Выбрать файл");
			if (ret == JFileChooser.APPROVE_OPTION) {
				try(FileReader reader = new FileReader(fileopen.getSelectedFile().getPath())){
					World.world.update(new JSONmake(reader));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JButton btnNewButton_3 = new JButton("Шаг");
		btnNewButton_3.addActionListener(e-> World.world.step());
		
		JPanel panel_4_1 = new JPanel();
		panel_4_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_7 = new JPanel();
		
		JPanel panel_8 = new JPanel();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel_8, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_7, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_6, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_5, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(btnNewButton_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(btnNewButton_2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(play, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(btnNewButton_3, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_4_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_4_1, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 217, Short.MAX_VALUE)
					.addComponent(btnNewButton_3)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(play)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton_2)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton_1)
					.addContainerGap())
		);
		panel_8.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_9 = new JLabel("Масштаб");
		lblNewLabel_9.setHorizontalAlignment(SwingConstants.CENTER);
		panel_8.add(lblNewLabel_9, BorderLayout.NORTH);
		
		scale = new JScrollBar();
		scale.addAdjustmentListener(e->{
			if(listener != null) {
				listener.dispatchEvent(new ComponentEvent(listener, ComponentEvent.COMPONENT_RESIZED));
				World.world.dispatchEvent(new ComponentEvent(World.world, ComponentEvent.COMPONENT_RESIZED));
			}
		});
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
		scrollBar_7.addAdjustmentListener(e->{
			World.msTimeout = scrollBar_7.getMaximum() - e.getValue();
		});
		panel_7.add(scrollBar_7, BorderLayout.SOUTH);
		
		JLabel lblNewLabel_7 = new JLabel("Скорость разложения");
		lblNewLabel_7.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4_1.add(lblNewLabel_7, BorderLayout.NORTH);
		
		scrollBar_6 = new JScrollBar();
		scrollBar_6.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_6.setMaximum(10);
		scrollBar_6.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar_6.addAdjustmentListener(e->{
			World.TIK_TO_EXIT = scrollBar_6.getMaximum() - e.getValue() + 1;
		});
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
		scrollBar_5.addAdjustmentListener(e->{
			World.CONCENTRATION_MINERAL = e.getValue()/10.0;
			World.world.recalculate();
		});
		panel_6.add(scrollBar_5, BorderLayout.SOUTH);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_5 = new JLabel("Высота минерализации");
		lblNewLabel_5.setHorizontalAlignment(SwingConstants.CENTER);
		panel_5.add(lblNewLabel_5, BorderLayout.NORTH);
		
		scrollBar_4 = new JScrollBar();
		scrollBar_4.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_4.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar_4.addAdjustmentListener(e->{
			World.LEVEL_MINERAL = 1-e.getValue()/100.0;
			World.world.recalculate();
		});
		panel_5.add(scrollBar_4, BorderLayout.SOUTH);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_4 = new JLabel("Множитель ФПС");
		lblNewLabel_4.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblNewLabel_4, BorderLayout.NORTH);
		
		scrollBar_3 = new JScrollBar();
		scrollBar_3.setEnabled(false);
		scrollBar_3.setMinimum(1);
		scrollBar_3.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_3.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar_3.addAdjustmentListener(e->{
			//World.FPS_TIC = e.getValue();
		});
		panel_4.add(scrollBar_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_3 = new JLabel("Мутагенность");
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(lblNewLabel_3, BorderLayout.NORTH);
		
		scrollBar_2 = new JScrollBar();
		scrollBar_2.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_2.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar_2.addAdjustmentListener(e->{
			World.AGGRESSIVE_ENVIRONMENT = e.getValue()/100.0;
		});
		panel_3.add(scrollBar_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_2 = new JLabel("Загрязнённость");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(lblNewLabel_2, BorderLayout.NORTH);
		
		scrollBar_1 = new JScrollBar();
		scrollBar_1.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_1.setMinimum(1);
		scrollBar_1.setMaximum(50);
		scrollBar_1.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar_1.addAdjustmentListener(e->{
			World.DIRTY_WATER = e.getValue();
			World.world.recalculate();
		});
		panel_2.add(scrollBar_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_1 = new JLabel("Освещённость");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblNewLabel_1, BorderLayout.NORTH);
		
		scrollBar = new JScrollBar();
		scrollBar.setMinimum(1);
		scrollBar.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar.setMaximum(50);
		scrollBar.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar.addAdjustmentListener(e->{
			World.SUN_POWER = e.getValue();
			World.world.recalculate();
		});
		panel_1.add(scrollBar, BorderLayout.SOUTH);
		panel.setLayout(gl_panel);
	}
	
	public void updateScrols() {
		if(World.isActiv)
			play.setText("Пауза");
		else
			play.setText("Пуск");
		scrollBar_7.setValue(scrollBar_7.getMaximum() - World.msTimeout);
		scrollBar_6.setValue(scrollBar_6.getMaximum() - World.TIK_TO_EXIT + 1);
		scrollBar_5.setValue((int) Math.round(World.CONCENTRATION_MINERAL*10));
		scrollBar_4.setValue((int)  Math.round((1-World.LEVEL_MINERAL)*100));
		scrollBar_2.setValue((int) Math.round(World.AGGRESSIVE_ENVIRONMENT*100));
		scrollBar_1.setValue((int) Math.round(World.DIRTY_WATER));
		scrollBar.setValue((int) Math.round(World.SUN_POWER));
	}

	public void setListener(JComponent scrollPane) {
		listener = scrollPane;
	}
}
