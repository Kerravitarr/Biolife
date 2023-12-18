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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
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
			interaptPanel.add(new SettingsSelect<>(CellEditor.class,"interraptPanel."+o.name(),ints,i, aInt, e -> interrupts[index] = e));
		}
	}
	/**Создаёт заголовок страницы*/
	private void updateHeaderPanel(){
		final var dna = object.getDna();
		PClabel.setText("PC: " + dna.getPC());
	}
	
	private class PaintJPanel extends javax.swing.JPanel{
		private enum TYPE{CMD, PARAM, BRANCH};	
		
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
			
			var r = min / 2.2; //Радиус ДНК
			{
				final var dr = (Math.PI) / dna.size; //Какой угол относится к одной команде ДНК
				final var rD = min / 80; //"ширина" спирали ДНК
				for (var i = 0; i < dna.size; ) {
					final var a = i * Math.PI * 2 / dna.size - Math.PI / 2;
					var af = a - dr;
					final var index = dna.getIndex(i);
					final var cmd = dna.get(i++);
					
					//Рисуем начало команды
					g.setColor(Color.BLUE);
					var tx = cx + (r + rD) * Math.cos(af);
					var ty = cy + (r + rD) * Math.sin(af);
					print(g,tx,ty,af,String.valueOf(index));
					
					drawStartEnd(g,cx,cy,r,rD,af, true);
					tx = cx + (r + 2*rD) * Math.cos(a);
					ty = cy + (r + 2*rD) * Math.sin(a);
					print(g,tx,ty,a,cmd.getShotName());
					//Её параметры
					{
						g.setColor(new Color(255, 70, 70, 150));
						for (int j = 0; j < cmd.getCountParams(); j++, i++) {
							final var ap = i * Math.PI * 2 / dna.size - Math.PI / 2;
							af = ap - dr * 2;
							r = drawCentral(g,cx,cy,r,rD, af,i == dna.size);
							if(i < dna.size){
								tx = cx + (r + 2*rD) * Math.cos(ap);
								ty = cy + (r + 2*rD) * Math.sin(ap);
							} else {
								tx = cx + (r - 2*rD) * Math.cos(ap);
								ty = cy + (r - 2*rD) * Math.sin(ap);
							}
							print(g,tx,ty,ap,cmd.getParam(object, j, dna));
						}
					}
					{//Её ветви
						g.setColor(new Color(100, 100, 100, 150));
						for (int j = 0; j < cmd.getCountBranch(); j++, i++) {
							final var ap = i * Math.PI * 2 / dna.size - Math.PI / 2;
							af = ap - dr * 2;
							r = drawCentral(g,cx,cy,r,rD, af,i == dna.size);
							if(i < dna.size){
								tx = cx + (r + 2*rD) * Math.cos(ap);
								ty = cy + (r + 2*rD) * Math.sin(ap);
							} else {
								tx = cx + (r - 2*rD) * Math.cos(ap);
								ty = cy + (r - 2*rD) * Math.sin(ap);
							}
							print(g,tx,ty,ap,cmd.getBranch(object, j, dna));
						}
					}
					//А теперь рисуем завершение ветви
					g.setColor(Color.BLUE);
					af = (i * Math.PI * 2 / dna.size - Math.PI / 2) - dr*2;
					drawStartEnd(g,cx,cy,r,rD,af, false);					
				}
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
		 */
		private void drawStartEnd(Graphics2D g, int cx, int cy, double r,double width, double angle, boolean isStart){	
			final var dna = object.getDna();
			final var dr = (Math.PI) / dna.size; //Какой угол относится к одной команде ДНК
			
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
		 * @param isOffset эта спираль переходная между верхним и нижним диаметром?
		 * @return новый радиус, если спираль переходная. Иначе - старый радиус
		 */
		private double drawCentral(Graphics2D g, int cx, int cy, double r,double width, double angle, boolean isOffset){
			final var dna = object.getDna();
			final var dr = (Math.PI) / dna.size; //Какой угол относится к одной команде ДНК
			final var step = Math.PI/180; //Как часто вырисовывать ДНК
			final var stepDR =  width / (dr / ( 2 * step)); //Это мы уменьшаем радиус хвоста, если он залазит на следующий круг
			
			var fx1 = cx + (r+width) * Math.cos(angle);
			var fy1 = cy + (r+width) * Math.sin(angle);
			var fx2 = cx + (r-width) * Math.cos(angle);
			var fy2 = cy + (r-width) * Math.sin(angle);
			for(var a = 0d ; a < dr * 2 + step / 2; a += step){
				if(isOffset) r -= stepDR;
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
			return r;
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
