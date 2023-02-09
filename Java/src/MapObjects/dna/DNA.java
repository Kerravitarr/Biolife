package MapObjects.dna;

import java.util.List;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import Utils.JSON;
import java.util.Arrays;

/**ДНК бота*/
public class DNA {
	/**Размер мозга*/
	public final int size;
	/**Мозг*/
	public final int [] mind;
	/**Номер выполняемой инструкции*/
	private int pc;
	
	/**
	 * Вектор прерываний:
	 * 0-(OBJECT.size()-1) по зрению. То есть если клетка не может совершить действие, потому что там стена (WALL(1)),
	 * то выполнится инструкция, адрес который записан в этом массиве на месте [1]
	 */
	public final int [] interrupts; 
	/**Флаг нахождения в прерывании*/
	private boolean activInterrupt = false;
	
	public DNA(int size){
		this.size=size;
		mind = new int[this.size];
		Arrays.fill(mind, CommandList.BLOCK_1);
		pc = 0;
		interrupts = new int[(OBJECT.size()-1)];
		for (int i = 0; i < interrupts.length; i++)
			interrupts[i] = i;
	}
	/**ДНК у нас неизменяемая, поэтому при копировании мы можем сослаться на старую версию*/
	public DNA(DNA dna){
		this.size=dna.size;
		this.mind=dna.mind;
		this.pc=dna.pc;
		interrupts = Arrays.copyOf(dna.interrupts, dna.interrupts.length);
	}
	public DNA(JSON dna) {
		this.size=dna.getI("size");
    	List<Integer> mindL = dna.getA("mind");
    	mind = new int[size];
    	for (int i = 0; i < size; i++) 
			mind[i] = mindL.get(i);
		this.pc=dna.getI("instruction");
    	List<Integer> interruptsL = dna.getA("interrupts");
		interrupts = new int[interruptsL.size()];
    	for (int i = 0; i < interrupts.length; i++) 
    		interrupts[i] = interruptsL.get(i);
	}
	/**
	 * Создаёт новую ДНК на основе существующей с заменой гена на новое значени
	 * ДНК[PC + offset] = value
	 * @param dna существующая ДНК
	 * @param offset смещение, относительно PC, в которое кладётся новая инструкция
	 * @param value новая инструкция
	 */
	private DNA(DNA dna, int offset, int value) {
		this.size=dna.size;
		this.mind = new int[dna.size];
    	System.arraycopy(dna.mind, 0, mind, 0, size);
		this.pc=dna.pc;
    	interrupts = Arrays.copyOf(dna.interrupts, dna.interrupts.length);
		//Изменение гена
		this.mind[getIndex(offset)] = value;
	}
	
	/**
	 * Прерывание
	 * @param num - его номер
	 */
	protected void interrupt(AliveCell cell, int num) {
		if(activInterrupt) return;
		activInterrupt = true;
		int stackInstr = pc; // Кладём в стек PC
		pc = interrupts[num] % size;
		for (int cyc = 0; (cyc < 15); cyc++)
			if(get().execute(cell)) break; // Выполняем программу прерывания
		pc = stackInstr; // Возвращаем из стека PC
		activInterrupt = false;
	}
	/**
	 * Возвращает индекс инструкции по смещению, относительно текущего положения PC
	 * @param offset Смещение инструкции
	 * @return PC += offset
	 */
	public int getIndex(int offset) {
		int ret = (pc + offset) % size;
        if(ret < 0)
        	ret += size;
        return ret;
	};
	/**Возвращает текущую инстуркцию*/
	public CommandDNA get() {
		return CommandList.list[mind[pc]];
	}
	/**Возвращает инстуркцию по смещению*/
	public CommandDNA get(int index) {
		return CommandList.list[mind[getIndex(index)]];
	}
	/**
	 * Передвигает указатель на команду вперёд
	 * @param offset - смещение
	 */
	public void next(int offset) {
        pc = getIndex(offset);
	}

