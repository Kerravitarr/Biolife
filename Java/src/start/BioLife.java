package start;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import panels.Settings;

public class BioLife extends JFrame {
	private class UpdateScrinTask implements Runnable {
		DecimalFormat df = new DecimalFormat("###,###");

		@Override
		public void run() {
			String title = MessageFormat.format(Configurations.bundle.getString("BioLife.title"), world.fps.FPS(), df.format(world.step),
					world.sps.FPS(), df.format(world.countLife), df.format(world.countOrganic), df.format(world.countPoison));
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
	
	private final JPanel contentPane;
	BotInfo botInfo = null;
	Settings settings = null;
	private World world;
	private JScrollPane scrollPane;
	private EvolTreeDialog dialog = new EvolTreeDialog();
	private GifSequenceWriter gifs = null;
	private JMenuItem startRecord;
	/**Спящий поток, в котором обрабатываются всякие там нажатия кнопок*/
	private ExecutorService executor;
	private final JLabel botInfo_label;
	private final Legend legend;
	private final JLabel legend_label;
	private final JLabel settings_label;

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
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Configurations.settings.save();
			}
		});
		setBounds(100, 100, (int) (450*2.5), (int) (300*2.5));
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu Menu = new JMenu("Меню");
		menuBar.add(Menu);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		executor = Executors.newSingleThreadExecutor();
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new BorderLayout(0, 0));
		settings_label = new JLabel("<html>Н<br>А<br>С<br>Т<br>Р<br>О<br>Й<br>К<br>И<br>&lt;</html>");
		settings_label.setFont(new Font("Tahoma", Font.PLAIN, 8));
		settings_label.addMouseListener(new MouseAdapter() {
			boolean isActive = false;
			@Override
			public void mouseClicked(MouseEvent e) {BioLife.this.mouseClicked_settings(isActive=!isActive);}
		});
		panel_1.add(settings_label, BorderLayout.WEST);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		legend = new Legend();
		panel.add(legend, BorderLayout.CENTER);
		legend_label = new JLabel("Легенда /\\");
		legend_label.addMouseListener(new MouseAdapter() {
			boolean isActive = false;
			@Override
			public void mouseClicked(MouseEvent e) {BioLife.this.mouseClicked_legend(e,isActive=!isActive);}
		});
		legend_label.setHorizontalAlignment(SwingConstants.CENTER);
		legend_label.setFont(new Font("Tahoma", Font.PLAIN, 8));
		panel.add(legend_label, BorderLayout.NORTH);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.WEST);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		botInfo_label = new JLabel("<html>И<br>Н<br>Ф<br>О<br>Р<br>М<br>А<br>Ц<br>И<br>Я<br><br>О<br><br>Б<br>О<br>Т<br>Е<br>&GT;</html>");
		botInfo_label.setFont(new Font("Tahoma", Font.PLAIN, 8));
		botInfo_label.addMouseListener(new MouseAdapter() {
			boolean isActive = false;
			@Override
			public void mouseClicked(MouseEvent e) {BioLife.this.mouseClicked_botInfo(e,isActive=!isActive);}
		});
		panel_2.add(botInfo_label, BorderLayout.EAST);

		botInfo = new BotInfo();
		panel_2.add(botInfo, BorderLayout.CENTER);

		settings = new Settings();
		panel_1.add(settings, BorderLayout.CENTER);
		
		world = new World();
		
		scrollPane = new JScrollPane();
		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				world.setPreferredSize(new Dimension(scrollPane.getWidth() * settings.scale.getValue() / 10  - 10,scrollPane.getHeight() * settings.scale.getValue() / 10  - 10));
			}
		});
		settings.setListener(scrollPane);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportView(world);
		botInfo.setVisible(false);
		settings.setVisible(false);
		legend.setVisible(false);
		
		JMenuItem restart = new JMenuItem("Рестарт");
		restart.addActionListener(e->{world.worldGenerate();world.repaint();});
		//Menu.add(restart);

		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JMenuItem mntmNewMenuItem = new JMenuItem("Дерево эволюции");
		mntmNewMenuItem.addActionListener(e->dialog.setVisible(true));
		Menu.add(mntmNewMenuItem);
		
		JMenuItem save_menu = new JMenuItem("Сохранить");
		save_menu.addActionListener(e->Configurations.settings.save());
		Menu.add(save_menu);
		
		JMenuItem load_menu = new JMenuItem("Загрузить");
		load_menu.addActionListener(e->Configurations.settings.load());
		Menu.add(load_menu);
		
		startRecord = new JMenuItem("Начать запись");
		startRecord.addActionListener(e->gifRecord());
		Menu.add(startRecord);
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {BioLife.this.keyPressed(e);}
		});
		
		Configurations.TIME_OUT_POOL.scheduleWithFixedDelay(new UpdateScrinTask(), 1, 1, TimeUnit.SECONDS);
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
	

	private void mouseClicked_settings(boolean isActive) {
		settings.setVisible(isActive);
		if (isActive)
			settings_label.setText("<html>Н<br>А<br>С<br>Т<br>Р<br>О<br>Й<br>К<br>И<br>&GT;</html>");
		else {
			settings_label.setText("<html>Н<br>А<br>С<br>Т<br>Р<br>О<br>Й<br>К<br>И<br>&lt;</html>");
			BioLife.this.toFront();
			BioLife.this.requestFocus();
		}
	}
	private void mouseClicked_legend(MouseEvent e, boolean isActive) {
		legend.setVisible(isActive);
		if (isActive)
			legend_label.setText("Легенда \\/");
		else {
			legend_label.setText("Легенда /\\");
			BioLife.this.toFront();
			BioLife.this.requestFocus();
		}
	}
	private void mouseClicked_botInfo(MouseEvent e, boolean isActive) {
		botInfo.setVisible(isActive);
		if (isActive)
			botInfo_label.setText("<html>И<br>Н<br>Ф<br>О<br>Р<br>М<br>А<br>Ц<br>И<br>Я<br><br>О<br><br>Б<br>О<br>Т<br>Е<br>&lt;</html>");
		else {
			botInfo_label.setText("<html>И<br>Н<br>Ф<br>О<br>Р<br>М<br>А<br>Ц<br>И<br>Я<br><br>О<br><br>Б<br>О<br>Т<br>Е<br>&GT;</html>");
			BioLife.this.toFront();
			BioLife.this.requestFocus();
		}
	}
	private void keyPressed(KeyEvent e) {
		//System.out.println(e);
		switch (e.getKeyCode()) {
			case KeyEvent.VK_SPACE -> {
				World.isActiv = !World.isActiv;
				settings.updateScrols();
			}
			case KeyEvent.VK_S -> {
				if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
					Configurations.settings.save();
				else
					executor.execute(() -> world.step());
			}
			case KeyEvent.VK_W ->
				executor.execute(() -> {
					CellObject cell = botInfo.getCell();
					if (cell != null)
						cell.step(Math.round(Math.random() * 1000));
				});
			case KeyEvent.VK_E ->
				executor.execute(() -> {
					CellObject cell = botInfo.getCell();
					if (cell != null)
						botInfo.step();
				});
		}
	}
}
