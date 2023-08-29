package start;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import MapObjects.CellObject;
import java.io.File;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.UIManager;
import main.Configurations;
import main.World;
import panels.BotInfo;
import panels.EvolTreeDialog;
import panels.Legend;
import panels.Menu;
import panels.Settings;

public class BioLife extends JFrame {
	private class UpdateScrinTask implements Runnable {
		/**Предыдущий шаг сохранения*/
		private long lastSave = 0;
		/**Период сохранения, шаги эволюции*/
		private long savePeriod = 100_000;
		/**Сколько файлов автосохранения держать*/
		private final int COUNT_SAVE = 3;
		
		/**Фабрика создания всплывающей подсказки*/
		private PopupFactory popupFactory;
		/**Окно подсказки*/
		private static Popup popup;
		/**Сама подсказка*/
		private JToolTip t;
		
		public UpdateScrinTask(){
			t = Configurations.world.createToolTip();
			t.setTipText(Configurations.getProperty(BioLife.class,"autosave"));
			popupFactory = PopupFactory.getSharedInstance();
		}

		@Override
		public void run() {try{runE();}catch(Exception ex){System.err.println(ex);ex.printStackTrace(System.err);}}
		
		public void runE() {
			final var world = Configurations.world;
			String title = MessageFormat.format(Configurations.getProperty(BioLife.class,"title"), world.fps.FPS(), world.step,
					world.sps.FPS(), world.countLife, world.countOrganic, world.countPoison, world.countWall, world.isActiv() ? ">" : "||");
			setTitle(title);
			if (dialog.isVisible())
				dialog.repaint();
			world.repaint();
			
			//Автосохранение
			if(Math.abs(world.step - lastSave) > savePeriod){
				if(world.isActiv()){
					//Если мир пассивный - то с чего мы вдруг решили его начать сохранять? Может он только загружен?
					final var loc = scrollPane.getLocationOnScreen();
					if(popup != null)
						popup.hide();
					popup = popupFactory.getPopup(Configurations.world, t,loc.x + scrollPane.getWidth() / 2, loc.y + scrollPane.getHeight() / 2);
					popup.show();

					var list = new File[COUNT_SAVE];
					for(var i = 0 ; i < COUNT_SAVE ; i++){
						list[i] = new File("autosave" + (i+1) + ".zbmap");
					}
					var save = list[0];
					for(var i = 1 ; i < COUNT_SAVE && save.exists(); i++){
						if(!list[i].exists() || save.lastModified() > list[i].lastModified())
							save = list[i];
					}
					Configurations.settings.save(save.getName());
					if(popup != null){
						popup.hide();
						popup = null;
					}
				}
				lastSave = world.step;
			}
		}
	}
	
	private class MouseMoveAdapter extends MouseAdapter{
		private Point origin;

		@Override
		public void mousePressed(MouseEvent e) {
			origin = new Point(e.getPoint());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (origin != null && !Configurations.menu.isSelectedCell()) {
				final var viewport = scrollPane.getViewport();
				int deltaX = origin.x - e.getX();
				int deltaY = origin.y - e.getY();

				Rectangle view = viewport.getViewRect();
				view.x += deltaX;
				view.y += deltaY;

				Configurations.world.scrollRectToVisible(view);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e){mousePoint = e.getPoint();}
		@Override
		public void mouseExited(MouseEvent e) { mousePoint = null;}
		

	}
	/**Центральная панель, на которой всё и происходит*/
	private final JPanel contentPane;
	/**Где последний раз видели мышку*/
	private java.awt.Point mousePoint = null;
	/**Легенда карты*/
	private final Legend legend;
	/**Панелька с миром*/
	private JScrollPane scrollPane;
	/**Дерево эволюции*/
	private final EvolTreeDialog dialog = new EvolTreeDialog();
	
	private JMenuItem startRecord;
	/**Размер карты высчитывается на основе размера экрана. А эта переменная определяет, сколько пикселей будет каждая клетка*/
	private	static final double PIXEL_PER_CELL = 10;

