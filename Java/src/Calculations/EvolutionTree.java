package Calculations;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import Utils.JSON;
import Utils.SaveAndLoad;
import java.util.Arrays;

//@Deprecated
public class EvolutionTree extends SaveAndLoad.JSONSerialization<EvolutionTree>{
	/**Число ветвей у всего дерева*/
	private final AtomicInteger countRoot = new AtomicInteger(0);
	/**Все корневые узлы дерева эволюции*/
	private final List<Node> roots = new ArrayList<>(1);
	/**Список узлов, подлежащих удалению по окночании шага*/
	private Set<Node> removeNode = new java.util.concurrent.CopyOnWriteArraySet <>();
	
	/**Узел дерева эволюции */
	public class Node{
		/**Базовый цвет узла и потомков узла*/
		private static final Color DEFAULT_COLOR = new Color(255,255,255,50);
		
		/**Время, когда узел появился*/
		private final long time;
		//Поколение в этой ветке
		private long generation;
		//Потомки нашего узла
		List<Node> child = new java.util.concurrent.CopyOnWriteArrayList<>();
		/**Счётчик ветвей*/
		private final AtomicInteger branshCount;
		/**Число живых потомков*/
		private final AtomicInteger countAliveCell;
		/**Наш родитель, общий предок, если хотите*/
		private Node perrent;		
		/**Клетка, создавшая узел. В ней произошла мутация, отличившая её от родителя:*/
		private AliveCell founder;
		
		//====================СПЕЦ ПЕРЕМЕННЫЕ. Нужны для дерева эволюции
		//Показывает цвет узла
		private Color colorNode = Color.BLACK;
		
		private Node(long t, Node perrentNode, long gener) {
			this.branshCount = new AtomicInteger(0);
			this.countAliveCell = new AtomicInteger(0);
			time = t;
			perrent = perrentNode;
			generation = gener;
		}
		
		public Node(JSON node, Node aThis, long version) {
			this(node.getL("time"), aThis, node.getL("generation"));
			branshCount.set(node.get("branshCount"));
			if(version >= 4){
				founder = new AliveCell(node.getJ("founder"), null, version);
			} else {
				founder = new AliveCell();
			}
			for(var i : node.getAJ("Nodes")) {
				child.add(new Node(i,this,version));
			}
		}
		/**
		 * Создаёт новый узел в дереве эволюции в определённый момент времени
		 * @param cell - клетка, которая создала новый узел
		 * @param time - текущее время, когда произошла мутация
		 * @return новый узел
		 */
		public Node newNode(AliveCell cell,long time) {
			Node node = new Node(time, this, branshCount.incrementAndGet());
			node.countAliveCell.set(1);
			node.colorNode = colorNode;
			
			node.founder = cell;
			child.add(node);
			remove(); //Мы больше не служим нашему родителю!
			return node;
			//return this;
		}
		/**
		 * Не создаёт ветки эволюции, просто помечает, что существует ещё один потомок у этой ветки эволюции
		 * То есть клонирует текущий узел, говоря, что он у нас теперь имеет ещё одного живого потомка
		 * @return текущий узел, который знает, что у него новый потомок
		 */
		@Override
		public Node clone() {
			countAliveCell.incrementAndGet();
			return this;
		}
		/**Увы, очередной наш потомок того. Если потомков больше нет, то ветвь тупиковая - удаляем!*/
		public void remove(){
			if(countAliveCell.decrementAndGet() <= 0 && child.size() <= 1)
				removeNode.add(this);
		}
		/**
		 * Наш ребёнок сказал нам, что умер. Если это был последний наш ребёнок, то всё - сворачиваем лавочку
		 * @param node наш ребёнок
		 */
		private void remove(Node node) {
			child.remove(node);
			node.child = null;
			if(countAliveCell.get() == 0 && child.isEmpty()){
				if(perrent != null)
					getPerrent().remove(this);
			}else if(countAliveCell.get() == 0 && child.size() == 1)
				merge();
		}
		/**
		 * Мы - тупиковый узел. Нас больше нет.
		 * Поэтому наш потомок становится потомком нашего родителя
		 * А мы, как лохи, просто исчезаем
		 */
		private void merge() {
			if(child == null)
				return;
			Node newNodePerrent = child.get(0);
			if(getPerrent() != null) {
				getPerrent().getChild().add(newNodePerrent); 
				//Мы меняем ему родителя. Теперь это не мы, а наш родитель
				newNodePerrent.perrent = getPerrent();
				//У родителя и поколение другое предназаначено для этого дитятки
				newNodePerrent.generation = getGeneration();
				//Удаляем у родителя нас
				getPerrent().remove(this);
				//Тестовый код. Теперь он уже не нужен, вроде как
				/*if(getPerrent().child == null)
					return;
				HashMap<Long, Integer> map = new HashMap<>();
				for (Node node : getPerrent().child) {
					if(!map.containsKey(node.generation)) {
						map.put(node.generation, 0);
					} else {
						throw new RuntimeException("ЖОПА");
					}
				}*/
			}
		}
		
		@Override
		public String toString() {
			String ret = "Поколение " + getGeneration() + " мутация произошла в " + time + " от рожденства Адама. ";
			if(child != null)
				ret += "Мутация порадила " + getChild().size() + " потомков";
			return ret;
		}
		
