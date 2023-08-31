/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package MapObjects.dna;

import MapObjects.AliveCell;
import Calculations.Configurations;
import Calculations.World;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import GUI.Legend;

/**
 *
 * @author zeus
 */
public class CommandListTest {
	
	public CommandListTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println();
		System.out.println("Тестирование списка команд ДНК");
	}
	
	@Before
	public void setUp() {
		Configurations.makeWorld(100, 100);
		new World().stop();
		Configurations.legend = new Legend();
	}
	
	@Test
	public void testCommandDNAText() {
		//Небольшие тестики, после создания новой функции
		//Они не влияют на логику, но нужны для отоброжения
		var adam = new AliveCell();
		for (CommandDNA cmd : CommandList.list) {
			try{
				if(cmd.isInterrupt())
					cmd.getInterrupt(adam, adam.getDna());
				String param = null;
				for (int j = 0; j < cmd.getCountParams(); j++) {
					String param2 = cmd.getParam(adam, j, adam.getDna());
					if(param2.equals(param))
						throw new RuntimeException("Параметры совпали для " + cmd);
					param = param2;
				}
			}catch(RuntimeException e){
				System.out.println(e);
			}
		}		
	}
}
