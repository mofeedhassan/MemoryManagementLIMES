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
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.NaiveClustering;
import de.uni_leipzig.simba.memorymanagement.Index.planner.DataManipulationCommand;
import de.uni_leipzig.simba.memorymanagement.Index.planner.TSPPlanner;
import de.uni_leipzig.simba.memorymanagement.Index.planner.TSPSolver;
import de.uni_leipzig.simba.memorymanagement.Index.planner.execution.CacheAccessExecution;
//import de.uni_leipzig.simba.memorymanagement.TSPSolver.GreedySolver;
import de.uni_leipzig.simba.memorymanagement.datacache.AbstractCache;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCache;
import de.uni_leipzig.simba.memorymanagement.datacache.DataCacheFactory;
import de.uni_leipzig.simba.memorymanagement.datacache.SimpleCache;
import de.uni_leipzig.simba.memorymanagement.indexing.Hr3Indexer;
import de.uni_leipzig.simba.memorymanagement.indexing.TrigramIndexItem;
import de.uni_leipzig.simba.memorymanagement.indexing.TrigramIndexer;
import de.uni_leipzig.simba.memorymanagement.pathfinder.PathFinder;
import de.uni_leipzig.simba.memorymanagement.pathfinder.SimpleSolver;
import de.uni_leipzig.simba.memorymanagement.pathfinder.SolverFactory;
import de.uni_leipzig.simba.memorymanagement.structure.CacheType;
import de.uni_leipzig.simba.memorymanagement.structure.ClusteringType;
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


	static List<Double> thresholds= new ArrayList<Double>();
	static List<Integer> capcities= new ArrayList<Integer>();
	static List<String> caches= new ArrayList<String>();
	static List<String> clusters= new ArrayList<String>();
	static List<String> solvers= new ArrayList<String>();

	static List<String> runingInfo= new ArrayList<String>();// storing running times for each section of code: indexer, clustering, TSP, caching
	static String InfoPiece="";//piece of run information

	static int repeats=5;// number of repetitions
	static String type= "integer";// (integer,string) = > (HR3,Trigrams)
	static String dataFile="";// the file's path contains the data
	static String resultsFile="";//the results' fiels naming
	static String resultsFolder="";// the results of the run
	static String runsInfoFolder="";//where run information data are recorded in it
	static String resultsFinalFolder="";//for extracting the required columns for plotting
	static int whatToRun=0;// which part to run (2,1,0)=>(approach,baseline,both)
	static int targetCol=1;
	static double optimTime=100;


	static boolean recordTimes =false;
	static boolean displayOnce =true;

	static String baseFolder="";
	static int alpha=4;
	static int iterations =0;
	static Map<Integer,String> resultsFiles= new HashMap<Integer,String> ();
	static String currentDirectory="";