		public JSON toJSON() {
			JSON make = new JSON();
			make.add("time", time);
			make.add("generation", getGeneration());
			make.add("branshCount", branshCount.get());
			make.add("countAliveCell", countAliveCell.get());
			make.add("branch", getBranch());
			make.add("founder",founder.toJSON());
			
			JSON[] nodes = new JSON[getChild().size()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = getChild().get(i).toJSON();
			}
			make.add("Nodes", nodes);
			return make;
		}
		public String getBranch() {
			if(getPerrent() != null)
				return getPerrent().getBranch() + ">" + getGeneration();
			else
				return "0";
		}
		/**Возвращает потомка под определеённым номером*/
		private Node getChild(long genCh) {
			for (Node node : child) {
				if(node.getGeneration() == genCh)
					return node;
			}
			throw new IllegalArgumentException("Узел " + genCh + " не найден в потомках узла " + this);
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
		public List<Node> getChild() {
			return child;
		}

		/**
		 * Возвращает все окончания ветви (у которых нет детей) для данного узла вниз
		 * @return
		 */
		public List<Node> getEndNode() {
			List<Node> ret = new ArrayList<>();
			if(child.isEmpty()) {
				ret.add(this);
			} else {
				for (Node node : child) {
					ret.addAll(node.getEndNode());
				}
			}
			return ret;
		}

		/**
		 * Сохраняет цвет всех детей узла
		 * @param c цвет
		 */
		public void setColor(Color c) {
			colorNode = c;
		}

		/**
		 * @return цвет узла
		 */
		public Color getColor() {
			return colorNode;
		}
		/**Сбрасывает цвет для узла и всех его потомков*/
		public void resetColor(){
			this.colorNode = DEFAULT_COLOR;
			for (var node : child)
				node.resetColor();
		}

		/**
		 * Возвращает количество живых потомков
		 * @return чило от 0 до бесконечности
		 */
		public int countAliveCell() {
			return countAliveCell.get();
		}

		/**
		 * @return the perrent
		 */
		public Node getPerrent() {
			return perrent;
		}
		/**Возвращает ту клетку, которая основаала наш узел*/
		public AliveCell getFounder(){
			return founder;
		}
		/**Возвращает время основания*/
		public long getTimeFounder(){
			return time;
		}
	}
	
	public EvolutionTree() {
		super(null,0);
	};
	
	public EvolutionTree(JSON json, long version) {
		this();
		if(version < 8){
			roots.add(new Node(json.getJ("Node"), null,version));
		} else {
			for(final var adam : json.getAJ("roots")){
				roots.add(new Node(adam, null,version));
			}
		}
	};
	/** Создаёт ещё одного родоначальника всего дерева.
	 * @return узел этого родоначальника
	 */
	public Node makeTree(){
		final var adam = new Node(0, null, countRoot.addAndGet(1));
		roots.add(adam);
		return adam;
	}

	@Override
	public String toString() {
		return Arrays.toString(roots.toArray(Node[]::new));
	}

	@Override
	public String getName() {
		return "EVOLUTION_TREE";
	}
	@Override
	public JSON getJSON() {
		JSON make = new JSON();
		make.add("roots", roots.stream().map(r -> r.toJSON()).toList());
		return make;
	}
	/**
	 * Функция, используемая при загрузке. Заодно помогает понять у какой ветви сколько потомков
	 * @param cell чей именно это узел. А то вдруг это - основатель!
	 * @param s полная строка эволюционного дерева, то есть 0>1>2...
	 * @return Конкретный узел дерева
	 */
	public Node getNode(AliveCell cell, String s) {
		Long[] numbers = Arrays.stream(s.split(">")).map(Long::valueOf).toArray(Long[]::new);
		if(numbers.length < 1){
			throw new IllegalArgumentException(Configurations.getProperty(EvolutionTree.class,"getNode.length",s));
		} else if(roots.size() < numbers[0]){
			throw new IllegalArgumentException(Configurations.getProperty(EvolutionTree.class,"getNode.no_root",numbers[0],s));
		}
		Node ret = roots.get(numbers[0].intValue());
		
		for (int i = 1; i < numbers.length; i++) {
			ret = ret.getChild(numbers[i]);
		}
		if(ret.founder.aliveStatus(CellObject.LV_STATUS.LV_ALIVE) && ret.founder.getPos().equals(cell.getPos()))
			ret.founder = cell;
		
		ret.countAliveCell.incrementAndGet(); //Подсчитали ещё одного живчика
		return ret;
	}
	/**Обновляет дерево эволюции - проверяет все узлы на их правильность и перерисовывает дерево*/
	public void updatre() {
		for(Node root : roots) {
			for(Node node : root.getEndNode()) {
				if(node.countAliveCell.get() <= 0)
					node.remove();
			}
		}
	}
	/**
	 * Перестраивает всё дерево эволюции для текущего шага.
	 * Удаляет нужные узлы, сливает остальные и т.д.
	 */
	public void step() {
		if(!removeNode.isEmpty()) {
			for(var node :removeNode) {
				var child = node.getChild();
				if(child == null)
					continue;
				else if (child.isEmpty()){// У нас нет детей, всё, удаляем у родителя
					if(node.getPerrent() == null) { //Ой. У нас и родителя нет... Упс
						//Симуляция закончена. Больше нет живых клеток
					} else {
						node.getPerrent().remove(node);
					}
				}else if(child.size() == 1) 
					node.merge();
			}
			removeNode.clear();
		}
	}
	/**Вставляет адама в эволюционное дерево
	 * @param adam клетка, которая образовала дерево
	 * @return узел, который теперь принадлежит этой клетке
	 */
	public Node setAdam(AliveCell adam) {
		final var root = makeTree();
		root.countAliveCell.set(1);
		root.founder = adam;
		return root;
	}
	/** Возращает размер корня дерева - сколько у него основателей
	 * @return 
	 */
	public int size(){return roots.size();}
	/** Возвращает корневой узел по индексу
	 * @param index номер корневого узла
	 * @return сам корневой узел
	 */
	public Node getRoot(int index){return roots.get(index);}
	/** Возвращает все корни дерева
	 * @return 
	 */
	public List<Node> getRoots(){return roots;}
}
