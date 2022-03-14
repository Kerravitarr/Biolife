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

import MapObjects.AliveCell;
import MapObjects.CellObject;
import Utils.Utils;
import main.Configurations;

public class BotInfo extends JPanel {
	enum CELL_COMMAND{
		PHOT(AliveCell.block1),MIN_TO_EN(AliveCell.block1+1),CLONE(AliveCell.block1+2),
		DNA_PROG(AliveCell.block5,2),DNA_CRASH_A(AliveCell.block5+1,2),DNA_COPY(AliveCell.block5+2,1),DNA_CRASH_O(AliveCell.block5+3,1),DNA_WALL(AliveCell.block5+4),WALL_BIT(AliveCell.block5+5),LOOP(AliveCell.block5+6,1),
		N_DIR_A(AliveCell.block2,1),N_DIR_R(AliveCell.block2+1,1),STEP_A(AliveCell.block2+2,1,AliveCell.OBJECT.size()-2),STEP_R(AliveCell.block2+3,1,AliveCell.OBJECT.size()-2),DIR_UP(AliveCell.block2+4),
		SEE_A(AliveCell.block3,1,AliveCell.OBJECT.size()-1),SEE_R(AliveCell.block3+1,1,AliveCell.OBJECT.size()-1),H_LV(AliveCell.block3+2,1,2),HP_LV(AliveCell.block3+3,1,2),MP_LV(AliveCell.block3+4,1,2),WHO_NEAR(AliveCell.block3+5,0,2),
			CAN_PH(AliveCell.block3+6,0,2),CAN_MIN(AliveCell.block3+7,0,2),HP_NEAR(AliveCell.block3+8,1,2+AliveCell.OBJECT.size()-3),MP_NEAR(AliveCell.block3+9,1,2+AliveCell.OBJECT.size()-3),I_MANY(AliveCell.block3+10,0,2),HOW_OLD(AliveCell.block3+11,1,2),HOW_DNA_W(AliveCell.block3+12,1,2),
		EAT_A(AliveCell.block4,1,1+AliveCell.OBJECT.size()-4),EAT_R(AliveCell.block4+1,1,1+AliveCell.OBJECT.size()-4),BITE_A(AliveCell.block4+2,1,1+AliveCell.OBJECT.size()-3),BITE_R(AliveCell.block4+3,1,1+AliveCell.OBJECT.size()-3),
		CARE_A(AliveCell.block4+4,1,1+AliveCell.OBJECT.size()-32),CARE_R(AliveCell.block4+5,1,1+AliveCell.OBJECT.size()-3),GIVE_A(AliveCell.block4+6,1,1+AliveCell.OBJECT.size()-3),GIVE_R(AliveCell.block4+7,1,1+AliveCell.OBJECT.size()-3),
		CLING_R(AliveCell.block6,1),CLING_A(AliveCell.block6+1,1),CLONE_R(AliveCell.block6+2,1),CLONE_A(AliveCell.block6+3,1),
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
	private CellObject cell = null;
	private JList<String> list;
	/**Филогинетическое дерево*/
	private JTextField filogen;
	private JTextField pos;
	
	class WorkTask implements Runnable{
		public void run() {
			while(true) {
				if(isVisible() && getCell() != null && !getCell().aliveStatus(AliveCell.LV_STATUS.GHOST)) {
					setDinamicHaracteristiks();
					if((getCell() instanceof AliveCell)) {
						AliveCell lcell = (AliveCell)getCell();
						DefaultListModel<String> model = new DefaultListModel<String> ();
						model.removeAllElements();
						int processorTik = lcell.getProcessorTik();
						for(int i = 0 ; i < lcell.mindLength() ; i ++) {
							int cmd = lcell.getCmdA(processorTik+i);
							int newNumber = (processorTik+i);
							 while (newNumber >= lcell.mindLength())
								 newNumber = newNumber - lcell.mindLength();
							String row = newNumber + " = " +  cmd;//Так как 0 - параметр следующей за тиком команды
							for(CELL_COMMAND cmdS : CELL_COMMAND.myEnumValues) {
								if(cmdS.cmdNum == cmd) {
									row += " - " + cmdS.name();
									if(cmdS.cmdParamsCount > 0) {
										row += " (" + cmdS.cmdParamsCount + " -";
										for (int j = 0; j < cmdS.cmdParamsCount; j++) {
											row += " " + lcell.getCmdA(processorTik + i +1+ j);
										}
										row += ")";
									}
									if(cmdS.cmdCountAns == 1) {
										row += " PC += 1";
									}else if(cmdS.cmdCountAns > 0) {
										row += " PC +=";
										for (int j = 0; j < cmdS.cmdCountAns; j++) {
											row += " " + lcell.getCmdA(processorTik + i +1+cmdS.cmdParamsCount+ j);
										}
									}
									break;
								}
							}
							model.add(i,row);
						}
						list.setModel(model);
					}
					Utils.pause_ms(100);
				} else {
					if(cell != null) {
						cell = null;
						clearText();

						list.setModel(new DefaultListModel<String> ());
					}
					Utils.pause(1);
				}
			}
		}
	}

