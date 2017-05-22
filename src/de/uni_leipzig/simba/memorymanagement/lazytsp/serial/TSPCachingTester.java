package de.uni_leipzig.simba.memorymanagement.lazytsp.serial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;*/
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;

import de.uni_leipzig.simba.measures.space.EuclideanMetric;
import de.uni_leipzig.simba.measures.string.TrigramMeasure;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Clustering;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.ClusteringFactory;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Graph;
import de.uni_leipzig.simba.memorymanagement.Index.planner.DataManipulationCommand;
import de.uni_leipzig.simba.memorymanagement.Index.planner.TSPPlanner;
import de.uni_leipzig.simba.memorymanagement.Index.planner.TSPSolver;
import de.uni_leipzig.simba.memorymanagement.Index.planner.execution.CacheAccessExecution;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCache;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCacheFactory;
import de.uni_leipzig.simba.memorymanagement.indexing.Hr3Indexer;
import de.uni_leipzig.simba.memorymanagement.indexing.TrigramIndexer;
import de.uni_leipzig.simba.memorymanagement.pathfinder.PathFinder;
import de.uni_leipzig.simba.memorymanagement.pathfinder.SimpleSolver;
import de.uni_leipzig.simba.memorymanagement.pathfinder.SolverFactory;
import de.uni_leipzig.simba.memorymanagement.structure.CacheType;
import de.uni_leipzig.simba.memorymanagement.structure.ClusteringType;
import de.uni_leipzig.simba.memorymanagement.structure.DataType;
import de.uni_leipzig.simba.memorymanagement.structure.RuningPartType;
import de.uni_leipzig.simba.memorymanagement.structure.SolverType;

/**
 * @author mofeed
 * This class aims to create tests for the TSP solver combined with different caching approaches
 * The input: large datasets (in our case for integers lgd and for strings dbpedia labels in en,de,fr)
 * It provides a sequence of Si and Ti to be interlinked which based on it the caching approaches work on.
 * It is not only in memory but for the large datasets it works on HDD too.
 * The out put is in a file including capacity,runtimes,misses, hits 
 */

/*run
/media/mofeed/A0621C46621C24164/CachingTests/parameters100
/media/mofeed/A0621C46621C24164/CachingTests/resultsHR3100
1
true*/

public class TSPCachingTester {
	/**
	 * logger
	 */
	//static Logger log = Logger.getLogger(TSPCachingTester.class.getName());
	static java.util.logging.Logger logger = Logger.getLogger("LIMES"); 


	static List<Double> thresholds= new ArrayList<Double>(); //Threshold: used by HR3 algorithm to specify which space tiling squares to be compared to the current square
	static List<Integer> capcities= new ArrayList<Integer>();//Capacity: The data cache capacity, set of capacities used
	static List<String> caches= new ArrayList<String>(); //Type of data cache (FIFO,LFU,...)
	static List<String> clusters= new ArrayList<String>(); //Cluster: Clustering algorithm used in Lazy TSP
	static List<String> solvers= new ArrayList<String>(); //Solvers: Solver algorithm used in Lazy TSP
	static int repeats=5;  // Number of experiment repetitions
	static DataType dataType = DataType.ECULIDEAN; // type of data either integers/lat. and long. => measure is eculidean or strings/labels => measure trigrams
	static RuningPartType partToRun = RuningPartType.APPROACH; // what parts to run in single experiement (base algorithm, the approach algorithm/LazyTsp, Both)
	static boolean recordSectionsRunningTimes =false;
	static boolean displayInfoOnce =true;
	static int alpha=4;// used by HR3 algorithm
	static double optimTime=100; // used by one of the solvers algorithms
	static int iterations =0; // used by one of the solvers algorithms
	
	static String dataFile="";// The path of file that contains the data
	static String resultsFile="";//The name of the file where results are stored
	static String resultsFolder="";// The folder where result files are stored
	static String runsInfoFolder="";//where running information data are recorded in it
	static String resultsFinalFolder="";//for extracting the required columns for plotting
	static String baseFolder=""; //used for tiling space
	static List<String> runingInfo= new ArrayList<String>();// storing running times for each section of code: indexer, clustering, TSP, caching
	static String InfoPiece="";//piece of run information that accumulated forming record of runningInfo
	static int targetCol=1;

