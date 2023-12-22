package Utils;


import java.util.HashMap;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Счётчик с несколькими триггерами
 * @author
 */
public class SameStepCounter {
	/**Сколько всего должно произойти событий*/
	private final int allCount;
	/**Какие события уже произошли*/
	private final HashMap<Integer,Boolean> flags;
	/**Значение счётчика*/
	private int count = 0;
	/**Сколько событий уже случилось*/
	private int countT = 0;
	/**
	 * Создаёт счётчик
	 * @param a количество разных событий, которые ждёт счётчик
	 */
	public SameStepCounter(int a){allCount = a;flags = new HashMap<>(allCount);for(var i = 0 ; i < allCount ; i++) flags.put(i, false);}
	/**
	 * Произошедшее событие.
	 * @param numBranch его номер. [0;allCount)
	 */
	public void step(int numBranch){
		if(!flags.get(numBranch)){
			flags.put(numBranch, true);
			countT++;
		}
		if(countT == allCount){
			for(var i = 0 ; i < allCount ; i++)
				flags.put(i, false);
			countT = 0;
			scroll(1);
		}
	}
	/**Dthyenm */
	public int get(){return count & 0xFFFFFF;};
	/**Перелистывает счётчик, априорно, на заданое число тактов*/
	public void scroll(int i) {
		count += i;
	}
}
