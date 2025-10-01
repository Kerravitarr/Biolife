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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import kerlib.StableValue;

/**
 * Анимация для чашки петри.
 * Тут у нас есть посредине эллипс и вокруг него... Стол?
 * 
 * @author Kerravitarr
 */
public class Microscope extends DefaultAnimation{
    private static abstract class Printer {
        ///Собственно мы, инструмент, который мы отрисовываем
        protected final Instrument our;

        public Printer(Instrument our) {
            this.our = our;
        }
        
        public void paint(Graphics2D g, Rectangle visible, java.awt.geom.Area field){
            var ot = g.getTransform();
            var transform = new AffineTransform();
            transform.translate(our.not_rotation.getCenterX(), our.not_rotation.getCenterY());
            transform.rotate(our.angle);
            transform.translate(-our.not_rotation.width/2, -our.not_rotation.height/2);
            g.setTransform(transform);
            
            //g.setColor(Color.WHITE);
            //g.fillRect(0, 0, (int)our.not_rotation.width, (int)our.not_rotation.height);
            g.setStroke(new BasicStroke(3f));
            g.setColor(Color.WHITE);
            paint(g, visible, field, our.not_rotation.width, our.not_rotation.height);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1f));
            paint(g, visible, field, our.not_rotation.width, our.not_rotation.height);
            
