/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import Calculations.Configurations;
import static Calculations.Configurations.WORLD_TYPE.LINE_H;
import Calculations.Point;
import MapObjects.CellObject;
import Utils.ColorRec;
import Utils.FPScounter;
import Utils.Utils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

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
	   public int toScrinX(int x) {return (int) Math.round(x*scalePxPerCell + pixelXDel);}
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
	   public int toScrinY(int y) {return (int) Math.round(y*scalePxPerCell + pixelYDel);}
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
	   public int toScrin(int r) {return (int) Math.round(r * scalePxPerCell) ;}
	   /**
		* Переводит координаты экрана в координаты мира
		* @param x координата на экране
		* @return x координата на поле. Эта координата может выходить за размеры мира!!!
		*/
	   public int toWorldX(int x) {return (int) Math.round((x - border.width)/scalePxPerCell - 0.5);}
	   /**
		* Переводит координаты экрана в координаты мира
		* @param y координата на экране
		* @return x координата на поле. Эта координата может выходить за размеры мира!!!
		*/
	   public int toWorldY(int y) {return (int) Math.round((y - border.height)/scalePxPerCell - 0.5);}
	   
		/** Специальная функция, которая обновляет все масштабные коэффициенты */
		private void recalculate() {
			//Размеры мира, с учётом обязательного запаса
			final double h = getHeight();
			final double w = getWidth();
			//Пересчёт размера мира
			scalePxPerCell = Math.min(h * (1d - getUborder() - getDborder()) / (Configurations.getHeight()), w * (1d - getLborder() - getRborder()) / (Configurations.getWidth()));
			switch (Configurations.confoguration.world_type) {
				case LINE_H -> {
					border.width = 0;
					border.height = (int) Math.round((h - Configurations.getHeight() * scalePxPerCell) / 2);
					pixelXDel = (w - Configurations.getWidth() * scalePxPerCell) / 2;
					pixelYDel = border.height + scalePxPerCell / 2;
				}
				case LINE_V -> {
					border.width = (int) Math.round((w - Configurations.getWidth() * scalePxPerCell) / 2);
					border.height = 0;
					pixelXDel = border.width + scalePxPerCell / 2;
					pixelYDel = (h - Configurations.getHeight() * scalePxPerCell) / 2;
				}
				default ->
					throw new AssertionError();
			}
		}
	}

	/** Creates new form WorldView */
	public WorldView() {
		initComponents();
		setVisible(new Point(0,0), new Point(Configurations.getWidth()-1, Configurations.getHeight()-1));
		recalculate();
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
				selectPoint[1] = recalculation(evt.getX(),evt.getY());
				selectPoint[0] = selectPoint[1];
			}
		}
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        selectPoint[1] = recalculation(evt.getX(),evt.getY());
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
						final var p = new Point(x, y);
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
        selectPoint[1] = recalculation(evt.getX(),evt.getY());
    }//GEN-LAST:event_formMouseDragged

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        if ( Configurations.getViewer() instanceof DefaultViewer df) {
			final var info = df.getBotInfo();
			if(info.isVisible()) {
				Point point = recalculation(evt.getX(),evt.getY());
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
		var legend = ((DefaultViewer) v ).getLegend();
		
		paintField(g);
		int r = transforms.toScrin(1);
		for (int x = 0; x < Configurations.getWidth(); x++) {
			for (int y = 0; y < Configurations.getHeight(); y++) {
				if(isAll || (visible[0].getX() <= x && x <= visible[1].getX()
						&& visible[0].getY() <= y && y <= visible[1].getY())){
					final var pos = new Point(x, y);
					//if(!pos.valid()) continue;					
					final var cell = Configurations.world.get(pos);
					if(cell != null){
						int cx = transforms.toScrinX(cell.getPos());
						int cy = transforms.toScrinY(cell.getPos());
						cell.paint(g, legend, cx, cy, r);
					}
				}
			}
		}
		final var view = Configurations.getViewer();
		final var infoCell = view instanceof DefaultViewer ? ((DefaultViewer) view).getBotInfo().getCell() : null;
		if(infoCell != null) {
			g.setColor(Color.GRAY);
			g.drawLine(transforms.toScrinX(infoCell.getPos()), 0, transforms.toScrinX(infoCell.getPos()), getHeight());
			g.drawLine(0, transforms.toScrinY(infoCell.getPos()), getWidth(), transforms.toScrinY(infoCell.getPos()));
		}
		if(selectPoint[0] != null && selectPoint[1] != null){
			g.setColor(new Color(255,255,255,25));
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
			case LINE_H,LINE_V ->{
				//Вода
				colors[1].paint(g);
			}
			default -> 	throw new AssertionError();
		}
		
		//А теперь, поверх воды, рисуем солнышки
		Configurations.suns.forEach(s -> s.paint(g,getTransform()));
		//И минералки
		Configurations.minerals.forEach(s -> s.paint(g,getTransform()));
		//И и шлефанём всё это потоками
		Configurations.streams.forEach(s -> s.paint(g,getTransform()));
		
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
			default -> 	throw new AssertionError();
		}
		//Вспомогательное построение
		//paintCells(g);
		//paintProc(g);
	}
	/**Вспомогательная, отладочная функция, рисования клеток поля
	 * @param g 
	 */
	private void paintCells(Graphics2D g) {
		int r = transforms.toScrin(1);
		for (int x = 0; x < Configurations.getWidth(); x++) {
			for (int y = 0; y < Configurations.getHeight(); y++) {
				final var pos = new Point(x, y);
				if(!pos.valid()) continue;
				if(x % 10 == 0)
					g.setColor(Color.RED);
				else if(y % 10 == 0)
					g.setColor(Color.YELLOW);
				else
					g.setColor(Color.BLACK);
				int cx = transforms.toScrinX(pos);
				int cy = transforms.toScrinY(pos);
				g.drawRect(cx-r/2, cy-r/2,r, r);
			}
		}
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
				colors[1] = new ColorRec(xs,yw, AllColors.WATER);
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
				colors[1] = new ColorRec(xw,yw, AllColors.WATER);
				//Песочек правый
				colors[2] = new ColorRec(xr,yw, AllColors.SAND);
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
			case LINE_H -> Transforms.UP_DOWN_border.getX();
			case LINE_V -> 0d;
			default -> throw new AssertionError();
		};
	}
	/**Возвращает сколкьо процентов экрана снизу нужно игровому полю для отрисовки дополнительных
	 * элементов как минимум
	 * @return процент от размера мира на необходимые элементы
	 */
	public double getDborder(){
		return switch (Configurations.confoguration.world_type) {
			case LINE_H -> Transforms.UP_DOWN_border.getY();
			case LINE_V -> 0d;
			default -> throw new AssertionError();
		};
	}
	/**Возвращает сколкьо процентов экрана слева нужно игровому полю для отрисовки дополнительных
	 * элементов как минимум
	 * @return процент от размера мира на необходимые элементы
	 */
	public double getLborder(){
		return switch (Configurations.confoguration.world_type) {
			case LINE_H -> 0d;
			case LINE_V -> Transforms.LEFT_RIGHT_border.getX();
			default -> throw new AssertionError();
		};
	}
	/**Возвращает сколкьо процентов экрана справа нужно игровому полю для отрисовки дополнительных
	 * элементов как минимум
	 * @return процент от размера мира на необходимые элементы
	 */
	public double getRborder(){
		return switch (Configurations.confoguration.world_type) {
			case LINE_H -> 0d;
			case LINE_V -> Transforms.LEFT_RIGHT_border.getY();
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
	
	/**
	 * Пересчитыавет координаты мировые в пикселях в координаты ячейки
	 * @param x экранная координата х
	 * @param y экранная координата у
	 * @return точку в реальном пространстве или null, если эта точка за гранью
	 */
	private Point recalculation(int x, int y) {
		x = Utils.betwin(Transforms.border.width, x, getWidth() - Transforms.border.width);
		y = Utils.betwin(Transforms.border.height, y, getWidth() - Transforms.border.height);
		return new Point(transforms.toWorldX(x),transforms.toWorldY(y));
	}

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
	

}
