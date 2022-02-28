package main;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import Utils.JSONmake;
import jdk.jfr.Unsigned;

//@Deprecated
public class EvolutionTree {

	static public class Node{
		/**Время, когда узел появился*/
		long time = 0;
		//Поколение в этой ветке
		long generation = 0;
		//Потомки нашего узла
		Vector<Node> child = new Vector<>();
		/**Счётчик ветвей*/
		int branshCount = 0;
		/**Число живых потомков*/
		int countAliveCell = 0;
		/**Наш родитель, общий предок, если хотите*/
		Node perrent = null;
		
		public Node(){};
		
		public Node(JSONmake node) {
			time = node.getL("time");
			generation = node.getL("generation");
			branshCount = node.getI("branshCount");
			countAliveCell = node.getI("countAliveCell");
			
			for(JSONmake i : node.getAJ("Nodes")) {
				Node nodeR = new Node(i);
				nodeR.perrent = this;
				child.add(nodeR);
			}
		}
		public Node newNode(long time) {
			remove(); //Мы больше не служим нашему родителю!
			Node node = new Node();
			synchronized (this) {
				node.generation = branshCount + 1;
				branshCount++;
				node.countAliveCell = 1;
				child.add(node);
				node.perrent = this;
				node.time = time;
			}
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
			if(countAliveCell == 0 && child.size() == 0)// У нас нет детей, всё, удаляем у родителя
				perrent.remove(this); 
			else if(countAliveCell == 0 && child.size() == 1) 
				merge();
			
		}
		/**Наш ребёнок сказал нам, что умер. Если это был последний наш ребёнок, то всё - сворачиваем лавочку*/
		private void remove(Node node) {
			child.remove(node);
			if(countAliveCell == 0 && child.size() == 0)
				perrent.remove(this);
			else if(countAliveCell == 0 && child.size() == 1)
				merge();
		}
		
		private void merge() {
			if(perrent != null) {
				synchronized (root) {
					perrent.child.add(child.get(0)); 
					child.get(0).perrent = perrent;
					child.get(0).generation = generation;
				}
				perrent.remove(this); 
			} else {
				synchronized (root) {
					root = child.get(0); 
					root.perrent = null;
					root.generation = 0;
				}
			}
		}
		
		public String toString() {
			String ret = "Поколение " + generation + " мутация произошла в " + time + " от рожденства Адама. ";
			ret += "Мутация порадила " + child.size() + " потомков";
			return ret;
		}
		
		public JSONmake toJSON() {
			JSONmake make = new JSONmake();
			make.add("time", time);
			make.add("generation", generation);
			make.add("branshCount", branshCount);
			make.add("countAliveCell", countAliveCell);
			make.add("branch", getBranch());
			
			JSONmake[] nodes = new JSONmake[child.size()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = child.get(i).toJSON();
			}
			make.add("Nodes", nodes);
			return make;
		}
		public String getBranch() {
			if(perrent != null)
				return perrent.getBranch() + ">" + generation;
			else
				return "0";
		}

		public Node getChild(long genCh) {
			for (Node node : child) {
				if(node.generation == genCh)
					return node;
			}
			return null;
		}
	}
	
	/**Корень эволюционного дерева, адам*/
	static Node root = new Node();
	
	public EvolutionTree() {};
	public EvolutionTree(JSONmake json) {
		root = new Node(json.getJ("Node"));
	}

	public String toString() {
		return root.toString();
	}

	public JSONmake toJSON() {
		JSONmake make = new JSONmake();
		make.add("Node", root.toJSON());
		return make;
	}
	public Node getNode(String s) {
		String[] numbers = s.split(">");
		Node ret = root;
		
		for (int i = 1; i < numbers.length; i++)
			ret = ret.getChild(Long.parseLong(numbers[i]));
		
		return ret;
	}
}
