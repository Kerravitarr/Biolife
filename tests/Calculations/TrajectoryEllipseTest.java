/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package Calculations;

import Calculations.Trajectories.TrajectoryEllipse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kerravitarr
 */
public class TrajectoryEllipseTest {
	
	public TrajectoryEllipseTest() {
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
	}
	
	@After
	public void tearDown() {
	}

	@Test
	public void cyrcle() {
		var t = new TrajectoryEllipse(0, Point.create(50, 50), 0, 10);
		var valid = new Point[]{Point.create(55, 50), Point.create(55, 50)};
		for (int i = 0; i < valid.length; i++) {
			Point point = valid[i];
			assertEquals(String.format("Для шага №%d (%f рад)",i,Math.PI*i/360),point, t.nextPosition());
		}
	}
	@Test
	public void ellipse20x40() {
		var t = new TrajectoryEllipse(0, Point.create(50, 50), 0, 20,40);
		var valid = new Point[]{Point.create(60, 50)};
		for (int i = 0; i < valid.length; i++) {
			Point point = valid[i];
			assertEquals(String.format("Для шага №%d (%f рад)",i,Math.PI*i/360),point, t.nextPosition());
		}
	}
	@Test
	public void ellipse40x20() {
		var t = new TrajectoryEllipse(0, Point.create(50, 50), 0, 40,20);
		var valid = new Point[]{Point.create(70, 50)};
		for (int i = 0; i < valid.length; i++) {
			Point point = valid[i];
			assertEquals(String.format("Для шага №%d (%f рад)",i,Math.PI*i/360),point, t.nextPosition());
		}
	}
	
}
