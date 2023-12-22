/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import Calculations.GenerateClassException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Фабрика по созданию окон загрузки и сохранения
 * @author Kerravitarr
 */
public class LoadSaveFactory {
	/**Функция сохранения*/
	public interface SaveFunction{
		/**Внутри надо обработать непосредственно сохранение*/
		public void save(String fileName) throws IOException;
	}
	/**Функция загрузки*/
	public interface LoadFunction{
		/**Внутри надо обработать непосредственно загрузку*/
		public void load(String fileName) throws IOException;
	}
	/**Ошибка в функции*/
	public interface FunctionCatch{
		/**Будет вызвана только при наличии ошибки*/
		public void error(Exception ex);
	}
	
	/**Путь для сохранения файлов*/
	private static String currentDirectoryPath = System.getProperty("user.dir");
	
	/** Создаёт окно и предлагает сохранить файл. По заврешению пишет сообщение с результатами
	 * @param title заголовок окна
	 * @param extension расширение файла без точки
	 * @param save функция, которая будет вызвана для сохранения
	 */
	public static void save(String title, String extension, SaveFunction save){
		save(title, extension,Configurations.getProperty(LoadSaveFactory.class,"save.selectTitle"), save, true, null, true);
	}
	/** Создаёт окно и предлагает сохранить файл. В случае ошибки покажет сообщение
	 * @param title заголовок окна
	 * @param extension расширение файла без точки
	 * @param save функция, которая будет вызвана для сохранения
	 * @param isPrintSaveOk печатать сообщение, что сохранение завершено?
	 */
	public static void save(String title, String extension, SaveFunction save, boolean isPrintSaveOk){
		save(title, extension,Configurations.getProperty(LoadSaveFactory.class,"save.selectTitle"), save, isPrintSaveOk, null, true);
	}
	/** Создаёт окно и предлагает сохранить файл
	 * @param title заголовок окна
	 * @param extension расширение файла без точки
	 * @param approveButtonText текст на кнопке "сохранить"
	 * @param save функция, которая будет вызвана для сохранения
	 * @param isPrintSaveOk печатать сообщение, что сохранение завершено?
	 * @param error функция, которая будет вызвана при ошибке
	 * @param isPrintSaveError печатать сообщение, что сохранение пошло по звезде?
	 */
	public static void save(String title, String extension, String approveButtonText, SaveFunction save, boolean isPrintSaveOk, FunctionCatch error, boolean isPrintSaveError){
		final var vw = Configurations.getViewer().get(WorldView.class);
		
		JFileChooser fileopen = new JFileChooser(currentDirectoryPath);
		fileopen.setFileFilter(new FileNameExtensionFilter(extension, extension));
		while(true) {
			int ret = fileopen.showDialog(vw, approveButtonText);
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
					result = JOptionPane.showConfirmDialog(vw,MessageFormat.format(Configurations.getProperty(LoadSaveFactory.class,"save.fileExist"),fileName.substring(fileName.lastIndexOf("\\")+1)), title,JOptionPane.YES_NO_CANCEL_OPTION);
				else
					result = JOptionPane.showConfirmDialog(vw,MessageFormat.format(Configurations.getProperty(LoadSaveFactory.class,"save.fileExist"),fileName), title,JOptionPane.YES_NO_CANCEL_OPTION);
				switch (result) {
					case JOptionPane.YES_OPTION-> {file.delete();}
					case JOptionPane.NO_OPTION-> {continue;}
					case JOptionPane.CANCEL_OPTION-> {return;}
				}
			}
			currentDirectoryPath = fileopen.getSelectedFile().getParent();
			try {
				save.save(fileName);
				if(isPrintSaveOk)
					JOptionPane.showMessageDialog(vw,	Configurations.getProperty(LoadSaveFactory.class,"save.ok"),	title, JOptionPane.INFORMATION_MESSAGE);
				break;
			} catch (Exception ex) {
				Logger.getLogger(LoadSaveFactory.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				if(error != null)
					error.error(ex);
				if(isPrintSaveError)
					JOptionPane.showMessageDialog(vw,	Configurations.getHProperty(LoadSaveFactory.class,"save.error") + ex,	title, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/** Создаёт окно и предлагает сохранить файл. В случае ошибки покажет сообщение
	 * @param title заголовок окна
	 * @param extension расширение файла без точки
	 * @param load функция, которая будет вызвана для загрузки
	 */
	public static void load(String title, String extension, LoadFunction load){
		load(title, extension, load, null,true);
	}
	/** Создаёт окно выбора файла загрузки
	 * @param title заголовок окна
	 * @param extension расширение файла без точки
	 * @param load функция, которая будет вызвана для загрузки
	 * @param error функция, которая будет вызвана при ошибке
	 * @param isPrintSaveError печатать сообщение, что всё пошло по звезде?
	 */
	public static void load(String title, String extension, LoadFunction load, FunctionCatch error, boolean isPrintSaveError){
		final var vw = Configurations.getViewer().get(WorldView.class);
		
		JFileChooser fileopen = new JFileChooser(currentDirectoryPath);
		fileopen.setFileFilter(new FileNameExtensionFilter(extension, extension));
		int ret = fileopen.showDialog(vw, Configurations.getProperty(LoadSaveFactory.class,"load.selectTitle"));
		if (ret == JFileChooser.APPROVE_OPTION) {
			currentDirectoryPath = fileopen.getSelectedFile().getParent();
			try {
				load.load(fileopen.getSelectedFile().getPath());
			} catch (IOException ex) {
				Logger.getLogger(LoadSaveFactory.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				if(error != null)
					error.error(ex);
				if(isPrintSaveError)
					JOptionPane.showMessageDialog(vw,	Configurations.getHProperty(LoadSaveFactory.class,"load.error") + ex,	title, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
