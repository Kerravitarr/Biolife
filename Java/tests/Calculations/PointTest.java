/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package Calculations;

import Utils.JSON;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
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
	      __
	      |*|
	   |*******|
	  |*********|
	  |*********|
	  |*********|
	 |**********|
	  |*********|
	  |*********|
	  |*********|
	   |*******|
	   ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
	 */
	public void makeCIRCLEWorld(){
		Configurations.makeWorld(Configurations.WORLD_TYPE.CIRCLE, 10, 10, null);
	}
	/**Создаёт круглый кусок океана
		 
	      *
	   *******
	  *********
	  *********
	  *********
	 **********
	  *********
	  *********
	  *********
	   *******
	   
	 */
	public void makeFIELD_CWorld(){
		Configurations.makeWorld(Configurations.WORLD_TYPE.FIELD_C, 10, 10, null);
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
		//В линейном мире y всегда y
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
		//В вертекальном мире мире x всегда x
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
		makeFIELD_CWorld();
		//На поле круга координаты так лего не выбрать...
		
		//Для начала пройдёмся по внутреннему кругу
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				var dx = 5 - x;
				var dy = 5 - y;
				//Все точки, находящиеся в круге. То есть от которых до центра, не больше радиуса
				if(dx*dx + dy*dy <= 5){
					assertEquals("(P (" + x + "; " + y + "))", new Point(x,y).toString());
				}
			}
		}
		//Теперь по оставшемуся квадрату
		//assertEquals("(P (8; 8))", new Point(1,1).toString());
		assertEquals("(P (7; 7))", new Point(0,0).toString());
	assertEquals("(P (7; 7))", new Point(1,0).toString());
	assertEquals("(P (7; 8))", new Point(2,0).toString());
		assertEquals("(P (6; 9))", new Point(3,0).toString());
		assertEquals("(P (5; 9))", new Point(4,0).toString());
		assertEquals("(P (4; 9))", new Point(6,0).toString());
		assertEquals("(P (3; 9))", new Point(7,0).toString());
	assertEquals("(P (2; 8))", new Point(8,0).toString());
	assertEquals("(P (2; 7))", new Point(9,0).toString());
		
	}
	
	@Test
	public void testNext() {
		System.out.println("next");
		Point.DIRECTION dir = null;
		Point instance = null;
		Point expResult = null;
		Point result = instance.next(dir);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	@Test
	public void testAdd() {
		System.out.println("add");
		Point point = null;
		Point instance = null;
		Point expResult = null;
		Point result = instance.add(point);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	@Test
	public void testSub() {
		System.out.println("sub");
		Point point = null;
		Point instance = null;
		Point expResult = null;
		Point result = instance.sub(point);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	@Test
	public void testUpdate() {
		System.out.println("update");
		Point point = null;
		Point instance = null;
		instance.update(point);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	@Test
	public void testEquals() {
		System.out.println("equals");
		Point obj = null;
		Point instance = null;
		boolean expResult = false;
		boolean result = instance.equals(obj);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	@Test
	public void testGetX() {
		System.out.println("getX");
		Point instance = null;
		int expResult = 0;
		int result = instance.getX();
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	@Test
	public void testGetY() {
		System.out.println("getY");
		Point instance = null;
		int expResult = 0;
		int result = instance.getY();
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	@Test
	public void testDistance() {
		System.out.println("distance");
		Point first = null;
		Point second = null;
		Point.Vector expResult = null;
		Point.Vector result = Point.distance(first, second);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	@Test
	public void testDirection() {
		System.out.println("direction");
		Point f = null;
		Point s = null;
		Point.DIRECTION expResult = null;
		Point.DIRECTION result = Point.direction(f, s);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

	@Test
	public void testValid() {
		System.out.println("valid");
		Point instance = null;
		boolean expResult = false;
		boolean result = instance.valid();
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
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