package start;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import MapObjects.CellObject;
import main.World;
import panels.BotInfo;
import panels.EvolTree;
import panels.Legend;
import panels.Settings;

public class BioLife extends JFrame {

	private JPanel contentPane;
	BotInfo botInfo = null;
	Settings settings = null;
	private World world;
	private JScrollPane scrollPane;
	private EvolTree dialog = new EvolTree();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BioLife frame = new BioLife();
					frame.setVisible(true);
					
					new Timer().schedule(new TimerTask() { // Определяем задачу
					    DecimalFormat df = new DecimalFormat( "###,###" );
					    public void run() {
					    	frame.setTitle("ФПС" + frame.world.fps.FPS()+" кадров/секунду. "
					    			+ "Шёл " + df.format(frame.world.step) + " цикл эволюции (" + frame.world.sps.FPS() + " шаг/сек) "
					    					+ "Живых: " + df.format(frame.world.countLife) + ", плоти: " + df.format(frame.world.countOrganic));
					    	if(frame.dialog.isVisible())
					    		frame.dialog.repaint();
					    	frame.world.repaint();
					    } 
					}, 0L, 1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BioLife() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new BorderLayout(0, 0));
		JLabel lblNewLabel_1 = new JLabel("<html>Н<br>А<br>С<br>Т<br>Р<br>О<br>Й<br>К<br>И<br>&lt;</html>");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 8));
		lblNewLabel_1.addMouseListener(new MouseAdapter() {
			boolean isActive = false;
			public void mouseClicked(MouseEvent e) {
				isActive = !isActive;
				settings.setVisible(isActive);
				if(isActive) 
					lblNewLabel_1.setText("<html>Н<br>А<br>С<br>Т<br>Р<br>О<br>Й<br>К<br>И<br>&GT;</html>");
				else {
					lblNewLabel_1.setText("<html>Н<br>А<br>С<br>Т<br>Р<br>О<br>Й<br>К<br>И<br>&lt;</html>");
					BioLife.this.toFront();
					BioLife.this.requestFocus();
				}
			}
		});
		panel_1.add(lblNewLabel_1, BorderLayout.WEST);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		
		JLabel lblNewLabel = new JLabel("Легенда /\\");
		lblNewLabel.addMouseListener(new MouseAdapter() {
			Legend legend = null;
			boolean isActive = false;
			public void mouseClicked(MouseEvent e) {
				if(legend == null) {
					legend = new Legend();
					panel.add(legend, BorderLayout.CENTER);
				}
				isActive = !isActive;
				legend.setVisible(isActive);
				if(isActive) 
					lblNewLabel.setText("Легенда \\/");
				else {
					lblNewLabel.setText("Легенда /\\");
					BioLife.this.toFront();
					BioLife.this.requestFocus();
				}
			}
		});
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 8));
		panel.add(lblNewLabel, BorderLayout.NORTH);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.WEST);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_2 = new JLabel("<html>И<br>Н<br>Ф<br>О<br>Р<br>М<br>А<br>Ц<br>И<br>Я<br><br>О<br><br>Б<br>О<br>Т<br>Е<br>&GT;</html>");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 8));
		lblNewLabel_2.addMouseListener(new MouseAdapter() {
			boolean isActive = false;
			public void mouseClicked(MouseEvent e) {
				isActive = !isActive;
				botInfo.setVisible(isActive);
				if(isActive) 
					lblNewLabel_2.setText("<html>И<br>Н<br>Ф<br>О<br>Р<br>М<br>А<br>Ц<br>И<br>Я<br><br>О<br><br>Б<br>О<br>Т<br>Е<br>&lt;</html>");
				else {
					lblNewLabel_2.setText("<html>И<br>Н<br>Ф<br>О<br>Р<br>М<br>А<br>Ц<br>И<br>Я<br><br>О<br><br>Б<br>О<br>Т<br>Е<br>&GT;</html>");
					BioLife.this.toFront();
					BioLife.this.requestFocus();
				}
			}
		});
		panel_2.add(lblNewLabel_2, BorderLayout.EAST);

		botInfo = new BotInfo();
		panel_2.add(botInfo, BorderLayout.CENTER);

		settings = new Settings();
		panel_1.add(settings, BorderLayout.CENTER);
		
		world = new World(botInfo,settings);
		
		scrollPane = new JScrollPane();
		scrollPane.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				world.setPreferredSize(new Dimension(scrollPane.getWidth() * settings.scale.getValue() / 10  - 10,scrollPane.getHeight() * settings.scale.getValue() / 10  - 10));
			}
		});
		settings.setListener(scrollPane);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportView(world);
		botInfo.setVisible(false);
		settings.setVisible(false);
		
		JMenuItem restart = new JMenuItem("Рестарт");
		restart.addActionListener(e->{
			contentPane.remove(world);
			world = new World(botInfo,settings);
			contentPane.add(world, BorderLayout.CENTER);
			world.repaint();
		});
		//Menu.add(restart);

		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JMenuItem mntmNewMenuItem = new JMenuItem("Дерево эволюции");
		mntmNewMenuItem.addActionListener(e->{
			dialog.setVisible(true);
		});
		Menu.add(mntmNewMenuItem);
		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				//System.out.println(e);
				switch (e.getKeyCode()) {
					case KeyEvent.VK_SPACE ->{
						World.isActiv = !World.isActiv;
						settings.updateScrols();
					}
					case KeyEvent.VK_S ->{
						new Thread() {
				            public void run() {
								world.step();
				            }
				        }.start();
					}
					case KeyEvent.VK_W ->{
						new Thread() {
				            public void run() {
				        		CellObject cell = botInfo.getCell();
				            	if(cell != null)
				            		cell.step(Math.round(Math.random() * 1000));
				            }
				        }.start();
					}
				}
			}
		});
		
	}
}