            g.setTransform(ot);
        }
        protected abstract void paint(Graphics2D g, Rectangle visible, Area field, double width, double height);

    }
    private static class Tweezers extends Printer {
        private Tweezers(Instrument i) {super(i);}
        @Override public void paint(Graphics2D g, Rectangle visible, Area field,double width, double height) {
            
            //А теперь будем рисовать пинцет.
            //Пускай это будет самый простой вариант. Шарнир и две дуги для губ
            var pivotRadius = Math.max(width,height) * 0.08; // Радиус шарнира
            double x0 = width/2, y0 = pivotRadius/2;
            // Рисуем шарнир (кружок)
            kerlib.draw.tools.fillCircle(g,x0, y0, pivotRadius);
            ///А теперь рисуем две губы
            var tweezersPath = new Path2D.Double();
            tweezersPath.moveTo(x0, y0);
            tweezersPath.quadTo(width, height/2, width*2/3, height);
            tweezersPath.moveTo(x0, y0);
            tweezersPath.quadTo(0, height/2, width*1/3, height);

            // Рисуем сам пинцет
            g.draw(tweezersPath);
        }
    }
    private static class ReverseTweezers extends Printer {
        private ReverseTweezers(Instrument i) {super(i);}
        @Override public void paint(Graphics2D g2d, Rectangle visible, Area field,double width, double height) {
            g2d.setColor(Color.BLACK);
            
        }
    }
    ///Инструмент, который реально лежит на столе
    private static class Instrument {
        ///Тип инструмента
        enum Type {
            ///Пинцет
            TWEEZERS(10,30, i -> new Tweezers(i)),
            ///Обратный пинцет
            //REVERSE_TWEEZERS(20,30, i -> new ReverseTweezers(i)),
            ;
            //Ширина объкта в клетках поля
            final int width;
            ///Высота объекта в клетках поля
            final int height;
            ///Генератор рисовалки этого инструмента
            final java.util.function.Function<Instrument,Printer> generator;

            private Type(int width, int height, Function<Instrument, Printer> generator) {
                this.width = width*2;
                this.height = height*2;
                this.generator = generator;
            }

                 
        }
        ///Проекция прямоугольника на ось. Эта сама проекция, поэтому только две точки
        private record Projection(double min,double max){
            boolean overlaps(Projection other) {
                return this.max >= other.min && this.min <= other.max;
            }
        }

        public Instrument(double cx, double cy, Type type,double size, double rotation) {
            this.type = type;
            this.printer = StableValue.supplier(() -> type.generator.apply(this));
            var transform = new AffineTransform();
            transform.rotate(angle = rotation, cx, cy);
            var w = type.width * size;
            var h = type.height * size;
            not_rotation = new Rectangle.Double(cx - w / 2, cy - h / 2,w,h);
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
        ///Првоеряет, что два инструменты не пересекаются
        ///Осуществляется методом ЫФЕ
        ///SAT утверждает, что два выпуклых многоугольника не пересекаются, если существует ось, на которую их проекции не пересекаются.
        /// Для прямоугольников, такими осями являются:
        ///     Оси, перпендикулярные сторонам первого прямоугольника.
        ///     Оси, перпендикулярные сторонам второго прямоугольника.
        public boolean intersects(Instrument other) {
            return bounds.get().intersects(other.bounds.get()) 
                    && Stream.concat(this.axes.get().stream(), other.axes.get().stream())
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
        
        ///Тип
        private final Type type;
        ///Прямоугольник, который мы и который при этом не повёрнут ни на какой угол
        public final Rectangle.Double not_rotation;
        ///Угол вращения, радианы
        public final double angle;
        ///Вершины этого объекта
        private final List<Point2D> corners = new ArrayList<>(4);
        ///Собственно мы
        private final Supplier<Polygon> poligion = StableValue.supplier(() -> {
            int[] xPoints = new int[corners.size()];
            int[] yPoints = new int[corners.size()];
            for (int i = 0; i < corners.size(); i++) {
                xPoints[i] = (int) Math.round(corners.get(i).getX()); // Polygon принимает int
                yPoints[i] = (int) Math.round(corners.get(i).getY());
            }
            return new Polygon(xPoints, yPoints, corners.size());
        });
        ///Оси для SAT
        private final Supplier<List<Point2D>> axes = StableValue.supplier(() -> {
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
        ///Очень грубый, ограничивающий прямоугольник
        private final Supplier<Rectangle> bounds = StableValue.supplier(() -> poligion.get().getBounds() );
        ///Рисователь этого объекта
        private final Supplier<Printer> printer;
    }
    ///Основной набор статических переменных, для работы после обновления экрана
    private static class Static {
        ///Вероятность появления на столе того или иного инструмента
        private static class Probability{
            int count = 0;
            double prob = 0;

            private void reset() {
                count = 0;
                prob = 1;
            }
        }
        ///Размеры игрового поля, в штуках
        private final java.awt.Point size = new java.awt.Point();
        ///Игровое поле, это прямоугольник вокруг эллипса игрового поля
        private java.awt.geom.Rectangle2D gameField;
        ///Эксцентриситет игрового поля.
        private double aspectRatio = 1;
        ///Инструменты, которые лежат на столе
        private final List<Instrument> set = new ArrayList();
        ///Сколько уже инструментов добавлено каждого типа. Нужно чтобы количество разных инструментов было примерно одинаковым
        private final EnumMap<Instrument.Type,Probability> instruments = new EnumMap<>(Instrument.Type.class){{for(var t : Instrument.Type.values()) put(t,new Probability());}}; 
        ///Инициализция поля
        public void init(double cx, double cy, double A, double B){
            if(size.x != Configurations.getWidth() || size.y != Configurations.getHeight()){
                size.x = Configurations.getWidth();
                size.y = Configurations.getHeight();
            }
            set.clear();
            for(var t : instruments.entrySet())
                t.getValue().reset();
            gameField = new java.awt.Rectangle.Double(cx-A,cy-B,A*2,B*2);
            aspectRatio = A / B;
        }
        
        ///Добавить инструмент на стол. Если, конечно, можно
        public void add(Instrument next){
            if(next.bounds.get().intersects(gameField)){
                //Нужна точная проверка на перекрытие с игровым полем
                //Так как все эллипсы лежат за пределами эллипса, то просто проверим, что ни одна из сторон прямоугольника
                //Не пересекает эллипс.
                //А сделать это не сложно - решить систему  из уравнения прямой и уравнения эллипса. Если есть решение - есть пересечение.
                var points = next.corners;
                for (int i = 0; i < points.size(); i++) {
                    var p1 = points.get(i);
                    var p2 = points.get(i == 0 ? (points.size()-1) : i - 1);
                    double x1 = p1.getX(), y1 = p1.getY();
                    double x2 = p2.getX(), y2 = p2.getY();
                    double cx = gameField.getCenterX(), cy = gameField.getCenterY();
                    double a = gameField.getWidth()/2, b = gameField.getHeight()/2;
                    // Сдвигаем начало координат в центр эллипса
                    x1 -= cx; y1 -= cy;
                    x2 -= cx; y2 -= cy;
                    
                    // Коэффициенты для квадратного уравнения At^2 + Bt + C = 0
                    // Из уравнения эллипса: ( (x1 + t*(x2-x1)) / a )^2 + ( (y1 + t*(y2-y1)) / b )^2 = 1
                    // Раскрываем скобки и собираем коэффициенты для t^2, t, и константы.
                    var dx = x2 - x1;
                    var dy = y2 - y1;
                    // Коэффициенты A, B, C
                    // A = (dx/a)^2 + (dy/b)^2
                    // B = 2 * ( (x1*dx)/a^2 + (y1*dy)/b^2 )
                    // C = (x1/a)^2 + (y1/b)^2 - 1
                    var A = (dx * dx) / (a * a) + (dy * dy) / (b * b);
                    var B = 2 * ((x1 * dx) / (a * a) + (y1 * dy) / (b * b));
                    var C = (x1 * x1) / (a * a) + (y1 * y1) / (b * b) - 1;
                    // Решаем квадратное уравнение At^2 + Bt + C = 0
                    var discriminant = B * B - 4 * A * C;
                    if (discriminant < 0) 
                        return; //Нет корней - нет решения. А мнимые числа нас не интересуют
                    var t1 = (-B + Math.sqrt(discriminant)) / (2 * A);
                    var t2 = (-B - Math.sqrt(discriminant)) / (2 * A);
                    // Проверяем, попадает ли хотя бы один корень в диапазон [0, 1].
                    // Это означает, что точка пересечения лежит на отрезке.
                    if (0 <= t1 && t1 <= 1 || 0 <= t2 && t2 <= 1)
                       return;
                    // Важно: если A очень близко к нулю (отрезок почти параллелен оси эллипса),
                    // это может вызвать проблемы с делением на ноль.
                    // Если A ~ 0, это линейное уравнение Bt + C = 0.
                    if (Math.abs(A) < 1e-9) { // Используем небольшую погрешность
                        if (Math.abs(B) > 1e-9) { // Линейное уравнение
                            var t = -C / B;
                            if (t >= 0 && t <= 1)
                                return;
                        } else { // Уравнение C = 0 (случай, когда отрезок лежит на касательной или совпадает с частью эллипса)
                            if (Math.abs(C) < 1e-9) { // Если C = 0, значит, точки отрезка лежат на эллипсе
                                //Может тут всё прохоит по касательной. Но даже так - одна точка больше 0!
                                return;
                            }
                        }
                    }
                }
            }
            //С полем не пересеклись. А с каким либо из размещённых объектов?
            if(set.stream().allMatch(p -> !p.intersects(next))){                    
                set.add(next);
                var avr = ((double)set.size()) / instruments.size();
                var max = avr * 1.1;
                var width = avr * 0.1;
                instruments.get(next.type).count++;
            for(var t : instruments.entrySet())
                t.getValue().prob = (max - t.getValue().count) / width;
            }
        }
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
        state.init(cx,cy,A,B);
        var startHash = a2*b2;
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
                var type = gen.nextDouble();
                for(var t : state.instruments.entrySet()){
                    if((type -= t.getValue().prob) <= 0){
                        var next = new Instrument(x, y, t.getKey(),cellSize, rot);
                        state.add(next);
                        break;
                    }
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
        state.set.forEach(t -> t.printer.get().paint(g,visible,field));
	}
	
	///Текущее состояние стола
	private static Static state;
}
