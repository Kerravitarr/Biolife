package Utils;

public class FPScounter {
	int counterFrame = 0;
	double k = 0.5;  // коэффициент фильтрации, 0.0-1.0
	private double FPS = 0;

	public FPScounter() {
		new Thread() {
			public void run() {
				while(true) {
					updateDPS();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
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
