/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Calculations.Emitters;

import Calculations.Configurations;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import Utils.ClassBuilder;
import Utils.JSON;

/**
 *Самое обычное прямоугольное залежо минералов.
 * Опять ребрендинк квадртаного солнца
 * @author Kerravitarr
 */
public class MineralRectangle extends MineralAbstract {
	static{
		final var builder = new ClassBuilder<MineralRectangle>(){
			@Override public MineralRectangle generation(JSON json, long version){return new MineralRectangle(json, version);}
			@Override public JSON serialization(MineralRectangle object) { return object.toJSON();}

			@Override public String serializerName() {return "Куб";}
			@Override public Class printName() {return MineralRectangle.class;}

		};
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,MineralRectangle>("width",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getWidth()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
			@Override public Integer get(MineralRectangle who) {return who.width;}
			@Override public void setValue(MineralRectangle who, Integer value) {who.width = value; who.updateMatrix();}
		});
		builder.addParam(new ClassBuilder.NumberParamAdapter<Integer,MineralRectangle>("height",0,0,0,0,null){
			@Override public Integer getDefault() {return Configurations.getHeight()/2;}
			@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
			@Override public Integer get(MineralRectangle who) {return who.height;}
			@Override public void setValue(MineralRectangle who, Integer value) {who.height = value; who.updateMatrix();}
		});
		final var attenuation = new ClassBuilder.NumberConstructorParamAdapter("super.attenuation",0d,0d,30d,0d,null){
			@Override public Double getDefault() {return Configurations.confoguration.DIRTY_WATER;}
		};
		builder.addConstructor(new ClassBuilder.Constructor<MineralRectangle>(){
			{
				addParam(new ClassBuilder.NumberConstructorParamAdapter("super.power",1,30,100,1,null));
				addParam(attenuation);
				addParam(new ClassBuilder.MapPointConstructorParam(){
					@Override public Point getDefault() {return Point.create(Configurations.getWidth()/2, Configurations.getHeight()/2);}
					@Override public String name() {return "super.center";}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("width",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getWidth()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getWidth();}
				});
				addParam(new ClassBuilder.NumberConstructorParamAdapter("height",0,0,0,0,null){
					@Override public Integer getDefault() {return Configurations.getHeight()/2;}
					@Override public Integer getSliderMaximum() {return Configurations.getHeight();}
				});
				addParam(new ClassBuilder.BooleanConstructorParam(){
					@Override public Object getDefault() {return false;}
					@Override public String name() { return "super.isLine";}
				});
				addParam(new ClassBuilder.StringConstructorParam(){
					@Override public Object getDefault() {return "Кубик";}
					@Override public String name() { return "super.name";}
					
				});
			}

			@Override
			public MineralRectangle build() {
				return new MineralRectangle(getParam(0,Integer.class),getParam(1,Double.class),new Trajectory(getParam(2,Point.class)),  getParam(3,Integer.class), getParam(4,Integer.class),getParam(5,Boolean.class), getParam(6,String.class));
			}
			@Override public String name() {return "";}
		});
		MineralAbstract.register(builder);
	}
	
	/**Ширина излучающей поверхности*/
	private int width;
	/**Высота излучающей поверхности*/
	private int height;
	
	/**Создаёт излучающу. прямоугольниую поверхность
	 * @param p сила излучения
	 * @param attenuation затухание. На сколько единиц/клетку уменьшается количество минералов вдали от объекта
	 * @param move движение ЦЕНТРА объекта
	 * @param width ширина полоски
	 * @param height высота полоски
	 * @param isLine если true, то объекта представляет собой только излучающую поверхность
	 * @param name название залежи
	 */
	public MineralRectangle(double p, double attenuation, Trajectory move, int width, int height, boolean isLine, String name) {
		super(p, attenuation,move,name,isLine);
		this.width = width;
		this.height = height;
	}
	protected MineralRectangle(JSON j, long v){
		super(j,v);
		this.width = j.get("width");
		this.height = j.get("height");
	}

	@Override
	public double calculation(Point pos) {
		if(getAttenuation() == 0d)
			return getPower();
		//Расстояние от точки до центра нашей полосы
		final var d = pos.distance(position);
		final var absX = Math.abs(d.x);
		final var absY = Math.abs(d.y);
		if (getIsLine()) {
			if (absX <= width / 2 && absY <= height / 2) {
				//Внутри прямоугольника
				return Math.max(0, getPower() - getAttenuation() * Math.min(width / 2 - absX, height / 2 - absY));
			} else {
				//Снаружи прямоугольника
				return Math.max(0, getPower() - getAttenuation() * Math.max(absX - width / 2, absY - height / 2));
			}
		} else {
			if(absX <= width / 2 && absY <= height / 2 )
				return getPower();
			else
				return Math.max(0, getPower() - getAttenuation() * Math.max(absX - width / 2, absY - height / 2));
		}
	}

	@Override
	public JSON toJSON(){
		final var j = super.toJSON();
		j.add("width", width);
		j.add("height", height);
		return j;
	}
}
