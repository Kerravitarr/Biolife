package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;

import main.Cell;
import main.Point;
import main.World;

public class Legend extends JPanel {
	
	public static class Graph extends JPanel{
		static final int HEIGHT = 40;
		static final int BORDER = 50;
		
		public enum MODE {	DOING,HP,YEAR,PHEN, GENER, MINERALS}
		static MODE mode = MODE.DOING;
		
		static long maxAge = 0;
		static long maxGenDef = 0;
		boolean isNedUpdate = false;
		
		/**Значение*/
		class Value{
			public Value(double right, double width, String title, Color color) {
				this.x=right - width/2;
				this.width=width/2;
				this.title=title;
				this.color=color;
			}
			/**0-1, где находится значение*/
			double x ;
			/**0-1 его ширина*/
			double width;
			//Подпись
			String title;
			//Цвет занчения
			Color color;
		}
		Value[] values = new Value[0];
		
		Graph(){
			new Timer().schedule(new TimerTask() { // Определяем задачу
			    @Override
			    public void run() {
			    	if(!isVisible()) return;
	
			    	switch (getMode()) {
						case DOING -> {
							values = new Value[4];
							values[0] = new Value(1.0/4,1.0/4,"Фотосинтез",Color.GREEN);
							values[1] = new Value(2.0/4,1.0/4,"Охота",Color.RED);
							values[2] = new Value(3.0/4,1.0/4,"Минерализация",Color.BLUE);
							values[3] = new Value(4.0/4,1.0/4,"Мёртвый",new Color(139,69,19,100));
						}
						case HP ->{
							long maxHP = 0;
							for (int x = 0; x < World.MAP_CELLS.width; x++) {
								for (int y = 0; y < World.MAP_CELLS.height; y++) {
									Cell cell = World.world.get(new Point(x,y));
									if(cell != null)
										maxHP = Math.max(maxHP, cell.getHealth());
								}
							}
							values = new Value[10];
							for (int i = 0; i < values.length; i++) {
								values[i] = new Value(1.0 * (i+1) / values.length,1.0/values.length,(i*maxHP/values.length)+"",new Color((int) (255.0*i/values.length),0,0));
							}
						}
						case YEAR ->{
							maxAge = 0;
							for (int x = 0; x < World.MAP_CELLS.width; x++) {
								for (int y = 0; y < World.MAP_CELLS.height; y++) {
									Cell cell = World.world.get(new Point(x,y));
									if(cell != null)
										maxAge = Math.max(maxAge, cell.getAge());
								}
							}
							values = new Value[10];
							for (int i = 0; i < values.length; i++) {
								values[i] = new Value(1.0 * (i+1) / values.length,1.0/values.length,(i*maxAge/values.length)+"",Color.getHSBColor((float)(1.0*i/values.length), (float)0.9, (float)0.9));
							}
						}
						case PHEN -> {values = new Value[0];}
						case GENER ->{
							maxGenDef = 0;
							for (int x = 0; x < World.MAP_CELLS.width; x++) {
								for (int y = 0; y < World.MAP_CELLS.height; y++) {
									Cell cell = World.world.get(new Point(x,y));
									if(cell != null)
										maxGenDef = Math.max(maxGenDef, cell.getGeneration());
								}
							}
							values = new Value[10];
							for (int i = 0; i < values.length; i++) {
								values[i] = new Value(1.0 * (i+1) / values.length,1.0/values.length,(i*maxGenDef/values.length)+"",Color.getHSBColor((float)(1.0*i/values.length), (float)0.9, (float)0.9));
							}
						}
						case MINERALS ->{
							long maxMP = 0;
							for (int x = 0; x < World.MAP_CELLS.width; x++) {
								for (int y = 0; y < World.MAP_CELLS.height; y++) {
									Cell cell = World.world.get(new Point(x,y));
									if(cell != null)
										maxMP = Math.max(maxMP, cell.getMineral());
								}
							}
							values = new Value[10];
							for (int i = 0; i < values.length; i++) {
								values[i] = new Value(1.0 * (i+1) / values.length,1.0/values.length,(i*maxMP/values.length)+"",new Color(0,0,(int) (255.0*i/values.length)));
							}
						}
					}
			    	if(isNedUpdate) {
			    		isNedUpdate = false;
				    	for (int x = 0; x < World.MAP_CELLS.width; x++) {
							for (int y = 0; y < World.MAP_CELLS.height; y++) {
								Cell cell = World.world.get(new Point(x,y));
								if(cell != null)
									cell.repaint();
							}
						}
			    	}
			    	Graph.this.repaint(1);
			    }
			}, 0L, 5000);
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(Color.BLACK);
			int width = getWidth()-BORDER*2;
			g.drawLine(BORDER, HEIGHT-20, getWidth()-BORDER, HEIGHT-20);
			
			for(Value i : values) {
				g.setColor(i.color);
				g.fillRect(BORDER+(int) ((i.x-i.width)*width), 0, (int) (i.width*width * 2), HEIGHT-25);
				g.drawString(i.title, BORDER+(int) (i.x*width) - i.title.length()*5/2, HEIGHT-10);
			}
		}