	/**
	 * Создаёт новую ДНК на основе существующей с заменой гена на новое значени
	 * ДНК[PC + offset] = value
	 * @param offset смещение, относительно PC, в которое кладётся новая инструкция
	 * @param value новая инструкция
	 */
	public DNA update(int offset, int value) {
		return new DNA(this,offset,value);
	}
	/**
	 * Заменяет значения в текущей ДНК
	 * Функция требует максимального сосредоточения, так как ДНК у нас типа неизменяемая
	 * И такие вот приколы могут порушить весь мир
	 * ДНК[PC + offset] = value
	 * @param offset смещение, относительно PC, в которое кладётся новая инструкция
	 * @param value новая инструкция
	 */
	public void criticalUpdate(int offset, int value) {
		mind[getIndex(offset)] = value;
	}
	
	
	public JSON toJSON() {
		JSON make = new JSON();
		make.add("size", size);
		make.add("mind", mind);
		make.add("instruction", pc);
		make.add("interrupts", interrupts);
		return make;
	}
	/**
	 * Создаёт новую ДНК с удвоенным геном по адресу
	 * @param index - удваиваемый индекс, абсолютный
	 * @return Новую ДНК в которой ДНК[index] = ДНК[index+1] удваивается
	 */
	public DNA doubling(int index) {
		return doubling(index,1);
	}
	
	/**
	 * Создаёт новую ДНК с копированием гена в позиции ДНК[index] count раз
	 * @param index - удваиваемый индекс, абсолютный
	 * @param count - сколько раз его удвоить
	 * @return Новую ДНК в которой ДНК[index] = ДНК[index+1] удваивается
	 */
	public DNA doubling(int index, int count) {
		DNA ret = new DNA(size+count);
    	System.arraycopy(mind, 0, ret.mind, 0, index);
		for(var i = 0 ; i < count ; i++)
			ret.mind[index + i] = mind[index]; //Копирование гена
    	System.arraycopy(mind, index+1, ret.mind, index + 1 + count, size - 1 - index);
    	ret.pc = pc;
    	if(ret.pc > index)
    		ret.next(count); // Наш тактовый счётчик идёт дальше
		for (int i = 0; i < interrupts.length; i++) {
			if(interrupts[i] >= index) interrupts[i] = Math.max(0,interrupts[i]-count); // И все прерывания тоже сдвигаются
			else interrupts[i] = interrupts[i];
		}
		return ret;
	}
	/**
	 * Сжимает ДНК, удаляя из неё один ген
	 * @param index - удаляемый индекс
	 * @return Новую ДНК в которой ДНК[index] удаляется
	 */
	public DNA compression(int index) {
		DNA ret = new DNA(size-1);
    	System.arraycopy(mind, 0, ret.mind, 0, index);
    	System.arraycopy(mind, index+1, ret.mind, index, size - 1 - index);
    	ret.pc = pc;
    	if(ret.pc >= index)
    		ret.next(-1);
		for (int i = 0; i < interrupts.length; i++) {
			if(interrupts[i] >= index) interrupts[i] = Math.max(0,interrupts[i]-1); // И все прерывания тоже сдвигаются
			else interrupts[i] = interrupts[i];
		}
		return ret;
	}
	/**
	 * Возвращает текущий индекс инструкции
	 * @return PC
	 */
	public int getPC() {
		return pc;
	}
	/**
	 * Возвращает код инструкции в абсоютном выражении
	 * @param PC программный счётчик
	 * @param offset смещение, относительно PC
	 * @return ДНК[PC + offset]
	 */
	public int get(int PC, int offset) {
		int pos = (PC+offset) % size;
		if(pos < 0)
			pos += size;
		return mind[pos];
	}
	
	public boolean equals(Object obj, int tolerance) {
		if(this == obj) {
			return true;
		} else if (obj instanceof DNA dna) {
			if(this.mind == dna.mind) //Они ссылаются на один финальный массив - они полностью равны
				return true;
			if(this.size != dna.size) //У них разная длинна, это априорно даст разные ДНК
				return false;
			int dif = 0;
			for (int i = 0; i < dna.size && dif < 2; i++) {
				if(dna.mind[i] != dna.mind[i])
					dif++;
			}
			return dif <= tolerance;
		} else {
			return false;
		}
	}
}
