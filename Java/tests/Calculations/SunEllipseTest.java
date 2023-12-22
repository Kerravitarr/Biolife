/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package Calculations;

import Calculations.Emitters.SunEllipse;
import Calculations.Trajectories.Trajectory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Kerravitarr
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SunEllipseTest {
	
	public SunEllipseTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
		Configurations.buildMap(new Configurations(Configurations.WORLD_TYPE.RECTANGLE, 100, 100), null);
		Configurations.confoguration.DIRTY_WATER = 2;
	}
	
	@After
	public void tearDown() {
	}
	private interface EvryPoint{ public void test(Point p);}
	private void forEvwyPoint(EvryPoint p){
		for (int x = 0; x < 100; x++) {
			for (int y = 0; y < 100; y++) {
				var point = Point.create(x, y);
				p.test(point);
			}
		}
	}
	

	@Test
	public void testCyrcle() {
		final var point = Point.create(50, 50);
		final var sun = new SunEllipse(10, new Trajectory(point), 20, false,"");
		forEvwyPoint(p -> {
			final var d = p.distance(point);
			final var msg = String.format("В точке %s удалённой от центра солнца на %s", p,d);
			if(d.getHypotenuse() < 10)
				assertEquals(msg,10,sun.getE(p),0d);
			else if(d.getHypotenuse() < 10 + 5)
				assertEquals(msg,10 - (d.getHypotenuse() - 10) * 2,sun.getE(p),0d);
			else
				assertEquals(msg,0,sun.getE(p),0d);			
		});
	}
	@Test
	public void testCyrcleLine() {
		final var point = Point.create(50, 50);
		final var sunL = new SunEllipse(10, new Trajectory(point), 20, true, "");
		forEvwyPoint(p -> {
			final var d = p.distance(point);
			final var msg = String.format("В точке %s удалённой от центра солнца на %s", p,d);
			if(d.getHypotenuse() == 10)
				assertEquals(msg,10,sunL.getE(p),0d);
			else if(d.getHypotenuse() < 10 && d.getHypotenuse() > 5)
				assertEquals(msg,10 - (10 - d.getHypotenuse()) * 2,sunL.getE(p),0d);
			else if(d.getHypotenuse() <= 5)
				assertEquals(msg,0,sunL.getE(p),0d);
			else if(d.getHypotenuse() < 10 + 5)
				assertEquals(msg,10 - (d.getHypotenuse() - 10) * 2,sunL.getE(p),0d);
			else
				assertEquals(msg,0,sunL.getE(p),0d);			
		});
	}
	
	@Test
	public void testEcllipseSun20x40() {
		final var sun = new SunEllipse(10, new Trajectory(Point.create(50, 50)), 20,40, false,"");
		var valid = new double[][]{
					//X: 35,36....
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0.3,1.0,1.6,1.9,2.0,1.9,1.6,1.0,0.3,0,0,0,0,0,0,0,0,0,0}, // y = 26
			new double[]{0,0,0,0,0,0,0,0,0,0.2,1.2,2.2,2.9,3.5,3.9,4.0,3.9,3.5,2.9,2.2,1.2,0.2,0,0,0,0,0,0,0,0}, // y = 27
			new double[]{0,0,0,0,0,0,0,0,0.5,1.8,2.9,3.9,4.8,5.4,5.9,6.0,5.9,5.4,4.8,3.9,2.9,1.8,0.5,0,0,0,0,0,0,0}, // y = 28
			new double[]{0,0,0,0,0,0,0,0.6,2.0,3.3,4.6,5.7,6.6,7.4,7.8,8.0,7.8,7.4,6.6,5.7,4.6,3.3,2.0,0.6,0,0,0,0,0,0}, // y = 29
			new double[]{0,0,0,0,0,0,0.4,2.0,3.4,4.8,6.2,7.4,8.4,9.2,9.8,10,9.8,9.2,8.4,7.4,6.2,4.8,3.4,2.0,0.4,0,0,0,0,0}, // y = 30
			new double[]{0,0,0,0,0,0.0,1.6,3.2,4.8,6.3,7.7,9.0,10,10,10,10,10,10,10,9.0,7.7,6.3,4.8,3.2,1.6,0.0,0,0,0,0}, // y = 31
			new double[]{0,0,0,0,0,1.1,2.8,4.4,6.0,7.6,9.1,10,10,10,10,10,10,10,10,10,9.1,7.6,6.0,4.4,2.8,1.1,0,0,0,0}, // y = 32
			new double[]{0,0,0,0,0.4,2.1,3.9,5.6,7.2,8.8,10,10,10,10,10,10,10,10,10,10,10,8.8,7.2,5.6,3.9,2.1,0.4,0,0,0}, // y = 33
			new double[]{0,0,0,0,1.3,3.1,4.9,6.6,8.3,10,10,10,10,10,10,10,10,10,10,10,10,10,8.3,6.6,4.9,3.1,1.3,0,0,0}, // y = 34
			new double[]{0,0,0,0.3,2.2,4.0,5.8,7.6,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,7.6,5.8,4.0,2.2,0.3,0,0}, // y = 35
			new double[]{0,0,0,1.1,3.0,4.8,6.6,8.5,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.5,6.6,4.8,3.0,1.1,0,0}, // y = 36
			new double[]{0,0,0,1.8,3.7,5.6,7.4,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,7.4,5.6,3.7,1.8,0,0}, // y = 37
			new double[]{0,0,0.5,2.4,4.3,6.2,8.1,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.1,6.2,4.3,2.4,0.5,0}, // y = 38
			new double[]{0,0,1.1,3.0,4.9,6.9,8.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.8,6.9,4.9,3.0,1.1,0}, // y = 39
			new double[]{0,0,1.6,3.6,5.5,7.4,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,7.4,5.5,3.6,1.6,0}, // y = 40
			new double[]{0,0.1,2.1,4.0,6.0,7.9,9.9,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.9,7.9,6.0,4.0,2.1,0.1}, // y = 41
			new double[]{0,0.5,2.5,4.5,6.4,8.4,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.4,6.4,4.5,2.5,0.5}, // y = 42
			new double[]{0,0.9,2.8,4.8,6.8,8.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.8,6.8,4.8,2.8,0.9}, // y = 43
			new double[]{0,1.2,3.2,5.1,7.1,9.1,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.1,7.1,5.1,3.2,1.2}, // y = 44
			new double[]{0,1.4,3.4,5.4,7.4,9.4,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.4,7.4,5.4,3.4,1.4}, // y = 45
			new double[]{0,1.6,3.6,5.6,7.6,9.6,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.6,7.6,5.6,3.6,1.6}, // y = 46
			new double[]{0,1.8,3.8,5.8,7.8,9.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.8,7.8,5.8,3.8,1.8}, // y = 47
			new double[]{0,1.9,3.9,5.9,7.9,9.9,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.9,7.9,5.9,3.9,1.9}, // y = 48
			new double[]{0,2.0,4.0,6.0,8.0,10.0,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10.0,8.0,6.0,4.0,2.0}, // y = 49
			new double[]{0,2.0,4.0,6.0,8.0,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.0,6.0,4.0,2.0}, // y = 50
			new double[]{0,2.0,4.0,6.0,8.0,10.0,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10.0,8.0,6.0,4.0,2.0}, // y = 51
			new double[]{0,1.9,3.9,5.9,7.9,9.9,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.9,7.9,5.9,3.9,1.9}, // y = 52
			new double[]{0,1.8,3.8,5.8,7.8,9.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.8,7.8,5.8,3.8,1.8}, // y = 53
			new double[]{0,1.6,3.6,5.6,7.6,9.6,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.6,7.6,5.6,3.6,1.6}, // y = 54
			new double[]{0,1.4,3.4,5.4,7.4,9.4,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.4,7.4,5.4,3.4,1.4}, // y = 55
			new double[]{0,1.2,3.2,5.1,7.1,9.1,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.1,7.1,5.1,3.2,1.2}, // y = 56
			new double[]{0,0.9,2.8,4.8,6.8,8.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.8,6.8,4.8,2.8,0.9}, // y = 57
			new double[]{0,0.5,2.5,4.5,6.4,8.4,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.4,6.4,4.5,2.5,0.5}, // y = 58
			new double[]{0,0.1,2.1,4.0,6.0,7.9,9.9,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.9,7.9,6.0,4.0,2.1,0.1}, // y = 59
			new double[]{0,0,1.6,3.6,5.5,7.4,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,7.4,5.5,3.6,1.6,0}, // y = 60
			new double[]{0,0,1.1,3.0,4.9,6.9,8.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.8,6.9,4.9,3.0,1.1,0}, // y = 61
			new double[]{0,0,0.5,2.4,4.3,6.2,8.1,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.1,6.2,4.3,2.4,0.5,0}, // y = 62
			new double[]{0,0,0,1.8,3.7,5.6,7.4,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,7.4,5.6,3.7,1.8,0,0}, // y = 63
			new double[]{0,0,0,1.1,3.0,4.8,6.6,8.5,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.5,6.6,4.8,3.0,1.1,0,0}, // y = 64
			new double[]{0,0,0,0.3,2.2,4.0,5.8,7.6,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,7.6,5.8,4.0,2.2,0.3,0,0}, // y = 65
			new double[]{0,0,0,0,1.3,3.1,4.9,6.6,8.3,10,10,10,10,10,10,10,10,10,10,10,10,10,8.3,6.6,4.9,3.1,1.3,0,0,0}, // y = 66
			new double[]{0,0,0,0,0.4,2.1,3.9,5.6,7.2,8.8,10,10,10,10,10,10,10,10,10,10,10,8.8,7.2,5.6,3.9,2.1,0.4,0,0,0}, // y = 67
			new double[]{0,0,0,0,0,1.1,2.8,4.4,6.0,7.6,9.1,10,10,10,10,10,10,10,10,10,9.1,7.6,6.0,4.4,2.8,1.1,0,0,0,0}, // y = 68
			new double[]{0,0,0,0,0,0.0,1.6,3.2,4.8,6.3,7.7,9.0,10,10,10,10,10,10,10,9.0,7.7,6.3,4.8,3.2,1.6,0.0,0,0,0,0}, // y = 69
			new double[]{0,0,0,0,0,0,0.4,2.0,3.4,4.8,6.2,7.4,8.4,9.2,9.8,10,9.8,9.2,8.4,7.4,6.2,4.8,3.4,2.0,0.4,0,0,0,0,0}, // y = 70
			new double[]{0,0,0,0,0,0,0,0.6,2.0,3.3,4.6,5.7,6.6,7.4,7.8,8.0,7.8,7.4,6.6,5.7,4.6,3.3,2.0,0.6,0,0,0,0,0,0}, // y = 71
			new double[]{0,0,0,0,0,0,0,0,0.5,1.8,2.9,3.9,4.8,5.4,5.9,6.0,5.9,5.4,4.8,3.9,2.9,1.8,0.5,0,0,0,0,0,0,0}, // y = 72
			new double[]{0,0,0,0,0,0,0,0,0,0.2,1.2,2.2,2.9,3.5,3.9,4.0,3.9,3.5,2.9,2.2,1.2,0.2,0,0,0,0,0,0,0,0}, // y = 73
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0.3,1.0,1.6,1.9,2.0,1.9,1.6,1.0,0.3,0,0,0,0,0,0,0,0,0,0}, // y = 74
		};
		forEvwyPoint(p -> {
			final var value = (35 <= p.getX() && p.getX() <= 64) && (26 <= p.getY() && p.getY() <= 74) ? valid[p.getY() - 26][p.getX() - 35] : 0;
			final var msg = String.format("В точке %s ожидалась освещённость %f", p,value);
			assertEquals(msg,value,sun.getE(p),0.2d);		
		});
	}
	@Test
	public void testEcllipseSun40x20() {
		final var sun = new SunEllipse(10, new Trajectory(Point.create(50, 50)), 40,20, false,"");
		var valid = new double[][]{
					//X: 26,...
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 35
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.1,0.5,0.9,1.2,1.4,1.6,1.8,1.9,2.0,2.0,2.0,1.9,1.8,1.6,1.4,1.2,0.9,0.5,0.1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 36
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0.5,1.1,1.6,2.1,2.5,2.8,3.2,3.4,3.6,3.8,3.9,4.0,4.0,4.0,3.9,3.8,3.6,3.4,3.2,2.8,2.5,2.1,1.6,1.1,0.5,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 37
			new double[]{0,0,0,0,0,0,0,0,0,0.3,1.1,1.8,2.4,3.0,3.6,4.0,4.5,4.8,5.1,5.4,5.6,5.8,5.9,6.0,6.0,6.0,5.9,5.8,5.6,5.4,5.1,4.8,4.5,4.0,3.6,3.0,2.4,1.8,1.1,0.3,0,0,0,0,0,0,0,0,0}, // y = 38
			new double[]{0,0,0,0,0,0,0,0.4,1.3,2.2,3.0,3.7,4.3,4.9,5.5,6.0,6.4,6.8,7.1,7.4,7.6,7.8,7.9,8.0,8.0,8.0,7.9,7.8,7.6,7.4,7.1,6.8,6.4,6.0,5.5,4.9,4.3,3.7,3.0,2.2,1.3,0.4,0,0,0,0,0,0,0}, // y = 39
			new double[]{0,0,0,0,0,0.0,1.1,2.1,3.1,4.0,4.8,5.6,6.2,6.9,7.4,7.9,8.4,8.8,9.1,9.4,9.6,9.8,9.9,10.0,10,10.0,9.9,9.8,9.6,9.4,9.1,8.8,8.4,7.9,7.4,6.9,6.2,5.6,4.8,4.0,3.1,2.1,1.1,0.0,0,0,0,0,0}, // y = 40
			new double[]{0,0,0,0,0.4,1.6,2.8,3.9,4.9,5.8,6.6,7.4,8.1,8.8,9.3,9.9,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.9,9.3,8.8,8.1,7.4,6.6,5.8,4.9,3.9,2.8,1.6,0.4,0,0,0,0}, // y = 41
			new double[]{0,0,0,0.6,2.0,3.2,4.4,5.6,6.6,7.6,8.5,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,8.5,7.6,6.6,5.6,4.4,3.2,2.0,0.6,0,0,0}, // y = 42
			new double[]{0,0,0.5,2.0,3.4,4.8,6.0,7.2,8.3,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,8.3,7.2,6.0,4.8,3.4,2.0,0.5,0,0}, // y = 43
			new double[]{0,0.2,1.8,3.3,4.8,6.3,7.6,8.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.8,7.6,6.3,4.8,3.3,1.8,0.2,0}, // y = 44
			new double[]{0,1.2,2.9,4.6,6.2,7.7,9.1,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.1,7.7,6.2,4.6,2.9,1.2,0}, // y = 45
			new double[]{0.3,2.2,3.9,5.7,7.4,9.0,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.0,7.4,5.7,3.9,2.2,0.3}, // y = 46
			new double[]{1.0,2.9,4.8,6.6,8.4,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.4,6.6,4.8,2.9,1.0}, // y = 47
			new double[]{1.6,3.5,5.4,7.4,9.2,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.2,7.4,5.4,3.5,1.6}, // y = 48
			new double[]{1.9,3.9,5.9,7.8,9.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.8,7.8,5.9,3.9,1.9}, // y = 49
			new double[]{2.0,4.0,6.0,8.0,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.0,6.0,4.0,2.0}, // y = 50
			new double[]{1.9,3.9,5.9,7.8,9.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.8,7.8,5.9,3.9,1.9}, // y = 51
			new double[]{1.6,3.5,5.4,7.4,9.2,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.2,7.4,5.4,3.5,1.6}, // y = 52
			new double[]{1.0,2.9,4.8,6.6,8.4,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.4,6.6,4.8,2.9,1.0}, // y = 53
			new double[]{0.3,2.2,3.9,5.7,7.4,9.0,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.0,7.4,5.7,3.9,2.2,0.3}, // y = 54
			new double[]{0,1.2,2.9,4.6,6.2,7.7,9.1,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.1,7.7,6.2,4.6,2.9,1.2,0}, // y = 55
			new double[]{0,0.2,1.8,3.3,4.8,6.3,7.6,8.8,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,8.8,7.6,6.3,4.8,3.3,1.8,0.2,0}, // y = 56
			new double[]{0,0,0.5,2.0,3.4,4.8,6.0,7.2,8.3,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,8.3,7.2,6.0,4.8,3.4,2.0,0.5,0,0}, // y = 57
			new double[]{0,0,0,0.6,2.0,3.2,4.4,5.6,6.6,7.6,8.5,9.3,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.3,8.5,7.6,6.6,5.6,4.4,3.2,2.0,0.6,0,0,0}, // y = 58
			new double[]{0,0,0,0,0.4,1.6,2.8,3.9,4.9,5.8,6.6,7.4,8.1,8.8,9.3,9.9,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,9.9,9.3,8.8,8.1,7.4,6.6,5.8,4.9,3.9,2.8,1.6,0.4,0,0,0,0}, // y = 59
			new double[]{0,0,0,0,0,0.0,1.1,2.1,3.1,4.0,4.8,5.6,6.2,6.9,7.4,7.9,8.4,8.8,9.1,9.4,9.6,9.8,9.9,10.0,10,10.0,9.9,9.8,9.6,9.4,9.1,8.8,8.4,7.9,7.4,6.9,6.2,5.6,4.8,4.0,3.1,2.1,1.1,0.0,0,0,0,0,0}, // y = 60
			new double[]{0,0,0,0,0,0,0,0.4,1.3,2.2,3.0,3.7,4.3,4.9,5.5,6.0,6.4,6.8,7.1,7.4,7.6,7.8,7.9,8.0,8.0,8.0,7.9,7.8,7.6,7.4,7.1,6.8,6.4,6.0,5.5,4.9,4.3,3.7,3.0,2.2,1.3,0.4,0,0,0,0,0,0,0}, // y = 61
			new double[]{0,0,0,0,0,0,0,0,0,0.3,1.1,1.8,2.4,3.0,3.6,4.0,4.5,4.8,5.1,5.4,5.6,5.8,5.9,6.0,6.0,6.0,5.9,5.8,5.6,5.4,5.1,4.8,4.5,4.0,3.6,3.0,2.4,1.8,1.1,0.3,0,0,0,0,0,0,0,0,0}, // y = 62
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0.5,1.1,1.6,2.1,2.5,2.8,3.2,3.4,3.6,3.8,3.9,4.0,4.0,4.0,3.9,3.8,3.6,3.4,3.2,2.8,2.5,2.1,1.6,1.1,0.5,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 63
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.1,0.5,0.9,1.2,1.4,1.6,1.8,1.9,2.0,2.0,2.0,1.9,1.8,1.6,1.4,1.2,0.9,0.5,0.1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 64
		};
		var pp = sun.getE(Point.create(26, 46));
		pp = 0;
		forEvwyPoint(p -> {
			final var value = (26 <= p.getX() && p.getX() <= 74) && (35 <= p.getY() && p.getY() <= 64) ? valid[p.getY() - 35][p.getX() - 26] : 0;
			final var msg = String.format("В точке %s ожидалась освещённость %f", p,value);
			assertEquals(msg,value,sun.getE(p),0.2d);		
		});
	}
	@Test
	public void testEcllipseSun20x40Line() {
		final var sun = new SunEllipse(10, new Trajectory(Point.create(50, 50)), 20,40, true,"");
		var valid = new double[][]{
					//X: 35,36....
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0.3,1.0,1.6,1.9,2.0,1.9,1.6,1.0,0.3,0,0,0,0,0,0,0,0,0,0}, // y = 26
			new double[]{0,0,0,0,0,0,0,0,0,0.2,1.2,2.2,2.9,3.5,3.9,4.0,3.9,3.5,2.9,2.2,1.2,0.2,0,0,0,0,0,0,0,0}, // y = 27
			new double[]{0,0,0,0,0,0,0,0,0.5,1.8,2.9,3.9,4.8,5.4,5.9,6.0,5.9,5.4,4.8,3.9,2.9,1.8,0.5,0,0,0,0,0,0,0}, // y = 28
			new double[]{0,0,0,0,0,0,0,0.6,2.0,3.3,4.6,5.7,6.6,7.4,7.8,8.0,7.8,7.4,6.6,5.7,4.6,3.3,2.0,0.6,0,0,0,0,0,0}, // y = 29
			new double[]{0,0,0,0,0,0,0.4,2.0,3.4,4.8,6.2,7.4,8.4,9.2,9.8,10.0,9.8,9.2,8.4,7.4,6.2,4.8,3.4,2.0,0.4,0,0,0,0,0}, // y = 30
			new double[]{0,0,0,0,0,0.0,1.6,3.2,4.8,6.3,7.7,9.0,9.9,8.9,8.2,8.0,8.2,8.9,9.9,9.0,7.7,6.3,4.8,3.2,1.6,0.0,0,0,0,0}, // y = 31
			new double[]{0,0,0,0,0,1.1,2.8,4.4,6.0,7.6,9.1,9.5,8.2,7.1,6.3,6.0,6.3,7.1,8.2,9.5,9.1,7.6,6.0,4.4,2.8,1.1,0,0,0,0}, // y = 32
			new double[]{0,0,0,0,0.4,2.1,3.9,5.6,7.2,8.8,9.6,8.1,6.7,5.4,4.4,4.0,4.4,5.4,6.7,8.1,9.6,8.8,7.2,5.6,3.9,2.1,0.4,0,0,0}, // y = 33
			new double[]{0,0,0,0,1.3,3.1,4.9,6.6,8.3,10.0,8.4,6.8,5.3,3.9,2.7,2.0,2.7,3.9,5.3,6.8,8.4,10.0,8.3,6.6,4.9,3.1,1.3,0,0,0}, // y = 34
			new double[]{0,0,0,0.3,2.2,4.0,5.8,7.6,9.3,8.9,7.2,5.6,4.0,2.4,1.0,0,1.0,2.4,4.0,5.6,7.2,8.9,9.3,7.6,5.8,4.0,2.2,0.3,0,0}, // y = 35
			new double[]{0,0,0,1.1,3.0,4.8,6.6,8.5,9.7,8.0,6.2,4.5,2.8,1.2,0,0,0,1.2,2.8,4.5,6.2,8.0,9.7,8.5,6.6,4.8,3.0,1.1,0,0}, // y = 36
			new double[]{0,0,0,1.8,3.7,5.6,7.4,9.3,8.9,7.1,5.3,3.5,1.7,0.0,0,0,0,0.0,1.7,3.5,5.3,7.1,8.9,9.3,7.4,5.6,3.7,1.8,0,0}, // y = 37
			new double[]{0,0,0.5,2.4,4.3,6.2,8.1,10.0,8.1,6.3,4.4,2.6,0.8,0,0,0,0,0,0.8,2.6,4.4,6.3,8.1,10.0,8.1,6.2,4.3,2.4,0.5,0}, // y = 38
			new double[]{0,0,1.1,3.0,4.9,6.9,8.8,9.3,7.4,5.6,3.7,1.8,0,0,0,0,0,0,0,1.8,3.7,5.6,7.4,9.3,8.8,6.9,4.9,3.0,1.1,0}, // y = 39
			new double[]{0,0,1.6,3.6,5.5,7.4,9.3,8.7,6.8,4.9,3.0,1.1,0,0,0,0,0,0,0,1.1,3.0,4.9,6.8,8.7,9.3,7.4,5.5,3.6,1.6,0}, // y = 40
			new double[]{0,0.1,2.1,4.0,6.0,7.9,9.9,8.2,6.3,4.3,2.4,0.5,0,0,0,0,0,0,0,0.5,2.4,4.3,6.3,8.2,9.9,7.9,6.0,4.0,2.1,0.1}, // y = 41
			new double[]{0,0.5,2.5,4.5,6.4,8.4,9.7,7.7,5.8,3.8,1.9,0,0,0,0,0,0,0,0,0,1.9,3.8,5.8,7.7,9.7,8.4,6.4,4.5,2.5,0.5}, // y = 42
			new double[]{0,0.9,2.8,4.8,6.8,8.8,9.3,7.3,5.4,3.4,1.4,0,0,0,0,0,0,0,0,0,1.4,3.4,5.4,7.3,9.3,8.8,6.8,4.8,2.8,0.9}, // y = 43
			new double[]{0,1.2,3.2,5.1,7.1,9.1,8.9,7.0,5.0,3.0,1.0,0,0,0,0,0,0,0,0,0,1.0,3.0,5.0,7.0,8.9,9.1,7.1,5.1,3.2,1.2}, // y = 44
			new double[]{0,1.4,3.4,5.4,7.4,9.4,8.6,6.7,4.7,2.7,0.7,0,0,0,0,0,0,0,0,0,0.7,2.7,4.7,6.7,8.6,9.4,7.4,5.4,3.4,1.4}, // y = 45
			new double[]{0,1.6,3.6,5.6,7.6,9.6,8.4,6.4,4.4,2.4,0.5,0,0,0,0,0,0,0,0,0,0.5,2.4,4.4,6.4,8.4,9.6,7.6,5.6,3.6,1.6}, // y = 46
			new double[]{0,1.8,3.8,5.8,7.8,9.8,8.2,6.2,4.2,2.3,0.3,0,0,0,0,0,0,0,0,0,0.3,2.3,4.2,6.2,8.2,9.8,7.8,5.8,3.8,1.8}, // y = 47
			new double[]{0,1.9,3.9,5.9,7.9,9.9,8.1,6.1,4.1,2.1,0.1,0,0,0,0,0,0,0,0,0,0.1,2.1,4.1,6.1,8.1,9.9,7.9,5.9,3.9,1.9}, // y = 48
			new double[]{0,2.0,4.0,6.0,8.0,10.0,8.0,6.0,4.0,2.0,0.0,0,0,0,0,0,0,0,0,0,0.0,2.0,4.0,6.0,8.0,10.0,8.0,6.0,4.0,2.0}, // y = 49
			new double[]{0,2.0,4.0,6.0,8.0,10.0,8.0,6.0,4.0,2.0,0.0,0,0,0,0,0,0,0,0,0,0.0,2.0,4.0,6.0,8.0,10.0,8.0,6.0,4.0,2.0}, // y = 50
			new double[]{0,2.0,4.0,6.0,8.0,10.0,8.0,6.0,4.0,2.0,0.0,0,0,0,0,0,0,0,0,0,0.0,2.0,4.0,6.0,8.0,10.0,8.0,6.0,4.0,2.0}, // y = 51
			new double[]{0,1.9,3.9,5.9,7.9,9.9,8.1,6.1,4.1,2.1,0.1,0,0,0,0,0,0,0,0,0,0.1,2.1,4.1,6.1,8.1,9.9,7.9,5.9,3.9,1.9}, // y = 52
			new double[]{0,1.8,3.8,5.8,7.8,9.8,8.2,6.2,4.2,2.3,0.3,0,0,0,0,0,0,0,0,0,0.3,2.3,4.2,6.2,8.2,9.8,7.8,5.8,3.8,1.8}, // y = 53
			new double[]{0,1.6,3.6,5.6,7.6,9.6,8.4,6.4,4.4,2.4,0.5,0,0,0,0,0,0,0,0,0,0.5,2.4,4.4,6.4,8.4,9.6,7.6,5.6,3.6,1.6}, // y = 54
			new double[]{0,1.4,3.4,5.4,7.4,9.4,8.6,6.7,4.7,2.7,0.7,0,0,0,0,0,0,0,0,0,0.7,2.7,4.7,6.7,8.6,9.4,7.4,5.4,3.4,1.4}, // y = 55
			new double[]{0,1.2,3.2,5.1,7.1,9.1,8.9,7.0,5.0,3.0,1.0,0,0,0,0,0,0,0,0,0,1.0,3.0,5.0,7.0,8.9,9.1,7.1,5.1,3.2,1.2}, // y = 56
			new double[]{0,0.9,2.8,4.8,6.8,8.8,9.3,7.3,5.4,3.4,1.4,0,0,0,0,0,0,0,0,0,1.4,3.4,5.4,7.3,9.3,8.8,6.8,4.8,2.8,0.9}, // y = 57
			new double[]{0,0.5,2.5,4.5,6.4,8.4,9.7,7.7,5.8,3.8,1.9,0,0,0,0,0,0,0,0,0,1.9,3.8,5.8,7.7,9.7,8.4,6.4,4.5,2.5,0.5}, // y = 58
			new double[]{0,0.1,2.1,4.0,6.0,7.9,9.9,8.2,6.3,4.3,2.4,0.5,0,0,0,0,0,0,0,0.5,2.4,4.3,6.3,8.2,9.9,7.9,6.0,4.0,2.1,0.1}, // y = 59
			new double[]{0,0,1.6,3.6,5.5,7.4,9.3,8.7,6.8,4.9,3.0,1.1,0,0,0,0,0,0,0,1.1,3.0,4.9,6.8,8.7,9.3,7.4,5.5,3.6,1.6,0}, // y = 60
			new double[]{0,0,1.1,3.0,4.9,6.9,8.8,9.3,7.4,5.6,3.7,1.8,0,0,0,0,0,0,0,1.8,3.7,5.6,7.4,9.3,8.8,6.9,4.9,3.0,1.1,0}, // y = 61
			new double[]{0,0,0.5,2.4,4.3,6.2,8.1,10.0,8.1,6.3,4.4,2.6,0.8,0,0,0,0,0,0.8,2.6,4.4,6.3,8.1,10.0,8.1,6.2,4.3,2.4,0.5,0}, // y = 62
			new double[]{0,0,0,1.8,3.7,5.6,7.4,9.3,8.9,7.1,5.3,3.5,1.7,0.0,0,0,0,0.0,1.7,3.5,5.3,7.1,8.9,9.3,7.4,5.6,3.7,1.8,0,0}, // y = 63
			new double[]{0,0,0,1.1,3.0,4.8,6.6,8.5,9.7,8.0,6.2,4.5,2.8,1.2,0,0,0,1.2,2.8,4.5,6.2,8.0,9.7,8.5,6.6,4.8,3.0,1.1,0,0}, // y = 64
			new double[]{0,0,0,0.3,2.2,4.0,5.8,7.6,9.3,8.9,7.2,5.6,4.0,2.4,1.0,0,1.0,2.4,4.0,5.6,7.2,8.9,9.3,7.6,5.8,4.0,2.2,0.3,0,0}, // y = 65
			new double[]{0,0,0,0,1.3,3.1,4.9,6.6,8.3,10.0,8.4,6.8,5.3,3.9,2.7,2.0,2.7,3.9,5.3,6.8,8.4,10.0,8.3,6.6,4.9,3.1,1.3,0,0,0}, // y = 66
			new double[]{0,0,0,0,0.4,2.1,3.9,5.6,7.2,8.8,9.6,8.1,6.7,5.4,4.4,4.0,4.4,5.4,6.7,8.1,9.6,8.8,7.2,5.6,3.9,2.1,0.4,0,0,0}, // y = 67
			new double[]{0,0,0,0,0,1.1,2.8,4.4,6.0,7.6,9.1,9.5,8.2,7.1,6.3,6.0,6.3,7.1,8.2,9.5,9.1,7.6,6.0,4.4,2.8,1.1,0,0,0,0}, // y = 68
			new double[]{0,0,0,0,0,0.0,1.6,3.2,4.8,6.3,7.7,9.0,9.9,8.9,8.2,8.0,8.2,8.9,9.9,9.0,7.7,6.3,4.8,3.2,1.6,0.0,0,0,0,0}, // y = 69
			new double[]{0,0,0,0,0,0,0.4,2.0,3.4,4.8,6.2,7.4,8.4,9.2,9.8,10.0,9.8,9.2,8.4,7.4,6.2,4.8,3.4,2.0,0.4,0,0,0,0,0}, // y = 70
			new double[]{0,0,0,0,0,0,0,0.6,2.0,3.3,4.6,5.7,6.6,7.4,7.8,8.0,7.8,7.4,6.6,5.7,4.6,3.3,2.0,0.6,0,0,0,0,0,0}, // y = 71
			new double[]{0,0,0,0,0,0,0,0,0.5,1.8,2.9,3.9,4.8,5.4,5.9,6.0,5.9,5.4,4.8,3.9,2.9,1.8,0.5,0,0,0,0,0,0,0}, // y = 72
			new double[]{0,0,0,0,0,0,0,0,0,0.2,1.2,2.2,2.9,3.5,3.9,4.0,3.9,3.5,2.9,2.2,1.2,0.2,0,0,0,0,0,0,0,0}, // y = 73
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0.3,1.0,1.6,1.9,2.0,1.9,1.6,1.0,0.3,0,0,0,0,0,0,0,0,0,0}, // y = 74
		};
		forEvwyPoint(p -> {
			final var value = (35 <= p.getX() && p.getX() <= 64) && (26 <= p.getY() && p.getY() <= 74) ? valid[p.getY() - 26][p.getX() - 35] : 0;
			final var msg = String.format("В точке %s ожидалась освещённость %f", p,value);
			assertEquals(msg,value,sun.getE(p),0.2d);		
		});
	}
	@Test
	public void testEcllipseSun40x20Line() {
		final var sun = new SunEllipse(10, new Trajectory(Point.create(50, 50)), 40,20, true,"");
		var valid = new double[][]{
					//X: 26,...
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 35
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.1,0.5,0.9,1.2,1.4,1.6,1.8,1.9,2.0,2.0,2.0,1.9,1.8,1.6,1.4,1.2,0.9,0.5,0.1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 36
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0.5,1.1,1.6,2.1,2.5,2.8,3.2,3.4,3.6,3.8,3.9,4.0,4.0,4.0,3.9,3.8,3.6,3.4,3.2,2.8,2.5,2.1,1.6,1.1,0.5,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 37
			new double[]{0,0,0,0,0,0,0,0,0,0.3,1.1,1.8,2.4,3.0,3.6,4.0,4.5,4.8,5.1,5.4,5.6,5.8,5.9,6.0,6.0,6.0,5.9,5.8,5.6,5.4,5.1,4.8,4.5,4.0,3.6,3.0,2.4,1.8,1.1,0.3,0,0,0,0,0,0,0,0,0}, // y = 38
			new double[]{0,0,0,0,0,0,0,0.4,1.3,2.2,3.0,3.7,4.3,4.9,5.5,6.0,6.4,6.8,7.1,7.4,7.6,7.8,7.9,8.0,8.0,8.0,7.9,7.8,7.6,7.4,7.1,6.8,6.4,6.0,5.5,4.9,4.3,3.7,3.0,2.2,1.3,0.4,0,0,0,0,0,0,0}, // y = 39
			new double[]{0,0,0,0,0,0.0,1.1,2.1,3.1,4.0,4.8,5.6,6.2,6.9,7.4,7.9,8.4,8.8,9.1,9.4,9.6,9.8,9.9,10.0,10.0,10.0,9.9,9.8,9.6,9.4,9.1,8.8,8.4,7.9,7.4,6.9,6.2,5.6,4.8,4.0,3.1,2.1,1.1,0.0,0,0,0,0,0}, // y = 40
			new double[]{0,0,0,0,0.4,1.6,2.8,3.9,4.9,5.8,6.6,7.4,8.1,8.8,9.3,9.9,9.7,9.3,8.9,8.6,8.4,8.2,8.1,8.0,8.0,8.0,8.1,8.2,8.4,8.6,8.9,9.3,9.7,9.9,9.3,8.8,8.1,7.4,6.6,5.8,4.9,3.9,2.8,1.6,0.4,0,0,0,0}, // y = 41
			new double[]{0,0,0,0.6,2.0,3.2,4.4,5.6,6.6,7.6,8.5,9.3,10.0,9.3,8.7,8.2,7.7,7.3,7.0,6.7,6.4,6.2,6.1,6.0,6.0,6.0,6.1,6.2,6.4,6.7,7.0,7.3,7.7,8.2,8.7,9.3,10.0,9.3,8.5,7.6,6.6,5.6,4.4,3.2,2.0,0.6,0,0,0}, // y = 42
			new double[]{0,0,0.5,2.0,3.4,4.8,6.0,7.2,8.3,9.3,9.7,8.9,8.1,7.4,6.8,6.3,5.8,5.4,5.0,4.7,4.4,4.2,4.1,4.0,4.0,4.0,4.1,4.2,4.4,4.7,5.0,5.4,5.8,6.3,6.8,7.4,8.1,8.9,9.7,9.3,8.3,7.2,6.0,4.8,3.4,2.0,0.5,0,0}, // y = 43
			new double[]{0,0.2,1.8,3.3,4.8,6.3,7.6,8.8,10.0,8.9,8.0,7.1,6.3,5.6,4.9,4.3,3.8,3.4,3.0,2.7,2.4,2.3,2.1,2.0,2.0,2.0,2.1,2.3,2.4,2.7,3.0,3.4,3.8,4.3,4.9,5.6,6.3,7.1,8.0,8.9,10.0,8.8,7.6,6.3,4.8,3.3,1.8,0.2,0}, // y = 44
			new double[]{0,1.2,2.9,4.6,6.2,7.7,9.1,9.6,8.4,7.2,6.2,5.3,4.4,3.7,3.0,2.4,1.9,1.4,1.0,0.7,0.5,0.3,0.1,0.0,0.0,0.0,0.1,0.3,0.5,0.7,1.0,1.4,1.9,2.4,3.0,3.7,4.4,5.3,6.2,7.2,8.4,9.6,9.1,7.7,6.2,4.6,2.9,1.2,0}, // y = 45
			new double[]{0.3,2.2,3.9,5.7,7.4,9.0,9.5,8.1,6.8,5.6,4.5,3.5,2.6,1.8,1.1,0.5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.5,1.1,1.8,2.6,3.5,4.5,5.6,6.8,8.1,9.5,9.0,7.4,5.7,3.9,2.2,0.3}, // y = 46
			new double[]{1.0,2.9,4.8,6.6,8.4,9.9,8.2,6.7,5.3,4.0,2.8,1.7,0.8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.8,1.7,2.8,4.0,5.3,6.7,8.2,9.9,8.4,6.6,4.8,2.9,1.0}, // y = 47
			new double[]{1.6,3.5,5.4,7.4,9.2,8.9,7.1,5.4,3.9,2.4,1.2,0.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.0,1.2,2.4,3.9,5.4,7.1,8.9,9.2,7.4,5.4,3.5,1.6}, // y = 48
			new double[]{1.9,3.9,5.9,7.8,9.8,8.2,6.3,4.4,2.7,1.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1.0,2.7,4.4,6.3,8.2,9.8,7.8,5.9,3.9,1.9}, // y = 49
			new double[]{2.0,4.0,6.0,8.0,10.0,8.0,6.0,4.0,2.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2.0,4.0,6.0,8.0,10.0,8.0,6.0,4.0,2.0}, // y = 50
			new double[]{1.9,3.9,5.9,7.8,9.8,8.2,6.3,4.4,2.7,1.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1.0,2.7,4.4,6.3,8.2,9.8,7.8,5.9,3.9,1.9}, // y = 51
			new double[]{1.6,3.5,5.4,7.4,9.2,8.9,7.1,5.4,3.9,2.4,1.2,0.0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.0,1.2,2.4,3.9,5.4,7.1,8.9,9.2,7.4,5.4,3.5,1.6}, // y = 52
			new double[]{1.0,2.9,4.8,6.6,8.4,9.9,8.2,6.7,5.3,4.0,2.8,1.7,0.8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.8,1.7,2.8,4.0,5.3,6.7,8.2,9.9,8.4,6.6,4.8,2.9,1.0}, // y = 53
			new double[]{0.3,2.2,3.9,5.7,7.4,9.0,9.5,8.1,6.8,5.6,4.5,3.5,2.6,1.8,1.1,0.5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.5,1.1,1.8,2.6,3.5,4.5,5.6,6.8,8.1,9.5,9.0,7.4,5.7,3.9,2.2,0.3}, // y = 54
			new double[]{0,1.2,2.9,4.6,6.2,7.7,9.1,9.6,8.4,7.2,6.2,5.3,4.4,3.7,3.0,2.4,1.9,1.4,1.0,0.7,0.5,0.3,0.1,0.0,0.0,0.0,0.1,0.3,0.5,0.7,1.0,1.4,1.9,2.4,3.0,3.7,4.4,5.3,6.2,7.2,8.4,9.6,9.1,7.7,6.2,4.6,2.9,1.2,0}, // y = 55
			new double[]{0,0.2,1.8,3.3,4.8,6.3,7.6,8.8,10.0,8.9,8.0,7.1,6.3,5.6,4.9,4.3,3.8,3.4,3.0,2.7,2.4,2.3,2.1,2.0,2.0,2.0,2.1,2.3,2.4,2.7,3.0,3.4,3.8,4.3,4.9,5.6,6.3,7.1,8.0,8.9,10.0,8.8,7.6,6.3,4.8,3.3,1.8,0.2,0}, // y = 56
			new double[]{0,0,0.5,2.0,3.4,4.8,6.0,7.2,8.3,9.3,9.7,8.9,8.1,7.4,6.8,6.3,5.8,5.4,5.0,4.7,4.4,4.2,4.1,4.0,4.0,4.0,4.1,4.2,4.4,4.7,5.0,5.4,5.8,6.3,6.8,7.4,8.1,8.9,9.7,9.3,8.3,7.2,6.0,4.8,3.4,2.0,0.5,0,0}, // y = 57
			new double[]{0,0,0,0.6,2.0,3.2,4.4,5.6,6.6,7.6,8.5,9.3,10.0,9.3,8.7,8.2,7.7,7.3,7.0,6.7,6.4,6.2,6.1,6.0,6.0,6.0,6.1,6.2,6.4,6.7,7.0,7.3,7.7,8.2,8.7,9.3,10.0,9.3,8.5,7.6,6.6,5.6,4.4,3.2,2.0,0.6,0,0,0}, // y = 58
			new double[]{0,0,0,0,0.4,1.6,2.8,3.9,4.9,5.8,6.6,7.4,8.1,8.8,9.3,9.9,9.7,9.3,8.9,8.6,8.4,8.2,8.1,8.0,8.0,8.0,8.1,8.2,8.4,8.6,8.9,9.3,9.7,9.9,9.3,8.8,8.1,7.4,6.6,5.8,4.9,3.9,2.8,1.6,0.4,0,0,0,0}, // y = 59
			new double[]{0,0,0,0,0,0.0,1.1,2.1,3.1,4.0,4.8,5.6,6.2,6.9,7.4,7.9,8.4,8.8,9.1,9.4,9.6,9.8,9.9,10.0,10.0,10.0,9.9,9.8,9.6,9.4,9.1,8.8,8.4,7.9,7.4,6.9,6.2,5.6,4.8,4.0,3.1,2.1,1.1,0.0,0,0,0,0,0}, // y = 60
			new double[]{0,0,0,0,0,0,0,0.4,1.3,2.2,3.0,3.7,4.3,4.9,5.5,6.0,6.4,6.8,7.1,7.4,7.6,7.8,7.9,8.0,8.0,8.0,7.9,7.8,7.6,7.4,7.1,6.8,6.4,6.0,5.5,4.9,4.3,3.7,3.0,2.2,1.3,0.4,0,0,0,0,0,0,0}, // y = 61
			new double[]{0,0,0,0,0,0,0,0,0,0.3,1.1,1.8,2.4,3.0,3.6,4.0,4.5,4.8,5.1,5.4,5.6,5.8,5.9,6.0,6.0,6.0,5.9,5.8,5.6,5.4,5.1,4.8,4.5,4.0,3.6,3.0,2.4,1.8,1.1,0.3,0,0,0,0,0,0,0,0,0}, // y = 62
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0.5,1.1,1.6,2.1,2.5,2.8,3.2,3.4,3.6,3.8,3.9,4.0,4.0,4.0,3.9,3.8,3.6,3.4,3.2,2.8,2.5,2.1,1.6,1.1,0.5,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 63
			new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.1,0.5,0.9,1.2,1.4,1.6,1.8,1.9,2.0,2.0,2.0,1.9,1.8,1.6,1.4,1.2,0.9,0.5,0.1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // y = 64
		};
		var pp = sun.getE(Point.create(26, 46));
		pp = 0;
		forEvwyPoint(p -> {
			final var value = (26 <= p.getX() && p.getX() <= 74) && (35 <= p.getY() && p.getY() <= 64) ? valid[p.getY() - 35][p.getX() - 26] : 0;
			final var msg = String.format("В точке %s ожидалась освещённость %f", p,value);
			assertEquals(msg,value,sun.getE(p),0.2d);		
		});
	}
	

	@Test
	public void testMove() {
		//Пустая функция у этого солнца
	}

	@Test
	public void testPaint() {
		//Графика не тестируется!
	}
	
}
