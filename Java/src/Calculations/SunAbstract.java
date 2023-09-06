package Calculations;

import GUI.AllColors;
import GUI.WorldView.Transforms;
import Utils.JSON;
import Utils.SaveAndLoad;
import java.awt.Graphics2D;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Болванка солнышка. Любое солнце должно быть похоже на это!
 * @author Илья
 *
 */
public abstract class SunAbstract extends DefaultEmitter{

	/**Создаёт солнце
	 * @param p максимальная энергия солнца, будто и не было тени
	 * @param move форма движения солнца. Доступны изначально LineMove и EllipseMove
	 * @param n название солнца
	 */
	public SunAbstract(double p, Trajectory move, String n){
		super(p,move,n);
	}
	/**
	 * Возвращает количество солнечной энергии в этой точке пространства
	 * @param pos позиция в пространстве
	 * @return количество энергии.
	 */
	public abstract double getEnergy(Point pos);
	
	public static SunAbstract generate(JSON json){
		String className = json.get("_className");
		try {
			Class<? extends SunAbstract> ac = Class.forName(className).asSubclass(SunAbstract.class);
			var constructor = ac.getDeclaredConstructor(JSON.class);
			return constructor.newInstance(json);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			Logger.getLogger(SunAbstract.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
