package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.PLTSPPartitioning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Cluster;
import de.uni_leipzig.simba.memorymanagement.Index.graphclustering.Graph;
import de.uni_leipzig.simba.memorymanagement.Index.planner.DataManipulationCommand;
import de.uni_leipzig.simba.memorymanagement.Index.planner.PTSPPlanner;
import de.uni_leipzig.simba.memorymanagement.Index.planner.TSPPlanner;
import de.uni_leipzig.simba.memorymanagement.Index.planner.TSPSolver;
import de.uni_leipzig.simba.memorymanagement.indexing.Hr3Indexer;
import de.uni_leipzig.simba.memorymanagement.io.Write;
import de.uni_leipzig.simba.memorymanagement.lazytsp.LazyTspComponents;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.graphPertitioner;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph;
import de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.primary.MGraph2;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.PLTSP.LTSPMain;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.RefactoredPLTSP.ParallelController;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.RefactoredPLTSP.ParallelMain;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities.ParametersProcessor;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities.PathUtils;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities.ResultsProcessing;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.utilities.ResultsRuntimes;
import de.uni_leipzig.simba.memorymanagement.pathfinder.SimpleSolver;
import de.uni_leipzig.simba.memorymanagement.structure.OperationType;
import de.uni_leipzig.simba.memorymanagement.structure.ParameterType;

public class PLTSPPMain {
	static Logger logger = Logger.getLogger("LIMES"); 

	static List<Double> thresholds= new ArrayList<>();
	static List<Integer> capacities= new ArrayList<>();
	static List<String> caches= new ArrayList<String>();
	static List<String> clusters= new ArrayList<String>();
	static List<String> solvers= new ArrayList<String>();

	static List<String> runningInfo= new ArrayList<String>();// storing running times for each section of code: indexer, clustering, TSP, caching
	static String InfoPiece="";//piece of run information

	static int repeats=5;// number of repetitions
	static String type= "integer";// (integer,string) = > (HR3,Trigrams)
	static String dataFile="";// the file's path contains the data
	static String resultsFile="";//the results' fiels naming
	static String resultsFolder="";// the results of the run
	static String runsInfoFolder="";//where run information data are recorded in it
	static String resultsFinalFolder="";//for extracting the required columns for plotting
	static String currentDirectory="";
	static int whatToRun=0;// which part to run (2,1,0)=>(approach,baseline,both)
	
	static int targetCol=1;
	static double optimTime=100;
	static int numberOfProcessors =Runtime.getRuntime().availableProcessors();

	static boolean recordTimes =false;
	static boolean displayOnce =false;

	static String baseFolder="";
	static int alpha=4;
	static int iterations =0;
	static Map<Integer,String> resultsFiles= new HashMap<Integer,String> ();
	static LinkedHashMap<Integer,List<DataManipulationCommand>> clustersCommands=null;
	
