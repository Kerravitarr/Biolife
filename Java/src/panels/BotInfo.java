package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import MapObjects.AliveCell.DNA;
import MapObjects.CellObject;
import MapObjects.CellObject.OBJECT;
import MapObjects.Poison;
import Utils.Utils;
import main.Configurations;
import main.Point.DIRECTION;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotInfo extends JPanel {
	/**Класс описывает одну командную опцию*/
	static class ComandOpt{
		public ComandOpt() {
			this(AliveCell.COUNT_COMAND);
		}
		public ComandOpt(int max_val) {
			maxVal = max_val;
		}

		/**Максимальное значение опции*/
		double maxVal = AliveCell.COUNT_COMAND;

		public String get(AliveCell cell, int val) {
			return "" + Math.round(maxVal * val / AliveCell.COUNT_COMAND);
		}
	}
	/**Класс описывает возможные следующие опции*/
	static class NextCmd{
		/**Количество опций*/
		int count;
		/**Смещение опций относительно ТП*/
		private int offset;

		public NextCmd(int count, int offset) {
			this.count = count;
			this.offset = offset;
		}

		public NextCmd(NextCmd nextComands, int offset) {
			this(nextComands.count,offset);
		}
	}
	/**Описывает ситуацию, когда следующей будет выполняться инстуркция дальше*/
	static class NextAdr extends NextCmd{
		public NextAdr(int offset) {
			super(1, offset);
		}
		
	}
	
	private static ComandOpt RELATIVELY = new ComandOpt(DIRECTION.size()) {
		public String get(AliveCell cell, int val) {
			return DIRECTION.toEnum(Integer.parseInt(super.get(cell, val))+ DIRECTION.toNum(cell.direction)).toString();
		}
	};
	private static ComandOpt ABSOLUTELY = new ComandOpt(DIRECTION.size()) {
		public String get(AliveCell cell, int val) {
			return DIRECTION.toEnum(Integer.parseInt(super.get(cell, val))).toString();
		}
	};
	
	private static NextCmd SEE = new NextCmd(OBJECT.size() - 2,1);
	
	enum CELL_COMMAND{
		CMD1_0("ФТС","Фотосинтез"),
		CMD1_1("-МП","Ням мин"),
		CMD1_2("⊶","Деление"),
		CMD1_3("х-х","Смерть"),
		CMD1_4("+Яд О","Пукнуть О",RELATIVELY),
		CMD1_5("+Яд A","Пукнуть A",ABSOLUTELY),
		
		CMD2_0("♲ О","Повернуться О",RELATIVELY),
		CMD2_1("♲ A","Повернуться A",ABSOLUTELY),
		CMD2_2("⍖ O","Шаг O",RELATIVELY,SEE),
		CMD2_3("⍖ А","Шаг А",ABSOLUTELY,SEE),
		CMD2_4("↟","Ориентация вверх"),
		
		CMD3_0("O_O O","Смотреть О",RELATIVELY,SEE),
		CMD3_1("O_O А","Смотреть А",ABSOLUTELY,SEE),
		CMD3_2("∸","Какая высота",new ComandOpt(Configurations.MAP_CELLS.height),new NextCmd(2,2)),
		CMD3_3("♡∸","Сколько ХП",new ComandOpt(AliveCell.maxHP),new NextCmd(2,2)),
		CMD3_4("♢∸","Сколько МП",new ComandOpt(AliveCell.MAX_MP),new NextCmd(2,2)),
		CMD3_5("∅","Я окружён?",new NextCmd(2,2)),
		CMD3_6("♡🠑","Много солнца?",new NextCmd(2,2)),
		CMD3_7("♢🠑","Есть минералы?",new NextCmd(2,2)),
		CMD3_8("O_O ♡∸","ХП у него ск?",new ComandOpt(AliveCell.maxHP),SEE),
		CMD3_9("O_O ♢∸","ХП у него ск?",new ComandOpt(AliveCell.maxHP),SEE),
		CMD3_10("⋇","Я многокл?",new NextCmd(2,2)),
		CMD3_11("Я стар","Сколько лет?",new ComandOpt(),new NextCmd(2,2)),
		CMD3_12("ДНК ⊡","ДНК защищена?",new ComandOpt(AliveCell.MAX_DNA_WALL),new NextCmd(2,2)),
		
		CMD4_0("⇲ O","Съесть О",RELATIVELY,SEE),
		CMD4_1("⇲ А","Съесть А",ABSOLUTELY,SEE),
		CMD4_2("⭹ O","Кусить О",RELATIVELY,SEE),
		CMD4_3("⭹ А","Кусить А",ABSOLUTELY,SEE),
		CMD4_4("↹ O","Поделиться О",RELATIVELY,SEE),
		CMD4_5("↹ А","Поделиться А",ABSOLUTELY,SEE),
		CMD4_6("⤞ O","Отдать О",RELATIVELY,SEE),
		CMD4_7("⤞ А","Отдать А",ABSOLUTELY,SEE),
		CMD4_8("↭ O","Толкнуть О",RELATIVELY),
		CMD4_9("↭ А","Толкнуть А",ABSOLUTELY),
		
		CMD5_0("ГЕН Х","Подменить ген",new ComandOpt(),new ComandOpt()),
		CMD5_1("ДНК Х","Подменить команду",new ComandOpt()),
		CMD5_2("ДНК ⊡→⊙","Подменить ДНК",new ComandOpt(100)),
		CMD5_3("ДНК ⊡←⊙","Забрать ДНК"),
		CMD5_4("ДНК ⊡++","Укрепить ДНК"),
		CMD5_5("ДНК ⊡⭹","Проломить ДНК"),
		CMD5_6("ЦИКЛ","Цикл",new ComandOpt()),

		CMD6_0("□∪□ O","Присосаться О",RELATIVELY,SEE),
		CMD6_1("□∪□ А","Присосаться А",ABSOLUTELY,SEE),
		CMD6_2("⊶∪□ O","Клон и присос О",RELATIVELY,SEE),
		CMD6_3("⊶∪□ А","Клон и присос А",ABSOLUTELY,SEE),
		
		;
		private static final CELL_COMMAND[] vals = CELL_COMMAND.values();
		
		/**Адрес команды*/
		int cmdNum;
		/**Возможные переходы*/
		private NextCmd commands;
		/**Команды*/
		private List<ComandOpt> params = new ArrayList<>();
		private String shot_name;
		private String long_name;

		CELL_COMMAND(String shot_name, String long_name) {
			this.shot_name=shot_name;
			this.long_name=long_name;
			String[] nums = this.toString().substring(3).split("_");
			int block;
			switch (nums[0]) {
				case "1" :block = AliveCell.block1;	break;
				case "2" :block = AliveCell.block2;	break;
				case "3" :block = AliveCell.block3;	break;
				case "4" :block = AliveCell.block4;	break;
				case "5" :block = AliveCell.block5;	break;
				case "6" :block = AliveCell.block6;	break;
				default :
					throw new IllegalArgumentException(
							"Unexpected value: " + nums[0]);
			}
			cmdNum = block + Integer.parseInt(nums[1]);
			commands = new NextAdr(1);
		}

		CELL_COMMAND(String shot_name, String long_name, ComandOpt comand) {
			this(shot_name,long_name);
			this.params.add(comand);
			commands = new NextAdr(2);
		}

		CELL_COMMAND(String shot_name, String long_name, ComandOpt comand,
				NextCmd nextComands) {
			this(shot_name,long_name,comand);
			commands = new NextCmd(nextComands, 2);
		}

		CELL_COMMAND(String shot_name, String long_name, NextCmd nextComands) {
			this(shot_name,long_name);
			commands = new NextCmd(nextComands, 1);
		}

		CELL_COMMAND(String shot_name, String long_name, ComandOpt comand1,ComandOpt comand2) {
			this(shot_name,long_name);
			this.params.add(comand1);
			this.params.add(comand2);
			commands = new NextAdr(3);
		}

		static CELL_COMMAND get(int cmd) {
			for(CELL_COMMAND cmdS : vals) {
				if(cmdS.cmdNum == cmd)
					return cmdS;
			}
			return null;
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
	private int oldIndex = -1;
	private boolean isFullMod = false;
	private JList<String> list;
	/**Филогинетическое дерево*/
	private JTextField filogen;
	private JTextField pos;
	private JTextField toxicFIeld;
	private JPanel panel_variant;
	private JPanel panel_DNA;
	
	class WorkTask implements Runnable{
		public void run() {
			while(true) {
				if(isVisible() && getCell() != null && !getCell().aliveStatus(AliveCell.LV_STATUS.GHOST)) {
					setDinamicHaracteristiks();
					if((getCell() instanceof AliveCell)) {
						AliveCell lcell = (AliveCell)getCell();
						DefaultListModel<String> model = new DefaultListModel<String> ();
						DNA dna = lcell.getDna();
						model.setSize(dna.size);
						/**Индекс с которого идёт пеерсчёт*/
						int index = dna.getIndex();
						if(index != oldIndex) {
							oldIndex = index;
							int countComands = 0;
							int countAdrs = 0;
							for(int i = 0 ; i < dna.size ; i ++) {
								int cmd = dna.get(index,i);
								int newNumber = (index+i)%dna.size;
								String row = newNumber + " = " +  cmd;//Так как 0 - параметр следующей за тиком команды
								CELL_COMMAND cmdS = CELL_COMMAND.get(cmd);
								if (countComands > 0) {
									row += " - П";
									countComands--;
								} else if(countAdrs > 0){
									row += " - A(" + ((index+i+cmd)%dna.size) + ")";
									countAdrs--;
								} else	if(cmdS == null){
									row += " PC += " + cmd + "(" + ((index+i+cmd)%dna.size) + ")";
								} else {
									row += " - ";
									if(isFullMod)
										row += cmdS.long_name;
									else
										row += cmdS.shot_name;
									if (cmdS.params.size() > 0) {
										row += " ( ";
										countComands = cmdS.params.size();
										for (int j = 0; j < cmdS.params.size(); j++) {
											int val = dna.get(index, i + j + 1);
											ComandOpt param = cmdS.params.get(j);
											if (j != 0)row += " ";
											row += param.get(lcell,val);
										}
										row += ")";
									}
									if (cmdS.commands.count == 1) {
										row += " PC += " + cmdS.commands.offset;
									} else {
										countAdrs = cmdS.commands.count;
										row += " PC += ";
										for (int j = 0; j < cmdS.commands.count; j++) {
											if (j != 0)row += " ";
											row += dna.get(index,i + cmdS.commands.offset + j);
										}
									}
								}
								model.add(i, row);
							}
							list.setModel(model);
						}
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
		
		panel_DNA = new JPanel();
		
		JPanel panel_const = new JPanel();
		panel_const.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "\u041A\u043E\u043D\u0441\u0442\u0430\u043D\u0442\u044B", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		panel_variant = new JPanel();
		panel_variant.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "\u041F\u0435\u0440\u0435\u043C\u0435\u043D\u043D\u044B\u0435", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_DNA, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
						.addComponent(panel_const, GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
						.addComponent(panel_variant, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_const, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_variant, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_DNA, GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		JPanel panel_3 = new JPanel();
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_8 = new JLabel("Покаление:");
		panel_3.add(lblNewLabel_8);
		
		generation = new JTextField();
		generation.setBackground(Color.WHITE);
		generation.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(generation);
		generation.setEnabled(false);
		generation.setEditable(false);
		generation.setColumns(4);
		
		JPanel panel_8 = new JPanel();
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
		
		JPanel panel_9 = new JPanel();
		panel_9.setLayout(new BoxLayout(panel_9, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_6 = new JLabel("Оринетация:");
		panel_9.add(lblNewLabel_6);
		
		direction = new JTextField();
		direction.setBackground(Color.WHITE);
		direction.setHorizontalAlignment(SwingConstants.CENTER);
		panel_9.add(direction);
		direction.setEnabled(false);
		direction.setEditable(false);
		direction.setColumns(7);
		
		JPanel panel_13 = new JPanel();
		panel_13.setLayout(new BoxLayout(panel_13, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_11 = new JLabel("Химзащита:");
		panel_13.add(lblNewLabel_11);
		
		toxicFIeld = new JTextField();
		toxicFIeld.setHorizontalAlignment(SwingConstants.CENTER);
		toxicFIeld.setBackground(Color.WHITE);
		toxicFIeld.setEditable(false);
		toxicFIeld.setEnabled(false);
		panel_13.add(toxicFIeld);
		toxicFIeld.setColumns(15);
		
		JPanel panel_10 = new JPanel();
		panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_2 = new JLabel("Хлорофил:");
		panel_10.add(lblNewLabel_2);
		
		photos = new JTextField();
		photos.setBackground(Color.WHITE);
		photos.setHorizontalAlignment(SwingConstants.CENTER);
		panel_10.add(photos);
		photos.setEnabled(false);
		photos.setEditable(false);
		photos.setColumns(4);
		
		JPanel panel_11 = new JPanel();
		panel_11.setLayout(new BoxLayout(panel_11, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_1 = new JLabel("Фенотип: ");
		panel_11.add(lblNewLabel_1);
		
		phenotype = new JTextField();
		phenotype.setHorizontalAlignment(SwingConstants.CENTER);
		phenotype.setEditable(false);
		phenotype.setEnabled(false);
		panel_11.add(phenotype);
		phenotype.setColumns(1);
		
		JPanel panel_1 = new JPanel();
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel = new JLabel("Филоген:");
		panel_1.add(lblNewLabel);
		
		filogen = new JTextField();
		filogen.setHorizontalAlignment(SwingConstants.RIGHT);
		filogen.setEnabled(false);
		filogen.setBackground(Color.WHITE);
		panel_1.add(filogen);
		filogen.setColumns(10);
		GroupLayout gl_panel_variant = new GroupLayout(panel_variant);
		gl_panel_variant.setHorizontalGroup(
			gl_panel_variant.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addGap(10)
					.addGroup(gl_panel_variant.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panel_variant.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(panel_10, GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
						.addGroup(gl_panel_variant.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panel_variant.createParallelGroup(Alignment.TRAILING)
								.addComponent(panel_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
								.addComponent(panel_11, GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)))
					.addGap(10))
				.addGroup(Alignment.LEADING, gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(Alignment.LEADING, gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_9, GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(Alignment.LEADING, gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_8, GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(Alignment.LEADING, gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_13, GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_panel_variant.setVerticalGroup(
			gl_panel_variant.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addComponent(panel_13, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addGap(7)
					.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(35, Short.MAX_VALUE))
		);
		panel_variant.setLayout(gl_panel_variant);
		
		JPanel panel_5 = new JPanel();
		
		JPanel panel_6 = new JPanel();
		
		JPanel panel_7 = new JPanel();
		
		JPanel panel_12 = new JPanel();
		GroupLayout gl_panel_const = new GroupLayout(panel_const);
		gl_panel_const.setHorizontalGroup(
			gl_panel_const.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_const.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_const.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel_5, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
						.addComponent(panel_6, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
						.addComponent(panel_7, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
						.addComponent(panel_12, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel_const.setVerticalGroup(
			gl_panel_const.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_const.createSequentialGroup()
					.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_12, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(13, Short.MAX_VALUE))
		);
		panel_12.setLayout(new BoxLayout(panel_12, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_10 = new JLabel("Позиция:");
		panel_12.add(lblNewLabel_10);
		
		pos = new JTextField();
		pos.setHorizontalAlignment(SwingConstants.CENTER);
		pos.setBackground(Color.WHITE);
		pos.setEnabled(false);
		pos.setEditable(false);
		panel_12.add(pos);
		pos.setColumns(10);
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
		
		JLabel lblNewLabel_7 = new JLabel("Возраст:");
		panel_5.add(lblNewLabel_7);
		
		age = new JTextField();
		age.setToolTipText("Через черту показывается степень защищённости ДНК");
		age.setHorizontalAlignment(SwingConstants.CENTER);
		age.setBackground(Color.WHITE);
		panel_5.add(age);
		age.setEnabled(false);
		age.setEditable(false);
		age.setColumns(4);
		panel_const.setLayout(gl_panel_const);
		panel_DNA.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_9 = new JLabel("ДНК");
		lblNewLabel_9.setHorizontalAlignment(SwingConstants.CENTER);
		panel_DNA.add(lblNewLabel_9, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_DNA.add(scrollPane, BorderLayout.CENTER);
		
		list = new JList<String>();
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				isFullMod = !isFullMod;
				oldIndex = -1;
			}
		});
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
			panel_variant.setVisible(true);
			panel_DNA.setVisible(true);
			AliveCell new_name = (AliveCell) getCell();
			generation.setText(new_name.getGeneration()+"");
			photos.setText((new_name.photosynthesisEffect+"").substring(0, 3));
			phenotype.setBackground(new_name.phenotype);
			phenotype.setText(Integer.toHexString(new_name.phenotype.getRGB()));
			filogen.setText(new_name.getBranch());
		} else if (getCell() instanceof Poison){
			panel_variant.setVisible(true);
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
			toxicFIeld.setText(new_name.getPosionType() + ":" + new_name.getPosionPower());
		} else if (getCell() instanceof Poison) {
			Poison new_name = (Poison) getCell();
			hp.setText(getCell().getHealth() + "");
			toxicFIeld.setText(new_name.type + "");
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
		toxicFIeld.setText("");
		list.setModel(new DefaultListModel<>());
		panel_variant.setVisible(false);
		panel_DNA.setVisible(false);
		oldIndex = -1;
		isFullMod = false;
	}


	/**
	 * @return the cell
	 */
	public CellObject getCell() {
		return cell;
	}
}
