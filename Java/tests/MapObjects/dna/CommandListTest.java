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
		Configurations.buildMap(new Configurations(Configurations.WORLD_TYPE.RECTANGLE, 10, 10), null);
	}
	
	@Test
	public void testCmdNames() {
		var adam = new AliveCell();
		for (CommandDNA cmd : CommandList.list) {
			try{
				if(cmd.isInterrupt())
					cmd.getInterrupt(adam, adam.getDna());
				CommandDNA.setFullMod(true);
				if(cmd.toString().contains(".Long"))
					System.err.println(cmd.toString());
				CommandDNA.setFullMod(false);
				if(cmd.toString().contains(".Shot"))
					System.err.println(cmd.toString());
			}catch(RuntimeException e){
				System.err.println(e);
			}
		}	
	}
	
	@Test
	public void testCmdParams() {
		var adam = new AliveCell();
		for (CommandDNA cmd : CommandList.list) {
			try{
				String paramT = null;
				String paramF = null;
				for (int j = 0; j < cmd.getCountParams(); j++) {
					CommandDNA.setFullMod(true);
					String param2T = cmd.getParam(adam, j, adam.getDna()); //Проверяем, чтобы все параметры имели разные названия 
					if(param2T.equals(paramT))
						throw new RuntimeException("Параметры совпали для " + cmd);
					paramT = param2T;
					CommandDNA.setFullMod(false);
					String param2F = cmd.getParam(adam, j, adam.getDna()); //Проверяем, чтобы все параметры имели разные названия 
					if(param2F.equals(paramF))
						throw new RuntimeException("Параметры совпали для " + cmd);
					paramF = param2F;
				}
			}catch(RuntimeException e){
				System.err.println(e);
			}
		}	
	}
	@Test
	public void testCmdBranchs() {
		var adam = new AliveCell();
		for (CommandDNA cmd : CommandList.list) {
			try{
				String paramT = null;
				String paramF = null;
				for (int j = 0; j < cmd.getCountBranch(); j++) {
					CommandDNA.setFullMod(true);
					String param2T = cmd.getBranch(adam, j, adam.getDna()); //Проверяем, чтобы все параметры имели разные названия 
					if(param2T.equals(paramT))
						throw new RuntimeException("Параметры совпали для " + cmd);
					paramT = param2T;
					CommandDNA.setFullMod(false);
					String param2F = cmd.getBranch(adam, j, adam.getDna()); //Проверяем, чтобы все параметры имели разные названия 
					if(param2F.equals(paramF))
						throw new RuntimeException("Параметры совпали для " + cmd);
					paramF = param2F;
				}
			}catch(RuntimeException e){
				System.err.println(e);
			}
		}	
	}
	
}
