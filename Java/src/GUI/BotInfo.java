package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
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
import MapObjects.CellObject;
import MapObjects.CellObject.OBJECT;
import MapObjects.Organic;
import MapObjects.Poison;
import MapObjects.dna.CommandDNA;
import MapObjects.dna.DNA;
import Utils.MyMessageFormat;
import Utils.Utils;
import java.awt.EventQueue;
import java.util.Map;
import Calculations.Configurations;
import Calculations.Point;

public class BotInfo extends JPanel implements Configurations.EvrySecondTask{
	
	private static final Color RED = new Color(255, 0, 0, 30);
	private static final Color RED2 = new Color(255, 0, 0, 60);
	private static final Color BLUE = new Color(0, 0, 255, 30);
	public static final Color YELLOW = new Color(255, 255, 0, 30);
	public static final Color ARG = new Color(100, 100, 100, 30);
	public static final Color PAR = new Color(255, 255, 255, 30);
	
	private TextPair photos;
	private TextPair state;
	private TextPair hp;
	private TextPair hpTank;
	private TextPair mp;
	private TextPair direction;
	private TextPair age;
	private TextPair generation;
	private TextPair phenotype;
	
	/**Филогинетическое дерево*/
	private TextPair filogen;
	/**Позиция*/
	private TextPair pos;
	/**Поле токсикации*/
	private TextPair toxicFIeld;
	/**Плотность*/
	private TextPair buoyancy;
	/**Слизь*/
	private TextPair mucosa;
	/**Список тех полей, которые нужно автоматически проматывать*/
	private final Set<TextPair> scrolFieldList;
	/**Лист всех полей*/
	List<TextPair> listFields;
	/**Выбранная клетка*/
	private CellObject cell = null;
	private long oldYear = -1;
	private int oldIndex = -1;
	/**Режим подписей полный или в виде значков*/
	private boolean isFullMod = false;
	/**Развёрнутая в лист ДНК*/
	private final JList<JListRow> listDNA;
	/**Панель, на которой написано про ДНК*/
	private final JPanel panel_DNA;
	/**Панель со всеми константами*/
	private final JPanel panelConstant;
	/**Список с прерываниями*/
	private final JList<String> list_inter;
	/**Панель с прерываниями*/
	private final JScrollPane scrollPane_inter;
	/**Главная панель, на которой всё и размещается*/
	private final JPanel panel;
	/**Тестовая клетка, для работы с командами без конца*/
	private TextAL testCell = null;
	//Специальный счётчик, который нужен для обновления инфы по клетке
	private int counter = 0;
	/**Форматирование чисел*/
	private static final MyMessageFormat numberFormat = new MyMessageFormat("{0,number,###,###}");
	
	/**Одна строка в таблице ДНК*/
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
					var pos = counter % (2 * (lenght + timeout));
					if (pos < lenght + timeout)
						setText(nextRow.text.substring(Utils.betwin(0, pos, lenght - countVChir)));
					else
						setText(nextRow.text.substring(Utils.betwin(0, 2 * (lenght + timeout) - (pos + 2 * timeout), lenght - countVChir)));
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
		/**Значение пары*/
		private JLabel field = null;

		/**Время паузы между встречными движениями*/
		private static int timeout = 10;
		
