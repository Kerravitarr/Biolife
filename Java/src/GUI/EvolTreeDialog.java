/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package GUI;

import Utils.MyMessageFormat;
import Utils.SameStepCounter;
import Utils.Utils;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import Calculations.Configurations;
import Calculations.EvolutionTree;

/**
 *
 * @author rjhjk
 */
public class EvolTreeDialog extends javax.swing.JDialog implements Configurations.EvrySecondTask{	
	/**Сколько пунктов по оси Y должно пройти*/
	static final int DEF_DEL_Y = 25;
	/**Ключевой узел, от которого рисуем*/
	private EvolutionTree.Node rootNode = EvolutionTree.root;
	/**Пара чисел, для вычисления количества детей и узлов*/
	private class Pair{	private int countAllChild,countChildCell; Pair(int cac, int ccc){countAllChild = cac; countChildCell = ccc;}}
	/**Пара значений - количество живых потомков и количество ветвей после узла*/
	private Pair rootPair = new Pair(0,0);
	/**Должны ли узлы отображаться по оси Y в зависимости от даты происхождения. Если тут 0 - нет. Если тут не 0, то это масштаб*/
	private double timeline = 0;
	/**Дата рождения*/
	private static final MyMessageFormat dateBirth = new MyMessageFormat(Configurations.getProperty(EvolTreeDialog.class,"dateBirth"));
	/**Возраст основателя*/
	private static final MyMessageFormat founderYear = new MyMessageFormat(Configurations.getProperty(EvolTreeDialog.class,"founderYear"));
	
	/**Одоин отображающийся узел*/
	private class NodeJpanel extends JPanel{
		/**Реальный узел, который мы изображаем*/
		private final EvolutionTree.Node node;
		/**Прозрачный цвет*/
		private static final Color bColor = new Color(0,0,0,0);
		/**Фабрика создания всплывающей подсказки*/
		private PopupFactory popupFactory;
		/**Окно подсказки*/
		private static Popup popup;
		/**Сама подсказка*/
		private JToolTip t;
		
		NodeJpanel(EvolutionTree.Node node){
			this.node = node;
			init();
		}
		
