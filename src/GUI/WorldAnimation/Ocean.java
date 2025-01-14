/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import Calculations.Point;
import GUI.AllColors;
import GUI.Legend;
import GUI.Menu;
import GUI.WorldView;
import Utils.ColorRec;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Анимация для океана
 * Океан - поле, с двух сторон (неизвестно с каких) оно будет сшито, а две соатвшиеся стороны могут быть чем угодно
 * 
 * @author Kerravitarr
 */
public class Ocean extends DefaultAnimation{
	/**водичка*/
	private final ColorRec water;
	/**трансформатор*/
	private final WorldView.Transforms transformer;
	/**стартовая точка по оси x*/
	private final int sx;
	/**конечная точка по оси x*/
	private final int ex;
	/**стартовая точка по оси y*/
	private final int sy;
	/**конечная точка по оси y*/
	private final int ey;
	
	public Ocean(WorldView.Transforms transform, int w, int h){
		super(transform);
		water = ColorRec.rectangle(0,0,w,h, AllColors.WATER_OCEAN);
		transformer = transform;
		var x = 0;
		while(transformer.toDScrinX(x)+transformer.getZScrin() > 0){
			x--;
		}
		this.sx = x;
		x = Configurations.getWidth() - 1;
		while(transformer.toDScrinX(x)-transformer.getZScrin() < w){
			x++;
		}
		ex = x;
		
		
		var y = 0;
		while(transformer.toDScrinY(y)+transformer.getZScrin() > 0){
			y--;
		}
		this.sy = y;
		y = Configurations.getHeight()- 1;
		while(transformer.toDScrinY(y)-transformer.getZScrin() < h){
			y++;
		}
		ey = y;
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}
	private int toField(int xy, int lenght){
		if(xy < 0){
			while(xy < 0) xy+= lenght; //Пододвигаем X к ближайшему квадрату
			return xy;
		} else if(xy < lenght) {
			return xy;
		} else {
			while(xy > lenght) xy -= lenght; //Пододвигаем X к ближайшему квадрату
			return xy;
		}
		/*var lenght2 = lenght * 2;
		if(xy < 0){
			while(xy < -lenght2) xy+= lenght2; //Пододвигаем X к ближайшей паре квадратов
			if(xy < -lenght){
				//х оказался в дальнем квадрате. Там копия поля
				return xy + lenght2;
			} else {
				return xy + lenght;
			}
		} else if(xy < lenght) {
			return xy;
		} else {
			while(xy > lenght + lenght2) xy -= lenght2; //Пододвигаем X к ближайшей паре квадратов
			if(xy < lenght2){
				//х оказался в ближнем квадрате. Там копия поля
				return xy - lenght;
			} else {
				return lenght2+lenght - xy;
			}
		}*/
	}
	@Override
	public void world(Graphics2D g, Rectangle visible, java.awt.geom.Area field) {
		var width = Configurations.getWidth() - 1;
		var height = Configurations.getHeight()- 1;
		var r = transformer.getZScrin();
		var world = Configurations.world;
		final var legend = Configurations.getViewer().get(Legend.class);
		final var menu = Configurations.getViewer().get(Menu.class);
		if(r > 2){
			for(var x = sx; x < ex; x++){
				var px = toField(x,width);
				for(var y = sy; y < ey; y++){
					if(0 <= x && x <= width && 0 <= y && y <= height) continue;
					var py = toField(y,height);
					var cell = world.get(Point.create(px,py));
					if(cell == null || !menu.isVisibleCell(cell)) continue;
					g.setColor(cell.getPaintColor(legend));
					cell.paint(g, transformer.toScrinX(x), transformer.toScrinY(y), r);
				}
			}
		} else {
			//А если меньше, то рисовать мы будем самыми общими чертами
			final var dr = transformer.getDZScrin();
			if (dr <= 0) return;
			final var step = (int)Math.ceil(4d/dr); //4 - потому что рисуем квадратиками 2х2 пк
			final var nr = transformer.toScrin(step);
			final var ritangleColor = new Color[step * step];
			for (int x = sx; x < ex; x+=step) {
				var px = toField(x,width);
				for (int y = sy; y < ey; y+=step) {
					if(0 <= x && x <= width && 0 <= y && y <= height) continue;
					var lendhtC = 0;
					var py = toField(y,height);
					for(var dx = 0 ; dx < step; dx++){
						for(var dy = 0 ; dy < step; dy++){
							final var pos = Point.create(px+dx, py+dy);		
							final var cell = world.get(pos);
							if(cell != null && menu.isVisibleCell(cell)){
								ritangleColor[lendhtC++] = cell.getPaintColor(legend);
							}
						}
					}
					if(lendhtC > 0){
						g.setColor(AllColors.blend(lendhtC, ritangleColor));
						int cx = transformer.toScrinX(x);
						int cy = transformer.toScrinY(y);
						g.fillRect(cx, cy, nr, nr);
					}
				}
			}
		}
	}
}
