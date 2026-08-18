/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import java.io.File;
import java.io.IOException;
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
        var ret = new ArrayList<Class<? extends T>>();
        //Изменяем путь к файлу в имя пакета
        var path = packageName.replace('.', '/');
        //Пройдём по всем поддиректориям
        try {
            var res = loader.getResources(path);
            while (res.hasMoreElements()) {
                var dir = new File(URLDecoder.decode(res.nextElement().getPath(), "UTF-8"));
                //Проверяем каждый файл в каталоге, если это каталог, рекурсивно добавляем его жизнеспособные файлы
                if(dir.isDirectory()){
                    for (var file : dir.listFiles()) {
                        if (file.isDirectory()){
                            if(packageName.isEmpty())
                                ret.addAll(getClassesByClasses(who,file.getName(), loader));
                            else
                                ret.addAll(getClassesByClasses(who,packageName + (packageName.isEmpty()? "": ".") + file.getName(), loader));
                        }
                    }
                }
            }
        } catch (IOException e) {
			Logger.getLogger(Reflector.class.getName()).log(Level.SEVERE, "Не смогли загрузить файл [" + packageName + "], что очень странно", e);
        }
        //А теперь попробуем поискать классы тут
        var tmp = loader.getResource(path);
        if (tmp == null) return ret;//Нету
        var currDir = new File(tmp.getPath());
        for (final var classFile : currDir.list()) {
            if (classFile.endsWith(".class")) {
                try {
                    var add = packageName.isEmpty() ? Class.forName(classFile.substring(0, classFile.length() - 6)) : Class.forName(packageName + '.' + classFile.substring(0, classFile.length() - 6));
                    var clP = add;
					var oldclP = clP;
					do{
						oldclP = clP;
						if (who.isAssignableFrom(clP)){
							ret.add((Class<? extends T>) add);
							break;
						}
					}while((clP = clP.getSuperclass()) != oldclP && clP != null);
                } catch (java.lang.ExceptionInInitializerError | NoClassDefFoundError e) {
					System.err.println( "Не смогли загрзуть класс [" + classFile + "], хотя нашли его, что очень странно. Официальная причина: " + e.getLocalizedMessage());
                } catch (ClassNotFoundException e) {
					System.err.println( "Не смогли найти класс [" + classFile + "], что очень странно");
                }
            }
        }
        return ret;
    }
	public static <T> List<Class<? extends T>> getClassesByClasses(final Class<T> who){
        try {
            // Получаем путь к месту, где лежит этот класс
            var codeSourceUrl = who.getProtectionDomain().getCodeSource().getLocation();
            var codeSourceFile = new File(codeSourceUrl.toURI());

            if (codeSourceFile.isDirectory()) {
                // Режим IDE: это папка с классами (target/classes)
                return getClassesByClasses(who,"",Thread.currentThread().getContextClassLoader());
            } else if (codeSourceFile.isFile() && codeSourceFile.getName().endsWith(".jar")) {
                // Режим ПРОД: это реальный JAR-файл
                var ret = new ArrayList<Class<? extends T>>();
                try (var jar = new java.util.jar.JarFile(codeSourceFile)) {
                    var entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        var name = entry.getName();
                        // Ищем все файлы .class с самого корня архива
                        if (name.endsWith(".class") && !entry.isDirectory()) {
                            // Превращаем "com/example/MyClass.class" в "com.example.MyClass"
                            var classFile = name.substring(0, name.length() - 6).replace('/', '.');
                            try {
                                // Загружаем класс, вызывая его статический блок
                                ret.add((Class) Class.forName(classFile));
                            } catch (java.lang.ExceptionInInitializerError | NoClassDefFoundError e) {
                                System.err.println( "Не смогли загрзуть класс [" + classFile + "], хотя нашли его, что очень странно. Официальная причина: " + e.getLocalizedMessage());
                            } catch (ClassNotFoundException e) {
                                System.err.println( "Не смогли найти класс [" + classFile + "], что очень странно");
                            }
                        }
                    }
                }
                return ret;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
		return getClassesByClasses(who,"",Thread.currentThread().getContextClassLoader());
	}
}
