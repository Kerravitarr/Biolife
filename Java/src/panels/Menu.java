package panels;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import MapObjects.AliveCell;
import MapObjects.CellObject;
import MapObjects.CellObject.CellObjectRemoveException;
import Utils.GifSequenceWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import main.Configurations;
import main.World;

public class Menu extends JPanel {
	/**Для выбора кнопочек меню*/
	enum MENU_SELECT{NONE,REMOVE,REMOVE_ALIVE,REMOVE_ORGANIC,REMOVE_POISON,REMOVE_FOSIL}
	/**Поток автоматического обновления*/
	private class WorkTask implements Runnable{
		@Override
		public void run() {try{runE();}catch(Exception ex){System.err.println(ex);ex.printStackTrace(System.err);}}
		public void runE() {
			update_per_second();
		}
	}
	
	/**Какая из кнопок выбрана*/
	private MENU_SELECT select = MENU_SELECT.NONE;
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

	/**
	 * Create the panel.
	 */
	public Menu() {
		Configurations.menu = this;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		//Конфигурация мира
		//Рестарт
		add(makeButton("save", e-> Configurations.settings.save()));
		add(makeButton("load", e-> Configurations.settings.load()));
		//add(makeButton("search", e-> System.out.println(e)));
		add(start = makeButton("play", e -> {if (Configurations.world.isActiv())Configurations.world.stop();else Configurations.world.start();} ));
		add(record = makeButton("record", e-> record()));
		add(makeButton("graph", e-> Configurations.evolTreeDialog.setVisible(true)));
		add(makeButton("cursor", e-> toDefault()));
		add(makeButton("kill", e-> remove()));
		

		Configurations.TIME_OUT_POOL.scheduleWithFixedDelay(new WorkTask(), 100, 100, TimeUnit.MILLISECONDS);
	}

	private JButton makeButton(String name, ActionListener al) {
		
		JButton button = new JButton();
		Configurations.setIcon(button,name);
		button.setMaximumSize (new Dimension(40,20));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(al);
        button.setToolTipText(Configurations.getHProperty(Menu.class,name));
        button.setFocusable(false);
		return button;
	}

	private void remove() {
		Configurations.world.stop();
		select = MENU_SELECT.REMOVE;
		Configurations.world.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}
	private void toDefault() {
		select = MENU_SELECT.NONE;
		Configurations.world.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	/**Функция, вызываемая каждую секунду автоматически*/
	private void update_per_second() {
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
				System.out.println("Start frame");
				gifs.nextFrame(g -> Configurations.world.paintComponent(g, true));
				System.out.println("End frame");
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

	public void setCell(List<CellObject> cellObjects) {
		if(cellObjects == null) return;
		for(var cellObject : cellObjects){
			switch (select) {
				case NONE -> {}
				case REMOVE -> {
					if (cellObject instanceof AliveCell acell) {
						try {
							acell.bot2Organic();
						}catch (CellObjectRemoveException e) {}
					} else {
						cellObject.remove_NE();
					}
				}
			}
		}
	}
	
	public boolean isSelectedCell(){
		return select == MENU_SELECT.REMOVE;
	}
	
	private void record(){
		if(gifs == null) { //Запуск
			Configurations.world.stop();
			int result = javax.swing.JOptionPane.showConfirmDialog(null, 
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
			}
		} else { // Закончили
			Configurations.world.stop();
			try {gifs.close();} catch (IOException e1) {e1.printStackTrace();}
			gifs = null;
		}
	}
}
