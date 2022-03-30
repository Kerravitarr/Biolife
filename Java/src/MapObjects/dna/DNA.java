package MapObjects.dna;

import java.util.List;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import Utils.JSON;

/**ДНК бота*/
public class DNA {
	/**Размер мозга*/
	public final int size;
	/**Мозг*/
	public final int [] mind;
	/**Номер выполняемой инструкции*/
	private int instruction;
	
	/**
	 * Вектор прерываний:
	 * 0-(OBJECT.size()-1) по зрению. То есть если клетка не может совершить действие, потому что там стена (WALL(1)),
	 * то выполнится инструкция, адрес который записан в этом массиве на месте [1]
	 */
	public final int [] interrupts = new int[(OBJECT.size()-1)]; 
	/**Флаг нахождения в прерывании*/
	private boolean activInterrupt = false;
	
	public DNA(int size){
		this.size=size;
		mind = new int[this.size];
		for (int i = 0; i < mind.length; i++) {
			mind[i] = CommandList.block1; // У клетки по базе только одна функция - фотосинтез
		}
		instruction = 0;
		for (int i = 0; i < interrupts.length; i++)
			interrupts[i] = 0;
	}
	/**ДНК у нас неизменяемая, поэтому при копировании мы можем сослаться на старую версию*/
	public DNA(DNA dna){
		this.size=dna.size;
		this.mind=dna.mind;
		this.instruction=dna.instruction;
		for (int i = 0; i < this.interrupts.length; i++)
			this.interrupts[i] = dna.interrupts[i];
	}
	public DNA(JSON dna) {
		this.size=dna.getI("size");
    	List<Integer> mindL = dna.getA("mind");
    	mind = new int[size];
    	for (int i = 0; i < size; i++) 
			mind[i] = mindL.get(i);
		this.instruction=dna.getI("instruction");
    	List<Integer> interruptsL = dna.getA("interrupts");
    	for (int i = 0; i < interrupts.length; i++) 
    		interrupts[i] = interruptsL.get(i);
	}
	/**А вот теперь ссылаться поздно - нам нужно поменять данные*/
	private DNA(DNA dna, int index, int value) {
		this.size=dna.size;
		this.mind = new int[dna.size];
    	System.arraycopy(dna.mind, 0, mind, 0, size);
		this.instruction=dna.instruction;
    	for (int i = 0; i < interrupts.length; i++) 
			this.interrupts[i] = dna.interrupts[i];
		//Изменение гена
		this.mind[getIndex(index)] = value;
	}
	
	/**
	 * Прерывание
	 * @param num - его номер
	 */
	protected void interrupt(AliveCell cell, int num) {
		if(activInterrupt) return;
		activInterrupt = true;
		int stackInstr = instruction; // Кладём в стек PC
		instruction = interrupts[num];
		for (int cyc = 0; (cyc < 15); cyc++)
			if(get().execute(cell)) break; // Выполняем программу прерывания
		instruction = stackInstr; // Возвращаем из стека PC
		activInterrupt = false;
	}
	
	public int getIndex(int offset) {
		int ret = (instruction + offset) % size;
        if(ret < 0)
        	ret += size;
        return ret;
	};
	/**Возвращает текущую инстуркцию*/
	public CommandDNA get() {
		return CommandList.list[mind[instruction]];
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
        instruction = getIndex(offset);
	}
	/**
	 * Обновляет значение гена в геноме
	 * @param index индекс в гене, причём 0 - под процессором
	 * @param value - значение, на которое надо заменить
	 * @return ДНК с обнавлёнными значениями
	 */
	public DNA update(int index, int value) {
		return new DNA(this,index,value);
	}
	public JSON toJSON() {
		JSON make = new JSON();
		make.add("size", size);
		make.add("mind", mind);
		make.add("instruction", instruction);
		make.add("interrupts", interrupts);
		return make;
	}
	/**
	 * Создаёт новую ДНК с удвоенным геном по адресу
	 * @param index - удваиваемый индекс, абсолютный
	 * @return
	 */
	public DNA doubling(int index) {
		DNA ret = new DNA(size+1);
    	System.arraycopy(mind, 0, ret.mind, 0, index+1);
    	ret.mind[index+1] = ret.mind[index]; //Дублирование гена
    	System.arraycopy(mind, index+1, ret.mind, index+2, size - 1 - index);
    	if(ret.instruction > index) {
    		ret.next(1); // Наш тактовый счётчик идёт дальше
    		for (int i = 0; i < interrupts.length; i++) {
				if(interrupts[i] > index) interrupts[i] = (interrupts[i]+1)%ret.size; // И все прерывания тоже сдвигаются
			}
    	}
		return ret;
	}
	/**
	 * Сжимает ДНК, удаляя из неё один ген
	 * @param index - удаляемый индекс, абсолютный
	 * @return
	 */
	public DNA compression(int index) {
		DNA ret = new DNA(size-1);
    	System.arraycopy(mind, 0, ret.mind, 0, index);
    	System.arraycopy(mind, index+1, ret.mind, index, size - 1 - index);
    	if(ret.instruction >= index) {
    		ret.next(-1);
    		for (int i = 0; i < interrupts.length; i++) {
				if(interrupts[i] >= index) interrupts[i] = Math.max(0,interrupts[i]-1); // И все прерывания тоже сдвигаются
			}
    	}
		return ret;
	}
	public int getIndex() {
		return instruction;
	}
	public int get(int instruction, int index) {
		int pos = (instruction+index) % size;
		if(pos < 0)
			pos += size;
		return mind[pos];
	}
	
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if (obj instanceof DNA) {
			DNA dna = (DNA) obj;
			if(this.mind == dna.mind) //Они ссылаются на один финальный массив - они полностью равны
				return true;
			if(this.size != dna.size) //У них разная длинна, это априорно даст разные ДНК
				return false;
			int dif = 0;
			for (int i = 0; i < dna.size && dif < 2; i++) {
				if(dna.mind[i] != dna.mind[i])
					dif++;
			}
			return dif >= 2;
		} else {
			return false;
		}
	}
}
