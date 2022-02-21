package panels;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.LayoutStyle.ComponentPlacement;

import main.World;

import javax.swing.JButton;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Settings extends JPanel {

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
		
		JButton btnNewButton = new JButton("Пауза");
		btnNewButton.addActionListener(e->{
			if(btnNewButton.getText().equals("Пауза")) {
				btnNewButton.setText("Пуск");
				World.isActiv = false;
			} else {
				btnNewButton.setText("Пауза");
				World.isActiv = true;
			}
		});
		
		JPanel panel_4 = new JPanel();
		
		JPanel panel_5 = new JPanel();
		
		JPanel panel_6 = new JPanel();
		
		JButton btnNewButton_1 = new JButton("Сохранить мир");
		
		JButton btnNewButton_2 = new JButton("Загрузить мир");
		
		JButton btnNewButton_3 = new JButton("Шаг");
		btnNewButton_3.addActionListener(e-> World.world.step());
		
		JPanel panel_4_1 = new JPanel();
		panel_4_1.setLayout(new BorderLayout(0, 0));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_6, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_5, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_3, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_4, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(btnNewButton_1, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(btnNewButton_2, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(btnNewButton_3, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addComponent(panel_4_1, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE))
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
					.addPreferredGap(ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
					.addComponent(btnNewButton_3)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton_2)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton_1)
					.addContainerGap())
		);
		
		JLabel lblNewLabel_7 = new JLabel("Скорость разложения");
		lblNewLabel_7.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4_1.add(lblNewLabel_7, BorderLayout.NORTH);
		
		JScrollBar scrollBar_6 = new JScrollBar();
		scrollBar_6.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_6.setValue(10);
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
		
		JScrollBar scrollBar_5 = new JScrollBar();
		scrollBar_5.setMaximum(40);
		scrollBar_5.setBlockIncrement(5);
		scrollBar_5.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_5.setValue(10);
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
		
		JScrollBar scrollBar_4 = new JScrollBar();
		scrollBar_4.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_4.setValue(50);
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
		
		JScrollBar scrollBar_3 = new JScrollBar();
		scrollBar_3.setMinimum(1);
		scrollBar_3.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_3.setValue(10);
		scrollBar_3.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar_3.addAdjustmentListener(e->{
			World.FPS_TIC = e.getValue();
		});
		panel_4.add(scrollBar_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_3 = new JLabel("Мутагенность");
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(lblNewLabel_3, BorderLayout.NORTH);
		
		JScrollBar scrollBar_2 = new JScrollBar();
		scrollBar_2.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_2.setValue(25);
		scrollBar_2.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar_2.addAdjustmentListener(e->{
			World.AGGRESSIVE_ENVIRONMENT = e.getValue()/100.0;
		});
		panel_3.add(scrollBar_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_2 = new JLabel("Загрязнённость");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(lblNewLabel_2, BorderLayout.NORTH);
		
		JScrollBar scrollBar_1 = new JScrollBar();
		scrollBar_1.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar_1.setMaximum(50);
		scrollBar_1.setValue(17);
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
		
		JScrollBar scrollBar = new JScrollBar();
		scrollBar.setVisibleAmount (0); // Значение экстента равно 0
		scrollBar.setMaximum(50);
		scrollBar.setValue(10);
		scrollBar.setOrientation(JScrollBar.HORIZONTAL);
		scrollBar.addAdjustmentListener(e->{
			World.SUN_POWER = e.getValue();
			World.world.recalculate();
		});
		panel_1.add(scrollBar, BorderLayout.SOUTH);
		panel.setLayout(gl_panel);

	}
}
