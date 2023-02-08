/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package panels;

import MapObjects.CellObject;
import Utils.SeveralClicksMouseAdapter;
import Utils.Utils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import main.Configurations;
import main.EvolutionTree;
import main.Point;
import main.World;

/**
 *
 * @author rjhjk
 */
public class EvolTreeDialog extends javax.swing.JDialog {	
	private class NodeJpanel extends JPanel{
		private final EvolutionTree.Node node;
		private final Color modeColor;
		private static final Color bColor = new Color(0,0,0,0);
		NodeJpanel(EvolutionTree.Node node, double color, int realWidth){
			this.node = node;
			modeColor = Utils.getHSBColor(color, 1.0, 1.0, 1.0);
			addMouseListener(new MouseAdapter() {
				@Override
			    public void mouseClicked(MouseEvent e) {
					System.out.println("Кликов: " + e.getClickCount() + ". Событие: " + e.toString());
					if(e.getClickCount() == 2){
						if(node.getPerrent() == null){
							rootNode = node;
						} else {
							rootNode = node.getPerrent();
						}
						DrawPanelEvoTree.isNeedUpdate = true;
						repaint();
					} else {
						EvolutionTree.root.setSelected(true);
						NodeJpanel.this.node.setSelected(false);
						CellUpdate();
						activNode = NodeJpanel.this;
					}
			    }
			});
			setBackground(bColor);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(modeColor);
			Utils.centeredText(g, getWidth()/2, getHeight()/2, delY / 3, NodeJpanel.this.node.getGeneration()+"");
			if(!NodeJpanel.this.node.isSelected())
				Utils.drawCircle(g, getWidth()/2, getHeight()/2, 10);
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
			int xStart = xCenter;
			int xEnd = (int) (getWidth() * scale) + xCenter;
			var root = rootNode.getPerrent() == null ? rootNode : rootNode;
			var yStart = (getHeight() + yCenter - delY * 2) - (rootNode.getPerrent() == null ? 0 :  - delY);
			if(isNeedUpdate || World.isActiv) {
				isNeedUpdate = false;
				removeAll();
				repaint();
				try{
					addNode(root,xStart,xEnd, (int) (yStart - delY),0,0.8);	
				}catch(java.lang.NullPointerException e){} //Нормально всё, асинхронность выполнения и всё прочее
			}
			super.paintComponent(g);
			
			paint(g,root,xStart,xEnd, (int) (yStart),0,0.8);
			try{
				//printText(g);
			}catch(java.lang.NullPointerException e){
				activNode = null;
			}
		}

		/**
		 * Пишет информацию о выбранной клетке на панели
		 * @param g - панель, куда пишем
		 * @throws java.lang.NullPointerException - может выбрасывать исключение, когда клетка удаляется 
		 */
		private void printText(Graphics g) throws java.lang.NullPointerException{
			/*if(activNode != null) {
				if(activNode.node.countAliveCell() > 0){
					g.drawString("Примечательные точки узла("+activNode.node.countAliveCell()+"клеток):", MIN_X, 10);
				}else{
					g.drawString("Примечательные точки узла:", MIN_X, 10);
					if(activNode.node.getChild().isEmpty())
						activNode.node.remove();
				}
				String DNA_s = "";
				for(int i : activNode.node.DNA_mind) {
					if(!DNA_s.isEmpty())
						DNA_s+= ", ";
					DNA_s += i + "";
				}
				g.drawString("ДНК (" + activNode.node.DNA_mind.length + "): " + DNA_s, MIN_X, 25);
				Color oldC = g.getColor();
				g.setColor(activNode.node.phenotype);
				g.drawString("Фенотип: " + "[r="
						+ activNode.node.phenotype.getRed() + ",g="
						+ activNode.node.phenotype.getGreen() + ",b="
						+ activNode.node.phenotype.getBlue() + "]", MIN_X, 40);
				g.setColor(oldC);
				g.drawString("Устойчивость к яду: " + activNode.node.poisonType, MIN_X, 55);
			}*/
		}
		
