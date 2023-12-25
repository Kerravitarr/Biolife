/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/AWTForms/Dialog.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.CellObject;
import MapObjects.Poison;
import MapObjects.dna.CommandDNA;
import MapObjects.dna.DNA;
import Utils.JSON;
import Utils.RingBuffer;
import Utils.SaveAndLoad;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;

/**
 * Редактор клеток
 * @author Kerravitarr
 */
public class CellEditor extends javax.swing.JDialog {
	private enum POPUP_STATUS {
		UNDEF, INC, DEC, REM, REM_GEN,ADD,COPY
	}

	/** Creates new form CellEditor */
	public CellEditor(AliveCell edit) {
		super();
		CommandDNA.setFullMod(false);
		initComponents();
		setAlwaysOnTop(true);
		centralPanel.add(new PaintJPanel(), java.awt.BorderLayout.CENTER);
		
		setButtonParam(decrement);
		setButtonParam(increment);
		setButtonParam(jampBack);
		
		
		setObject(edit);
		
	}
	/**Делает кнопочки покрасивее
	 * @param button 
	 */
	private void setButtonParam(JButton button) {
		button.setContentAreaFilled(false);
		button.setFocusable(false);
	}
	/** @param o новый объет, который мы описываем*/
	private void setObject(AliveCell o){
		object = o.clone();
		makeSettingsPanel();
		makeInterraptPanel();
		updateHeaderPanel();
		
		nextDNA(0);
		while(!history.isEmpty())history.pop();
	}
	
	
	/**Создаёт панель настроек*/
	private void makeSettingsPanel() {
		settingsPanel.removeAll();
		final var ST = AliveCellProtorype.Specialization.TYPE.values;
		
		final var spec = new ArrayList<SettingsSlider>(ST.length);
		for(final var s : ST){
			final var slider = new SettingsSlider<>(CellEditor.class, "specialization."+s.name(),
					0, 50, 100, 0, object.getSpecialization().get(s), 100, e->{
						object.getSpecialization().set(s, e);
						for (int i = 0; i < spec.size(); i++)
							spec.get(i).setValue(object.getSpecialization().get(ST[i]));
					});
			spec.add(slider);
			settingsPanel.add(slider);
		}
		settingsPanel.add(new SettingsSlider<>(CellEditor.class, "settingsPanel.HP",
					0d, AliveCellProtorype.START_HP, AliveCellProtorype.MAX_HP, 0d, object.getHealth(), null, 
				e->object.setHealth(e)));
		settingsPanel.add(new SettingsSlider<>(CellEditor.class, "settingsPanel.MP",
					0L, AliveCellProtorype.START_MP, AliveCellProtorype.MAX_MP, 0L, object.getMineral(), null, 
				e->object.setMineral(e)));
		final var PT = Poison.TYPE.vals;
		final var poisonPower = new SettingsSlider<>(CellEditor.class, "settingsPanel.poisonPower",
					0, 0, Poison.MAX_TOXIC, 0, object.getPosionPower(), null, 
				e->object.setPosionPower(e));
		poisonPower.setVisible(object.getPosionType() != Poison.TYPE.UNEQUIPPED);
		final var PoiosnTypeS = new SettingsSelect<>(CellEditor.class, "settingsPanel.poisonType", PT, Poison.TYPE.UNEQUIPPED, object.getPosionType(), e -> {
			object.setPosionType(e);
			poisonPower.setValue(object.getPosionPower());
			poisonPower.setVisible(e != Poison.TYPE.UNEQUIPPED);
		});
		settingsPanel.add(PoiosnTypeS);
		settingsPanel.add(poisonPower);
		settingsPanel.add(new SettingsSlider<>(CellEditor.class, "settingsPanel.buoyancy",
					-100, 0, 100, -100, object.getBuoyancy(), 100, 
				e->object.setBuoyancy(e)));
		settingsPanel.add(new SettingsSlider<>(CellEditor.class, "settingsPanel.hp_by_div",
					0, (int)AliveCellProtorype.MAX_HP/10, (int)AliveCellProtorype.MAX_HP, 0, object.getHp_by_div(), null, 
				e->object.setHp_by_div(e)));
		settingsPanel.add(new SettingsSlider<>(CellEditor.class, "settingsPanel.tolerance",
					0, 2, AliveCellProtorype.DEF_MINDE_SIZE, 0, object.getTolerance(), null, 
				e->object.setTolerance(e)));
		settingsPanel.add(javax.swing.Box.createVerticalGlue()); //Чтобы кнопки были внизу
		
		final var panelSL = new javax.swing.JPanel();
		panelSL.setLayout(new javax.swing.BoxLayout(panelSL, javax.swing.BoxLayout.X_AXIS));
		{
			final var load = new javax.swing.JButton();
			Configurations.setIcon(load,"loadCell");
			load.addActionListener(e -> {
				LoadSaveFactory.load("BioLife", "zbcell", filename -> {
					var js = SaveAndLoad.load(filename);
					try{
						final var c = js.load((j, version)-> {
							final var node = Configurations.tree.makeTree();
							j.add("GenerationTree", node.getBranch()); //Так ну совсем совсем нельзя делать... А я делаю :(
							final var acell = new AliveCell(j, Configurations.tree, version);
							node.remove();
							return acell;
						},"cell");
						setObject(c);
					} catch (Exception ex){
						Logger.getLogger(CellEditor.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
						JOptionPane.showMessageDialog(null,	Configurations.getHProperty(Menu.class,"loadCell.error",ex.getMessage()), "BioLife", JOptionPane.ERROR_MESSAGE);
					}
				});
			});
			load.setToolTipText(Configurations.getHProperty(CellEditor.class, "load"));
			load.setFocusable(false);
			panelSL.add(load);
		}
		{
			final var save = new javax.swing.JButton();
			Configurations.setIcon(save,"saveCell");
			save.addActionListener(e -> {
				final var cell = object;
				LoadSaveFactory.save("BioLife", "zbcell", name -> {
					var js = SaveAndLoad.save(name, Configurations.VERSION);
					js.save(new SaveAndLoad.Serialization() {
						@Override public String getName() { return "cell";}
						@Override public JSON getJSON() {return cell.toJSON();}
					});
				}, false);
			});
			save.setToolTipText(Configurations.getHProperty(CellEditor.class, "save"));
			save.setFocusable(false);
			panelSL.add(save);
		}
		{
			final var copy = new javax.swing.JButton();
			Configurations.setIcon(copy,"clipboardCopy");
			copy.addActionListener(e -> {
				final var j = object.toJSON();
				final var stringSelection = new java.awt.datatransfer.StringSelection(j.toJSONString());
				final var clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			});
			copy.setToolTipText(Configurations.getHProperty(CellEditor.class, "copy"));
			copy.setFocusable(false);
			panelSL.add(copy);
		}
		{
			final var close = new javax.swing.JButton();
			Configurations.setIcon(close,"close");
			close.addActionListener(e -> closeDialog(null));
			close.setToolTipText(Configurations.getHProperty(CellEditor.class, "close"));
			close.setFocusable(false);
			panelSL.add(close);
		}
		panelSL.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK));
		settingsPanel.add(panelSL);
	}
	/**Создаёт панель со всеми прерываниями*/
	private void makeInterraptPanel(){
		interaptPanel.removeAll();
		final var dna = object.getDna();
		final var interrupts = dna.interrupts;
		final var objects = CellObject.OBJECT.values;
		final var ints = IntStream.range(0, dna.size).boxed().toArray(Integer[]::new);
		for (int i = 0; i < interrupts.length; i++) {
			final var index = i;
			final var aInt = interrupts[index];
			final var o = objects[index];
			interaptPanel.add(new SettingsSelect<>(CellEditor.class,"interraptPanel."+o.name(),ints,i % dna.size, aInt, e -> {
				interrupts[index] = e;
				centralPanel.repaint();
			}));
		}
	}
	/**Создаёт заголовок страницы*/
	private void updateHeaderPanel(){
		final var dna = object.getDna();
		PClabel.setText("PC: " + dna.getPC());
		jampBack.setEnabled(!history.isEmpty());
		
		nextCmdPanel.removeAll();
		final var cmd = dna.get();
		if(cmd.getClass().equals(MapObjects.dna.Jump.class)){
			final var j = ((MapObjects.dna.Jump) cmd).JAMP;
			final var next = new JButton(Configurations.getProperty(CellEditor.class,"JAMP.L", j));
			next.setToolTipText(Configurations.getProperty(CellEditor.class,"JAMP.T"));
			setButtonParam(next);
			next.addActionListener( e-> nextDNA(j));
			nextCmdPanel.add(next);
		} else if(cmd.getClass().equals(MapObjects.dna.Loop.class)){
			final var j = ((MapObjects.dna.Loop) cmd).LOOP;
			final var next = new JButton(Configurations.getProperty(CellEditor.class,"LOOP.L", j));
			next.setToolTipText(Configurations.getProperty(CellEditor.class,"LOOP.T"));
			setButtonParam(next);
			next.addActionListener( e-> nextDNA(-j));
			nextCmdPanel.add(next);
		} else if(cmd.getCountBranch() > 0) {
			for(var j = 0 ; j < cmd.getCountBranch() ; j++){
				var nPC = dna.get(1 + cmd.getCountParams() + j, false); //Индекс того гена, куда мы прыгнем
				final var color = Utils.Utils.getHSBColor(((double)j)/cmd.getCountBranch(), 1, 1, 0.5);
				final var next = new JButton(Configurations.getProperty(CellEditor.class,"BRANCH.L", j,nPC)){
					@Override public void paintComponent(Graphics g){
						super.paintComponent(g);
						g.setColor(color);
						g.fillRect(0, 0, getWidth(), getHeight());
					}
				};
				next.setToolTipText(Configurations.getHProperty(CellEditor.class,"BRANCH.T",j,cmd.getLongName(),cmd.getBranch(object, j, dna).replaceAll("<", "&#60;")));
				setButtonParam(next);
				next.addActionListener( e-> nextDNA(+nPC));
				nextCmdPanel.add(next);
			}
		}
		if(nextCmdPanel.getComponentCount() == 0){
			final var next = new JButton(Configurations.getProperty(CellEditor.class,"NEXT.L", cmd.size()));
			next.setToolTipText(Configurations.getProperty(CellEditor.class,"NEXT.T"));
			setButtonParam(next);
			next.addActionListener( e-> nextDNA(cmd.size()));
			nextCmdPanel.add(next);
		}
	}
	
	private class PaintJPanel extends javax.swing.JPanel{
		/**Цвета прерываний*/
		private final Color[] I_COLOR;
		/**Радиус нарисованной ДНК*/
		private double Rdna = 0;
		/**Ширина нарисованной ДНК*/
		private double Wdna = 0;
		/**Выделенный угол или null, если ни какой угол не выделен*/
		private Double selectAngle = null;
		/**Выбранная команда*/
		private CommandDNA selectComand = null;
		/**Индекс выбранной команды*/
		private Integer selectComandIndex = null;
		/**Выбраный кодон команды*/
		private Integer codon = null;
		/**Всплывшее окно рядом с командой ДНК*/
		private static Popup popup = null;
		/**Сама подсказка*/
		private final JToolTip DnaToolTip;
		/**Выделение жирненьким по умолчанию*/
		private final static java.awt.BasicStroke DASHED = new java.awt.BasicStroke(3);
		
		PaintJPanel(){
			I_COLOR = new Color[CellObject.OBJECT.lenght];
			for (int i = 0; i < I_COLOR.length; i++) {
				I_COLOR[i] = Utils.Utils.getHSBColor(((double)i)/I_COLOR.length, 1, 0.7, 0.5);
			}
			
			final var mouseListener = new java.awt.event.MouseAdapter(){
				@Override public void mouseMoved(MouseEvent e){
					final var h = getHeight();
					final var w = getWidth();
					final var cx = w / 2;
					final var cy = h / 2;
					final var x = cx - e.getX();
					final var y = cy - e.getY();
					final var rxy = x*x + y*y;
					if(Math.pow(Rdna-Wdna, 2) <= rxy && rxy <= Math.pow(Rdna+Wdna, 2)){
						//Мы попали аккурат в ДНК. Нам теперь нужен угол
						selectAngle = Math.atan2(y, x) + Math.PI;
						repaint();
					} else if(selectAngle != null){
						selectAngle = null; selectComand = null;
						if(popup != null)popup.hide();
						repaint();
					}
				}
				@Override public void mouseClicked(MouseEvent e){
					if(e.getButton() == MouseEvent.BUTTON3){
						var dna = object.getDna();
						instruments.show(PaintJPanel.this, e.getX(), e.getY());
						removeGen.setVisible(dna.size > 10);
						remove.setVisible(dna.size > 2);
					} else if(e.getButton() == MouseEvent.BUTTON1 && codon != null && pstatus != POPUP_STATUS.UNDEF){
						var dna = object.getDna();
						switch (pstatus) {
							case INC -> {
								dna = dna.update(codon, false, dna.get(codon, false) + 1);
							}
							case DEC -> {
								dna = dna.update(codon, false, dna.get(codon, false) - 1);
							}
							case REM -> {
								dna = dna.compression(codon, false);
								if(dna.size <= 2) set(POPUP_STATUS.UNDEF);
							}
							case REM_GEN -> {
								dna = dna.compression(selectComandIndex, true, selectComand.size());
								if(dna.size <= 10) set(POPUP_STATUS.UNDEF);
							}
							case ADD -> {
								dna = dna.doubling(codon, false);
							}
							case COPY -> {
								dna = dna.doubling(selectComandIndex, true, selectComand.size());
							}
							default -> {return;}
						}
						object.setDna(dna);
						mouseMoved(e); //Мы ещё и пришли в эту точку, а точка теперь по факту в другом месте!
						updateHeaderPanel();
						repaint();
					}
				}
			};
			addMouseListener(mouseListener);
			addMouseMotionListener(mouseListener);
			addMouseWheelListener(mouseListener);
			DnaToolTip = createToolTip();
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			try{
				paintComponent((Graphics2D)g);
			} catch(Exception ex){ //Вообще не ожидаются такие события... Но кто мы такие, чтобы спорить с фактами?
				Logger.getLogger(CellEditor.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}
		}
		/**Отрисовывает ДНК клетки*/
		public void paintComponent(Graphics2D g) {	
			final var h = getHeight();
			final var w = getWidth();
			final var cx = w / 2;
			final var cy = h / 2;
			final var min = Math.min(h, w);
			final var dna_o = object.getDna();
			int size;
			for (size = 0; size < dna_o.size; ) {
				size += dna_o.get(size).size();
			}
			final var BETWIN_LINE = 4; //Интервал между линиями связывающими прерывания и гены
			Wdna = min / 80d; //"ширина" спирали ДНК
			Rdna = min/2  - BETWIN_LINE * CellObject.OBJECT.lenght - Wdna*2; //Радиус ДНК
			printInterrapts(g,size,cx,cy, Rdna,Wdna,h,BETWIN_LINE);
			printDNA(g, cx, cy, size, Rdna, Wdna);
			{//Рисуем толстую стрелочку от ПК
				final var os = g.getStroke();
				g.setStroke(DASHED);
				g.setColor(Color.BLACK);
				final var my = cy - Rdna - 3 * Wdna;
				g.draw(new Line2D.Double(cx, 0, cx, my));
				g.draw(new Line2D.Double(cx, my, cx + my / 2, my / 2));
				g.draw(new Line2D.Double(cx, my, cx - my / 2, my / 2));
				g.setStroke(os);
			}
			printJumps(g, cx, cy, size, Rdna, Wdna);
			{//Рисуем кодоны
				final var dna = object.getDna();
				final var dr = (Math.PI) / size; //Какой угол относится к одной команде ДНК
				final var dr2 = dr*2;
				final var r = Rdna;
				final var wDNA = Wdna - 2;
				codon = null;
				for (var i = 0; i < dna.size; i++ ) {
					final var num = dna.get(i, false);
					var a = i * dr2 - Math.PI / 2;
					if(selectAngle != null && isOneAngle(a - dr, selectAngle, a+dr)) codon = i;
					var tx = cx + (r) * Math.cos(a);
					var ty = cy + (r) * Math.sin(a); //Центр кружочка
					g.setColor(getBackground());
					g.fill(new Ellipse2D.Double(tx - wDNA, ty - wDNA, wDNA*2, wDNA*2));
					g.setColor(Color.BLACK);
					Utils.Utils.centeredText(g,(int) tx,(int) ty, 12, String.valueOf(num));
				}
			}
		}
		/** Рисует все прыжки внутри ДНК
		 * @param g холст
		 * @param cx центр холста
		 * @param cy центр холста
		 * @param size количество отображаемых кадонов
		 * @param r радиус ДНК
		 * @param rD ширина ДНК
		 */
		private void printJumps(Graphics2D g, final int cx, final int cy, int size, final double r, final double rD) {
			final var dna = object.getDna();
			final var PC = dna.getPC();
			final var dr2 = (Math.PI * 2) / size; //Какой угол относится к одной команде ДНК
			final var rL = r - 2*rD; //На каком радиусе чертим линии
			final var center = new Point2D.Double(cx,cy);
			final var os = g.getStroke();
			
			for (var i = 0; i < dna.size; ) {
				final var cmd = dna.get(i);
				var index = dna.normalization(PC + i);
				if(index < PC) index += dna.size; //Проворачиваем на один круг
				index -= PC; //А теперь переводим индекс в нулевую позицию и получаем где на круге этот индекс находится
				final var cmd_a = index * dr2 - Math.PI / 2; //Угол, на котором находится команда
				if(index == 0){
					g.setStroke(DASHED);
				}
				
				
				if(cmd.getClass().equals(MapObjects.dna.Jump.class)){
					var nPC = dna.normalization(PC + i + ((MapObjects.dna.Jump) cmd).JAMP); //Индекс того гена, куда мы прыгнем
					if(nPC < PC) nPC += dna.size; //Проворачиваем на один круг
					nPC -= PC; //А теперь переводим индекс в нулевую позицию и получаем где на круге этот индекс находится
					final var cmd_to = nPC * dr2 - Math.PI / 2;
					final int sx = (int) (cx + rL * Math.cos(cmd_a)); //Координаты старта
					final int sy = (int) (cy + rL * Math.sin(cmd_a));
					final int ex = (int) (cx + rL * Math.cos(cmd_to)); //Координаты конца
					final int ey = (int) (cy + rL * Math.sin(cmd_to));
					bezie(g, new Point2D.Double(sx,sy),center,new Point2D.Double(ex,ey));
				} else if(cmd.getClass().equals(MapObjects.dna.Loop.class)){
					var nPC = dna.normalization(PC + i - ((MapObjects.dna.Loop) cmd).LOOP); //Индекс того гена, куда мы прыгнем
					if(nPC < PC) nPC += dna.size; //Проворачиваем на один круг
					nPC -= PC; //А теперь переводим индекс в нулевую позицию и получаем где на круге этот индекс находится
					final var cmd_to = nPC * dr2 - Math.PI / 2;
					final int sx = (int) (cx + rL * Math.cos(cmd_a)); //Координаты старта
					final int sy = (int) (cy + rL * Math.sin(cmd_a));
					final int ex = (int) (cx + rL * Math.cos(cmd_to)); //Координаты конца
					final int ey = (int) (cy + rL * Math.sin(cmd_to));
					bezie(g, new Point2D.Double(sx,sy),center,new Point2D.Double(ex,ey));
				} else if(cmd.getCountBranch() > 0) {
					var anglS = cmd_a + dr2 * (1 + cmd.getCountParams());
					for(var j = 0 ; j < cmd.getCountBranch() ; j++, anglS += (dr2)){
						var nPC = dna.normalization((PC + i) + dna.get(PC + i + 1 + cmd.getCountParams() + j, true)); //Индекс того гена, куда мы прыгнем
						if(nPC < PC) nPC += dna.size; //Проворачиваем на один круг
						nPC -= PC; //А теперь переводим индекс в нулевую позицию и получаем где на круге этот индекс находится
						final var cmd_to = nPC * dr2 - Math.PI / 2;
						final int sx = (int) (cx + rL * Math.cos(anglS)); //Координаты старта
						final int sy = (int) (cy + rL * Math.sin(anglS));			
						final int ex = (int) (cx + rL * Math.cos(cmd_to)); //Координаты конца
						final int ey = (int) (cy + rL * Math.sin(cmd_to));
						if(index == 0)
							g.setColor(Utils.Utils.getHSBColor(((double)j)/cmd.getCountBranch(), 1, 1, 0.5));
						bezie(g, new Point2D.Double(sx,sy),center,new Point2D.Double(ex,ey));
					}
					if(index == 0)
						g.setColor(Color.BLACK);
				}
				if(index == 0){
					g.setStroke(os);
				}
				i += cmd.size();
			}
		}
		/** Рисует все прерывания на холсте
		 * @param g холст
		 * @param size размер ДНК
		 * @param cx центр холста 
		 * @param cy центр холста
		 * @param r радиус ДНК
		 * @param rD ширина ДНК
		 * @param h высота экрана
		 * @param BETWIN_LINE 
		 */
		private void printInterrapts(Graphics2D g, int size, final int cx,final int cy, final double r, final double rD, final int h, final int BETWIN_LINE) {
			final var locationOnScreen = this.getLocationOnScreen();
			final var dna = object.getDna();
			final var interrupts = dna.interrupts;
			final var PC = dna.getPC();
			final var dr = (Math.PI) / size; //Какой угол относится к одной команде ДНК
			final var os = g.getStroke();
			
			//Рисуем связи от прерываний к ДНК
			//От каждого прерывания надо нарисовать линию к его нуклеотиду.
			//Эти линии будут огибать центральный рисунок со всех сторон
			//
			//            _____
			//           /  0  \     |I1
			//          /7     1\    |I2
			//         (6       2)   |I3
			//          \5     3/    |
			//           \__4__/     |In
			//
			// В зону 1-3 все линии подходят прямо
			// В зону 3-5 все линии подходят снизу
			// В зону 6 все линии подоходя слева. А вот слева через верх (6А) или слева через низ(6В), зависит от длины лини
			// В зону 7-1 все линии подходят сверху
			
			var count0 = 0;
			var count1 = 0;
			var count2 = 0;
			var count3 = 0;
			var count4 = 0;
			var count6A = 0;
			var count6B = 0;
			for (int i = 0; i < interrupts.length; i++) {
				g.setColor(I_COLOR[i]);
				var interrupt = interrupts[i];
				if(interrupt < PC) interrupt += dna.size; //Проворачиваем на один круг
				interrupt -= PC; //А теперь переводим индекс в нулевую позицию
				final var field = ((8 * interrupt + size / 2) / size) % 8; //Номер зоны
				final var cmd_a = interrupt * 2 * dr - Math.PI / 2; //Угол, на котором находится команда
				final var cos = Math.cos(cmd_a);
				final var sin = Math.sin(cmd_a);
				final int tx = (int) (cx + (r + 2*rD) * cos); //Координаты этой команды
				final int ty = (int) (cy + (r + 2*rD) * sin);
				
				final var panel = (SettingsSelect<Integer>)interaptPanel.getComponent(i);
				var point = panel.getLocationOnScreen();
				point.move(point.x - locationOnScreen.x, point.y - locationOnScreen.y + panel.getHeight() / 2);//Вернём положение относительно нас
				final var fieldStart = 1 + 3 * point.y / h; //А это зона, из которой мы выходим
				if(interrupt == 0) g.setStroke(DASHED);
				{//Сначала рисуем стартовую полочку
					switch (fieldStart) {//Откуда выходим
						case 1 -> {
							switch (field) { //куда мы целимся
								case 0,1,6,7 -> {
									final var xel = point.x - count1 * BETWIN_LINE - 10; //Стартовая полочка
									g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
									point.x = xel;
									count1++;
								}
								case 2 -> {
									final var count = Math.max(count1, count2); //Линия на максимальном удалении для обоих блоков
									final var xel = point.x - count * BETWIN_LINE - 10; //Стартовая полочка
									g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
									point.x = xel;
									count1 = count2 = count + 1;
								}
								default -> {
									final var count = Math.max(Math.max(count1, count2), count3);
									final var xel = point.x - count * BETWIN_LINE - 10; //Стартовая полочка
									g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
									point.x = xel;
									count1 = count2 = count3 = count + 1;
								}
							}
						}
						case 2-> {
							switch (field) { //куда мы целимся
								case 2 -> {
									final var xel = point.x - count2 * BETWIN_LINE - 10; //Стартовая полочка
									g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
									point.x = xel;
									count2++;
								}
								case 0,1,7 -> {
									final var count = Math.max(count1, count2); //Линия на максимальном удалении для обоих блоков
									final var xel = point.x - count * BETWIN_LINE - 10; //Стартовая полочка
									g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
									point.x = xel;
									count1 = count2 = count + 1;
								}
								case 3,4,5 -> {
									final var count = Math.max(count3, count2); //Линия на максимальном удалении для обоих блоков
									final var xel = point.x - count * BETWIN_LINE - 10; //Стартовая полочка
									g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
									point.x = xel;
									count3 = count2 = count + 1;
								}
								default ->{
									if(ty > cy ){ //Будем рисовать вниз
										final var count = Math.max(count1, count2); //Линия на максимальном удалении для обоих блоков
										final var xel = point.x - count * BETWIN_LINE - 10; //Стартовая полочка
										g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
										point.x = xel;
										count1 = count2 = count + 1;
									} else {
										final var count = Math.max(count3, count2); //Линия на максимальном удалении для обоих блоков
										final var xel = point.x - count * BETWIN_LINE - 10; //Стартовая полочка
										g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
										point.x = xel;
										count3 = count2 = count + 1;
									}
								}
							}
						}
						default -> {
							switch (field) { //куда мы целимся
								case 3,4,5,6 -> {
									final var xel = point.x - count3 * BETWIN_LINE - 10; //Стартовая полочка
									g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
									point.x = xel;
									count3++;
								}
								case 2 -> {
									final var count = Math.max(count3, count2); //Линия на максимальном удалении для обоих блоков
									final var xel = point.x - count * BETWIN_LINE - 10; //Стартовая полочка
									g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
									point.x = xel;
									count3 = count2 = count + 1;
								}
								default -> {
									final var count = Math.max(Math.max(count1, count2), count3);
									final var xel = point.x - count * BETWIN_LINE - 10; //Стартовая полочка
									g.draw(new Line2D.Double(point.x, point.y,xel, point.y));
									point.x = xel;
									count1 = count2 = count3 = count + 1;
								}
							}
						}
					}
				}
				{
					switch (field) { //куда мы целимся
						case 1,2,3 -> {
							g.draw(new Line2D.Double(point.x, point.y, point.x, ty));
							point.y = ty;
							g.draw(new Line2D.Double(point.x, point.y, tx, point.y));
						}
						case 0,7 -> {
							final var xey = (count0++) * BETWIN_LINE; //Верхняя планка
							g.draw(new Line2D.Double(point.x, point.y, point.x,xey));
							point.y = xey;
							g.draw(new Line2D.Double(point.x, point.y, tx,point.y));
							point.x = tx;
							g.draw(new Line2D.Double(point.x, point.y, point.x,ty));
						}
						case 4,5 -> {
							final var xey = h - ((count4++) + 1) * BETWIN_LINE; //Нижняя планка
							g.draw(new Line2D.Double(point.x, point.y, point.x,xey));
							point.y = xey;
							g.draw(new Line2D.Double(point.x, point.y, tx,point.y));
							point.x = tx;
							g.draw(new Line2D.Double(point.x, point.y, point.x,ty));
						}
						default -> {
							final int xey, xex;
							if(ty > cy){ //Будем рисовать вниз
								xey = h - ((count4++) + 1) * BETWIN_LINE;
								xex = ((count6B++) + 1) * BETWIN_LINE;
							} else {
								xey = (count0++) * BETWIN_LINE;
								xex = ((count6A++) + 1) * BETWIN_LINE;
							}
							g.draw(new Line2D.Double(point.x, point.y, point.x,xey));
							point.y = xey;
							g.draw(new Line2D.Double(point.x, point.y, xex,point.y));
							point.x = xex;
							g.draw(new Line2D.Double(point.x, point.y, point.x,ty));
							point.y = ty;
							g.draw(new Line2D.Double(point.x, point.y, tx, point.y));
						}
					}
				}
				if(interrupt == 0) g.setStroke(os);
			}
		}
		/** Рисует спираль ДНК
		 * @param g холст, на котором рисуем
		 * @param cx координаты центра
		 * @param cy координаты центра
		 * @param size количество нуклиотидов в ДНК
		 * @param r кадиус ДНК
		 * @param rD ширина ДНК
		 */
		private void printDNA(Graphics2D g, final int cx, final int cy,  int size, double r, final double rD) {
			final var dna = object.getDna();
			final var PC = dna.getPC();
			final var os = g.getStroke();
			
			//Рисуем ДНК
			final var dr = (Math.PI) / size; //Какой угол относится к одной команде ДНК
			final var dr2 = dr*2;
			for (var i = 0; i < dna.size; ) {
				var a = i * dr2 - Math.PI / 2;
				var af = a - dr;
				final var index = dna.normalization(PC + (i++));
				//Тут приходится не реально крутиться, так как параметры команды вычитываются относительно PC!
				//Поэтому каждый раз приходится докручивать ДНК до нужного нам значения РС
				dna.next(index - dna.getPC()); 
				final var cmd = dna.get();
				final var ae = a + dr + dr2 * (cmd.size() - 1); 
				final var isSelect = selectAngle != null && isOneAngle(af, selectAngle, ae);
				
				if(isSelect){
					g.setStroke(DASHED);
					generateToolTip(dna);
				}
				//Рисуем начало команды
				{
					g.setColor(Color.BLUE);
					final var cos = Math.cos(a);
					final var sin = Math.sin(a);
					var tx = cx + (r - 2*rD) * cos;
					var ty = cy + (r - 2*rD) * sin;
					print(g,tx,ty,af,String.valueOf(index));

					drawStartEnd(g,cx,cy,r,rD,af, true,dr, size);
					tx = cx + (r + 2*rD) * cos;
					ty = cy + (r + 2*rD) * sin;
					print(g,tx,ty,a,cmd.getShotName());
				}
				//Её параметры
				{
					g.setColor(new Color(255, 70, 70, 150));
					for (int j = 0; j < cmd.getCountParams(); j++, i++) {
						final var ap = i * dr2 - Math.PI / 2;
						af = ap - dr * 2;
						drawCentral(g,cx,cy,r,rD, af, dr);
						final var tx = cx + (r + 3*rD) * Math.cos(ap);
						final var ty = cy + (r + 3*rD) * Math.sin(ap);
						print(g,tx,ty,ap,cmd.getParam(object, j, dna));
					}
				}
				{//Её ветви
					g.setColor(new Color(100, 100, 100, 150));
					for (int j = 0; j < cmd.getCountBranch(); j++, i++) {
						final var ap = i * dr2 - Math.PI / 2;
						af = ap - dr2;
						drawCentral(g,cx,cy,r,rD, af, dr);
						final var tx = cx + (r + 4*rD) * Math.cos(ap);
						final var ty = cy + (r + 4*rD) * Math.sin(ap);
						print(g,tx,ty,ap,cmd.getBranch(object, j, dna));
					}
				}
				//А теперь рисуем завершение ветви
				g.setColor(Color.BLUE);
				af = (i * dr2 - Math.PI / 2) - dr2;
				drawStartEnd(g,cx,cy,r,rD,af, false,dr, size);
				
				if(isSelect){
					g.setStroke(os);
				}
			}
			dna.next(PC - dna.getPC());
		}
		/** Рисует стартовую или финальную часть спирали
		 * @param g холст
		 * @param cx центр холста
		 * @param cy центр холста
		 * @param r радиус спирали
		 * @param width ширина спирали
		 * @param angle стартовый угол, на котором начинается рисовка
		 * @param isStart стартовая часть?
		 * @param dr какой угол занимает эта дуга
		 * @param size количество отображаемых нуклеотидов
		 */
		private void drawStartEnd(Graphics2D g, int cx, int cy, double r,double width, double angle, boolean isStart,double dr, int size){
		
			double fx1, fx2, fy1, fy2;
			fx1 = fx2 = cx + r * Math.cos(angle);
			fy1 = fy2 = cy + r * Math.sin(angle);
			for(var a = 0d ; a < dr + Math.PI/360; a += Math.PI/180){
				final var rx = isStart ? width * Math.sin(a*size / 2) : width * Math.cos(a*size / 2);

				final var rad1 = r + rx;
				final var rad2 = r - rx;
				
				final var cos = Math.cos(angle + a);
				final var sin = Math.sin(angle + a);

				final var tx1 = cx + rad1 * cos;
				final var ty1 = cy + rad1 * sin;
				final var tx2 = cx + rad2 * cos;
				final var ty2 = cy + rad2 * sin;

				g.draw(new Line2D.Double(fx1, fy1, tx1, ty1));
				g.draw(new Line2D.Double(fx2, fy2, tx2, ty2));
				g.draw(new Line2D.Double(fx1, fy1,fx2, fy2));
				fx1 = tx1; fy1 = ty1; fx2 = tx2; fy2 = ty2;
			}
		}
		/** Рисует промежуточную часть спирали
		 * @param g холст
		 * @param cx центр холста
		 * @param cy центр холста
		 * @param r радиус спирали
		 * @param width ширина спирали
		 * @param angle стартовый гол, на котором начинается рисовка
		 * @param dr какой угол занимает эта дуга
		 * @return новый радиус, если спираль переходная. Иначе - старый радиус
		 */
		private void drawCentral(Graphics2D g, int cx, int cy, double r,double width, double angle, double dr){
			final var step = Math.PI/180; //Как часто вырисовывать ДНК
			
			var fx1 = cx + (r+width) * Math.cos(angle);
			var fy1 = cy + (r+width) * Math.sin(angle);
			var fx2 = cx + (r-width) * Math.cos(angle);
			var fy2 = cy + (r-width) * Math.sin(angle);
			for(var a = 0d ; a < dr * 2 + step / 2; a += step){
				final var rad1 = r + width;
				final var rad2 = r - width;
				
				final var cos = Math.cos(angle + a);
				final var sin = Math.sin(angle + a);

				final var tx1 = cx + rad1 * cos;
				final var ty1 = cy + rad1 * sin;
				final var tx2 = cx + rad2 * cos;
				final var ty2 = cy + rad2 * sin;

				g.draw(new Line2D.Double(fx1, fy1, tx1, ty1));
				g.draw(new Line2D.Double(fx2, fy2, tx2, ty2));
				g.draw(new Line2D.Double(fx1, fy1,fx2, fy2));
				fx1 = tx1; fy1 = ty1; fx2 = tx2; fy2 = ty2;
			}
		}
		/** Печатает текст под углом
		 * @param g холст
		 * @param cx координата центра, где нужно напечатать текст
		 * @param cy координата центра, где нужно напечатать текст
		 * @param angle угол, на которй надо повернуть текст
		 * @param text сам текст
		 */
		private void print(Graphics2D g,double cx, double cy, double angle, String text){
			final var ot = g.getTransform();
			g.rotate(angle + Math.PI / 2, cx, cy);
			Utils.Utils.centeredText(g, (int) cx,(int)cy, 12, text);
			g.setTransform(ot);
		}
		/** Рисует кривую безье по трём точкам
		 * @param g холст
		 * @param from откуда 
		 * @param intermediate промежуточная точка
		 * @param to куда
		 */
		private void bezie(Graphics2D g,java.awt.Point.Double from,java.awt.Point.Double intermediate,java.awt.Point.Double to){
			final double F0 = 1;
			final double F1 = 1;
			final double F2 = 1 * 2;
			
			final var lenght = from.distance(intermediate) +  intermediate.distance(to);

			var pref = from;
			var start = from;
			var isPrint = true;
			for(var i = 0 ; i < lenght; i+=2){
				final var t = i/lenght;
				final var ut = 1 - t;
				final var l_stroke = 1 + ut * 10;
				final var B0 = (F2 / (F0 * F2)) * Math.pow(t, 0) * Math.pow(ut, 2);
				final var B1 = (F2 / (F1 * F1)) * Math.pow(t, 1) * Math.pow(ut, 1);
				final var B2 = (F2 / (F2 * F0)) * Math.pow(t, 2) * Math.pow(ut, 0);
				final var x = B0 * from.x + B1 * intermediate.x + B2 * to.x;
				final var y = B0 * from.y + B1 * intermediate.y + B2 * to.y;
				final var point = new java.awt.Point.Double(x,y);
				final var delta = start.distance(point);
				if(delta > l_stroke){
					start = point;
					isPrint = !isPrint;
				}
				if(isPrint)
					g.draw(new Line2D.Double(pref, point));
				pref = point;
			}
		}
		/** Проверяет, что точка попадает в промежуток [from;to]
		 * @param from первый угол
		 * @param a анализируемый угол
		 * @param to второй угол
		 * @return true, если анализируемый угол находится между углами
		 */
		private boolean isOneAngle(double from, double a, double to){
			final var PI2 = Math.PI * 2;
			if(from > 0 && a > 0 && to > 0){
				return from <= a && a <= to;
			} else {
				a -= from;
				to -= from; //Сдвигаем всех в начало координат
				if(a > PI2) a -= PI2; //И, если провернулся a, то возвращаем его обратно
				return 0 <= a && a <= to;
			}
		}
		/**Создаёт подсказку из команды ДНК
		 * @param dna ДНК в которой PC установлен на выбранную команду
		 */
		private void generateToolTip(DNA dna){
			final var isFull = CommandDNA.isFullMod();
			CommandDNA.setFullMod(true);
			final var cmd = selectComand = dna.get();
			selectComandIndex = dna.getPC();
			final var text = new StringBuilder();
			text.append("<html>&nbsp;&nbsp;");
			text.append(dna.getPC());
			text.append("=");
			text.append(cmd.getLongName());
			text.append("<br>");
			for(var i = 0 ; i < cmd.getCountParams() ; i++){
				text.append(" П ");
				text.append(cmd.getParam(object, i, dna).replaceAll("<", "&#60;"));
				text.append("<br>");
			}
			for(var i = 0 ; i < cmd.getCountBranch(); i++){
				text.append("  B");
				text.append(i);
				text.append(" ");
				text.append(cmd.getBranch(object, i, dna).replaceAll("<", "&#60;"));
				text.append("<br>");
			}
			showToolTip(text.toString());
			CommandDNA.setFullMod(isFull);
		}
		/** Герерирует бегающую за мышкой подсказку на экране
		 * @param text текст, который будет в подсказке
		 */
		private void showToolTip(String text){
			final var ml = java.awt.MouseInfo.getPointerInfo().getLocation();
			DnaToolTip.setTipText(text);
			if(popup != null) popup.hide();
			if(pstatus != POPUP_STATUS.UNDEF) return; //Не показываем подсказку
			popup = PopupFactory.getSharedInstance().getPopup(this, DnaToolTip, ml.x, ml.y);
			popup.show();
		}
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        instruments = new javax.swing.JPopupMenu();
        def = new javax.swing.JMenuItem();
        inc = new javax.swing.JMenuItem();
        dec = new javax.swing.JMenuItem();
        remove = new javax.swing.JMenuItem();
        removeGen = new javax.swing.JMenuItem();
        add = new javax.swing.JMenuItem();
        copyGen = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        settingsPanel = new javax.swing.JPanel();
        botPanel = new javax.swing.JPanel();
        interaptPanel = new javax.swing.JPanel();
        centralPanel = new javax.swing.JPanel();
        PCpanel = new javax.swing.JPanel();
        jampBack = new javax.swing.JButton();
        decrement = new javax.swing.JButton();
        PClabel = new javax.swing.JLabel();
        increment = new javax.swing.JButton();
        nextCmdPanel = new javax.swing.JPanel();

        def.setText(Configurations.getProperty(CellEditor.class,"instruments.def"));
        def.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defActionPerformed(evt);
            }
        });
        instruments.add(def);

        inc.setText(Configurations.getProperty(CellEditor.class,"instruments.inc"));
        inc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incActionPerformed(evt);
            }
        });
        instruments.add(inc);

        dec.setText(Configurations.getProperty(CellEditor.class,"instruments.dec"));
        dec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decActionPerformed(evt);
            }
        });
        instruments.add(dec);

        remove.setText(Configurations.getProperty(CellEditor.class,"instruments.remove"));
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });
        instruments.add(remove);

        removeGen.setText(Configurations.getProperty(CellEditor.class,"instruments.removeGEN"));
        removeGen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGenActionPerformed(evt);
            }
        });
        instruments.add(removeGen);

        add.setText(Configurations.getProperty(CellEditor.class,"instruments.add"));
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });
        instruments.add(add);

        copyGen.setText(Configurations.getProperty(CellEditor.class,"instruments.copy"));
        copyGen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyGenActionPerformed(evt);
            }
        });
        instruments.add(copyGen);

        setTitle(Configurations.getProperty(CellEditor.class,"title"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1068, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        settingsPanel.setLayout(new javax.swing.BoxLayout(settingsPanel, javax.swing.BoxLayout.Y_AXIS));

        interaptPanel.setLayout(new javax.swing.BoxLayout(interaptPanel, javax.swing.BoxLayout.Y_AXIS));

        centralPanel.setLayout(new java.awt.BorderLayout());

        PCpanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jampBack.setText("--");
        jampBack.setToolTipText(Configurations.getProperty(CellEditor.class,"history"));
        jampBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jampBackActionPerformed(evt);
            }
        });
        PCpanel.add(jampBack);

        decrement.setText("-");
        decrement.setToolTipText(Configurations.getProperty(CellEditor.class,"decrement"));
        decrement.setAlignmentX(0.5F);
        decrement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decrementActionPerformed(evt);
            }
        });
        PCpanel.add(decrement);

        PClabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        PClabel.setText("PC:0");
        PClabel.setAlignmentX(0.5F);
        PCpanel.add(PClabel);

        increment.setText("+");
        increment.setToolTipText(Configurations.getProperty(CellEditor.class,"increment"));
        increment.setAlignmentX(0.5F);
        increment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incrementActionPerformed(evt);
            }
        });
        PCpanel.add(increment);

        nextCmdPanel.setLayout(new javax.swing.BoxLayout(nextCmdPanel, javax.swing.BoxLayout.LINE_AXIS));
        PCpanel.add(nextCmdPanel);

        centralPanel.add(PCpanel, java.awt.BorderLayout.NORTH);

        javax.swing.GroupLayout botPanelLayout = new javax.swing.GroupLayout(botPanel);
        botPanel.setLayout(botPanelLayout);
        botPanelLayout.setHorizontalGroup(
            botPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, botPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(centralPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(interaptPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        botPanelLayout.setVerticalGroup(
            botPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(botPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(botPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(interaptPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(centralPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(botPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(botPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
    }//GEN-LAST:event_closeDialog

    private void incrementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incrementActionPerformed
        nextDNA(1);
    }//GEN-LAST:event_incrementActionPerformed

    private void decrementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decrementActionPerformed
        nextDNA(-1);
    }//GEN-LAST:event_decrementActionPerformed

    private void jampBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jampBackActionPerformed
        final var pc = history.pop();
		nextDNA(pc - object.getDna().getPC());
		history.pop(); //Удаляем этот переход
		updateHeaderPanel(); //И перерисовываем панели
		centralPanel.repaint();
    }//GEN-LAST:event_jampBackActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        assert (isNeedHello = false) == false : "Специально скрываем эту плашку, когда происходит отладка. В выпуске она появится потому что там асертов нет!";
		
		final var ss = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		ss.height *= 0.9;
		ss.width *= 0.9;
		setSize(ss);
		
		if(isNeedHello){
			String message = Configurations.getHProperty(CellEditor.class, "helloText");
			Object[] params = {message};
			JOptionPane.showConfirmDialog(this, params, "BioLife", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
			isNeedHello = false;
		}
    }//GEN-LAST:event_formWindowOpened

    private void defActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defActionPerformed
       set(POPUP_STATUS.UNDEF);
    }//GEN-LAST:event_defActionPerformed

    private void incActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incActionPerformed
        set(POPUP_STATUS.INC);
    }//GEN-LAST:event_incActionPerformed

    private void decActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decActionPerformed
        set(POPUP_STATUS.DEC);
    }//GEN-LAST:event_decActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        set(POPUP_STATUS.REM);
    }//GEN-LAST:event_removeActionPerformed

    private void removeGenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGenActionPerformed
        set(POPUP_STATUS.REM_GEN);
    }//GEN-LAST:event_removeGenActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        set(POPUP_STATUS.ADD);
    }//GEN-LAST:event_addActionPerformed

    private void copyGenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyGenActionPerformed
        set(POPUP_STATUS.COPY);
    }//GEN-LAST:event_copyGenActionPerformed
	/**Поворачивает ДНК на выбранное число
	 * @param val на сколько повернуть ДНК
	 */
	private void nextDNA(int val){
		final var dna = object.getDna();
		history.push(dna.getPC());
		dna.next(val);
		updateHeaderPanel();
		centralPanel.repaint();
		set(POPUP_STATUS.UNDEF);
	}
	/**@param s текущий выбранный инструмент*/
	private void set(POPUP_STATUS s){
		pstatus = s;
		switch (s) {
			case UNDEF -> this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			case INC -> this.setCursor(loadCursor("add"));
			case DEC -> this.setCursor(loadCursor("sub"));
			case REM,REM_GEN -> this.setCursor(loadCursor("kill"));
			case ADD,COPY -> this.setCursor(loadCursor("clipboardCopy"));
			default -> throw new AssertionError();
		}
	}
	/**Название картинки из ресурсов игры, которая будет загружена как курсор*/
	private Cursor loadCursor(String name){
		var name_const = MessageFormat.format("resources/{0}.png", name);
		var constResource = Configurations.class.getClassLoader().getResource(name_const);
		if(constResource == null) {
			System.err.println("Не смогли загрузить фотографию " + name_const);
			return new Cursor(Cursor.HAND_CURSOR);
		}
		//Координаты сдвига. Сдвигаем на половину, так получается мышка по центру рисунка
        final var p11 = new java.awt.Point(7, 7); 
		return java.awt.Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(constResource).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH), p11, "cursor."+name); 
	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PClabel;
    private javax.swing.JPanel PCpanel;
    private javax.swing.JMenuItem add;
    private javax.swing.JPanel botPanel;
    private javax.swing.JPanel centralPanel;
    private javax.swing.JMenuItem copyGen;
    private javax.swing.JMenuItem dec;
    private javax.swing.JButton decrement;
    private javax.swing.JMenuItem def;
    private javax.swing.JMenuItem inc;
    private javax.swing.JButton increment;
    private javax.swing.JPopupMenu instruments;
    private javax.swing.JPanel interaptPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jampBack;
    private javax.swing.JPanel nextCmdPanel;
    private javax.swing.JMenuItem remove;
    private javax.swing.JMenuItem removeGen;
    private javax.swing.JPanel settingsPanel;
    // End of variables declaration//GEN-END:variables
	private AliveCell object;
	/**История*/
	private RingBuffer<Integer> history = new RingBuffer(100);
	/**Текущий инструмент*/
	private POPUP_STATUS pstatus = POPUP_STATUS.UNDEF;	
	/**Нужна печать сообщение приветствия?*/
	private static boolean isNeedHello = true;

}
