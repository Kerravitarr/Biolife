/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author Kerravitarr
 */
public class StreamProgressBar {
	ArrayList<String> events = new ArrayList<>();
	int eventIndex = 0;
	long prefMc = 0;
	DecimalFormat df = new DecimalFormat("###,###");
	public void addEvent(String event){
		events.add(event);
	}
	public void event(){
		var evText = new StringBuilder();
		evText.append("[");
		evText.append(eventIndex);
		evText.append("/");
		evText.append(events.size());
		evText.append("] ");
		evText.append(events.get(eventIndex++));
		if(prefMc == 0){
			prefMc = System.currentTimeMillis();
		} else {
			var mc = System.currentTimeMillis();
			evText.append(". Время выполнения ");
			evText.append(df.format(mc-prefMc));
			evText.append("мс");
			prefMc=mc;
		}
		System.out.println(evText.toString());
	}
}
