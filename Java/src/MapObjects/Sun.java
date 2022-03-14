package MapObjects;

import java.awt.Graphics;

import Utils.ColorRec;
import Utils.Utils;
import main.Configurations;
import main.Point;

/**
 * Солнышко, которое нас освещает
 * @author Илья
 *
 */
public class Sun {
	/**Одна часть экрана*/
	private static class Part{
		public Part(int startX, int lenght) {
			this.startX=startX;
			this.endX=startX+lenght;
		}
		int startX;
		int endX;
		double power = 0;
		//Строки в этой части
		ColorRec[] rows;
		
		/**На сколько подпунктов делить ячейки, на сколько плавно будут смотреться переходы*/
		static final double sizeDraw = 2;
		
		public void paint(Graphics g) {
			for (ColorRec colorRec : rows)
				colorRec.paint(g);
		}
		public String toString() {
			return "x0: " + startX + " endx: " + endX + " pw: " + power;
		}
		public void resize(int w, int h) {
			ColorRec[] rowsL = new ColorRec[(int) Math.round(Configurations.DIRTY_WATER*sizeDraw)];
			int lenghtY = h-Configurations.border.height*2;
	        int heightSun = (int) Math.ceil(lenghtY/(Configurations.DIRTY_WATER*sizeDraw));
	        int startXPx = Point.getRx(startX);
	        int endPx = Point.getRx(endX);
			for (int i = 0; i < Configurations.DIRTY_WATER*sizeDraw; i++) {
				float sunPower = (float) ((240 - Math.max(0, (power - i/sizeDraw)/(power+1))*60)/360);
				rowsL[i] = new ColorRec(startXPx,Configurations.border.height + i*heightSun, endPx-startXPx, heightSun,Utils.getHSBColor(sunPower, 1, 1,0.7));
			}
			rows = rowsL;
		}
		public double getEnergy(Point pos) {
			return  power - (Configurations.DIRTY_WATER * pos.y / Configurations.MAP_CELLS.height);
		}
		public void repaint() {
			for (int i = 0; i < Configurations.DIRTY_WATER*sizeDraw; i++) {
				float sunPower = (float) ((240 - Math.max(0, (power - i/sizeDraw)/(power+1))*60)/360);
				rows[i].setColor(Utils.getHSBColor(sunPower, 1, 1,0.7));
			}
		}
	}
	
	Part[] cr;
	
	public Sun(int width, int height){
		Configurations.sun = this;
		cr = new Part[Configurations.SUN_PARTS];
		int startX = 0;
		for (int i = 0; i < Configurations.SUN_PARTS; i++) {
			int lenght = (i+1) * Configurations.MAP_CELLS.width / Configurations.SUN_PARTS - startX;
			cr[i] = new Part(startX,lenght);	
			startX = (i+1) * Configurations.MAP_CELLS.width / Configurations.SUN_PARTS;
		}
		startX= 0;
		Configurations.SUN_POSITION = Configurations.SUN_PARTS / 2;
		resize(width, height);
	}

	public void resize(int width, int height) {
		setPowers();
		for (Part part : cr)
			part.resize(width,height);
	}

	public double getEnergy(Point pos) {
		for (Part pa : cr) {
			if(pa.startX <= pos.x && pos.x <= pa.endX) {
				return pa.getEnergy(pos);
			}
		}
		return 0;
	}

	public void paint(Graphics g) {
		for (Part colorRec : cr)
			colorRec.paint(g);
	}
	
	public void repaint() {
		setPowers();
		for (Part part : cr)
			part.repaint();
	}

	/**Шаг мира для пересчёта*/
	public void step(long step) {
		if(step % Configurations.SUN_SPEED == 0) {
			Configurations.SUN_POSITION++;
			if(Configurations.SUN_POSITION >= Configurations.SUN_PARTS)
				Configurations.SUN_POSITION -= Configurations.SUN_PARTS;
			repaint();
		}
	}

	private void setPowers() {
		for (int i = 0; i < Configurations.SUN_PARTS / 2 + 1; i++) {
			int pos = Configurations.SUN_POSITION + i;
			while (pos >= Configurations.SUN_PARTS)
				pos -= Configurations.SUN_PARTS;
			
			if (i > Configurations.SUN_LENGHT/2) {
				cr[pos].power = 0;
			} else {
				cr[pos].power = Configurations.SUN_POWER * Math.cos((1.0 * i / Configurations.SUN_LENGHT) * Math.PI);
			}
			
			if(i != 0) {
				pos = Configurations.SUN_POSITION - i;
				while (pos < 0)
					pos += Configurations.SUN_PARTS;
				
				if (i > Configurations.SUN_LENGHT/2) {
					cr[pos].power = 0;
				} else {
					cr[pos].power = Configurations.SUN_POWER * Math.cos((1.0 * i / Configurations.SUN_LENGHT) * Math.PI);
				}
			}
			
		}
	}

}
