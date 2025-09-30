/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI.WorldAnimation;

import Calculations.Configurations;
import Calculations.Point;
import GUI.AllColors;
import GUI.WorldView;
import Utils.ColorRec;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import kerlib.StableValue;

/**
 * Анимация для чашки петри.
 * Тут у нас есть посредине эллипс и вокруг него... Стол?
 * 
 * @author Kerravitarr
 */
public class Microscope extends DefaultAnimation{
    ///Инструмент, который реально лежит на столе
    private class Instrument {
        ///Тип инструмента
        enum Type {
            TWEEZERS(20,30),
            FLASK(10,10),
            TEST_TUBE(10,40),
            REFRIGERATOR(20,40);
            //Ширина объкта в клетках поля
            final int width;
            ///Высота объекта в клетках поля
            final int height;

            private Type(int width, int height) {
                this.width = width;
                this.height = height;
            }            
        }
        ///Проекция прямоугольника на ось. Эта сама проекция, поэтому только две точки
        private record Projection(double min,double max){
            boolean overlaps(Projection other) {
                return this.max >= other.min && this.min <= other.max;
            }
        }

        public Instrument(double cx, double cy, Type type,double size, double rotation) {
            var transform = new AffineTransform();
            transform.rotate(rotation, cx, cy);
            var w = type.width * size;
            var h = type.height * size;
            {
                Point2D[] initialCorners = {
                        new Point2D.Double(cx - w / 2, cy - h / 2), // Top-left
                        new Point2D.Double(cx + w / 2, cy - h / 2), // Top-right
                        new Point2D.Double(cx + w / 2, cy + h / 2), // Bottom-right
                        new Point2D.Double(cx - w / 2, cy + h / 2)  // Bottom-left
                };
                for (var corner : initialCorners) {
                    var transformed = new Point2D.Double();
                    transform.transform(corner, transformed);
                    corners.add(transformed);
                }
            }
        }
        ///Првоеряет, что два инструменты не пересекаются
        ///Осуществляется методом ЫФЕ
        ///SAT утверждает, что два выпуклых многоугольника не пересекаются, если существует ось, на которую их проекции не пересекаются.
        /// Для прямоугольников, такими осями являются:
        ///     Оси, перпендикулярные сторонам первого прямоугольника.
        ///     Оси, перпендикулярные сторонам второго прямоугольника.
        public boolean intersects(Instrument other) {
            return Stream.concat(this.axes.get().stream(), other.axes.get().stream())
                .allMatch(axis -> {
                    var p1 = project(this, axis);
                    var p2 = project(other, axis);
                    return p1.overlaps(p2); // Найдена ось разделения, прямоугольники не пересекаются
                });
        }
        // Проецировать прямоугольник на заданную ось
        private static Projection project(Instrument rect, Point2D axis) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            // Нормализуем ось (чтобы проекция была в скалярном виде)
            var axisLength = Math.sqrt(axis.getX() * axis.getX() + axis.getY() * axis.getY());
            if (axisLength == 0) return new Projection(0, 0); // На случай нулевого вектора

            var normalizedAxisX = axis.getX() / axisLength;
            var normalizedAxisY = axis.getY() / axisLength;

            for (var corner : rect.corners) {
                // Скалярное произведение (dot product)
                double dotProduct = corner.getX() * normalizedAxisX + corner.getY() * normalizedAxisY;
                min = Math.min(min, dotProduct);
                max = Math.max(max, dotProduct);
            }
            return new Projection(min, max);
        }
        