	static Map<Integer,String> resultsFiles= new HashMap<Integer,String> ();
	static String currentDirectory="";

/*	run
	parameters100
	resultsHR3100
	1
	true*/
	public static void main(String args[]) {
		/*Logger.getLogger("ac.biu.nlp.nlp.engineml").setLevel(Level.OFF);
		Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.OFF);
		Logger.getRootLogger().setLevel(Level.OFF);*/
		
        Handler fileHandler  = null;
        try{
			//Creating consoleHandler and fileHandler
			fileHandler  = new FileHandler("./TSP.log");
			logger.addHandler(fileHandler);
			
			Formatter simpleFormatter = new SimpleFormatter();
			fileHandler.setFormatter(simpleFormatter);
			//Setting levels to handlers and LOGGER
			fileHandler.setLevel(Level.ALL);
			logger.setLevel(Level.ALL);
 			
			logger.log(Level.FINE, "Finer logged");
		}catch(IOException exception){
			logger.log(Level.SEVERE, "Error occur in FileHandler.", exception);
		}
        
		//get current folder
		currentDirectory = System.getProperty("user.dir");
		currentDirectory = standardizePath(currentDirectory); //put folder path in standard form by ensuring it ends with / 
		resultsFolder = runsInfoFolder = resultsFinalFolder = currentDirectory; // all in the same folder
		
		resultsFile = resultsFolder+"/Cache"; // each result file has prefix "Cache"

		String option  = args[0]; //two options exist, either run the experiment or extract information from results
		
		if(option.equals("run")) // run experiment
		{
			if(args.length < 2)
			{
				System.out.println("parameters for run are: [1] path to parameters file \n [2] path to results folder \n");
				System.exit(1);
			}
			System.out.println("Starting run");
			
			initializeExperimentParameters(args[1]); //read experiment parameters from file "parameters100"
			resultsFolder = standardizePath(resultsFolder);
			resultsFile=resultsFolder+"Cache";
			displayRunParameters();// display parameters the experiment runs with them
			
			TSPCachingTester tsp = new TSPCachingTester();
			tsp.runExperiemment(); //run experiment
		}
		else if(option.equals("extract"))
		{
			if(args.length != 4)
			{
				System.out.println("parameters for extract are: [1] path to results folder \n [2] column to extract {base line: 2 time, 3 hits, 4 misses / approach: 6 time, 7 hits, 8 misses} \n [3] path to final extractions");
				System.exit(1);
			}
			System.out.println("Starting data extraction");

			resultsFolder = standardizePath(args[1]);
			targetCol =Integer.parseInt(args[2]);
			resultsFinalFolder =standardizePath(args[3]);
			List<String> res = extractResults(/*"/media/mofeed/A0621C46621C24164/03_Work/CachingProject/Experiement/testResults/ServerTestHR3/8"*/resultsFolder, targetCol);
			for (String lines : res) {
				System.out.println(lines);
			}
			initializeFilesNames();
			wrtieToFile(res, resultsFinalFolder+"/"+resultsFiles.get(targetCol));
		}
		else
			System.out.println("Wrong operation option (run/exract)");
	}

