package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.RefactoredPLTSP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.measures.Measure;
import de.uni_leipzig.simba.measures.space.EuclideanMetric;
import de.uni_leipzig.simba.memorymanagement.Index.planner.DataManipulationCommand;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCache;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCacheFactory;
import de.uni_leipzig.simba.memorymanagement.indexing.Hr3Indexer;
import de.uni_leipzig.simba.memorymanagement.indexing.Indexer;
import de.uni_leipzig.simba.memorymanagement.structure.CacheType;

public class ParallelController {
	////////////////////////////////////////////////    INPUTS   /////////////////////////////////
	//////Cache data for both cases shared cache or individual caches
	private DataCache cache=null; //The capacity object to bes used by tasks(in case of cache/task(s) - lock as common)
	private String cacheName=""; //the cache name to be created
	private int capacity=0; // the capacity of cache to be created for each task (in case of cache/task)
	private boolean shared=false;
	//Indexer of the data used, passed as parameter from the main()
	Indexer indexer=null;
	//Threshold of the indexer, passed as parameter from the main()
	Double threshold=0.0;
	// The measure used for comparisons
	Measure measure=null;
	//The number of processors that controls the process of creation of the threads of ParallelRunner class
	private static int numberOfProcessors = Runtime.getRuntime().availableProcessors(); //lock as common (updates after finish and when start
	private static Map<Integer,String> resultsCollector = new HashMap<Integer, String>();

	////////////////////////////////////////////////    OUTPUTS   /////////////////////////////////
    Map<Integer,Mapping> mappings = new HashMap<Integer, Mapping>();//cluster,mappings
    int numberOfMappings=0; //lock as common when update the number of mappings
    Map<Integer,Double> runTimes = new HashMap<Integer, Double>(); //lock as common when update the runtimes
    static LinkedHashMap<Integer,List<DataManipulationCommand>> clustersCommands = new LinkedHashMap<Integer, List<DataManipulationCommand>>();
    private int clusterId=0;
    ///////////////////////////////////////////////
	private static final ExecutorService pool =  Executors.newFixedThreadPool(getNumberOfProcessors());
	public /*static*/ List<String> results = new ArrayList<String>();
	public /*static*/ List<String> cacheHitsValues = new ArrayList<String>();
	public /*static*/ List<String> cacheMissesValues = new ArrayList<String>();
	public int[] clustersIds=null;

	
    /**
	 * @return the cache
	 */
	public DataCache getCache() {
		return cache;
	}


	/**
	 * @return the resultsCollector
	 */
	public static Map<Integer,String> getResultsCollector() {
		return resultsCollector;
	}

	public ParallelController(LinkedHashMap<Integer, List<DataManipulationCommand>> clustersCommands, DataCache cache, Integer capacity, Hr3Indexer indexer, Measure measure, Double threshold, boolean share, int numberOfProcessors)
	{
		this.clustersCommands=clustersCommands;
		this.cache = cache;
		this.capacity = capacity;
		this.indexer = indexer;
		this.measure=measure;
		this.threshold=threshold;
		this.shared=share;
		this.numberOfProcessors=numberOfProcessors;
	}
    // This constructor is called in case of a common cache object is used among the tasks
	public ParallelController(DataCache cache,Indexer indexer, Measure measure,Double threshold, int numberOfProcessors){
		this.cache=cache; // this is a common cache for all
		this.threshold=threshold;
		this.indexer = indexer;
		this.measure=measure;
		this.setNumberOfProcessors(numberOfProcessors);
	}
	
    /**
     * This constructor is used to get the basic information required to start the threads for parallel execution of the tasks
     * @param clustersCommands The list of the clusters and their associated data manipulation commands
     * @param cache The cache required for each task to store and retrieve the computed mapping
     * @param capacity The cache capacity of data items
     * @param indexer It is used to retrieve a computationally required data item from the data set. It is either trigram or hr3
     * @param measure The similarity measure used in computing the mappings. It is either Euclidean or  
     * @param threshold The threshold
     */
	public ParallelController(LinkedHashMap<Integer,List<DataManipulationCommand>> clustersCommands,String cache,int capacity,Indexer indexer, Measure measure,Double threshold){
		this.clustersCommands=clustersCommands;
		this.cacheName = cache;
		this.capacity = capacity;
		this.indexer = indexer;
		this.measure=measure;
		this.threshold=threshold;
	}
	
