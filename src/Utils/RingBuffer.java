/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *Колцевой буефер с перезаписью элементов!!!
 * То есть, когда в нём заканчивается место, он просто удаляет последний элемент и на его места заносит новый!
 * @author https://www.baeldung.com/java-ring-buffer
 */
public class RingBuffer<T> {
	/**Вместимость буфера*/
	private final int capacity;
	/**Буфер с данными*/
	private final T[] data;
	/**Голова буфера, куда мы будем записывать*/
	private int writeSequence;
	/**Хвост буфера, откуда мы будем читать*/
	private int readSequence;
	
	/**Создание кольцевого буфера
	 * @param capacity вместимость буфера
	 */
	public RingBuffer(int capacity) {
		this.capacity = capacity;
		this.data = (T[]) new Object[this.capacity];
		this.readSequence = 0;
		this.writeSequence = -1;
	}
	/**Проверка на пустоту
	 * @return true, если буфер пуст
	 */
	public boolean isEmpty() {
		return writeSequence < readSequence;
	}
	/**Проверка на заполненность
	 * @return true, если буфер заполнен и больше в него нельзя добавить элемент
	 */
	public boolean isFull() {
		return size() == capacity;
	}
	/**Текущий размер буфера
	 * @return сколько в нём элементов
	 */
	public int size() {
		return (writeSequence - readSequence) + 1;
	}
	/**Кладёт новый элемент в буфер. Если в буфере нет места - то перезаписывает хвост
	 * @param item элемент, который будет положен в голову буфера
	 */
	public void push(T item) throws IndexOutOfBoundsException {
		if (isFull()) readSequence++;
        data[++writeSequence % capacity] = item;
	}
	/**Возвращает элемент с головы и удаляет его
	 * @return 
	 */
	public T pop() {
		if (isEmpty()) {return null;}
		return data[writeSequence-- % capacity];
	}
}
