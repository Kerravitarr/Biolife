package main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import MapObjects.AliveCell;
import Utils.JSONmake;

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
		
		//ПРИМЕЧАТЕЛЬНЫЕ ТОЧКИ
		//ДНК клетки, образующей узел
		public int [] DNA = null;
		//Цвет
		public Color phenotype = null;
		
		//====================СПЕЦ ПЕРЕМЕННЫЕ. Нужны для дерева эволюции
		//Показывает, нужно ли обновлять цвета у ботов
		boolean isSelected = false;
		
		private Node(){	};
		
		public Node(JSONmake node) {
			this();
			time = node.getL("time");
			generation = node.getL("generation");
			branshCount = node.getI("branshCount");
			countAliveCell = 0;
			phenotype = new Color((Long.decode("0x"+node.getS("phenotype"))).intValue(),true);

	    	List<Long> mindL = node.getAL("DNA");
	    	DNA = new int[mindL.size()];
	    	for (int i = 0; i < DNA.length; i++) 
	    		DNA[i] = mindL.get(i).intValue();
			
			for(JSONmake i : node.getAJ("Nodes")) {
				Node nodeR = new Node(i);
				nodeR.perrent = this;
				getChild().add(nodeR);
			}
		}
		/**
		 * Создаёт новый узел в дереве эволюции в определённый момент времени
		 * @param time - текущее время, когда произошла мутация
		 * @return новый узел
		 */
		public Node newNode(AliveCell cell,long time) {
			Node node = new Node();
			synchronized (root) {
				node.generation = ++branshCount;
				node.countAliveCell = 1;
				getChild().add(node);
				node.perrent = this;
				node.time = time;
				node.isSelected = isSelected();
			}
			node.setChild(cell);
			remove(); //Мы больше не служим нашему родителю!
			return node;
		}

		private void setChild(AliveCell cell) {
			DNA = cell.getDNA();
			if(DNA == null) {
				Configurations.world.isActiv = false;
				return;
			}
			phenotype = new Color(cell.phenotype.getRGB());
		}
		/**Создаёт новую ветку, куда будем эволюционировать*/
		public Node clone() {
			countAliveCell++;
			return this;
		}
		/**Увы, очередной наш потомок того. Если потомков больше нет, то ветвь тупиковая - удаляем!*/
		public void remove() {
			countAliveCell--;
			if(countAliveCell <= 0 && getChild().size() == 0)// У нас нет детей, всё, удаляем у родителя
				perrent.remove(this); 
			else if(countAliveCell <= 0 && getChild().size() == 1) 
				merge();
			
		}
		/**Наш ребёнок сказал нам, что умер. Если это был последний наш ребёнок, то всё - сворачиваем лавочку*/
		private void remove(Node node) {
			getChild().remove(node);
			if(countAliveCell <= 0 && getChild().size() == 0)
				perrent.remove(this);
			else if(countAliveCell <= 0 && getChild().size() == 1)
				merge();
		}
		/**
		 * Сливает нас с нашим предком сверху
		 */
		private void merge() {
			if(perrent != null) {
				synchronized (root) {
					perrent.getChild().add(getChild().get(0)); 
					getChild().get(0).perrent = perrent;
					getChild().get(0).generation = getGeneration();
				}
				perrent.remove(this); 
			} else {
				synchronized (root) {
					root = getChild().get(0); 
					root.perrent = null;
					root.generation = 0;
				}
			}
		}
		
		public String toString() {
			String ret = "Поколение " + getGeneration() + " мутация произошла в " + time + " от рожденства Адама. ";
			ret += "Мутация порадила " + getChild().size() + " потомков";
			return ret;
		}
		
		public JSONmake toJSON() {
			JSONmake make = new JSONmake();
			make.add("time", time);
			make.add("generation", getGeneration());
			make.add("branshCount", branshCount);
			make.add("countAliveCell", countAliveCell);
			make.add("branch", getBranch());
			make.add("phenotype",phenotype.getRGB()+"");
			make.add("DNA", DNA);
			
			JSONmake[] nodes = new JSONmake[getChild().size()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = getChild().get(i).toJSON();
			}
			make.add("Nodes", nodes);
			return make;
		}
		public String getBranch() {
			if(perrent != null)
				return perrent.getBranch() + ">" + getGeneration();
			else
				return "0";
		}

		public Node getChild(long genCh) {
			for (Node node : child) {
				if(node.getGeneration() == genCh)
					return node;
			}
			return null;
		}

		/**
		 * @return the generation
		 */
		public long getGeneration() {
			return generation;
		}

		/**
		 * @return the child
		 */
		public Vector<Node> getChild() {
			return child;
		}

		/**
		 * Возвращает все окончания ветви (у которых нет детей) для данного узла вниз
		 * @return
		 */
		public List<Node> getEndNode() {
			List<Node> ret = new ArrayList<>();
			if(child.size() == 0) {
				ret.add(this);
			} else {
				for (Node node : child) {
					ret.addAll(node.getEndNode());
				}
			}
			return ret;
		}

		public int getAlpha() {
			return isSelected() ? 124 : 255;
		}

		/**
		 * @param isSelected the isSelected to set
		 */
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
			for (Node node : child)
				node.setSelected(this.isSelected());
		}

		/**
		 * @return the isSelected
		 */
		public boolean isSelected() {
			return isSelected;
		}
	}
	
	/**Корень эволюционного дерева, адам*/
	public static Node root = new Node();
	
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
	/**
	 * Функция, используемая при загрузке. Заодно помогает понять у какой ветви сколько потомков
	 * @param s полная строка эволюционного дерева, то есть 0>1>2...
	 * @return Конкретный узел дерева
	 */
	public Node getNode(String s) {
		String[] numbers = s.split(">");
		Node ret = root;
		
		for (int i = 1; i < numbers.length; i++)
			ret = ret.getChild(Long.parseLong(numbers[i]));
		
		ret.countAliveCell++; //Подсчитали ещё одного живчика
		return ret;
	}
	/**
	 * Обновляет дерево эволюции - проверяет все узлы на их правильность и перерисовывает дерево
	 */
	public void updatre() {
		for(Node node : root.getEndNode()) {
			if(node.countAliveCell == 0)
				node.remove();
		}
	}

	public void setAdam(AliveCell adam) {
		root.countAliveCell = 1;
		root.setChild(adam);
	}
}
