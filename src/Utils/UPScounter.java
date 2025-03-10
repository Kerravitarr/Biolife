package Utils;

public class UPScounter {    
	private final double k = 0.25;  // коэффициент фильтрации, 0.0-1.0
    /**Когда был прошлый удар*/
	private long lastINT = -1;
    /**Ударов в секунду*/
	private double UPS2 = 0;	
    
	/**Добавилось новое событие в счётчик*/
	public void interapt() {
        var tm = System.nanoTime();
        if(lastINT == -1){
            lastINT = tm;
        } else {
            UPS2 = nextUPS(tm - lastINT, k);
            lastINT = tm;
        }
    }
    /**Вовзращает следующее колличество шагов
     * @param dt
     * @return 
     */
    private double nextUPS(double dt, double k){
        var next = (1e9 / (dt == 0 ? 1 : dt));
        if(UPS2 == 0){
            return next;
        } else {
            return (1 - k) * UPS2 + k * next;
        }
    }
    /**Возвращает текущее количество кадров в секунуд*/
    private double cUPS(){return Math.min(UPS2, nextUPS(System.nanoTime() - lastINT, 1));}
    
	/**@return Сколько действий в секунду*/
	public long UPS() {return Math.round(cUPS());}
	/**@return Сколько действий в минуту*/
	public long UPM() {return Math.round(cUPS() * 60);}
	/**@return Сколько действий в секунду*/
	public double dUPS() {return Utils.round(cUPS(), 2);}
    @Override public String toString() {return String.format("UPS:%g",dUPS());}
    
}
