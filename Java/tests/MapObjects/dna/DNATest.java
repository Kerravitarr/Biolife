/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package MapObjects.dna;

import MapObjects.AliveCell;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 *
 * @author zeus
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DNATest {
	
	public DNATest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println();
		System.out.println("Тестирование ДНК");
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
	/**Создаёт ДНК состоящую из 10 команд от 0 до 9 и PC находящимся в позиции 5
	 * @return 
	 */
	private DNA make(){
		var ret = new DNA(10);
		for(var i = 0 ; i < 10 ; i++)
			ret.mind[i] = i;
		ret.next(5);
		return ret;
	}
	/**
	 * Test of interrupt method, of class DNA.
	 */
	@Test
	public void testHelpFunction() {
		var make = make();
		assertArrayEquals(new int[]{0,1,2,3,4,5,6,7,8,9},make.mind);
		assertArrayEquals(new int[]{0,1,2,3,4,5,6,7},make.interrupts);
		assertEquals(5,make.getPC());
	}

	/**
	 * Test of interrupt method, of class DNA.
	 */
	@Test
	public void testInterrupt() {
		System.err.print("функцию interrupt невозможно проверить!");
		/*AliveCell cell = null;
		int num = 0;
		DNA instance = null;
		instance.interrupt(cell, num);
		// TODO review the generated test code and remove the default call to fail.
		fail("interrupt невозможн опроверить!");*/
	}

	/**
	 * Test of getIndex method, of class DNA.
	 */
	@Test
	public void testGetIndex() {
		var make = make();
		assertEquals(5,make.getIndex(0));
		assertEquals(6,make.getIndex(1));
		assertEquals(5,make.getIndex(10));
		assertEquals(4,make.getIndex(-1));
		assertEquals(5,make.getIndex(-10));
	}

	/**
	 * Test of get method, of class DNA.
	 */
	@Test
	public void testGet_0args() {
		var make = make();
		assertEquals(CommandList.list[5],make.get());
	}

	/**
	 * Test of get method, of class DNA.
	 */
	@Test
	public void testGet_int() {
		var make = make();
		assertEquals(CommandList.list[5],make.get(0));
		assertEquals(CommandList.list[6],make.get(1));
		assertEquals(CommandList.list[5],make.get(10));
		assertEquals(CommandList.list[4],make.get(-1));
		assertEquals(CommandList.list[5],make.get(-10));
	}

	/**
	 * Test of get method, of class DNA.
	 */
	@Test
	public void testGet_int_boolean_relative() {
		var make = make();
		assertEquals(5,make.get(0,false));
		assertEquals(6,make.get(1,false));
		assertEquals(5,make.get(10,false));
		assertEquals(4,make.get(-1,false));
		assertEquals(5,make.get(-10,false));
	}
	@Test
	public void testGet_int_boolean_absolute() {
		var make = make();
		assertEquals(0,make.get(0,true));
		assertEquals(1,make.get(1,true));
		assertEquals(0,make.get(10,true));
		assertEquals(9,make.get(-1,true));
		assertEquals(0,make.get(-10,true));
	}

	/**
	 * Test of next method, of class DNA.
	 */
	@Test
	public void testNext() {
		var make = make();
		make.next(0);
		assertEquals(5,make.getPC());
		make.next(1);
		assertEquals(6,make.getPC());
		make.next(-1);
		assertEquals(5,make.getPC());
		make.next(10);
		assertEquals(5,make.getPC());
		make.next(5);
		assertEquals(0,make.getPC());
		make.next(2);
		assertEquals(2,make.getPC());
		make.next(-4);
		assertEquals(8,make.getPC());
	}

	/**
	 * Test of getPC method, of class DNA.
	 */
	@Test
	public void testGetPC() {
		var make = make();
		assertEquals(5,make.getPC());
	}
	@Test
	public void testSubDNA_absolute() {
		var make = make();
		assertArrayEquals(new int[] {0,1,2,3,4},make.subDNA(0, true, 5));
		assertArrayEquals(new int[] {1,2,3,4,5},make.subDNA(1, true, 5));
		assertArrayEquals(new int[] {5,6,7,8,9},make.subDNA(5, true, 5));
		assertArrayEquals(new int[] {9,0,1,2,3},make.subDNA(9, true, 5));
		assertArrayEquals(new int[] {5,6,7,8,9},make.subDNA(15, true, 5));
	}
	@Test
	public void testSubDNA_relative() {
		var make = make();
		assertArrayEquals(new int[] {5,6,7,8,9},make.subDNA(0, false, 5));
		assertArrayEquals(new int[] {6,7,8,9,0},make.subDNA(1, false, 5));
		assertArrayEquals(new int[] {0,1,2,3,4},make.subDNA(5, false, 5));
		assertArrayEquals(new int[] {4,5,6,7,8},make.subDNA(9, false, 5));
		assertArrayEquals(new int[] {0,1,2,3,4},make.subDNA(15, false, 5));
	}
	@Test(expected = IllegalArgumentException.class)
	public void testSubDNA_count0() {
		var make = make();
		make.subDNA(0, false, 0);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testSubDNA_count11() {
		var make = make();
		make.subDNA(0, false, 11);
	}
	@Test
	public void testUpdate_3args_1() {
		var make = make();
		make = make.update(0, true, 64);
		assertArrayEquals(new int[] {64,1,2,3,4,5,6,7,8,9},make.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,7},make.interrupts);
		assertEquals(5,make.getPC());
	}
	@Test
	public void testUpdate_3args_1_absolute() {
		var make = make();
		assertArrayEquals(new int[] {64,1,2,3,4,5,6,7,8,9},make.update(0, true, 64).mind);
		assertArrayEquals(new int[] {0,64,2,3,4,5,6,7,8,9},make.update(1, true, 64).mind);
		assertArrayEquals(new int[] {0,1,2,3,4,64,6,7,8,9},make.update(5, true, 64).mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,7,8,64},make.update(9, true, 64).mind);
		assertArrayEquals(new int[] {0,1,2,3,4,64,6,7,8,9},make.update(15, true, 64).mind);
	}
	@Test
	public void testUpdate_3args_1_relative() {
		var make = make();
		assertArrayEquals(new int[] {0,1,2,3,4,64,6,7,8,9},make.update(0, false, 64).mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,64,7,8,9},make.update(1, false, 64).mind);
		assertArrayEquals(new int[] {64,1,2,3,4,5,6,7,8,9},make.update(5, false, 64).mind);
		assertArrayEquals(new int[] {0,1,2,3,64,5,6,7,8,9},make.update(9, false, 64).mind);
		assertArrayEquals(new int[] {64,1,2,3,4,5,6,7,8,9},make.update(15, false, 64).mind);
	}
	@Test
	public void testUpdate_3args_2() {
		var make = make();
		make = make.update(0, true, new int[] {64,65,66,67,68});
		assertArrayEquals(new int[] {64,65,66,67,68,5,6,7,8,9},make.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,7},make.interrupts);
		assertEquals(5,make.getPC());
	}
	@Test
	public void testUpdate_3args_2_absolute() {
		var make = make();
		assertArrayEquals(new int[] {64,65,66,67,68,5,6,7,8,9},make.update(0, true, new int[] {64,65,66,67,68}).mind);
		assertArrayEquals(new int[] {0,64,65,66,67,68,6,7,8,9},make.update(1, true, new int[] {64,65,66,67,68}).mind);
		assertArrayEquals(new int[] {0,1,2,3,4,64,65,66,67,68},make.update(5, true, new int[] {64,65,66,67,68}).mind);
		assertArrayEquals(new int[] {65,66,67,68,4,5,6,7,8,64},make.update(9, true, new int[] {64,65,66,67,68}).mind);
		assertArrayEquals(new int[] {0,1,2,3,4,64,65,66,67,68},make.update(15, true, new int[] {64,65,66,67,68}).mind);
	}
	@Test
	public void testUpdate_3args_2_relative() {
		var make = make();
		assertArrayEquals(new int[] {0,1,2,3,4,64,65,66,67,68},make.update(0, false, new int[] {64,65,66,67,68}).mind);
		assertArrayEquals(new int[] {68,1,2,3,4,5,64,65,66,67},make.update(1, false, new int[] {64,65,66,67,68}).mind);
		assertArrayEquals(new int[] {64,65,66,67,68,5,6,7,8,9},make.update(5, false, new int[] {64,65,66,67,68}).mind);
		assertArrayEquals(new int[] {0,1,2,3,64,65,66,67,68,9},make.update(9, false, new int[] {64,65,66,67,68}).mind);
		assertArrayEquals(new int[] {64,65,66,67,68,5,6,7,8,9},make.update(15, false, new int[] {64,65,66,67,68}).mind);
	}
	@Test
	public void testUpdate_3args_2_count0() {
		var make = make();
		assertEquals(make,make.update(0, false, new int[] {}));
	}
	@Test(expected = IllegalArgumentException.class)
	public void testUpdate_3args_2_count11() {
		var make = make();
		make.update(0, true, new int[] {64,65,66,67,68,69,70,71,72,73,74});
	}
	@Test
	public void testDoubling_int_boolean_absolute() {
		var test = make();
		test = test.doubling(0, true);
		assertArrayEquals(new int[] {0,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,2,3,4,5,6,7,8},test.interrupts);
		assertEquals(6,test.getPC());
		
		test = make();
		test = test.doubling(1, true);
		assertArrayEquals(new int[] {0,1,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,3,4,5,6,7,8},test.interrupts);
		assertEquals(6,test.getPC());
		
		test = make();
		test = test.doubling(5, true);
		assertArrayEquals(new int[] {0,1,2,3,4,5,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,7,8},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.doubling(9, true);
		assertArrayEquals(new int[] {9,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {1,2,3,4,5,6,7,8},test.interrupts);
		assertEquals(6,test.getPC());
		
		test = make();
		test = test.doubling(15, true);
		assertArrayEquals(new int[] {0,1,2,3,4,5,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,7,8},test.interrupts);
		assertEquals(5,test.getPC());
	}
	@Test
	public void testDoubling_int_boolean_relative() {
		var test = make();
		test = test.doubling(0, false);
		assertArrayEquals(new int[] {0,1,2,3,4,5,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,7,8},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.doubling(1, false);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,8},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.doubling(5, false);
		assertArrayEquals(new int[] {0,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,2,3,4,5,6,7,8},test.interrupts);
		assertEquals(6,test.getPC());
		
		test = make();
		test = test.doubling(9, false);
		assertArrayEquals(new int[] {0,1,2,3,4,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,6,7,8},test.interrupts);
		assertEquals(6,test.getPC());
		
		test = make();
		test = test.doubling(15, false);
		assertArrayEquals(new int[] {0,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,2,3,4,5,6,7,8},test.interrupts);
		assertEquals(6,test.getPC());
	}
	@Test
	public void testDoubling_3args_absolute() {
		var test = make();
		test = test.doubling(0, true,5);
		assertArrayEquals(new int[] {0,1,2,3,4,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.doubling(1, true,5);
		assertArrayEquals(new int[] {0,1,2,3,4,5,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,11,12},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.doubling(5, true,5);
		assertArrayEquals(new int[] {5,6,7,8,9,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {5,6,7,8,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.doubling(9, true,5);
		assertArrayEquals(new int[] {0,1,2,3,9,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.doubling(15, true,5);
		assertArrayEquals(new int[] {5,6,7,8,9,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {5,6,7,8,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
	}
	@Test
	public void testDoubling_3args_relative() {
		var test = make();
		test = test.doubling(0, false,5);
		assertArrayEquals(new int[] {5,6,7,8,9,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {5,6,7,8,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.doubling(1, false,5);
		assertArrayEquals(new int[] {0,6,7,8,9,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,6,7,8,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.doubling(5, false,5);
		assertArrayEquals(new int[] {0,1,2,3,4,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.doubling(9, false,5);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,7,8,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,7},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.doubling(15, false,5);
		assertArrayEquals(new int[] {0,1,2,3,4,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
	}
	@Test
	public void testDoubling_3args_count0() {
		var test = make();
		assertEquals(test,test.doubling(0, true,0));
	}
	@Test(expected = IllegalArgumentException.class)
	public void testDoubling_3args_count11() {
		var test = make();
		test.doubling(0, true,11);
	}
	@Test
	public void testInsert_absolute() {
		var test = make();
		var cmds = new int[] {64,65,66,67,68};
		test = test.insert(0, true,cmds);
		assertArrayEquals(new int[] {64,65,66,67,68,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {5,6,7,8,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.insert(1, true,cmds);
		assertArrayEquals(new int[] {0,64,65,66,67,68,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,6,7,8,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.insert(5, true,cmds);
		assertArrayEquals(new int[] {0,1,2,3,4,64,65,66,67,68,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.insert(9, true,cmds);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,7,8,64,65,66,67,68,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,7},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.insert(15, true,cmds);
		assertArrayEquals(new int[] {0,1,2,3,4,64,65,66,67,68,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
	}
	@Test
	public void testInsert_relative() {
		var test = make();
		var cmds = new int[] {64,65,66,67,68};
		test = test.insert(0, false,cmds);
		assertArrayEquals(new int[] {0,1,2,3,4,64,65,66,67,68,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.insert(1, false,cmds);
		assertArrayEquals(new int[] {0,1,2,3,4,5,64,65,66,67,68,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,11,12},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.insert(5, false,cmds);
		assertArrayEquals(new int[] {64,65,66,67,68,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {5,6,7,8,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.insert(9, false,cmds);
		assertArrayEquals(new int[] {0,1,2,3,64,65,66,67,68,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
		
		test = make();
		test = test.insert(15, false,cmds);
		assertArrayEquals(new int[] {64,65,66,67,68,0,1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {5,6,7,8,9,10,11,12},test.interrupts);
		assertEquals(10,test.getPC());
	}
	@Test
	public void testInsert_count0() {
		var make = make();
		assertEquals(make,make.insert(0, false, new int[] {}));
	}

	/**
	 * Test of compression method, of class DNA.
	 */
	@Test
	public void testCompression_int_boolean_absolute() {
		var test = make();
		test = test.compression(0, true);
		assertArrayEquals(new int[] {1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,0,1,2,3,4,5,6},test.interrupts);
		assertEquals(4,test.getPC());
		
		test = make();
		test = test.compression(1, true);
		assertArrayEquals(new int[] {0,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,1,2,3,4,5,6},test.interrupts);
		assertEquals(4,test.getPC());
		
		test = make();
		test = test.compression(5, true);
		assertArrayEquals(new int[] {0,1,2,3,4,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,5,6},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.compression(9, true);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,7,8}, test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,7},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.compression(15, true);
		assertArrayEquals(new int[] {0,1,2,3,4,6,7,8,9}, test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,5,6},test.interrupts);
		assertEquals(5,test.getPC());
	}
	@Test
	public void testCompression_int_boolean_relative() {
		var test = make();
		test = test.compression(0, false);
		assertArrayEquals(new int[] {0,1,2,3,4,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,5,6},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.compression(1, false);
		assertArrayEquals(new int[] {0,1,2,3,4,5,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,5,6,6},test.interrupts);
		assertEquals(5,test.getPC());
		
		test = make();
		test = test.compression(5, false);
		assertArrayEquals(new int[] {1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,0,1,2,3,4,5,6},test.interrupts);
		assertEquals(4,test.getPC());
		
		test = make();
		test = test.compression(9, false);
		assertArrayEquals(new int[] {0,1,2,3,5,6,7,8,9}, test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,4,5,6},test.interrupts);
		assertEquals(4,test.getPC());
		
		test = make();
		test = test.compression(15, false);
		assertArrayEquals(new int[] {1,2,3,4,5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,0,1,2,3,4,5,6},test.interrupts);
		assertEquals(4,test.getPC());
	}
	@Test
	public void testCompression_3args_absolute() {		
		var test = make();
		test = test.compression(0, true,5);
		assertArrayEquals(new int[] {5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,0,0,0,0,0,1,2},test.interrupts);
		assertEquals(0,test.getPC());
		
		test = make();
		test = test.compression(1, true,5);
		assertArrayEquals(new int[] {0,6,7,8,9}, test.mind);
		assertArrayEquals(new int[] {0,1,1,1,1,1,1,2},test.interrupts);
		assertEquals(1,test.getPC());
		
		test = make();
		test = test.compression(5, true,5);
		assertArrayEquals(new int[] {0,1,2,3,4},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,4,4,4},test.interrupts);
		assertEquals(0,test.getPC());
		
		test = make();
		test = test.compression(9, true,5);
		assertArrayEquals(new int[] {4,5,6,7,8},test.mind);
		assertArrayEquals(new int[] {4,4,4,4,0,1,2,3},test.interrupts);
		assertEquals(1,test.getPC());
		
		test = make();
		test = test.compression(15, true,5);
		assertArrayEquals(new int[] {0,1,2,3,4},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,4,4,4},test.interrupts);
		assertEquals(0,test.getPC());
	}
	@Test
	public void testCompression_3args_relative() {
		var test = make();
		test = test.compression(0, false,5);
		assertArrayEquals(new int[] {0,1,2,3,4},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,4,4,4},test.interrupts);
		assertEquals(0,test.getPC());
		
		test = make();
		test = test.compression(1, false,5);
		assertArrayEquals(new int[] {1,2,3,4,5},test.mind);
		assertArrayEquals(new int[] {4,0,1,2,3,4,4,4},test.interrupts);
		assertEquals(4,test.getPC());
		
		test = make();
		test = test.compression(5, false,5);
		assertArrayEquals(new int[] {5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,0,0,0,0,0,1,2},test.interrupts);
		assertEquals(0,test.getPC());
		
		test = make();
		test = test.compression(9, false,5);
		assertArrayEquals(new int[] {0,1,2,3,9},test.mind);
		assertArrayEquals(new int[] {0,1,2,3,4,4,4,4},test.interrupts);
		assertEquals(4,test.getPC());
		
		test = make();
		test = test.compression(15, false,5);
		assertArrayEquals(new int[] {5,6,7,8,9},test.mind);
		assertArrayEquals(new int[] {0,0,0,0,0,0,1,2},test.interrupts);
		assertEquals(0,test.getPC());
	}
	@Test(expected = IllegalArgumentException.class)
	public void testCompression_3args_ex_count0() {
		var test = make();
		test.compression(15, false,0);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testCompression_3args_ex_count11() {
		var test = make();
		test.compression(15, false,11);
	}

	/**
	 * Test of equals method, of class DNA.
	 */
	@Test
	public void testEquals() {
		var def = make();
		var v = make();
		assertTrue(def.equals(v, 0));
		v = v.update(0, true, new int[]{64,65,66,67,68});
		assertFalse(def.equals(v, 0));
		assertFalse(def.equals(v, 1));
		assertFalse(def.equals(v, 2));
		assertFalse(def.equals(v, 3));
		assertFalse(def.equals(v, 4));
		assertTrue(def.equals(v, 5));
		v = v.insert(0, true, new int[]{64});
		assertFalse(def.equals(v, 0));
		assertFalse(def.equals(v, 1000));
	}

	/**
	 * Test of toJSON method, of class DNA.
	 */
	@Test
	public void testToJSON() {
		var test = make();
		assertEquals("{\"size\":10,\"mind\":[0,1,2,3,4,5,6,7,8,9],\"instruction\":5,\"interrupts\":[0,1,2,3,4,5,6,7]}",test.toJSON().toJSONString());
	}
	
}