		/**
		 * Собирает дерево из узлов
		 * @param root сам изображаемый узел
		 * @param xStart начало отрезка узла
		 * @param xEnd конец отрезка узла
		 * @param yPos позиция по оси y
		 */
	    private void addNode(EvolutionTree.Node root, int xStart, int xEnd, int yPos, double colorStart, double colorEnd) {
			var delX = (xEnd - xStart);
			NodeJpanel rootJ = new NodeJpanel(root, colorStart + (colorEnd - colorStart) / 2, delX);
	    	add(rootJ);
			var centerX = xStart + delX / 2;
			//Сохраняем положение
			rootJ.setBounds(centerX - delY / 2, yPos + delY / 2, delY, delY);
			rootJ.addMouseListener(new SeveralClicksMouseAdapter() {
			    public void mouseSeveralClick(MouseEvent e) {
					System.out.println("Кликов: " + e.getClickCount() + ". Событие: " + e.toString());
			    }
			});

			List<EvolutionTree.Node> childs = root.getChild();
			if(childs.isEmpty() || (xEnd - xStart) < 10) return;
			var step = ((double) delX) / childs.size();
			var stepColor = (colorEnd - colorStart) / childs.size();
			
			for(int i = 0 ; i < childs.size() ; i++) {
				addNode(childs.get(i), 
						(int) Math.round(xStart + step * i), 
						(int) Math.round(xStart + step * (i + 1)), 
						yPos - delY, 
						colorStart + stepColor * i, 
						colorStart + stepColor * (i + 1));
			}
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
			var step = ((double) delX) / childs.size();
			var stepColor = (colorEnd - colorStart) / childs.size();
			var centerX = xStart + delX / 2;
			
			for(int i = 0 ; i < childs.size() ; i++) {
				var center = (xStart + step * i) + ((xStart + step * (i + 1)) - (xStart + step * i)) / 2;
				g.drawLine(centerX, yPos, (int) center, yPos - delY);
				paint(g,childs.get(i), 
						(int) Math.round(xStart + step * i), 
						(int) Math.round(xStart + step * (i + 1)), 
						yPos - delY, 
						colorStart + stepColor * i, 
						colorStart + stepColor * (i + 1));
			}
		}
		
	}

	
	/**Сколько пунктов по оси Y должно пройти*/
	static final int DEF_DEL_Y = 40;
	private NodeJpanel activNode = null;
	/**Масштаб дерева*/
	private double scale = 1;
	/**Смещение дерева по оси Х*/
	private int xCenter = 0;
	/**Смещение дерева по оси У*/
	private int yCenter = 0;
	/**С каким шагом дерево поднимается по оси Y*/
	private final int delY = (int) (DEF_DEL_Y * scale);
	/**Ключевой узел, от которого рисуем*/
	private EvolutionTree.Node rootNode = EvolutionTree.root;
	
	/** Creates new form E */
	public EvolTreeDialog() {
		super((Frame) null, false);
		initComponents();
		
		Configurations.evolTreeDialog = this;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
		    	EvolutionTree.root.setSelected(false);
		    	CellUpdate();
	    		activNode = null;
			}
			@Override
		    public void windowActivated(WindowEvent e) {
		    	EvolutionTree.root.setSelected(true);
		    	CellUpdate();
	    		activNode = null;
		    }
		});
		
        //buttonWidth.setText(Configurations.getProperty(EvolTreeDialog.class, "addW"));
       // buttonWidth.setToolTipText("Расширить дерево");
	}
	
	@Override
    public void repaint() {
    	super.repaint();
    }

	private static void CellUpdate() {
		for (int x = 0; x < Configurations.MAP_CELLS.width; x++) {
			for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
				CellObject cell = Configurations.world.get(new Point(x,y));
				if(cell != null)
					cell.repaint();
			}
		}
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
        jPanelTree = new DrawPanelEvoTree();

        jScrollPane1.setViewportView(jEditorPane1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        jPanelTree.setBackground(new java.awt.Color(153, 153, 153));
        jPanelTree.setToolTipText("");
        jPanelTree.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanelTreeComponentResized(evt);
            }
        });

        javax.swing.GroupLayout jPanelTreeLayout = new javax.swing.GroupLayout(jPanelTree);
        jPanelTree.setLayout(jPanelTreeLayout);
        jPanelTreeLayout.setHorizontalGroup(
            jPanelTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 423, Short.MAX_VALUE)
        );
        jPanelTreeLayout.setVerticalGroup(
            jPanelTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 365, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelTree, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelTree, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jPanelTreeComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelTreeComponentResized
        ((DrawPanelEvoTree)jPanelTree).isNeedUpdate = true;
    }//GEN-LAST:event_jPanelTreeComponentResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jPanelTree;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