/*	run
	/media/mofeed/A0621C46621C24164/CachingTests/parameters100
	/media/mofeed/A0621C46621C24164/CachingTests/resultsHR3100
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
        
		
		currentDirectory = System.getProperty("user.dir");
		currentDirectory = standardizePath(currentDirectory);
		resultsFolder = runsInfoFolder = resultsFinalFolder = currentDirectory;
		
		//resultsFolder = runsInfoFolder = resultsFinalFolder = System.getProperty("user.dir");
		resultsFile = resultsFolder+"/Cache";

		String option  = args[0];
		if(option.equals("run"))
		{
			if(args.length < 2)
			{
				System.out.println("parameters for run are: [1] path to parameters file \n [2] path to results folder \n");
				System.exit(1);
			}
			System.out.println("Starting run");
			initializeExperimentParameters(args[1]);
			resultsFolder = standardizePath(resultsFolder);
			resultsFile=resultsFolder+"Cache";
			displayRunParameters();
			TSPCachingTester tsp = new TSPCachingTester();
			tsp.runExperiemment();
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
/*	public static void main(String args[]) {
		Logger.getLogger("ac.biu.nlp.nlp.engineml").setLevel(Level.OFF);
		Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.OFF);
		Logger.getRootLogger().setLevel(Level.OFF);
		resultsFolder = runsInfoFolder = resultsFinalFolder = System.getProperty("user.dir");
		resultsFile = resultsFolder+"/Cache";

		String option  = args[0];
		if(option.equals("run"))
		{
			if(args.length < 2)
			{
				System.out.println("parameters for run are: [1] path to parameters file \n [2] path to results folder \n");
				System.exit(1);
			}
			System.out.println("Starting run");
			initializeExperimentParameters(args[1]);
			resultsFolder = standardizePath(resultsFolder);
			resultsFile=resultsFolder+"Cache";
			displayRunParameters();
			runExperiemment();
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
			List<String> res = extractResults("/media/mofeed/A0621C46621C24164/03_Work/CachingProject/Experiement/testResults/ServerTestHR3/8"resultsFolder, targetCol);
			for (String lines : res) {
				System.out.println(lines);
			}
			initializeFilesNames();
			wrtieToFile(res, resultsFinalFolder+"/"+resultsFiles.get(targetCol));
		}
		else
			System.out.println("Wrong operation option (run/exract)");
	}*/
	public static void displayRunParameters()
	{
		System.out.println("The program will run with parameteres:");
		System.out.println("Data path: "+dataFile);
		System.out.println("Results Folder path: "+resultsFolder);
		System.out.println("Results files prefixes: "+resultsFile);
		System.out.println("Base Foder path: "+baseFolder);
		System.out.println("Folder for steps run times: "+runsInfoFolder);
		System.out.println("Record steps times (y/n): "+recordTimes);
		System.out.println("Thresholds: "+thresholds);
		System.out.println("Capacities: "+capcities);
		System.out.println("Clusters: "+clusters);
		System.out.println("Solvers: "+solvers);
		System.out.println("Optimization Time: "+optimTime);
		System.out.println("Optimization Time: "+iterations);
		System.out.println("Caches: "+caches);
		System.out.println("Repeats number: "+repeats);
		System.out.println("Integer/String(HR3,Trigram): "+type);
		System.out.println("Run base/approach/both(1,2,0): "+whatToRun);
	}
	public static String standardizePath(String originalPath)
	{
		if(!originalPath.endsWith("/"))
			originalPath+="/";
		return originalPath;
	}
	private /*static*/ void runExperiemment()
	{
		long begin=0,end=0,InfoBegin=0;
		List<DataManipulationCommand> plan=null;
		DataCache dataCache=null;
		CacheAccessExecution cae=null;
		TSPPlanner planner =null;
		Graph g=null;
		String label="";
		String commonInfo="";
		String typeLabel="";
		if(type.equals("integer"))
			typeLabel="HR3";
		else
			typeLabel="Tri";
		for(String cluster:clusters){ //pick a cluster
			for(String solver:solvers){ //pick a solver
				for(String cache:caches){ //pick a cache
					List<String> results= null; // intitalize the results list
					String cache_type=null;
					cache_type = cache;
					for (Double threshold : thresholds) {//pick a threshold
						results= new ArrayList<String>(); 
						results.add("Capacity\tRuntime\tHits\tMisses\n");
	//					System.out.println("Capacity\tRuntime\tHits\tMisses");
						String titles="What\tCapacity\tIndexing\tGraph\tClustering\tClustersNo\tPath\tPlan\tMappings\tPlanExec\n";
						runingInfo.add(titles);
						for (Integer capacity : capcities) {// pick a capacity
							String line="";
							for(int i=0; i< repeats; i++)// do the repeats
							{
								InfoPiece="";
								commonInfo=capacity+"\t";
								if(type.equals("integer"))
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
									if(whatToRun == 1 || whatToRun == 0)//1:base , 0: both
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
										if(displayOnce)
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
										if(whatToRun == 1)
											line+= "\n";
										else//0
											line+= "\t";
									}
									////////////////////////////------------Approach-------------------------------////////////////////////////
									if(whatToRun == 2 || whatToRun == 0)//2:approach , 0: both
									{
										if(whatToRun == 0)
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
										if(displayOnce)
											System.out.println(InfoPiece);

										///create path
										InfoBegin = System.currentTimeMillis();

										/*					                TSPSolver tsp = new TSPSolver();
					                int[] path = tsp.getPath(tsp.getMatrix(clusters), iterations);*/

										/// execute the TSP greedy solver
										/*   GreedySolver gs = new GreedySolver();
					                List<Integer> pathList = gs.getPath(clusters);
					                int[] path = new int[pathList.size()];
					                for (int x = 0; x < pathList.size(); x++) {
					                    path[x] = pathList.get(x);
					                }*/
/*										SimpleSolver.optimizationTime = optimTime;
										PathFinder gs = new SimpleSolver();
										List<Integer> pathList = gs.getPath(clusters);
										int[] path = new int[pathList.size()];
										for (int k = 0; k < pathList.size(); k++) {
											path[k] = pathList.get(k);
										}*/
										
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
										if(displayOnce)
										{
											System.out.println(titles);
											System.out.println(InfoPiece);
										}
										runingInfo.add(InfoPiece);
										InfoPiece="";
										/////////////////////overall info
										if(whatToRun == 2)
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
								else if(type.equals("string"))
								{
									TrigramIndexer tix = new TrigramIndexer(threshold);
									tix.endColumn = 2;
									tix.baseFolder=baseFolder;

									///////start indexing
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
									if(whatToRun == 1 || whatToRun == 0)//1:base , 0: both
									{
										//start baseline
										/// start creating plan with load,flush,get
										begin = System.currentTimeMillis();
										plan = planner.plan(g);
										InfoPiece="BaseLine:/" +commonInfo +"\tNA\tNA\tNA\t"+(System.currentTimeMillis()-begin)+"\t";
										if(displayOnce)
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
										if(displayOnce)
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
										if(whatToRun == 1)
											line+= "\n";
										else//0
											line+= "\t";
									}
									////////////////////////////------------Approach-------------------------------////////////////////////////
									if(whatToRun == 2 || whatToRun == 0)//2:approach , 0: both
									{
										if(whatToRun == 0)
											InfoPiece="";
										begin = System.currentTimeMillis();
										iterations = 0;
										/// create clustering method
										Clustering gc = ClusteringFactory.createClustering(ClusteringType.valueOf(cluster));

										///start graph clustering
										InfoBegin = System.currentTimeMillis();
										Map<Integer, Cluster> clusters = gc.cluster(g, capacity);
										InfoPiece="Approach\t" +commonInfo +(System.currentTimeMillis()-InfoBegin)+"\t"+clusters.size()+"\t";
										///create path
										InfoBegin = System.currentTimeMillis();

										PathFinder theSolver = SolverFactory.createSolver(SolverType.valueOf(solver));// get solver
										SimpleSolver.optimizationTime = optimTime;// incase it is simple solver otherwise it will take the default
										TSPSolver.iterations = iterations;// in case it is TSP solver, otherwise the default is set inside the class- To change you need to do that from code
										
										int[] path = theSolver.getPath(clusters);
/*										TSPSolver tsp = new TSPSolver();
						                int[] path = tsp.getPath(tsp.getMatrix(clusters), iterations);*/

										/*					                
										GreedySolver gs = new GreedySolver();
						                List<Integer> pathList = gs.getPath(clusters);
						                int[] path = new int[pathList.size()];
						                for (int x = 0; x < pathList.size(); x++) {
						                    path[x] = pathList.get(x);
						                }*/

/*										SimpleSolver.optimizationTime = optimTime;
										PathFinder gs = new SimpleSolver();
										List<Integer> pathList = gs.getPath(clusters);
										int[] path = new int[pathList.size()];
										for (int k = 0; k < pathList.size(); k++) {
											path[k] = pathList.get(k);
										}*/

										InfoPiece+=(System.currentTimeMillis()-InfoBegin)+"\t";

										/// create and execution plan
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
										if(displayOnce)
										{
											System.out.println(titles);
											System.out.println(InfoPiece);
										}										
										runingInfo.add(InfoPiece);
										InfoPiece="";

										/////////////////////overall info
										if(whatToRun == 2)
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
								displayOnce =false;
							}//repeats
							//results.add(line);


						}//capacities
						System.out.println("================================================================");
						System.out.println(resultsFile+"_"+threshold+"_"+cache_type+"_"+cluster);

						if(recordTimes)
							wrtieToFile(runingInfo,runsInfoFolder+typeLabel+"_"+threshold+"_"+cache_type+"_"+cluster+"_"+solver);
						wrtieToFile(results,resultsFile+"_"+threshold+"_"+cache_type+"_"+cluster+"_"+solver+typeLabel);
						runingInfo.clear();
					}//thresholds
				}//caches
			}//solver
		}//cluster

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
	private static void initializeExperimentParameters(String fileName)
	{
		List<String> rawParameters = readFromFile(fileName);
		String split[];
		for (String rawParameter : rawParameters) {
			if(rawParameter.toLowerCase().startsWith("thresholds"))
			{
				split = rawParameter.split(":");
				for(int i=1;i<split.length;i++)
					thresholds.add(Double.parseDouble(split[i]));
			}
			else if(rawParameter.toLowerCase().startsWith("capacities"))
			{
				split = rawParameter.split(":");
				int  min=Integer.parseInt(split[1]);
				int max = Integer.parseInt(split[2]);
				int factor = Integer.parseInt(split[3]);
				for(int i=min;i<=max;i*=factor)
					capcities.add(i);
			}
			else if(rawParameter.toLowerCase().startsWith("repeats"))
			{
				split = rawParameter.split(":");
				repeats = Integer.parseInt(split[1]);

			}
			else if(rawParameter.toLowerCase().startsWith("type"))
			{
				split = rawParameter.split(":");
				type = split[1];

			}
			else if(rawParameter.toLowerCase().startsWith("data"))
			{
				split = rawParameter.split(":");
				dataFile = currentDirectory +split[1];

			}
			else if(rawParameter.toLowerCase().startsWith("caches"))
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
			else if(rawParameter.toLowerCase().startsWith("part"))
			{
				split = rawParameter.split(":");
				whatToRun= Integer.parseInt(split[1]);
			}	
			else if(rawParameter.toLowerCase().startsWith("recordtimes"))
			{
				split = rawParameter.split(":");
				recordTimes=	Boolean.parseBoolean(split[1]);
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
	private static void displayParameters()
	{
		for (Integer capacity : capcities) {
			System.out.println(capacity);
		}
		for (Double threshold : thresholds) {
			System.out.println(threshold);
		}
		System.out.println(repeats);
		System.out.println(type);
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
	private static void wrtieToFile2(List<String> results,String fileName)
	{
		try{
			//	String data = " This content will append to the end of the file";

			File file =new File(fileName);

			//if file doesnt exists, then create it
			if(!file.exists()){
				file.createNewFile();
			}

			//true = append file
			FileWriter fileWritter = new FileWriter(file.getName(),true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			for (String data : results) {
				bufferWritter.write(data);
			}
			bufferWritter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public static int getLineNumber() {
	    return Thread.currentThread().getStackTrace()[2].getLineNumber();
	}
}
