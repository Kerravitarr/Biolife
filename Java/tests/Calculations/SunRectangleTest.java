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

/**
 *
 * @author Kerravitarr
 */
public class SunRectangleTest {
	
	public SunRectangleTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
		Configurations.buildMap(Configurations.WORLD_TYPE.RECTANGLE, 100, 100, null);
		Configurations.DIRTY_WATER = 2;
	}
	
	@After
	public void tearDown() {
	}
	
}
