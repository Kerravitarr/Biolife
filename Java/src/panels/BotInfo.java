package panels;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import main.Cell;

public class BotInfo extends JPanel {
	enum CELL_COMMAND{
		PHOT(0),MIN_TO_EN(Cell.block1),CLONE(Cell.block1+1),
		DNA_PROG(Cell.block5,2),DNA_CRASH(Cell.block5+1,2),
		NEXT_DIR_A(Cell.block2,1),NEXT_DIR_R(Cell.block2+1,1),STEP_A(Cell.block2+2,1,Cell.OBJECT.size()-1),STEP_R(Cell.block2+3,1,Cell.OBJECT.size()-1),
		SEE_A(Cell.block3,1),SEE_R(Cell.block3+1,1),H_LV(Cell.block3+2,1,2),HP_LV(Cell.block3+3,1,2),MP_LV(Cell.block3+4,1,2),WHO_NEAR(Cell.block3+5,0,2),MAKE_PH(Cell.block3+6,0,2),MAKE_MIN(Cell.block3+7,0,2),HP_NEAR(Cell.block3+8,1,2+Cell.OBJECT.size()),MP_NEAR(Cell.block3+9,1,2+Cell.OBJECT.size()),
		EAT_A(Cell.block4,1,1+Cell.OBJECT.size()-3),EAT_R(Cell.block4+1,1,1+Cell.OBJECT.size()-3),BITE_A(Cell.block4+2,1,1+Cell.OBJECT.size()-2),BITE_R(Cell.block4+3,1,1+Cell.OBJECT.size()-2),
			CARE_A(Cell.block4+4,1,1+Cell.OBJECT.size()-2),CARE_R(Cell.block4+5,1,1+Cell.OBJECT.size()-2),GIVE_A(Cell.block4+6,1,1+Cell.OBJECT.size()-2),GIVE_R(Cell.block4+7,1,1+Cell.OBJECT.size()-2),
		;
		private static final CELL_COMMAND[] myEnumValues = CELL_COMMAND.values();
		
		int cmdNum;
		int cmdParamsCount;
		int cmdCountAns;

		CELL_COMMAND(int num) {this(num,0);	}

		CELL_COMMAND(int num, int countParams) {this(num,countParams,1);}

		CELL_COMMAND(int num, int countParams, int countAdsver) {
			cmdNum=num;
			cmdParamsCount=countParams;
			cmdCountAns=countAdsver;
		}
	}
	
	
	private JTextField photos;
	private JTextField state;
	private JTextField hp;
	private JTextField mp;
	private JTextField direction;
	private JTextField age;
	private JTextField generation;
	private JTextField phenotype;
	private Cell cell = null;
	private JList<String> list;
	static class Model extends DefaultListModel<String> {}
	
