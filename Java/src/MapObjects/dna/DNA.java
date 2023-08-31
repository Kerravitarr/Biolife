package MapObjects.dna;

import java.util.List;

import MapObjects.AliveCell;
import MapObjects.CellObject.OBJECT;
import Utils.JSON;
import java.text.MessageFormat;
import java.util.Arrays;
import Calculations.Configurations;

/**ДНК бота*/
public class DNA {
	/**Размер мозга*/
	public final int size;
	/**Мозг. Внесение в него изменений черевато поломкой всего мира!*/
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
	/**Создаёт пустую ДНК
	 * @param size сколько в ней будет инструкций. Все инструкции - первая инструкция из CommandList.BLOCK_1
	 */
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
		this.mind=dna.mind; //Он не копируется!!! Тут создаётся ссылка!
		this.pc=dna.pc;
		interrupts = Arrays.copyOf(dna.interrupts, dna.interrupts.length);
	}
	/**Загружает ДНК из объекта
	 * @param dna объект ДНК
	 */
	public DNA(JSON dna) {
		this.size=dna.getI("size");
    	List<Integer> mindL = dna.getA("mind");
    	mind = new int[size]; //Тут происходит излишнее копирование ДНК... Но клетка скоро умрёт и память освободит :)
    	for (int i = 0; i < size; i++) 
			mind[i] = mindL.get(i);
		this.pc=dna.getI("instruction");
    	List<Integer> interruptsL = dna.getA("interrupts");
		interrupts = new int[interruptsL.size()];
    	for (int i = 0; i < interrupts.length; i++) 
    		interrupts[i] = interruptsL.get(i);
	}
	
	/**
	 * Прерывание в ДНК
	 * @param cell наш владелец, кто вызвал прерывание
	 * @param num номер прерывания
	 */
	protected void interrupt(AliveCell cell, int num) {
		if(activInterrupt) return; //Прерывание не может быть вложенным
		activInterrupt = true;
		final var stackInstr = pc; // Кладём в стек PC
		pc = interrupts[num] % size;
		for (int cyc = 0; cyc < 15; cyc++)
			if(get().execute(cell)) break; // Выполняем программу прерывания
		pc = stackInstr; // Возвращаем из стека PC
		activInterrupt = false;
	}
	/**
	 * Возвращает относитеьный индекс инструкции
	 * @param offset Смещение инструкции относительно текущей
	 * @return PC + offset
	 */
	public int getIndex(int offset) {
        return normalization(pc + offset);
	};
	/**Нормализует число по длине ДНК
	 * @param num число
	 * @return число [0,size]
	 */
	private int normalization(int num){
		num = num % size;
        if(num < 0)
        	num += size;
        return num;
	}
	/**Возвращает текущую инстуркцию*/
	public CommandDNA get() {
		return CommandList.list[mind[pc]];
	}
	/**Возвращает инстуркцию относительно PC
	 * @param index относительное смещение инструкции
	 * @return ДНК[PC + index]
	 */
	public CommandDNA get(int index) {
		return CommandList.list[get(index, false)];
	}
	/**Возвращает код команды ДНК по запросу
	 * @param index индекс команды
	 * @param isAbsolute index представленн как абсолютное местопложение или относительное?
	 * @return цифровой код команды
	 */
	public int get(int index, boolean isAbsolute){
		if(!isAbsolute) index = getIndex(index);
		else index = normalization(index);
		return mind[index];
	}
	/**
	 * Передвигает указатель на команду вперёд PC += offset
	 * @param offset - смещение
	 */
	public void next(int offset) {
        pc = getIndex(offset);
	}
	/**
	 * Возвращает текущий индекс инструкции
	 * @return PC
	 */
	public int getPC() {
		return pc;
	}
	/**Возвращает кусочек ДНК из ДНК
	 * @param index откуда кусочек начнётся
	 * @param isAbsolute index представленн как абсолютное местопложение или относительное?
	 * @param count длина кусочка
	 * @return 
	 */
	public int[] subDNA(int index, boolean isAbsolute, int count){
		if(count > size || count == 0) throw new IllegalArgumentException(MessageFormat.format(Configurations.getProperty(DNA.class, "error.subDNA.illegalCount"),count,size));
	
		if(!isAbsolute) index = getIndex(index);
		else index = normalization(index);
		final var ei = normalization(index + count);
		var cmds = new int[count];
		if(ei > index){
			System.arraycopy(mind, index, cmds, 0, ei - index);
		} else {
			System.arraycopy(mind, index, cmds, 0, size - index);
			System.arraycopy(mind, 0, cmds, size - index, ei);
		}
		return cmds;
	}

	/**
	 * Создаёт новую ДНК на основе существующей с заменой гена на новое значение
	 * @param index индекс замены
	 * @param isAbsolute index представленн как абсолютное местопложение или относительное?
	 * @param cmd какую команду обновляем
	 * @return Новую ДНК в которой ДНК[0,index) U cmd U ДНК[index+1,size]
	 */
	public DNA update(int index, boolean isAbsolute, int cmd) {
		return update(index, isAbsolute, new int[]{cmd});
	}
	/**
	 * Создаёт новую ДНК на основе существующей с заменой генов на новое значение
	 * @param index индекс замены
	 * @param isAbsolute index представленн как абсолютное местопложение или относительное?
	 * @param cmds какую последовательность надо обновить
	 * @return Новую ДНК в которой ДНК[0,index) U cmds U ДНК[index+cmds.lenght,size]
	 */
	public DNA update(int index, boolean isAbsolute, int[] cmds) {
		if(cmds.length > size) throw new IllegalArgumentException(MessageFormat.format(Configurations.getProperty(DNA.class, "error.update.illegalCount"),cmds.length,size));
		else if(cmds.length == 0) return this;
		DNA ret = new DNA(size);
		if(!isAbsolute) index = getIndex(index);
		else index = normalization(index);
		final var ei = (index + cmds.length) % size;
		if(ei > index){
			System.arraycopy(mind, 0, ret.mind, 0, index);
			System.arraycopy(cmds,0, ret.mind, index, cmds.length); //Копирование генома
			System.arraycopy(mind, ei, ret.mind, ei, size - ei);
		} else {
			System.arraycopy(cmds,  size - index, ret.mind, 0, ei);
			System.arraycopy(mind, ei, ret.mind, ei, index - ei);
			System.arraycopy(cmds,  0, ret.mind, index, size - index);
		}
    	ret.pc = pc;
		System.arraycopy(interrupts,  0, ret.interrupts, 0, interrupts.length);
		return ret;
	}
	/**
	 * Создаёт новую ДНК с удвоенным геном по адресу
	 * @param index индекс, откуда начинается интересующая нас последовательность
	 * @param isAbsolute index представленн как абсолютное местопложение или относительное?
	 * @return Новую ДНК в которой ДНК[0,index+1) U ДНК[index,index+1) U ДНК[index,size]
	 */
	public DNA doubling(int index, boolean isAbsolute) {
		return doubling(index,isAbsolute,1);
	}
	/**
	 * Создаёт новую ДНК с копированием участка генома с позиции ДНК[index]
	 * @param index индекс, откуда начинается интересующая нас последовательность
	 * @param isAbsolute index представленн как абсолютное местопложение или относительное?
	 * @param count сколько скопировать инструкций
	 * @return Новую ДНК в которой ДНК[0,index+count) U ДНК[index,index+count) U ДНК[index,size]
	 */
	public DNA doubling(int index, boolean isAbsolute, int count) {
		if(count == 0) return this;
		if(count > size) throw new IllegalArgumentException(MessageFormat.format(Configurations.getProperty(DNA.class, "error.doubling.illegalCount"),count,size));
		if(!isAbsolute) index = getIndex(index);
		else if(index >= size) index = index % size;
		return insert(index + count,true,subDNA(index,true,count));
	}
	/**
	 * Создаёт новую ДНК с вставкой в указанное место нового куска
	 * PC,	если он находился дальше начала зоны вставки, то он сдвигается вперёд на количество новых генов (указывает на тот-же нуклеотид)
	 *		если он находился до зоны вырезания - он не двигается
	 * Прерывания,	если они указывали дальше зоны вставки - они сдвигаются вперёд на количество новых генов (указывает на тот-же нуклеотид)
	 *				если находились до зоны веразния - не двигаются
	 * @param index индекс вставки
	 * @param isAbsolute index представленн как абсолютное местопложение или относительное?
	 * @param cmds какую последовательность надо вставить
	 * @return Новую ДНК в которой ДНК[0,index) U cmds U ДНК[index,size]
	 */
	public DNA insert(int index, boolean isAbsolute, int[] cmds) {
		if(cmds.length == 0) return this;
		DNA ret = new DNA(size+cmds.length);
		if(!isAbsolute) index = getIndex(index);
		else index = normalization(index);
    	System.arraycopy(mind, 0, ret.mind, 0, index);
    	System.arraycopy(cmds,0, ret.mind, index, cmds.length); //Копирование генома
    	System.arraycopy(mind, index, ret.mind, index + cmds.length, size - index);
    	ret.pc = pc;
    	if(ret.pc >= index)
    		ret.next(cmds.length); // Наш тактовый счётчик идёт дальше
		for (int i = 0; i < interrupts.length; i++) {
			if(interrupts[i] >= index) ret.interrupts[i] = interrupts[i]+cmds.length; // И все прерывания тоже сдвигаются
			else ret.interrupts[i] = interrupts[i];
		}
		return ret;
	}
	/**
	 * Сжимает ДНК, удаляя из неё гены
	 * PC,	если он находился дальше index, то он сдвигается назад на 1 (указывает на тот-же нуклеотид)
	 *		если он находился в index, то он сдвигается в index
	 *		если он находился до index - он не двигается
	 * Прерывания,	если они указывали дальше index - они сдвигаются назад на 1 (указывает на тот-же нуклеотид)
	 *				если они указывали на index, сдвигаются в index
	 *				если находились до index - не двигаются
	 * @param index с какой позиции, включительно, удалять
	 * @param isAbsolute index представленн как абсолютное местопложение или относительное?
	 * @return Новую ДНК - ДНК[0,index) U ДНК[index+1,size]
	 */
	public DNA compression(int index, boolean isAbsolute){
		return compression(index,isAbsolute, 1);
	}
	/**
	 * Сжимает ДНК, удаляя из неё гены
	 * PC,	если он находился дальше начала зоны вырезания, то он сдвигается назад на количество вырезанных генов (указывает на тот-же нуклеотид)
	 *		если он находился в зоне вырезания, то он сдвигается в начало этой зоны
	 *		если он находился до зоны вырезания - он не двигается
	 * Прерывания,	если они указывали дальше вырезаемого места - они сдвигаются назад на количество вырезаний генов (указывает на тот-же нуклеотид)
	 *				если они указывали на зону вырезания, сдвигаются в начало этой зоны
	 *				если находились до зоны веразния - не двигаются
	 * @param index с какой позиции, включительно, удалять
	 * @param isAbsolute index представленн как абсолютное местопложение или относительное?
	 * @param count сколько удалять
	 * @return Новую ДНК - ДНК[0,index) U ДНК[index+count,size]
	 */
	public DNA compression(int index, boolean isAbsolute, int count) {
		if(count >= size || count == 0) throw new IllegalArgumentException(MessageFormat.format(Configurations.getProperty(DNA.class, "error.compression.illegalCount"),count,size));
		
		if(!isAbsolute) index = getIndex(index);
		else index = normalization(index);
		final var ei = normalization(index + count);
		DNA ret = new DNA(size-count);
    	ret.pc = pc;
		if(ei > index){
			System.arraycopy(mind, 0, ret.mind, 0, index);
			System.arraycopy(mind, ei, ret.mind, index, size - ei);
			
			if(ret.pc > index){
				if(ret.pc < ei)
					ret.pc = index;
				else
					ret.next(-count);
			}
			// И все прерывания тоже сдвигаются
			for (int i = 0; i < interrupts.length; i++) {
				if(interrupts[i] > index) {
					if(interrupts[i] < ei)	ret.interrupts[i] = index;
						else				ret.interrupts[i] = ret.normalization(interrupts[i] - count); 
				}else						ret.interrupts[i] = interrupts[i];
			}
		} else {
			System.arraycopy(mind, ei, ret.mind, 0, index - ei);
			
			if(index < ret.pc || ret.pc < ei)
				ret.pc = ret.size-1;
			else
				ret.next(-ei);
			
			// И все прерывания тоже сдвигаются
			for (int i = 0; i < interrupts.length; i++) {
				if(index <= interrupts[i] || interrupts[i] < ei)	ret.interrupts[i] = ret.size-1;
				else												ret.interrupts[i] = ret.normalization(interrupts[i] - ei);
			}
		}
		return ret;
	}
			
	/**Сравнивает две ДНК
	 * @param dna чужая ДНК
	 * @param tolerance сколько допустимо расхождений генов
	 * @return true, если похожи
	 */
	public boolean equals(DNA dna, int tolerance) {
		if(this.mind == dna.mind) //Они ссылаются на один финальный массив - они полностью равны
			return true;
		if(this.size != dna.size) //У них разная длинна, это априорно даст разные ДНК
			return false;
		int dif = 0;
		for (int i = 0; i < dna.size && dif <= tolerance; i++) {
			if(mind[i] != dna.mind[i])
				dif++;
		}
		return dif <= tolerance;
	}
	
	public JSON toJSON() {
		JSON make = new JSON();
		make.add("size", size);
		make.add("mind", mind);
		make.add("instruction", pc);
		make.add("interrupts", interrupts);
		return make;
	}
}
