package start;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.UIManager;
import Calculations.Configurations;
import Calculations.GenerateClassException;
import GUI.MainFrame;
import GUI.WithoutGUI;
import java.io.IOException;
import java.util.regex.Pattern;

public class BioLife{
	/**Паттерен для проверки валидности ip адреса*/
	private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	
	/**Точка входа в приложение
	 * @param args аргументы командной строки
	 * @throws java.io.IOException
	 * @throws Calculations.GenerateClassException
	 */
	public static void main(String[] args) throws IOException, GenerateClassException {
		final var OS = System.getProperty("os.name").toLowerCase();
		if(OS.contains("win")){
			//Винда, долбанная, имеет консоль по умолчанию не настроенную на UTF-8. Сколько я с этим намучился!!!
			try {
				System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out), true, "UTF-8"));
				System.setErr(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.err), true, "UTF-8"));
			} catch (java.io.UnsupportedEncodingException e) {
				System.out.println("App not support encoding UTF-8");
			}
		}		
		final var _opts = new Utils.CMDOptions(args);
		_opts.add(new Utils.CMDOptions.Option('V',getProperty("V")));
		_opts.add(new Utils.CMDOptions.Option('W',100,700,null,1d,getProperty("V")));
		_opts.add(new Utils.CMDOptions.Option('H',100,700,null,1d,getProperty("H")));
		_opts.add(new Utils.CMDOptions.Option('h',getProperty("h")));
		_opts.add(new Utils.CMDOptions.Option('L',"",getProperty("L")));
		_opts.add(new Utils.CMDOptions.Option('p',1,8080,65535,1d,getProperty("p")));
		//Обработка опций
		var print_help = _opts.get('h').get(Boolean.class) ? (false ? 1 : 2) : 0;
		boolean isNeedHelp = print_help != 0;
		if (isNeedHelp) {
			if(print_help == 1)
				System.out.println(getProperty("cmd.params.now"));
			else
				System.out.println(getProperty("cmd.params.def"));
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
		//Рефлексия нужна, чтобы отработали статические методы даже в тех классах, на которые нет пути отсюда, из этой точки старта.
		Utils.Reflector.getClassesByClasses(BioLife.class);
		//Создаём случайный мир
		final var defType = Configurations.WORLD_TYPE.values[Utils.Utils.random(0, Configurations.WORLD_TYPE.length - 1)];
		final var load = _opts.get('L').get(String.class);
		if(!!_opts.get('V').get(Boolean.class)){
			//С графической частью
			final var  sSize = Configurations.getDefaultConfiguration(defType);
			Configurations.makeDefaultWord(defType,sSize.MAP_CELLS.width, sSize.MAP_CELLS.height);
			if(!load.isEmpty())
				Configurations.load(load);

			
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
			if(!load.isEmpty())
				Configurations.load(load);
			
			WithoutGUI.start(_opts.get('L').get(String.class),_opts.get('p').get(Integer.class));
			//Configurations.world.start();
			
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
	
	private static String getProperty(String name){
		return Configurations.getProperty(BioLife.class, name);
	}
	/**Проверяет валидность ip адреса*/
	public static boolean validate(final String ip) {
		return PATTERN.matcher(ip).matches();
	}
}
