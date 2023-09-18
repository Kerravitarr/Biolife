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
import java.awt.Graphics2D;

/**
 *
 * @author rjhjk
 */
public class EvolTreeDialog extends javax.swing.JDialog implements Configurations.EvrySecondTask{	
	/**Высота текста подписей*/
	static final int TEXT_SIZE = 12;
	/**Ключевой узел, от которого рисуем*/
	private EvolutionTree.Node rootNode = EvolutionTree.root;
	/**Пара чисел, для вычисления количества детей и узлов*/
	private static class Pair{	private int countAllChild,countChildCell; Pair(int cac, int ccc){countAllChild = cac; countChildCell = ccc;}}
	/**Круглая диаграмма времени или плоская?*/
	private boolean isCurcleDiagram = false;
	/**Пропорционально времени отображать или нет?*/
	private boolean isTimeLine = false;
	/**В режиме пропорции по времени отображает сколько на пк приходится 1 век. В режиме без пропрции показывает расстояние между слоями*/
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
		
		NodeJpanel(EvolutionTree.Node node,int x, int y){
			this.node = node;
			init();
			final var width = TEXT_SIZE * 4;
			setBounds(x - width / 2, y, width, TEXT_SIZE);
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
			final var cx = getWidth()/2;
			final var cy = getHeight()/2;
			final var gen = NodeJpanel.this.node.getGeneration();
			Utils.centeredText(g, cx, cy, TEXT_SIZE, String.valueOf(gen));
			g.setColor(node.getColor());
			if(gen < 10)
				Utils.drawCircle(g, cx, cy, TEXT_SIZE);
			else if(gen < 100)
				Utils.drawCircle(g, cx, cy, TEXT_SIZE * 2);
			else
				Utils.drawCircle(g, cx, cy, TEXT_SIZE * 3);
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
		/**Максимальная глубина, на которую могружается дерево. Количество ветвей по оси Y (по R), которые ещё имеют цифровое обозначение*/
		private int maxDeep = 0;
		/**Сколько у нас будет засечек времени при отображении диограммы в хронологическом порядке*/
		private final double countTimeLinePoints = 10;
		
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
		public void paintComponent(Graphics gold) {
			final var g = (Graphics2D) gold;
			final var rootPerent = rootNode.getPerrent();
			if(isCurcleDiagram) {
				final var startTimeOffset = rootNode.getTimeFounder() - (rootPerent == null ? 0 : (long)((Configurations.world.step - rootNode.getTimeFounder()) / 10d));
				final var cx = getWidth() / 2;
				final var cy = getMaxY() / 2;
				if(isTimeLine)
					timeline = ((double) Math.min(cx, cy)) / (Configurations.world.step - startTimeOffset);
				else
					timeline = Math.min(cx, cy) / (5);
				if(isNeedUpdate || Configurations.world.isActiv()) {
					removeAll();
					try{
						g.setColor(Color.WHITE);
						if(rootPerent != null){
							final var r = isTimeLine? timeline * (rootNode.getTimeFounder() - startTimeOffset) : timeline;
							add(new NodeJpanel(rootPerent, cx, cy - TEXT_SIZE / 2));
							addCircleNode(rootNode,cx,cy,0d, Math.PI*2, r, startTimeOffset);
						} else {
							addCircleNode(rootNode,cx,cy,0d,Math.PI*2, 0, startTimeOffset);
						}
						isNeedUpdate = false;
					}catch(Exception e){isNeedUpdate = true;} //Нормально всё, асинхронность выполнения и всё прочее
					repaint();
				}
				//Рисуем, собственно, поле и всех детей
				super.paintComponent(g);
				//А теперь рисуем связи между детками
				try{
					if(rootPerent != null){
						g.setColor(Color.WHITE);
						final var r = isTimeLine? timeline * (rootNode.getTimeFounder() - startTimeOffset) : timeline;
						g.drawLine(cx, cy, (int) (cx - r), cy);
						paintCircleNode(g,rootNode,cx,cy,0d,360d, r,0,0.8, startTimeOffset);
					} else {
						paintCircleNode(g,rootNode,cx,cy,0d,360d, 2,0,0.8, startTimeOffset);
					}
				}catch(Exception e){} //Нормально всё, асинхронность выполнения и всё прочее
				//А теперь, если надо, рисуем шкалу времени
				if(isTimeLine){
					g.setColor(AllColors.toDark(Color.BLACK, 100));
					var scale = (Configurations.world.step - startTimeOffset) / countTimeLinePoints;
					//Рисуем 10 отметок
					for (int i = 0; i < countTimeLinePoints; i++) {
						final int r = (int) Math.round(timeline * scale * i);
						if(r > 0)
							g.drawOval(cx - r, cy - r, r*2, r*2);
						if(i == 0)
							g.drawString(Utils.degree(Math.round(startTimeOffset + scale * i)), cx + r, cy);
						else
							g.drawString("+"+Utils.degree(Math.round(i*scale)), cx + r, cy);
					}
				}
			} else {
				final var startTimeOffset = rootNode.getTimeFounder();
				final var xStart = isTimeLine ? TEXT_SIZE * 3 : 0;
				final var xEnd = getWidth();
				if(isTimeLine)
					timeline = ((double) getMaxY() - TEXT_SIZE * 2) / (Configurations.world.step - startTimeOffset);
				else if(maxDeep == 0)
					timeline = TEXT_SIZE * 3;
				else
					timeline = (getMaxY()) / (maxDeep * 3d / 2);
				final var yStart = getMaxY() - (rootPerent == null ? 0 : TEXT_SIZE * 2);
				//Определяем, нужно ли нам узлы перестроить?
				if(isNeedUpdate || Configurations.world.isActiv()) {
					removeAll();
					try{
						if(rootPerent != null)
							addLineRootNode(rootPerent,(xEnd +xStart)/ 2, yStart + TEXT_SIZE*2);
						final var ndeep = addLinearNode(rootNode,xStart,xEnd, yStart,startTimeOffset, 0);
						isNeedUpdate = maxDeep != ndeep;
						maxDeep = ndeep;
					}catch(Exception e){isNeedUpdate = true;} //Нормально всё, асинхронность выполнения и всё прочее
					repaint();
				}
				//Рисуем, собственно, поле и всех детей
				super.paintComponent(g);
				//А теперь рисуем связи между детками
				try{
					if(rootPerent != null)
						paintLineRootNode(g,(xEnd +xStart) / 2,yStart, yStart + TEXT_SIZE*2);
					paintLineNode(g,rootNode,xStart,xEnd, yStart,0,0.8,startTimeOffset);
				}catch(Exception e){} //Нормально всё, асинхронность выполнения и всё прочее
				//А теперь, если надо, рисуем шкалу времени
				if(isTimeLine){
					g.setColor(Color.BLACK);
					g.drawLine(TEXT_SIZE,  TEXT_SIZE * 2, TEXT_SIZE, getMaxY());
					var scale = (Configurations.world.step - startTimeOffset) / countTimeLinePoints;
					//Рисуем 10 отметок
					for (double i = 0; i < countTimeLinePoints; i++) {
						if(i == 0)
							g.drawString(Utils.degree(Math.round(startTimeOffset)), TEXT_SIZE, (int) (getMaxY() - timeline));
						else
							g.drawString("+" + Utils.degree(Math.round(i*scale)), TEXT_SIZE, (int) (getMaxY() - scale * i * timeline));
					}
				}
			}
		}
		
		/**
		 * При линейной диаграмме добавляет родителя на экран
		 * @param root сам изображаемый узел
		 * @param xCenter Центр экрана
		 * @param yPos позиция по оси y на которой находится предок, чьего родителя мы добавляем
		 */
	    private void addLineRootNode(EvolutionTree.Node root, int xCenter, int yPos) {
			//Родитель ниже предка на половину длины длину плеча Y
			add(new NodeJpanel(root, xCenter, yPos));
		}
		/**
		 * При линейной диаграмме добавляет линию от родителя к первой клетке потомку на экран
		 * @param g холств
		 * @param xStart 
		 * @param xCenter Центр экрана
		 * @param yPos позиция по оси y на которой находится предок, чьего родителя мы добавляем
		 */
		private void paintLineRootNode(Graphics g, int xCenter, int y0, int yPos) {
			g.setColor(Color.WHITE);
			g.drawLine(xCenter, y0, xCenter, yPos);
		}
		/**
		 * Рисует узел и всех его потомков в линейной диаграмме
		 * @param root сам изображаемый узел
		 * @param xStart начало отрезка узла
		 * @param xEnd конец отрезка узла
		 * @param yPos позиция основателя узла по оси Y
		 * 
		 * @return максимальную глубину погружения, то есть сколько клеток по оси Y отображается
		 */
		private int addLinearNode(EvolutionTree.Node root, int xStart, int xEnd, int yPos, long startTimeOffset, int deep) {
			final var delX = (xEnd - xStart);
			final var centerX = xStart + delX / 2;
			add(new NodeJpanel(root, centerX, yPos));

			final var childs = root.getChild();
			if(childs.isEmpty() || (xEnd - xStart) < TEXT_SIZE * 2) return deep;
			
			final var step = ((double) delX) / childs.size();
			var maxD = deep;
			EvolutionTree.Node child = null;
			for(int i = 0 ; i < childs.size() ; i++) {
				child = next(childs,child);
				final var d = addLinearNode(child, 
						(int) Math.round(xStart + step * i), 
						(int) Math.round(xStart + step * (i + 1)), 
						(int) (isTimeLine ? getMaxY() - (child.getTimeFounder()-startTimeOffset) * timeline : yPos - timeline),
						startTimeOffset, deep + 1);
				maxD = Math.max(maxD, d);
			}
			return maxD;
		}
		/**
		 * Рисует линии между узлами
		 * @param g холст
		 * @param root сам изображаемый узел
		 * @param xStart начало отрезка узла
		 * @param xEnd конец отрезка узла
		 * @param yPos позиция по оси y
		 */
		private void paintLineNode(Graphics g, EvolutionTree.Node root, int xStart, int xEnd, int yPos, double colorStart, double colorEnd, long startTimeOffset) {
			final var childs = root.getChild();
			if(childs.isEmpty()) return;
			final var delX = (xEnd - xStart);
			final var delColor = (colorEnd - colorStart);
			final var stepXPerChild = ((double) delX) / childs.size();
			final var stepColor = delColor / childs.size();
			final var centerX = xStart + delX / 2;
			
			EvolutionTree.Node child = null;
			for(int i = 0 ; i < childs.size() ; i++) {
				child = next(childs,child);
				var cx = (xStart + stepXPerChild * i) + ((xStart + stepXPerChild * (i + 1)) - (xStart + stepXPerChild * i)) / 2;
				if(delColor > 0.5)
					g.setColor(Color.WHITE);
				else
					g.setColor(Utils.getHSBColor(colorStart + delColor / 2, 1.0, 1.0, 1.0));
				final var cy = (int) (isTimeLine ? getMaxY() - (child.getTimeFounder()-startTimeOffset) * timeline : yPos - timeline);
				g.drawLine(centerX, yPos, (int) cx, cy + TEXT_SIZE);
				paintLineNode(g,child, 
						(int) Math.round(xStart + stepXPerChild * i), 
						(int) Math.round(xStart + stepXPerChild * (i + 1)), 
						cy, 
						colorStart + stepColor * i, 
						colorStart + stepColor * (i + 1),
						startTimeOffset);
			}
		}
		/**Выстраивает узлы круглого дерева эволюции
		 * @param root узел, который рисуем
		 * @param cx центр по x
		 * @param cy
		 * @param startAngle угол, В РАДИАНАХА
		 * @param endAngle угол, В РАДИАНАХА
		 * @param r 
		 */
		private void addCircleNode(EvolutionTree.Node root, int cx, int cy, double startAngle, double endAngle, double r, long startTimeOffset) {
			final double delAngle = (endAngle - startAngle);
			final double centerAngle = startAngle + delAngle / 2;
			final int fromX = (int) Math.round(cx + r * Math.cos(centerAngle));
			final int fromY = getMaxY() - (int) Math.round(cy + r * Math.sin(centerAngle) + TEXT_SIZE / 2d);
			add(new NodeJpanel(root, fromX, fromY));
			
			final var childs = root.getChild();
			final var lenght = r * delAngle;
			if(childs.isEmpty() || lenght < TEXT_SIZE && r > 0) return;
			
			final double stepAnglePerChild = delAngle / childs.size();
			
			EvolutionTree.Node child = null;
			for(int i = 0 ; i < childs.size() ; i++) {
				child = next(childs,child);
				final var cr = (int) (isTimeLine? timeline * (child.getTimeFounder() - startTimeOffset) : r + timeline);
				final var csa = startAngle + stepAnglePerChild * i;
				final var cea = startAngle + stepAnglePerChild * (i + 1);
				addCircleNode(child,
						cx,cy,
						csa, 
						cea, 
						cr, startTimeOffset);
			}
		}
		/**Рисует линии между узлами в круглой диаграмме
		 * @param g холст
		 * @param root узел, на котором рисуем
		 * @param cx центр, вокруг которого рисуем
		 * @param cy центр, вокруг которого рисуем
		 * @param startAngle начальный гол
		 * @param endAngle конечный угол
		 * @param r радиус, на котором находится стартовая позиция
		 */
		private void paintCircleNode(Graphics2D g, EvolutionTree.Node root, int cx, int cy, double startAngle, double endAngle, double r, double colorStart, double colorEnd, long startTimeOffset) {
			final var childs = root.getChild();
			if(childs.isEmpty()) return;
			final double delAngle = (endAngle - startAngle);
			final double delColor = (colorEnd - colorStart);
			final double stepAnglePerChild = delAngle / childs.size();
			final double stepColor = delColor / childs.size();
			//final double centerAngle = Math.toRadians(startAngle + delAngle / 2);
			//final int fromX = (int) Math.round(cx + r * Math.cos(centerAngle));
			//final int fromY = getMaxY() - (int) Math.round(cy + r * Math.sin(centerAngle));
			if(childs.size() > 1){
				g.draw(new java.awt.geom.Arc2D.Double(cx - r, cy - r, r*2, r*2, startAngle + stepAnglePerChild/2, endAngle - startAngle - stepAnglePerChild, java.awt.geom.Arc2D.OPEN));
			}
			
			EvolutionTree.Node child = null;
			for(int i = 0 ; i < childs.size() ; i++) {
				child = next(childs,child);
				if(delColor > 0.5) g.setColor(Color.WHITE);
				else			   g.setColor(Utils.getHSBColor(colorStart + delColor / 2, 1.0, 1.0, 1.0));
				final var cr = (int) (isTimeLine? timeline * (child.getTimeFounder()-startTimeOffset) : r + timeline);
				final var csa = startAngle + stepAnglePerChild * i;
				final var cea = startAngle + stepAnglePerChild * (i + 1);
				final var cangle = Math.toRadians((csa + cea) / 2);
				final var cos = Math.cos(cangle);
				final var sin = Math.sin(cangle);
				g.drawLine((int)Math.round(cx + r * cos), getMaxY() - (int) Math.round(cy + r * sin), (int)Math.round(cx + cr * cos), getMaxY() - (int) Math.round(cy + cr * sin));
				//g.drawLine(fromX, fromY, (int)Math.round(cx + cr * Math.cos(Math.toRadians(csa))), getMaxY() - (int) Math.round(cy + cr * Math.sin(Math.toRadians(csa))));
				//g.drawLine(fromX, fromY, (int)Math.round(cx + cr * Math.cos(Math.toRadians(cea))), getMaxY() - (int) Math.round(cy + cr * Math.sin(Math.toRadians(cea))));
				paintCircleNode(g,child,
						cx,cy,
						csa, 
						cea, 
						cr, 
						colorStart + stepColor * i, 
						colorStart + stepColor * (i + 1),
						startTimeOffset);
			}
		}
		
		/**Возвращает самый нижний край, на котором мы можем рисовать
		 * @return 
		 */
		private int getMaxY(){
			return getHeight() - TEXT_SIZE*2;
		}

	}
		