	/**
	 * This method is responsible for creating the threads where the parallel tasks are executed
	 * @param clustersCommands The list of commands to be executed grouped by clusters id
	 * @param singleCache It specifies if the cache will be a single shared objject or multiple individual objects for the task
	 */
	public void runParallelTasks()
	{
		int NrProcessors = getNumberOfProcessors();
		
		if(NrProcessors > 0)
		{
			List<Future<String>> futures = new ArrayList<>();
			//logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":"+(getLineNumber()-1)+":runParallelTasks() :"+ System.currentTimeMillis());

			for(int clusterId = 0; clusterId < clustersCommands.size();clusterId++)
			{

					if(!shared || getCache() == null) // if it is not shared || the first creation of a shared cache
					{
						cache = DataCacheFactory.createCache(CacheType.valueOf(cacheName), Integer.MAX_VALUE, 1,capacity);//create new cache object
						System.out.println("**ATTENTION**This part creates NON-SHARED caches. It is preceeded by using the constructor specified for sharing cache. In case of not using it the defaul is FIFO");
					}
					
					int currentCluster = clustersIds[clusterId];
					List<DataManipulationCommand> commands  = clustersCommands.get(currentCluster);//get a cluster's commands set
					//logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":"+(getLineNumber()-1)+":runParallelTasks(): assign to a task cluster Id = "+currentCluster+", commands = "+commands+" :"+ System.currentTimeMillis());
					
					ParallelRunner task = new ParallelRunner(getCache(), commands, measure, threshold, indexer);// create a task and assign the commands set and the cache

					futures.add(pool.submit(task));//send to parallelism
				//}
			}
			
			int totalMappings=0;
			for(Future<String> future: futures)
			{
				try
				{
					results.add(future.get());//add the task as clusterId,threadRunTime,mappings
					cacheHitsValues.add(String.valueOf(cache.getHits())); //#hits when the thread executes in this moment (not total)
					cacheMissesValues.add(String.valueOf(cache.getMisses())); //#misses when the thread executes in this moment (not total)
				}
				catch (InterruptedException | ExecutionException e) {e.printStackTrace();}
			}			
			pool.shutdown(); 
		}
	}
	
	
	LinkedHashMap<Integer,List<DataManipulationCommand>> getClustersCommands() {
		return clustersCommands;
	}
	
	void setClustersCommands(LinkedHashMap<Integer,List<DataManipulationCommand>> clusterCommands) {
		this.clustersCommands = clusterCommands;
	}
	/**
	 * This static method is called by the running threads to accumulate the results computed by each one
	 *It is called inside the thread.run() after finishing its computations
	 * @param clusterId : int The cluster id where the results belong to
	 * @param result : String represents the task results combined and separated by colon : and it includes
	 * the start runtime, the end runtime and  the number of mappings
	 */
	synchronized public static void addToResultsCollector(int clusterId, String result)
	{getResultsCollector().put(clusterId, result);}
	
	/**
	 * This method gives the list of commands based on a given cluster's id
	 * @param clusterId : int represents the cluster id in focus
	 * @return List<DataManipulationCommand> : the list of the data manipulation commands associated with the cluster
	 */
	List<DataManipulationCommand> getClusterCommands(Integer clusterId)
	{ return clustersCommands.get(clusterId);}
	/**
	 * This method shows if there is a free processor to be assigned new task. IT is called by the controller
	 * @return true if there is a free processor and false otherwise
	 */
	synchronized static boolean  checkFreeProcessor()
	{return (getNumberOfProcessors()!=0);}
	
