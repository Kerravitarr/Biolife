package start;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.UIManager;
import Calculations.Configurations;
import static Calculations.Configurations.world;
import GUI.MainFrame;
import MapObjects.CellObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;

public class BioLife{
	
	/**Точка входа в приложение
	 * @param args аргументы командной строки
	 */
	public static void main(String[] args) {
		final var _opts = new Utils.CMDOptions(args);
		_opts.add(new Utils.CMDOptions.Option('V',"Не запускать GUI, приложение останется в консольном варианте"));
		_opts.add(new Utils.CMDOptions.Option('W',100,700,10000,1d,"Ширина мира для запуска без GUI"));
		_opts.add(new Utils.CMDOptions.Option('H',100,700,10000,1d,"Высота мира для запуска без GUI"));
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
		//Создаём мир
		final var defType = Configurations.WORLD_TYPE.LINE_V;
		if(!_opts.get('V').get(Boolean.class)){
			//С графической частью
			final var  sSize = Configurations.getDefaultConfiguration(defType);
			Configurations.makeDefaultWord(defType,sSize.MAP_CELLS.width, sSize.MAP_CELLS.height);

			
			//Обработка переменных окружения
			//Настраиваем буковки
			Configurations.defaultFont = new java.awt.Font("Default", Font.BOLD, 12);
			Configurations.smalFont = Configurations.defaultFont.deriveFont(10f);
			setUIFont(new javax.swing.plaf.FontUIResource(Configurations.defaultFont));
			
			EventQueue.invokeLater(() -> {try {new MainFrame().setVisible(true);} catch (Exception e) {e.printStackTrace();}});
			Configurations.world.start();
		} else {
			//Только с матаном
			Configurations.makeDefaultWord(defType,_opts.get('W').get(Integer.class), _opts.get('H').get(Integer.class));
			Configurations.world.start();
			start();
		}
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
	/**Функция управления миром через консоль */
	private static void start() {
		printTitle();
		System.out.println(Configurations.getProperty(BioLife.class,"help"));
		
		try(final var reader = new BufferedReader(new InputStreamReader(System.in));){
			var lut = System.currentTimeMillis() / 1000;
			var lastSave = 0l;
			while(true){
				var nut = System.currentTimeMillis() / 1000;
				if(reader.ready()){
					final var ch = reader.readLine();
					switch (ch) {
						case "p","P" -> {
							if(Configurations.world.isActiv()) {
								Configurations.world.stop();
								printTitle();
							} else {
								Configurations.world.start();
							}
						}
						default -> {
							System.out.println(Configurations.getProperty(BioLife.class,"help"));
						}
					}
				}
				if(nut - lut > 10){ //Каждые 60 с
					if(Configurations.world.isActiv()){
						printTitle();
						//Автосохранение
						if(Math.abs(world.step - lastSave) > Configurations.confoguration.SAVE_PERIOD){
							var list = new File[Configurations.confoguration.COUNT_SAVE];
							for(var i = 0 ; i < Configurations.confoguration.COUNT_SAVE ; i++){
								list[i] = new File("autosave" + (i+1) + ".zbmap");
							}
							var save = list[0];
							for(var i = 1 ; i < Configurations.confoguration.COUNT_SAVE && save.exists(); i++){
								if(!list[i].exists() || save.lastModified() > list[i].lastModified())
									save = list[i];
							}
							Configurations.save(save.getName());
						}
						lastSave = world.step;
					}
					lut = nut;
				}
				Utils.Utils.pause(1);
			}
		} catch(IOException ex){
			System.out.println(ex);
			System.out.println("Закончили работу...");
		}
	}
	
	private static void printTitle(){
		System.out.println("\n-------------------");
		String title = MessageFormat.format(Configurations.getProperty(BioLife.class,"title"), world.step,
				world.pps.FPS(), world.getCount(CellObject.LV_STATUS.LV_ALIVE), world.getCount(CellObject.LV_STATUS.LV_ORGANIC),
				world.getCount(CellObject.LV_STATUS.LV_POISON), world.getCount(CellObject.LV_STATUS.LV_WALL), world.isActiv() ? ">" : "||");
		System.out.println(title);
        System.out.println("-------------------\n");
	}
}
