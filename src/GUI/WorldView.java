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
import Calculations.Streams.StreamAbstract;
import Calculations.Trajectories.Trajectory;
import GUI.WorldAnimation.DefaultAnimation;
import MapObjects.CellObject;
import Utils.ColorRec;
import Utils.FPScounter;
import Utils.Variant;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


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
		public int toScrinX(double x) {return (int) Math.round(toDScrinX(x));}
		/**
		* Переводит координаты мира в координаты экрана
		* @param x координата в масштабах клеток
		* @return x координата в масштабе окна, пк
		*/
		public double toDScrinX(double x) {return x*scalePxPerCell + pixelXDel;}
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
		public int toScrinY(double y) {return (int) Math.round(toDScrinY(y));}
	   /**
		* Переводит координаты мира в координаты экрана
		* @param y координата в масштабах клетки
		* @return y координата в масштабе окна, пк
		*/
		public double toDScrinY(double y) {return y*scalePxPerCell + pixelYDel;}
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
		public int toScrin(double r) {return (int) Math.round(toDScrin(r));}
	   /**Возвращает размеры мира в пикселях
		* @param r размер объекта в клетках мира
		* @return радиус объекта в пикселях
		*/
		public double toDScrin(double r) {return r * scalePxPerCell;}
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
							if(cell != null && menu.isVisibleCell(cell))
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
		try{
			paintComponent((Graphics2D)g,false);
		} catch(Exception ex){ //Вообще не ожидаются такие события... Но кто мы такие, чтобы спорить с фактами?
			Logger.getLogger(WorldView.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
	}
	/**Отрисовывает мир на холст
	 * @param g куда рисовать
	 * @param isAll рисовать всё или только то, что видно на экране?
	 */
	public void paintComponent(Graphics2D g, boolean isAll) {
		super.paintComponent(g);
		
		var v = Configurations.getViewer();
		if(!(v instanceof DefaultViewer)) return;
		
		final var cms = System.currentTimeMillis();
		if(cms > lastUpdate){
			lastUpdate = cms + 1000/25; //25 кадров в секунду
			frame++;
			if(cms > lastSelected){
				lastSelected = cms + SELECT_PERIOD / 2; //Половина, потому что за период мы должны дважды переключиться
				isSelected = !isSelected;
			}
		}
		
		paintField(g);
		paintCells(g, isAll);
		
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
		animation.water(g, frame);
		//И рисуем бордюр когда он должен быть под объектами
		if(Configurations.confoguration.world_type == Configurations.WORLD_TYPE.FIELD_R)
			animation.world(g);
		{ //Теперь рисуем звёзды, минералы и прочее
			final var oldC = g.getComposite();
			g.setComposite(AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.6f ));

			final var mineralSelect = (select.isNull() || !isSelected || !select.isContains(MineralAbstract.class)) ? null : select.get(MineralAbstract.class);
			final var sunSelect = (select.isNull() || !isSelected || !select.isContains(SunAbstract.class)) ? null : select.get(SunAbstract.class);
			final var streamSelect = (select.isNull() || !isSelected || !select.isContains(StreamAbstract.class)) ? null : select.get(StreamAbstract.class);
			//А теперь, поверх воды, рисуем минеральки
			Configurations.minerals.paint(g,getTransform(), mineralSelect);
			//И сонышки
			Configurations.suns.paint(g,getTransform(), sunSelect);
			//И и шлефанём всё это потоками
			if(streamSelect == null){
				Configurations.streams.forEach(s -> s.paint(g, getTransform(), frame));
			} else {
				boolean isPrint = false;
				for(final var s : Configurations.streams){
					if(s != streamSelect) s.paint(g, getTransform(), frame);
					else isPrint = true;
				}
				if(!isPrint) streamSelect.paint(g, getTransform(), frame);
			}

			//И, если нужно, нанесём траекторию
			if (select.isContains(Trajectory.class)) {
				select.get(Trajectory.class).paint(g, getTransform(), frame);
			}

			g.setComposite(oldC);
		}
		//Рисуем всё остальное
		if(Configurations.confoguration.world_type != Configurations.WORLD_TYPE.FIELD_R)
			animation.world(g);
		//Вспомогательное построение
		//Utils.DeprecatedMetods.paintCells(g);
	}
	/**Рисует на холсте клетки
	 * @param g холст
	 * @param isAll нужно рисовать прям всё или только то, что видно на экране?
	 */
	private void paintCells(Graphics2D g, boolean isAll) {
		final var legend = Configurations.getViewer().get(Legend.class);
		final var settings = Configurations.getViewer().get(Settings.class);
		final var menu = Configurations.getViewer().get(Menu.class);
		
		if (!settings.isEdit()) {
			int r = transforms.toScrin(1);
			if (r > 2) {
				//Если у нас радиус больше 2пк, то тут можно рисовать что угодно - от кругов до детальной проработки
				for (int x = 0; x < Configurations.getWidth(); x++) {
					for (int y = 0; y < Configurations.getHeight(); y++) {
						if(isAll || (visible[0].getX() <= x && x <= visible[1].getX()
								&& visible[0].getY() <= y && y <= visible[1].getY())){
							final var pos = Point.create(x, y);
							if(!pos.valid()) continue;					
							final var cell = Configurations.world.get(pos);
							if(cell != null && menu.isVisibleCell(cell)){
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
				if (dr <= 0) return;
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
									if(cell != null && menu.isVisibleCell(cell)){
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
	}
	
	/**Пересчитывает относительные размеры мира в пикселях.*/
	public synchronized void recalculate() {
		var oActiv = Configurations.world.isActiv();
		Configurations.world.awaitStop();
		transforms.recalculate();
		
		animation = switch (Configurations.confoguration.world_type) {
			case LINE_H -> new GUI.WorldAnimation.Pond(transforms, getWidth(), getHeight());
			case LINE_V -> new GUI.WorldAnimation.River(transforms, getWidth(), getHeight());
			case RECTANGLE -> new GUI.WorldAnimation.Aquarium(transforms, getWidth(), getHeight());
			case FIELD_R -> new GUI.WorldAnimation.Ocean(transforms, getWidth(), getHeight());
			case CIRCLE -> new GUI.WorldAnimation.Microscope(transforms, getWidth(), getHeight());
			default -> throw new AssertionError("Не реализовано для " + Configurations.confoguration.world_type);
		};
		
		if(oActiv)
			Configurations.world.start();
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
			case LINE_V,FIELD_R,CIRCLE -> 0d;
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
			case LINE_V,FIELD_R,CIRCLE -> 0d;
			default -> throw new AssertionError();
		};
	}
	/**Возвращает сколкьо процентов экрана слева нужно игровому полю для отрисовки дополнительных
	 * элементов как минимум
	 * @return процент от размера мира на необходимые элементы
	 */
	public double getLborder(){
		return switch (Configurations.confoguration.world_type) {
			case LINE_H,FIELD_R,CIRCLE -> 0d;
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
			case LINE_H,FIELD_R,CIRCLE -> 0d;
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
	/**@return теущйий выделенный объект или null*/
	public Object getSelect() {
		if(select.isNull()) return null;
		else if(select.isContains(SunAbstract.class)) return select.get(SunAbstract.class);
		else if(select.isContains(MineralAbstract.class)) return select.get(MineralAbstract.class);
		else if(select.isContains(Trajectory.class)) return select.get(Trajectory.class);
		else if(select.isContains(StreamAbstract.class)) return select.get(StreamAbstract.class);
		else throw new UnsupportedOperationException("Не ожидали того, что получили");
	}
	/**@param select Сохраняет выделение на этом потоке*/
	public void setSelect(StreamAbstract select){
		this.select.set(select);
	}
	/**@param select Сохраняет выделение на этом объекте*/
	public void setSelect(SunAbstract select){
		this.select.set(select);
	}
	/**@param select Сохраняет выделение на этом объекте*/
	public void setSelect(MineralAbstract select){
		this.select.set(select);
	}
	/**@param select Сохраняет выделение на этом объекте*/
	public void setSelect(Trajectory select){
		this.select.set(select);
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
	/**Класс, отвечающий за красивое поле и его окружение*/
	private DefaultAnimation animation = null;
	/**Все цвета, которые мы должны отобразить на поле*/
	private final ColorRec [] colors = new ColorRec[3];
	/**Преобразователь из одних координат в другие*/
	private final Transforms transforms = new Transforms();
	/**Тот объект на экране, что мы должны вделить при редактировании*/
	private final Variant select = new Variant(SunAbstract.class, MineralAbstract.class,Trajectory.class, StreamAbstract.class);
	/**Номер кадра для рисования*/
	protected static int frame = Integer.MAX_VALUE / 2;
	/**Время последнего обновления счётчика кадров*/
	private static long lastUpdate = 0;
	/**Время последнего обновления для выделения определённого объекта*/
	private static long lastSelected = 0;
	/**Выделяем мы сейчас объект или нет?*/
	private static boolean isSelected = false;
	/**Как часто надо мигать объектами на экране. Это время периода!, мс*/
	private static final long SELECT_PERIOD = 1000 * 40 / 60; // 40 миганий в минуту... Образование моё такое :)
}
