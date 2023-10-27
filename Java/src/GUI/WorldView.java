/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import static Calculations.Configurations.WORLD_TYPE.LINE_H;
import Calculations.Emitters.MineralAbstract;
import Calculations.Emitters.SunAbstract;
import Calculations.Point;
import Calculations.Trajectories.Trajectory;
import MapObjects.CellObject;
import Utils.ColorRec;
import Utils.FPScounter;
import Utils.Variant;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

	import java.awt.*;
import java.awt.image.*;
import java.util.Objects;
/**
 *
 * @author Kerravitarr
 */
public class WorldView extends javax.swing.JPanel {
	/**Класс для всех преобразований из размеров мира в размеры пиксеелй на экране*/
	public class Transforms{
		/**Масштаб - количество пикселей на 1 ячейку мира*/
		private double scalePxPerCell = 1;
		/**Верхний и нижний бордюр. Нужен, чтобы мир не упиралдся прям в потолок, а имел небольшой зазор сверху и снизу*/
		private static final java.awt.geom.Point2D UP_DOWN_border = new java.awt.Point.Double(0.02, 0.02);
		/**Верхний и нижний бордюр. Нужен, чтобы мир не упиралдся прям в края, а имел небольшой зазор слева и справа*/
		private static final java.awt.geom.Point2D LEFT_RIGHT_border = new java.awt.Point.Double(0.02, 0.02);
		/**Дополнительный край из-за не совершенства арены*/
		public static final Dimension border = new Dimension();
		/**Количество пиксеелй на сколько мир смещается от верхней границы из за необходимости отрисовки границы*/
		private double pixelXDel = 0d;
		/**Количество пиксеелй на сколько мир смещается от левой границы из за необходимости отрисовки границы*/
		private double pixelYDel = 0d;
		
		/**
		* Переводит координаты мира в координаты экрана
		* @param x координата в масштабах клеток
		* @return x координата в масштабе окна, пк
		*/
		public int toScrinX(int x) {return (int) Math.round(toDScrinX(x));}
		/**
		* Переводит координаты мира в координаты экрана
		* @param x координата в масштабах клеток
		* @return x координата в масштабе окна, пк
		*/
		public double toDScrinX(int x) {return x*scalePxPerCell + pixelXDel;}
	   /**
		* Переводит координаты мира в координаты экрана
		* @param point объект с координатами, из которого выбирается только Х
		* @return x координата в масштабе окна, пк
		*/
		public int toScrinX(Point point) {return toScrinX(point.getX());}
	   /**
		* Переводит координаты мира в координаты экрана
		* @param y координата в масштабах клетки
		* @return y координата в масштабе окна, пк
		*/
		public int toScrinY(int y) {return (int) Math.round(toDScrinY(y));}
	   /**
		* Переводит координаты мира в координаты экрана
		* @param y координата в масштабах клетки
		* @return y координата в масштабе окна, пк
		*/
		public double toDScrinY(int y) {return y*scalePxPerCell + pixelYDel;}
	   /**
		* Переводит координаты мира в координаты экрана
		* @param point объект с координатами, из которого выбирается только Y
		* @return y координата в масштабе окна, пк
		*/
		public int toScrinY(Point point) {return toScrinY(point.getY());}
	   /**
		* Переводит координаты мира в координаты экрана
		* @param point объект с координатами мира
		* @return объект с координатами экрана
		*/
		public java.awt.Point toScrin(Point point) {return new java.awt.Point(toScrinX(point),toScrinY(point));}
	   
