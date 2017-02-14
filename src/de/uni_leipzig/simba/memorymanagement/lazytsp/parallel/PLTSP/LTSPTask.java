package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.PLTSP;

import java.util.List;
import java.util.concurrent.Callable;

/*import org.apache.log4j.Logger;*/
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.matheclipse.core.reflection.system.Trace;

import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.measures.Measure;
import de.uni_leipzig.simba.measures.space.EuclideanMetric;
import de.uni_leipzig.simba.memorymanagement.Index.planner.DataManipulationCommand;
import de.uni_leipzig.simba.memorymanagement.Index.planner.execution.CacheAccessExecution;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCache;
import de.uni_leipzig.simba.memorymanagement.indexing.Indexer;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.RefactoredPLTSP.ParallelController;

public class LTSPTask implements Callable<String>{
	static java.util.logging.Logger logger = Logger.getLogger("LIMES"); 
	private int clusterId;
	private static int count=0;
	/////////////////////////
	//inputs
		DataCache cache=null; //lock as common
		Measure measure;
		Indexer indexer=null;
		Double threshold;
		//outputs mappings,runtimes
	    Mapping mappings = new Mapping();
	    int numberOfMappings=0; //lmappings number retrieved from the process inside the Runner (thread)
	    double runTime = 0; //the runtime of the process inside the Runner (thread)
	    List<DataManipulationCommand> commands =null;
	/////////////////////////    
	
	public LTSPTask(){


	}
	public LTSPTask(int i){this.setClusterId(i);}
	public LTSPTask(DataCache c, List<DataManipulationCommand> commands, Measure measure, double threshold, Indexer indexer)
	{
		this.cache=c;
		this.commands=commands;
		this.measure=measure;
		this.threshold=threshold;
		this.indexer=indexer;
	}

	int getClusterId() {
		return clusterId;
	}
	void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}
	@Override
	public String call() throws Exception {//what runs for each thread
		
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":call():start thread action:"+ System.currentTimeMillis());

		CacheAccessExecution cae = new CacheAccessExecution(cache, commands, measure, threshold, indexer);
		logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":call():initialize cache"+ System.currentTimeMillis());

		//ParallelController.updateNoProcessors('+');// increments the number of processors as it frees one
		//count++;//count the threads number
		//System.out.println("Nr. threads "+count);
		long runnerRuntimeStart = System.currentTimeMillis();
		int NrResultedMappings = cae.run();//returns number of mappings
		long runnerRuntimeEnd = System.currentTimeMillis();
		
		//System.out.println("Nr. of threads ="+ count);
		//System.out.println(Thread.currentThread().getId());
		//String result=clusterId+":"+runnerRuntimeStart+":"+runnerRuntimeEnd+":"+NrResultedMappings;
		String result=clusterId+":"+(runnerRuntimeEnd-runnerRuntimeStart)+":"+NrResultedMappings;//(runnerRuntimeEnd-runnerRuntimeStart) the thread run time
		return result;
	}
}
