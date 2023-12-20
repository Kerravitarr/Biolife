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
import MapObjects.dna.DNA;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.JButton;

/**
 * Редактор клеток
 * @author Kerravitarr
 */
public class CellEditor extends java.awt.Dialog {

	/** Creates new form CellEditor */
	public CellEditor(AliveCell edit) {
		super(null, true);
		object = edit.clone();
		initComponents();
		setAlwaysOnTop(true);
		centralPanel.add(new PaintJPanel(), java.awt.BorderLayout.CENTER);
		
		setButtonParam(decrement);
		setButtonParam(increment);
		setButtonParam(jampPC);
		
		
		makeSettingsPanel();
		makeInterraptPanel();
		updateHeaderPanel();
	}
	/**Делает кнопочки покрасивее
	 * @param button 
	 */
	private void setButtonParam(JButton button) {
		button.setContentAreaFilled(false);
	}
	/**Создаёт панель настроек*/
	private void makeSettingsPanel() {
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
	}
	/**Создаёт панель со всеми прерываниями*/
	private void makeInterraptPanel(){
		final var dna = object.getDna();
		final var interrupts = dna.interrupts;
		final var objects = CellObject.OBJECT.values;
		final var ints = IntStream.range(0, dna.size).boxed().toArray(Integer[]::new);
		for (int i = 0; i < interrupts.length; i++) {
			final var index = i;
			final var aInt = interrupts[index];
			final var o = objects[index];
			interaptPanel.add(new SettingsSelect<>(CellEditor.class,"interraptPanel."+o.name(),ints,i, aInt, e -> {
				interrupts[index] = e;
				centralPanel.repaint();
			}));
		}
	}
	/**Создаёт заголовок страницы*/
	private void updateHeaderPanel(){
		final var dna = object.getDna();
		PClabel.setText("PC: " + dna.getPC());
	}
	
	private class PaintJPanel extends javax.swing.JPanel{
		/**Цвета прерываний*/
		private final Color[] I_COLOR;
		
