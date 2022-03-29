package Utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import Utils.JSON.ParseException;

/**
 * Класс для сохранения и загрузки объекта в файл в JSON
 * Главной папкой по умолчанию считается папка где запущен файл
 * @author rjhjk
 *
 */
public class JsonSave {
	
	private String path;
	/**Название проекта/окна*/
	private String projectName;
	
	public JsonSave(String projectName){
		path = System.getProperty("user.dir");
		this.projectName=projectName;
	}

	/**
	 * Создаёт диалоговое окно и сохраняет файл
	 * @param json объект, который надо сохранить
	 * @param isBeautiful красивое сохранине или в одну строчку
	 * @return true, если файл сохранён
	 */
	public boolean save(JSON json, boolean isBeautiful) {
		JFileChooser fileopen = new JFileChooser(path);
		fileopen.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
		int ret = fileopen.showDialog(null, "Сохранить файл");
		if (ret != JFileChooser.APPROVE_OPTION) return false;
		String fileName = fileopen.getSelectedFile().getPath();
		if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
			if(!fileName.substring(fileName.lastIndexOf(".")+1).equals("json"))
				fileName += ".json";
		} else {
			fileName += ".json";
		}
		try(FileWriter writer = new FileWriter(fileName, true)){
			if(isBeautiful)
				json.toBeautifulJSONString(writer);
			else
				json.toJSONString(writer);
			writer.flush();
			JOptionPane.showMessageDialog(null,	"Сохранение заверешно",	projectName, JOptionPane.INFORMATION_MESSAGE);
			return true;
		} catch (IOException | java.lang.RuntimeException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null,	"Ошибка сохранения!\n" + e1.getMessage(), projectName, JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	/**
	 * Создаёт окно выбора JSON объекта и загружает объект
	 * @return
	 */
	public JSON load() {
		JFileChooser fileopen = new JFileChooser(path);
		fileopen.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
		int ret = fileopen.showDialog(null, "Выбрать файл");
		if (ret == JFileChooser.APPROVE_OPTION) {
			try(FileReader reader = new FileReader(fileopen.getSelectedFile().getPath())){
				return new JSON(reader);
			} catch (IOException | java.lang.RuntimeException | ParseException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null,	"<html>Ошибка загрузки!<br>" + e1.getMessage(),	projectName, JOptionPane.ERROR_MESSAGE);
			} 
		}
		return null;
	}
}
