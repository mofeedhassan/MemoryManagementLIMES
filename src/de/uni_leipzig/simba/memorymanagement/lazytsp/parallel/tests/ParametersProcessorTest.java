package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.tests;

import de.uni_leipzig.simba.memorymanagement.TSPSolver.Map;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities.ParametersProcessor;
import de.uni_leipzig.simba.memorymanagement.structure.ParameterType;

public class ParametersProcessorTest {

	public static void main(String[] args) {
		ParametersProcessor pp = new ParametersProcessor();
		pp.extractParametersFromFile();
		java.util.Map<ParameterType, String> extractParametersFromFile = pp.getParameter();
		for (ParameterType paramter : extractParametersFromFile.keySet()) {
			System.out.println(paramter+":"+extractParametersFromFile.get(paramter));
		}

	}

}
