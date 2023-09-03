/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package Calculations;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.ComparisonFailure;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Kerravitarr
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PointTest {
	
	public PointTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	/**Создаёт квадртный мир
	 ____________
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 ‾‾‾‾‾‾‾‾‾‾‾‾
	 */
	public void makeRECTANGLEWorld(){
		Configurations.makeWorld(Configurations.WORLD_TYPE.RECTANGLE, 10, 10, null);
	}
	/**Создаёт Пруд мир
	 ____________
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 ‾‾‾‾‾‾‾‾‾‾‾‾
	 */
	public void makeLINE_HWorld(){
		Configurations.makeWorld(Configurations.WORLD_TYPE.LINE_H, 10, 10, null);
	}
	/**Создаёт реку мир
	 
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	
	 */
	public void makeLINE_VWorld(){
		Configurations.makeWorld(Configurations.WORLD_TYPE.LINE_V, 10, 10, null);
	}
	/**Создаёт круглый мир
	     _____
	    |****|
	  |********|
	  |********|
	 |**********|
	 |**********|
	 |**********|
	 |**********|
	  |********|
	  |********|
	    |****|
	    ‾‾‾‾‾‾‾‾‾‾
	 */
	public void makeCIRCLEWorld(){
		Configurations.makeWorld(Configurations.WORLD_TYPE.CIRCLE, 10, 10, null);
	}
	/**Создаёт квадртаный кусок океана
	
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 **********
	 
	 */
	public void makeFIELD_RWorld(){
		Configurations.makeWorld(Configurations.WORLD_TYPE.FIELD_R, 10, 10, null);
	}
	@Test
	public void testConstructor() {
		makeRECTANGLEWorld();
		//В квадратном мире координаты всегда соответствуют себе
		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				assertEquals("{\"x\":" + x + ",\"y\":" + y + "}", p.toJSON().toString());
			}
		}
		makeCIRCLEWorld();
		//В круглом мире координаты всегда свои
		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				assertEquals("{\"x\":" + x + ",\"y\":" + y + "}", p.toJSON().toString());
			}
		}
		makeLINE_HWorld();
		//В линейном мире y1 всегда y1
		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				int eX;
				if(-20 <= x && x < -10)
					eX = 10 * 2 + x;
				else if(-10 <= x && x < 0)
					eX = 10 + x;
				else if(0 <= x && x < 10)
					eX = x;
				else if(10 <= x && x < 20)
					eX = x - 10 * 1;
				else
					eX = x - 10 * 2;
				assertEquals("["+ x + ":" + y + "]","{\"x\":" + eX + ",\"y\":" + y + "}", p.toJSON().toString());
			}
		}
		
		makeLINE_VWorld();
		//В вертекальном мире мире x1 всегда x1
		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				int eY;
				if(-20 <= y && y < -10)
					eY = 10 * 2 + y;
				else if(-10 <= y && y < 0)
					eY = 10 + y;
				else if(0 <= y && y < 10)
					eY = y;
				else if(10 <= y && y < 20)
					eY = y - 10 * 1;
				else
					eY = y - 10 * 2;
				assertEquals("["+ x + ":" + y + "]","{\"x\":" + x + ",\"y\":" + eY + "}", p.toJSON().toString());
			}
		}
		
		makeFIELD_RWorld();
		//На поле все координаты меняются
		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				int eY;
				if(-20 <= y && y < -10)
					eY = 10 * 2 + y;
				else if(-10 <= y && y < 0)
					eY = 10 + y;
				else if(0 <= y && y < 10)
					eY = y;
				else if(10 <= y && y < 20)
					eY = y - 10 * 1;
				else
					eY = y - 10 * 2;
				int eX;
				if(-20 <= x && x < -10)
					eX = 10 * 2 + x;
				else if(-10 <= x && x < 0)
					eX = 10 + x;
				else if(0 <= x && x < 10)
					eX = x;
				else if(10 <= x && x < 20)
					eX = x - 10 * 1;
				else
					eX = x - 10 * 2;
				assertEquals("["+ x + ":" + y + "]","{\"x\":" + eX + ",\"y\":" + eY + "}", p.toJSON().toString());
			}
		}
	}
	
	/**Тестирует, что мир непрерывен. Что каждая клетка связана с любой другой на протяжении всего действительного мира*/
	@Test
	public void testContinuty() {
		for (int i = 0; ; i++) {
			switch (i) {
				case 0 -> makeRECTANGLEWorld();
				case 1 -> makeLINE_HWorld();
				case 2 -> makeLINE_VWorld();
				case 3 -> makeCIRCLEWorld();
				case 4 -> makeFIELD_RWorld();
				default -> {return;}
			}
			
			for (int x = 0; x < 10; x++) {
				for (int y = 0; y < 10; y++) {
					var p = new Point(x, y);
					if(!p.valid()) continue;
					for (final var dir : Point.DIRECTION.values) {
						var p1 = p.next(dir);
						var p2 = p1.next(dir.inversion());
						assertEquals(String.format("Неудачная попытка совершить шаг из [%d,%d] в сторону "
								+ "%s и вернуться обратно. После первого шага мы стали %s после второго %s",
								x, y, dir, p1, p2), "(P (" + x + "; " + y + "))", p2.toString());
					}
				}
			}
		}
		
	}
	@Test
	public void testNext() {		
		//Тут нечего тестировать. Просто получается новая точка
		//смещённая не на 1/1, а на число из направления
	}
	@Test
	public void testAdd() {
		//Тут нечего тестировать. Так как add - просто получить новую точку и её нормализовать
	}
	@Test
	public void testSub() {
		//Тут нечего тестировать. Так как sub - просто получить новую точку и её нормализовать
	}

	@Test
	public void testUpdate() {
		makeRECTANGLEWorld();
		var f = new Point(5,5);
		var s = new Point(10, 10);
		assertNotEquals(s, f);
		f.update(s);
		assertFalse(f == s);
		assertTrue(f.equals(s));
	}
	@Test
	public void testEquals() {
		makeRECTANGLEWorld();
		Point obj = new Point(42, 42);
		Point instance = new Point(42, 42);
		boolean expResult = true;
		
		makeRECTANGLEWorld();
		assertEquals(expResult, instance.equals(obj));
		makeLINE_HWorld();
		assertEquals(expResult, instance.equals(obj));
		makeLINE_VWorld();
		assertEquals(expResult, instance.equals(obj));
		makeCIRCLEWorld();
		assertEquals(expResult, instance.equals(obj));
		makeFIELD_RWorld();
		assertEquals(expResult, instance.equals(obj));
	}

	@Test
	public void testDistance() {
		makeRECTANGLEWorld();
		for (int x1 = 0; x1 < 10; x1++) {
			for (int y1 = 0; y1 < 10; y1++) {
				var p1 = new Point(x1, y1);
				for (int x2 = 0; x2 < 10; x2++) {
					for (int y2 = 0; y2 < 10; y2++) {
						var p2 = new Point(x2, y2);
						var d = p1.distance(p2);
						assertEquals(String.format("От %s к %s", p1,p2),d.x, x2-x1);
						assertEquals(String.format("От %s к %s", p1,p2),d.y, y2-y1);
					}
				}
			}
		}
		makeLINE_HWorld();
		for (int x1 = 0; x1 < 10; x1++) {
			for (int y1 = 0; y1 < 10; y1++) {
				var p1 = new Point(x1, y1);
				for (int x2 = 0; x2 < 10; x2++) {
					for (int y2 = 0; y2 < 10; y2++) {
						var p2 = new Point(x2, y2);
						var d = p1.distance(p2);
						 //Пока расстояние между клетками меньше 5, то это значит, что между ними меньше половины
						 //экрана.
						 //Если расстояние больше 5, то мы можем телепортировать x1 за пределы поля на ширину поля
						 //Если расстояние меньше -5, то мы можем телепортировать x1, но на этот раз перед точкой х2
						if(Math.abs(x2-x1) <= 5)
							assertEquals(String.format("От %s к %s", p1, p2), d.x, x2 - x1);
						else if(x2-x1 > 5) 
							assertEquals(String.format("От %s к %s", p1, p2), d.x, x2 - (x1 + 10));
						else if(x2-x1 < -5) 
							assertEquals(String.format("От %s к %s", p1, p2), d.x, x2 - (x1 - 10));
						assertEquals(String.format("От %s к %s", p1,p2),d.y, y2-y1);
					}
				}
			}
		}
		makeLINE_VWorld();
		for (int x1 = 0; x1 < 10; x1++) {
			for (int y1 = 0; y1 < 10; y1++) {
				var p1 = new Point(x1, y1);
				for (int x2 = 0; x2 < 10; x2++) {
					for (int y2 = 0; y2 < 10; y2++) {
						var p2 = new Point(x2, y2);
						var d = p1.distance(p2);
						assertEquals(String.format("От %s к %s", p1,p2),d.x, x2-x1);
						if(Math.abs(y2-y1) <= 5)
							assertEquals(String.format("От %s к %s", p1, p2), d.y, y2 - y1);
						else if(y2-y1 > 5) 
							assertEquals(String.format("От %s к %s", p1, p2), d.y, y2 - (y1 + 10));
						else if(y2-y1 < -5) 
							assertEquals(String.format("От %s к %s", p1, p2), d.y, y2 - (y1 - 10));
					}
				}
			}
		}
		
		makeCIRCLEWorld();
		for (int x1 = 0; x1 < 10; x1++) {
			for (int y1 = 0; y1 < 10; y1++) {
				var p1 = new Point(x1, y1);
				if(!p1.valid()) continue;
				for (int x2 = 0; x2 < 10; x2++) {
					for (int y2 = 0; y2 < 10; y2++) {
						var p2 = new Point(x2, y2);
						if(!p2.valid()) continue;
						var d = p1.distance(p2);
						assertEquals(String.format("От %s к %s", p1,p2),d.x, x2-x1);
						assertEquals(String.format("От %s к %s", p1,p2),d.y, y2-y1);
					}
				}
			}
		}
		makeFIELD_RWorld();
		for (int x1 = 0; x1 < 10; x1++) {
			for (int y1 = 0; y1 < 10; y1++) {
				var p1 = new Point(x1, y1);
				for (int x2 = 0; x2 < 10; x2++) {
					for (int y2 = 0; y2 < 10; y2++) {
						var p2 = new Point(x2, y2);
						var d = p1.distance(p2);
						if(Math.abs(x2-x1) <= 5)
							assertEquals(String.format("От %s к %s", p1, p2), d.x, x2 - x1);
						else if(x2-x1 > 5) 
							assertEquals(String.format("От %s к %s", p1, p2), d.x, x2 - (x1 + 10));
						else if(x2-x1 < -5) 
							assertEquals(String.format("От %s к %s", p1, p2), d.x, x2 - (x1 - 10));
						if(Math.abs(y2-y1) <= 5)
							assertEquals(String.format("От %s к %s", p1, p2), d.y, y2 - y1);
						else if(y2-y1 > 5) 
							assertEquals(String.format("От %s к %s", p1, p2), d.y, y2 - (y1 + 10));
						else if(y2-y1 < -5) 
							assertEquals(String.format("От %s к %s", p1, p2), d.y, y2 - (y1 - 10));
					}
				}
			}
		}
	}

	@Test
	public void testDirection() {
		for (int i = 0; ; i++) {
			switch (i) {
				case 0 -> makeRECTANGLEWorld();
				case 1 -> makeLINE_HWorld();
				case 2 -> makeLINE_VWorld();
				case 3 -> makeCIRCLEWorld();
				case 4 -> makeFIELD_RWorld();
				default -> {return;}
			}
			
			for (int x1 = 0; x1 < 10; x1++) {
				for (int y1 = 0; y1 < 10; y1++) {
					var p1 = new Point(x1, y1);
					if(!p1.valid()) continue;
					for (int x2 = 0; x2 < 10; x2++) {
						for (int y2 = 0; y2 < 10; y2++) {
							var p2 = new Point(x2, y2);
							if(!p2.valid()) continue;
							var d = p1.distance(p2);
							Throwable thr = null;
							switch (d.x) {
								case -1 -> {
									switch (d.y) {
										case -1 -> assertEquals(Point.DIRECTION.UP_L, p1.direction(p2));
										case 0 -> assertEquals(Point.DIRECTION.LEFT, p1.direction(p2));
										case 1 -> assertEquals(Point.DIRECTION.DOWN_L, p1.direction(p2));
										default -> thr = assertThrows(IllegalArgumentException.class,() -> p1.direction(p2));
									}
								}
								case 0 -> {
									switch (d.y) {
										case -1 -> assertEquals(Point.DIRECTION.UP, p1.direction(p2));
										case 0 -> thr = assertThrows(IllegalArgumentException.class,() -> p1.direction(p2));
										case 1 -> assertEquals(Point.DIRECTION.DOWN, p1.direction(p2));
										default -> thr = assertThrows(IllegalArgumentException.class,() -> p1.direction(p2));
									}
								}
								case 1 -> {
									switch (d.y) {
										case -1 -> assertEquals(Point.DIRECTION.UP_R, p1.direction(p2));
										case 0 -> assertEquals(Point.DIRECTION.RIGHT, p1.direction(p2));
										case 1 -> assertEquals(Point.DIRECTION.DOWN_R, p1.direction(p2));
										default -> thr = assertThrows(IllegalArgumentException.class,() -> p1.direction(p2));
									}
								}
								default -> thr = assertThrows(IllegalArgumentException.class,() -> p1.direction(p2));
							}
							if(thr != null)
								assertEquals("Расстояние между точками должно быть ровно 1 клетка!", thr.getMessage());
						}
					}
				}
			}
		}
	}
	@Test
	public void testValid() {
		makeRECTANGLEWorld();

		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				if(x >= 0 && x < 10 && y >= 0 && y < 10)
					assertTrue(p.toString(),p.valid());
				else
					assertFalse(p.toString(),p.valid());
			}
		}
		
		makeCIRCLEWorld();
		var valid = new boolean[][]{
			new boolean[]{false,false,false,true ,true ,true ,true ,false,false,false},
			new boolean[]{false,true ,true ,true ,true ,true ,true ,true ,true ,false},
			new boolean[]{false,true ,true ,true ,true ,true ,true ,true ,true ,false},
			new boolean[]{true ,true ,true ,true ,true ,true ,true ,true ,true ,true },
			new boolean[]{true ,true ,true ,true ,true ,true ,true ,true ,true ,true },
			new boolean[]{true ,true ,true ,true ,true ,true ,true ,true ,true ,true },
			new boolean[]{true ,true ,true ,true ,true ,true ,true ,true ,true ,true },
			new boolean[]{false,true ,true ,true ,true ,true ,true ,true ,true ,false},
			new boolean[]{false,true ,true ,true ,true ,true ,true ,true ,true ,false},
			new boolean[]{false,false,false,true ,true ,true ,true ,false,false,false}
		};
		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				if(x >= 0 && x < 10 && y >= 0 && y < 10){
					if(valid[x][y])
						assertTrue(p.toString(),p.valid());
					else
						assertFalse(p.toString(),p.valid());
				}else{
					assertFalse(p.toString(),p.valid());
				}
			}
		}
		
		makeLINE_HWorld();
		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				if(y >= 0 && y < 10)
					assertTrue(p.toString(),p.valid());
				else
					assertFalse(p.toString(),p.valid());
			}
		}
		makeLINE_VWorld();
		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				if(x >= 0 && x < 10)
					assertTrue(p.toString(),p.valid());
				else
					assertFalse(p.toString(),p.valid());
			}
		}
		makeFIELD_RWorld();
		for (int x = -20; x < 20; x++) {
			for (int y = -20; y < 20; y++) {
				var p = new Point(x,y);
				assertTrue(p.toString(),p.valid());
			}
		}
	}
	@Test
	public void testToJSON() {
		makeRECTANGLEWorld();
		final var p = new Point(64,65);
		assertEquals("{\"x\":64,\"y\":65}", p.toJSON().toString());
	}
	@Test
	public void testToString() {
		makeRECTANGLEWorld();
		final var p = new Point(64,65);
		assertEquals("(P (64; 65))", p.toString());
	}
	
}
