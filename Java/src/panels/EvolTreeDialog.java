/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package panels;

import Utils.MyMessageFormat;
import Utils.SameStepCounter;
import Utils.Utils;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import main.Configurations;
import main.EvolutionTree;

/**
 *
 * @author rjhjk
 */
public class EvolTreeDialog extends javax.swing.JDialog {	
	/**Задача по обновлению экрана*/
	private class UpdateScrinTask implements Runnable {
		/**Пара чисел, для вычисления количества детей и узлов*/
		private class Pair{	private int countAllChild,countChildCell; Pair(int cac, int ccc){countAllChild = cac; countChildCell = ccc;}}
		private Pair rootPair = new Pair(0,0);
		//Специальный счётчик, который нужен для обновления инфы по клетке
		private SameStepCounter counter = new SameStepCounter(2);
		@Override
		public void run() {try{runE();}catch(java.lang.NullPointerException e){}catch(Exception ex){System.err.println(ex);ex.printStackTrace(System.err);}}
		
		public void runE() {
			if (Legend.Graph.getMode() == Legend.Graph.MODE.EVO_TREE){
				updateColor();
			}
			if(EvolTreeDialog.this.isVisible()){
				rootPair = countPair(rootNode);
			}
			counter.step(0);
		}
		public void updateColor(){
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

		private Pair countPair(EvolutionTree.Node root) {
			var next = new Pair(root.getChild().size(), root.countAliveCell());
			for(var i : root.getChild()){
				var add = countPair(i);
				next.countAllChild += add.countAllChild;
				next.countChildCell += add.countChildCell;
			}
			return next;
		}
	}
	/**Одоин отображающийся узел*/
	private class NodeJpanel extends JPanel{
		private final EvolutionTree.Node node;
		private static final Color bColor = new Color(0,0,0,0);
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
			});
			setBackground(bColor);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(node.getColor());
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
			relaintFun.counter.step(1);
			int xStart = 0;
			int xEnd = getWidth();
			var yStart = getHeight() - DEF_DEL_Y * 3;
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
			try{
				printText(g);
			}catch(java.lang.NullPointerException e){
				restart();
			}
		}

		/**
		 * Пишет информацию о выбранной клетке на панели
		 * @param g - панель, куда пишем
		 * @throws java.lang.NullPointerException - может выбрасывать исключение, когда клетка удаляется 
		 */
		private void printText(Graphics g) throws java.lang.NullPointerException{
			var yStart = getHeight() - DEF_DEL_Y;
			g.setColor(Color.BLACK);
			String text = switch ((relaintFun.counter.get() / 2) % 7) {
				default -> String.format("Полное название узла: %s", rootNode.getBranch());
				case 1 -> String.format("Дочерних узлов: %d",rootNode.getChild().size());
				case 2 -> String.format("Всего узлов в ветви: %d, живых клеток в ветви: %d",relaintFun.rootPair.countAllChild, relaintFun.rootPair.countChildCell);
				case 3 -> dateBirth.format(rootNode.getTimeFounder());
				case 4 -> founderYear.format(rootNode.getFounder().getAge());
				case 5 -> String.format("Устойчивость к яду: %s",rootNode.getFounder().getPosionType().toString());
				case 6 -> String.format("Длина ДНК: %d",rootNode.getFounder().getDna().size);
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
			g.drawString(text, 0, yStart);
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
			rootJ.setBounds(centerX - DEF_DEL_Y / 2, yPos + DEF_DEL_Y / 2, DEF_DEL_Y, DEF_DEL_Y);
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
				var center = (xStart + step * i) + ((xStart + step * (i + 1)) - (xStart + step * i)) / 2;
				if(delColor > 0.5)
					g.setColor(Color.WHITE);
				else
					g.setColor(Utils.getHSBColor(colorStart + delColor / 2, 1.0, 1.0, 1.0));
				g.drawLine(centerX, yPos, (int) center, yPos - DEF_DEL_Y);
				paint(g,childs.get(i), 
						(int) Math.round(xStart + step * i), 
						(int) Math.round(xStart + step * (i + 1)), 
						yPos - DEF_DEL_Y, 
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

	
	/**Сколько пунктов по оси Y должно пройти*/
	static final int DEF_DEL_Y = 25;
	/**Ключевой узел, от которого рисуем*/
	private EvolutionTree.Node rootNode = EvolutionTree.root;
	/**Закрашивалка узлов*/
	private final UpdateScrinTask relaintFun;
	/**Дата рождения*/
	private static final MyMessageFormat dateBirth = new MyMessageFormat(Configurations.getProperty(EvolTreeDialog.class,"dateBirth"));
	/**Возраст основателя*/
	private static final MyMessageFormat founderYear = new MyMessageFormat(Configurations.getProperty(EvolTreeDialog.class,"founderYear"));
	
	/** Creates new form E */
	public EvolTreeDialog() {
		super((Frame) null, false);
		initComponents();
		
		Configurations.evolTreeDialog = this;
		
		Configurations.TIME_OUT_POOL.scheduleWithFixedDelay(relaintFun = new UpdateScrinTask(), 1, 1, TimeUnit.SECONDS);
	}
	
	@Override
    public void repaint() {
    	super.repaint();
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
		relaintFun.updateColor();
		DrawPanelEvoTree.isNeedUpdate = true;
		repaint();
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

        jPanelTree.setBackground(new java.awt.Color(204, 204, 204));
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