		PaintJPanel(){
			I_COLOR = new Color[CellObject.OBJECT.lenght];
			for (int i = 0; i < I_COLOR.length; i++) {
				I_COLOR[i] = Utils.Utils.getHSBColor(((double)i)/I_COLOR.length, 1, 1, 0.5);
			}
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
			final var dna = object.getDna();
			int size;
			for (size = 0; size < dna.size; ) {
				size += dna.get(size).size();
			}
			final var BETWIN_LINE = 4; //Интервал между линиями связывающими прерывания и гены
			final var rD = min / 80d; //"ширина" спирали ДНК
			final var r = min/2  - BETWIN_LINE * CellObject.OBJECT.lenght - rD*2; //Радиус ДНК
			printInterrapts(g,size,cx,cy, r,rD,h,BETWIN_LINE);
			printDNA(g, cx, cy, size, dna, r, rD);
			{//Рисуем толстую стрелочку от ПК
				final var os = g.getStroke();
				final var dashed = new java.awt.BasicStroke(3);
				g.setStroke(dashed);
				g.setColor(Color.BLACK);
				final var my = cy - r - 3 * rD;
				g.draw(new Line2D.Double(cx, 0, cx, my));
				g.draw(new Line2D.Double(cx, my, cx + my / 2, my / 2));
				g.draw(new Line2D.Double(cx, my, cx - my / 2, my / 2));
				g.setStroke(os);
			}
			{
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
						g.setStroke(new java.awt.BasicStroke(3));
					}
					g.setColor(Utils.Utils.getHSBColor(((double)i)/size, 1, 1, 0.5));
					
					
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
							bezie(g, new Point2D.Double(sx,sy),center,new Point2D.Double(ex,ey));
						}
					}
					if(index == 0){
						g.setStroke(os);
					}
					i += cmd.size();
				}
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
			}
		}
		/** Рисует спираль ДНК
		 * @param g холст, на котором рисуем
		 * @param cx координаты центра
		 * @param cy координаты центра
		 * @param size количество нуклиотидов в ДНК
		 * @param dna сама ДНК
		 * @param r кадиус ДНК
		 * @param rD ширина ДНК
		 */
		private void printDNA(Graphics2D g, final int cx, final int cy,  int size, final DNA dna,double r, final double rD) {
			//Рисуем ДНК
			final var dr = (Math.PI) / size; //Какой угол относится к одной команде ДНК
			for (var i = 0; i < dna.size; ) {
				final var a = i * 2 * dr - Math.PI / 2;
				var af = a - dr;
				final var index = dna.getIndex(i);
				final var cmd = dna.get(i++);
				
				//Рисуем начало команды
				{
				g.setColor(Color.BLUE);
				final var cos = Math.cos(a);
				final var sin = Math.sin(a);
				var tx = cx + (r - 2*rD) * cos;
				var ty = cy + (r - 2*rD) * sin;
				print(g,tx,ty,af,String.valueOf(index));
				
				drawStartEnd(g,cx,cy,r,rD,af, true,dr);
				tx = cx + (r + 2*rD) * cos;
				ty = cy + (r + 2*rD) * sin;
				print(g,tx,ty,a,cmd.getShotName());
			}
				//Её параметры
				{
				g.setColor(new Color(255, 70, 70, 150));
				for (int j = 0; j < cmd.getCountParams(); j++, i++) {
					final var ap = i * 2 * dr - Math.PI / 2;
					af = ap - dr * 2;
					drawCentral(g,cx,cy,r,rD, af, dr);
					final var tx = cx + (r + 2*rD) * Math.cos(ap);
					final var ty = cy + (r + 2*rD) * Math.sin(ap);
					print(g,tx,ty,ap,cmd.getParam(object, j, dna));
				}
			}
				{//Её ветви
					g.setColor(new Color(100, 100, 100, 150));
					for (int j = 0; j < cmd.getCountBranch(); j++, i++) {
						final var ap = i * 2 * dr - Math.PI / 2;
						af = ap - dr * 2;
						drawCentral(g,cx,cy,r,rD, af, dr);
						final var tx = cx + (r + 2*rD) * Math.cos(ap);
						final var ty = cy + (r + 2*rD) * Math.sin(ap);
						print(g,tx,ty,ap,cmd.getBranch(object, j, dna));
					}
				}
				//А теперь рисуем завершение ветви
				g.setColor(Color.BLUE);
				af = (i * 2 * dr - Math.PI / 2) - dr*2;
				drawStartEnd(g,cx,cy,r,rD,af, false,dr);
			}
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
		 */
		private void drawStartEnd(Graphics2D g, int cx, int cy, double r,double width, double angle, boolean isStart,double dr){	
			final var dna = object.getDna();
			
			double fx1, fx2, fy1, fy2;
			fx1 = fx2 = cx + r * Math.cos(angle);
			fy1 = fy2 = cy + r * Math.sin(angle);
			for(var a = 0d ; a < dr + Math.PI/360; a += Math.PI/180){
				final var rx = isStart ? width * Math.sin(a*dna.size / 2) : width * Math.cos(a*dna.size / 2);

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
			for(var i = 0.0 ; i < lenght; i++){
				final var t = i/lenght;
				final var B0 = (F2 / (F0 * F2)) * Math.pow(t, 0) * Math.pow(1 - t, 2);
				final var B1 = (F2 / (F1 * F1)) * Math.pow(t, 1) * Math.pow(1 - t, 1);
				final var B2 = (F2 / (F2 * F0)) * Math.pow(t, 2) * Math.pow(1 - t, 0);
				final var x = B0 * from.x + B1 * intermediate.x + B2 * to.x;
				final var y = B0 * from.y + B1 * intermediate.y + B2 * to.y;
				final var point = new java.awt.Point.Double(x,y);
				g.draw(new Line2D.Double(pref, point));
				pref = point;
			}
		}
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        settingsPanel = new javax.swing.JPanel();
        botPanel = new javax.swing.JPanel();
        interaptPanel = new javax.swing.JPanel();
        centralPanel = new javax.swing.JPanel();
        PCpanel = new javax.swing.JPanel();
        decrement = new javax.swing.JButton();
        PClabel = new javax.swing.JLabel();
        increment = new javax.swing.JButton();
        jampPC = new javax.swing.JButton();

        setTitle(Configurations.getProperty(CellEditor.class,"title"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
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

        add(jPanel1, java.awt.BorderLayout.NORTH);

        settingsPanel.setLayout(new javax.swing.BoxLayout(settingsPanel, javax.swing.BoxLayout.Y_AXIS));

        interaptPanel.setLayout(new javax.swing.BoxLayout(interaptPanel, javax.swing.BoxLayout.Y_AXIS));

        centralPanel.setLayout(new java.awt.BorderLayout());

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

        jampPC.setText("++");
        jampPC.setToolTipText(Configurations.getProperty(CellEditor.class,"jamp"));
        jampPC.setAlignmentX(0.5F);
        jampPC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jampPCActionPerformed(evt);
            }
        });
        PCpanel.add(jampPC);

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

        add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
    }//GEN-LAST:event_closeDialog

    private void incrementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incrementActionPerformed
        nxtDNA(1);
    }//GEN-LAST:event_incrementActionPerformed

    private void decrementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decrementActionPerformed
        nxtDNA(-1);
    }//GEN-LAST:event_decrementActionPerformed

    private void jampPCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jampPCActionPerformed
        final var dna = object.getDna();
		final var cmd = dna.get();
		nxtDNA(1 + cmd.getCountBranch() + cmd.getCountParams());
    }//GEN-LAST:event_jampPCActionPerformed
	private void nxtDNA(int val){
		object.getDna().next(val);
		updateHeaderPanel();
		centralPanel.repaint();
	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PClabel;
    private javax.swing.JPanel PCpanel;
    private javax.swing.JPanel botPanel;
    private javax.swing.JPanel centralPanel;
    private javax.swing.JButton decrement;
    private javax.swing.JButton increment;
    private javax.swing.JPanel interaptPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jampPC;
    private javax.swing.JPanel settingsPanel;
    // End of variables declaration//GEN-END:variables
	private AliveCell object;


}
