package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;

import MapObjects.AliveCell;
import MapObjects.AliveCellProtorype;
import MapObjects.CellObject;
import MapObjects.Poison;
import Utils.Utils;
import main.Configurations;
import main.Point;
import main.World;
import panels.Legend.Graph.MODE;

public class Legend extends JPanel{
	public static class Graph extends JPanel {
		private class UpdateScrinTask implements Runnable {
			@Override
			public void run() {
				if (!isVisible()) return;
				switch (getMode()) {
					case DOING -> {
						values = new Graph.Value[AliveCell.ACTION.size()];
						for(int i = 0 ; i < values.length ; i++) {
							var act = AliveCell.ACTION.staticValues[i];
							values[i] = new Graph.Value((i + 1.0) / values.length, 1.0 / values.length, act.description, new Color(act.r,act.g,act.b));
						}
					}
					case HP -> {
						values = new Graph.Value[10];
						for (int i = 0; i < values.length; i++) {
							values[i] = new Graph.Value(1.0 * (i + 1) / values.length, 1.0 / values.length, (i * AliveCell.MAX_HP / values.length) + "", Utils.getHSBColor(0, 1, 1, (0.25 + 3d*i / (4d*values.length))));
						}
					}
					case YEAR -> {
						maxAge = 0;
						for (int x = 0; x < Configurations.MAP_CELLS.width; x++) {
							for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
								CellObject cell = Configurations.world.get(new Point(x, y));
								if (cell != null && cell instanceof AliveCell acell)
									maxAge = Math.max(maxAge, acell.getAge());
							}
						}
						var rmaxAge = maxAge;
						maxAge *= 1.4;//Увеличиваем на 40%, чтобы избавиться от фиолетового и розового в цветах
						values = new Graph.Value[10];
						long mAge = 0;
						StringBuilder text = new StringBuilder(100);
						for (int i = 0; i < values.length; i++) {
							long nAge = (i+1) * rmaxAge / values.length;
							numToStr(mAge,text);
							text.append(" - ");
							numToStr(nAge,text);
							values[i] = new Graph.Value(1.0 * (i + 1) / values.length, 1.0 / values.length, text.toString(), Color.getHSBColor(i / (values.length * 1.4f), 0.9f, 0.9f));
							text.setLength(0);
							mAge = nAge;
						}
					}
					case PHEN ->  {
						values = new Graph.Value[AliveCellProtorype.Specialization.TYPE.size()];
						for(int i = 0 ; i < values.length ; i++) {
							var act = AliveCellProtorype.Specialization.TYPE.staticValues[i];
							values[i] = new Graph.Value((i + 1.0) / values.length, 1.0 / values.length, act.toString(), Utils.getHSBColor(act.color, 1f, 1f, 1f));
						}
					}
					case GENER -> {
						maxGenDef = 0;
						minGenDef = Long.MAX_VALUE;
						for (int x = 0; x < Configurations.MAP_CELLS.width; x++) {
							for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
								CellObject cell = Configurations.world.get(new Point(x, y));
								if (cell != null && cell instanceof AliveCell) {
									maxGenDef = Math.max(maxGenDef, ((AliveCell) cell).getGeneration());
									minGenDef = Math.min(minGenDef, ((AliveCell) cell).getGeneration());
								}
							}
						}
						var rdel = maxGenDef - minGenDef;
						long del = (long) (rdel * 1.4);//Увеличиваем на 40%, чтобы избавиться от фиолетового и розового в цветах
						maxGenDef = minGenDef + del;
						long mGen = minGenDef;
						StringBuilder text = new StringBuilder(100);
						values = new Graph.Value[10];
						for (int i = 0; i < values.length; i++) {
							long nGen = minGenDef + (i+1) * rdel / values.length;
							numToStr(mGen,text);
							text.append(" - ");
							numToStr(nGen,text);
							values[i] = new Graph.Value(1.0 * (i + 1) / values.length, 1.0 / values.length, text.toString(), Color.getHSBColor(i / (values.length * 1.4f), 0.9f, 0.9f));
							text.setLength(0);
							mGen = nGen;
						}
					}
					case MINERALS -> {
						long maxMP = 0;
						for (int x = 0; x < Configurations.MAP_CELLS.width; x++) {
							for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
								CellObject cell = Configurations.world.get(new Point(x, y));
								if (cell != null && cell instanceof AliveCell acell)
									maxMP = Math.max(maxMP, acell.getMineral());
							}
						}
						values = new Graph.Value[10];
						for (int i = 0; i < values.length; i++) {
							values[i] = new Graph.Value(1.0 * (i + 1) / values.length, 1.0 / values.length, Integer.toString((int) (i * maxMP / values.length)),Utils.getHSBColor(0.661111, 1, 1, (0.25 + 3d*i / (4d*values.length))));
						}
					}
					case POISON -> {
						values = new Graph.Value[10];
						for (int i = 0; i < values.length; i++) {
							var rg = (int) (255.0 * i / values.length);
							values[i] = new Graph.Value(1.0 * (i + 1) / values.length, 1.0 / values.length, (i * Poison.MAX_TOXIC / values.length) + "", new Color(rg, rg, rg));
						}
					}
					case EVO_TREE -> {
						values = new Graph.Value[0];
					}
				}
				if (updateSrin) {
					updateSrin = false;
					for (int x = 0; x < Configurations.MAP_CELLS.width; x++) {
						for (int y = 0; y < Configurations.MAP_CELLS.height; y++) {
							CellObject cell = Configurations.world.get(new Point(x, y));
							if (cell != null)
								cell.repaint();
						}
					}
				}
				Graph.this.repaint(1);
			}
			private void numToStr(long num, StringBuilder sb) {
				if(num < 1000){
					sb.append(Long.toString(num));
				} else if(num < 10000) {
					sb.append(Long.toString(num / 1000));
					sb.append(".");
					sb.append(Long.toString((num % 1000) / 100));
					sb.append("k");
				}  else if(num < 1000000) {
					sb.append(Long.toString(num / 1000));
					sb.append("k");
				} else if(num < 10000000) {
					sb.append(Long.toString(num / 1000000));
					sb.append(".");
					sb.append(Long.toString((num % 1000000) / 100000));
					sb.append("M");
				} else {
					sb.append(Long.toString(num / 1000000));
					sb.append("M");
				} 
			}
		}

		
		static final int HEIGHT = 40;
		static final int BORDER = 50;
		
		public enum MODE {DOING,HP,YEAR,PHEN, GENER, MINERALS, POISON, EVO_TREE}
		static MODE mode = MODE.DOING;
		/**Максимальный возраст объекта на экране*/
		static long maxAge = 0;
		/**Минимальное покаление объекта на экране*/
		static long minGenDef = 0;
		/**Максимальное покаление объекта на экране*/
		static long maxGenDef = 0;
		/**Если нужно обновить цвет объектов на экране*/
		boolean updateSrin = false;
		
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
			Configurations.TIME_OUT_POOL.scheduleWithFixedDelay(new UpdateScrinTask(), 1, 1, TimeUnit.SECONDS);
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			
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
		/**
		 * Превращает покаление клетки в конкретный цвет
		 * @param gen покаление
		 * @return Цвет
		 */
		public static Color generationToColor(long gen) {
			return Utils.getHSBColor(Utils.betwin(0.0, ((double)(gen - minGenDef))/(maxGenDef-minGenDef), 1.0), 1, 1,1);
		}
		/**
		 * Превращает возраст клетки в конкретный цвет
		 * @param age возраст, в тиках
		 * @return Цвет
		 */
		public static Color AgeToColor(long age) {
			return Utils.getHSBColor(Utils.betwin(0.0, ((double)age)/maxAge, 1.0), 1, 1,1);
		}
		
		public static Color HPtToColor(double hp){
			return Utils.getHSBColor(0, 1, 1, Utils.betwin(0, (0.25 + 3d * hp / (4d * AliveCell.MAX_HP)), 1.0));
		}
		public static Color MPtToColor(double mp){
			return Utils.getHSBColor(0.661111, 1, 1, Utils.betwin(0, (0.25 + 3d * mp / (4d * AliveCell.MAX_HP)), 1.0));
		}
	}

	/**Панель, на которой рисуются радиокнопки*/
	JPanel panel;
	/**Панель, на которой рисуются шкалы*/
	Graph graph;
	/**
	 * Create the panel.
	 */
	public Legend() {
		setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(panel, BorderLayout.NORTH);
		
		{
			var f = makeRB("Doing",Graph.MODE.DOING);
			f.setSelected(true);
			panel.add(f);
		}
		panel.add(makeRB("Hp",Graph.MODE.HP));
		panel.add(makeRB("Age",Graph.MODE.YEAR));
		panel.add(makeRB("Generation",Graph.MODE.GENER));
		panel.add(makeRB("Phenotype",Graph.MODE.PHEN));
		panel.add(makeRB("Mp",Graph.MODE.MINERALS));
		//panel.add(makeRB("Poison",Graph.MODE.POISON));
		panel.add(makeRB("EvoTree",Graph.MODE.EVO_TREE));
		

		graph = new Graph();
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
	}

	private void action(ActionEvent e, MODE doing) {
		for(var i : panel.getComponents()) {
			if(i instanceof JRadioButton rb) {
				rb.setSelected(i == e.getSource());
			}
		}
		Graph.mode = doing;
		graph.updateSrin = !Configurations.world.isActiv();
	}
	
	private JRadioButton makeRB(String name, Graph.MODE mode) {
		JRadioButton jrbuton = new JRadioButton(Configurations.getHProperty(Legend.class,"Label" + name));
		jrbuton.setToolTipText(Configurations.getHProperty(Legend.class,"ToolTip" + name));
		jrbuton.addActionListener(e->action(e,mode));
		jrbuton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		jrbuton.setFocusable(false);
		return jrbuton;
	}
	
	
}
