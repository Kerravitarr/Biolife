/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package Utils;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Map;
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
public class UtilsTest {
	
	public UtilsTest() {
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

	@Test
	public void testRound_double_int() {
		System.out.println("round");
		assertEquals(0.00037, Utils.round(0.0003658313517468447045911834644229, 2), 0);
		assertEquals(0.15, Utils.round(0.15, 2), 0);
		assertEquals(0.16, Utils.round(0.156, 2), 0);
		assertEquals(0.006, Utils.round(0.006, 2), 0);
		assertEquals(0.0066, Utils.round(0.0066, 2), 0);
		assertEquals(2.3, Utils.round(2.340, 2), 0);
		assertEquals(230, Utils.round(234.56, 2), 0);
		assertEquals(210, Utils.round(205.67, 2), 0);
		assertEquals(0.26, Utils.round(0.256, 2), 0);
		assertEquals(0.24, Utils.round(0.2356, 2), 0);
		assertEquals(0.0056, Utils.round(0.0056, 2), 0);
		assertEquals(0.051, Utils.round(0.05062, 2), 0);
		assertEquals(120, Utils.round(120.02, 2), 0);
		assertEquals(0.57, Utils.round(0.567, 2), 0);
		assertEquals(0.56, Utils.round(0.561, 2), 0);
		assertEquals(0.56, Utils.round(0.5600, 2), 0);
		assertEquals(0, Utils.round(0, 2), 0);
		assertEquals(5, Utils.round(5, 2), 0);
		assertEquals(-1, Utils.round(-1, 2), 0);
		assertEquals(-0.052, Utils.round(-0.05223, 2), 0);
	}
	
}