	private  void runExperiemment()
	{
		long begin=0,end=0,InfoBegin=0; // values to record the strat and begin of a running phase time such as clustering the data, the solver action
		List<DataManipulationCommand> plan=null; // plan's commands provided by solver algorithm, It specifies the sequence of command to work with data such as load, compare and remove 
		DataCache dataCache=null; // data cache (e.g.FIFO)
		CacheAccessExecution cae=null; // executes plan over the data cache => load, compare if exist, remove to free space
		TSPPlanner planner =null; // The planner that uses the solvers to create the plan
		Graph g=null; // the graph representing the data
		String commonInfo="";
		String typeLabel="";
		if(dataType == DataType.ECULIDEAN)
			typeLabel="Ecu";
		else
			typeLabel="Tri";
		for(String cluster:clusters){ //pick a cluster of the list in case of more than one algorithm to be used
			for(String solver:solvers){ //pick a solver of the list in case of more than one algorithm to be used
				for(String cache:caches){ //pick a cache of the list in case of more than one cache to be used
					List<String> results= null; // intitalize the results list
					String cache_type=null;
					cache_type = cache;
					
					for (Double threshold : thresholds) {//apply set of algorithms on different thresholds' values
						results= new ArrayList<String>(); 
						results.add("Capacity\tRuntime\tHits\tMisses\n");
	//					System.out.println("Capacity\tRuntime\tHits\tMisses");
						String titles="What\tCapacity\tIndexing\tGraph\tClustering\tClustersNo\tPath\tPlan\tMappings\tPlanExec\n";
						runingInfo.add(titles);
						for (Integer capacity : capcities) {// assign a capacity to the used data cache
							String line="";
							for(int i=0; i< repeats; i++)// number of times to repeat the experiments with the formed collection of parameters and algorithms
							{
								InfoPiece="";
								commonInfo=capacity+"\t";
								//if(type.equals("integer"))
								if(dataType == DataType.ECULIDEAN)// data is lat/long and it uses eculidean measure
								{
									//InfoPiece="BaseLine:/";
									/// create Hr3 indexer
									Hr3Indexer hr3 = new Hr3Indexer(alpha, threshold);
									hr3.endColumn = 2;
									hr3.baseFolder=baseFolder;
									InfoBegin = System.currentTimeMillis();

									///////start indexing
									hr3.runIndexing(new File(dataFile), true);
									

									commonInfo+=(System.currentTimeMillis()-InfoBegin)+"\t";

									/////// create graph
									InfoBegin = System.currentTimeMillis();
									g = hr3.generateTaskGraph();
									logger.info(Thread.currentThread().getName()+":"+ getClass().getName()+":"+(getLineNumber()-1)+":call():graph craeted with # Nodes = "+g.getAllNodes().size()+" #Edges = "+g.getAllEdges().size()+":"+ System.currentTimeMillis());
									commonInfo+=(System.currentTimeMillis()-InfoBegin)+"\t";
									//   runingInfo.add(InfoPiece);
									///create planner object
									planner = new TSPPlanner();
									logger.info(Thread.currentThread().getName()+":"+ getClass().getName()+":"+(getLineNumber()-1)+":call():object planner is created:"+ System.currentTimeMillis());

									////////////////////////////------------Base Line (without TSP)-------------------------------////////////////////////////

									if(partToRun == RuningPartType.BASE || partToRun == RuningPartType.BOTH)// not used for the parallelization paper
									{
										//start baseline
										/// start creating plan with load,flush,get
										begin = System.currentTimeMillis();
										plan = planner.plan(g);
										logger.info(Thread.currentThread().getName()+":"+ getClass().getName()+":"+(getLineNumber()-1)+":call():Plan is created with length = "+plan.size()+":"+ System.currentTimeMillis());
										InfoPiece += "BaseLine\t" +commonInfo +"\tNA\tNA\tNA\t"+(System.currentTimeMillis()-begin)+"\t";
										///create the cache
										dataCache = DataCacheFactory.createCache(CacheType.valueOf(cache), Integer.MAX_VALUE, 1,capacity); // new SimpleCache(capacity);
										logger.info(Thread.currentThread().getName()+":"+ getClass().getName()+":"+(getLineNumber()-1)+":call():cache is created:"+ System.currentTimeMillis());
										cae = new CacheAccessExecution(dataCache, plan, new EuclideanMetric(), threshold, hr3);
										logger.info(Thread.currentThread().getName()+":"+ getClass().getName()+":"+(getLineNumber()-1)+":call():CacheAccessExecution craeted:"+System.currentTimeMillis());

										///execute the plan
										InfoBegin = System.currentTimeMillis();
										int numberOfMappings = cae.run();
										logger.info(Thread.currentThread().getName()+":"+ getClass().getName()+":"+(getLineNumber()-1)+":call():Mappings created = "+numberOfMappings+":"+ System.currentTimeMillis());
										InfoPiece+=numberOfMappings+"\t";
										end = System.currentTimeMillis();
										InfoPiece+=(end-InfoBegin)+"\n";
										if(displayInfoOnce)
										{
											System.out.println(titles);
											System.out.println(InfoPiece);
										}
										runingInfo.add(InfoPiece);
										System.out.println("Capacity\tRuntime\tHits\tMisses");
										System.out.print("Baseline (" + capacity + ")\t");
										System.out.print((end - begin) + "\t");
										System.out.print(dataCache.getHits() + "\t");
										System.out.print(dataCache.getMisses() + "\t");
										line="Baseline (" + capacity + ")\t" + "\t"+(end - begin) + "\t"+dataCache.getHits() + "\t"+dataCache.getMisses();
										if(partToRun == RuningPartType.BASE)
											line+= "\n";
										else//0
											line+= "\t";
									}
									////////////////////////////------------Approach-------------------------------////////////////////////////

									if(partToRun == RuningPartType.APPROACH || partToRun == RuningPartType.BOTH)
									{
										if(partToRun == RuningPartType.BOTH)
											InfoPiece="";
										begin = System.currentTimeMillis();
										iterations = 0;
										/// create clustering method
										Clustering gc = ClusteringFactory.createClustering(ClusteringType.valueOf(cluster));

										///start graph clustering
										InfoBegin = System.currentTimeMillis();
										Map<Integer, Cluster> clusters = gc.cluster(g, capacity);
										logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":"+(getLineNumber()-1)+":runExperiments(): clustering leads to #clusters = "+clusters.size()+" :"+ System.currentTimeMillis());

										InfoPiece="Approach\t" +commonInfo +(System.currentTimeMillis()-InfoBegin)+"\t"+clusters.size()+"\t";
										if(displayInfoOnce)
											System.out.println(InfoPiece);

										///create path
										InfoBegin = System.currentTimeMillis();
										PathFinder theSolver = SolverFactory.createSolver(SolverType.valueOf(solver));// get solver
										SimpleSolver.optimizationTime = optimTime;// incase it is simple solver otherwise it will take the default
										TSPSolver.iterations = iterations;// in case it is TSP solver, otherwise the default is set inside the class- To change you need to do that from code
										
										int[] path = theSolver.getPath(clusters);
										logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":"+(getLineNumber()-1)+":runExperiments(): solver created path with size = "+path.length+" :"+ System.currentTimeMillis());

										InfoPiece+=(System.currentTimeMillis()-InfoBegin)+"\t";
										logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":"+(getLineNumber()-1)+":runExperiments(): create plan :"+ System.currentTimeMillis());

										/// create and execution plan
										InfoBegin = System.currentTimeMillis();
										plan = planner.plan(clusters, path);
										InfoPiece+=(System.currentTimeMillis()-InfoBegin)+"\t";
										///create cache
										dataCache = DataCacheFactory.createCache(CacheType.valueOf(cache), Integer.MAX_VALUE, 1,capacity);  //new SimpleCache(capacity);
										cae = new CacheAccessExecution(dataCache, plan, new EuclideanMetric(), threshold, hr3);

										logger.info(Thread.currentThread().getName()+":"+getClass().getName()+":"+(getLineNumber()-1)+":runExperiments(): Run :"+ System.currentTimeMillis());

										///run the plan
										InfoBegin = System.currentTimeMillis();
										int numberOfMappings = cae.run();
										InfoPiece+=numberOfMappings+"\t";
										end = System.currentTimeMillis();
										InfoPiece+=(end-InfoBegin)+"\n";
										if(displayInfoOnce)
										{
											System.out.println(titles);
											System.out.println(InfoPiece);
										}
										runingInfo.add(InfoPiece);
										InfoPiece="";
										/////////////////////overall info
										if(partToRun == RuningPartType.APPROACH)
											line=capacity + "\t"+(end - begin) + "\t"+dataCache.getHits() + "\t"+dataCache.getMisses() + "\n";
										else//0
											line+=capacity + "\t"+(end - begin) + "\t"+dataCache.getHits() + "\t"+dataCache.getMisses() + "\n";
										//results.add(line);
										System.out.println("Capacity\tRuntime\tHits\tMisses");
										System.out.print(capacity + "\t");
										System.out.print((end - begin) + "\t");
										System.out.print(dataCache.getHits() + "\t");
										System.out.print(dataCache.getMisses() + "\n");
										System.out.println(line);
									}
								}
								//else if(type.equals("string"))
								else if(dataType == DataType.TRIGRAMS)	
								{
									TrigramIndexer tix = new TrigramIndexer(threshold);
									tix.endColumn = 2;
									tix.baseFolder=baseFolder;

									///////start indexing the data
									InfoBegin = System.currentTimeMillis();
									tix.runIndexing(new File(dataFile), true);
									commonInfo+=(System.currentTimeMillis()-InfoBegin)+"\t";


									/////// create graph
									InfoBegin = System.currentTimeMillis();
									g = tix.generateTaskGraph();
									commonInfo+=(System.currentTimeMillis()-InfoBegin)+"\t";

									// runingInfo.add(commonInfo);
									
									///create planner object
									planner = new TSPPlanner();

									////////////////////////////------------Base Line (without TSP)-------------------------------////////////////////////////

									if(partToRun == RuningPartType.BASE || partToRun == RuningPartType.BOTH) // not used for the parallelization paper
									{
										//start baseline
										/// start creating plan with load,flush,get
										begin = System.currentTimeMillis();
										plan = planner.plan(g);
										InfoPiece="BaseLine:/" +commonInfo +"\tNA\tNA\tNA\t"+(System.currentTimeMillis()-begin)+"\t";
										if(displayInfoOnce)
											System.out.println(InfoPiece);
										///create the cache
										dataCache = DataCacheFactory.createCache(CacheType.valueOf(cache), Integer.MAX_VALUE, 1,capacity); // new SimpleCache(capacity);
										cae = new CacheAccessExecution(dataCache, plan, new TrigramMeasure(), threshold, tix);

										///execute the plan
										InfoBegin = System.currentTimeMillis();
										int numberOfMappings = cae.run();
										InfoPiece+=numberOfMappings+"\t";
										end = System.currentTimeMillis();
										InfoPiece+=(end-InfoBegin)+"\n";
										if(displayInfoOnce)
										{
											System.out.println(titles);
											System.out.println(InfoPiece);
										}										
										runingInfo.add(InfoPiece);
										System.out.println("Capacity\tRuntime\tHits\tMisses");

										System.out.print("Baseline (" + capacity + ")\t");
										System.out.print((end - begin) + "\t");
										System.out.print(dataCache.getHits() + "\t");
										System.out.print(dataCache.getMisses() + "\t");
										line="Baseline (" + capacity + ")\t" + "\t"+(end - begin) + "\t"+dataCache.getHits() + "\t"+dataCache.getMisses();
										if(partToRun == RuningPartType.BASE)
											line+= "\n";
										else//0
											line+= "\t";
									}
									////////////////////////////------------Approach-------------------------------////////////////////////////
									//if(whatToRun == 2 || whatToRun == 0)//2:approach , 0: both
									if(partToRun == RuningPartType.APPROACH || partToRun == RuningPartType.BOTH)
									{
										if(partToRun == RuningPartType.BOTH)
											InfoPiece="";
										begin = System.currentTimeMillis();
										iterations = 0;
										/// create clustering method
										Clustering gc = ClusteringFactory.createClustering(ClusteringType.valueOf(cluster));

										///start graph clustering
										InfoBegin = System.currentTimeMillis();
										Map<Integer, Cluster> clusters = gc.cluster(g, capacity);
										InfoPiece="Approach\t" +commonInfo +(System.currentTimeMillis()-InfoBegin)+"\t"+clusters.size()+"\t";
										
										///create path that specifies how to move from node to another where each node represents a comparison between 
										//a tiling square with its neighbors within the threshold
										InfoBegin = System.currentTimeMillis();

										PathFinder theSolver = SolverFactory.createSolver(SolverType.valueOf(solver));// get solver
										SimpleSolver.optimizationTime = optimTime;// incase it is simple solver otherwise it will take the default
										TSPSolver.iterations = iterations;// in case it is TSP solver, otherwise the default is set inside the class- To change you need to do that from code
										
										int[] path = theSolver.getPath(clusters);

										InfoPiece+=(System.currentTimeMillis()-InfoBegin)+"\t";

										/// create the execution plan
										InfoBegin = System.currentTimeMillis();
										plan = planner.plan(clusters, path);
										InfoPiece+=(System.currentTimeMillis()-InfoBegin)+"\t";
										
										///create cache
										dataCache = DataCacheFactory.createCache(CacheType.valueOf(cache), Integer.MAX_VALUE, 1,capacity);  //new SimpleCache(capacity);
										cae = new CacheAccessExecution(dataCache, plan, new TrigramMeasure(), threshold, tix);

										///run the plan
										InfoBegin = System.currentTimeMillis();
										int numberOfMappings = cae.run();
										InfoPiece+=numberOfMappings+"\t";
										end = System.currentTimeMillis();
										InfoPiece+=(end-InfoBegin)+"\n";
										if(displayInfoOnce)
										{
											System.out.println(titles);
											System.out.println(InfoPiece);
										}										
										runingInfo.add(InfoPiece);
										InfoPiece="";

										/////////////////////overall info
										if(partToRun == RuningPartType.APPROACH)
											line=capacity + "\t"+(end - begin) + "\t"+dataCache.getHits() + "\t"+dataCache.getMisses() + "\n";
										else//0
											line+=capacity + "\t"+(end - begin) + "\t"+dataCache.getHits() + "\t"+dataCache.getMisses() + "\n";
										System.out.println("Capacity\tRuntime\tHits\tMisses");
										System.out.print(capacity + "\t");
										System.out.print((end - begin) + "\t");
										System.out.print(dataCache.getHits() + "\t");
										System.out.print(dataCache.getMisses() + "\n");
									}
								}
								//after finishing with string or integer
								results.add(line);
								displayInfoOnce =false;
							}//repeats
							//results.add(line);


						}//capacities
						System.out.println("================================================================");
						System.out.println(resultsFile+"_"+threshold+"_"+cache_type+"_"+cluster);

						if(recordSectionsRunningTimes)
							wrtieToFile(runingInfo,runsInfoFolder+typeLabel+"_"+threshold+"_"+cache_type+"_"+cluster+"_"+solver);
						wrtieToFile(results,resultsFile+"_"+threshold+"_"+cache_type+"_"+cluster+"_"+solver+typeLabel);
						runingInfo.clear();
					}//thresholds
				}//caches
			}//solver
		}//cluster

	}
	
	
	
	/**
	 * Reads the experiments parameters from the specified file
	 * @param fileName
	 */
	private static void initializeExperimentParameters(String fileName)
	{
		List<String> rawParameters = readFromFile(fileName); // read parameters' file
		String split[];
		for (String rawParameter : rawParameters) {
			if(rawParameter.toLowerCase().startsWith("thresholds"))
			{
				split = rawParameter.split(":");
				for(int i=1;i<split.length;i++)
					thresholds.add(Double.parseDouble(split[i]));
			}
			else if(rawParameter.toLowerCase().startsWith("capacities")) // set of capacities used by data cache
			{
				split = rawParameter.split(":");
				int  min=Integer.parseInt(split[1]);
				int max = Integer.parseInt(split[2]);
				int factor = Integer.parseInt(split[3]);
				for(int i=min;i<=max;i*=factor)
					capcities.add(i);
			}
			else if(rawParameter.toLowerCase().startsWith("repeats"))// experiment's repetition
			{
				split = rawParameter.split(":");
				repeats = Integer.parseInt(split[1]);

			}
			else if(rawParameter.toLowerCase().startsWith("type")) // Integer, String
			{
				split = rawParameter.split(":");
				//type =  split[1];
				dataType = DataType.valueOf(split[1].toUpperCase());

			}
			else if(rawParameter.toLowerCase().startsWith("data"))
			{
				split = rawParameter.split(":");
				dataFile = currentDirectory +split[1];

			}
			else if(rawParameter.toLowerCase().startsWith("caches")) // caches types
			{
				split = rawParameter.split(":");
				for(int i=1;i<split.length;i++)
					caches.add(split[i]);

			}
			else if(rawParameter.toLowerCase().startsWith("results"))
			{
				split = rawParameter.split(":");
				//resultsFile = split[1];
				resultsFolder = currentDirectory + split[1];
				if(!resultsFolder.endsWith("/"))
					resultsFolder+="/";
			}	
			else if(rawParameter.toLowerCase().startsWith("basefolder"))
			{
				split = rawParameter.split(":");
				baseFolder = currentDirectory+split[1];
				if(!baseFolder.endsWith("/"))
					baseFolder+="/";

			}
			else if(rawParameter.toLowerCase().startsWith("clusters"))
			{
				split = rawParameter.split(":");
				for(int i=1;i<split.length;i++)
					clusters.add(split[i]);

			}
			else if(rawParameter.toLowerCase().startsWith("solvers"))
			{
				split = rawParameter.split(":");
				for(int i=1;i<split.length;i++)
					solvers.add(split[i]);

			}	
			else if(rawParameter.toLowerCase().startsWith("infofolder"))
			{
				split = rawParameter.split(":");
				runsInfoFolder=	currentDirectory + split[1];
				if(!runsInfoFolder.endsWith("/"))
					runsInfoFolder+="/";
			}	
			else if(rawParameter.toLowerCase().startsWith("part")) //base,approach, both
			{
				split = rawParameter.split(":");
				partToRun= RuningPartType.valueOf(split[1].toUpperCase());
			}	
			else if(rawParameter.toLowerCase().startsWith("recordtimes")) // flag to record results or not
			{
				split = rawParameter.split(":");
				recordSectionsRunningTimes=	Boolean.parseBoolean(split[1]);
			}
			else if(rawParameter.toLowerCase().startsWith("optimtime"))
			{
				split = rawParameter.split(":");
				optimTime=	Double.parseDouble(split[1]);
			}
			else if(rawParameter.toLowerCase().startsWith("iterations"))
			{
				split = rawParameter.split(":");
				iterations=	Integer.parseInt(split[1]);
			}

		}
	}
	
	
	public static void displayRunParameters()
	{
		System.out.println("The program will run with parameteres:");
		System.out.println("Data path: "+dataFile);
		System.out.println("Results Folder path: "+resultsFolder);
		System.out.println("Results files prefixes: "+resultsFile);
		System.out.println("Base Foder path: "+baseFolder);
		System.out.println("Folder for steps run times: "+runsInfoFolder);
		System.out.println("Record steps times (y/n): "+recordSectionsRunningTimes);
		System.out.println("Thresholds: "+thresholds);
		System.out.println("Capacities: "+capcities);
		System.out.println("Clusters: "+clusters);
		System.out.println("Solvers: "+solvers);
		System.out.println("Optimization Time: "+optimTime);
		System.out.println("Optimization Time: "+iterations);
		System.out.println("Caches: "+caches);
		System.out.println("Repeats number: "+repeats);
		System.out.println("Integer/String(HR3,Trigram): "+dataType);
		System.out.println("Run base/approach/both: "+partToRun);
	}
	
	private static List<String> extractResults(String folder, int col)//2 run times, 3 hits, 4 misses
	{
		List<String> files =getAllFiles(folder);// get list of files in the folder
		List<String> results = new ArrayList<String>();//
		String cacheName="";
		boolean firstTime =true;
		if(files!=null)
		{
			List<String> data = new ArrayList<String>();
			for (String file : files) {
				cacheName+=file.substring(file.lastIndexOf("/")+1)+"\t";// extracts the file name is a header
				data = readFromFile(file);// read the data recorded from this file as lines
				if(firstTime)//first time
				{
					results.add(0, "");
					firstTime=false;
				}
				for(int i=1;i< data.size();i++)// for each line
				{
					if(results.size()==i)
						results.add(i, "");
					String l = data.get(i); // get the line
					String value = l.split("\\s+")[col]; // split it and get the required column's data
					String oldValue= results.get(i);
					results.set(i, oldValue+value+"\t");// add it to the results in the same position concatenated with other results from previous files
				}
			}
			for(int i=0;i< data.size();i++)
			{
				results.set(i, results.get(i)+"\n");
			}
		}
		results.set(0, cacheName+"\n");

		return results;
	}
	private static void initializeFilesNames()
	{
		resultsFiles.put(2, "baselineRunTimes");
		resultsFiles.put(3, "baselineHits");
		resultsFiles.put(4, "baselineMisses");
		resultsFiles.put(6, "approachRunTimes");
		resultsFiles.put(7, "approachHits");
		resultsFiles.put(8, "approachMisses");
	}
	private static List<String> getAllFiles(String folderPath)
	{
		List<String> files= new ArrayList<String>();
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains("Cache")) {
				//	        System.out.println("File " + listOfFiles[i]);
				try {
					files.add(listOfFiles[i].getCanonicalPath().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return files;
	}
	
	
	
	//put folder path in standard form by ensuring it ends with / 
	public static String standardizePath(String originalPath)
	{
		if(!originalPath.endsWith("/"))
			originalPath+="/";
		return originalPath;
	}
	
	private static List<String> readFromFile(String fileName)
	{
		List<String> lines = new ArrayList<String>();
		BufferedReader bufferedReader=null;
		try {
			bufferedReader = new BufferedReader(new FileReader(currentDirectory+fileName));
			String line ="";
			while((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}   
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");                
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");                  
			// Or we could just do this: 
			// ex.printStackTrace();
		}
		finally{try {
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}         
		}		return lines;
	}
	private static void wrtieToFile(List<String> results,String fileName)
	{
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(fileName));

			for (String line : results) {
				bufferedWriter.write(line);
			}
		}
		catch(IOException ex) {
			System.out.println(
					"Error writing to file '"
							+ fileName + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
		finally{try {
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
	}
	public static int getLineNumber() {
	    return Thread.currentThread().getStackTrace()[2].getLineNumber();
	}
}
