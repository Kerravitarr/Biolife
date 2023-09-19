package GUI;

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
import Calculations.Configurations;
import Calculations.Point;
import java.util.HashMap;
import java.util.Map;

public class Legend extends JPanel implements Configurations.EvrySecondTask{
	private class Graph extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setFont(Configurations.defaultFont);
			
			g.setColor(Color.BLACK);
			int width = getWidth()-_BORDER*2;
			g.drawLine(_BORDER, _HEIGHT-20, getWidth()-_BORDER, _HEIGHT-20);
			
			for(Value i : values) {
				g.setColor(i.color);
				g.fillRect(_BORDER+(int) ((i.x-i.width)*width), 0, (int) (i.width*width * 2), _HEIGHT-25);
				g.drawString(i.title, _BORDER+(int) (i.x*width) - i.title.length()*5/2, _HEIGHT-10);
			}
		}
	}

	/**Панель, на которой рисуются радиокнопки*/
	private final JPanel panel;
	/**Панель, на которой рисуются шкалы*/
	private final JPanel graph;
	/**Текущий режим работы легеды*/
	private MODE mode = MODE.DOING;
	/**Максимальный возраст объекта на экране*/
	private long maxAge = 0;
	/**Минимальное покаление объекта на экране*/
	private long minGenDef = 0;
	/**Максимальное покаление объекта на экране*/
	private long maxGenDef = 0;
	/**Максимальное количество жизней у существ на экране*/
	private long maxHP = 0;
	/**Максимальное количество минералов у существ на экране*/
	private long maxMP = 0;
	/**Если нужно обновить цвет объектов на экране*/
	private boolean updateSrin = false;
	/**Блоки, из которых состоит легеда - цвета и значения интервалов*/
	private Value[] values = new Value[0];
	/**Цветовой градиент для здоровья*/
	private final static ColorGradient HPColors = new ColorGradient(new Color(215,42,89,127),new Color(68,231,26,255), false);
	/**Цветовой градиент для минералов*/
	private final static ColorGradient MPColors = new ColorGradient(new Color(102,155,154,64),new Color(103,2,255,255), true);
	/**Цветовой градиент для возраста*/
	private final static ColorGradient AgeColors = new ColorGradient(Color.RED,Color.ORANGE, false);
	/**Цветовой градиент для покалений*/
	private final static ColorGradient GenerationColors = new ColorGradient(Color.RED,Color.ORANGE, false);
	/**Высота области легеды*/
	private final int _HEIGHT = 40;
	/**Размер краёв области легеды*/
	private final int _BORDER = 50;

	/**Режимы работы легенды*/
	public enum MODE {DOING,HP,YEAR,PHEN, GENER, MINERALS, POISON, EVO_TREE}
	/**Интервал значений и цвет значений для подписей внизу экрана*/
	private class Value{
		/**0-1, где находится значение*/
		double x ;
		/**0-1 его ширина*/
		double width;
		//Подпись
		String title;
		//Цвет занчения
		Color color;
		public Value(double right, double width, String title, Color color) {this.x=right - width/2;this.width=width/2;this.title=title;this.color=color;}
	}
	/**Класс, определяющий результирующий цвет на основе цветового круга и прогресса на этом кругу*/
	private static class ColorGradient{
		/**Список всех уже посчитанных цветов*/
		private Map<Integer, Color> colors = new HashMap<>();
		/**Создаёт круговой градиент
		 * @param from от какого цвета
		 * @param to к какому цвету
		 * @param isROYGBVR переход Красный-Оранжевый-Жёлтый-Зелёный-Голубой-Синий-Фиолетовый-Красный или обратный?
		 */
		public ColorGradient(Color from, Color to, boolean isROYGBVR){
			final float[] hsbfrom = new float[4];
			final float[] params = new float[4];
			Color.RGBtoHSB(from.getRed(), from.getGreen(), from.getBlue(), hsbfrom);
			hsbfrom[3] = from.getAlpha() / 255f;
			final var hsbto = new float[4];
			Color.RGBtoHSB(to.getRed(), to.getGreen(), to.getBlue(), hsbto);
			hsbto[3] = to.getAlpha() / 255f;
			if(hsbfrom[0] <= hsbto[0])
				params[0] = ((hsbto[0] - hsbfrom[0]) + (isROYGBVR ? 0 : -1)) / 100f;
			else
				params[0] = ((hsbto[0] - hsbfrom[0]) + (isROYGBVR ? 0 : +1)) / 100f;
			for (int i = 1; i < params.length; i++)
				params[i] = (hsbto[i] - hsbfrom[i]) / 100f;
			for (int i = 0; i < 151; i++) {
				colors.put(i, getHSBColor(hsbfrom[0] + params[0] * i, hsbfrom[1] + params[1] * i, hsbfrom[2] + params[2] * i, hsbfrom[3] + params[3] * i));
			}
		}
		/**Возвращает один из цветов прогресса
		 * @param progress
		 * @return 
		 */
		public Color cyrcleGradient(double progress){
			final var p = Utils.betwin(0,(int) Math.round(progress*100),151);
			return colors.get(p);
		}
		private Color getHSBColor(float h, float s, float b, float a){
			while(h > 1)h -= 1f;
			while(h < 0)h += 1f;
			s = Utils.betwin(0f, s, 1f);
			b = Utils.betwin(0f, b, 1f);
			a = Utils.betwin(0f, a, 1f);
			return Utils.getHSBColor(h,s,b,a);
		}
	}
	
		
	/**
	 * Create the panel.
	 */
	public Legend() {
		setName("Legend");
		setLayout(new BorderLayout(0, 0));		
		
		panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(panel, BorderLayout.NORTH);
		
		{
			var f = makeRB("Doing",MODE.DOING);
			f.setSelected(true);
			panel.add(f);
		}
		panel.add(makeRB("Hp",MODE.HP));
		panel.add(makeRB("Age",MODE.YEAR));
		panel.add(makeRB("Generation",MODE.GENER));
		panel.add(makeRB("Phenotype",MODE.PHEN));
		panel.add(makeRB("Mp",MODE.MINERALS));
		//panel.add(makeRB("Poison",Graph.MODE.POISON));
		panel.add(makeRB("EvoTree",MODE.EVO_TREE));
		

		graph = new Graph();
		add(graph, BorderLayout.CENTER);
		GroupLayout gl_Graph = new GroupLayout(graph);
		gl_Graph.setHorizontalGroup(
			gl_Graph.createParallelGroup(Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		gl_Graph.setVerticalGroup(
			gl_Graph.createParallelGroup(Alignment.LEADING)
				.addGap(0, _HEIGHT, Short.MAX_VALUE)
		);
		graph.setLayout(gl_Graph);
		add(graph);
		Configurations.addTask(this);
	}
	
	@Override
	public void taskStep(){
		if (!isVisible()) return;
		//Число ячеек с иписанием легенды
		final var countColumns = 10;
		
		double summHP_ = 0d;
		double maxHP_ = 0;
		long summMP_ = 0l;
		long maxMP_ = 0l;
		long maxAge_ = 0l;
		long maxGen_ = 0l;
		long minGen_ = Long.MAX_VALUE;

		for (int x = 0; x < Configurations.getWidth(); x++) {
			for (int y = 0; y < Configurations.getHeight(); y++) {
				CellObject cell = Configurations.world.get(Point.create(x, y));
				if (cell != null && cell instanceof AliveCell acell){
					summHP_ += acell.getHealth();
					maxHP_ = Math.max(maxHP_, acell.getHealth());
					summMP_ += acell.getMineral();
					maxMP_ = Math.max(maxMP_, acell.getMineral());
					maxAge_ = Math.max(maxAge_, acell.getAge());
					maxGen_ = Math.max(maxGen_, acell.getGeneration());
					minGen_ = Math.min(minGen_, acell.getGeneration());
				}
			}
		}
		maxHP = (long) maxHP_;
		maxMP = maxMP_;
		maxAge = (long) (maxAge_ * 1.4); //Увеличиваем на 40%, чтобы избавиться от фиолетового и розового в цветах и отдать их для стен и прочего
		maxGenDef = maxGen_;
		minGenDef = minGen_;
		
		switch (getMode()) {
			case DOING -> {
				values = new Value[AliveCell.ACTION.size()];
				for(int i = 0 ; i < values.length ; i++) {
					var act = AliveCell.ACTION.staticValues[i];
					values[i] = new Value((i + 1.0) / values.length, 1.0 / values.length, act.description, new Color(act.r,act.g,act.b));
				}
			}
			case PHEN ->  {
				values = new Value[AliveCellProtorype.Specialization.TYPE.size()];
				for(int i = 0 ; i < values.length ; i++) {
					var act = AliveCellProtorype.Specialization.TYPE.values[i];
					values[i] = new Value((i + 1.0) / values.length, 1.0 / values.length, act.toString(), Utils.getHSBColor(act.color, 1f, 1f, 1f));
				}
			}
			case HP -> {
				values = new Value[countColumns + 1];
				var w = 1.0 / values.length;
				for (int i = 0; i < values.length - 1; i++) {
					values[i] = new Value(1.0 * (i + 1) / values.length, w, (i * maxHP / countColumns) + "", HPColors.cyrcleGradient(((double) i) / countColumns));
				}
				values[values.length - 1]  = new Value(1.0, w, String.format("Σ=%s",Utils.degree((long)summHP_)),HPColors.cyrcleGradient(1d));
			}
			case MINERALS -> {
				values = new Value[countColumns + 1];
				var w = 1.0 / values.length;
				for (int i = 0; i < values.length - 1; i++) {
					values[i] = new Value(1.0 * (i + 1) / values.length, w, Integer.toString((int) (i * maxMP / countColumns)),MPColors.cyrcleGradient(((double) i) / countColumns));
				}
				values[values.length - 1]  = new Value(1.0, w, String.format("Σ=%s",Utils.degree((long)summMP_)),MPColors.cyrcleGradient(1));
			}
			case YEAR -> {
				values = new Value[countColumns];
				long mAge = 0;
				StringBuilder text = new StringBuilder(100);
				for (int i = 0; i < values.length; i++) {
					long nAge = (i+1) * maxAge_ / values.length;
					Utils.degree(text,mAge);
					text.append(" - ");
					Utils.degree(text,nAge);
					values[i] = new Value(1.0 * (i + 1) / values.length, 1.0 / values.length, text.toString(), AgeColors.cyrcleGradient(i / (values.length * 1.4f)));
					text.setLength(0);
					mAge = nAge;
				}
			}
			case GENER -> {
				var rdel = maxGenDef - minGenDef;
				long del = (long) (rdel * 1.4);//Увеличиваем на 40%, чтобы избавиться от фиолетового и розового в цветах
				maxGenDef = minGenDef + del;
				long mGen = minGenDef;
				StringBuilder text = new StringBuilder(100);
				values = new Value[countColumns];
				for (int i = 0; i < values.length; i++) {
					long nGen = minGenDef + (i+1) * rdel / values.length;
					if(i == 0){
						Utils.degree(text,minGenDef);
						text.append("+ [");
					}
					Utils.degree(text,mGen - minGenDef);
					text.append(" - ");
					Utils.degree(text,nGen - minGenDef);
					if(i+1 == values.length){
						text.append("]");
					}
					values[i] = new Value(1.0 * (i + 1) / values.length, 1.0 / values.length, text.toString(), GenerationColors.cyrcleGradient(i / (values.length * 1.4f)));
					text.setLength(0);
					mGen = nGen;
				}
			}
			case POISON -> {
				values = new Value[countColumns];
				for (int i = 0; i < values.length; i++) {
					var rg = (int) (255.0 * i / values.length);
					values[i] = new Value(1.0 * (i + 1) / values.length, 1.0 / values.length, (i * Poison.MAX_TOXIC / values.length) + "", new Color(rg, rg, rg));
				}
			}
			case EVO_TREE -> {
				values = new Value[0];
			}
		}
		if (updateSrin) {
			updateSrin = false;
		}
		repaint(1);
	}
	
	/**Активировать ту или иную кнопку
	 * @param e событие, в котором указано какая кнопка активированна
	 * @param doing какое теперь будет действие
	 */
	private void action(ActionEvent e, MODE doing) {
		for(var i : panel.getComponents()) {
			if(i instanceof JRadioButton rb) {
				rb.setSelected(i == e.getSource());
			}
		}
		mode = doing;
		updateSrin = !Configurations.world.isActiv();
	}
	/**Создаёт кнопку выбора режима работы легенды
	 * @param name имя кнопки, оно используется для подтягивания текста
	 * @param mode какой режим выбирается этой кнопкой
	 * @return переключатель
	 */
	private JRadioButton makeRB(String name, MODE mode) {
		JRadioButton jrbuton = new JRadioButton(Configurations.getHProperty(Legend.class,"Label" + name));
		jrbuton.setToolTipText(Configurations.getHProperty(Legend.class,"ToolTip" + name));
		jrbuton.addActionListener(e->action(e,mode));
		jrbuton.setFont(Configurations.defaultFont);
		jrbuton.setFocusable(false);
		return jrbuton;
	}
	
	

	/**
	 * @return the mode
	 */
	public MODE getMode() {
		return mode;
	}
	/**
	 * Превращает покаление клетки в конкретный цвет
	 * @param gen покаление
	 * @return Цвет
	 */
	public Color generationToColor(long gen) {
		return GenerationColors.cyrcleGradient(((double) (gen - minGenDef)) / (maxGenDef - minGenDef));
	}
	/**
	 * Превращает возраст клетки в конкретный цвет
	 * @param age возраст, в тиках
	 * @return Цвет
	 */
	public Color AgeToColor(long age) {
		return AgeToColor(((double)age)/maxAge);
	}
	/**
	 * Превращает возраст клетки в конкретный цвет
	 * @param age возраст, в процентах от максимального. [0;1]
	 * @return Цвет
	 */
	public Color AgeToColor(double age) {
		return AgeColors.cyrcleGradient(age);
	}
	/**Переводит очки здровья в цвет
	 * @param hp сколько очков здоровья
	 * @return цвет, соответствующий указанному числу
	 */
	public Color HPtToColor(double hp){
		if(maxHP != 0)
			return HPColors.cyrcleGradient(hp/maxHP);
		else
			return HPColors.cyrcleGradient(1);
	}
	/**Переводит очки минералов в цвет
	 * @param mp сколько минералов
	 * @return цвет, соответствующий указанному числу
	 */
	public Color MPtToColor(double mp){
		if(maxMP != 0)
			return MPColors.cyrcleGradient(mp/maxMP);
		else
			return MPColors.cyrcleGradient(1);
	}
	
}
