package main;

import java.awt.Color;
import java.util.Vector;

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
		//Потомки нашего узла
		Vector<Node> child = new Vector<>();
		
		public Node newNode(long time,int newadr,int newCmd) {
			Node node = new Node();
			node.newadr = newadr;
			node.newCmd = newCmd;
			node.time = time;
			node.generation = generation + 1;
			child.add(node);
			return node;
		}
		public Node newNode(long time,double nowPh) {
			Node node = new Node();
			node.nowPh = nowPh;
			node.time = time;
			node.generation = generation + 1;
			child.add(node);
			return node;
		}
		public Node newNode(long time,Color color) {
			Node node = new Node();
			node.color = color;
			node.time = time;
			node.generation = generation + 1;
			child.add(node);
			return node;
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
	}
	
	/**Корень эволюционного дерева, адам*/
	Node root = new Node();
	
	public String toString() {
		return root.toString();
	}
	
}
