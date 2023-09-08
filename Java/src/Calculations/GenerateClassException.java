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
	/**Дополнительный текст сообщения*/
	public StringBuilder addTextToMasg;
	
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
	/**Добавляет текст к ошибке, возможно, для уточнения
	 * @param msg часть текста, которая будет вставленна после основного сообщения
	 */
	public void addMsg(String msg){
		if(addTextToMasg == null)
			addTextToMasg = new StringBuilder(super.getMessage());
		addTextToMasg.append("\n").append(msg);
	}
	@Override
	public String getMessage(){
		if(addTextToMasg == null)
			return super.getMessage();
		else
			return addTextToMasg.toString();
	}
}
