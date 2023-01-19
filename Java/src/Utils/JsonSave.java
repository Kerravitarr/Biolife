package Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

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
	public interface Serialization{
		/**Функция записывает в поток строку в красивом исполнении*/
		public void toBeautifulJSONString(Writer writer) throws IOException;
		/** Функция записывает в поток информацию в виде одной строки */
		public void toJSONString(Writer writer) throws IOException;
		/** Разбирает объект из потока */
		public void parse(Reader reader) throws IOException;
		
	}
	
	private final String path;
	/**Название проекта/окна*/
	private final String projectName;
	/**Название проекта/окна*/
	private final String extension;
	
	public JsonSave(String projectName,String extension){
		path = System.getProperty("user.dir");
		this.projectName=projectName;
		this.extension=extension;
	}

	/**
	 * Создаёт диалоговое окно и сохраняет файл
	 * @param obj объект, который надо сохранить
	 * @param isBeautiful красивое сохранине или в одну строчку
	 * @return true, если файл сохранён
	 */
	public boolean save(Serialization obj, boolean isBeautiful) {
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
			try(FileWriter writer = new FileWriter(fileName, true)){
				if(isBeautiful)
					obj.toBeautifulJSONString(writer);
				else
					obj.toJSONString(writer);
				writer.flush();
				JOptionPane.showMessageDialog(null,	"Сохранение заверешно",	projectName, JOptionPane.INFORMATION_MESSAGE);
				return true;
			} catch (IOException | java.lang.RuntimeException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null,	"Ошибка сохранения!\n" + e1.getMessage(), projectName, JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}
	}
	/**
	 * Создаёт окно выбора JSON объекта и загружает объект
	 * @param obj - объект, который уже создан и осталось только парсировать файл
	 * @return true есди загрузка удалась
	 */
	public boolean load(Serialization obj) {
		JFileChooser fileopen = new JFileChooser(path);
		fileopen.setFileFilter(new FileNameExtensionFilter(extension, extension));
		int ret = fileopen.showDialog(null, "Загрузить файл");
		if (ret == JFileChooser.APPROVE_OPTION) {
			try ( FileReader reader = new FileReader(fileopen.getSelectedFile().getPath())) {
				obj.parse(reader);
				return true;
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "<html>Ошибка загрузки!<br>" + e1.getMessage(), projectName, JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} else {
			return false;
		}
	}
}
