package start;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.HeadlessException;
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
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import MapObjects.CellObject;
import Utils.GifSequenceWriter;
import main.Configurations;
import main.World;
import panels.BotInfo;
import panels.EvolTreeDialog;
import panels.Legend;
import panels.Menu;
import panels.Settings;

public class BioLife extends JFrame {
	private class UpdateScrinTask implements Runnable {

		@Override
		public void run() {
			String title = MessageFormat.format(Configurations.bundle.getString("BioLife.title"), world.fps.FPS(), world.step,
					world.sps.FPS(), world.countLife, world.countOrganic, world.countPoison, world.countWall);
			setTitle(title);
			if (dialog.isVisible())
				dialog.repaint();
			world.repaint();
			if (gifs != null) {
				try {
					gifs.nextFrame(g -> Configurations.world.paintComponent(g));
				} catch (IOException e) {
					World.isActiv = false;
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "<html>Запись видео оборвалась по причине<br>"
							+ e.getMessage(), "BioLife", JOptionPane.ERROR_MESSAGE);
					gifs = null;
					startRecord.setText("Начать запись");
				}
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
			if (origin != null) {
				JViewport viewPort = (JViewport) javax.swing.SwingUtilities.getAncestorOfClass(JViewport.class, world);
				if (viewPort != null) {
					int deltaX = origin.x - e.getX();
					int deltaY = origin.y - e.getY();

					Rectangle view = viewPort.getViewRect();
					view.x += deltaX;
					view.y += deltaY;

					world.scrollRectToVisible(view);
				}
			}
		}

	}
	/**Центральная панель, на которой всё и происходит*/
	private final JPanel contentPane;
	
	private final Menu menu;
	private final BotInfo botInfo;
	private final Settings settings;
	private final World world;
	private final Legend legend;
	