	   /**Возвращает размеры мира в пикселях
		* @param r размер объекта в клетках мира
		* @return радиус объекта в пикселях
		*/
		public int toScrin(int r) {return (int) Math.round(toDScrin(r));}
	   /**Возвращает размеры мира в пикселях
		* @param r размер объекта в клетках мира
		* @return радиус объекта в пикселях
		*/
		public double toDScrin(int r) {return r * scalePxPerCell;}
	   /**
		* Переводит координаты экрана в координаты мира
		* @param x координата на экране
		* @return x координата на поле. Эта координата может выходить за размеры мира!!!
		*/
		public int toWorldX(int x) {return (int) ((x - border.width)/scalePxPerCell);}
	   /**
		* Переводит координаты экрана в координаты мира
		* @param y координата на экране
		* @return x координата на поле. Эта координата может выходить за размеры мира!!!
		*/
		public int toWorldY(int y) {return (int) ((y - border.height)/scalePxPerCell);}
		
		/**
		 * Пересчитыавет координаты мировые в пикселях в координаты ячейки
		 *
		 * @param event событие мыши для этой области
		 *
		 * @return точку в реальном пространстве
		 */
		public Point toWorldPoint(java.awt.event.MouseEvent event) {
			if(event.getSource() == WorldView.this){
				return recalculation(event.getX(),event.getY());
			} else {
				throw new UnsupportedOperationException("Обождите!");
			}
		}
		/**
		 * Пересчитыавет координаты мировые в пикселях в координаты ячейки
		 *
		 * @param x экранная координата х
		 * @param y экранная координата у
		 *
		 * @return точку в реальном пространстве
		 */
		private Point recalculation(int x, int y) {
			x = (int) Utils.Utils.betwin(transforms.pixelXDel, x, WorldView.this.getWidth() - Transforms.border.width);
			y = (int) Utils.Utils.betwin(transforms.pixelYDel, y, WorldView.this.getHeight() - Transforms.border.height);
			return Point.create(transforms.toWorldX(x), transforms.toWorldY(y));
		}
	   
		/** Специальная функция, которая обновляет все масштабные коэффициенты */
		private void recalculate() {
			//Размеры мира, с учётом обязательного запаса
			final double h = WorldView.this.getHeight();
			final double w = WorldView.this.getWidth();
			//Пересчёт размера мира
			scalePxPerCell = Math.min(h * (1d - getUborder() - getDborder()) / (Configurations.getHeight()), w * (1d - getLborder() - getRborder()) / (Configurations.getWidth()));
			border.width = (int) Math.round((w - Configurations.getWidth() * scalePxPerCell) / 2);
			border.height = (int) Math.round((h - Configurations.getHeight() * scalePxPerCell) / 2);
			pixelXDel = (w - (Configurations.getWidth() - 1) * scalePxPerCell) / 2;
			pixelYDel = (h - (Configurations.getHeight() - 1) * scalePxPerCell) / 2;
		}
	}

