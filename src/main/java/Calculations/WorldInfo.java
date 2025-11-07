package Calculations;

import java.util.Arrays;

import MapObjects.CellObject;
import MapObjects.CellObject.LV_STATUS;

/**Информация по игровому полю на текущий момент с возможностью записи истории */
public class WorldInfo {
	///Целочисленные параметры мира
	private final int[] ints = new int[CellObject.LV_STATUS.length];
	///Не целочисленные параметры мира
	private final double [] doubles = new double[0];
	///История мира в числах
	private final kerlib.ZipBuffer<?>[] worldHistory;

	///Создать объект информации по миру
	/// @param isAddHistory - добавлять ли историю? Если да, то каждый раз при вызове reset() история тоже будет обновляться
    public WorldInfo(boolean isAddHistory) {
        reset();
		if(isAddHistory){
			worldHistory = new kerlib.ZipBuffer[ints.length + doubles.length];
			for(var i = 0 ; i < ints.length ; i++){
				var val = kerlib.ZipBuffer.asIn(1000,1);
				worldHistory[i] = val;
				val.next(kerlib.ZipBuffer.asIn(1000, 10))
					.next(kerlib.ZipBuffer.asIn(1000, 10))
					.next(kerlib.ZipBuffer.asIn(1000, 10))
					.next(kerlib.ZipBuffer.asIn(1000, 10))
					.next(kerlib.ZipBuffer.asIn(1000, 10));
			}
			for(var i = 0 ; i < doubles.length ; i++){
				var val = kerlib.ZipBuffer.asDouble(1000,1);
				worldHistory[i] = val;
				val.next(kerlib.ZipBuffer.asDouble(1000, 10))
					.next(kerlib.ZipBuffer.asDouble(1000, 10))
					.next(kerlib.ZipBuffer.asDouble(1000, 10))
					.next(kerlib.ZipBuffer.asDouble(1000, 10))
					.next(kerlib.ZipBuffer.asDouble(1000, 10));
			}
		}else{
			worldHistory = null;
		}
    }
	///Создать объект информации по миру без истории
	public WorldInfo(){this(false);}
    
	///Очистить информацию, чтобы можно было заново её заполнить
	public void reset(){
		if(worldHistory != null){
			for(var i = 0 ; i < ints.length ; i++)
				((kerlib.ZipBuffer<Integer>)worldHistory[i]).add(ints[i]);
			for(var i = 0 ; i < doubles.length ; i++)
				((kerlib.ZipBuffer<Double>)worldHistory[i+ints.length]).add(doubles[i]);
		}
		Arrays.fill(ints, 0);
		Arrays.fill(doubles, 0);
	}
	///Добавить информацию о текущем объекте в набор
	public void add(CellObject o){
		++ints[o.getAlive().ordinal()];
	}
	///Возвращает количество элементов заданного типа
	public int get(LV_STATUS type) {
		return ints[type.ordinal()];
	}
	///Объединить два набора информации
    public void add(WorldInfo worldInfo) {
		for(var i = 0 ; i < ints.length ; i++)
			ints[i] += worldInfo.ints[i];
		for(var i = 0 ; i < doubles.length ; i++)
			doubles[i] += worldInfo.doubles[i];
    }
	///Объединить два набора информации
    public void copy(WorldInfo worldInfo) {
        System.arraycopy(worldInfo.ints, 0, ints, 0, ints.length);
        System.arraycopy(worldInfo.doubles, 0, doubles, 0, doubles.length);
		if(worldHistory != null){
			for(var i = 0 ; i < ints.length ; i++)
				((kerlib.ZipBuffer<Integer>)worldHistory[i]).add(ints[i]);
			for(var i = 0 ; i < doubles.length ; i++)
				((kerlib.ZipBuffer<Double>)worldHistory[i+ints.length]).add(doubles[i]);
		}
    }

    @Override
    public String toString() {
        return Arrays.toString(ints) + "," + Arrays.toString(doubles);
    }
}
