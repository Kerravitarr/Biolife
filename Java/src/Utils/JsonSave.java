package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Класс для сохранения и загрузки объекта в файл в виде текстовой информации
 * Главной папкой по умолчанию считается папка где запущен файл
 * @author Keravitarr
 *
 */
public class JsonSave {
	/**
	 * Интерфейс серелизации
	 */
	public interface SerializationOld{
		/**Функция записывает в поток строку в красивом исполнении*/
		public void toBeautifulJSONString(Writer writer) throws IOException;
		/** Функция записывает в поток информацию в виде одной строки */
		public void toJSONString(Writer writer) throws IOException;
		/** Разбирает объект из потока */
		public void parse(Reader reader) throws IOException;
	}
	
	/**
	 * Интерфейс серелизации
	 */
	public interface Serialization{
		/**Вернуть имя объекта серилизации*/
		public String getName();
		/**Функция записывает в поток строку в красивом исполнении*/
		public void toBeautifulJSONString(Writer writer) throws IOException;
		/** Функция записывает в поток информацию в виде одной строки */
		public void toJSONString(Writer writer) throws IOException;
		/** Разбирает объект из потока */
		public void parse(Reader reader, long version) throws IOException;
	}
	
	private class VERSION extends JsonSave.JSONSerialization{
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

		@Override
		public void setJSON(JSON json, long version) {
			version = json.getL("VERSION");
		}
	}
	
	public static abstract class JSONSerialization implements Serialization{
		/**Вернуть имя объекта серилизации*/
		public abstract String getName();
		/**Вернуть имя объекта серилизации*/
		public abstract JSON getJSON();
		/**Вернуть имя объекта серилизации*/
		public abstract void setJSON(JSON json, long version);
		@Override
		public void toBeautifulJSONString(Writer writer) throws IOException {
			getJSON().toBeautifulJSONString(writer);
		}

		@Override
		public void toJSONString(Writer writer) throws IOException {
			getJSON().toJSONString(writer);
		}
		/** Разбирает объект из потока */
		public void parse(Reader reader, long version) throws IOException{
			setJSON(new JSON(reader), version);
		}
	}
	
	private final String path;
	/**Название проекта/окна*/
	private final String projectName;
	/**Название проекта/окна*/
	private final String extension;
	/**версия проекта/окна*/
	private final long version;
	
	public JsonSave(String projectName,String extension, long v){
		path = System.getProperty("user.dir");
		this.projectName=projectName;
		this.extension=extension;
		version=v;
	}
	
	/**
	 * Создаёт диалоговое окно выбора файла сохранения
	 * @param sers объекты, которые надо сохранить
	 * @param isBeautiful красивое сохранине или в одну строчку
	 * @return true, если сохранение завершилось успешно
	 */
	public boolean save(boolean isBeautiful, Serialization ... sers) {
		JFileChooser fileopen = new JFileChooser(path);
		fileopen.setFileFilter(new FileNameExtensionFilter(extension, extension));
		while(true) {
			int ret = fileopen.showDialog(null, "Сохранить файл");
			if (ret != JFileChooser.APPROVE_OPTION) return false;
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
					result = JOptionPane.showConfirmDialog(null,"Файл " + fileName.substring(fileName.lastIndexOf("\\")+1) + " существует, перзаписать? ", projectName,JOptionPane.YES_NO_CANCEL_OPTION);
				else
					result = JOptionPane.showConfirmDialog(null,"Файл " + fileName + " существует, перзаписать? ", projectName,JOptionPane.YES_NO_CANCEL_OPTION);
				switch (result) {
					case JOptionPane.YES_OPTION-> {file.delete();}
					case JOptionPane.NO_OPTION-> {continue;}
					case JOptionPane.CANCEL_OPTION-> {return false;}
				}
			}
			if(save(fileName,isBeautiful,sers)){
				JOptionPane.showMessageDialog(null,	"Сохранение заверешно",	projectName, JOptionPane.INFORMATION_MESSAGE);
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Сохраняет объекты в определённый файл
	 * @param pathToFile - файл в который будет произведено сохранение
	 * @param isBeautiful красивое сохранине или в одну строчку
	 * @param sers объекты, которые надо сохранить
	 * @return true, если сохранение завершилось успешно
	 */
	public boolean save(String pathToFile,boolean isBeautiful, Serialization ... sers){
		try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(pathToFile))) {
			java.io.OutputStreamWriter out = new OutputStreamWriter(zout);
			var v = new VERSION();
			save(zout,out, v,isBeautiful);
			for(var s : sers){
				save(zout,out, s,isBeautiful);
			}
			return true;
		} catch (IOException | java.lang.RuntimeException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null,	"Ошибка сохранения!\n" + e1.getMessage(), projectName, JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private void save(ZipOutputStream zout,Writer w, Serialization s, boolean isBeautiful) throws IOException{
		ZipEntry entry1 = new ZipEntry(s.getName() + ".json");
		zout.putNextEntry(entry1);
		 // добавляем содержимое к архиву
		if (isBeautiful)
			s.toBeautifulJSONString(w);
		else
			s.toJSONString(w);
		w.flush();
		// закрываем текущую запись для новой записи
		zout.closeEntry();
	}
	
	/**
	 * Загружает JSON из этого файла
	 * @param pathToFile - файл с объектами
	 * @param obj - объект, который уже создан и осталось только парсировать файл
	 * @return true есди загрузка удалась
	 */
	public boolean load(String pathToFile, SerializationOld obj) {
		try ( FileReader reader = new FileReader(pathToFile)) {
			obj.parse(reader);
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "<html>Ошибка загрузки!<br>" + e1.getMessage(), projectName, JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	/**
	 * Загружает объект из определеённного файла
	 * @param pathToFile - файл с объектами
	 * @param sers - объекты, которые нужно прочитать
	 * @return true есди загрузка удалась
	 */
	public boolean load(String pathToFile, Serialization ... sers) {
		var v = new VERSION();
		if(!load(pathToFile, v)) return false;
		for(var s : sers){
			if(!load(pathToFile, s)) return false;
		}
		return true;
	}
	/**
	 * Создаёт окно и предлагает загрузить файл
	 * @return путь к файлу загрузки или null
	 */
	public String load(){
		JFileChooser fileopen = new JFileChooser(path);
		fileopen.setFileFilter(new FileNameExtensionFilter(extension, extension));
		int ret = fileopen.showDialog(null, "Загрузить файл");
		if (ret == JFileChooser.APPROVE_OPTION) {
			return fileopen.getSelectedFile().getPath();
		} else {
			return null;
		}
	}
	
	private boolean load(String path, Serialization obj){
		try (ZipInputStream zin = new ZipInputStream(new FileInputStream(path))) {
			ZipEntry entry;
			String name = obj.getName() + ".json";
			while ((entry = zin.getNextEntry()) != null) {
				if (entry.getName().equals(name)) {
					try (java.io.InputStreamReader input = new InputStreamReader(zin)) {
						obj.parse(input,version);
					}
					return true;
				}
				zin.closeEntry();
			}
			JOptionPane.showMessageDialog(null, "<html>Ошибка загрузки!<br>Не найден файл " + name, projectName, JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "<html>Ошибка загрузки!<br>" + e1.getMessage(), projectName, JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
}
