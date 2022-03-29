package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
		CMD1_6("Zzz","Уснуть"),
		CMD1_7("☁","Стать легче"),
		CMD1_8("◼","Стать тяжелее"),
		
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
	
	
	private static class TextPair extends JPanel {
		/**Текст пары */
		private JLabel text = null;
		/**Текстовое поле пары*/
		private JTextField field = null;
		
		public TextPair(String label) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			text = new JLabel(label);
			add(text);
			
			field = new JTextField();
			field.setBackground(Color.WHITE);
			field.setHorizontalAlignment(SwingConstants.CENTER);
			field.setEnabled(false);
			field.setEditable(false);
			add(field);
		}

		public void clear() {
			setText("");
		}

		public void setText(String string) {
			field.setText(string);
		}

		public void setBackground(Color bg) {
			super.setBackground(bg);
			if(text != null && field != null) {
				text.setBackground(bg);
				field.setBackground(bg);
			}
		}
	}
	
	private TextPair photos;
	private TextPair state;
	private TextPair hp;
	private TextPair mp;
	private TextPair direction;
	private TextPair age;
	private TextPair generation;
	private TextPair phenotype;
	private CellObject cell = null;
	private int oldIndex = -1;
	private boolean isFullMod = false;
	private JList<String> list;
	/**Филогинетическое дерево*/
	private TextPair filogen;
	private TextPair pos;
	private TextPair toxicFIeld;
	private TextPair Buoyancy;
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
						.addComponent(panel_variant, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
						.addComponent(panel_DNA, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
						.addComponent(panel_const, GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_const, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(panel_variant, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_DNA, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
					.addGap(9))
		);
		generation = new TextPair("Покаление:");
		mp = new TextPair("Минералов:");
		direction = new TextPair("Оринетация:");
		toxicFIeld = new TextPair("Химзащита:");
		photos = new TextPair("Хлорофил:");
		phenotype = new TextPair("Фенотип:");
		filogen = new TextPair("Филоген:");
		Buoyancy = new TextPair("Плавучесть:");
		GroupLayout gl_panel_variant = new GroupLayout(panel_variant);
		gl_panel_variant.setHorizontalGroup(
			gl_panel_variant.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(generation, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(direction, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(mp, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(toxicFIeld, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(phenotype, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(photos, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(filogen, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addContainerGap()
					.addComponent(Buoyancy, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_panel_variant.setVerticalGroup(
			gl_panel_variant.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_variant.createSequentialGroup()
					.addComponent(toxicFIeld, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(mp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(direction, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(generation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(photos, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(phenotype, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(filogen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(Buoyancy, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(20, Short.MAX_VALUE))
		);
		
		panel_variant.setLayout(gl_panel_variant);

		pos = new TextPair("Позиция:");
		hp = new TextPair("Здоровье:");
		state = new TextPair("Состояние:");
		age = new TextPair("Возраст:");
		age.setToolTipText("Через черту показывается степень защищённости ДНК");
		
		
		GroupLayout gl_panel_const = new GroupLayout(panel_const);
		gl_panel_const.setHorizontalGroup(
			gl_panel_const.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_const.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_const.createParallelGroup(Alignment.TRAILING)
						.addComponent(age, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
						.addComponent(state, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
						.addComponent(hp, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
						.addComponent(pos, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel_const.setVerticalGroup(
			gl_panel_const.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_const.createSequentialGroup()
					.addComponent(age, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(state, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(hp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(pos, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(20, Short.MAX_VALUE))
		);
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
		age.setText(String.valueOf(getCell().getAge()));
		if (getCell() instanceof AliveCell) {
			AliveCell new_name = (AliveCell) getCell();
            mp.setText(String.valueOf(new_name.getMineral()));
			direction.setText(new_name.direction.name());
			hp.setText(((int)getCell().getHealth())+"+" + Math.round(Configurations.sun.getEnergy(new_name.getPos())+(1+new_name.photosynthesisEffect) * new_name.getMineral() / AliveCell.MAX_MP)+"\\" + new_name.getDNA_wall());
			double realLv = new_name.getPos().getY() - (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL);
        	double dist = Configurations.MAP_CELLS.height * (1 - Configurations.LEVEL_MINERAL);
			mp.setText(new_name.getMineral()+"+" + Math.round(Configurations.CONCENTRATION_MINERAL * (realLv/dist) * (5 - new_name.photosynthesisEffect)));
			toxicFIeld.setText(new_name.getPosionType() + ":" + new_name.getPosionPower());
			Buoyancy.setText(String.valueOf(new_name.getBuoyancy()));
		} else if (getCell() instanceof Poison) {
			Poison new_name = (Poison) getCell();
			hp.setText(String.valueOf((int)getCell().getHealth()));
			toxicFIeld.setText(new_name.type.name());
		} else {
			hp.setText(String.valueOf((int)getCell().getHealth()));
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
		Buoyancy.clear();
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
