package main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import MapObjects.AliveCell;
import MapObjects.Poison;
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
		private AtomicInteger branshCount = new AtomicInteger(0);
		/**Число живых потомков*/
		private AtomicInteger countAliveCell = new AtomicInteger(0);
		/**Наш родитель, общий предок, если хотите*/
		Node perrent = null;
		
		//ПРИМЕЧАТЕЛЬНЫЕ ТОЧКИ
		//ДНК клетки, образующей узел
		public int [] DNA = null;
		//Цвет
		public Color phenotype = null;
		/**Устойчивость к яду*/
		public Poison.TYPE poisonType = null;
		
		//====================СПЕЦ ПЕРЕМЕННЫЕ. Нужны для дерева эволюции
		//Показывает, нужно ли обновлять цвета у ботов
		boolean isSelected = false;
		
		private Node(){	};
		
		public Node(JSONmake node) {
			this();
			time = node.getL("time");
			generation = node.getL("generation");
			branshCount.set(node.getI("branshCount"));
			countAliveCell.set(0);
			phenotype = new Color((Long.decode("0x"+node.getS("phenotype"))).intValue(),true);
			poisonType = Poison.TYPE.toEnum(node.getI("poisonType"));

	    	List<Long> mindL = node.getAL("DNA");
	    	DNA = new int[mindL.size()];
	    	for (int i = 0; i < DNA.length; i++) 
	    		DNA[i] = mindL.get(i).intValue();
			
			for(JSONmake i : node.getAJ("Nodes")) {
				Node nodeR = new Node(i);
				nodeR.perrent = this;
				child.add(nodeR);
			}
		}
		/**
		 * Создаёт новый узел в дереве эволюции в определённый момент времени
		 * @param time - текущее время, когда произошла мутация
		 * @return новый узел
		 */
		public Node newNode(AliveCell cell,long time) {
			Node node = new Node();
			node.generation = branshCount.incrementAndGet();
			node.countAliveCell.set(1);
			node.isSelected = isSelected();
			node.time = time;
			node.setChild(cell);
			
			node.perrent = this;
			child.add(node);
			remove(); //Мы больше не служим нашему родителю!
			return node;
		}

		private void setChild(AliveCell cell) {
			DNA = cell.getDNA();
			phenotype = new Color(cell.phenotype.getRGB());
			poisonType = cell.getPosionType();
		}
		/**Не создаёт ветки эволюции, просто помечает, что существует ещё один потомок у этой ветки эволюции*/
		public Node clone() {
			countAliveCell.incrementAndGet();
			return this;
		}
		/**Увы, очередной наш потомок того. Если потомков больше нет, то ветвь тупиковая - удаляем!*/
		public void remove(){
			if(countAliveCell.decrementAndGet() == 0 && child.size() <= 1)
				removeNode.add(this);
		}
		/**Наш ребёнок сказал нам, что умер. Если это был последний наш ребёнок, то всё - сворачиваем лавочку*/
		private void remove(Node node) {
			getChild().remove(node);
			node.removeChild();
			if(countAliveCell.get() == 0 && child.size() == 0)
				perrent.remove(this);
			else if(countAliveCell.get() == 0 && child.size() == 1)
				merge();
		}
		/**
		 * Сливает нас с нашим предком сверху
		 */
		private void merge() {
			if(child == null)
				return;
			Node margeNode = child.get(0);
			if(perrent != null) {
				perrent.getChild().add(margeNode); 
				margeNode.perrent = perrent;
				margeNode.generation = getGeneration();
				perrent.remove(this);
				if(perrent.child == null)
					return;
				
				HashMap<Long, Integer> map = new HashMap<>();
				for (Node node : perrent.child) {
					if(!map.containsKey(node.generation)) {
						map.put(node.generation, 0);
					} else {
						throw new RuntimeException("ЖОПА");
					}
				}
			} else {
				root = margeNode; 
				root.perrent = null;
				root.generation = 0;
			}
		}
		
		private void removeChild() {
			child = null;
		}
		
		public String toString() {
			String ret = "Поколение " + getGeneration() + " мутация произошла в " + time + " от рожденства Адама. ";
			if(child != null)
				ret += "Мутация порадила " + getChild().size() + " потомков";
			return ret;
		}
		
		public JSONmake toJSON() {
			JSONmake make = new JSONmake();
			make.add("time", time);
			make.add("generation", getGeneration());
			make.add("branshCount", branshCount.get());
			make.add("countAliveCell", countAliveCell.get());
			make.add("branch", getBranch());
			make.add("phenotype",Integer.toHexString(phenotype.getRGB()));
			make.add("DNA", DNA);
			make.add("poisonType", poisonType.ordinal());
			
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
	/***/
	private static Vector<Node> removeNode = new Vector<>();
	
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
		
		for (int i = 1; i < numbers.length; i++) {
			ret = ret.getChild(Long.parseLong(numbers[i]));
		}
		
		ret.countAliveCell.incrementAndGet(); //Подсчитали ещё одного живчика
		return ret;
	}
	/**
	 * Обновляет дерево эволюции - проверяет все узлы на их правильность и перерисовывает дерево
	 */
	public void updatre() {
		for(Node node : root.getEndNode()) {
			if(node.countAliveCell.get() <= 0)
				node.remove();
		}
	}
	public void step() {
		if(removeNode.size() > 0) {
			for(Node node :removeNode) {
				if(node.getChild() == null)
					continue;
				else if (node.getChild().size() == 0)// У нас нет детей, всё, удаляем у родителя
					node.perrent.remove(node); 
				else if(node.getChild().size() == 1) 
					node.merge();
			}
			removeNode.clear();
		}
	}

	public void setAdam(AliveCell adam) {
		root.countAliveCell.set(1);
		root.setChild(adam);
	}
}
