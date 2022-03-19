package Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FPScounter {
	int counterFrame = 0;
	double k = 0.5;  // коэффициент фильтрации, 0.0-1.0
	private double FPS = 0;	
	private static List<FPScounter> listeners = new ArrayList<>();
	
	private static Timer timer = new Timer("FPScounter");
	static {
		timer.schedule(new TimerTask() {
			public void run() {
				listeners.forEach(counter -> {
					counter.updateDPS();
				});
			}
		}, 0L, 1000);
	}

	public FPScounter() {
		listeners.add(this);
	}
	public void close() {
		listeners.remove(this);
	}
	
	public void interapt() {
		counterFrame++;
	}

	private void updateDPS() {
		if(FPS == 0)
			FPS = counterFrame;
		FPS += (counterFrame - FPS) * k;
		counterFrame = 0;
	}
	
	public long FPS() {
		return Math.round(FPS);
	}
}
