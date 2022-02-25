package main;

import java.awt.Color;
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
		Integer branshCount = 0;
		/**Число живых потомков*/
		Integer countAliveCell = 0;
		/**Наш родитель, общий предок, если хотите*/
		Node perrent = null;
		
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
					perrent = null;
					generation = 0;
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
				return perrent.getBranch() + "->" + generation;
			else
				return "0";
		}
	}
	
	/**Корень эволюционного дерева, адам*/
	static Node root = new Node();
	
	public String toString() {
		return root.toString();
	}

	public JSONmake toJSON() {
		JSONmake make = new JSONmake();
		make.add("Node", root.toJSON());
		return make;
	}
}
