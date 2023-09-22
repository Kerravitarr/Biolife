package GUI;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.CellObjectRemoveException;
import Utils.GifSequenceWriter;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.JMenuItem;
import Calculations.Configurations;
import static Calculations.Configurations.getViewer;
import Calculations.GenerateClassException;
import java.awt.Cursor;
import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Menu extends JPanel implements Configurations.EvrySecondTask{
	/**Какая из кнопок выбрана*/
	private MENU_SELECT select = MENU_SELECT.NONE;
	/**Объекты какого типа удаляем*/
	private REMOVE_O removeO;
	/**Кнопка запуска моделирования*/
	private final JButton start;
	/*Память на то какая иконка у кнопки старта**/
	private boolean startButtonIsStart = true;
	/**Кнопка гифок*/
	private final JButton record;
	/**Писчик гифок*/
	private GifSequenceWriter gifs = null;
	/**Флаг, что мы пишем гифки*/
	private boolean gifRecord = false;
	/**Дерево эволюции, которым мы правим*/
	private EvolTreeDialog evolTreeDialog;
	
	
	/**Для выбора кнопочек меню*/
	enum MENU_SELECT{NONE,REMOVE}
	/**Перечисление для удаления только определённых объектов*/
	enum REMOVE_O{
		ORGANIC("organic"),POISON("poison"),OWALL("fossil"),BOT("alive"),ALL("all"),
		;
		public static final REMOVE_O[] values = REMOVE_O.values();
		/**Описание пункта меню*/
		private final String text;
		private REMOVE_O(String n){text = Configurations.getHProperty(Menu.class,"remove." + n);}
	}
	
	/**
	 * Create the panel.
	 */
	public Menu(EvolTreeDialog ed) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		evolTreeDialog = ed;
		//Конфигурация мира
		//Рестарт
		add(makeButton("save", e-> save()));
		add(makeButton("load", e-> load()));
		//add(makeButton("search", e-> System.out.println(e)));
		add(start = makeButton("play", e -> {if (Configurations.world.isActiv())Configurations.world.stop();else Configurations.world.start();} ));
		add(record = makeButton("record", e-> record()));
		add(makeButton("graph", e-> {
			evolTreeDialog.setVisible(true);
			evolTreeDialog.setLocation(Menu.this.getLocationOnScreen());
		}));
		add(makeButton("cursor", e-> toDefault()));
		JButton kill;
		add(kill = makeButton("kill", e-> remove(REMOVE_O.ALL)));
		kill.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent evt) {
				if(evt.getButton() == MouseEvent.BUTTON3){
					var jPopupMenu1 = new javax.swing.JPopupMenu();
					for(var i : REMOVE_O.values){
						var visible = new JMenuItem(i.text);
						visible.addActionListener( e -> remove(i));
						jPopupMenu1.add(visible);
					}
					jPopupMenu1.show(kill, evt.getX(), evt.getY());
				}
			}
		});

		Configurations.addTask(this);
	}
	
	@Override
	public void taskStep(){
		var isA = Configurations.world.isActiv();
		if(isA != startButtonIsStart) {
			startButtonIsStart = isA;
			Configurations.setIcon(start,isA ? "pause" : "play");
		}
		
		if (gifs != null) {
			if(!gifRecord){
				Configurations.setIcon(record,"record_stop");
				gifRecord = true;
			}
			try {
				final var vw = ((DefaultViewer) Configurations.getViewer()).getWorld();
				gifs.nextFrame(g -> vw.paintComponent(g, true));
			} catch (IOException e) {
				Configurations.world.stop();
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, Configurations.getHProperty(Menu.class,"record.break")
						+ e.getMessage(), "BioLife", JOptionPane.ERROR_MESSAGE);
				gifs = null;
			}
		} else if(gifRecord){
			Configurations.setIcon(record,"record");
			gifRecord = false;
		}
	}

	private JButton makeButton(String name, ActionListener al) {
		
		JButton button = new JButton();
		Configurations.setIcon(button,name);
		button.setMaximumSize (new Dimension(40,20));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(e -> EventQueue.invokeLater(() -> al.actionPerformed(e)));
        button.setToolTipText(Configurations.getHProperty(Menu.class,name));
        button.setFocusable(false);
		return button;
	}
	/**Запускает удаление объектов на карте
	 * @param o тип объектов, подлежащих удалению
	 */
	private void remove(REMOVE_O o) {
		Configurations.world.stop();
		final var vw = Configurations.getViewer().get(WorldView.class);
		removeO = o;
		select = MENU_SELECT.REMOVE;
		vw.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}
	private void toDefault() {
		select = MENU_SELECT.NONE;
		final var vw = Configurations.getViewer().get(WorldView.class);
		vw.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void setCell(List<CellObject> cellObjects) {
		if(cellObjects == null) return;
		for(var cellObject : cellObjects){
			if(cellObject == null) continue;
			switch (select) {
				case NONE -> {}
				case REMOVE -> {
					switch (removeO) {
						case ALL -> {
							if (cellObject instanceof AliveCell acell) {
								try {
									acell.bot2Organic();
								}catch (CellObjectRemoveException e) {}
							} else {
								cellObject.remove_NE();
							}
						}
						case ORGANIC -> {
							if (cellObject instanceof MapObjects.Organic)
								cellObject.remove_NE();
						}
						case POISON -> {
							if (cellObject instanceof MapObjects.Poison)
								cellObject.remove_NE();
						}
						case OWALL -> {
							if (cellObject instanceof MapObjects.Fossil)
								cellObject.remove_NE();
						}
						case BOT -> {
							if (cellObject instanceof AliveCell acell) {
								try {
									acell.bot2Organic();
								}catch (CellObjectRemoveException e) {}
							}
						}
					}
				}
			}
		}
	}
	/**Режим выбора клеток для меню?
	 * @return true, если мы должны нарисовать квадратик и выбрать некоторые клетки
	 */
	public boolean isSelectedCell(){
		return select == MENU_SELECT.REMOVE;
	}
	/**Функция активации и остановки записи видео */
	private void record(){
		if(gifs == null) { //Запуск
			Configurations.world.awaitStop();
			final var v = Configurations.getViewer();
			if(!(v instanceof DefaultViewer)) return;
			final var vw = ((DefaultViewer) v).getWorld();
			int result = javax.swing.JOptionPane.showConfirmDialog(vw, 
					MessageFormat.format(Configurations.getHProperty(Menu.class,"record.warning"),vw.getWidth(),vw.getHeight()),
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
				gifs = new GifSequenceWriter(fileName, true, vw.getSize());
				Configurations.world.start();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(vw,	Configurations.getHProperty(Menu.class,"record.error")
						+ e1.getMessage(),	"BioLife", JOptionPane.ERROR_MESSAGE);
			}
		} else { // Закончили
			Configurations.world.stop();
			try {gifs.close();} catch (IOException e1) {e1.printStackTrace();}
			gifs = null;
		}
	}
	
	/**Открывает окошечко сохранения мира и... Сохраняет мир, собственно*/
	public void save() {
		if(Configurations.confoguration.lastSaveCount == Configurations.world.step) return;
		boolean oldStateWorld = Configurations.world.isActiv();	
		Configurations.world.awaitStop();
		final var vw = Configurations.getViewer().get(WorldView.class);
		
		JFileChooser fileopen = new JFileChooser(System.getProperty("user.dir"));
		final var extension = "zbmap";
		final var title = "BioLife";
		fileopen.setFileFilter(new FileNameExtensionFilter(extension, extension));
		while(true) {
			int ret = fileopen.showDialog(vw, Configurations.getProperty(this.getClass(),"save.selectTitle"));
			if (ret != JFileChooser.APPROVE_OPTION) return;
			String fileName = fileopen.getSelectedFile().getPath();
			if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
				if(!fileName.substring(fileName.lastIndexOf(".")+1).equals(extension))
					fileName += "."+extension;
			} else {
				fileName += "."+extension;
			}
			var file = new File(fileName);
			if(file.exists()) {
				int result;
				if(fileName.lastIndexOf("\\") != -1)
					result = JOptionPane.showConfirmDialog(vw,MessageFormat.format(Configurations.getProperty(this.getClass(),"save.fileExist"),fileName.substring(fileName.lastIndexOf("\\")+1)), title,JOptionPane.YES_NO_CANCEL_OPTION);
				else
					result = JOptionPane.showConfirmDialog(vw,MessageFormat.format(Configurations.getProperty(this.getClass(),"save.fileExist"),fileName), "BioLife",JOptionPane.YES_NO_CANCEL_OPTION);
				switch (result) {
					case JOptionPane.YES_OPTION-> {file.delete();}
					case JOptionPane.NO_OPTION-> {continue;}
					case JOptionPane.CANCEL_OPTION-> {return;}
				}
			}
			try {
				Configurations.save(fileName);
				JOptionPane.showMessageDialog(vw,	Configurations.getProperty(this.getClass(),"save.ok"),	title, JOptionPane.INFORMATION_MESSAGE);
				break;
			} catch (IOException ex) {
				Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				JOptionPane.showMessageDialog(vw,	Configurations.getHProperty(this.getClass(),"save.error") + ex,	title, JOptionPane.ERROR_MESSAGE);
			}
		}
		if (oldStateWorld)
			Configurations.world.start();
	}
	/**Открывает окошечко загрузки мира и... Загружает мир, собственно*/
	public void load() {
		Configurations.world.awaitStop();
		final var vw = Configurations.getViewer().get(WorldView.class);
		
		JFileChooser fileopen = new JFileChooser(System.getProperty("user.dir"));
		final var extension = "zbmap";
		final var title = "BioLife";
		fileopen.setFileFilter(new FileNameExtensionFilter(extension, extension));
		int ret = fileopen.showDialog(vw, Configurations.getProperty(this.getClass(),"load.selectTitle"));
		if (ret == JFileChooser.APPROVE_OPTION) {
			try {
				Configurations.load(fileopen.getSelectedFile().getPath());
				try {
					Configurations.getViewer().get(Settings.class).rebuild();
				} catch (IllegalArgumentException | NullPointerException ex){} //Всё нормально, просто нет такого класса
				evolTreeDialog.restart();
			} catch (IOException ex) {
				Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				JOptionPane.showMessageDialog(vw,	Configurations.getHProperty(this.getClass(),"load.error") + ex,	title, JOptionPane.ERROR_MESSAGE);
			} catch (GenerateClassException ex) {
				Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				JOptionPane.showMessageDialog(vw,	ex.getLocalizedMessage(),	title, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
