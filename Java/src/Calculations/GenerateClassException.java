/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations;

import java.lang.reflect.InvocationTargetException;

/**Исключение, возникающее в следствии ошибки в загрузке класса
 * @author Kerravitarr
 */
public class GenerateClassException extends Exception{
	
	public GenerateClassException(ClassNotFoundException ex, String className){super("Не удалось обнаружить класс " + className,ex);}
	public GenerateClassException(NoSuchMethodException ex){super(ex);}
	public GenerateClassException(InstantiationException ex){super(ex);}
	public GenerateClassException(IllegalAccessException ex){super(ex);}
	public GenerateClassException(IllegalArgumentException ex){super(ex);}

	public GenerateClassException(InvocationTargetException ex) {
		super(ex);
		setStackTrace(ex.getCause().getStackTrace());
	}
	
	@Override
	public String toString(){
		if(getCause() instanceof InvocationTargetException ie){
			return ie.getCause().toString();
		} else {
			return getCause().toString();
		}
	}
}
