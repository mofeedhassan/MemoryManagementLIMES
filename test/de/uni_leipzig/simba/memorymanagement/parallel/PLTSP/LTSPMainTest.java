package de.uni_leipzig.simba.memorymanagement.parallel.PLTSP;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_leipzig.simba.memorymanagement.testTSPCaching.TSPCachingTester;

public class LTSPMainTest {

/*	@Test
	public void testRun1()
	{
		LTSPMain.main(new String[] {"run","PLTSP1KParametersRelative","resultsPLTSPHR3","1","true"});
	}*/

	@Test
	public void testRun2()
	{
		TSPCachingTester.main(new String[] {"run","TSP1KParametersRelative","resultsTSPHR3","1","true"});
	}

}
