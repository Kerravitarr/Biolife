package Utils;

import Calculations.GenerateClassException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * Класс для сохранения и загрузки объекта в файл в виде текстовой информации
 * Главной папкой по умолчанию считается папка где запущен файл
 * @author Keravitarr
 *
 */
public class SaveAndLoad {	
	/**Интерфейс серелизации*/
	public static interface Serialization{
		/** * Вернуть имя объекта серилизации.По этому имени будет создан файл для каждого объекта серелизации
		 * @return имя объекта
		 */
		public String getName();
		/**Преобразовать объект в JSON представление
		 * @return объект JSON, из которого потом можно собрать объект обратно
		 */
		public JSON getJSON();
	}
	public static interface Deserialization<T>{
		/**Создать объект на оснвое переданного JSON
		 * @param json объект, из которого создаём текущий
		 * @param version версия файла, чтобы следить за измененями
		 * @throws Calculations.GenerateClassException при желании можно кинуть исключение, означающее, что генерация объекта пошла по звезде
		 */
		public T fromJSON(JSON json, long version) throws GenerateClassException;
	}
	public static abstract class JSONSerialization<T> implements Serialization, Deserialization  {
		/**Обзятаельный конструктор для класса серелизации
		 * @param json
		 * @param version 
		 */
		protected JSONSerialization(JSON json, long version){}
		@Override
		public T fromJSON(JSON json, long version) throws GenerateClassException{
			try {
				return (T) this.getClass().getDeclaredConstructor(JSON.class, long.class).newInstance(json, version);
			} catch (NoSuchMethodException ex) { throw new GenerateClassException(ex);}
			catch (InstantiationException ex) {throw new GenerateClassException(ex);}
			catch (IllegalAccessException ex) { throw new GenerateClassException(ex);}
			catch (IllegalArgumentException ex) { throw new GenerateClassException(ex);}
			catch (InvocationTargetException ex) { throw new GenerateClassException(ex);}
		}
	}
	/**Событие, происходящее при сохранении/загрузке файла*/
	public interface SaveLoadListener extends EventListener {
		/**Событие*/
		public static class Event{
			public static enum TYPE {SAVE,LOAD}
			/**Тип события*/
			public final TYPE type;
			/**Сколько всего файлов нужно обработать*/
			public final int all;
			/**Какой по счёту файл в работе*/
			public final int now;
			/**Сколько прошло времени от начала действия, мс*/
			public final long allMC;
			/**Сколько прошло времени на предыдущее действие, мс*/
			public final long lastMC;
			public Event(TYPE t, int a, int n, long am, long lm){type = t; all=a; now=n;allMC=am;lastMC=lm;}
			/**Возвращает сколько процентов выполнено
			 * @return число в интервале [0;1]
			 */
			public double getProgress(){return ((double)now ) / all;}
			/**Возвращает сколько времени ещё нужно на все действия
			 * @return число милесекунд до завершения
			 */
			public double getTime(){if(now == 0) return 0; else return allMC / getProgress() - allMC;}
		}
		public void event(Event e);
	}
	
	/**Загручзчик объектов из памяти компьютера*/
	public static final class Loader extends Listener{
		/**версия проекта/окна*/
		private VERSION version = new VERSION(0);
		/**Путь к файлу из которого будем загружать данные*/
		private final String path;
		private Loader(String p) throws IOException{
			path = p;
			try {
				version = load((j,v) -> new VERSION(j,v), version.getName());
			} catch (GenerateClassException ex) {
				Logger.getLogger(SaveAndLoad.class.getName()).log(Level.SEVERE, null, ex);
				throw new UnknownError("Недостижимая часть кода");
			}
		}
		/**Загружает один конкретный объект из файла
		 * @param <T> тип объекта, который у нас должен получиться
		 * @param factory фабрика, которая сгенерирует нужный объект
		 * @param name имя этого объекта
		 * @return загружаемый объект. Если такого объекта нет - вернётся null
		 * @throws java.io.IOException мы работем с файловой системой, ошибки неизбежны
		 * @throws Calculations.GenerateClassException возникает, когда происходят ошибки создания объекта
		 */
		public <T> T load(Deserialization<T> factory, String name) throws IOException, GenerateClassException{
			try (ZipInputStream zin = new ZipInputStream(new FileInputStream(path))) {
				ZipEntry entry;
				name += ".json";
				while ((entry = zin.getNextEntry()) != null) {
					if (entry.getName().equals(name)) {
						try (java.io.InputStreamReader input = new InputStreamReader(zin)) {
							return factory.fromJSON(new JSON(input), version.version);
						}
					}
					zin.closeEntry();
				}
				return null;
			}
		}

