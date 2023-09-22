/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import Calculations.GenerateClassException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс для работы с загрузчиком классов
 * @author Kerravitarr
 */
public class Reflector {
	/**Ищет всех потомков заказанного класса
	 * @param <T> 
	 * @param who класс, чьи наследники (и который сам в том числе) нужен
	 * @param packageName имя пакета, в котором ищем классы
	 * @param loader загрузчик классов
	 * @return список всех найденных потомков текущего класса в пакете
	 */
	private static <T> List<Class<? extends T>> getClassesByClasses(final Class<T> who,final String packageName, ClassLoader loader) {
        final var ret = new ArrayList<Class<? extends T>>();
        //Изменяем путь к файлу в имя пакета
        final var path = packageName.replace('.', '/');
        //Пройдём по всем поддиректориям
        try {
            final var res = loader.getResources(path);
            while (res.hasMoreElements()) {
                // Get the file path the the directory
                final var dir = new File(URLDecoder.decode(res.nextElement().getPath(), "UTF-8"));
                //Проверяем каждый файл в каталоге, если это каталог, рекурсивно добавляем его жизнеспособные файлы
                for (final var file : dir.listFiles()) {
                    if (file.isDirectory()){
						if(packageName.isEmpty())
							ret.addAll(getClassesByClasses(who,file.getName(), loader));
						else
							ret.addAll(getClassesByClasses(who,packageName + (packageName.isEmpty()? "": ".") + file.getName(), loader));
					}
                }
            }
        } catch (IOException e) {
			Logger.getLogger(Reflector.class.getName()).log(Level.SEVERE, "Не смогли загрузить файл [" + packageName + "], что очень странно", e);
        }
        //А теперь попробуем поискать классы тут
        final var tmp = loader.getResource(path);
        if (tmp == null) return ret;//Нету
        final var currDir = new File(tmp.getPath());
        for (final var classFile : currDir.list()) {
            if (classFile.endsWith(".class")) {
                try {
                    final var add = packageName.isEmpty() ? Class.forName(classFile.substring(0, classFile.length() - 6)) : Class.forName(packageName + '.' + classFile.substring(0, classFile.length() - 6));
                    var clP = add;
					do{
						if (who.isAssignableFrom(clP)){
							ret.add((Class<? extends T>) add);
							break;
						}
					}while((clP = add.getSuperclass()) != Object.class);
                } catch (NoClassDefFoundError e) {
					Logger.getLogger(Reflector.class.getName()).log(Level.SEVERE, "Не смогли загрзуть класс [" + classFile + "], хотя нашли его, что очень странно", e);
                } catch (ClassNotFoundException e) {
					Logger.getLogger(Reflector.class.getName()).log(Level.SEVERE, "Не смогли найти класс [" + classFile + "], что очень странно", e);
                }
            }
        }
        return ret;
    }
	public static <T> List<Class<? extends T>> getClassesByClasses(final Class<T> who){
		return getClassesByClasses(who,"",Thread.currentThread().getContextClassLoader());
	}
	
	/** 
	 * Создаёт реальный класс на основе JSON файла. 
	 * <br>
	 * Главное условие создание - в JSON файле должен быть параметр "_className" - название класса, который ищем
	 * <br>
	 * А сам создаваемый объект обязан иметь конструктор JSON long
	 * @param <T>
	 * @param who класс, который надо создать
	 * @param json объект, описывающий солнце
	 * @param version версия файла json в котором объект сохранён
	 * @return готовый, построенный объект на основе запроса
	 * 
	 * @throws Calculations.GenerateClassException ошибка, которая может легко возникнуть при создании класса
	 */
	public static <T> T generate(final Class<T> who, JSON json, long version) throws GenerateClassException{
		String className = json.get("_className");
		try{
			final var ac = Class.forName(className).asSubclass(who);
			var constructor = ac.getDeclaredConstructor(JSON.class, long.class);
			return constructor.newInstance(json,version);
		} catch (ClassNotFoundException ex)		{throw new GenerateClassException(ex,className);}
		catch (NoSuchMethodException ex)		{throw new GenerateClassException(ex);}
		catch (InstantiationException ex)		{throw new GenerateClassException(ex);}
		catch (IllegalAccessException ex)		{throw new GenerateClassException(ex);} 
		catch (IllegalArgumentException ex)		{throw new GenerateClassException(ex);}
		catch (InvocationTargetException ex)	{throw new GenerateClassException(ex);}
	}
}