	class WorkTask implements Runnable{
		public void run() {
			while(true) {
				if(isVisible() && cell != null) {
					generation.setText(cell.getGeneration()+"");
					age.setText(cell.getAge()+"");
					state.setText(cell.alive.name());
					hp.setText(cell.getHealth()+"");
					mp.setText(cell.getMineral()+"");
					direction.setText(cell.direction.name());
					photos.setText(cell.photosynthesisEffect+"");
					phenotype.setBackground(cell.phenotype);
					
					DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
					model.removeAllElements();
					int processorTik = cell.getProcessorTik();
					for(int i = 0 ; i < Cell.MINDE_SIZE ; i ++) {
						int cmd = cell.getCmdA(processorTik+i);
						String row = (processorTik+i) + " = " +  cmd;//Так как 0 - параметр следующей за тиком команды
						for(CELL_COMMAND cmdS : CELL_COMMAND.myEnumValues) {
							if(cmdS.cmdNum == cmd) {
								row += " - " + cmdS.name() + " (" + cmdS.cmdParamsCount;
								if(cmdS.cmdParamsCount > 0) {
									row += " -" ;
									for (int j = 0; j < cmdS.cmdParamsCount; j++) {
										row += " " + cell.getCmdA(processorTik + i +1+ j);
									}
								}
								row += ")";
								if(cmdS.cmdCountAns == 1) {
									row += " PC += 1";
								}else if(cmdS.cmdCountAns > 0) {
									row += " PC +=";
									for (int j = 0; j < cmdS.cmdCountAns; j++) {
										row += " " + cell.getCmdA(processorTik + i +1+cmdS.cmdParamsCount+ j);
									}
								}
								break;
							}
						}
						model.add(i,row);
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					cell = null;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Create the panel.
	 */
	public BotInfo() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		
		JPanel panel_2 = new JPanel();
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "\u041A\u043E\u043D\u0441\u0442\u0430\u043D\u0442\u044B", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(17, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 240, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		JPanel panel_3 = new JPanel();
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_8 = new JLabel("Покаление");
		panel_3.add(lblNewLabel_8);
		
		generation = new JTextField();
		generation.setBackground(Color.WHITE);
		generation.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(generation);
		generation.setEnabled(false);
		generation.setEditable(false);
		generation.setColumns(4);
		
		JPanel panel_5 = new JPanel();
		
		JPanel panel_6 = new JPanel();
		
		JPanel panel_7 = new JPanel();
		
		JPanel panel_8 = new JPanel();
		
		JPanel panel_9 = new JPanel();
		
		JPanel panel_10 = new JPanel();
		
		JPanel panel_11 = new JPanel();
		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
		gl_panel_4.setHorizontalGroup(
			gl_panel_4.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_4.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_4.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel_11, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_10, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_9, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_8, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_7, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_6, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addGroup(Alignment.LEADING, gl_panel_4.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(panel_5, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(panel_3, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)))
					.addGap(59))
		);
		gl_panel_4.setVerticalGroup(
			gl_panel_4.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_4.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(36, Short.MAX_VALUE))
		);
		panel_11.setLayout(new BoxLayout(panel_11, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_1 = new JLabel("Фенотип: ");
		panel_11.add(lblNewLabel_1);
		
		phenotype = new JTextField();
		phenotype.setEditable(false);
		phenotype.setEnabled(false);
		panel_11.add(phenotype);
		phenotype.setColumns(1);
		panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_2 = new JLabel("Фотосинтетичность");
		panel_10.add(lblNewLabel_2);
		
		photos = new JTextField();
		photos.setBackground(Color.WHITE);
		photos.setHorizontalAlignment(SwingConstants.CENTER);
		panel_10.add(photos);
		photos.setEnabled(false);
		photos.setEditable(false);
		photos.setColumns(4);
		panel_9.setLayout(new BoxLayout(panel_9, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_6 = new JLabel("Направление:");
		panel_9.add(lblNewLabel_6);
		
		direction = new JTextField();
		direction.setBackground(Color.WHITE);
		direction.setHorizontalAlignment(SwingConstants.CENTER);
		panel_9.add(direction);
		direction.setEnabled(false);
		direction.setEditable(false);
		direction.setColumns(4);
		panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_5 = new JLabel("Минералов:");
		panel_8.add(lblNewLabel_5);
		
		mp = new JTextField();
		mp.setBackground(Color.WHITE);
		mp.setHorizontalAlignment(SwingConstants.CENTER);
		panel_8.add(mp);
		mp.setEnabled(false);
		mp.setEditable(false);
		mp.setColumns(4);
		panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_4 = new JLabel("Здоровье:");
		panel_7.add(lblNewLabel_4);
		
		hp = new JTextField();
		hp.setBackground(Color.WHITE);
		hp.setHorizontalAlignment(SwingConstants.CENTER);
		panel_7.add(hp);
		hp.setEnabled(false);
		hp.setEditable(false);
		hp.setColumns(4);
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_3 = new JLabel("Состояние:");
		panel_6.add(lblNewLabel_3);
		
		state = new JTextField();
		state.setBackground(Color.WHITE);
		state.setHorizontalAlignment(SwingConstants.CENTER);
		panel_6.add(state);
		state.setEnabled(false);
		state.setEditable(false);
		state.setColumns(4);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_7 = new JLabel("Возраст");
		panel_5.add(lblNewLabel_7);
		
		age = new JTextField();
		age.setHorizontalAlignment(SwingConstants.CENTER);
		age.setBackground(Color.WHITE);
		panel_5.add(age);
		age.setEnabled(false);
		age.setEditable(false);
		age.setColumns(4);
		panel_4.setLayout(gl_panel_4);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_9 = new JLabel("ДНК");
		lblNewLabel_9.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(lblNewLabel_9, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_2.add(scrollPane, BorderLayout.CENTER);
		
		list = new JList<String>();
		list.setVisibleRowCount(3);
		list.setEnabled(false);
		scrollPane.setViewportView(list);
		list.setModel(new Model());
		list.setSelectedIndex(0);
		panel.setLayout(gl_panel);
		
		new Thread(new WorkTask()).start();
	}
	
	
	public void setCell(Cell cell) {
		this.cell=cell;
	}
}