		private void init(){
			addMouseListener(new MouseAdapter() {
				@Override
			    public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						setRootNode(node);
					}
			    }
				@Override
			    public void mouseEntered(MouseEvent e) {
					if(t == null){
						t = jPanelTree.createToolTip();
						StringBuilder sb = new StringBuilder();
						sb.append("<html>");
						for (int i = 0; i < 7; i++) {
							if(i > 0) sb.append("<br>");
							sb.append(formatNode(node,i));
						}
						t.setTipText(sb.toString());
						popupFactory = PopupFactory.getSharedInstance();
					}
					if(popup != null)
						popup.hide();
					popup = popupFactory.getPopup(jPanelTree, t, e.getXOnScreen(), e.getYOnScreen());
					popup.show();
			    }
				@Override
				public void mouseExited(MouseEvent e) {if(popup != null) popup.hide();}
			});
			setBackground(bColor);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(node.getFounder().phenotype);
			Utils.centeredText(g, getWidth()/2, getHeight()/2, DEF_DEL_Y / 2, String.valueOf(NodeJpanel.this.node.getGeneration()));
		}
		@Override
		public String toString(){
			return node.getBranch();
		}
	}
	/**Панель, на которой  рисуется дерево эволюции*/
	public class DrawPanelEvoTree extends JPanel {
		/**Нужно пересчитать дерево*/
		static boolean isNeedUpdate = false;
		public DrawPanelEvoTree() {
			GroupLayout groupLayout = new GroupLayout(this);
			groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGap(0, 450, Short.MAX_VALUE)
			);
			groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGap(0, 300, Short.MAX_VALUE)
			);
			setLayout(groupLayout);
		}
		@Override
		public void paintComponent(Graphics g) {
			//Традиционное рисование - в виде дерева на всю высоту экрана
			//Круглое рисование - когда Адам в центре, а все остальные идут по кругу
			//Режим хронологии, для расположения узлов в зависимости от старости
			int xStart = 0;
			int xEnd = getWidth();
			var yStart = getHeight() - DEF_DEL_Y * 3;
			if(timeline != 0)
				timeline = ((double) getHeight()) / Configurations.world.step;
			if(isNeedUpdate || Configurations.world.isActiv()) {
				removeAll();
				try{
					if(rootNode.getPerrent() != null)
						addRNode(rootNode.getPerrent(),xStart,xEnd, yStart);
					addNode(rootNode,xStart,xEnd, yStart - DEF_DEL_Y);	
					isNeedUpdate = false;
				}catch(Exception e){} //Нормально всё, асинхронность выполнения и всё прочее
				repaint();
			}
			super.paintComponent(g);
			
			try{
				if(rootNode.getPerrent() != null)
					paint(g,xStart,xEnd, yStart + DEF_DEL_Y);
				paint(g,rootNode,xStart,xEnd, yStart,0,0.8);
			}catch(Exception e){} //Нормально всё, асинхронность выполнения и всё прочее
		}
		
		/**
		 * Собирает дерево из узлов
		 * @param root сам изображаемый узел
		 * @param xStart начало отрезка узла
		 * @param xEnd конец отрезка узла
		 * @param yPos позиция по оси y
		 */
	    private void addNode(EvolutionTree.Node root, int xStart, int xEnd, int yPos) {
			var delX = (xEnd - xStart);
			NodeJpanel rootJ = new NodeJpanel(root);
			var centerX = xStart + delX / 2;
			//Сохраняем положение
			if(timeline == 0)
				rootJ.setBounds(centerX - DEF_DEL_Y / 2, yPos + DEF_DEL_Y / 2, DEF_DEL_Y, DEF_DEL_Y);
			else
				rootJ.setBounds(centerX - DEF_DEL_Y / 2,(int) ( getHeight() - root.getTimeFounder() * timeline), DEF_DEL_Y, DEF_DEL_Y);
			add(rootJ);

			List<EvolutionTree.Node> childs = root.getChild();
			if(childs.isEmpty() || xEnd - xStart < 10) return;
			
			var step = ((double) delX) / childs.size();
			
			for(int i = 0 ; i < childs.size() ; i++) {
				addNode(childs.get(i), 
						(int) Math.round(xStart + step * i), 
						(int) Math.round(xStart + step * (i + 1)), 
						yPos - DEF_DEL_Y);
			}
		}
		/**
		 * Собирает дерево родителя из узлов
		 * @param root сам изображаемый узел
		 * @param xStart начало отрезка узла
		 * @param xEnd конец отрезка узла
		 * @param yPos позиция по оси y
		 */
	    private void addRNode(EvolutionTree.Node root, int xStart, int xEnd, int yPos) {
			var delX = (xEnd - xStart);
			NodeJpanel rootJ = new NodeJpanel(root);
			var centerX = xStart + delX / 2;
			//Сохраняем положение
			rootJ.setBounds(centerX - DEF_DEL_Y / 2, yPos + DEF_DEL_Y / 2, DEF_DEL_Y, DEF_DEL_Y);

			add(rootJ);
		}

		/**
		 * Рисует линии между узлами
		 * @param g
		 * @param root сам изображаемый узел
		 * @param xStart начало отрезка узла
		 * @param xEnd конец отрезка узла
		 * @param yPos позиция по оси y
		 */
		private void paint(Graphics g, EvolutionTree.Node root, int xStart, int xEnd, int yPos, double colorStart, double colorEnd) {
			List<EvolutionTree.Node> childs = root.getChild();
			if(childs.isEmpty()) return;
			var delX = (xEnd - xStart);
			var delColor = (colorEnd - colorStart);
			var step = ((double) delX) / childs.size();
			var stepColor = delColor / childs.size();
			var centerX = xStart + delX / 2;
			
			for(int i = 0 ; i < childs.size() ; i++) {
				final var child = childs.get(i);
				var center = (xStart + step * i) + ((xStart + step * (i + 1)) - (xStart + step * i)) / 2;
				if(delColor > 0.5)
					g.setColor(Color.WHITE);
				else
					g.setColor(Utils.getHSBColor(colorStart + delColor / 2, 1.0, 1.0, 1.0));
				final var toY = (int) (timeline == 0 ? yPos - DEF_DEL_Y : getHeight() - timeline * child.getTimeFounder());
				g.drawLine(centerX, yPos, (int) center, toY);
				paint(g,child, 
						(int) Math.round(xStart + step * i), 
						(int) Math.round(xStart + step * (i + 1)), 
						toY, 
						colorStart + stepColor * i, 
						colorStart + stepColor * (i + 1));
			}
		}
		
		private void paint(Graphics g, int xStart, int xEnd, int yPos) {
			var delX = (xEnd - xStart);
			var centerX = xStart + delX / 2;
			g.setColor(Color.WHITE);
			g.drawLine(centerX, yPos, centerX, yPos - DEF_DEL_Y);
		}
		
	}
		
	/** Creates new form E */
	public EvolTreeDialog() {
		super((Frame) null, false);
		initComponents();
		Configurations.setIcon(resetButton,"reset");
		resetButton.addActionListener( e-> restart());
		resetButton.setToolTipText(Configurations.getHProperty(EvolTreeDialog.class,"reset"));
		Configurations.setIcon(timelineB,"reset");
		timelineB.setToolTipText(Configurations.getHProperty(EvolTreeDialog.class,"timelineB"));
		
		Configurations.addTask(this);
		restart();
	}
	
	@Override
    public void taskStep() {
		final var v = Configurations.getViewer();
		if (v != null && v.get(Legend.class).getMode() == Legend.MODE.EVO_TREE){
			updateColor();
		}
		if(EvolTreeDialog.this.isVisible()){
			try{
				rootPair = countPair(rootNode);
			} catch(java.lang.NullPointerException e){} //Всё нормально, у нас прямо во время перерисовывания изменилось дерево. Такое бывает частенько. Асинхронность
		}
    }
	/**Обновляет цвета узлов*/
	private void updateColor(){
		EvolutionTree.root.resetColor();
		if(rootNode.getPerrent() != null)
			colorNode(rootNode.getPerrent());
		colorNode(rootNode,0.0,0.8);
	}
	/**
	 * Раскрашивает дерево потомков
	 * @param root сам изображаемый узел
	 */
	private void colorNode(EvolutionTree.Node root, double colorStart, double colorEnd) {
		var delColor = (colorEnd - colorStart);
		if(delColor > 0.5)
			root.setColor(Utils.getHSBColor(1.0, 0.0, 1.0, 1.0));
		else
			root.setColor(Utils.getHSBColor(colorStart + delColor / 2, 1.0, 1.0, 1.0));

		List<EvolutionTree.Node> childs = root.getChild();		
		var stepColor = delColor / childs.size();

		for(int i = 0 ; i < childs.size() ; i++) {
			colorNode(childs.get(i),colorStart + stepColor * i,colorStart + stepColor * (i + 1));
		}
	}
	/**
	 * Раскрашивает дерево потомков
	 * @param root сам изображаемый узел
	 */
	private void colorNode(EvolutionTree.Node root) {
		root.setColor(Utils.getHSBColor(1.0, 0.0, 1.0, 1.0));
		if(root.getPerrent() != null)
			colorNode(root.getPerrent());
	}
	/**Возвращает число узлов наследования и число живых клеток*/
	private Pair countPair(EvolutionTree.Node root) {
		var next = new Pair(root.getChild().size(), root.countAliveCell());
		for(var i : root.getChild()){
			var add = countPair(i);
			next.countAllChild += add.countAllChild;
			next.countChildCell += add.countChildCell;
		}
		return next;
	}
	
	/**
	 * Сбрасывает корневой узел дерева
	 */
	public void restart(){
		setRootNode(EvolutionTree.root);
	}
	
	private void setRootNode(EvolutionTree.Node newNode){
		EvolutionTree.root.resetColor();
		rootNode = newNode;
		updateColor();
		DrawPanelEvoTree.isNeedUpdate = true;
		resetButton.setVisible(newNode != EvolutionTree.root);
		repaint();
	}
	
	/**Возвращает одну из строк текстового описания узла*/
	private String formatNode(EvolutionTree.Node node, int row){
		var index = row % 7;
		try{
			Pair p = index == 2 ? countPair(node) : null;
			return switch (index) {
				default -> MessageFormat.format(Configurations.getProperty(EvolTreeDialog.class,"nodeDescriptionNode"), node.getBranch());
				case 1 -> MessageFormat.format(Configurations.getProperty(EvolTreeDialog.class,"nodeDaughter"),node.getChild().size());
				case 2 -> MessageFormat.format(Configurations.getProperty(EvolTreeDialog.class,"nodeStatistic"),p.countAllChild, p.countChildCell);
				case 3 -> dateBirth.format(node.getTimeFounder());
				case 4 -> founderYear.format(node.getFounder().getAge());
				case 5 -> MessageFormat.format(Configurations.getProperty(EvolTreeDialog.class,"nodePoison"),node.getFounder().getPosionType().toString());
				case 6 -> MessageFormat.format(Configurations.getProperty(EvolTreeDialog.class,"nodeDna"),node.getFounder().getDna().size);
				/*case 7 -> {
					StringBuilder sb = new StringBuilder();
					for(var i : Utils.sortByValue(rootNode.getFounder().getSpecialization())) {
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

					sb.toString();

				} */
			};
		} catch (Exception e){ return index + ")";} //Если узлы обновляются, то всегда есть шанс нарваться на асинхронность и вылететь нафиг
	}


	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        mainP = new javax.swing.JPanel();
        jPanelTree = new DrawPanelEvoTree();
        jPanel1 = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();
        timelineB = new javax.swing.JButton();

        jScrollPane1.setViewportView(jEditorPane1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        mainP.setLayout(new java.awt.BorderLayout());

        jPanelTree.setBackground(new java.awt.Color(204, 204, 204));
        jPanelTree.setToolTipText(Configurations.getHProperty(EvolTreeDialog.class,"toolTipText"));
        jPanelTree.setAlignmentX(0.0F);
        jPanelTree.setAlignmentY(0.0F);
        jPanelTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanelTreeMouseEntered(evt);
            }
        });
        jPanelTree.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanelTreeComponentResized(evt);
            }
        });

        javax.swing.GroupLayout jPanelTreeLayout = new javax.swing.GroupLayout(jPanelTree);
        jPanelTree.setLayout(jPanelTreeLayout);
        jPanelTreeLayout.setHorizontalGroup(
            jPanelTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 689, Short.MAX_VALUE)
        );
        jPanelTreeLayout.setVerticalGroup(
            jPanelTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 401, Short.MAX_VALUE)
        );

        mainP.add(jPanelTree, java.awt.BorderLayout.CENTER);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setAlignmentX(0.0F);
        jPanel1.setAlignmentY(0.0F);

        resetButton.setAlignmentY(0.0F);
        resetButton.setBorderPainted(false);
        resetButton.setContentAreaFilled(false);
        resetButton.setFocusPainted(false);
        resetButton.setFocusable(false);
        resetButton.setInheritsPopupMenu(true);
        resetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        resetButton.setPreferredSize(new java.awt.Dimension(40, 20));

        timelineB.setBorderPainted(false);
        timelineB.setContentAreaFilled(false);
        timelineB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timelineBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(565, Short.MAX_VALUE)
                .addComponent(timelineB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(resetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(timelineB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        mainP.add(jPanel1, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jPanelTreeComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelTreeComponentResized
        ((DrawPanelEvoTree)jPanelTree).isNeedUpdate = true;
    }//GEN-LAST:event_jPanelTreeComponentResized

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
         switch (evt.getKeyCode()) {
			case KeyEvent.VK_SPACE -> {
				if (Configurations.world.isActiv())
					Configurations.world.stop();
				else
					Configurations.world.start();
			}
		}
    }//GEN-LAST:event_formKeyReleased

    private void jPanelTreeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanelTreeMouseEntered
		if(NodeJpanel.popup != null)
			NodeJpanel.popup.hide();
    }//GEN-LAST:event_jPanelTreeMouseEntered

    private void timelineBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timelineBActionPerformed
        timeline = timeline == 0 ? 1d : 0d;
    }//GEN-LAST:event_timelineBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelTree;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainP;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton timelineB;
    // End of variables declaration//GEN-END:variables
}