		/**Загружает один конкретный объект из файла
		 * @param <T> тип объекта, который у нас должен получиться
		 * @param factory фабрика, которая сгенерирует нужный объект
		 * @return 
		 * @throws java.io.IOException 
		 */
		public <T> T load(JSONSerialization<T> factory) throws IOException, GenerateClassException{
			return (T) load(factory,factory.getName());
		}
	}
	/**Сохранитель объектов в память */
	public static final class Saver extends Listener{
		/**Путь к файлу из которого будем загружать данные*/
		private final String path;
		/**версия проекта/окна*/
		private final VERSION version;
		
		private Saver(String p, long v){
			path = p;
			version = new VERSION(v);
		}
		/**
		 * Сохраняет объекты в определённый файл
		 * @param sers объекты, которые надо сохранить
		 * 
		 * @throws java.io.IOException возникает при невозможности сохраниться
		 */
		public void save(Serialization ... sers) throws IOException{
			save( true,sers);
		}
		/**
		 * Сохраняет объекты в определённый файл
		 * @param isBeautiful красивое сохранине или в одну строчку
		 * @param sers объекты, которые надо сохранить
		 * 
		 * @throws java.io.IOException возникает при невозможности сохраниться
		 */
		public void save(boolean isBeautiful, Serialization ... sers) throws IOException{
			var startMC = System.currentTimeMillis();
			var prefMC = startMC;
			try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(path))) {
				java.io.OutputStreamWriter out = new OutputStreamWriter(zout);
				save(zout,out, version,isBeautiful);
				for (int i = 0; i < sers.length; i++) {
					var nmc = System.currentTimeMillis();
					dispatchEvent(new SaveLoadListener.Event(SaveLoadListener.Event.TYPE.SAVE, sers.length,i,nmc - startMC,nmc - prefMC));
					prefMC = nmc;
					final var s = sers[i];
					save(zout,out, s,isBeautiful);
				}
				var nmc = System.currentTimeMillis();
				dispatchEvent(new SaveLoadListener.Event(SaveLoadListener.Event.TYPE.SAVE, sers.length,sers.length,nmc - startMC,nmc - prefMC));
			}
		}
		/**Сохраняет один конкретный объект в определённый поток
		 * @param zout
		 * @param w
		 * @param s
		 * @param isBeautiful
		 * @throws IOException 
		 */
		private void save(ZipOutputStream zout,Writer w, Serialization s, boolean isBeautiful) throws IOException{
			ZipEntry entry1 = new ZipEntry(s.getName() + ".json");
			zout.putNextEntry(entry1);
			 // добавляем содержимое к архиву
			if (isBeautiful)
				s.getJSON().toBeautifulJSONString(w);
			else
				s.getJSON().toJSONString(w);
			w.flush();
			// закрываем текущую запись для новой записи
			zout.closeEntry();
		}
	}
	/**Слушатель и генератор событий*/
	private static class Listener {
		/**список слушателей событий загрузки*/
		private final List<SaveLoadListener> listeners = new ArrayList<>();
		
		/**Добавить слушателя на событие сохранения/загрузки
		 * @param l слушатель, который будет получать уведомления по мере прогресса
		 */
		public void addActionListener(SaveLoadListener l){
			listeners.add(l);
		}
		/**Рассылает уведомление о ходе работы всем слушателям
		 * @param e уведомление о состоянии
		 */
		public void dispatchEvent(SaveLoadListener.Event e){
			for(var l : listeners)
				l.event(e);
		}
	}
	
	private static class VERSION extends SaveAndLoad.JSONSerialization<VERSION>{
		/**версия проекта/окна*/
		private final long version;
		
		public VERSION(long v) {super(null,v); version = v;};
		public VERSION(JSON json, long v) {
			super(json,v);
			version = json.getL("VERSION");
		}
		@Override
		public String getName() {
			return "VERSION";
		}

		@Override
		public JSON getJSON() {
			JSON make = new JSON();
			make.add("VERSION", version);
			return make;
		}
	}
	/**Получить загрузчик файлов
	 * @param puth
	 * @return объект, который умеет загружать
	 * @throws java.io.IOException ну тут всё просто - мы читаем файл, вот и ошибки чтения
	 */
	public static Loader load(String puth) throws IOException{
		return new Loader(puth);
	}
	/**Получить загрузчик файлов
	 * @param puth путь к файлу сохранения
	 * @param version версия файла сохранения
	 * @return объект, который умеет сохранять
	 * @throws java.io.IOException 
	 */
	public static Saver save(String puth, long version) throws IOException{
		return new Saver(puth, version);
	}
}