	private JScrollPane scrollPane;
	private EvolTreeDialog dialog = new EvolTreeDialog();
	private GifSequenceWriter gifs = null;
	private JMenuItem startRecord;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				BioLife frame = new BioLife();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BioLife() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, (int) (450*2.5), (int) (300*2.5));
		
		Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
		Configurations.makeWorld((int) sSize.getWidth() / 5, (int) (sSize.getHeight() - 120 ) / 5); //120 - пикселей на верхнюю и нижнюю шапочки
		
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		settings = new Settings();
		contentPane.add(makePanel(settings, "Settings", BorderLayout.EAST), BorderLayout.EAST);
		legend = new Legend();
		contentPane.add(makePanel(legend, "Legend", BorderLayout.SOUTH), BorderLayout.SOUTH);
		botInfo = new BotInfo();
		contentPane.add(makePanel(botInfo,"BotInfo", BorderLayout.WEST), BorderLayout.WEST);
		world = new World();
		contentPane.add(makeWorldPanel(), BorderLayout.CENTER);
		menu = new Menu();
		contentPane.add(makePanel(menu, "Menu", BorderLayout.NORTH), BorderLayout.NORTH);
		//setJMenuBar(makeMenu());
		
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

	private Component makeWorldPanel() {
		scrollPane = new JScrollPane(){
		    @Override
		    protected void processMouseWheelEvent(MouseWheelEvent e) {
		    	mouseWheel(e);
		        super.processMouseWheelEvent(e);
		    }

		};
		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int newW = (int) (scrollPane.getWidth() * (1 + (settings.getScale() / 100d))) - 10;
				int newH = (int) (scrollPane.getHeight() * (1 + (settings.getScale() / 100d))) - 10;
				var horizont = scrollPane.getHorizontalScrollBar();
				var vertical = scrollPane.getVerticalScrollBar();
				
				if(settings.getScale() > 0) {
					newH = (int) ((newW * (1 + (Configurations.UP_border + Configurations.DOWN_border)) * Configurations.MAP_CELLS.height) / Configurations.MAP_CELLS.width);
					
					horizont.setUnitIncrement(Math.max(1, newW / 100));
					vertical.setUnitIncrement(Math.max(1, newH / 100));
				}
				
				var wOffset = ((double) newW) / world.getWidth();
				var hOffset = ((double) newH) / world.getHeight();
				
				world.setPreferredSize(new Dimension(newW,newH));
				
				horizont.setValue((int) (horizont.getValue() * wOffset));
				vertical.setValue((int) (vertical.getValue() * hOffset));
			}
		});
		settings.setListener(scrollPane);
		scrollPane.setViewportView(world);
		var adapter = new MouseMoveAdapter();
		world.addMouseListener(adapter);
		world.addMouseMotionListener(adapter);
		return scrollPane;
	}
	
	private Component makePanel(JPanel panel, String name, String borderLayoutConst) {
		JPanel panel_2 = new JPanel();
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel label = new JLabel();
		label.setFont(new Font("Tahoma", Font.PLAIN, 8));
		label.setHorizontalAlignment(SwingConstants.CENTER);
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
		return sb.toString();
	}

	private void gifRecord() throws HeadlessException {
		if(gifs == null) { //Запуск
			World.isActiv = false;
			int result = javax.swing.JOptionPane.showConfirmDialog(null, "<html>ВНИМАНИЕ!!!<br>"
					+ "Запись видео будет происходить в текущем разрешении ("
					+ Configurations.world.getWidth() + "x" + Configurations.world.getHeight()
					+ "пк)<br>"
							+ "Менять во время записи размеры программы и открывать/закрывать панели "
							+ "крайне нерекомендуется!<br>"
							+ "Это может привести к кривой записи!!!<br>"
							+ "Снимок экрана производится раз в секунду, воспроизводится 25 кадров в секунду<br>"
							+ "Вы согласны начать запись?",
					"BioLife", javax.swing.JOptionPane.OK_CANCEL_OPTION);
			if(result == javax.swing.JOptionPane.CANCEL_OPTION) return;
			
			String pathToRoot = System.getProperty("user.dir");
			JFileChooser fileopen = new JFileChooser(pathToRoot);
			fileopen.setFileFilter(new FileNameExtensionFilter("gif", "gif"));
			int ret = fileopen.showDialog(null, "Началь запись");
			if (ret != JFileChooser.APPROVE_OPTION) return;
			try {
				String fileName = fileopen.getSelectedFile().getPath();
				if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
					if(!fileName.substring(fileName.lastIndexOf(".")+1).equals("gif"))
						fileName += ".gif";
				} else {
					fileName += ".gif";
				}
				gifs = new GifSequenceWriter(fileName, true, Configurations.world.getSize());
				World.isActiv = true;
				startRecord.setText("Остановить запись");
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null,	"<html>Запись видео неудалась по причине<br>"
						+ e1.getMessage(),	"BioLife", JOptionPane.ERROR_MESSAGE);
			}
		} else { // Закончили
			World.isActiv = false;
			try {gifs.close();} catch (IOException e1) {e1.printStackTrace();}
			gifs = null;
			startRecord.setText("Начать запись");
		}
	}
	
	private void mouseClicked_panel(JPanel panel ,JLabel panel_Label, String name, boolean isActive, String borderLayoutConst) {
		String symbol =  switch (borderLayoutConst) {
			case BorderLayout.EAST -> isActive ? "&GT;" : "&lt;";
			case BorderLayout.WEST -> !isActive ? "&GT;" : "&lt;";
			case BorderLayout.SOUTH -> isActive ? " \\/;" : " /\\";
			case BorderLayout.NORTH -> !isActive ? " \\/;" : " /\\";
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
			settings.addScale(-e.getWheelRotation() * 10);
        }
	}
	private void keyPressed(KeyEvent e) {
		//System.out.println(e);
		switch (e.getKeyCode()) {
			case KeyEvent.VK_SPACE -> {
				World.isActiv = !World.isActiv;
			}
			case KeyEvent.VK_S -> {
				if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
					Configurations.settings.save();
				else
					Configurations.TIME_OUT_POOL.execute(() -> world.step());
			}
			case KeyEvent.VK_W ->
				Configurations.TIME_OUT_POOL.execute(() -> {
					CellObject cell = botInfo.getCell();
					if (cell != null)
						cell.step(Math.round(Math.random() * 1000));
				});
			case KeyEvent.VK_E ->
				Configurations.TIME_OUT_POOL.execute(() -> {
					CellObject cell = botInfo.getCell();
					if (cell != null)
						botInfo.step();
				});
		}
	}
}
