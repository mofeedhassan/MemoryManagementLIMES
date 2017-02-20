package de.uni_leipzig.simba.memorymanagement.parallel;

import static org.junit.Assert.*;

import org.junit.Test;
import org.matheclipse.core.reflection.system.Array;

import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities.ParametersProcessor;
import de.uni_leipzig.simba.memorymanagement.structure.ParameterType;

public class ParametersProcessorTest {
	ParametersProcessor pprocessor = new ParametersProcessor();
	
	@Test
	public void testExtractParametersFromFile() {
		for (ParameterType parameter : pprocessor.extractParametersFromFile().keySet()) {
			System.out.println(parameter);
		}
	}
	
	@Test
	public void testSetUpLazyTspComponents()
	{
		pprocessor.setUpLazyTspComponents();
	}

}
