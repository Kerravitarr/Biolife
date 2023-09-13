/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package Calculations;

import MapObjects.CellObject;
import Utils.JSON;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
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
public class StreamEllipseTest extends AbstractBenchmark{
	
	public StreamEllipseTest() {
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
	public void testAction() {
		System.out.println("action");
		CellObject cell = null;
		StreamEllipse instance = null;
		instance.action(cell);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}
	
	@BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 0)
	@Test
	public void speedMath() {
		var summ = 0;
		for (int x1 = 0; x1 < 10; x1++) {
			for (int y1 = 0; y1 < 10; y1++) {
				var p1 = new Point(x1, y1);
				for (int x2 = 0; x2 < 10; x2++) {
					for (int y2 = 0; y2 < 10; y2++) {
						var p2 = new Point(x2, y2);
						final var d = p1.distance(p2);
						summ += Math.atan2(d.y,d.x);
					}
				}
			}
		}
		assertTrue( summ != 0);
	}

	
}
