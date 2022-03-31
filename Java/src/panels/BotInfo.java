package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
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
import MapObjects.CellObject.OBJECT;
import MapObjects.Poison;
import MapObjects.dna.CommandDNA;
import MapObjects.dna.CommandDo;
import MapObjects.dna.CommandExplore;
import MapObjects.dna.CommandList;
import MapObjects.dna.DNA;
import Utils.Utils;
import main.Configurations;

public class BotInfo extends JPanel {
	
	private static final Color RED = new Color(255, 0, 0, 10);
	private static final Color BLUE = new Color(0, 0, 255, 10);
	public static final Color YELLOW = new Color(255, 255, 0, 10);
	public static final Color ARG = new Color(200, 200, 200, 10);
	public static final Color PAR = new Color(255, 255, 255, 10);
	
	private class JListRow {
		enum TYPE{
			CMD,PARAM,ARG
		}
		/**Команда, которая выполняется. Или Главная команда, если тут её параметр*/
		private CommandDNA command;
		/**Порядковый номер гена в ДНК*/
		private int number;
		/**Числовое значение гена в ДНК*/
		private int value;
		/**Длина ДНК*/
		private int size;
		private Color color;
		/**Тип строки*/
		private TYPE type = TYPE.CMD;
		
		public JListRow(int number, int value,int size, CommandDNA cmd_o) {
			this.number=number;
			this.value=value;
			this.size=size;
			command = cmd_o;
			if(command instanceof CommandDo)
				color = RED;
			else if(command instanceof CommandExplore)
				color = YELLOW;
			else
				color = BLUE;
		}

		public String getText() {
			StringBuilder sb = new StringBuilder();
			sb.append(number);
			sb.append("=");
			sb.append(value);
			sb.append(" ");
			switch (type) {
				case PARAM-> {
					sb.append("П");
					sb.append(command.getParam(value));
				}
				case ARG->{
					 sb.append("А(");
					 sb.append((value)%size);
					 sb.append(")");
				}
				case CMD->sb.append(command.toString(isFullMod));
			}
			return sb.toString();
		}

		public Color getColor() {
			return color;
		}

		public void setType(TYPE type) {
			this.type = type;
			switch (type) {
				case ARG->color = ARG;
				case PARAM->color = PAR;
				default ->{}
			}
		}
	}
	
	private static class JlistRender extends DefaultListCellRenderer{
		 @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if(value == null) return c;
              if (value instanceof JListRow) {
            	  JListRow nextRow = (JListRow) value;
                   setText(nextRow.getText());
                   setBackground(nextRow.getColor());
                   if (isSelected) {
                        setBackground(getBackground().darker());
                   }
              } else {
            	  throw new IllegalArgumentException("Список параметров содержит странные параметры. Это вообще что?! " + value.getClass());
              }
              return c;
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
	private JList<JListRow> listDNA;
	/**Филогинетическое дерево*/
	private TextPair filogen;
	private TextPair pos;
	private TextPair toxicFIeld;
	private TextPair Buoyancy;
	private JPanel panel_variant;
	/**Панель, на которой написано про ДНК*/
	private JPanel panel_DNA;
	/**Список с прерываниями*/
	private JList<String> list_inter;
	private JScrollPane scrollPane_inter;
	
	class WorkTask implements Runnable{
		public void run() {
                    Thread.currentThread().setName("BotInfo");
			while(true) {
				if(isVisible() && getCell() != null && !getCell().aliveStatus(AliveCell.LV_STATUS.GHOST)) {
					setDinamicHaracteristiks();
					if((getCell() instanceof AliveCell)) {
						AliveCell lcell = (AliveCell)getCell();
						DefaultListModel<JListRow> model = new DefaultListModel<>();
						DNA dna = lcell.getDna();
						model.setSize(dna.size);
						/**Индекс с которого идёт пеерсчёт*/
						int index = dna.getIndex();
						CommandDNA mainCMD = null;
						if(index != oldIndex) {
							oldIndex = index;
							int countComands = 0;
							int countAdrs = 0;
							for(int i = 0 ; i < dna.size ; i ++) {
								int cmd = dna.get(index,i);
								int newNumber = (index+i)%dna.size;
								var cmd_o = CommandList.list[cmd];
								JListRow obj_row = new JListRow(newNumber,cmd,dna.size,cmd_o);
								if (countComands > 0) {
									obj_row.setType(JListRow.TYPE.PARAM);
									obj_row.command = mainCMD;
									countComands--;
								} else if(countAdrs > 0){
									obj_row.setType(JListRow.TYPE.ARG);
									obj_row.command = mainCMD;
									countAdrs--;
								}else {
									mainCMD = cmd_o;
									countComands = cmd_o.getCountParams();
									countAdrs = cmd_o.getCountBranch();
								}
								model.add(i, obj_row);
							}
							listDNA.setModel(model);
						}
						
						
						
					}
					Utils.pause_ms(100);
				} else {
					if(cell != null) {
						cell = null;
						clearText();

						listDNA.setModel(new DefaultListModel<> ());
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
		
		scrollPane_inter = new JScrollPane();
		scrollPane_inter.setEnabled(false);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_DNA, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
						.addComponent(scrollPane_inter, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
						.addComponent(panel_const, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
						.addComponent(panel_variant, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(panel_const, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(panel_variant, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane_inter, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_DNA, GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		JLabel lblNewLabel = new JLabel("Прерывания");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane_inter.setColumnHeaderView(lblNewLabel);
		
		list_inter = new JList<String>();
		list_inter.setEnabled(false);
		list_inter.setModel(new DefaultListModel<String> ());
		scrollPane_inter.setViewportView(list_inter);
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
		
		listDNA = new JList<JListRow>();
		listDNA.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				isFullMod = !isFullMod;
				oldIndex = -1;
			}
		});
		listDNA.setCellRenderer(new JlistRender());
		listDNA.setVisibleRowCount(3);
		listDNA.setEnabled(false);
		scrollPane.setViewportView(listDNA);
		listDNA.setModel(new DefaultListModel<> ());
		listDNA.setSelectedIndex(0);
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
			scrollPane_inter.setVisible(true);
			AliveCell new_name = (AliveCell) getCell();
			generation.setText(new_name.getGeneration()+"");
			photos.setText((new_name.photosynthesisEffect+"").substring(0, 3));
			phenotype.setBackground(new_name.phenotype);
			phenotype.setText(Integer.toHexString(new_name.phenotype.getRGB()));
			filogen.setText(new_name.getBranch());
			
			DefaultListModel<String> model = new DefaultListModel<String> ();
			DNA dna = new_name.getDna();
			model.setSize(dna.interrupts.length);
			for(int i = 0 ; i < dna.interrupts.length ; i++)
				model.add(i,OBJECT.get(i) + " - " + String.valueOf(dna.interrupts[i]));
			list_inter.setModel(model);
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
		listDNA.setModel(new DefaultListModel<>());
		panel_variant.setVisible(false);
		panel_DNA.setVisible(false);
		scrollPane_inter.setVisible(false);
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