		/**
		 * @return the mode
		 */
		public static MODE getMode() {
			return mode;
		}

		public static double getMaxAge() {
			return maxAge;
		}

		public static double getMaxGen() {
			return maxGenDef;
		}
	}

	/**
	 * Create the panel.
	 */
	public Legend() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(panel, BorderLayout.NORTH);
		
		JRadioButton doing = new JRadioButton("Род деятельности");
		doing.setFont(new Font("Tahoma", Font.PLAIN, 11));
		doing.setSelected(true);
		panel.add(doing);
		
		JRadioButton hp = new JRadioButton("Здоровье");
		hp.setFont(new Font("Tahoma", Font.PLAIN, 11));
		panel.add(hp);
		
		JRadioButton year = new JRadioButton("Возраст");
		year.setFont(new Font("Tahoma", Font.PLAIN, 11));
		panel.add(year);
		
		JRadioButton generation = new JRadioButton("Поколение");
		generation.setFont(new Font("Tahoma", Font.PLAIN, 11));
		panel.add(generation);
		
		JRadioButton phenotype = new JRadioButton("Фенотип");
		phenotype.setFont(new Font("Tahoma", Font.PLAIN, 11));
		panel.add(phenotype);
		
		JRadioButton mineral = new JRadioButton("Минералы");
		mineral.setFont(new Font("Tahoma", Font.PLAIN, 11));
		panel.add(mineral);

		Graph graph = new Graph();
		add(graph, BorderLayout.CENTER);
		GroupLayout gl_Graph = new GroupLayout(graph);
		gl_Graph.setHorizontalGroup(
			gl_Graph.createParallelGroup(Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		gl_Graph.setVerticalGroup(
			gl_Graph.createParallelGroup(Alignment.LEADING)
				.addGap(0, Graph.HEIGHT, Short.MAX_VALUE)
		);
		graph.setLayout(gl_Graph);

		doing.addActionListener(e->{
			if(doing.isSelected()) {
				Graph.mode = Graph.MODE.DOING;
				hp.setSelected(false);
				year.setSelected(false);
				generation.setSelected(false);
				phenotype.setSelected(false);
				mineral.setSelected(false);
				graph.isNedUpdate = true;
			}else {
				doing.setSelected(true);
			}
		});
		hp.addActionListener(e->{
			if(hp.isSelected()) {
				Graph.mode = Graph.MODE.HP;
				doing.setSelected(false);
				year.setSelected(false);
				generation.setSelected(false);
				phenotype.setSelected(false);
				mineral.setSelected(false);
				graph.isNedUpdate = true;
			} else {
				hp.setSelected(true);
			}
		});
		year.addActionListener(e->{
			if(year.isSelected()) {
				Graph.mode = Graph.MODE.YEAR;
				hp.setSelected(false);
				doing.setSelected(false);
				generation.setSelected(false);
				phenotype.setSelected(false);
				mineral.setSelected(false);
				graph.isNedUpdate = true;
			}else {
				year.setSelected(true);
			}
		});
		generation.addActionListener(e->{
			if(generation.isSelected()) {
				Graph.mode = Graph.MODE.GENER;
				hp.setSelected(false);
				year.setSelected(false);
				doing.setSelected(false);
				phenotype.setSelected(false);
				mineral.setSelected(false);
				graph.isNedUpdate = true;
			}else {
				generation.setSelected(true);
			}
		});
		phenotype.addActionListener(e->{
			if(phenotype.isSelected()) {
				Graph.mode = Graph.MODE.PHEN;
				hp.setSelected(false);
				year.setSelected(false);
				generation.setSelected(false);
				doing.setSelected(false);
				mineral.setSelected(false);
				graph.isNedUpdate = true;
			}else {
				phenotype.setSelected(true);
			}
		});
		mineral.addActionListener(e->{
			if(mineral.isSelected()) {
				Graph.mode = Graph.MODE.MINERALS;
				hp.setSelected(false);
				year.setSelected(false);
				generation.setSelected(false);
				doing.setSelected(false);
				phenotype.setSelected(false);
				graph.isNedUpdate = true;
			}else {
				mineral.setSelected(true);
			}
		});
	}
}