        ///Вершины этого объекта
        private final List<Point2D> corners = new ArrayList<>(4);
        ///Собственно мы
        private final StableValue<Polygon> poligion = StableValue.supplier(() -> {
            int[] xPoints = new int[corners.size()];
            int[] yPoints = new int[corners.size()];
            for (int i = 0; i < corners.size(); i++) {
                xPoints[i] = (int) Math.round(corners.get(i).getX()); // Polygon принимает int
                yPoints[i] = (int) Math.round(corners.get(i).getY());
            }
            return new Polygon(xPoints, yPoints, corners.size());
        });
        ///Оси для SAT
        private final StableValue<List<Point2D>> axes = StableValue.supplier(() -> {
            var ret = new ArrayList<Point2D>(corners.size());
            for (int i = 0; i < corners.size(); i++) {
                 var p1 = corners.get(i);
                 var p2 = corners.get((i + 1) % corners.size()); // Следующая вершина, замыкая цикл
                 // Вектор стороны
                 double edgeX = p2.getX() - p1.getX();
                 double edgeY = p2.getY() - p1.getY();
                 // Перпендикулярный вектор (rotated 90 degrees)
                 // Если вектор (x, y), то перпендикулярный (-y, x)
                 ret.add(new Point2D.Double(-edgeY, edgeX));
             } 
            return ret;
        });
    }
    ///Основной набор статических переменных, для работы после обновления экрана
    private static class Static {
        ///Инструменты, которые лежат на столе
        List<Instrument> set = new ArrayList();
        
        
        List<java.util.function.Consumer<Graphics2D>> tmp = new ArrayList();
    }
    
	/**стол сверху*/
	private ColorRec table0;
	/**водичка*/
	private ColorRec water;
	/**стол снизу*/
	private ColorRec table1;
	
	public Microscope(WorldView.Transforms transform, int w, int h){
		super(transform);
        if(state == null) state = new Static();
        
		var a2 = Configurations.getWidth();
		var b2 = Configurations.getHeight();
		var a = a2/2d;
		var b = b2/2d;
		//Поле, вода
		final var xw = new ArrayList<Integer>(a2 * b2);
		final var yw = new ArrayList<Integer>(a2 * b2);
		//Верхняя половина стола
		final var xut = new ArrayList<Integer>(a2 * b2);
		final var yut = new ArrayList<Integer>(a2 * b2);
		//Нижняя половина стола
		final var xdt = new ArrayList<Integer>(a2 * b2);
		final var ydt = new ArrayList<Integer>(a2 * b2);

		//Прочёсываем все точки слева направо, в поисках первых (верхних) наших 
		xut.add(0);
		yut.add(transform.toScrinY(b));
		boolean isFirst = true; //Флаг, чтобы первая точка в обязательном порядке была посредине
		for(var x = 0; x < a2; x++){
			var y = 0;
			for(; y < b2; y++){
				final var point = Point.create(x, y);
				if(point.valid()){
					final int sx = transform.toScrinX(x);
					final int sy;
					if(isFirst){
						isFirst = false;
						sy = transform.toScrinY(b);
					} else {
						sy = transform.toScrinY(y);
					}
					xw.add(sx);
					yw.add(sy);
					xut.add(sx);
					yut.add(sy);
					break;
				}
			}
			if(y == b2 && x > a){ //Когда мы не встретим ни одной правильной точки и пройдём больше половины пути по X - мы в конце. Заканчиваем
				break;
			}
		}
		xut.add(xut.get(xut.size()-1));
		yut.add(transform.toScrinY(b));
		xut.add(w);
		yut.add(transform.toScrinY(b));
		xut.add(w);
		yut.add(0);
		xut.add(0);
		yut.add(0);
		//А теперь пройдём тоже самое, но в обратную сторону
		xdt.add(w);
		ydt.add(transform.toScrinY(b));
		isFirst = true;
		for(var x = a2-1; x >= 0; x--){
			var y = b2-1;
			for(; y >= 0; y--){
				final var point = Point.create(x, y);
				if(point.valid()){
					final int sx = transform.toScrinX(x);
					final int sy;
					if(isFirst){
						isFirst = false;
						sy = transform.toScrinY(b);
					} else {
						sy = transform.toScrinY(y);
					}
					xw.add(sx);
					yw.add(sy);
					xdt.add(sx);
					ydt.add(sy);
					break;
				}
			}
			if(y == 0 && x < a){ //Когда мы не встретим ни одной правильной точки и пройдём больше половины пути по X - мы в самом начале. Заканчиваем
				break;
			}
		}
		xdt.add(xdt.get(xdt.size()-1));
		ydt.add(transform.toScrinY(b));
		xdt.add(0);
		ydt.add(transform.toScrinY(b));
		xdt.add(0);
		ydt.add(h);
		xdt.add(w);
		ydt.add(w);
		water = new ColorRec(xw.stream().mapToInt(Integer::intValue).toArray(),yw.stream().mapToInt(Integer::intValue).toArray(), AllColors.GLASS);
		table0 = new ColorRec(xut.stream().mapToInt(Integer::intValue).toArray(),yut.stream().mapToInt(Integer::intValue).toArray(),AllColors.OAK);
		table1 = new ColorRec(xdt.stream().mapToInt(Integer::intValue).toArray(),ydt.stream().mapToInt(Integer::intValue).toArray(), AllColors.OAK);
        
        var maxSize = Math.sqrt(w*w+h*h);
        var cellSize = transform.getDZScrin();
        var A = cellSize * a;
        var B = cellSize * b;
        var cx = w/2;
        var cy = h/2;
        state.tmp.clear();
        state.set.clear();
        var startHash = a2*b2;
        var rec = new java.awt.Rectangle.Double(cx-A,cy-B,A*2,B*2);
        for (double i = Math.max(transform.toScrinX(0), transform.toScrinY(0)); i < maxSize; i+=cellSize) {
            //А теперь начинаем гулять по всем возможным точкам
            //Длина эллипса в этом месте
            var P = 2*Math.PI * Math.sqrt((a*a+b*b)/8);
            for (double angl = 0; angl < Math.PI*2;) {
                //0.1 - плотность точек
                var gen = new kerlib.random.Hash(++startHash);
                angl += gen.nextDouble(0, Math.PI*2 / (0.001 * P));
                var x = cx + A * Math.cos(angl);
                var y = cy + B * Math.sin(angl);
                var rot = gen.nextDouble(Math.PI*2);
                var type = gen.next(Instrument.Type.class);
                var next = new Instrument(x, y, type,cellSize, rot);
                if(state.set.stream().allMatch(p -> !p.intersects(next))){                    
                    state.set.add(next);
                    state.tmp.add(g -> {
                        g.drawPolygon(next.poligion.get());
                    });
                }
            }
            A += transform.getDZScrin();
            B += transform.getDZScrin();
            ++a;
            ++b;
        }
                
	}

	@Override
	public void water(Graphics2D g) {
		water.paint(g);
	}

	@Override
	public void world(Graphics2D g, Rectangle visible, java.awt.geom.Area field) {
		table0.paint(g);
		table1.paint(g);
        g.setColor(Color.RED);
        state.tmp.forEach(t -> t.accept(g));
	}
	
	///Текущее состояние стола
	private static Static state;
}