	/**
	 * This method updates the number of processors that shows how many processors are free. It is called in  two situations
	 * First- called by the controller when a task is started by the controller with sign '-' to decrement number of processors
	 * Second- called by the thread when the task is finished with sign '+' to increment number of processors
	 * @param sign
	 */
	synchronized public static void updateNoProcessors(char sign)
	{
		if(sign=='-')
			numberOfProcessors--;
		if(sign=='+')
			numberOfProcessors++;
	}
	
	/**
	 * This method specifies if the cache will be shared among the tasks or it is individual caches for each.
	 * It is called by the controller before starting threads creation
	 * @param shared true if single shared cache required , false otherwise
	 */
	public void setCacheSharing(boolean shared){this.shared = shared;}
	/**
	 * This method is responsible for creating the threads where the parallel tasks are executed
	 * @param clustersCommands The list of commands to be executed grouped by clusters id
	 * @param singleCache It specifies if the cache will be a single shared objject or multiple individual objects for the tass
	 */
	public void runParallelController(LinkedHashMap<Integer,List<DataManipulationCommand>> clustersCommands, boolean shared)
	{
		
/*		if(getNumberOfProcessors() > 0)
		{
			this.shared=shared;
			while(true)
			{
				if(checkFreeProcessor()) // there is free processor
				{
					if(!shared || getCache() == null) // if it is not shared || the first creation of a shared cache
						cache = DataCacheFactory.createCache(CacheType.valueOf(cacheName), Integer.MAX_VALUE, 1,capacity);
					List<DataManipulationCommand> commands  = clustersCommands.get(clusterId);
					ParallelRunner task = new ParallelRunner(getCache(), commands, measure, threshold, indexer);
					task.setClusterId(clusterId++);
					task.run();
					updateNoProcessors('-');// the controller decrements the number of processors when thread started
					if(clusterId == clustersCommands.size())
						break;
				}
			}
		}*/
		
		if(getNumberOfProcessors() > 0)
		{
			this.shared=shared;
			while(true)
			{
/*				if(checkFreeProcessor()) // there is free processor
				{*/
					//System.out.println("#Threads "+Thread.getAllStackTraces().keySet().size());
					if(!shared || getCache() == null) // if it is not shared || the first creation of a shared cache
						cache = DataCacheFactory.createCache(CacheType.valueOf(cacheName), Integer.MAX_VALUE, 1,capacity);
					List<DataManipulationCommand> commands  = clustersCommands.get(clusterId);
					ParallelRunner task = new ParallelRunner(getCache(), commands, measure, threshold, indexer);
					task.setClusterId(clusterId++);
					try {
						task.call();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println("#Threads "+Thread.getAllStackTraces().keySet().size());
					updateNoProcessors('-');// the controller decrements the number of processors when thread started
					if(clusterId == clustersCommands.size())
						break;
/*				}
*/			}
		}
	}
	
	static int getNumberOfProcessors() {
		return numberOfProcessors;
	}
	static void setNumberOfProcessors(int numberOfProcessors) {
		ParallelController.numberOfProcessors = numberOfProcessors;
	}
	private static void initializeClustersCommmand()
	{
		clustersCommands.put(0, new ArrayList<DataManipulationCommand>());
		clustersCommands.put(1, new ArrayList<DataManipulationCommand>());
		clustersCommands.put(2, new ArrayList<DataManipulationCommand>());
		clustersCommands.put(3, new ArrayList<DataManipulationCommand>());
		clustersCommands.put(4, new ArrayList<DataManipulationCommand>());
		clustersCommands.put(5, new ArrayList<DataManipulationCommand>());

	}
	
	public static void main(String[] args)
	{
		initializeClustersCommmand();
		//System.out.println(numberOfProcessors);
		if(getNumberOfProcessors() > 0)
		{
			
			for (Integer clusterId : clustersCommands.keySet()) {
				//create object from threadable class
				ParallelRunner runner = new ParallelRunner(clusterId);
				//assign the parameter to it
				//run the thread
				try {
					runner.call();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				while(getNumberOfProcessors() == 0) ;
				//while(true){//lock n if(n!=0) break; unlock n)}
			}
		}
	}


}