	public static void initializeLogger()
	{
        Handler fileHandler  = null;
        try{
			//Creating consoleHandler and fileHandler
			fileHandler  = new FileHandler("./PLTSP.log");
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
	}
	
	public static void setPathsFoldersDefaults()
	{
		currentDirectory = PathUtils.standardizePath(System.getProperty("user.dir"));
		resultsFolder = runsInfoFolder = resultsFinalFolder = currentDirectory;
	}
	public static LazyTspComponents initializeParameters(ParametersProcessor pp)
	{
	    LazyTspComponents pComponents = pp.setUpLazyTspComponents().creatParameters();
	    
	    dataFile = pp.getParameter(ParameterType.DATA);
		resultsFolder = PathUtils.standardizePath(resultsFolder);
		resultsFile=resultsFolder+"Cache";
		pp.displayRunParameters();
		
		return pComponents;
	}
	   public static void runExperiment2(LazyTspComponents pParameters, ParametersProcessor pp)
	   {
		    ResultsRuntimes rr = new ResultsRuntimes();
		    
		    rr.setExperiementStart(System.currentTimeMillis());
		    
		    Hr3Indexer hr3 = new Hr3Indexer(alpha, pParameters.getThreshold()); // in future make its factory
			hr3.endColumn = 2;
			hr3.baseFolder=pp.getParameter(ParameterType.BASEFOLDER);
			
			///////start indexing
			rr.setPhaseStart(System.currentTimeMillis());
			hr3.runIndexing(new File(dataFile), true);
			rr.setPhaseInterval("indexing", System.currentTimeMillis());
			
			/////// create graph
			rr.setPhaseStart(System.currentTimeMillis());
			Graph g = hr3.generateTaskGraph();
			rr.setPhaseInterval("graph", System.currentTimeMillis());

			/// partition the graph
			List<Graph> graphs = graphPertitioner.partition(g);
			System.out.println(graphs.size());
			LinkedHashMap<Integer,List<DataManipulationCommand>> plans = new LinkedHashMap<>();
			int count=0;
			//each node in the graph contains only an item index
			for (Graph graph : graphs) {
				///create planner object
				iterations = 0; // doese it change so you need to reset
				///start graph clustering
				rr.setPhaseStart(System.currentTimeMillis());
				Map<Integer, Cluster> clusters = pParameters.getClustering().cluster(graph, pParameters.getCapacity()); //--> parameters.clustering.cluster(g,parameters.capacity)
				//each cluster represents a a set of itemindex
			
				rr.setPhaseInterval("clustering", System.currentTimeMillis());
				
				SimpleSolver.optimizationTime = optimTime;// incase it is simple solver otherwise it will take the default
				TSPSolver.iterations = iterations;// in case it is TSP solver, otherwise the default is set inside the class- To change you need to do that from code
				
				//get path
				rr.setPhaseStart(System.currentTimeMillis());
				int[] path = pParameters.getSolver().getPath(clusters);
				rr.setPhaseInterval("path", System.currentTimeMillis());
				
				//get plan
				TSPPlanner planner = new TSPPlanner();

				/// create and execution plan
				rr.setPhaseStart(System.currentTimeMillis());
				List<DataManipulationCommand> Plan = planner.plan(clusters, path);
				plans.put(count++,Plan);
				rr.setPhaseInterval("plan", System.currentTimeMillis());
			}
			
			///////////////////////////// create parallel controller
			
			ParallelController pc = new ParallelController(plans,pParameters.getDataCache(),pParameters.getCapacity(), hr3,pParameters.getMeasure(), pParameters.getThreshold(),true,numberOfProcessors);//-->parameters_ others
			int[] arr = new int [graphs.size()];
			for(int i=0;i<graphs.size();i++)
				arr[i]=i;
			//pc.clustersIds = path; // copy the path sequence to the parallel controller to iterate over it
			pc.clustersIds = arr;
			//Set the cache if it is shared or notmeters
			pc.setCacheSharing(true);
			
			//Run the parallel controller
			rr.setPhaseStart(System.currentTimeMillis());
			pc.runParallelTasks();
			rr.setPhaseInterval("parallel", System.currentTimeMillis());
			rr.setExperiementEnd(System.currentTimeMillis());

			System.out.println(rr.getRunInfo());
			System.out.println(pc.results);
			System.out.println(pc.cacheHitsValues);
			System.out.println(pc.cacheMissesValues);
			long totlaMappings=0;
			for (String runInfo : pc.results) {
				String[] infos = runInfo.split(":");
				totlaMappings+= Integer.valueOf(infos[2]);
			}
			System.out.println("Total Mappings = "+ totlaMappings);
		
	   }
	   
	   public static void runExperiment(LazyTspComponents pParameters, ParametersProcessor pp)
	   {
		    ResultsRuntimes rr = new ResultsRuntimes();
		    
		    rr.setExperiementStart(System.currentTimeMillis());
		    
		    Hr3Indexer hr3 = new Hr3Indexer(alpha, pParameters.getThreshold()); // in future make its factory
			hr3.endColumn = 2;
			hr3.baseFolder=pp.getParameter(ParameterType.BASEFOLDER);
			
			///////start indexing
			rr.setPhaseStart(System.currentTimeMillis());
			hr3.runIndexing(new File(dataFile), true);
			rr.setPhaseInterval("indexing", System.currentTimeMillis());
			
			/////// create graph
			rr.setPhaseStart(System.currentTimeMillis());
			Graph graph = hr3.generateTaskGraph();
			rr.setPhaseInterval("graph", System.currentTimeMillis());

			iterations = 0; // doese it change so you need to reset
			///start graph clustering
			rr.setPhaseStart(System.currentTimeMillis());
			Map<Integer, Cluster> clusters = pParameters.getClustering().cluster(graph, pParameters.getCapacity()); //--> parameters.clustering.cluster(g,parameters.capacity)
			//each cluster represents a mapping task = a set of itemindex
			rr.setPhaseInterval("clustering", System.currentTimeMillis());
			///partition clusters
			//itterate over each cluster
			//create solver
			//get path to each one
			//get plan for the path
			//create a parallel task foreach plan
			/// partition the graph
			List<Graph> graphs = graphPertitioner.partition(graph);
			LinkedHashMap<Integer,List<DataManipulationCommand>> plans = new LinkedHashMap<>();
			int count=0;
			//each node in the graph contains only an item index
			for (Graph g : graphs) {
				///create planner object
				iterations = 0; // doese it change so you need to reset
				///start graph clustering
				rr.setPhaseStart(System.currentTimeMillis());
				//Map<Integer, Cluster> clusters = pParameters.getClustering().cluster(graph, pParameters.getCapacity()); //--> parameters.clustering.cluster(g,parameters.capacity)
				//each cluster represents a a set of itemindex
			
				rr.setPhaseInterval("clustering", System.currentTimeMillis());
				
				SimpleSolver.optimizationTime = optimTime;// incase it is simple solver otherwise it will take the default
				TSPSolver.iterations = iterations;// in case it is TSP solver, otherwise the default is set inside the class- To change you need to do that from code
				
				//get path
				rr.setPhaseStart(System.currentTimeMillis());
				int[] path = pParameters.getSolver().getPath(clusters);
				rr.setPhaseInterval("path", System.currentTimeMillis());
				
				//get plan
				TSPPlanner planner = new TSPPlanner();

				/// create and execution plan
				rr.setPhaseStart(System.currentTimeMillis());
				List<DataManipulationCommand> Plan = planner.plan(clusters, path);
				plans.put(count++,Plan);
				rr.setPhaseInterval("plan", System.currentTimeMillis());
			}
			
			///////////////////////////// create parallel controller
			
			ParallelController pc = new ParallelController(plans,pParameters.getDataCache(),pParameters.getCapacity(), hr3,pParameters.getMeasure(), pParameters.getThreshold(),true,numberOfProcessors);//-->parameters_ others
			int[] arr = new int [graphs.size()];
			for(int i=0;i<graphs.size();i++)
				arr[i]=i;
			//pc.clustersIds = path; // copy the path sequence to the parallel controller to iterate over it
			pc.clustersIds = arr;
			//Set the cache if it is shared or notmeters
			pc.setCacheSharing(true);
			
			//Run the parallel controller
			rr.setPhaseStart(System.currentTimeMillis());
			pc.runParallelTasks();
			rr.setPhaseInterval("parallel", System.currentTimeMillis());
			rr.setExperiementEnd(System.currentTimeMillis());

			System.out.println(rr.getRunInfo());
			System.out.println(pc.results);
			System.out.println(pc.cacheHitsValues);
			System.out.println(pc.cacheMissesValues);
			long totlaMappings=0;
			for (String runInfo : pc.results) {
				String[] infos = runInfo.split(":");
				totlaMappings+= Integer.valueOf(infos[2]);
			}
			System.out.println("Total Mappings = "+ totlaMappings);
		
	   }
	   
	   public static void runExperimentTestCoarsening(LazyTspComponents pParameters, ParametersProcessor pp)
	   {
		    ResultsRuntimes rr = new ResultsRuntimes();
		    
		    rr.setExperiementStart(System.currentTimeMillis());
		    
		    Hr3Indexer hr3 = new Hr3Indexer(alpha, pParameters.getThreshold()); // in future make its factory
			hr3.endColumn = 2;
			hr3.baseFolder=pp.getParameter(ParameterType.BASEFOLDER);
			
			///////start indexing
			rr.setPhaseStart(System.currentTimeMillis());
			hr3.runIndexing(new File(dataFile), true);
			rr.setPhaseInterval("indexing", System.currentTimeMillis());
			
			/////// create graph
			rr.setPhaseStart(System.currentTimeMillis());
			Graph graph = hr3.generateTaskGraph();
			rr.setPhaseInterval("graph", System.currentTimeMillis());

			iterations = 0; // doese it change so you need to reset
			///start graph clustering
			rr.setPhaseStart(System.currentTimeMillis());
			Map<Integer, Cluster> clusters = pParameters.getClustering().cluster(graph, pParameters.getCapacity()); //--> parameters.clustering.cluster(g,parameters.capacity)
			//each cluster represents a mapping task = a set of itemindex
			rr.setPhaseInterval("clustering", System.currentTimeMillis());
			System.out.println("clusters");

			System.out.println(clusters);
			///partition clusters
			MGraph2 g = new MGraph2();
			g.createFineGraph(clusters);
			System.out.println("fine graph");
			g.displayGraph(g.nodeWeights, g.edgesWeights);
			
			System.out.println("coarsened graph");
			g.getCorsenedGraphNodeOrder();
			g.displayGraph(g.coarsenedNodeWeights, g.coarsenedEdgesWeights);
		
	   }
	public static void main(String[] args) {

		initializeLogger();
		setPathsFoldersDefaults();
		String option  = args[0];
		
		if(option.equals(String.valueOf(OperationType.RUN)))
		{
			if(args.length < 2)
			{
				System.out.println("parameters for run are: [1] operation\n [2] path to parameters file  \n");
				System.exit(1);
			}
			
			System.out.println("Extract parameters......");
			
			ParametersProcessor paramters = new ParametersProcessor();
			LazyTspComponents experiementComponents = initializeParameters(paramters);

			PLTSPPMain driver = new PLTSPPMain();
			
			//driver.runExperiment(experiementComponents,paramters);
			driver.runExperimentTestCoarsening(experiementComponents, paramters);
		}
		else if(option.equals(String.valueOf(OperationType.EXT)))
		{
			if(args.length != 4)
			{
				System.out.println("parameters for extract are: [1] path to results folder \n [2] column to extract {base line: 2 time, 3 hits, 4 misses / approach: 6 time, 7 hits, 8 misses} \n [3] path to final extractions");
				System.exit(1);
			}
			System.out.println("Starting data extraction");
			List<String> res = ResultsProcessing.extractResults(args);
			Write.wrtieToFile(res, resultsFinalFolder+"/"+resultsFiles.get(targetCol));
		}
		else
			System.out.println("Wrong operation option (run/extract)");

	}
}
