package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.EmptyBorder;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import Utils.Utils;
import main.EvolutionTree;
import main.Point;
import main.World;
import main.EvolutionTree.Node;

public class EvolTree extends JDialog {
	static int delX = 23;
	
	public static class NodeJpanel extends JPanel{
		Node node = null;
		NodeJpanel(Node node){
			this.node = node;
			addMouseListener(new MouseAdapter() {
			    public void mouseClicked(MouseEvent e) {
			    	NodeJpanel.this.node.setSelected(!NodeJpanel.this.node.isSelected());
			    	for (int x = 0; x < World.MAP_CELLS.width; x++) {
						for (int y = 0; y < World.MAP_CELLS.height; y++) {
							CellObject cell = World.world.get(new Point(x,y));
							if(cell != null)
								cell.repaint();
						}
					}
			    }
			});
		}

		public void paintComponent(Graphics g) {
			//super.paintComponent(g);
			if(getHeight() >= 10) {
				g.setColor(Color.BLACK);
				Utils.centeredText(g, getWidth()/2, getHeight()/2, 10, NodeJpanel.this.node.getGeneration()+"");
				if(NodeJpanel.this.node.isSelected())
					Utils.drawCircle(g, getWidth()/2, getHeight()/2, 10);
			}
		}
	}
	
	public class DrawPanelEvoTree extends JPanel {
		
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
			super.paintComponent(g);
			int xPos = delX * 2;
			int minY = delX / 2;
			int maxY = getHeight() - minY;
			
			paint(g,EvolutionTree.root,xPos,minY,maxY);
		}
		
	    public void repaint() {
	    	super.repaint();
	    	
			int xPos = delX * 2;
			int minY = delX / 2;
			int maxY = getHeight() - minY;
			
			removeAll();
			paint(EvolutionTree.root,xPos,minY,maxY);
	    }
	    private void paint(Node root, int xPos, int minY, int maxY) {
	    	NodeJpanel rootJ = new NodeJpanel(root);
	    	add(rootJ);
	    	rootJ.setBounds(xPos - delX / 2, minY + (maxY-minY)/2 - delX / 2, Math.min((maxY-minY)/2,delX), Math.min((maxY-minY)/2,delX));
	    	
			Vector<Node> childs = root.getChild();
			if(childs.size() == 0 || maxY-minY < 10) return;
			double step = (maxY-minY) / childs.size();
			
			for(int i = 0 ; i < childs.size() ; i++) {
				paint(childs.get(i),xPos + delX,(int)Math.round(minY + step * i),(int)Math.round(minY + step * (i + 1)));
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

	private final JPanel contentPanel = new JPanel();
	private JPanel draw;

	/**
	 * Create the dialog.
	 */
	public EvolTree() {
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

}
