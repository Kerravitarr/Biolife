package MapObjects;

import java.awt.Graphics;

import Utils.ColorRec;
import Utils.Utils;
import main.Point;
import main.World;

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
		
		public void paint(Graphics g) {
			for (ColorRec colorRec : rows)
				colorRec.paint(g);
		}
		public String toString() {
			return "x0: " + startX + " endx: " + endX + " pw: " + power;
		}
		public void resize(int w, int h) {
			ColorRec[] rowsL = new ColorRec[(int) Math.round(World.DIRTY_WATER)];
			int lenghtY = h-World.border.height*2;
	        int heightSun = (int) Math.ceil(lenghtY/World.DIRTY_WATER);
	        int startXPx = (int) Math.round(World.border.width+startX*World.scale + World.scale/2);
	        int endPx = (int) Math.round(World.border.width+endX*World.scale + World.scale/2);
			for (int i = 0; i < World.DIRTY_WATER; i++) {
				float sunPower = (float) ((240 - Math.max(0, (power - i)/(power+1))*60)/360);
				rowsL[i] = new ColorRec(startXPx,World.border.height + i*heightSun, endPx-startXPx, heightSun,Utils.getHSBColor(sunPower, 1, 1,0.5));
			}
			rows = rowsL;
		}
		public double getEnergy(Point pos) {
			return  power - (World.DIRTY_WATER * pos.y / World.MAP_CELLS.height);
		}
		public void repaint() {
			for (int i = 0; i < World.DIRTY_WATER; i++) {
				float sunPower = (float) ((240 - Math.max(0, (power - i)/(power+1))*60)/360);
				rows[i].setColor(Utils.getHSBColor(sunPower, 1, 1,0.5));
			}
		}
	}
	/**На сколько частей нужно поделить экран*/
	private int PARTS = 30;
	//Скорость движения солнца в тиках мира
	private int SPEED = 1;
	//Положение солнца в частях экрана
	private int SunPos;
	/**"Ширина" солнечного света в частях экрана*/
	private int SunLenght = 20;
	
	Part[] cr;
	
	public Sun(int width, int height){
		cr = new Part[PARTS];
		int startX = 0;
		for (int i = 0; i < PARTS; i++) {
			int lenght = (i+1) * World.MAP_CELLS.width / PARTS - startX;
			cr[i] = new Part(startX,lenght);	
			startX = (i+1) * World.MAP_CELLS.width / PARTS;
		}
		startX= 0;
		SunPos = PARTS / 2;
		resize(width, height);
	}

	public void resize(int width, int height) {
		for(int i = 0 ; i < PARTS ; i++) {
			int pos = (SunPos-SunLenght/2) + i;
			while(pos >= PARTS)
				pos -= PARTS;
			while(pos < 0)
				pos += PARTS;
			if(i > SunLenght) {
				cr[pos].power = 0;
			}else {
				cr[pos].power = (int) Math.round(World.SUN_POWER * Math.cos((1.0*i/SunLenght - 0.5)*Math.PI));
			}
			cr[pos].resize(width,height);
		}
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

	/**Шаг мира для пересчёта*/
	public void step(long step) {
		if(step % SPEED == 0) {
			SunPos++;
			if(SunPos >= PARTS)
				SunPos -= PARTS;
			for(int i = 0 ; i < PARTS ; i++) {
				int pos = (SunPos-SunLenght/2) + i;
				while(pos >= PARTS)
					pos -= PARTS;
				while(pos < 0)
					pos += PARTS;
				if(i > SunLenght) {
					cr[pos].power = 0;
				}else {
					cr[pos].power = World.SUN_POWER * Math.cos((1.0*i/SunLenght - 0.5)*Math.PI);
				}
				cr[pos].repaint();
			}
		}
	}

}
