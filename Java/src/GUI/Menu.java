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
	public Menu() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		//Конфигурация мира
		//Рестарт
		add(makeButton("save", e-> save()));
		add(makeButton("load", e-> load()));
		//add(makeButton("search", e-> System.out.println(e)));
		add(start = makeButton("play", e -> {if (Configurations.world.isActiv())Configurations.world.stop();else Configurations.world.start();} ));
		add(record = makeButton("record", e-> record()));
		add(makeButton("graph", e-> Configurations.evolTreeDialog.setVisible(true)));
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
					jPopupMenu1.show(Menu.this, evt.getX(), evt.getY());
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
			/*try {
				System.out.println("Start frame");
				gifs.nextFrame(g -> Configurations.world.paintComponent(g, true));
				System.out.println("End frame");
			} catch (IOException e) {
				Configurations.world.stop();
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, Configurations.getHProperty(Menu.class,"record.break")
						+ e.getMessage(), "BioLife", JOptionPane.ERROR_MESSAGE);
				gifs = null;
			}*/ throw new AssertionError();
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
		removeO = o;
		select = MENU_SELECT.REMOVE;
		/*Configurations.world.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));*/ throw new AssertionError();
	}
	private void toDefault() {
		select = MENU_SELECT.NONE;
		/*Configurations.world.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));*/ throw new AssertionError();
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
			Configurations.world.stop();
			/*int result = javax.swing.JOptionPane.showConfirmDialog(null, 
					MessageFormat.format(Configurations.getHProperty(Menu.class,"record.warning"),Configurations.world.getWidth(),Configurations.world.getHeight()),
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
				Configurations.world.start();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null,	Configurations.getHProperty(Menu.class,"record.error")
						+ e1.getMessage(),	"BioLife", JOptionPane.ERROR_MESSAGE);
			}*/ throw new AssertionError();
		} else { // Закончили
			Configurations.world.stop();
			try {gifs.close();} catch (IOException e1) {e1.printStackTrace();}
			gifs = null;
		}
	}
	
	/**Открывает окошечко сохранения мира и... Сохраняет мир, собственно*/
	public void save() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}
	/**Открывает окошечко загрузки мира и... Загружает мир, собственно*/
	public void load() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}
}
