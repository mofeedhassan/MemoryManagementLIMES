package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.RefactoredPLTSP;

import java.util.List;
import java.util.concurrent.Callable;

import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.measures.Measure;
import de.uni_leipzig.simba.measures.space.EuclideanMetric;
import de.uni_leipzig.simba.memorymanagement.Index.planner.DataManipulationCommand;
import de.uni_leipzig.simba.memorymanagement.Index.planner.execution.CacheAccessExecution;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCache;
import de.uni_leipzig.simba.memorymanagement.indexing.Indexer;

/**
 * 
 * @author mofeed
 * This class represents a single task to run on the processor's core. It comprises from set of commands to be
 * executed one by one that lead to provide set of mappings regarding a given threshold using a cache
 */
public class ParallelRunner implements Callable<String>{// = the task
	private int clusterId;
	private static int count=0;
	/////////////////////////
	//inputs
		DataCache cache=null; // areference to the cache used by the runners in case of sharing and its own otherwise. It should be locked as common resource
		Measure measure;
		Indexer indexer=null; // contains the points inside the task
		Double threshold;
		//outputs mappings,runtimes
	    Mapping mappings = new Mapping();
	    int numberOfMappings=0; //lmappings number retrieved from the process inside the Runner (thread)
	    double runTime = 0; //the runtime of the process inside the Runner (thread)
	    //static LinkedHashMap<Integer,List<DataManipulationCommand>> clusterCommands = new LinkedHashMap<Integer, List<DataManipulationCommand>>();
	    List<DataManipulationCommand> commands =null; // list of commands to be executed in the core
	/////////////////////////    
	
	public ParallelRunner(){}
	public ParallelRunner(int i){this.setClusterId(i);}
	public ParallelRunner(DataCache c, List<DataManipulationCommand> commands, Measure measure, double threshold, Indexer indexer)
	{
		this.cache=c;
		this.commands=commands;
		this.measure=measure;
		this.threshold=threshold;
		this.indexer=indexer;
	}
	//private synchronized void decrementN(){n--;}
	//private synchronized void incrementN(){n++;}
	@Override
	public String call() throws Exception
	{
		//decrement the numberOfProcessors
		//run the required steps
		//add the new runtime to a common pool
		//add the mapping results/number to a common variable
		//increment the numberOfProcessors
		//System.out.println("Star running thread for cluaster = "+getClusterId());
		//List<DataManipulationCommand> commands=clusterCommands.get(clusterId);
		//System.out.println("#Threads "+Thread.getAllStackTraces().keySet().size());

		CacheAccessExecution cae = new CacheAccessExecution(cache, commands, measure, threshold, indexer);
		ParallelController.updateNoProcessors('+');// increments the number of processors as it frees one
		count++; //increment number of tasks ran in processor
		long runnerRuntimeStart = System.currentTimeMillis();
		int NrResultedMappings = cae.run();
		long runnerRuntimeEnd = System.currentTimeMillis();
		//System.out.println("Nr. of threads ="+ count);
		System.out.println(Thread.currentThread().getId());
		String result=runnerRuntimeStart+":"+runnerRuntimeEnd+":"+NrResultedMappings;
		ParallelController.addToResultsCollector(clusterId, result);
		return result;
	}


	int getClusterId() {
		return clusterId;
	}
	void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

}
