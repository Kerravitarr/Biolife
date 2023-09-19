/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;


import Calculations.Configurations;
import MapObjects.CellObject;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;

/**
 *
 * @author Илья
 */
public class MainFrame extends javax.swing.JFrame implements Configurations.EvrySecondTask{
	/**Где последний раз видели мышку*/
	private java.awt.Point mousePoint = null;
	/**Панелька с миром*/
	private JScrollPane scrollPane;
	/**Фабрика создания всплывающей подсказки*/
	private PopupFactory popupFactory;
	/**Окно подсказки*/
	private static Popup popup;
	/**Сама подсказка*/
	private JToolTip t;
	/**Состояние мира, для конечного автомата. Чем больше индекс, тем хуже стало миру*/
	private enum WORLD_STATUS {
		HAS_LIFE, HAS_OBJECT, EMPTY;
		public static WORLD_STATUS getStatus(){
			if(Configurations.world.getCount(CellObject.LV_STATUS.LV_ALIVE) > 0) return HAS_LIFE;
			final var countAll = Arrays.stream(CellObject.LV_STATUS.values).reduce(0d, (a,b) -> a + Configurations.world.getCount(b), Double::sum);
			return countAll > 0 ? HAS_OBJECT : EMPTY;
		}
	}
	/**Текущий статус мира*/
	private WORLD_STATUS nowStatus = WORLD_STATUS.EMPTY;
	
	
	/**Поток, который автоматически стартует при создании*/
	class AutostartThread extends Thread{private AutostartThread(Runnable target){super(target);start();} private static void start(Runnable target){new Thread(target).start();}}
	/**Слушатель событий мыши*/
	private class MouseMoveAdapter extends MouseAdapter{
		private Point origin;

