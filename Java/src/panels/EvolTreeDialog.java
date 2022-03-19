package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import MapObjects.CellObject;
import Utils.Utils;
import main.Configurations;
import main.EvolutionTree;
import main.EvolutionTree.Node;
import main.Point;

public class EvolTreeDialog extends JDialog {
	static int delX = 23;
	static int minX = delX * 2;
	static int minY = 55;
	
	public class NodeJpanel extends JPanel{
		Node node = null;
		NodeJpanel(Node node){
			this.node = node;
			addMouseListener(new MouseAdapter() {
			    public void mouseClicked(MouseEvent e) {
			    	EvolutionTree.root.setSelected(true);
			    	NodeJpanel.this.node.setSelected(false);
			    	CellUpdate();
		    		activNode = NodeJpanel.this;
			    }
			});
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(getHeight() >= 10) {
				g.setColor(Color.BLACK);
				Utils.centeredText(g, getWidth()/2, getHeight()/2, 10, NodeJpanel.this.node.getGeneration()+"");
				if(!NodeJpanel.this.node.isSelected())
					Utils.drawCircle(g, getWidth()/2, getHeight()/2, 10);
			}
		}
	}
	
	public class DrawPanelEvoTree extends JPanel {
		boolean isNeedUpdate = false;
		
		/**
		 * Create the panel.
		 */
		public DrawPanelEvoTree() {
			GroupLayout groupLayout = new GroupLayout(this);
			groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGap(0, 450, Short.MAX_VALUE)
			);
			groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGap(0, 300, Short.MAX_VALUE)
			);
			setLayout(groupLayout);
			
		}
	
		
		public void paintComponent(Graphics g) {
			if(isNeedUpdate) {
				isNeedUpdate = false;
				int maxY = getHeight() - minY;

				removeAll();
				addNode(EvolutionTree.root,minX,minY,maxY);
			}
			super.paintComponent(g);
			
			int maxY = getHeight() - minY;
			
			paint(g,EvolutionTree.root,minX,minY,maxY);
			
			if(activNode != null) {
				g.drawString("Примечательные точки узла:", minX, 10);
				String DNA_s = "";
				for(int i : activNode.node.DNA) {
					if(!DNA_s.isEmpty())
						DNA_s+= ", ";
					DNA_s += i + "";
				}
				g.drawString("ДНК (" + activNode.node.DNA.length + "): " + DNA_s, minX, 25);
				Color oldC = g.getColor();
				g.setColor(activNode.node.phenotype);
				g.drawString("Фенотип: " + "[r="
						+ activNode.node.phenotype.getRed() + ",g="
						+ activNode.node.phenotype.getGreen() + ",b="
						+ activNode.node.phenotype.getBlue() + "]", minX, 40);
				g.setColor(oldC);
				g.drawString("Устойчивость к яду: " + activNode.node.poisonType, minX, 55);
			}
		}
		
	    public void repaint() {
			super.repaint();
			isNeedUpdate = true;
	    }
	    private void addNode(Node root, int xPos, int minY, int maxY) {
	    	NodeJpanel rootJ = new NodeJpanel(root);
	    	add(rootJ);
	    	rootJ.setBounds(xPos - delX / 2, minY + (maxY-minY)/2 - delX / 2, Math.min((maxY-minY)/2,delX), Math.min((maxY-minY)/2,delX));
	    	
			Vector<Node> childs = root.getChild();
			if(childs.size() == 0 || maxY-minY < 10) return;
			double step = (maxY-minY) / childs.size();
			
			for(int i = 0 ; i < childs.size() ; i++) {
				addNode(childs.get(i),xPos + delX,(int)Math.round(minY + step * i),(int)Math.round(minY + step * (i + 1)));
			}
		}
	
		private void paint(Graphics g, Node root, int xPos, int minY, int maxY) {
			Vector<Node> childs = root.getChild();
			if(childs.size() == 0) return;
			double step = (maxY-minY) / childs.size();
			
			for(int i = 0 ; i < childs.size() ; i++) {
				g.drawLine(xPos, minY + (maxY-minY)/2, xPos + delX, (int)Math.round(minY  +  step * (2 * i + 1)/2));
				paint(g,childs.get(i),xPos + delX,(int)Math.round(minY + step * i),(int)Math.round(minY + step * (i + 1)));
			}
		}
		
	}

	private static final JPanel contentPanel = new JPanel();
	private DrawPanelEvoTree draw;
	private NodeJpanel activNode = null;;

	/**
	 * Create the dialog.
	 */
	public EvolTreeDialog() {
		addWindowListener(new WindowAdapter() {
			public void windowDeactivated(WindowEvent e) {
		    	EvolutionTree.root.setSelected(false);
		    	CellUpdate();
	    		activNode = null;
			}
		    public void windowActivated(WindowEvent e) {
		    	EvolutionTree.root.setSelected(true);
		    	CellUpdate();
	    		activNode = null;
		    }
		});
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			draw = new DrawPanelEvoTree();
			contentPanel.add(draw, BorderLayout.CENTER);
		}
	}
	
    public void repaint() {
    	super.repaint();
    	draw.repaint();
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

}
