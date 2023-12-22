/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package start;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
    MapObjects.dna.CommandListTest.class,
    MapObjects.dna.DNATest.class,
    Calculations.PointTest.class,
    Calculations.SunEllipseTest.class,
    Calculations.TrajectoryEllipseTest.class
})
public class TestList {}