	/** Creates new form WorldView */
	public WorldView() {
		initComponents();
		setVisible(Point.create(0, 0), Point.create(Configurations.getWidth()-1, Configurations.getHeight()-1));
		recalculate();
		setBackground(null);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1, 1));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        EventQueue.invokeLater(() -> recalculate());
    }//GEN-LAST:event_formComponentResized

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
       if ( Configurations.getViewer() instanceof DefaultViewer df) {
			if(df.getMenu().isSelectedCell()){
				selectPoint[1] = transforms.toWorldPoint(evt);
				selectPoint[0] = selectPoint[1];
			}
		}
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
         selectPoint[1] = transforms.toWorldPoint(evt);
		if(selectPoint[0] == null || selectPoint[1] == null) return;
		if ( Configurations.getViewer() instanceof DefaultViewer df) {
			final var menu = df.getMenu();
			if(menu.isSelectedCell()){
				var selected = new ArrayList<CellObject>();
				var x0 = Math.min(selectPoint[0].getX(), selectPoint[1].getX());
				var y0 = Math.min(selectPoint[0].getY(), selectPoint[1].getY());
				var x1 = Math.max(selectPoint[0].getX(), selectPoint[1].getX());
				var y1 = Math.max(selectPoint[0].getY(), selectPoint[1].getY());
				
				for (int x = 0; x < Configurations.getWidth(); x++) {
					for (int y = 0; y < Configurations.getHeight(); y++) {
						final var p = Point.create(x, y);
						//if(!p.valid()) continue;
						if((x0 <= x && x <= x1 && y0 <= y && y <= y1)){
							final var cell = Configurations.world.get(p);
							if(cell != null)
								selected.add(cell);
						}
					}
				}
				menu.setCell(selected);
				selectPoint[0] = null;
			}
		}
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        selectPoint[1] = transforms.toWorldPoint(evt);
    }//GEN-LAST:event_formMouseDragged

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        if ( Configurations.getViewer() instanceof DefaultViewer df) {
			final var info = df.getBotInfo();
			if(info.isVisible()) {
				Point point = transforms.toWorldPoint(evt);
				if(point != null && point.valid())
					info.setCell(Configurations.world.get(point));
			}
		}
    }//GEN-LAST:event_formMouseClicked

	@Override
	public void paintComponent(Graphics g) {
		paintComponent((Graphics2D)g,false);
		
	}
	/**Отрисовывает мир на холст
	 * @param g куда рисовать
	 * @param isAll рисовать всё или только то, что видно на экране?
	 */
	public void paintComponent(Graphics2D g, boolean isAll) {
		super.paintComponent(g);
		
		var v = Configurations.getViewer();
		if(!(v instanceof DefaultViewer)) return;
		final var legend = v.get(Legend.class);
		final var settings = v.get(Settings.class);
		
		paintField(g);
		
		if(!settings.isEdit()){
			int r = transforms.toScrin(1);
			if(r > 2){
				//Если у нас радиус больше 2пк, то тут можно рисовать что угодно - от кругов до детальной проработки
				for (int x = 0; x < Configurations.getWidth(); x++) {
					for (int y = 0; y < Configurations.getHeight(); y++) {
						if(isAll || (visible[0].getX() <= x && x <= visible[1].getX()
								&& visible[0].getY() <= y && y <= visible[1].getY())){
							final var pos = Point.create(x, y);
							if(!pos.valid()) continue;					
							final var cell = Configurations.world.get(pos);
							if(cell != null){
								int cx = transforms.toScrinX(cell.getPos());
								int cy = transforms.toScrinY(cell.getPos());
								if(legend.getMode() == Legend.MODE.PROGRAMMER)
									g.setColor(legend.ProgrammerMove(cell));
								else
									g.setColor(cell.getPaintColor(legend));
								cell.paint(g, cx, cy, r);
							}
						}
					}
				}
			} else {
				//А если меньше, то рисовать мы будем самыми общими чертами
				final var dr = transforms.toDScrin(1);
				if(dr <= 0) return;
				final var step =  r >= 1 ? 2 : (int)Math.round(2d/dr);
				final var nr = transforms.toScrin(step);
				final var ritangleColor = new Color[step * step];
				
				for (int x = 0; x < Configurations.getWidth(); x+=step) {
					for (int y = 0; y < Configurations.getHeight(); y+=step) {
						if(isAll || (visible[0].getX() <= (x+step) && x <= visible[1].getX()
								&& visible[0].getY() <= (y+step) && y <= visible[1].getY())){
							var lendhtC = 0;
							for(var dx = 0 ; dx < step; dx++){
								for(var dy = 0 ; dy < step; dy++){
									final var pos = Point.create(x+dx, y+dy);
									if(!pos.valid()) break;					
									final var cell = Configurations.world.get(pos);
									if(cell != null){
										if(legend.getMode() == Legend.MODE.PROGRAMMER)
											ritangleColor[lendhtC++] = legend.ProgrammerMove(cell);
										else
											ritangleColor[lendhtC++] = cell.getPaintColor(legend);
									}
								}
							}
							if(lendhtC > 0){
								g.setColor(AllColors.blend(lendhtC, ritangleColor));
								final var pos = Point.create(x, y);
								int cx = transforms.toScrinX(pos);
								int cy = transforms.toScrinY(pos);
								g.fillRect(cx, cy, nr, nr);
							}
						}
					}
				}
			}
		}
		//Отрисовываем перекрестие на выбранную клетку
		final var infoCell = v.get(BotInfo.class).getCell();
		if(infoCell != null) {
			g.setColor(Color.GRAY);
			g.drawLine(transforms.toScrinX(infoCell.getPos()), 0, transforms.toScrinX(infoCell.getPos()), getHeight());
			g.drawLine(0, transforms.toScrinY(infoCell.getPos()), getWidth(), transforms.toScrinY(infoCell.getPos()));
		}
		//Отрисовываем рамку выбора клеток на поле
		if(selectPoint[0] != null && selectPoint[1] != null){
			g.setColor(new Color(255,255,255,50));
			var x0 = transforms.toScrinX(selectPoint[0]);
			var y0 = transforms.toScrinY(selectPoint[0]);
			var x1 = transforms.toScrinX(selectPoint[1]);
			var y1 = transforms.toScrinY(selectPoint[1]);
			g.fillRect(Math.min(x0, x1), Math.min(y0, y1), Math.abs(x0 - x1), Math.abs(y0 - y1));
		}
		
		if(isAll) return;
		fps.interapt();
		repaint();
	}
	
	/**Закрашивает картину, согласно текущему раскладу
	 * @param g полотно, которое красим
	 */
	private void paintField(Graphics2D g) {
		//Рисуем игровое поле
		switch (Configurations.confoguration.world_type) {
			case LINE_H,LINE_V, RECTANGLE ->{
				//Вода
				colors[1].paint(g);
			}
			case FIELD_R -> {
				//Вода
				colors[1].paint(g);
				//Замаза
				colors[0].paint(g);
				colors[2].paint(g);
			}
			default -> 	throw new AssertionError();
		}
		final var oldC = g.getComposite();
		g.setComposite(AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.6f ));
		if(select.isNull()){
			//А теперь, поверх воды, рисуем минеральки
			Configurations.minerals.paint(g,getTransform());
			//И сонышки
			Configurations.suns.paint(g,getTransform());
			//И и шлефанём всё это потоками
			Configurations.streams.forEach(s -> s.paint(g,getTransform()));
		} else {
			
		}

		g.setComposite(oldC);
		//Рисуем всё остальное
		switch (Configurations.confoguration.world_type) {
			case LINE_H ->{
				//Небо
				colors[0].paint(g);
				//Земля
				colors[2].paint(g);
			}
			case LINE_V ->{
				//Песочки
				colors[0].paint(g);
				colors[2].paint(g);
			}
			case RECTANGLE ->{
				//Нижняя и верхняя части
				colors[0].paint(g);
				colors[2].paint(g);
			}
			case FIELD_R-> {}
			default -> 	throw new AssertionError();
		}
		//Вспомогательное построение
		//Utils.DeprecatedMetods.paintCells(g);
	}
	
	/**Пересчитывает относительные размеры мира в пикселях.*/
	public synchronized void recalculate() {
		var oActiv = Configurations.world.isActiv();
		Configurations.world.awaitStop();
		transforms.recalculate();
		
		updateScrin();
		
		if(oActiv)
			Configurations.world.start();
	}
	
	/**Пересчитывает всю побочную графику на мониторе*/
	public void updateScrin() {
		switch (Configurations.confoguration.world_type) {
			case LINE_H ->{
				//Верхнее небо
				int xs[] = new int[4];
				int ys[] = new int[4];
				//Поле, вода
				int yw[] = new int[4];
				//Дно
				int yb[] = new int[4];

				xs[0] = xs[1] = 0;
				xs[2] = xs[3] = getWidth();
				
				ys[0] = ys[3] = 0;
				ys[1] = ys[2] = yw[0] = yw[3] = transforms.toScrinY(0);
				yb[0] = yb[3] = yw[1] = yw[2] = transforms.toScrinY(Configurations.getHeight() - 1);
				yb[1] = yb[2] = getHeight();
				//Небо
				colors[0] = new ColorRec(xs,ys,AllColors.SKY);
				//Вода
				colors[1] = new ColorRec(xs,yw, AllColors.WATER_POND);
				//Земля
				colors[2] = new ColorRec(xs,yb, AllColors.DRY);
			}
			case LINE_V -> {
				//Левый песочек
				int xl[] = new int[4];
				//Поле, вода
				int xw[] = new int[4];
				int yw[] = new int[4];
				//Правый песочек
				int xr[] = new int[4];
				
				xl[0] = xl[3] = 0;
				xl[1] = xl[2] = xw[0] = xw[3] = transforms.toScrinX(0);
				xw[1] = xw[2] = xr[0] = xr[3] = transforms.toScrinX(Configurations.getWidth()-1);
				xr[1] = xr[2] = getWidth();
				
				yw[0] = yw[1] = 0;
				yw[2] = yw[3] = getHeight();
				//Песочек левый
				colors[0] = new ColorRec(xl,yw,AllColors.SAND);
				//Вода
				colors[1] = new ColorRec(xw,yw, AllColors.WATER_RIVER);
				//Песочек правый
				colors[2] = new ColorRec(xr,yw, AllColors.SAND);
			}
			case RECTANGLE -> {
				//Нижняя часть поля
				final int xd[] = new int[8];
				final int yd[] = new int[8];
				//Поле, вода
				final int xw[] = new int[4];
				final int yw[] = new int[4];
				//Верхняя часть поля
				final int yu[] = new int[8];
				
				xd[0] = xd[1] = 0;
				xd[6] = xd[7] = xw[0] = xw[3] = transforms.toScrinX(0);
				xd[4] = xd[5] = xw[1] = xw[2] = transforms.toScrinX(Configurations.getWidth()-1);
				xd[2] = xd[3] = getWidth();
				
				yd[1] = yd[2] = 0;
				yw[0] = yw[1] = yd[5] = yd[6] = transforms.toScrinY(0);
				yd[0] = yd[3] = yd[4] = yd[7] = yu[0] = yu[3] = yu[4] = yu[7] = transforms.toScrinY(Configurations.getHeight()-3); //Место сшивания полей
				yw[2] = yw[3] = yu[5] = yu[6] = transforms.toScrinY(Configurations.getHeight()-1);
				yu[1] = yu[2] = getHeight();
				
				
				colors[0] = new ColorRec(xd,yd,AllColors.SKY);
				colors[2] = new ColorRec(xd,yu, AllColors.OAK);
				//Вода
				colors[1] = new ColorRec(xw,yw, AllColors.WATER_AQUARIUM);
			}
			case FIELD_R -> {
				//Поле, вода
				final int xw[] = new int[4];
				final int yw[] = new int[4];
				//Верхний прямоугольник
				final int xu[] = new int[4];
				final int yu[] = new int[4];
				//Нижний блок
				final int xd[] = new int[8];
				final int yd[] = new int[8];
				
				xu[0] = xu[3] = xd[0] = xd[7] = 0;
				xd[1] = xd[2] = xw[0] = xw[3] = transforms.toScrinX(0);
				xd[3] = xd[4] = xw[1] = xw[2] = transforms.toScrinX(Configurations.getWidth()-1);
				xd[5] = xd[6] = xu[1] = xu[2] = getWidth();
				
				yu[0] = yu[1] = 0;
				yd[0] = yd[0] = yd[1] = yd[4] = yd[5] = yu[2] = yu[3] = yw[0] = yw[1] = transforms.toScrinY(0);
				yd[2] = yd[3] = yw[2] = yw[3] = transforms.toScrinY(Configurations.getHeight()-1);
				yd[6] = yd[7] = getHeight();
				
				colors[0] = new ColorRec(xu,yu, AllColors.WATER_OCEAN);
				colors[1] = new ColorRec(xw,yw, AllColors.WATER_OCEAN);
				colors[2] = new ColorRec(xd,yd, AllColors.WATER_OCEAN);
				
			}
			default -> 	throw new AssertionError();
		}
	}
	
	/**Сохраняет размеры в координатах мира отображаемого прямоугольника
	 * @param leftUp верхний левый угол прямоугольника
	 * @param rightDown нижний правый угол прямоугольника 
	 */
	public final void setVisible(Point leftUp, Point rightDown){
		visible[0] = leftUp;
		visible[1] = rightDown;
	}
	/**Возвращает сколкьо процентов экрана сверху нужно игровому полю для отрисовки дополнительных
	 * элементов как минимум
	 * @return процент от размера мира на необходимые элементы
	 */
	public double getUborder(){
		return switch (Configurations.confoguration.world_type) {
			case LINE_H,RECTANGLE -> Transforms.UP_DOWN_border.getX();
			case LINE_V,FIELD_R -> 0d;
			default -> throw new AssertionError();
		};
	}
	/**Возвращает сколкьо процентов экрана снизу нужно игровому полю для отрисовки дополнительных
	 * элементов как минимум
	 * @return процент от размера мира на необходимые элементы
	 */
	public double getDborder(){
		return switch (Configurations.confoguration.world_type) {
			case LINE_H,RECTANGLE -> Transforms.UP_DOWN_border.getY();
			case LINE_V,FIELD_R -> 0d;
			default -> throw new AssertionError();
		};
	}
	/**Возвращает сколкьо процентов экрана слева нужно игровому полю для отрисовки дополнительных
	 * элементов как минимум
	 * @return процент от размера мира на необходимые элементы
	 */
	public double getLborder(){
		return switch (Configurations.confoguration.world_type) {
			case LINE_H,FIELD_R -> 0d;
			case LINE_V,RECTANGLE -> Transforms.LEFT_RIGHT_border.getX();
			default -> throw new AssertionError();
		};
	}
	/**Возвращает сколкьо процентов экрана справа нужно игровому полю для отрисовки дополнительных
	 * элементов как минимум
	 * @return процент от размера мира на необходимые элементы
	 */
	public double getRborder(){
		return switch (Configurations.confoguration.world_type) {
			case LINE_H,FIELD_R -> 0d;
			case LINE_V,RECTANGLE -> Transforms.LEFT_RIGHT_border.getY();
			default -> throw new AssertionError();
		};
	}
	/**Возвращает преобразователь координат
	 * @return класс, который может преобразовать коррдинаты из ячеестых в пиксельные и наоборот
	 */
	public Transforms getTransform(){return transforms;}
	/**Добавляет к текущему зуму ещё кусочек. Или убирает кусочек.
	 * В общем изменяет на сколько пользователь будет видеть экран
	 * @param add на сколько нужно изменить зум
	 */
	public void addZoom(int add) {zoom = Math.max(1, getZoom() + add);}
	
	/**Возвращает текущее увеличение размера карты, относительно возможности отображения на экране
	 * @return если 1, то мир без скрола. Если 2, то мир примерно в 2 раза больше, чем может увидеть пользователь
	 */
	public int getZoom() {return zoom;}
	

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

	/**Увеличение экрана, на сколько он больше, чем 1 к 1*/
	private int zoom = 1;
	/**Координаты видимой части экрана. Специально для перерисовки только ограниченной части. Две точки - верхний угол и нижний*/
	private final Point[] visible = new Point[2];
	/**Координаты выделения клеток при использовании мыши в качестве выделителя*/
	private final Point[] selectPoint = new Point[2];
	/**Счётчик шагов. Puls Per Second*/
	public final FPScounter fps = new FPScounter();
	/**Все цвета, которые мы должны отобразить на поле*/
	private final ColorRec [] colors = new ColorRec[3];
	/**Преобразователь из одних координат в другие*/
	private final Transforms transforms = new Transforms();
	/**Тот объект на экране, что мы должны вделить при редактировании*/
	private final Variant select = new Variant(SunAbstract.class, MineralAbstract.class,Trajectory.class);
}
