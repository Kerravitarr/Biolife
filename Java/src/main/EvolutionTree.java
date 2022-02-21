package main;

import java.awt.Color;
import java.util.Vector;

import panels.JSONmake;

public class EvolutionTree {

	static public class Node{
		/**Время, когда узел появился*/
		long time = 0;
		/**Мутации гена*/
		int newadr = -1;
		/**Мутации гена*/
		int newCmd = -1;
		//Новая фотосинтетичность
		double nowPh = -1;
		//Новый фенотип
		Color color = null;
		//Поколение в этой ветке
		long generation = 0;
		/**Ветвь нашего узла*/
		String branch = "0";
		//Потомки нашего узла
		Vector<Node> child = new Vector<>();
		/**Счётчик ветвей*/
		int branshCount = 0;
		/**Число живых потомков*/
		int countAliveCell = 1;
		/**Наш родитель, общий предок, если хотите*/
		Node perrent = null;
		
		public Node newNode(long time,int newadr,int newCmd) {
			Node node = forAllNode(time);
			node.newadr = newadr;
			node.newCmd = newCmd;
			return node;
		}
		public Node newNode(long time,double nowPh) {
			Node node = forAllNode(time);
			node.nowPh = nowPh;
			return node;
		}
		public Node newNode(long time,Color color) {
			Node node = forAllNode(time);
			node.color = color;
			return node;
		}
		private Node forAllNode(long time) {
			Node node = new Node();
			synchronized (child) {
				node.generation = branshCount + 1;
				branshCount++;
			}
			child.add(node);
			node.perrent = this;
			node.time = time;
			node.branch = branch + "->" + node.generation;
			return node;
		}
		/**Создаёт новую ветку, куда будем эволюционировать*/
		public Node clone() {
			countAliveCell++;
			return this;
		}
		/**Увы, очередной наш потомок того. Если потомков больше нет, то ветвь тупиковая - удаляем!*/
		public void remove() {
			countAliveCell--;
			if(countAliveCell == 0 && child.size() == 0)
				perrent.remove(this);
		}
		/**Наш ребёнок сказал нам, что умер. Если это был последний наш ребёнок, то всё - сворачиваем лавочку*/
		private void remove(Node node) {
			child.remove(node);
			if(countAliveCell == 0 && child.size() == 0)
				perrent.remove(this);
		}
		
		public String toString() {
			String ret = "Поколение " + generation + " мутация произошла в " + time + " от рожденства Адама. Мутация: ";
			if(newadr != -1)
				ret += "гена " + newadr + " на " + newCmd;
			else if(nowPh != -1)
				ret += " фотосинтетичности, до " + nowPh;
			else if(color != null)
				ret += " цвета, до " + color.toString();
			ret += ". Мутация порадила " + child.size() + " потомков";
			return ret;
		}
		
		public JSONmake toJSON() {
			JSONmake make = new JSONmake();
			make.add("time", time);
			make.add("branch", branch);
			if(newadr != -1) {
				make.add("newadr", newadr);
				make.add("newCmd", newCmd);
			}else if(nowPh != -1)
				make.add("nowPh", nowPh);
			else if(color != null)
				make.add("color", Integer.toHexString(color.getRGB()));
			
			JSONmake[] nodes = new JSONmake[child.size()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = child.get(i).toJSON();
			}
			make.add("Nodes", nodes);
			return make;
		}
	}
	
	/**Корень эволюционного дерева, адам*/
	Node root = new Node();
	
	public String toString() {
		return root.toString();
	}

	public JSONmake toJSON() {
		JSONmake make = new JSONmake();
		make.add("Node", root.toJSON());
		return make;
	}
	
}