	/**
	 * Create the panel.
	 */
	public BotInfo() {
		Configurations.info = this;
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
					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 299, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
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
		
		JPanel panel_1 = new JPanel();
		
		JPanel panel_12 = new JPanel();
		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
		gl_panel_4.setHorizontalGroup(
			gl_panel_4.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_4.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_12, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_11, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_10, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_9, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_8, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_7, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(panel_6, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addGroup(gl_panel_4.createParallelGroup(Alignment.TRAILING, false)
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
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_12, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(24, Short.MAX_VALUE))
		);
		panel_12.setLayout(new BoxLayout(panel_12, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_10 = new JLabel("Позиция:");
		panel_12.add(lblNewLabel_10);
		
		pos = new JTextField();
		pos.setBackground(Color.WHITE);
		pos.setEnabled(false);
		pos.setEditable(false);
		panel_12.add(pos);
		pos.setColumns(10);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel = new JLabel("Филоген:");
		panel_1.add(lblNewLabel);
		
		filogen = new JTextField();
		filogen.setEnabled(false);
		filogen.setBackground(Color.WHITE);
		panel_1.add(filogen);
		filogen.setColumns(10);
		panel_11.setLayout(new BoxLayout(panel_11, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_1 = new JLabel("Фенотип: ");
		panel_11.add(lblNewLabel_1);
		
		phenotype = new JTextField();
		phenotype.setHorizontalAlignment(SwingConstants.CENTER);
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
		age.setToolTipText("Через черту показывается степень защищённости ДНК");
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
		list.setModel(new DefaultListModel<String> ());
		list.setSelectedIndex(0);
		panel.setLayout(gl_panel);
		
		new Thread(new WorkTask()).start();
	}
	
	
	public void setCell(CellObject cellObject) {
		clearText();
		this.cell=cellObject;
		if(cellObject == null)
			return;

		setDinamicHaracteristiks();
		if (getCell() instanceof AliveCell) {
			AliveCell new_name = (AliveCell) getCell();
			generation.setText(new_name.getGeneration()+"");
			photos.setText((new_name.photosynthesisEffect+"").substring(0, 3));
			phenotype.setBackground(new_name.phenotype);
			phenotype.setText(Integer.toHexString(new_name.phenotype.getRGB()));
			filogen.setText(new_name.getBranch());
		}
	}

	private void setDinamicHaracteristiks() {
		pos.setText(cell.getPos().toString());
		state.setText(getCell().alive.name());
		age.setText(getCell().getAge()+"");
		if (getCell() instanceof AliveCell) {
			AliveCell new_name = (AliveCell) getCell();
            mp.setText(new_name.getMineral()+"");
			direction.setText(new_name.direction.name());
			hp.setText(getCell().getHealth()+"+" + Math.round(Configurations.sun.getEnergy(new_name.getPos())+(1+new_name.photosynthesisEffect) * new_name.getMineral() / AliveCell.MAX_MP)+"\\" + new_name.getDNA_wall());
			double realLv = new_name.getPos().y - (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL);
        	double dist = Configurations.MAP_CELLS.height * (1 - Configurations.LEVEL_MINERAL);
			mp.setText(new_name.getMineral()+"+" + Math.round(Configurations.CONCENTRATION_MINERAL * (realLv/dist) * (5 - new_name.photosynthesisEffect)));
		} else {
			hp.setText(getCell().getHealth() + "");
		}
	}
	private void clearText() {
		generation.setText("");
		age.setText("");
		state.setText("");
		hp.setText("");
		mp.setText("");
		direction.setText("");
		photos.setText("");
		phenotype.setText("");
		filogen.setText("");
		pos.setText("");
	}


	/**
	 * @return the cell
	 */
	public CellObject getCell() {
		return cell;
	}
}