	/**Точка входа в приложение
	 * @param args аргументы командной строки
	 */
	public static void main(String[] args) {
		final var _opts = new Utils.CMDOptions(args);
		_opts.add(new Utils.CMDOptions.Option('h',"Печать справки по драйверу"));
		//Обработка опций
		var print_help = _opts.get('h').get(Boolean.class) ? (false ? 1 : 2) : 0;
		boolean isNeedHelp = print_help != 0;
		if (isNeedHelp) {
			if(print_help == 1)
				System.out.println("Параметры командной строки, установленные:");
			else
				System.out.println("Параметры командной строки, по умолчанию:");
			String opts_str = "";
			for (var it = _opts.iterator(); it.hasNext();) {
				Utils.CMDOptions.Option i = it.next();
				if(i.get(Utils.CMDOptions.Option.state.class) != Utils.CMDOptions.Option.state.remove)
					opts_str += "\n" + i.print(75,!(print_help == 1));
			}
			System.out.println(opts_str);
			return;
		} else {
			String optsStr = "";
			for(var i : _opts.getCmdParams().entrySet())
				optsStr += String.format("-%s%s ",i.getKey(), i.getValue());
			System.out.println("Опции находятся в допустимых пределах. Параметры запуска: " + optsStr);
		}
		//Обработка переменных окружения
		//Настраиваем буковки
		Configurations.defaultFont = new java.awt.Font("Default", Font.BOLD, 12);
		Configurations.smalFont = Configurations.defaultFont.deriveFont(10f);
		//Создаём мир
		Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
		Configurations.makeWorld((int) (sSize.getWidth() / PIXEL_PER_CELL), (int) ((sSize.getHeight() - 120 ) / PIXEL_PER_CELL)); //120 - пикселей на верхнюю и нижнюю шапочки
		Configurations.settings = new Settings();
		Configurations.info = new BotInfo();
		Configurations.world = new World();
		Configurations.menu = new Menu();
				
		setUIFont(new javax.swing.plaf.FontUIResource(Configurations.defaultFont));
		
		//Запускаем приложение!
		EventQueue.invokeLater(() -> {try {new BioLife().setVisible(true);} catch (Exception e) {e.printStackTrace();}});		
	}
	/**Сохраняет шрифты по умолчанию для всего приложения
	 * @param f 
	 */
	public static void setUIFont(javax.swing.plaf.FontUIResource f) {
		java.util.Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, f);
			}
		}
	}

	/**
	 * Create the frame.
	 */
	public BioLife() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
		//8 - это потому что 5/8 это примерно 60% экрана. 
		//А 4.5 - потому что именно при соотношении в 5 раз начинается детальная отрисовка клеток
		//а это нехило так роняет ФПС
		if(PIXEL_PER_CELL > 8)
			setBounds(100, 100, (int) (sSize.getWidth() * 4.5 / PIXEL_PER_CELL), (int)((sSize.getHeight()) * 4.5 / PIXEL_PER_CELL));
		else
			setBounds(100, 100, (int) (sSize.getWidth() / 2), (int)((sSize.getHeight()) / 2));
		
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 2));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		contentPane.add(makePanel(Configurations.settings, "Settings", BorderLayout.EAST), BorderLayout.EAST);
		legend = new Legend();
		contentPane.add(makePanel(legend, "Legend", BorderLayout.SOUTH), BorderLayout.SOUTH);
		contentPane.add(makePanel(Configurations.info,"BotInfo", BorderLayout.WEST), BorderLayout.WEST);
		contentPane.add(makeWorldPanel(), BorderLayout.CENTER);
		contentPane.add(makePanel(Configurations.menu, "Menu", BorderLayout.NORTH), BorderLayout.NORTH);
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {BioLife.this.keyPressed(e);}
		});
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Configurations.settings.save();
			}
		});
		
		Configurations.TIME_OUT_POOL.scheduleWithFixedDelay(new UpdateScrinTask(), 1, 1, TimeUnit.SECONDS);
	}

	/**Так как мир несколько особенный, то тут создаётся центральная панель, на которой будет рисоваться мир
	 * @return панель мира
	 */
	private Component makeWorldPanel() {
		scrollPane = new JScrollPane(){
		    @Override
		    protected void processMouseWheelEvent(MouseWheelEvent e) {
		    	mouseWheel(e);
		        super.processMouseWheelEvent(e);
		    }
		};
		final var viewport = scrollPane.getViewport();
		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				
				var scale = (Math.pow(5,(Configurations.settings.getScale()-1) / 100d));
				var newW = (int) (scrollPane.getWidth() * scale) - 10;
				var newH = (int) (scrollPane.getHeight() * scale) - 10;
				var horizont = scrollPane.getHorizontalScrollBar();
				var vertical = scrollPane.getVerticalScrollBar(); 
				
				if(scale > 1) {
					newH = (int) ((newW * (1 + (Configurations.UP_border + Configurations.DOWN_border)) * Configurations.MAP_CELLS.height) / Configurations.MAP_CELLS.width);
					
					horizont.setUnitIncrement(Math.max(1, newW / 100));
					vertical.setUnitIncrement(Math.max(1, newH / 100));
				}
				
				var lastP = mousePoint;
				if(lastP == null){
					lastP = viewport.getViewPosition();
					lastP.x += viewport.getWidth()/2;
					lastP.y += viewport.getHeight()/2;
				}
				var Zw = ((double) newW) / Configurations.world.getWidth();
				var Zh = ((double) newH) / Configurations.world.getHeight();
				
				Configurations.world.setPreferredSize(new Dimension(newW,newH));

				int newX = (int) ((lastP.x *= Zw) - viewport.getWidth()/2);
				int newY = (int) ((lastP.y *= Zh) - viewport.getHeight()/2);
				EventQueue.invokeLater(() -> viewport.setViewPosition(new Point(Math.max(0, newX), Math.max(0,newY))));
			}
		});
		
		viewport.addChangeListener(e -> EventQueue.invokeLater(() ->{
			if(Configurations.settings.getScale() > 1){
				var horizont = scrollPane.getHorizontalScrollBar();
				var vertical = scrollPane.getVerticalScrollBar();
				var xMin = Math.max(0, main.Point.rxToX(horizont.getValue()));
				var xMax = Math.min(Configurations.MAP_CELLS.width - 1,main.Point.rxToX(horizont.getValue() + viewport.getSize().width));
				var yMin = Math.max(0, main.Point.ryToY(vertical.getValue()));
				var yMax = Math.min(Configurations.MAP_CELLS.height - 1,main.Point.ryToY(vertical.getValue() + viewport.getSize().height));
				Configurations.world.setVisible(new main.Point(xMin, yMin),new main.Point(xMax, yMax));
			} else {
				Configurations.world.setVisible(new main.Point(0, 0),new main.Point(Configurations.MAP_CELLS.width - 1, Configurations.MAP_CELLS.height - 1));
			}
		}));
		Configurations.settings.setListener(scrollPane);
		scrollPane.setViewportView(Configurations.world);
		var adapter = new MouseMoveAdapter();
		Configurations.world.addMouseListener(adapter);
		Configurations.world.addMouseMotionListener(adapter);
		return scrollPane;
	}
	
	private Component makePanel(JPanel panel, String name, String borderLayoutConst) {
		JPanel panel_2 = new JPanel();
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel label = new JLabel();
		label.setFont(Configurations.smalFont);
		switch (borderLayoutConst) {
			case BorderLayout.EAST -> label.setHorizontalAlignment(SwingConstants.RIGHT);
			case BorderLayout.WEST -> label.setHorizontalAlignment(SwingConstants.LEFT);
			default -> label.setHorizontalAlignment(SwingConstants.CENTER);
		}
		label.addMouseListener(new MouseAdapter() {
			boolean isActive = false;
			@Override
			public void mouseClicked(MouseEvent e) {mouseClicked_panel(panel,label,name,isActive=!isActive, borderLayoutConst);}
		});
		panel_2.add(panel, BorderLayout.CENTER);
		switch (borderLayoutConst) {
			case BorderLayout.EAST -> panel_2.add(label, BorderLayout.WEST);
			case BorderLayout.WEST -> panel_2.add(label, BorderLayout.EAST);
			case BorderLayout.SOUTH -> panel_2.add(label, BorderLayout.NORTH);
			case BorderLayout.NORTH -> panel_2.add(label, BorderLayout.SOUTH);
			default ->	throw new IllegalArgumentException("Unexpected value: " + borderLayoutConst);
		};
		mouseClicked_panel(panel,label,name,false, borderLayoutConst);
		return panel_2;
	}
	
	private String toVerticalText(String text) {
		StringBuilder sb = new StringBuilder(20 + text.length() * 5);
		sb.append("<html>");
		for(var i = 0 ; i < text.length() ; i++) {
			sb.append(text.charAt(i));
			sb.append("<br>");
		}
		//И ещё небольшой отпуск снизу
		sb.append("<br>");
		return sb.toString();
	}

	private void mouseClicked_panel(JPanel panel ,JLabel panel_Label, String name, boolean isActive, String borderLayoutConst) {
		String symbol =  "" + switch (borderLayoutConst) {
			case BorderLayout.EAST -> isActive ? "&GT;" : "&lt;";
			case BorderLayout.WEST -> !isActive ? "&GT;" : "&lt;";
			case BorderLayout.SOUTH -> isActive ? "  \\/" : "  /\\";
			case BorderLayout.NORTH -> !isActive ? "  \\/" : "  /\\";
			default ->	throw new IllegalArgumentException("Unexpected value: " + borderLayoutConst);
		};
		panel.setVisible(isActive);
		switch (borderLayoutConst) {
			case BorderLayout.EAST , BorderLayout.WEST -> panel_Label.setText(toVerticalText(Configurations.getProperty(BioLife.class, name)) + symbol);
			case BorderLayout.SOUTH, BorderLayout.NORTH -> panel_Label.setText(Configurations.getProperty(BioLife.class, name) + symbol);
		};
		
		if (!isActive) {
			BioLife.this.toFront();
			BioLife.this.requestFocus();
		}
	}
	private void mouseWheel(MouseWheelEvent e) {
		if (e.isControlDown()) {
			Configurations.settings.addScale(-e.getWheelRotation() * 10);
        }
	}
	private void keyPressed(KeyEvent e) {
		//System.out.println(e);
       		switch (e.getKeyCode()) {
			case KeyEvent.VK_SPACE -> {
				if (Configurations.world.isActiv())
					Configurations.world.stop();
				else
					Configurations.world.start();
			}
			case KeyEvent.VK_S -> {
				if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
					Configurations.settings.save();
				else
					Configurations.TIME_OUT_POOL.execute(() -> Configurations.world.step());
			}
			case KeyEvent.VK_W ->
				Configurations.TIME_OUT_POOL.execute(() -> {
					CellObject cell = Configurations.info.getCell();
					if (cell != null)
						new Thread(() -> cell.step(Math.round(Math.random() * 1000))).start();
				});
			case KeyEvent.VK_E ->
				Configurations.TIME_OUT_POOL.execute(() -> {
					CellObject cell = Configurations.info.getCell();
					if (cell != null)
						Configurations.info.step();
				});
		}
	}
}
