package Utils;

import java.util.ArrayDeque;

/**
 *Буфер сжатия. Его основная фишка в работе, постараюсь объяснить:
 * Шаг 1: Пришла 1 и заняла своё место {1,0}→1 [1,0]. В буфере 2 пока добавляется сумма {1,0}→1 [1,0]
Шаг 2: Пришла 2 и сдвинула 1 на следующее место {2,1}→3 [3,0]. В буфере 2 обновляется сумма {3,0}→3 [3,0]
Шаг 3: Пришла 3. В буфере уже два числа 2 и 1, поэтому их сумма 3 переходит дальше, а буфер сдвигается и записывается 3 {3,2}→5 [5,3]. В буфере 2 сумма переходит дальше, а в начало встаёт новая сумма {5,3}→8 [8,0]
Шаг 4: Пришла 4. 4 опять записывается в первую ячейку {4,3}→7 [7,3]. В буфере 2 пока меняется только первое число {7,3}→10 [10,0]
Шаг 5: Пришла 5. Обновляем значения и записываем 5 в своё место {5,4}→9 [9,7]. В буфере 2 тоже сдвигаем данные  {9,7}→16 [16,10]
* Таким образом, набирая эти буферы в цепочку, в последнем буффере всегда будут все значения, при этом каждый следующий буффер в масштабе меньше предыдущего
* Однако есть и свой минус: Первый элемент массива у нас всегда "опережает" своё значение. Так что его лучше не использовать для всяких сложных оценок
 * @author Илья
 */
public class ZipBuffer{
    /**Большое кольцо, которое хранит значения*/
    private final ArrayDeque<Long> bigRing;
    /**Малое кольцо, которое хранит значения только для малой суммы*/
    private final ArrayDeque<Long> miniRing;
    /**Размер буфера*/
    private final int size;
    /**На сколько надо сжимать входные значения*/
    private final int zip;
    /**Следующий буфер в цепочке*/
    private final ZipBuffer next;
    /**Бесконечный буфер?*/
    private final boolean isInfinity;
    /**Безразмерный подбуфер?*/
    private final boolean isUnsize;
    
    /**Сколько элементов в малом кольце*/
    private long minRingSize = 0;
    /**Сумма элементов в малом кольце*/
    private long minRingSum = 0;
    
    /**Создаёт элемент буффера
     * @param size количество элементов в этом буфере. Если 0, то буфер будет бесконечно расти,
     *  если меньше нуля - буфер будет завершающим только с одним элементом
     * @param zip на сколько сжимать входящие значения
     * @param next следующий буфер
     */
    public ZipBuffer(int size, int zip, ZipBuffer next){
        isUnsize = size < 0;
        isInfinity = size < 1;
        this.zip = size < 1 ? Math.max(2,zip) : Math.max(1,zip);
        this.size = Math.max(1,size);
        this.next = next;
        bigRing = new ArrayDeque<>(this.size);
        miniRing = new ArrayDeque<>(this.zip);
        for (int i = 0; i < this.size; i++) {bigRing.add(0l);}
        for (int i = 0; i < this.zip; i++) {miniRing.add(0l);}
    }
    /**Создаёт элемент буффера
     * @param size количество элементов в этом буфере
     * @param zip на сколько сжимать входящие значения
     */
    public ZipBuffer(int size, int zip){this(size,zip,null);}
    /**Создаёт элемент буффера
     * @param size количество элементов в этом буфере
     */
    public ZipBuffer(int size){this(size,1);}
    /**Создаёт буфер суммы. В нём будет только один элемент, который хранит все возможные значения*/
    public ZipBuffer(){this(0);}
    /**Добавляет элемент в буфер. Добавлять следует только в последний буффер элементы, в остальные значения перейдут сами
     * @param element последний элемент выборки
     */
    public void add(long element){
        if((!isInfinity || isUnsize) && minRingSize++ % zip == 0){
            if(!isUnsize || minRingSize == 1)
                bigRing.removeLast();
            bigRing.addFirst(minRingSum);
            if(next != null) next.add(minRingSum);
        }
        if(isInfinity && !isUnsize){
            minRingSum += element;
            miniRing.removeLast();
            miniRing.removeLast();
            miniRing.addFirst(minRingSum);
            miniRing.addFirst(element);
        } else {
            minRingSum += element - miniRing.removeLast();
            miniRing.addFirst(element);
        }
        bigRing.removeFirst();
        bigRing.addFirst(minRingSum);
        if(!isInfinity && next != null) next.readd(minRingSum);
    }
    /**Внутренний метод, нужен, чтобы изменить значение внутреннего буфера, пока остальные значения не изменились
     * @param element 
     */
    private void readd(long element){
        minRingSum += element - miniRing.removeFirst();
        miniRing.addFirst(element);
        bigRing.removeFirst();
        bigRing.addFirst(minRingSum);
        if(!isInfinity && next != null) next.readd(minRingSum);
    }

    @Override
    public String toString() {
        var sb = new StringBuffer();
        sb.append('[');
        sb.append('{');
        var isFirst = true;
        for(var val : miniRing){
            if(isFirst) isFirst = false;
            else sb.append(',');
            sb.append(val);
        }
        sb.append("}->");
        isFirst = true;
        for(var val : bigRing){
            if(isFirst) isFirst = false;
            else sb.append(',');
            sb.append(val);
        }
        sb.append("] ");
        if(next != null)
            sb.append(next);
        return sb.toString();
    }
}