	/** Creates new form E */
	public EvolTreeDialog() {
		super((Frame) null, false);
		initComponents();
		Configurations.setIcon(resetButton,"reset");
		resetButton.addActionListener( e-> restart());
		resetButton.setToolTipText(Configurations.getHProperty(EvolTreeDialog.class,"reset"));
		Configurations.setIcon(_timeLineButton,"timeline");
		_timeLineButton.setToolTipText(Configurations.getHProperty(EvolTreeDialog.class,"timelineB"));
		Configurations.setIcon(curcle,"cycleFilogen");
		curcle.setToolTipText(Configurations.getHProperty(EvolTreeDialog.class,"cycle"));
		Configurations.addTask(this);
		restart();
	}
	
	@Override
    public void taskStep() {
		final var v = Configurations.getViewer();
		if (v != null && v.get(Legend.class).getMode() == Legend.MODE.EVO_TREE || EvolTreeDialog.this.isVisible()){
			try{updateColor();} catch(java.lang.NullPointerException e){} //Всё нормально, у нас прямо во время перерисовывания изменилось дерево. Такое бывает частенько. Асинхронность
		}
		if(EvolTreeDialog.this.isVisible()){
			try{
				if(countPair(rootNode).countChildCell == 0)
					restart();
				//А теперь проверка. Если у нас корень - адам, а в дерев эволюции другой адам... У нас перезагрузилась карта!
				if(rootNode.getPerrent() == null && rootNode != EvolutionTree.root)
					restart();
			} catch(java.lang.NullPointerException e){} //Всё нормально, у нас прямо во время перерисовывания изменилось дерево. Такое бывает частенько. Асинхронность
			jPanelTree.updateUI();
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
	 * Раскрашивает дерево потомков в цвета, согласно их дереву эволюции
	 * @param root сам изображаемый узел
	 */
	private static void colorNode(EvolutionTree.Node root, double colorStart, double colorEnd) {
		var delColor = (colorEnd - colorStart);
		if(delColor > 0.5)
			root.setColor(Color.WHITE);
		else
			root.setColor(Utils.getHSBColor(colorStart + delColor / 2, 1.0, 1.0, 1.0));

		List<EvolutionTree.Node> childs = root.getChild();		
		var stepColor = delColor / childs.size();

		EvolutionTree.Node child = null;
		for(int i = 0 ; i < childs.size() ; i++) {
			child = next(childs,child);
			colorNode(child,colorStart + stepColor * i,colorStart + stepColor * (i + 1));
		}
	}
	/**
	 * Раскрашивает дерево предков в белый цвет. Все предки - белые
	 * @param root сам изображаемый узел
	 */
	private static void colorNode(EvolutionTree.Node root) {
		root.setColor(Color.WHITE);
		if(root.getPerrent() != null)
			colorNode(root.getPerrent());
	}
	/**Возвращает число узлов наследования и число живых клеток*/
	private static Pair countPair(EvolutionTree.Node root) {
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
	private static String formatNode(EvolutionTree.Node node, int row){
		var index = row % 7;
		try{
			Pair p = index == 2 ? countPair(node) : null;
			return switch (index) {
				default -> Configurations.getProperty(EvolTreeDialog.class,"nodeDescriptionNode", node.getBranch());
				case 1 -> Configurations.getProperty(EvolTreeDialog.class,"nodeDaughter",node.getChild().size());
				case 2 -> Configurations.getProperty(EvolTreeDialog.class,"nodeStatistic",p.countAllChild, p.countChildCell);
				case 3 -> dateBirth.format(node.getTimeFounder());
				case 4 -> founderYear.format(node.getFounder().getAge());
				case 5 -> Configurations.getProperty(EvolTreeDialog.class,"nodePoison",node.getFounder().getPosionType().toString());
				case 6 -> Configurations.getProperty(EvolTreeDialog.class,"nodeDna",node.getFounder().getDna().size);
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
	/**Возвращает следующий элемент списка по возрастанию
	 * @param list список
	 * @param prefur предыдущий элемент. Или Null, если нужен первый
	 * @return элемент, у которого getGeneration больше, чем у prefur
	 */
	private static EvolutionTree.Node next(List<EvolutionTree.Node> list, EvolutionTree.Node prefur) {
		if (prefur == null) {
			return list.stream().min((a, b) -> (int) (a.getGeneration() - b.getGeneration())).orElse(null);
		} else {
			var minG = Long.MAX_VALUE;
			EvolutionTree.Node ret = null;
			for (final var node : list) {
				if(node.getGeneration() > prefur.getGeneration() && node.getGeneration() < minG){
					minG = node.getGeneration();
					ret = node;
				}
			}
			return ret;
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
        mainP = new javax.swing.JPanel();
        jPanelTree = new DrawPanelEvoTree();
        jPanel1 = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();
        curcle = new javax.swing.JToggleButton();
        _timeLineButton = new javax.swing.JToggleButton();

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
            .addGap(0, 398, Short.MAX_VALUE)
        );

        mainP.add(jPanelTree, java.awt.BorderLayout.CENTER);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setAlignmentX(0.0F);
        jPanel1.setAlignmentY(0.0F);

        resetButton.setText("1");
        resetButton.setAlignmentY(0.0F);
        resetButton.setBorderPainted(false);
        resetButton.setContentAreaFilled(false);
        resetButton.setDefaultCapable(false);
        resetButton.setFocusPainted(false);
        resetButton.setFocusable(false);
        resetButton.setInheritsPopupMenu(true);
        resetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        resetButton.setPreferredSize(new java.awt.Dimension(40, 20));

        curcle.setText("jToggleButton1");
        curcle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                curcleActionPerformed(evt);
            }
        });

        _timeLineButton.setText("jToggleButton1");
        _timeLineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _timeLineButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(486, Short.MAX_VALUE)
                .addComponent(curcle, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_timeLineButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(resetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(curcle)
                    .addComponent(_timeLineButton)))
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

    private void curcleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_curcleActionPerformed
        isCurcleDiagram = curcle.isSelected();
		 ((DrawPanelEvoTree)jPanelTree).isNeedUpdate = true;
    }//GEN-LAST:event_curcleActionPerformed

    private void _timeLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__timeLineButtonActionPerformed
        isTimeLine = _timeLineButton.isSelected();
		 ((DrawPanelEvoTree)jPanelTree).isNeedUpdate = true;
    }//GEN-LAST:event__timeLineButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton _timeLineButton;
    private javax.swing.JToggleButton curcle;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelTree;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainP;
    private javax.swing.JButton resetButton;
    // End of variables declaration//GEN-END:variables
}
