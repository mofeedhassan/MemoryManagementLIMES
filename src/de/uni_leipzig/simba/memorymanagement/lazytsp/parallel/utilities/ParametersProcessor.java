package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities;

import java.util.HashMap;
import java.util.Map;

import de.uni_leipzig.simba.measures.MeasureFactory;
import de.uni_leipzig.simba.memorymanagement.io.LazyTspConfigReader;
import de.uni_leipzig.simba.memorymanagement.lazytsp.LazyTspComponents;
import de.uni_leipzig.simba.memorymanagement.lazytsp.LazyTspComponents.LazyTspComponentsBuilder;
import de.uni_leipzig.simba.memorymanagement.structure.CacheType;
import de.uni_leipzig.simba.memorymanagement.structure.ClusteringType;
import de.uni_leipzig.simba.memorymanagement.structure.ParameterType;
import de.uni_leipzig.simba.memorymanagement.structure.RuningPartType;
import de.uni_leipzig.simba.memorymanagement.structure.SolverType;
/**
 * 
 * @author mofeed
 * This class extracts parallel processing's parameters from the configuration and creates obejcts related to each parameter
 */
public class ParametersProcessor {
	
	Map<ParameterType,String> parameters = new HashMap<>();
	public String getParameter(ParameterType param){return parameters.get(param);}
	public Map<ParameterType,String> getParameter(){return parameters;}

	
	//ectracts parameters from configuration reader and add it to parameters list
	public Map<ParameterType,String> extractParametersFromFile()
	{
		LazyTspConfigReader confReader = LazyTspConfigReader.getInstance();
		for(String property : confReader.getAllPropertyNames())
			parameters.put(ParameterType.valueOf(property), confReader.getProperty(property));// if it is no a parameter in enum it will crash
		return parameters;
	}
	//It uses builder pattern to build an object represents the components of the parallelization run
	public LazyTspComponentsBuilder setUpLazyTspComponents()
	{
		extractParametersFromFile();
		
		LazyTspComponentsBuilder components = new LazyTspComponents.LazyTspComponentsBuilder()
				.setCache(CacheType.valueOf(parameters.get(ParameterType.CACHE)), Integer.valueOf(parameters.get(ParameterType.CAPACITY)))
				.setCluster(ClusteringType.valueOf(parameters.get(ParameterType.CLUSTER)))
				.setCore(Integer.valueOf(parameters.get(ParameterType.CORE)))
				.setPart(RuningPartType.valueOf(parameters.get(ParameterType.PART)))
				.setRepetition(Integer.valueOf(parameters.get(ParameterType.REPEATS)))
				.setCapacity(Integer.valueOf(parameters.get(ParameterType.CAPACITY)))
				.setThreshold(Double.valueOf(parameters.get(ParameterType.THRESHOLDS)))
				.setMeasure(String.valueOf(parameters.get(ParameterType.TYPE)));
		if((parameters.get(ParameterType.SOLVER).equals(SolverType.TSPSOLVER) || parameters.get(ParameterType.SOLVER).equals(SolverType.SIMPLESOLVER)))
			components = components.setSolver(SolverType.valueOf(parameters.get(ParameterType.SOLVER)), Double.valueOf(parameters.get(ParameterType.SOLVERPARAMETER)));
		else
			components = components.setSolver(SolverType.valueOf(parameters.get(ParameterType.SOLVER)));

		return components;
	}
	
	public void displayRunParameters()
	{
		
		System.out.println("The program will run with parameteres:");
		System.out.println("Data path: "+parameters.get(ParameterType.DATA));
		System.out.println("Results Folder path: "+parameters.get(ParameterType.RESULTSFOLDER));
		System.out.println("Base Foder path: "+parameters.get(ParameterType.BASEFOLDER));
		System.out.println("Folder for steps run times: "+parameters.get(ParameterType.INFOFOLDER));
		System.out.println("Record steps times (y/n): "+parameters.get(ParameterType.RECORDRUNTIMES));
		System.out.println("Thresholds: "+parameters.get(ParameterType.THRESHOLDS));
		System.out.println("Capacities: "+parameters.get(ParameterType.CAPACITY));
		System.out.println("Clusters: "+parameters.get(ParameterType.CLUSTER));
		System.out.println("Solvers: "+parameters.get(ParameterType.SOLVER));
		System.out.println("Caches: "+parameters.get(ParameterType.CACHE));
		System.out.println("Repeats number: "+parameters.get(ParameterType.REPEATS));
		System.out.println("Integer/String(HR3,Trigram): "+parameters.get(ParameterType.TYPE));
		System.out.println("Run base/approach/both(1,2,0): "+parameters.get(ParameterType.PART));
		System.out.println("Core: "+parameters.get(ParameterType.CORE));

	}
}
