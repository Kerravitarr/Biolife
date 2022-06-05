package panels;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.OBJECT;
import MapObjects.Poison;
import MapObjects.dna.CommandDNA;
import MapObjects.dna.DNA;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;
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
import main.Configurations;

public class BotInfo extends JPanel {
	
	private static final Color RED = new Color(255, 0, 0, 30);
	private static final Color RED2 = new Color(255, 0, 0, 60);
	private static final Color BLUE = new Color(0, 0, 255, 30);
	public static final Color YELLOW = new Color(255, 255, 0, 30);
	public static final Color ARG = new Color(100, 100, 100, 30);
	public static final Color PAR = new Color(255, 255, 255, 30);
	
	/**Одна строка в таблице*/
	private class JListRow {
		enum TYPE{
			CMD_I,CMD_D,CMD,PARAM,ARG
		}
		private final Color color;
		private final String text;
		
		public JListRow(String text,TYPE type) {
			switch (type) {
				case CMD_I -> color = YELLOW;
				case CMD_D -> color = RED;
				case CMD -> color = BLUE;
				case PARAM -> color = PAR;
				case ARG-> color = ARG;
				default -> color = null;
			}
			this.text=text;
		}
	}
	/**Отрисовщик строк таблицы. Очень помогает каждой строке давать свой цвет*/
	private static class JlistRender extends DefaultListCellRenderer{
		 @Override
         public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if(value == null) return c;
              if (value instanceof JListRow nextRow) {
                   setText(nextRow.text);
                   setBackground(nextRow.color);
                   if (isSelected) {
                        setBackground(getBackground().darker());
                   }
              } else {
            	  throw new IllegalArgumentException("Список параметров содержит странные параметры. Это вообще что?! " + value.getClass());
              }
              return c;
         }
	}
	/**Пара подписи и текстового поля*/
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

		@Override
		public void setBackground(Color bg) {
			super.setBackground(bg);
			if(text != null && field != null) {
				text.setBackground(bg);
				field.setBackground(bg);
			}
		}
	}
	/**Клетка-затычка*/
	private class TextAL extends AliveCell{
		DNA interDNA = null;
		private TextAL(AliveCell cell) {
			super(cell,cell.getPos());
			evolutionNode.remove();//Мы ложные
			cell.addHealth(getHealth());//Возвращаем здоровье
			addHealth(getHealth());
			cell.setMineral(getMineral()*2);
			setMineral(getMineral()*2);
			direction = cell.direction; // И направление
		}
		
		@Override
		protected void mutation(){};
		/**Выполняет следующую инструкцию */
		private void next() {
			var cmd = getDna().get();
			if(!cmd.isDoing()){
				cmd.execute(this);
				printDNA(this); 
			}else if(cmd.isInterrupt() && cmd.getInterrupt(this, getDna()) != -1){
				var dna = getDna();
				if (interDNA != null) return;
				interDNA = new DNA(dna); // Копируем для прерывания
				int newIndex = interDNA.interrupts[cmd.getInterrupt(this, getDna())];
				while(interDNA.getIndex() != newIndex) //Топорное решение, но первое что пришло в голову
					interDNA.next(1);
				printDNA(this); 
			}
		}
		
		@Override
		public DNA getDna(){
			if(interDNA == null) return super.getDna();
			else return interDNA;
		}
	}
	
	private final TextPair photos;
	private final TextPair state;
	private final TextPair hp;
	private final TextPair mp;
	private final TextPair direction;
	private final TextPair age;
	private final TextPair generation;
	private final TextPair phenotype;
	private CellObject cell = null;
	private long oldYear = -1;
	private int oldIndex = -1;
	private boolean isFullMod = false;
	private final JList<JListRow> listDNA;
	/**Филогинетическое дерево*/
	private final TextPair filogen;
	private final TextPair pos;
	private final TextPair toxicFIeld;
	private final TextPair Buoyancy;
	private final JPanel panel_variant;
	/**Панель, на которой написано про ДНК*/
	private final JPanel panel_DNA;
	/**Список с прерываниями*/
	private final JList<String> list_inter;
	private final JScrollPane scrollPane_inter;
	private final JPanel panel;
	/**Тестовая клетка, для работы с командами без конца*/
	private TextAL testCell = null;
	
	class WorkTask implements Runnable{
		@Override
		public void run() {
			if(isVisible() && getCell() != null && !getCell().aliveStatus(AliveCell.LV_STATUS.GHOST)) {
				setDinamicHaracteristiks();
				if(getCell() instanceof AliveCell lcell) {
					/**Индекс с которого идёт пеерсчёт*/
					long age = lcell.getAge();
					if(age != oldYear || oldIndex != lcell.getDna().getIndex()) {
						oldYear = age;
						oldIndex = lcell.getDna().getIndex();
						testCell = null;
						printDNA(lcell); 
					}
				}
			} else {
				if(cell != null) {
					cell = null;
					clearText();
					listDNA.setModel(new DefaultListModel<> ());
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
		
		panel = new JPanel();
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
		
		list_inter = new JList<>();
		list_inter.setEnabled(false);
		list_inter.setModel(new DefaultListModel<> ());
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
		
		listDNA = new JList<>();
		listDNA.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				isFullMod = !isFullMod;
				CommandDNA.setFullMod(isFullMod);
				if(testCell == null)
					printDNA((AliveCell) getCell());
				else
					printDNA(testCell);
			}
		});
		listDNA.setCellRenderer(new JlistRender());
		listDNA.setVisibleRowCount(3);
		listDNA.setEnabled(false);
		scrollPane.setViewportView(listDNA);
		listDNA.setModel(new DefaultListModel<> ());
		listDNA.setSelectedIndex(0);
		panel.setLayout(gl_panel);
		
		Configurations.TIME_OUT_POOL.scheduleWithFixedDelay(new WorkTask(), 100, 100, TimeUnit.MILLISECONDS);
	}
	
	
	public void setCell(CellObject cellObject) {
		clearText();
		this.cell=cellObject;
		if(cellObject == null)
			return;

		setDinamicHaracteristiks();
		if (getCell() instanceof AliveCell aliveCell) {
			panel_variant.setVisible(true);
			panel_DNA.setVisible(true);
			generation.setText(aliveCell.getGeneration()+"");
			photos.setText((aliveCell.photosynthesisEffect+"").substring(0, 3));
			phenotype.setBackground(aliveCell.phenotype);
			phenotype.setText(Integer.toHexString(aliveCell.phenotype.getRGB()));
			filogen.setText(aliveCell.getBranch());
			
			DefaultListModel<String> model = new DefaultListModel<> ();
			DNA dna = aliveCell.getDna();
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
		state.setText(getCell().getAlive().name());
		age.setText(String.valueOf(getCell().getAge()));
		if (getCell() instanceof AliveCell alive) {
			if(testCell != null) //Так мы сможем обновлять эту клетку
				alive = testCell;
            mp.setText(String.valueOf(alive.getMineral()));
			direction.setText(alive.direction.name());
			hp.setText(((int)getCell().getHealth())+"+" + Math.round(Configurations.sun.getEnergy(alive.getPos())+(1+alive.photosynthesisEffect) * alive.getMineral() / AliveCell.MAX_MP)+"\\" + alive.getDNA_wall());
			double realLv = alive.getPos().getY() - (Configurations.MAP_CELLS.height * Configurations.LEVEL_MINERAL);
        	double dist = Configurations.MAP_CELLS.height * (1 - Configurations.LEVEL_MINERAL);
			mp.setText(alive.getMineral()+"+" + Math.round(Configurations.CONCENTRATION_MINERAL * (realLv/dist) * (5 - alive.photosynthesisEffect)));
			toxicFIeld.setText(alive.getPosionType() + ":" + alive.getPosionPower());
			Buoyancy.setText(String.valueOf(alive.getBuoyancy()));
		} else if (getCell() instanceof Poison poison) {
			hp.setText(String.valueOf((int)getCell().getHealth()));
			toxicFIeld.setText(poison.type.name());
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
		oldYear = -1;
		isFullMod = false;
		CommandDNA.setFullMod(isFullMod);
	}


	/**
	 * @return the cell
	 */
	public CellObject getCell() {
		return cell;
	}

	public void step() {
		if(!(cell instanceof AliveCell))
			return;
		if(testCell == null){
			testCell = new TextAL((AliveCell) getCell());
		}
		testCell.next();
	}
	/**
	 * Печатает в табличку гены клетки
	 * @param lcell - клетка, которую обрабатываем
	 * @param dna - её ДНК
	 */
	private void printDNA(AliveCell lcell) {
		DNA dna = new DNA(lcell.getDna());
		DefaultListModel<JListRow> model = new DefaultListModel<>();
		model.setSize(dna.size);
		var first = dna.get();
		int interVal = -1;
		if (first.isInterrupt()) {
			scrollPane_inter.setVisible(true);
			var inter = first.getInterrupt(lcell, dna);
			list_inter.setSelectedIndex(inter);
			if(inter != -1){
				list_inter.setBackground(RED);
				interVal = dna.interrupts[inter];
			}else {
				list_inter.setBackground(Color.WHITE);
			}
		} else {
			scrollPane_inter.setVisible(false);
		}
		for (int i = 0; i < dna.size; i++) {
			int cmd = dna.getIndex();
			CommandDNA cmd_o = dna.get();
			StringBuilder sb = new StringBuilder();
			if(cmd == interVal)
				sb.append("**");
			sb.append(cmd);
			sb.append("=");
			sb.append(dna.get(cmd, 0));
			sb.append(" ");
			sb.append(cmd_o.toString(lcell, dna));
			if (cmd_o.isInterrupt())
				model.add(i, new JListRow(sb.toString(), JListRow.TYPE.CMD_I));
			else if (cmd_o.isDoing())
				model.add(i, new JListRow(sb.toString(), JListRow.TYPE.CMD_D));
			else
				model.add(i, new JListRow(sb.toString(), JListRow.TYPE.CMD));

			for (int j = 0; j < cmd_o.getCountParams(); j++) {
				sb = new StringBuilder();
				var index_cmd = (cmd + j + 1) % dna.size;
				if(index_cmd == interVal)
					sb.append("**");
				sb.append(index_cmd);
				sb.append("=");
				sb.append(dna.get(cmd + j + 1, 0));
				sb.append(" П ");
				sb.append(cmd_o.getParam(lcell, j, dna));
				model.add(++i, new JListRow(sb.toString(), JListRow.TYPE.PARAM));
			}
			for (int j = 0; j < cmd_o.getCountBranch(); j++) {
				sb = new StringBuilder();
				var index_cmd = (cmd + cmd_o.getCountParams() + j + 1) % dna.size;
				if(index_cmd == interVal)
					sb.append("**");
				sb.append(index_cmd);
				sb.append("=");
				sb.append(dna.get(cmd + j + 1, 0));
				sb.append(" А");
				sb.append(j);
				sb.append(" (");
				var atr = dna.get(dna.getIndex() + 1 + cmd_o.getCountParams(), j);
				sb.append((dna.getIndex() + atr) % dna.size);
				sb.append(")");
				model.add(++i, new JListRow(sb.toString(), JListRow.TYPE.CMD));
			}
			dna.next(1 + cmd_o.getCountParams() + cmd_o.getCountBranch());
		}
		listDNA.setModel(model);
		BotInfo.this.revalidate();
	}
}
