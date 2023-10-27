/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Trajectories;

import Calculations.Point;
import GUI.WorldView;
import Utils.ClassBuilder;
import Utils.JSON;
import java.awt.Graphics2D;

/**
 * Траектория движения по случайным точкам
 * @author Kerravitarr
 */
public class TrajectoryRandom extends Trajectory{
	static{
		final var builder = new ClassBuilder<TrajectoryRandom>(){
			@Override public TrajectoryRandom generation(JSON json, long version){return new TrajectoryRandom(json, version);}
			@Override public JSON serialization(TrajectoryRandom object) { return object.toJSON();}

			@Override public String serializerName() {return "Случайность";}
			@Override public Class printName() {return TrajectoryRandom.class;}

		};
		builder.addConstructor(new ClassBuilder.Constructor<TrajectoryRandom>(){
			{
				addParam(new ClassBuilder.NumberConstructorParamAdapter("super.speed", 0,500,1000,0,null));
			}
			@Override
			public TrajectoryRandom build() {
				return new TrajectoryRandom(getParam(0,Integer.class));
			}
			@Override public String name() {return "";}
		});
		Trajectory.register(builder);
	}
	/**Стартовая точка траектории. С которой мы начинаем движение*/
	private Point start;
	/**Зерно генерации, чтобы все траектории от одного начала были одинаковыми*/
	private int seed;
	
	/** * Создаёт линейную, траекторию от точки к точке.объект смещается каждый раз на 1 клетку мира
	 * @param speed скорость, в тиков на шаг
	 * 
	 */
	public TrajectoryRandom(long speed){
		super(speed);
		
	}
	protected TrajectoryRandom(JSON j, long version){
		super(j,version);
	}

	@Override
	protected Point position(long wstep) {
		throw new UnknownError("Мы сюда вообще не можем дойти...");
	}
	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		return j;
	}
	
	@Override
	public void paint(Graphics2D g, WorldView.Transforms transform) {
	}
}