		public TextPair(String label, String toolTipText) {
			setLayout(new BorderLayout());
			text = new JLabel(label);
			add(text,BorderLayout.WEST);

			scroll = new JScrollPane();
			scroll.getVerticalScrollBar().setVisibleAmount(0);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			scroll.getVerticalScrollBar().setUnitIncrement(1);
			add(scroll,BorderLayout.CENTER);
			
			field = new JLabel();
			field.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			scroll.setViewportView(field);
			
	        scrolFieldList.add(this);
	        setToolTipText(toolTipText);
	        
			var adapter = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON1)
						counter -= 10;
					else if(e.getButton() == MouseEvent.BUTTON3)
						counter += 10;
					counter &= 0xFFFF;
				}
			};
			addMouseListener(adapter);
			text.addMouseListener(adapter);
			scroll.addMouseListener(adapter);
			field.addMouseListener(adapter);
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
			var pos = counter % (2 * (max + timeout));
			if (pos < max + timeout)
				scroll.getHorizontalScrollBar().setValue(pos);
			else
				scroll.getHorizontalScrollBar().setValue(2 * (max + timeout) - (pos + timeout));
		}
		
		public String toString() {
			return "TextPair " + text.getText() + " " + field.getText();
		}
	}
	/**Клетка-затычка для симулирования дальнейших шагов*/
	private class TextAL extends AliveCell{
		DNA interDNA = null;
		private final Map<Point,AliveCell> friends;
		private TextAL(AliveCell cell) {
			super(cell);
			friends = cell.getFriends();
		}
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
				while(interDNA.getPC() != newIndex) //Топорное решение, но первое что пришло в голову
					interDNA.next(1);
				printDNA(this); 
			}
		}
		
		@Override
		public DNA getDna(){
			if(interDNA == null) return super.getDna();
			else return interDNA;
		}
		@Override
		public Map<Point,AliveCell> getFriends(){return friends;}
	}
	
	/**
	 * Create the panel.
	 */
	public BotInfo() {
		
		scrolFieldList = new HashSet<>();
		
		setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		panel.setToolTipText(getProperty("main"));
		add(panel, BorderLayout.CENTER);

		panelConstant = new JPanel();
		makeParamsPanel();
		
		panel_DNA = new JPanel();
				
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
						.addComponent(panelConstant, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(panelConstant, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
		
		Configurations.addTask(this, 100);
	}
	
	@Override
	public void taskStep(){
		if(isVisible() && getCell() != null && !getCell().aliveStatus(AliveCell.LV_STATUS.GHOST)) {
			setDinamicHaracteristiks();
			if(getCell() instanceof AliveCell lcell) {
				/**Индекс с которого идёт пеерсчёт*/
				long age = lcell.getAge();
				if(age != oldYear || oldIndex != lcell.getDna().getPC()) {
					oldYear = age;
					oldIndex = lcell.getDna().getPC();
					testCell = null;
					printDNA(lcell); 
				} else {
					listDNA.repaint();
				}
			}
			EventQueue.invokeLater(() -> {counter++;});
		} else {
			if(cell != null) {
				cell = null;
				clearText();
				listDNA.setModel(new DefaultListModel<>());
			}
		}
	}
	/**Создаёт все текстовые поля информации*/
	private void makeParamsPanel() {
		panelConstant.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), getProperty("ConstPanel"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		listFields = new ArrayList<>();

		listFields.add(pos = new TextPair(getProperty("LabelPos"), getProperty("fieldPos")));
		listFields.add(hp = new TextPair(getProperty("LabelHp"), getProperty("fieldHp")));
		listFields.add(hpTank = new TextPair(getProperty("LabelHpTank"), getProperty("fieldHpTank")));
		listFields.add(state = new TextPair(getProperty("LabelState"), getProperty("fieldState")));
		listFields.add(age = new TextPair(getProperty("LabelAge"), getProperty("fieldAge")));	
		listFields.add(generation = new TextPair(getProperty("LabelGeneration"), getProperty("fieldGeneration")));
		listFields.add(mp = new TextPair(getProperty("LabelMp"), getProperty("fieldMp")));
		listFields.add(direction = new TextPair(getProperty("LabelDirection"), getProperty("fieldDirection")));
		listFields.add(toxicFIeld = new TextPair(getProperty("LabelToxic"), getProperty("fieldToxic")));
		listFields.add(photos = new TextPair(getProperty("LabelPhotos"), getProperty("fieldPhotos")));
		listFields.add(phenotype = new TextPair(getProperty("LabelPhenotype"), getProperty("fieldPhenotype")));
		listFields.add(filogen = new TextPair(getProperty("LabelFilogen"), getProperty("fieldFilogen")));
		listFields.add(buoyancy = new TextPair(getProperty("LabelBuoyancy"), getProperty("fieldBuoyancy")));
		listFields.add(mucosa = new TextPair(getProperty("LabelMucosa"), getProperty("fieldMucosa")));
		

		GroupLayout gl_panel_const = new GroupLayout(panelConstant);
		var hGroupe = gl_panel_const.createParallelGroup(Alignment.TRAILING);
		for(var i : listFields) {
			hGroupe.addComponent(i, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE);
		}
		gl_panel_const.setHorizontalGroup(
			gl_panel_const.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_const.createSequentialGroup()
					.addContainerGap()
					.addGroup(hGroupe)
					.addContainerGap())
		);
		var wGroupe = gl_panel_const.createSequentialGroup();
		for(var i : listFields) {
			wGroupe.addComponent(i, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addPreferredGap(ComponentPlacement.RELATED);
		}
		
		gl_panel_const.setVerticalGroup(
			gl_panel_const.createParallelGroup(Alignment.LEADING)
				.addGroup(wGroupe)
		);
		panelConstant.setLayout(gl_panel_const);
	}
	/**Выбрать объект карты для исследования
	 * @param cellObject объект карты, который отобразит панель
	 */
	public void setCell(CellObject cellObject) {
		clearText();
		this.cell=cellObject;
		if(cellObject == null)
			return;

		setDinamicHaracteristiks();
		if (getCell() instanceof AliveCell aliveCell) {
			panel_DNA.setVisible(true);
			generation.setText(Integer.toString(aliveCell.getGeneration()));
			
			StringBuilder sb = new StringBuilder();
		    for(var i : Utils.sortByValue(aliveCell.getSpecialization())) {
		    	if(i.getValue() == 0) continue;
				if (sb.isEmpty())
					sb.append(i.getKey().toString());
				else
					sb.append(i.getKey().toSString());
	    		sb.append(' ');
	    		sb.append(i.getValue());
	    		sb.append('%');
	    		sb.append(' ');
		    }
			
			photos.setText(sb.toString());
			phenotype.setBackground(aliveCell.phenotype);
			phenotype.setText(MessageFormat.format("R{0} G{1} B{2}", aliveCell.phenotype.getRed(), aliveCell.phenotype.getGreen(), aliveCell.phenotype.getBlue()));
			filogen.setText(aliveCell.getBranch());
			
			DefaultListModel<String> model = new DefaultListModel<> ();
			DNA dna = aliveCell.getDna();
			model.setSize(dna.interrupts.length);
			for(int i = 0 ; i < dna.interrupts.length ; i++)
				model.add(i,OBJECT.get(i) + " - " + String.valueOf(dna.interrupts[i]));
			list_inter.setModel(model);
		}
		updateVisibleParams();
	}

	/**Обновляет значения динамических полей - возраст, направление т.д.*/
	private void setDinamicHaracteristiks() {
		pos.setText(cell.getPos().toString());
		state.setText(getCell().getAlive().toString());
		age.setText(numberFormat.format(getCell().getAge()));
		if (getCell() instanceof AliveCell alive) {
			if(testCell != null) //Так мы сможем обновлять эту клетку
				alive = testCell;
			direction.setText(alive.direction.toSString());
			{
				StringBuilder sb = new StringBuilder();
				sb.append(numberFormat.format((int) alive.getHealth()));
				if(alive.getFoodTank() > 0)
					hpTank.setText(numberFormat.format(alive.getFoodTank()));
				else
					hpTank.clear();
				if(alive.getDNA_wall() > 0) {
					sb.append(" ⌧§");
					sb.append(alive.getDNA_wall());
				}
				hp.setText(sb.toString());
			}
			if(alive.getMineral() > 0){
				StringBuilder sb = new StringBuilder();
				sb.append(((int) alive.getMineral()));
				if(alive.getMineralTank() > 0){
					sb.append(" +");
					sb.append(alive.getMineralTank());
				}
				/*if(alive.mineralAround() > 0){
					sb.append(" →← ");
					sb.append((int) alive.mineralAround());
				}
				mp.setText(sb.toString());*/ throw new AssertionError();
			} else {
				mp.clear();
			}
			if(alive.getPosionPower() > 0)
				toxicFIeld.setText(alive.getPosionType() + ":" + alive.getPosionPower());
			else
				toxicFIeld.clear();
			if(alive.getBuoyancy() > 0)
				buoyancy.setText(String.valueOf(alive.getBuoyancy()));
			else
				buoyancy.clear();
			if(alive.getMucosa() > 0)
				mucosa.setText(String.valueOf(alive.getMucosa()));
			else
				mucosa.clear();
		} else if (getCell() instanceof Poison poison){
			hp.setText(String.valueOf((int)getCell().getHealth()));
			toxicFIeld.setText(poison.getType().toString());
		} else if (getCell() instanceof Organic org){
			hp.setText(numberFormat.format((int) org.getHealth()));
			if(org.getPoison() != Poison.TYPE.UNEQUIPPED)
				toxicFIeld.setText(org.getPoison() + ":" + org.getPoisonCount());
			else
				toxicFIeld.clear();
		} else {
			hp.setText( numberFormat.format((int) getCell().getHealth()));	
		}
		for(var i : scrolFieldList) 
			i.scrol();
		panelConstant.repaint();
		updateVisibleParams();
	}
	/**Очищает все поля*/
	private void clearText() {
		for(var i : listFields) {
			i.clear();
		}
		listDNA.setModel(new DefaultListModel<>());
		panel_DNA.setVisible(false);
		scrollPane_inter.setVisible(false);
		oldYear = -1;
		isFullMod = false;
		updateVisibleParams();
		CommandDNA.setFullMod(isFullMod);
	}
	
	private void updateVisibleParams() {
		for(var i : listFields) {
			i.setVisible(i.field.getText().length() != 0);
		}
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
		DefaultListModel<JListRow> model = new DefaultListModel<>();
		model.setSize(dna.size);
		for (int i = 0; i < dna.size; ) {
			int cmd = dna.getPC();
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
	 * @param indexCmd какой абсолютный индекс у команды
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
		sb.append(dna.get(indexCmd, true));
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
				sb.append(" ");
				sb.append(cmd_o.getBranch(lcell, indexParamOrBrenh, dna));
			}
		}
		
		model.add(indexRowInModel, new JListRow(sb.toString(), type));
	}
	
	private String getProperty(String name) {
		return Configurations.getHProperty(BotInfo.class,name);
	}
}
