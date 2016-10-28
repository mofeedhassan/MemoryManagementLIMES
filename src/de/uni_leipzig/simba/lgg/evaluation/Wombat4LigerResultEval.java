package de.uni_leipzig.simba.lgg.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.evaluation.PRFCalculator;
import de.uni_leipzig.simba.filter.LinearFilter;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser;
import de.uni_leipzig.simba.genetics.evaluation.basics.EvaluationData;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser.DataSets;
import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.learning.oracle.mappingreader.CSVMappingReader;
import de.uni_leipzig.simba.lgg.Wombat;
import de.uni_leipzig.simba.lgg.WombatFactory;
import de.uni_leipzig.simba.lgg.refinementOperator.RefinementNode;
import de.uni_leipzig.simba.mapper.SetConstraintsMapper;
import de.uni_leipzig.simba.mapper.SetConstraintsMapperFactory;
import de.uni_leipzig.simba.measures.pointsets.evaluation.FMeasureRecorder;
import de.uni_leipzig.simba.multilinker.MappingMath;

public class Wombat4LigerResultEval extends LGGEvaluator4BenchmarkDatasets {
	private static final Logger logger = Logger.getLogger(Wombat4LigerResultEval.class.getName());


	public static String run(String fileName, String dataSetName, String lggType){
		// read training data

		Mapping trainingMap = (new CSVMappingReader()).getMapping(fileName);
		trainingMap.initReversedMap();

		//Fill caches
		EvaluationData data = DataSetChooser.getData(toDataset(dataSetName));
		source = data.getSourceCache();
		target = data.getTargetCache();
		reference = data.getReferenceMapping();


		// remove error mappings (if any)
		int refMapSize = reference.size();
		reference = removeLinksWithNoInstances(reference);
		reference.initReversedMap();
		logger.info("Number of removed error mappings = " + (refMapSize - reference.size()));

		// 1. Learning phase
		long start = System.currentTimeMillis();
		Wombat lgg = WombatFactory.createOperator(lggType, source, target, trainingMap, MIN_COVERAGE);
		Mapping learnedMap = lgg.getMapping();
		learnedMap = Mapping.getBestOneToOneMappings(learnedMap);
		learnedMap.initReversedMap();

		//2. Test results
		reference = data.getReferenceMapping();
		String metricExpr = lgg.getMetricExpression();

		String result = 
				PRFCalculator.precision(trainingMap, reference )+ "\t" + 
						PRFCalculator.recall(trainingMap, reference ) 	+ "\t" +
						PRFCalculator.fScore(trainingMap, reference ) 	+ "\t" + 
						PRFCalculator.precision(learnedMap, reference)	+ "\t" + 
						PRFCalculator.recall(learnedMap, reference) 	+ "\t" + 
						PRFCalculator.fScore(learnedMap, reference) 	+ "\t" +
						(System.currentTimeMillis() - start) 			+ "\t" +
						metricExpr 					+ "\n" ;
		return result;
	}

	/**
	 * @param fileName
	 * @throws IOException
	 * @author sherif
	 */
	public static void saveResultToFile(String fileName) throws IOException{
		logger.info("Writting result to " + fileName + " ...");
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
		bw.write(resultStr);
		bw.close();
		logger.info("Done.");
	}


	static List<String> dirs = new ArrayList<>();

	public static void getAllDirectories(File[] files) {
		for (File file : files) {
			if (file.isDirectory()) {
				try {
					dirs.add(file.getCanonicalPath()+"/");
				} catch (IOException e) {
					e.printStackTrace();
				}
				getAllDirectories(file.listFiles()); 
			} 
		}
	}
	