		@Override
		public void mousePressed(MouseEvent e) {
			origin = new Point(e.getPoint());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (Configurations.getViewer() instanceof DefaultViewer dv) {
				if (origin != null && !dv.getMenu().isSelectedCell()) {
					final var viewport = scrollPane.getViewport();
					int deltaX = origin.x - e.getX();
					int deltaY = origin.y - e.getY();

					Rectangle view = viewport.getViewRect();
					view.x += deltaX;
					view.y += deltaY;

					dv.getWorld().scrollRectToVisible(view);
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e){mousePoint = e.getPoint();}
		@Override
		public void mouseExited(MouseEvent e) { mousePoint = null;}
		

	}

	/** Creates new form MainFrame */
	public MainFrame() {
		initComponents();
		
		Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
		//8 - это потому что 5/8 это примерно 60% экрана. 
		//А 4.5 - потому что именно при соотношении в 5 раз начинается детальная отрисовка клеток
		//а это нехило так роняет ФПС
		if(Configurations.PIXEL_PER_CELL > 8)
			setBounds(100, 100, (int) (sSize.getWidth() * 4.5 / Configurations.PIXEL_PER_CELL), (int)((sSize.getHeight()) * 4.5 / Configurations.PIXEL_PER_CELL));
		else
			setBounds(100, 100, (int) (sSize.getWidth() / 2), (int)((sSize.getHeight()) / 2));
		
		Configurations.setViewer(new DefaultViewer());
		contentPane.add(makePanel(Configurations.getViewer().get("Settings"), BorderLayout.EAST), BorderLayout.EAST);
		contentPane.add(makePanel(Configurations.getViewer().get("Legend"), BorderLayout.SOUTH), BorderLayout.SOUTH);
		contentPane.add(makePanel(Configurations.getViewer().get("BotInfo"),BorderLayout.WEST), BorderLayout.WEST);
		contentPane.add(makeWorldPanel(Configurations.getViewer().get(WorldView.class)), BorderLayout.CENTER);
		contentPane.add(makePanel(Configurations.getViewer().get("Menu"), BorderLayout.NORTH), BorderLayout.NORTH);
		
		t = ((WorldView) Configurations.getViewer().get("World")).createToolTip();
		t.setTipText(Configurations.getProperty(MainFrame.class,"autosave"));
		popupFactory = PopupFactory.getSharedInstance();
		
		Configurations.addTask(this);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPane = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        contentPane.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                contentPaneMouseWheelMoved(evt);
            }
        });
        contentPane.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		if (Configurations.getViewer() instanceof DefaultViewer dv) {
			dv.getMenu().save();
		}
    }//GEN-LAST:event_formWindowClosing

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        switch (evt.getKeyCode()) {
			case KeyEvent.VK_SPACE -> {
				if (Configurations.world.isActiv())
					Configurations.world.stop();
				else
					Configurations.world.start();
			}
			case KeyEvent.VK_S -> {
				if ((evt.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0){
					if (Configurations.getViewer() instanceof DefaultViewer dv) {
						dv.getMenu().save();
					}
				}else{
					AutostartThread.start(() -> Configurations.world.step());
				}
			}
			case KeyEvent.VK_W ->
				AutostartThread.start(() -> {
					if (Configurations.getViewer() instanceof DefaultViewer dv) {
						CellObject cell = dv.getBotInfo().getCell();
						if (cell != null)
							cell.step(cell.getStepCount() - 1);
					}
				});
			case KeyEvent.VK_E ->
				AutostartThread.start(() -> {
					if (Configurations.getViewer() instanceof DefaultViewer dv) {
						final var bi = dv.getBotInfo();
						CellObject cell = bi.getCell();
						if (cell != null)
							bi.step();
					}
				});
		}
    }//GEN-LAST:event_formKeyPressed

    private void contentPaneMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_contentPaneMouseWheelMoved
		if (Configurations.getViewer() instanceof DefaultViewer dv) {
			if (evt.isControlDown()) {
				dv.getWorld().addZoom(-evt.getWheelRotation() * 10);
				scrollPane.dispatchEvent(new ComponentEvent(scrollPane, ComponentEvent.COMPONENT_RESIZED));
			}
		}
    }//GEN-LAST:event_contentPaneMouseWheelMoved

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
       
    }//GEN-LAST:event_formComponentResized

	
	@Override
	public void taskStep() {
		final var world = Configurations.world;
		final var v = Configurations.getViewer();
		if(!(v instanceof DefaultViewer)) return;
		final var wv = v.get(WorldView.class);
		final var settings = v.get(Settings.class);
		final var legend = v.get(Legend.class);
		final var menu = v.get(Menu.class);
		final var bi = v.get(BotInfo.class);
		
		String title = MessageFormat.format(Configurations.getProperty(MainFrame.class,"title"), wv.fps.FPS(), world.step,
				world.pps.FPS(), world.getCount(CellObject.LV_STATUS.LV_ALIVE), world.getCount(CellObject.LV_STATUS.LV_ORGANIC),
				world.getCount(CellObject.LV_STATUS.LV_POISON), world.getCount(CellObject.LV_STATUS.LV_WALL), world.isActiv() ? ">" : "||");
		setTitle(title);
		wv.repaint();
		//Действия, только при моделировании
		if(world.isActiv()){
			//Вывод сообщения, если мир опустеет
			final var status = WORLD_STATUS.getStatus();
			if(nowStatus.ordinal() < status.ordinal()){
				java.awt.Toolkit.getDefaultToolkit().beep();
				Configurations.world.awaitStop();
				JOptionPane.showMessageDialog(null, Configurations.getProperty(MainFrame.class,status.name()), "BioLife", JOptionPane.WARNING_MESSAGE);
			}
			nowStatus = status;
		
			//Автосохранение
			if(nowStatus == WORLD_STATUS.HAS_LIFE && Math.abs(world.step - Configurations.confoguration.lastSaveCount) > Configurations.confoguration.SAVE_PERIOD){
				//Если мир пассивный - то с чего мы вдруг решили его начать сохранять? Может он только загружен?
				final var loc = scrollPane.getLocationOnScreen();
				if(popup != null)
					popup.hide();
				popup = popupFactory.getPopup(wv, t,loc.x + scrollPane.getWidth() / 2, loc.y + scrollPane.getHeight() / 2);
				popup.show();

				var list = new File[Configurations.confoguration.COUNT_SAVE];
				for(var i = 0 ; i < Configurations.confoguration.COUNT_SAVE ; i++){
					list[i] = new File("autosave" + (i+1) + ".zbmap");
				}
				var save = list[0];
				for(var i = 1 ; i < Configurations.confoguration.COUNT_SAVE && save.exists(); i++){
					if(!list[i].exists() || save.lastModified() > list[i].lastModified())
						save = list[i];
				}
				try {
					Configurations.save(save.getName());
				} catch (IOException ex) {
					Logger.getLogger(this.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					JOptionPane.showMessageDialog(null,	"Ошибка сохранения!\n" + ex.getMessage(), "BioLife", JOptionPane.ERROR_MESSAGE);
				}
				if(popup != null){
					popup.hide();
					popup = null;
				}
			}
		}
		
		//А теперь изменяем форму нашего окна, в зависимости от настроек
		legend.getParent().setVisible(!settings.isEdit());
		bi.getParent().setVisible(!settings.isEdit());
		menu.getParent().setVisible(!settings.isEdit());
	}
	
	/**Распределяет панели по экрану
	 * @param panel панель, которую нужно воткнуть 
	 * @param name её название
	 * @param borderLayoutConst куда она втыкаться будет
	 * @return готовая для втыкания панель со всеми настройками
	 */
	private Component makePanel(JPanel panel, String borderLayoutConst) {
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
			public void mouseClicked(MouseEvent e) {mouseClicked_panel(panel,label,isActive=!isActive, borderLayoutConst);}
		});
		panel_2.add(panel, BorderLayout.CENTER);
		switch (borderLayoutConst) {
			case BorderLayout.EAST -> panel_2.add(label, BorderLayout.WEST);
			case BorderLayout.WEST -> panel_2.add(label, BorderLayout.EAST);
			case BorderLayout.SOUTH -> panel_2.add(label, BorderLayout.NORTH);
			case BorderLayout.NORTH -> panel_2.add(label, BorderLayout.SOUTH);
			default ->	throw new IllegalArgumentException("Unexpected value: " + borderLayoutConst);
		}
		mouseClicked_panel(panel,label,false, borderLayoutConst);
		return panel_2;
	}
	
	
	/**Так как мир несколько особенный, то тут создаётся центральная панель, на которой будет рисоваться мир
	 * @return панель мира
	 */
	private Component makeWorldPanel(WorldView world) {
		scrollPane = new JScrollPane(world){
		    @Override
		    protected void processMouseWheelEvent(MouseWheelEvent e) {
		    	contentPaneMouseWheelMoved(e);
		        super.processMouseWheelEvent(e);
		    }
		};
		final var viewport = scrollPane.getViewport();
		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				worldResized();
			}
		});
		
		viewport.addChangeListener(e -> EventQueue.invokeLater(() ->{
			if(world.getZoom() > 1){
				final var transform = world.getTransform();
				var horizont = scrollPane.getHorizontalScrollBar();
				var vertical = scrollPane.getVerticalScrollBar();
				var xMin = Math.max(0, transform.toWorldX(horizont.getValue()));
				var xMax = Math.min(Configurations.getWidth() - 1,transform.toWorldX(horizont.getValue() + viewport.getSize().width));
				var yMin = Math.max(0, transform.toWorldY(vertical.getValue()));
				var yMax = Math.min(Configurations.getHeight() - 1,transform.toWorldY(vertical.getValue() + viewport.getSize().height));
				world.setVisible(Calculations.Point.create(xMin, yMin), Calculations.Point.create(xMax, yMax));
			} else {
				world.setVisible(Calculations.Point.create(0, 0), Calculations.Point.create(Configurations.getWidth() - 1, Configurations.getHeight() - 1));
			}
		}));
		var adapter = new MouseMoveAdapter();
		world.addMouseListener(adapter);
		world.addMouseMotionListener(adapter);
		return scrollPane;
	}
	/**Функция для пересчёта размеров мира*/
	private void worldResized() {
		final var viewport = scrollPane.getViewport();
		final var world = Configurations.getViewer().get(WorldView.class);
		
		var scale = (Math.pow(5,(world.getZoom()-1) / 100d));
		double requiredW = scrollPane.getWidth() * scale - 20;
		double requiredH = scrollPane.getHeight() * scale - 20;
		var horizont = scrollPane.getHorizontalScrollBar();
		var vertical = scrollPane.getVerticalScrollBar(); 

		switch (Configurations.confoguration.world_type) {
			case LINE_H -> {
				requiredH = (1.0 + world.getUborder() + world.getDborder()) * requiredW * Configurations.getHeight() / Configurations.getWidth();
			} case LINE_V -> {
				requiredW = (1.0 + world.getLborder() + world.getRborder()) * requiredH * Configurations.getWidth() / Configurations.getHeight();
			} case RECTANGLE -> {}
			default -> throw new AssertionError();
		}
		horizont.setUnitIncrement((int)Math.max(1, requiredW / 100));
		vertical.setUnitIncrement((int)Math.max(1, requiredH / 100));

		var lastP = mousePoint;
		if(lastP == null){
			lastP = viewport.getViewPosition();
			lastP.x += viewport.getWidth()/2;
			lastP.y += viewport.getHeight()/2;
		}
		var Zw = (requiredW) / world.getWidth();
		var Zh = (requiredH) / world.getHeight();

		world.setPreferredSize(new Dimension((int)requiredW,(int)requiredH));

		int newX = (int) ((lastP.x *= Zw) - viewport.getWidth()/2);
		int newY = (int) ((lastP.y *= Zh) - viewport.getHeight()/2);
		EventQueue.invokeLater(() -> viewport.setViewPosition(new java.awt.Point(Math.max(0, newX), Math.max(0,newY))));
	}
	
	
	/**Превращает текст в вертикальный
	 * @param text текст, который надо преобразовать
	 * @return текст, записанный на языке html который уже будет отображён как вертикальный
	 */
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
	
	/**Клик на панели с целью её выдвинуть
	 * @param panel панель, которую двигаем
	 * @param panel_Label объект текста панели
	 * @param name название панели
	 * @param isActive выдвинули панель или задвинули?
	 * @param borderLayoutConst какое местоположение панели?
	 */
	private void mouseClicked_panel(JPanel panel ,JLabel panel_Label, boolean isActive, String borderLayoutConst) {
		String symbol =  "" + switch (borderLayoutConst) {
			case BorderLayout.EAST -> isActive ? "&GT;" : "&lt;";
			case BorderLayout.WEST -> !isActive ? "&GT;" : "&lt;";
			case BorderLayout.SOUTH -> isActive ? "  \\/" : "  /\\";
			case BorderLayout.NORTH -> !isActive ? "  \\/" : "  /\\";
			default ->	throw new IllegalArgumentException("Unexpected value: " + borderLayoutConst);
		};
		panel.setVisible(isActive);
		switch (borderLayoutConst) {
			case BorderLayout.EAST , BorderLayout.WEST -> panel_Label.setText(toVerticalText(Configurations.getProperty(panel.getClass(), "name")) + symbol);
			case BorderLayout.SOUTH, BorderLayout.NORTH -> panel_Label.setText(Configurations.getProperty(panel.getClass(), "name") + symbol);
		}
		
		if (!isActive) {
			toFront();
			requestFocus();
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPane;
    // End of variables declaration//GEN-END:variables
}
