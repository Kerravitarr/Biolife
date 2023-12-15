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
import Calculations.Point;
import Utils.JSON;
import Utils.SaveAndLoad;
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
	private Object select_mode;
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
	/**Окно поиска*/
	private MenuSearch menuSearch = null;
	/**Слушатель события щелчка по экрану для загрузи клетки*/
	private java.awt.event.MouseAdapter loadListener;
	
	
	/**Для выбора кнопочек меню*/
	private enum MENU_SELECT{NONE,REMOVE,SAVE, LOAD, EDIT}
	/**Перечисление для удаления только определённых объектов*/
	private enum REMOVE_O{
		ORGANIC("organic"),POISON("poison"),OWALL("fossil"),BOT("alive"),ALL("all"),CLEAR("clear"),
		;
		public static final REMOVE_O[] values = REMOVE_O.values();
		/**Описание пункта меню*/
		private final String text;
		private REMOVE_O(String n){text = Configurations.getHProperty(Menu.class,"remove." + n);}
	}
	/**Перечисление режимов сохранения*/
	private enum SAVE_T{
		TO_DISK,TO_CLIPBOARD;
		public static final SAVE_T[] values = SAVE_T.values();
		/**Описание пункта меню*/
		private final String text;
		private SAVE_T(){text = Configurations.getHProperty(Menu.class,"SAVE_T." + name());}
	}
	/**Перечисление режимов сохранения*/
	private enum LOAD_T{
		FROM_DISK,FROM_CLIPBOARD;
		public static final LOAD_T[] values = LOAD_T.values();
		/**Описание пункта меню*/
		private final String text;
		private LOAD_T(){text = Configurations.getHProperty(Menu.class,"LOAD_T." + name());}
	}
	/**Перечисление режимов изменения*/
	private enum EDIT_T{
		FROM_DISK,FROM_CLIPBOARD,FROM_FIELD;
		public static final EDIT_T[] values = EDIT_T.values();
		/**Описание пункта меню*/
		private final String text;
		private EDIT_T(){text = Configurations.getHProperty(Menu.class,"EDIT_T." + name());}
	}
	
	private class jPopupMenuButton extends JButton {
		public interface PopupGenerate{
			public void generate(javax.swing.JPopupMenu menu);
		}
		
		public jPopupMenuButton(PopupGenerate generator){
			addMouseListener(new MouseAdapter(){
				@Override
				public void mouseClicked(MouseEvent evt) {
					if(evt.getButton() == MouseEvent.BUTTON3){
						var jPopupMenu1 = new javax.swing.JPopupMenu();
						generator.generate(jPopupMenu1);
						jPopupMenu1.show(jPopupMenuButton.this, evt.getX(), evt.getY());
					}
				}
			});
		}
	}
	
	/**
	 * Create the panel.
	 */
	public Menu(EvolTreeDialog ed) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		evolTreeDialog = ed;
		
		loadListener = new LoadCellClickListener();
		
		add(makeButton("save", e-> save()));
		add(makeButton("load", e-> load()));
		add(start = makeButton("play", e -> {if (Configurations.world.isActiv())Configurations.world.stop();else Configurations.world.start();} ));
		add(record = makeButton("record", e-> record()));
		add(makeButton("graph", e-> {
			evolTreeDialog.setVisible(true);
			evolTreeDialog.setLocation(Menu.this.getLocationOnScreen());
		}));
		add(makeButton("cursor", e-> toDefault()));
		add(makeButton("search", e-> search()));
		add(configButton(new jPopupMenuButton((jPopupMenu1) -> {
			for(var i : REMOVE_O.values){
				var visible = new JMenuItem(i.text);
				visible.addActionListener( e -> remove(i));
				jPopupMenu1.add(visible);
			}
		}), "kill", e-> remove(REMOVE_O.ALL)));
		add(configButton(new jPopupMenuButton((jPopupMenu1) -> {
			for(var i : SAVE_T.values){
				var visible = new JMenuItem(i.text);
				visible.addActionListener( e -> saveCell(i));
				jPopupMenu1.add(visible);
			}
		}),"saveCell", e->saveCell(SAVE_T.TO_CLIPBOARD)));
		add(configButton(new jPopupMenuButton((jPopupMenu1) -> {
			for(var i : LOAD_T.values){
				var visible = new JMenuItem(i.text);
				visible.addActionListener( e -> loadCell(i));
				jPopupMenu1.add(visible);
			}
		}),"loadCell", e->loadCell(LOAD_T.FROM_CLIPBOARD)));
		add(configButton(new jPopupMenuButton((jPopupMenu1) -> {
			for(var i : EDIT_T.values){
				var visible = new JMenuItem(i.text);
				visible.addActionListener( e -> editCell(i));
				jPopupMenu1.add(visible);
			}
		}),"editCell", e->editCell(EDIT_T.FROM_FIELD)));
		

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
	/** Создаёт кнопку с иконкой для меню
	 * @param name имя файла из ресурсов, какую картинку взять
	 * @param al слушатель события, что что-то произошо
	 * @return новосозданая кнопка
	 */
	private JButton makeButton(String name, ActionListener al) {
		return configButton(new JButton(),name,al);
	}
	/** Конфигурирует кнопку с иконкой для меню
	 * @param button кнопка, которую конфигурируем
	 * @param name имя файла из ресурсов, какую картинку взять
	 * @param al слушатель события, что что-то произошо
	 * @return новосозданая кнопка
	 */
	private JButton configButton(JButton button, String name, ActionListener al) {
		Configurations.setIcon(button,name);
		button.setMaximumSize (new Dimension(40,20));
        button.addActionListener(e -> EventQueue.invokeLater(() -> al.actionPerformed(e)));
        button.setToolTipText(Configurations.getHProperty(Menu.class,name));
		return button;
	}
	/**Запускает удаление объектов на карте
	 * @param o тип объектов, подлежащих удалению
	 */
	private void remove(REMOVE_O o) {
		if(o == REMOVE_O.CLEAR){
			Configurations.world.awaitStop();
			for(var x = 0 ; x < Configurations.getWidth() ; x++){
				for(var y = 0 ; y < Configurations.getHeight(); y++){
					final var point = Point.create(x,y);
					if(!point.valid()) continue;
					final var get = Configurations.world.get(point);
					if(get != null) get.remove_NE();
				}
			}
		} else {
			Configurations.world.stop();
			final var vw = Configurations.getViewer().get(WorldView.class);
			select_mode = o;
			select = MENU_SELECT.REMOVE;
			vw.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		}
	}
	/**Сбрасывает курсор и пункт меню в положение по умолчанию*/
	private void toDefault() {
		select = MENU_SELECT.NONE;
		final var vw = Configurations.getViewer().get(WorldView.class);
		vw.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	/** Обрабатывает выбранные на экране клетки
	 * @param cellObjects 
	 */
	public void setCell(List<CellObject> cellObjects) {
		if(cellObjects == null) return;
		switch (select) {
			case REMOVE -> {
				for(var cellObject : cellObjects){
					if(cellObject == null) continue;
					switch ((REMOVE_O)select_mode) {
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
			case SAVE->{
				switch ((SAVE_T)select_mode) {
					case TO_CLIPBOARD -> {
						for(var cellObject : cellObjects){
							if(cellObject == null) continue;
							final var stringSelection = new java.awt.datatransfer.StringSelection(cellObject.toJSON().toJSONString());
							final var clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
							clipboard.setContents(stringSelection, null);
							break;
						}
					}
					case TO_DISK -> {
						final var countCell = cellObjects.stream().filter(c -> c != null).count();
						if(countCell == 0) return;
						if(countCell == 1){
							final var cell = cellObjects.stream().filter(c -> c != null).findFirst().get();
							LoadSaveFactory.save("BioLife", "zbcell", name -> {
								var js = SaveAndLoad.save(name, Configurations.VERSION);
								js.save(new SaveAndLoad.Serialization() {
									@Override public String getName() { return "cell";}
									@Override public JSON getJSON() {return cell.toJSON();}
								});
							}, false);
						} else {
							LoadSaveFactory.save("BioLife", "zbcells", name -> {
								var js = SaveAndLoad.save(name, Configurations.VERSION);
								js.save(new SaveAndLoad.Serialization() {
									@Override public String getName() { return "cells";}
									@Override public JSON getJSON() {
										final var j = new JSON();
										j.add("array", cellObjects.stream().filter(c -> c != null).map(c -> c.toJSON()).toList());
										return j;
									}
								});
							}, false);
						}
					}
					default -> throw new AssertionError();
				}
				toDefault();
			}
			default -> throw new AssertionError();
		}
	}
	/**Режим выбора клеток для меню?
	 * @return true, если мы должны нарисовать квадратик и выбрать некоторые клетки
	 */
	public boolean isSelectedCell(){
		return select == MENU_SELECT.REMOVE || select == MENU_SELECT.SAVE;
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
			
			LoadSaveFactory.save("BioLife", "gif", Configurations.getProperty(Menu.class,"record.start"),
					fileName -> {gifs = new GifSequenceWriter(fileName, true, vw.getSize());Configurations.world.start();}, false,
					el -> {JOptionPane.showMessageDialog(vw,	Configurations.getHProperty(Menu.class,"record.error")
						+ el.getMessage(),	"BioLife", JOptionPane.ERROR_MESSAGE);}, false);
			
		} else { // Закончили
			Configurations.world.stop();
			try {gifs.close();} catch (IOException e1) {e1.printStackTrace();}
			gifs = null;
		}
	}
	/**Открывает окошечко сохранения мира и... Сохраняет мир, собственно*/
	public void save() {
		boolean oldStateWorld = Configurations.world.isActiv();	
		Configurations.world.awaitStop();
		LoadSaveFactory.save("BioLife", "zbmap", name -> Configurations.save(name));
		if (oldStateWorld)
			Configurations.world.start();
	}
	/**Открывает окошечко загрузки мира и... Загружает мир, собственно*/
	public void load() {
		Configurations.world.awaitStop();
		LoadSaveFactory.load("BioLife", "zbmap", name -> {
			final var vw = Configurations.getViewer().get(WorldView.class);
			try {
				Configurations.load(name);
				try {
					Configurations.getViewer().get(Settings.class).rebuild();
				} catch (IllegalArgumentException | NullPointerException ex){} //Всё нормально, просто нет такого класса
				evolTreeDialog.restart();
			} catch (GenerateClassException ex) {
				Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				JOptionPane.showMessageDialog(vw,	ex.getLocalizedMessage(),	"BioLife", JOptionPane.ERROR_MESSAGE);
			}
		});
	}
	/**Открывает/закрывает окно поиска объектов на экране*/
	public void search(){
		if(menuSearch != null && menuSearch.isVisible()){
			menuSearch.dispose();
			menuSearch = null;
		} else {
			menuSearch = new MenuSearch(false);
			menuSearch.setVisible(true);
			menuSearch.setLocationRelativeTo(this);
		}
	}
	/**Проверяет объект по условиям поиска
	 * @param co проверяемый объект
	 * @return true, если это тот самый объект, что мы ищем
	 */
	public boolean isVisibleCell(CellObject co){
		return (menuSearch == null || !menuSearch.isVisible() || menuSearch.isCorrect(co)) && (select != MENU_SELECT.SAVE || co.aliveStatus(CellObject.LV_STATUS.LV_ALIVE));
	}
	/**Активирует режим сохранения клетки
	 * @param mode сопосб сохранения
	 */
	private void saveCell(SAVE_T mode){
		Configurations.world.stop();
		select = MENU_SELECT.SAVE;
		select_mode = mode;
		final var vw = Configurations.getViewer().get(WorldView.class);
		vw.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	/**Активирует режим сохранения клетки
	 * @param mode сопосб сохранения
	 */
	private void loadCell(LOAD_T mode){
		Configurations.world.stop();
		select = MENU_SELECT.LOAD;
		select_mode = mode;
		final var vw = Configurations.getViewer().get(WorldView.class);
		vw.setCursor(new Cursor(Cursor.HAND_CURSOR));
		vw.addMouseListener(loadListener);
	}
	
	/**Активирует режим редактирования клетки
	 * @param mode сопосб сохранения
	 */
	private void editCell(EDIT_T mode){
		Configurations.world.stop();
		select = MENU_SELECT.EDIT;
		select_mode = mode;
		switch (mode) {
			case FROM_CLIPBOARD -> {
				try{
					final var  data = (String) java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
					final var json = new Utils.JSON(data);
					final var node = Configurations.tree.makeTree();
					json.add("GenerationTree", node.getBranch()); //Так ну совсем совсем нельзя делать... А я делаю :(
					final var cell = new AliveCell(json, Configurations.tree, Configurations.VERSION);
					node.remove();
					toDefault();
					editCell(cell);
				} catch (Exception ex){
					Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					JOptionPane.showMessageDialog(null,	Configurations.getHProperty(Menu.class,"loadCell.error",ex.getMessage()), "BioLife", JOptionPane.ERROR_MESSAGE);
				}
			}
			case FROM_DISK ->{
				LoadSaveFactory.load("BioLife", "zbcell", filename -> {
					var js = SaveAndLoad.load(filename);
					try{
						final var cell = js.load((j, version)-> {
							final var node = Configurations.tree.makeTree();
							j.add("GenerationTree", node.getBranch()); //Так ну совсем совсем нельзя делать... А я делаю :(
							final var acell = new AliveCell(j, Configurations.tree, version);
							node.remove();
							return acell;
						},"cell");
						toDefault();
						editCell(cell);
					} catch (Exception ex){
						Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
						JOptionPane.showMessageDialog(null,	Configurations.getHProperty(Menu.class,"loadCell.error",ex.getMessage()), "BioLife", JOptionPane.ERROR_MESSAGE);
					}
				});
			}
			case FROM_FIELD -> {
				final var vw = Configurations.getViewer().get(WorldView.class);
				vw.setCursor(new Cursor(Cursor.HAND_CURSOR));
				vw.addMouseListener(loadListener);
			}
			default -> throw new AssertionError();
		}
	}
	
	/** Открывает окно изменения клетк
	 * @param cell сопосб сохранения
	 */
	private void editCell(AliveCell cell){
		Configurations.world.stop();
		final var editor = new CellEditor(cell);
		final var vw = Configurations.getViewer().get(WorldView.class);
		editor.setLocationRelativeTo(vw);
		editor.setVisible(true);
	}
	
	/**Слушатель события, что человек нажал на экран для вставки тудой клетки*/
	private class LoadCellClickListener extends MouseAdapter {
		public LoadCellClickListener() {}
		@Override public void mousePressed(MouseEvent e) {
			if(select == MENU_SELECT.LOAD && e.getButton() == MouseEvent.BUTTON1){
				final var vw = Configurations.getViewer().get(WorldView.class);
				final var point = vw.getTransform().toWorldPoint(e);
				if(point.valid()){
					final var old = Configurations.world.get(point);
					if(old != null){
						JOptionPane.showMessageDialog(null,	Configurations.getHProperty(Menu.class,"loadCell.busy",old), "BioLife", JOptionPane.ERROR_MESSAGE);
					} else {
						final var cell = new AliveCell[]{null};
						switch ((LOAD_T)select_mode) {
							case FROM_CLIPBOARD -> {
								try{
									final var  data = (String) java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
									final var json = new Utils.JSON(data);
									final var node = Configurations.tree.makeTree();
									json.add("GenerationTree", node.getBranch()); //Так ну совсем совсем нельзя делать... А я делаю :(
									cell[0] = new AliveCell(json, Configurations.tree, Configurations.VERSION);
								} catch (Exception ex){
									Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
									JOptionPane.showMessageDialog(null,	Configurations.getHProperty(Menu.class,"loadCell.error",ex.getMessage()), "BioLife", JOptionPane.ERROR_MESSAGE);
									cell[0] = null;
								}
							}
							case FROM_DISK -> {
								LoadSaveFactory.load("BioLife", "zbcell", filename -> {
									var js = SaveAndLoad.load(filename);
									try{
										cell[0] = js.load((j, version)-> {
											final var node = Configurations.tree.makeTree();
											j.add("GenerationTree", node.getBranch()); //Так ну совсем совсем нельзя делать... А я делаю :(
											return new AliveCell(j, Configurations.tree, version);
										},"cell");
									} catch (Exception ex){
										Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
										JOptionPane.showMessageDialog(null,	Configurations.getHProperty(Menu.class,"loadCell.error",ex.getMessage()), "BioLife", JOptionPane.ERROR_MESSAGE);
										cell[0] = null;
									}
								});
							}
							default -> throw new AssertionError();
						}
						if(cell[0] != null){
							cell[0].setPos(point);
							Configurations.world.add(cell[0]);
						}
						toDefault();
					}
				}
			} else if(select == MENU_SELECT.EDIT && e.getButton() == MouseEvent.BUTTON1){
				final var vw = Configurations.getViewer().get(WorldView.class);
				final var point = vw.getTransform().toWorldPoint(e);
				if(point.valid()){
					final var cell = Configurations.world.get(point);
					if(cell == null || !(cell instanceof AliveCell)) return;
					toDefault();
					editCell((AliveCell)cell);
				}
			}
		}
	}
}