	public static void evaluateLigerResults(String[] args){
		RefinementNode.saveMapping = true;
		if(args == null || args[0] == "?" || args.length < 3){
			System.out.println("posExFolderName datasetName lggType");
			System.exit(1);
		}
		File[] baseDir = new File(args[0]).listFiles();
		getAllDirectories(baseDir);
		if(dirs.isEmpty()){
			dirs.add(args[0]);
		}
		for(String dir : dirs){
			resultStr = new String();
			int j = (args.length > 3) ? Integer.parseInt(args[3]): 100;
			for(int i = 1 ; i <= j; i++){
				String fileName = dir + i + ".txt";
				if (new File(fileName).exists()) {
					logger.info("Processing file: " + fileName);
					resultStr += i + "\t" + run(fileName, args[1], args[2]);
				}
				System.out.println(resultStr);
			}
			//CSV header
			resultStr = "Dataset: " + args[1] + "\tWombat: " + args[2] +"\n\n" + 
					"i\ttP\ttR\ttF\tP\tR\tF\tT\tMetricExpr\n" + resultStr; 
			try {
				saveResultToFile(dir + "wombar_results.csv");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String evaluateBaseLineWombat(DataSets d, double trainingFactor, String lggType) {
		logger.info("Running " + lggType + " WOMBAT for the " + d + " dataset with positive example size = " +  trainingFactor*100 + "%");
		
		EvaluationData data = DataSetChooser.getData(d);

		//Fill caches
		source = data.getSourceCache();
		target = data.getTargetCache();
		reference = data.getReferenceMapping();

		// remove error mappings (if any)
		int refMapSize = reference.size();
		reference = removeLinksWithNoInstances(reference);
		logger.info("Number of removed error mappings = " + (refMapSize - reference.size()));
		
		Mapping trainMap = sampleReferenceMap(reference, trainingFactor/10f);
		Mapping testMap  = MappingMath.removeSubMap(reference, trainMap);
		fillTrainingCaches(trainMap);
		fillTestingCaches(testMap);
		trainMap.initReversedMap();

		// 1. Learning phase
		long start = System.currentTimeMillis();
		Wombat lgg = WombatFactory.createOperator(lggType, sourceTrainCache, targetTrainCache, trainMap, MIN_COVERAGE);
		Mapping learnedMap = lgg.getMapping();
		learnedMap.initReversedMap();

		String metricExpr = lgg.getMetricExpression();
		String result =  //(int) trainingFactor*10 + "%"			+ "\t" + 
				PRFCalculator.precision(learnedMap, trainMap)	+ "\t" + 
				PRFCalculator.recall(learnedMap, trainMap) 		+ "\t" + 
				PRFCalculator.fScore(learnedMap, trainMap) 		+ "\t" +
				(System.currentTimeMillis() - start) 			+ "\t" +
				metricExpr 										+ "\t" ;

		// 2. Apply for the rest of the KB
		SetConstraintsMapper mapper = SetConstraintsMapperFactory.getMapper("simple",
				new KBInfo("?x"), new KBInfo("?y"), sourceTestCache, targetTestCache, new LinearFilter(), 2);
		start = System.currentTimeMillis();
		String expression = metricExpr.substring(0, metricExpr.lastIndexOf("|"));
		Double threshold = Double.parseDouble(metricExpr.substring(metricExpr.lastIndexOf("|")+1, metricExpr.length()));
		Mapping learnedTestMap = mapper.getLinks(expression, threshold);

		result += PRFCalculator.precision(learnedTestMap, testMap)	+ "\t" + 
				PRFCalculator.recall(learnedTestMap, testMap) 	 	+ "\t" + 
				PRFCalculator.fScore(learnedTestMap, testMap)		+ "\t" +
				(System.currentTimeMillis() - start) 				+ "\n" ;
		System.out.println(d + " Results so far:\n" + resultStr);
		return result;
	}	


	
	public static void main(String[] args){
		evaluateLigerResults(args);
//		if(args == null || args[0] == "?" || args.length < 3){
//			System.out.println("dataset trainingFactor wombatType");
//			System.exit(1);
//		}
//		DataSets datasetName = toDataset(args[0]);
//		double trainingFactor = Double.parseDouble(args[1]);
//		String lggType = args[2];
//		for(int i = 1 ; i <= 100 ; i++){
//			resultStr += i + "\t" +evaluateBaseLineWombat(datasetName, trainingFactor, lggType);
//		}
//		resultStr = lggType + "LGG\t" + datasetName +"\n\n" +
//				"i\tlP\tlR\tlF\tlTime\tMetricExpr\tP\tR\tF\tTime\n" + resultStr;
//		try {
//			saveResultToFile("wombar_" + datasetName + "_baseline_results.csv");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
	}
}
