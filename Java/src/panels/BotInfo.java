package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.CellObject;
import MapObjects.CellObject.OBJECT;
import MapObjects.Poison;
import MapObjects.dna.CommandDNA;
import MapObjects.dna.DNA;
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
		/**Цвет строки*/
		private final Color color;
		/**Текст строки*/
		private final String text;
		/**Преобразование символов в квадрат. Спецобъект*/
		private static final FontRenderContext frc = new FontRenderContext( new AffineTransform(),true,true);     
		private static final Font font = javax.swing.UIManager.getDefaults().getFont("Label.font");
		/**Ширина текста в пикселях*/
		private int textwidth;
		
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
			textwidth = (int)(font.getStringBounds(text, frc).getWidth());
		}
	}
	/**Отрисовщик строк таблицы. Очень помогает каждой строке давать свой цвет*/
	private class JlistRender extends DefaultListCellRenderer {
		/**Общий для всех счётчик, который листает бегущие строки*/
		private static int counter = 0;
		/**Время паузы между встречными движениями*/
		private static int timeout = 10;
		
		@Override
		public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			var c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value == null)
				return c;
			if (value instanceof JListRow nextRow) {
				var maxW = panel_DNA.getWidth() - 40;
				if (nextRow.textwidth > maxW) {
					var lenght = nextRow.text.length();
					int countVChir = (int) (lenght * maxW / nextRow.textwidth);
					var pos = JlistRender.counter % (2 * (lenght + timeout));
					if (pos < lenght + timeout)
						setText(nextRow.text.substring(Utils.Utils.betwin(0, pos, lenght - countVChir)));
					else
						setText(nextRow.text.substring(Utils.Utils.betwin(0, 2 * (lenght + timeout) - (pos + 2 * timeout), lenght - countVChir)));
				} else {
					setText(nextRow.text);
				}
				if (isSelected) 
					setBackground(getBackground().darker());
				else 
					setBackground(nextRow.color);
				setOpaque(true);
			} else {
				throw new IllegalArgumentException("Список параметров содержит странные параметры. Это вообще что?! " + value.getClass());
			}
			return c;
		}
	}
	/**Пара подписи и текстового поля*/
	private class TextPair extends JPanel {
		/**Текст пары */
		private JLabel text = null;
		/**Панель пролистывания*/
		private JScrollPane scroll = null;
		/**Текстовое поле пары*/
		private JLabel field = null;

		/**Время паузы между встречными движениями*/
		private static int timeout = 10;
		
		public TextPair(String label) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			text = new JLabel(label);
			add(text);

			scroll = new JScrollPane();
			scroll.getVerticalScrollBar().setVisibleAmount(0);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			scroll.getVerticalScrollBar().setUnitIncrement(1);
			add(scroll);
			
			field = new JLabel();
			scroll.setViewportView(field);
			
	        scrolFieldList.add(this);
		}

		public void clear() {setText("");}

		public void setText(String text) {
			field.setText(text);
		}

		@Override
		public void setBackground(Color bg) {
			super.setBackground(bg);
			if(text != null && field != null && scroll != null) {
				text.setBackground(bg);
				scroll.setBackground(bg);
				field.setBackground(bg);
				field.setOpaque(true);
				
				var hsb = java.awt.Color.RGBtoHSB(bg.getRed(),bg.getGreen(),bg.getBlue(), null);
				var invert = new Color(Color.HSBtoRGB(hsb[0] > 0.5f ? (hsb[0] - 0.5f) : (hsb[0] + 0.5f), hsb[1] > 0.5f ? (hsb[1] - 0.5f) : (hsb[1] + 0.5f),hsb[2] > 0.5f ? (hsb[2] - 0.5f) : (hsb[2] + 0.5f)));
				text.setForeground(invert);
				field.setForeground(invert);
			}
		}

		public void setToolTipText(String tttext) {
			super.setToolTipText(tttext);
			if(text != null && field != null && scroll != null) {
				text.setToolTipText(tttext);
				scroll.setToolTipText(tttext);
				field.setToolTipText(tttext);
			}
		}
		/**Автоматически проматывает текст поля на один символ дальше*/
		public void scrol() {
			var max = (scroll.getHorizontalScrollBar().getMaximum() - scroll.getHorizontalScrollBar().getVisibleAmount());
			var pos = JlistRender.counter % (2 * (max + timeout));
			if (pos < max + timeout)
				scroll.getHorizontalScrollBar().setValue(pos);
			else
				scroll.getHorizontalScrollBar().setValue(2 * (max + timeout) - (pos + timeout));
		}
		
		public String toString() {
			return "TextPair " + text.getText() + " " + field.getText();
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
	private final TextPair buoyancy;
	private final JPanel panel_variant;
	/**Панель, на которой написано про ДНК*/
	private final JPanel panel_DNA;
	/**Панель со всеми константами*/
	private final JPanel panelConstant;
	/**Список с прерываниями*/
	private final JList<String> list_inter;
	private final JScrollPane scrollPane_inter;
	private final JPanel panel;
	/**Тестовая клетка, для работы с командами без конца*/
	private TextAL testCell = null;
	/**Список тех полей, которые нужно автоматически проматывать*/
	private final Set<TextPair> scrolFieldList;
	
	class WorkTask implements Runnable{
		static boolean updateCounter = false;
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
					} else {
						listDNA.repaint();
					}
					if(!updateCounter) {
						updateCounter = true;
						JlistRender.counter++;
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
		
		scrolFieldList = new HashSet<>();
		
		setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		panel.setToolTipText(getProperty("main"));
		add(panel, BorderLayout.CENTER);
		
		panel_DNA = new JPanel();
		
		panelConstant = new JPanel();
		panelConstant.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), getProperty("ConstPanel"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		panel_variant = new JPanel();
		panel_variant.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), getProperty("VarPanel"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
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
						.addComponent(panelConstant, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
						.addComponent(panel_variant, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(panelConstant, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(panel_variant, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane_inter, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_DNA, GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		JLabel lblNewLabel = new JLabel(getProperty("InterraptLabel"));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane_inter.setColumnHeaderView(lblNewLabel);
		lblNewLabel.setToolTipText(getProperty("Interrapt"));
		scrollPane_inter.setToolTipText(getProperty("Interrapt"));
		
		list_inter = new JList<>();
		list_inter.setToolTipText(getProperty("InterraptToolTipText"));
		list_inter.setEnabled(false);
		list_inter.setModel(new DefaultListModel<> ());
		list_inter.setToolTipText(getProperty("Interrapt"));
		scrollPane_inter.setViewportView(list_inter);
		generation = new TextPair(getProperty("LabelGeneration"));
		mp = new TextPair(getProperty("LabelMp"));
		direction = new TextPair(getProperty("LabelDirection"));
		toxicFIeld = new TextPair(getProperty("LabelToxic"));
		photos = new TextPair(getProperty("LabelPhotos"));
		phenotype = new TextPair(getProperty("LabelPhenotype"));
		filogen = new TextPair(getProperty("LabelFilogen"));
		buoyancy = new TextPair(getProperty("LabelBuoyancy"));
		
		generation.setToolTipText(getProperty("fieldGeneration"));
		mp.setToolTipText(getProperty("fieldMp"));
		direction.setToolTipText(getProperty("fieldDirection"));
		toxicFIeld.setToolTipText(getProperty("fieldToxic"));
		photos.setToolTipText(getProperty("fieldPhotos"));
		phenotype.setToolTipText(getProperty("fieldPhenotype"));
		filogen.setToolTipText(getProperty("fieldFilogen"));
		buoyancy.setToolTipText(getProperty("fieldBuoyancy"));
		
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
					.addComponent(buoyancy, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
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
					.addComponent(buoyancy, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(20, Short.MAX_VALUE))
		);
		
		panel_variant.setLayout(gl_panel_variant);

		pos = new TextPair(getProperty("LabelPos"));
		hp = new TextPair(getProperty("LabelHp"));
		state = new TextPair(getProperty("LabelState"));
		age = new TextPair(getProperty("LabelAge"));

		pos.setToolTipText(getProperty("fieldPos"));
		hp.setToolTipText(getProperty("fieldHp"));
		state.setToolTipText(getProperty("fieldState"));
		age.setToolTipText(getProperty("fieldAge"));
		
		
		GroupLayout gl_panel_const = new GroupLayout(panelConstant);
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
		panelConstant.setLayout(gl_panel_const);
		panel_DNA.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_9 = new JLabel(getProperty("DNA_Panel"));
		lblNewLabel_9.setHorizontalAlignment(SwingConstants.CENTER);
		panel_DNA.add(lblNewLabel_9, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel_DNA.add(scrollPane, BorderLayout.CENTER);
		
		listDNA = new JList<>();
		listDNA.setToolTipText(getProperty("DNAToolTipText"));
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
			generation.setText(Integer.toString(aliveCell.getGeneration()));
			var type = AliveCellProtorype.Specialization.TYPE.PHOTOSYNTHESIS;
			var max = 0d;
			for(var t : AliveCellProtorype.Specialization.TYPE.staticValues) {
				if(max < aliveCell.get(t)) {
					max = aliveCell.get(t);
					type = t;
				}
			}
			photos.setText(MessageFormat.format("{0} {1}%",type,aliveCell.get(type) * 100));
			phenotype.setBackground(aliveCell.phenotype);
			phenotype.setText(MessageFormat.format("R{0} G{0} B{0}", aliveCell.phenotype.getRed(), aliveCell.phenotype.getGreen(), aliveCell.phenotype.getBlue()));
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

	/**
	 * Обновляет значения динамических полей - возраст, направление т.д.
	 */
	private void setDinamicHaracteristiks() {
		pos.setText(cell.getPos().toString());
		state.setText(getCell().getAlive().toString());
		age.setText(String.valueOf(getCell().getAge()));
		if (getCell() instanceof AliveCell alive) {
			if(testCell != null) //Так мы сможем обновлять эту клетку
				alive = testCell;
			direction.setText(alive.direction.toSString());
			if(alive.getDNA_wall() > 0)
				hp.setText(((int) getCell().getHealth()) + " ⊡" + alive.getDNA_wall());
			else
				hp.setText(Integer.toString((int)getCell().getHealth()));
			mp.setText(Long.toString(alive.getMineral()));
			toxicFIeld.setText(alive.getPosionType() + ":" + alive.getPosionPower());
			buoyancy.setText(String.valueOf(alive.getBuoyancy()));	
			
			for(var i : scrolFieldList) 
				i.scrol();
			WorkTask.updateCounter = false;
		} else if (getCell() instanceof Poison poison) {
			hp.setText(String.valueOf((int)getCell().getHealth()));
			toxicFIeld.setText(poison.getType().name());
		} else {
			hp.setText(String.valueOf((int)getCell().getHealth()));
		}
		panelConstant.repaint();
		panel_variant.repaint();
	}
	
	private void clearText() {
		generation.clear();
		age.clear();
		state.clear();
		hp.clear();
		mp.clear();
		direction.clear();
		photos.clear();
		phenotype.clear();
		filogen.clear();
		pos.clear();
		toxicFIeld.clear();
		buoyancy.clear();
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
		//Адрес прерывания
		int interVal = -1;
		if (first.isInterrupt()) {
			scrollPane_inter.setVisible(true);
			var inter = first.getInterrupt(lcell, dna);
			if(inter != -1){
				DefaultListModel<String> modelinterrapt = new DefaultListModel<> ();
				modelinterrapt.setSize(dna.interrupts.length);
				modelinterrapt.add(0,OBJECT.get(inter) + " - " + String.valueOf(dna.interrupts[inter]));
				for(int i = 0 ; i < dna.interrupts.length - 1; i++) {
					if(i == inter) continue;
					modelinterrapt.add(i + (i < inter ? 1 : 0),OBJECT.get(i) + " - " + String.valueOf(dna.interrupts[i]));
				}
				list_inter.setModel(modelinterrapt);
				list_inter.setSelectedIndex(0);
				list_inter.setBackground(RED);
				interVal = dna.interrupts[inter];
			}else {
				list_inter.setSelectedIndex(inter);
				list_inter.setBackground(Color.WHITE);
			}
		} else {
			scrollPane_inter.setVisible(false);
		}
		for (int i = 0; i < dna.size; ) {
			int cmd = dna.getIndex();
			CommandDNA cmd_o = dna.get();
			addDNA(lcell, dna, model, interVal, i++, cmd, cmd_o, cmd_o.isInterrupt() ? JListRow.TYPE.CMD_I : (cmd_o.isDoing() ? JListRow.TYPE.CMD_D : JListRow.TYPE.CMD));
			
			for (int j = 0; j < cmd_o.getCountParams(); j++) {
				addDNA(lcell, dna, model, interVal, i++, cmd + j + 1, cmd_o, JListRow.TYPE.PARAM, j);
			}
			for (int j = 0; j < cmd_o.getCountBranch(); j++) {
				addDNA(lcell, dna, model, interVal, i++ , cmd + cmd_o.getCountParams() + j + 1, cmd_o, JListRow.TYPE.ARG, j);
			}
			dna.next(1 + cmd_o.getCountParams() + cmd_o.getCountBranch());
		}
		listDNA.setModel(model);
		BotInfo.this.revalidate();
	}
	
	private void addDNA(AliveCell lcell, DNA dna, DefaultListModel<JListRow> model, int interVal, int indexRowInModel, int indexCmd, CommandDNA cmd_o, JListRow.TYPE type) {
		addDNA(lcell, dna, model, interVal, indexRowInModel, indexCmd, cmd_o, type, -1);
	}
	/**
	 * Добавляет одну строчку к листу ДНК
	 * @param lcell клетка, чья строка
	 * @param dna её ДНК (фишка в том, что это копия)
	 * @param model модель, в которую добавлять будем данные
	 * @param interVal какой индексу прерывания, если активное
	 * @param indexRowInModel какой номер строки добавляем
	 * @param indexCmd какой индекс у команды
	 * @param cmd_o команда, которую разбираем
	 * @param type тип команды. Это может быть команда, а может быть её параметр или аргумент
	 * @param indexParamOrBrenh
	 */
	private void addDNA(AliveCell lcell, DNA dna, DefaultListModel<JListRow> model, int interVal, int indexRowInModel, int indexCmd, CommandDNA cmd_o, JListRow.TYPE type, int indexParamOrBrenh) {
		StringBuilder sb = new StringBuilder();
		indexCmd = indexCmd % dna.size;
		if(indexCmd == interVal)
			sb.append("* ");
		else
			sb.append("  ");
		sb.append(indexCmd);
		sb.append("=");
		sb.append(dna.get(indexCmd, 0));
		switch (type) {
			case CMD,CMD_D,CMD_I -> {
				sb.append(" ");
				sb.append(cmd_o.toString(lcell, dna));
				if(indexRowInModel == 0) {
					var val = cmd_o.value(lcell, dna);
					if(val != null) {
						sb.append(" → ");
						sb.append(cmd_o.value(lcell, dna));
					}
				}
			}
			case PARAM -> {
				sb.append(" П ");
				sb.append(cmd_o.getParam(lcell, indexParamOrBrenh, dna));
			}
			case ARG -> {
				sb.append(" А");
				sb.append(indexParamOrBrenh);
				sb.append(" (");
				var atr = dna.get(dna.getIndex() + 1 + cmd_o.getCountParams(), indexParamOrBrenh);
				sb.append((dna.getIndex() + atr) % dna.size);
				sb.append(")");
			}
		}
		
		model.add(indexRowInModel, new JListRow(sb.toString(), type));
	}
	
	private String getProperty(String name) {
		return Configurations.getHProperty(BotInfo.class,name);
	}
}
