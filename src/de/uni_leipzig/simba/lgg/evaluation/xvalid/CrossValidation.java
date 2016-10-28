package de.uni_leipzig.simba.lgg.evaluation.xvalid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgap.InvalidConfigurationException;
import org.jgap.gp.GPProblem;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.impl.GPPopulation;
import org.jgap.gp.impl.ProgramChromosome;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.evaluation.PRFCalculator;
import de.uni_leipzig.simba.filter.LinearFilter;
import de.uni_leipzig.simba.genetics.core.ExpressionFitnessFunction;
import de.uni_leipzig.simba.genetics.core.ExpressionProblem;
import de.uni_leipzig.simba.genetics.core.IFitnessFunction;
import de.uni_leipzig.simba.genetics.core.LinkSpecGeneticLearnerConfig;
import de.uni_leipzig.simba.genetics.core.Metric;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser;
import de.uni_leipzig.simba.genetics.evaluation.basics.EvaluationData;
import de.uni_leipzig.simba.genetics.evaluation.pseudomeasures.EvaluationPseudoMemory;
import de.uni_leipzig.simba.genetics.util.PropertyMapping;
import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.lgg.CompleteWombat;
import de.uni_leipzig.simba.lgg.Wombat;
import de.uni_leipzig.simba.lgg.WombatFactory;
import de.uni_leipzig.simba.lgg.refinementOperator.RefinementNode;
import de.uni_leipzig.simba.mapper.SetConstraintsMapper;
import de.uni_leipzig.simba.mapper.SetConstraintsMapperFactory;
import de.uni_leipzig.simba.multilinker.MappingMath;
import de.uni_leipzig.simba.selfconfig.ComplexClassifier;
import de.uni_leipzig.simba.selfconfig.DisjunctiveMeshSelfConfigurator;
import de.uni_leipzig.simba.selfconfig.LinearMeshSelfConfigurator;
import de.uni_leipzig.simba.selfconfig.MeshBasedSelfConfigurator;
import de.uni_leipzig.simba.selfconfig.PseudoMeasures;
import de.uni_leipzig.simba.selfconfig.SimpleClassifier;
import de.uni_leipzig.simba.ukulele.EvaluationParameter;
import de.uni_leipzig.simba.util.Clock;

public class CrossValidation {
	private static final int EUCLID_ITERATIONS = 20;
	private static final Logger logger = Logger.getLogger(CrossValidation.class.getName());
	private static final int FOLDS_COUNT = 10;
	protected static final double MIN_COVERAGE = 0.6d;
	private static String resultStr = new String();
	private List<FoldData> folds = new ArrayList<>();

	//	public GPFitnessFunction fitness; //FIXME UnsupFitness extends Express.
	static double bestEverFitness = Double.MAX_VALUE;
	static IGPProgram bestEver;
	static Clock clock;
	//	LinkedList<EvaluationPseudoMemory> perRunAndDataSet;
//	static RunSolution solution;


	List<FoldData> generateFolds(String d){
		folds = new ArrayList<>();
		EvaluationData data = DataSetChooser.getData(d);

		//Fill caches
		Cache source = data.getSourceCache();
		Cache target = data.getTargetCache();
		Mapping refMap = data.getReferenceMapping();

		// remove error mappings (if any)
		int refMapSize = refMap.size();
		refMap = removeLinksWithNoInstances(refMap, source, target);
		logger.info("Number of removed error mappings = " + (refMapSize - refMap.size()));

		// generate Mapping folds
		List<Mapping> foldMaps = generateMappingFolds(refMap);

		// fill fold caches
		for(Mapping foldMap : foldMaps){
			Cache sourceFoldCache = new HybridCache();
			Cache targetFoldCache = new HybridCache();
			for (String s : foldMap.map.keySet()) {
				if(source.containsUri(s)){
					sourceFoldCache.addInstance(source.getInstance(s));
					for (String t : foldMap.map.get(s).keySet()) {
						if(target.containsUri(t)){
							targetFoldCache.addInstance(target.getInstance(t));
						}else{	
							logger.warn("Instance " + t + " not exist in the target dataset");
						}
					}
				}else{
					logger.warn("Instance " + s + " not exist in the source dataset");
				}
			}
			folds.add(new FoldData(foldMap, sourceFoldCache, targetFoldCache));
		}
		return folds;
	}

	private String crossValidate(String d, String type) {
		fillHeader(d, type);
		folds = generateFolds(d);
		for(int k = 0 ; k < FOLDS_COUNT ; k++){
			FoldData trainData = new FoldData();
			FoldData testData = folds.get(k);
			for(int i = 0 ; i < FOLDS_COUNT ; i++){
				if(i != k){
					trainData.map = trainData.map.union(folds.get(i).map);
					trainData.sourceCache = trainData.sourceCache.union(folds.get(i).sourceCache);
					trainData.targetCache = trainData.targetCache.union(folds.get(i).targetCache);
				}
			}
			trainData.map.initReversedMap();
			testData.map.initReversedMap();
			
			if(type.equalsIgnoreCase("simple")){
				RefinementNode.saveMapping = true;
				crossValidateSimple(trainData, testData);
			}else if(type.equalsIgnoreCase("complete")){
				RefinementNode.saveMapping = false;
				crossValidateComplete(trainData, testData);
			}else if(type.toLowerCase().endsWith("euclid")){
				crossValidateEuclid(trainData, testData, type);
			}else if(type.equalsIgnoreCase("eagle")){
				try {
					crossValidateEagle(trainData, testData, DataSetChooser.getData(d));
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
			System.out.println("-------------- Results so far --------------\n" + resultStr);
		}
		return resultStr;
	}

	private void fillHeader(String d, String type) {
		if(type.equalsIgnoreCase("simple")){
			logger.info("Cross validating WOMBAT Simple for the " + d + " dataset.");
			resultStr += FOLDS_COUNT + " Fold cross validation results for Wombat Simple: " + d +"\n" +
					"lP\tlR\tlF\tlTime\tMetricExpr\tP\tR\tF\tTime\n";
		}else if(type.equalsIgnoreCase("complete")){
			logger.info("Cross validating WOMBAT Complete for the " + d + " dataset.");
			resultStr += FOLDS_COUNT + " Fold cross validation results for Wombat Complete: " + d +"\n" +
					"lP\tlR\tlF\tlT\tMetricExpr\tP\tR\tF\tT\tpnN\tpnT\n";
		}else if(type.toLowerCase().endsWith("euclid")){
			logger.info("Cross validating EUCLID for the " + d + " dataset.");
			resultStr += FOLDS_COUNT + " Fold cross validation results for " + type + " : " + d +"\n" +
					"lP\tlR\tlF\tlTime\tMetricExpr\tP\tR\tF\tTime\n";
		}else if(type.equalsIgnoreCase("eagle")){
			logger.info("Cross validating EAGLE for the " + d + " dataset.");
			resultStr += FOLDS_COUNT + " Fold cross validation results for " + type + " : " + d +"\n" +
					"lP\tlR\tlF\tlTime\tMetricExpr\tP\tR\tF\tTime\n";
		}else{
			logger.error(type +" is not implemented!");
			System.exit(1);
		}
	}


	/**
	 * @param trainData
	 * @param testData
	 * @return
	 */
	private static String crossValidateSimple(FoldData trainData, FoldData testData) {
		trainData.map.initReversedMap();
		testData.map.initReversedMap();

		// 1. Learning phase
		long start = System.currentTimeMillis();
		Wombat lgg = WombatFactory.createOperator( "Simple", trainData.sourceCache, trainData.targetCache, trainData.map, MIN_COVERAGE);
		Mapping learnedMap = lgg.getMapping();
		learnedMap.initReversedMap();

		String metricExpr = lgg.getMetricExpression();
		resultStr += PRFCalculator.precision(learnedMap, trainData.map)	+ "\t" + 
				PRFCalculator.recall(learnedMap, trainData.map) 	+ "\t" + 
				PRFCalculator.fScore(learnedMap, trainData.map) 	+ "\t" +
				(System.currentTimeMillis() - start) 				+ "\t" +
				metricExpr 					+ "\t" ;

		// Test phase
		start = System.currentTimeMillis();
		Mapping learnedTestMap = computeMapping(testData.sourceCache, testData.targetCache, metricExpr);
		long durationMS = System.currentTimeMillis() - start;

		resultStr += PRFCalculator.precision(learnedTestMap, testData.map)	+ "\t" + 
				PRFCalculator.recall(learnedTestMap, testData.map) 	 		+ "\t" + 
				PRFCalculator.fScore(learnedTestMap, testData.map) 	 		+ "\t" +
				durationMS							 						+ "\n" ;
		return resultStr;
	}


	private static String crossValidateEuclid(FoldData trainData, FoldData testData, String euclideType) {
		// Training phase
		long begin = System.currentTimeMillis();
		MeshBasedSelfConfigurator lsc = null;
		if (euclideType.toLowerCase().startsWith("l")) {
			lsc = new LinearMeshSelfConfigurator(trainData.sourceCache, trainData.targetCache, MIN_COVERAGE, 1d);
		} else if (euclideType.toLowerCase().startsWith("d")) {
			lsc = new DisjunctiveMeshSelfConfigurator(trainData.sourceCache, trainData.targetCache, MIN_COVERAGE, 1d);
		} else {
			lsc = new MeshBasedSelfConfigurator(trainData.sourceCache, trainData.targetCache, MIN_COVERAGE, 1d);
		}
		logger.info("Running " + euclideType);
		lsc.setMeasure(new PseudoMeasures());
		logger.info("Computing simple classifiers...");
		List<SimpleClassifier> cp = lsc.getBestInitialClassifiers();
		long middle = System.currentTimeMillis();
		logger.info(cp.size()+" simple classifiers computed in "+(middle-begin)+" ms. Computing complex classifier for " + EUCLID_ITERATIONS + " iterations...");
		ComplexClassifier cc = lsc.getZoomedHillTop(5, EUCLID_ITERATIONS, cp);
		long durationMS = System.currentTimeMillis() - begin;
		Mapping learnedMap = cc.mapping;
		learnedMap.initReversedMap();
		String metricExpr = cc.toString();

		resultStr += PRFCalculator.precision(learnedMap, trainData.map)	+ "\t" + 
				PRFCalculator.recall(learnedMap, trainData.map) 		+ "\t" + 
				PRFCalculator.fScore(learnedMap, trainData.map) 		+ "\t" +
				durationMS 												+ "\t" +
				metricExpr 												+ "\t" ;

		// Test phase
		lsc.setSource(testData.sourceCache);
		lsc.setTarget(testData.targetCache);
		begin = System.currentTimeMillis();
		Mapping learnedTestMap = lsc.getMapping(cc.classifiers);
		durationMS = System.currentTimeMillis() - begin;
		resultStr += PRFCalculator.precision(learnedTestMap, testData.map)	+ "\t" + 
				PRFCalculator.recall(learnedTestMap, testData.map) 	 		+ "\t" + 
				PRFCalculator.fScore(learnedTestMap, testData.map) 	 		+ "\t" +
				durationMS							 						+ "\n" ;
		return resultStr;
	}

	private static String crossValidateComplete(FoldData trainData, FoldData testData) {
		trainData.map.initReversedMap();
		testData.map.initReversedMap();

		// Training phase
		long start = System.currentTimeMillis();
		CompleteWombat lgg = new CompleteWombat(trainData.sourceCache, trainData.targetCache, trainData.map, MIN_COVERAGE);
		Mapping learnedMap = lgg.getMapping();
		learnedMap.initReversedMap();

		String metricExpr = lgg.getMetricExpression();
		resultStr += PRFCalculator.precision(learnedMap, trainData.map)	+ "\t" + 
				PRFCalculator.recall(learnedMap, trainData.map) 		+ "\t" + 
				PRFCalculator.fScore(learnedMap, trainData.map) 		+ "\t" +
				(System.currentTimeMillis() - start) 					+ "\t" +
				metricExpr 					+ "\t" ;

		// Test phase
		start = System.currentTimeMillis();
		Mapping learnedTestMap = computeMapping(testData.sourceCache, testData.targetCache, metricExpr);
		long durationMS = (System.currentTimeMillis() - start);
		resultStr += PRFCalculator.precision(learnedTestMap, testData.map)	+ "\t" + 
				PRFCalculator.recall(learnedTestMap, testData.map) 	 	+ "\t" + 
				PRFCalculator.fScore(learnedTestMap, testData.map) 	 	+ "\t" +
				durationMS							 					+ "\t" +
				lgg.pruneNodeCount 										+ "\t" + 	
				lgg.pruningTime 										+ "\n";
		return resultStr;
	}

	public static Mapping computeMapping(Cache sourceCache, Cache targetCache, String metricExpr){
		SetConstraintsMapper mapper = SetConstraintsMapperFactory.getMapper("simple",
				new KBInfo("?x"), new KBInfo("?y"), sourceCache, targetCache, new LinearFilter(), 2);
		String expression = metricExpr.substring(0, metricExpr.lastIndexOf("|"));
		Double threshold = Double.parseDouble(metricExpr.substring(metricExpr.lastIndexOf("|")+1, metricExpr.length()));
		return mapper.getLinks(expression, threshold);
	}


	/**
	 * Computes the best of this generation, if it's better then the global best log it
	 * @param gp
	 * @param run
	 * @param gen
	 * @return current best program
	 */
	protected static IGPProgram processGeneration(GPGenotype gp, int run, int gen, IFitnessFunction fitness) {
		GPPopulation pop = gp.getGPPopulation();
		pop.sortByFitness();

		IGPProgram bests[] = { gp.getFittestProgramComputed(),
				pop.determineFittestProgram(),
				gp.getAllTimeBest(),
				pop.getGPProgram(0),
		};
		IGPProgram bestHere = null;
		double fittest = Double.MAX_VALUE;
		for(IGPProgram p : bests) {
			if(p != null) {
				double fitM =	fitness.calculateRawFitness(p);
				if(fitM<fittest) {
					fittest = fitM;
					bestHere = (IGPProgram) p.clone();
				}
			}
		}
		// if we reached a new better solution
		if(fittest<bestEverFitness) {
			bestEverFitness = fittest;
			gp.getGPPopulation().addFittestProgram(bestEver);
			bestEver = bestHere;
			double pfm = fitness.calculateRawMeasure(bestHere);
			EvaluationPseudoMemory mem = new EvaluationPseudoMemory(run, gen, getMetric(bestHere),
					fittest, pfm, clock.totalDuration());
			System.out.println("New best gen="+gen+" : "+fittest+" => "+mem.metric);
//			solution.perRunAndDataSet.add(mem);
			return bestHere;
		}
		if(bestEver == null) { // for initial generation
			bestEver = bestHere;
		}
		return bestHere;
	}



	/**
	 * divide the input refMap to foldCount foldMaps
	 * Note: refMap will be empty at the end
	 * 
	 * @param refMap
	 * @param foldCount
	 * @return
	 */
	public static List<Mapping> generateMappingFolds(Mapping refMap) {
		List<Mapping> foldMaps = new ArrayList<>();
		int mapSize = refMap.map.keySet().size();
		int foldSize = (int) (mapSize / FOLDS_COUNT);

		for(int foldIndex = 0 ; foldIndex < FOLDS_COUNT ; foldIndex++){
			Set<Integer> index = new HashSet<>();
			//get random indexes
			while (index.size() < foldSize) {
				int number;
				do {
					number = (int) (refMap.map.keySet().size() * Math.random());
				} while (index.contains(number));
				index.add(number);
			}
			//get data
			Mapping foldMap = new Mapping();
			int count = 0;
			for (String key : refMap.map.keySet()) {
				if (index.contains(count)) {
					foldMap.map.put(key, refMap.map.get(key));
				}
				count++;
			}

			// compute fold size
			for (String key : foldMap.map.keySet()) {
				for (@SuppressWarnings("unused") String value : foldMap.map.get(key).keySet()) {
					foldMap.size++;
				}
			}
			foldMaps.add(foldMap);
			refMap = MappingMath.removeSubMap(refMap, foldMap);
		}
		int i = 0;
		// if any remaining links in the refMap, then distribute them to all folds
		for (String key : refMap.map.keySet()) {
			foldMaps.get(i).map.put(key, refMap.map.get(key));
			foldMaps.get(i).size++;
			i = (i + 1) % FOLDS_COUNT; 
		}
		return foldMaps;
	}


	protected static Mapping removeLinksWithNoInstances(Mapping map, Cache source, Cache target) {
		Mapping result = new Mapping();
		for (String s : map.map.keySet()) {
			for (String t : map.map.get(s).keySet()) {
				if(source.containsUri(s) && target.containsUri(t)){
					result.add(s,t, map.map.get(s).get(t));
				}
			}
		}
		return result;
	}


	public static void main(String[] args) {
		CrossValidation cv = new CrossValidation();
		System.out.println("------------------------- RESULTS -------------------------");
		System.out.println(cv.crossValidate(args[0], args[1]));
	}


	public static String crossValidateEagle(FoldData trainData, FoldData testData, EvaluationData data) throws InvalidConfigurationException{ 
		//	Training phase
		EvaluationParameter param = new EvaluationParameter();
		bestEverFitness = Double.MAX_VALUE;
		//build EAGLEs propertyMapping n-to-m based
		PropertyMapping propMap = buildNtoMPropertyMapping(data);
		data.setPropertyMapping(propMap);
		// init EAGLE
		LinkSpecGeneticLearnerConfig config = new LinkSpecGeneticLearnerConfig(data.getConfigReader().sourceInfo, data.getConfigReader().targetInfo, data.getPropertyMapping());
		config.sC = trainData.sourceCache;
		config.tC = trainData.targetCache;
		//		config.setSelectFromPrevGen(param.reproductionProbability);
		config.setCrossoverProb(param.mutationProbability);
		config.setMutationProb(param.mutationProbability);
		config.setReproductionProb(param.reproductionProbability);
		config.setPopulationSize(param.population);
		config.setPreservFittestIndividual(true);
		config.setAlwaysCaculateFitness(true);		
		ExpressionFitnessFunction fitness = ExpressionFitnessFunction.getInstance(config, trainData.map, "f-score", trainData.map.size);
		fitness.setReferenceMapping(trainData.map);
		fitness.useFullCaches(param.useFullCaches);
		config.setFitnessFunction(fitness);
		clock = new Clock();

		GPProblem gpP = new ExpressionProblem(config);
		GPGenotype gp = gpP.create();
		// evolve generations
		for(int gen = 0; gen<param.generations; gen++) {
			gp.evolve();
			// inspect generation: fitter solution?
			processGeneration(gp, 1, gen, fitness);
		}		

		long start = System.currentTimeMillis();
		Metric learnedMetric = getMetric(bestEver);
		Mapping learnedMap = fitness.getMapping(learnedMetric.getExpression(), learnedMetric.getThreshold(), true); // trainingMap
		long durationMS = System.currentTimeMillis() - start;

		resultStr += PRFCalculator.precision(learnedMap, trainData.map)	+ "\t" + 
				PRFCalculator.recall(learnedMap, trainData.map) 		+ "\t" + 
				PRFCalculator.fScore(learnedMap, trainData.map) 		+ "\t" +
				durationMS 												+ "\t" +
				learnedMetric 											+ "\t" ;

		// Test phase
		start = System.currentTimeMillis();
		Mapping learnedTestMap = computeMapping(testData.sourceCache, testData.targetCache, learnedMetric.getExpression()+"|"+learnedMetric.getThreshold());
		durationMS = (System.currentTimeMillis() - start);

		resultStr += PRFCalculator.precision(learnedTestMap, testData.map)	+ "\t" + 
				PRFCalculator.recall(learnedTestMap, testData.map) 	 		+ "\t" + 
				PRFCalculator.fScore(learnedTestMap, testData.map) 	 		+ "\t" +
				durationMS							 						+ "\n" ;

		return resultStr;	
	}

	/**
	 * Generates a n-to-m mapping of all n source Properties and all m target properties.
	 * @param data
	 * @return
	 */
	public static PropertyMapping buildNtoMPropertyMapping(EvaluationData data) {
		PropertyMapping map = new PropertyMapping();
		for(String sp : data.getConfigReader().getSourceInfo().properties) {
			for(String tp : data.getConfigReader().getTargetInfo().properties) {
				map.addStringPropertyMatch(sp, tp);
			}
		}

		return map;
	}

	/**
	 * Generates LIMES' link specification for GP program
	 * @param p
	 * @return
	 */
	protected static Metric getMetric(IGPProgram p) {
		Object[] args = {};
		ProgramChromosome pc = p.getChromosome(0);
		return (Metric) pc.getNode(0).execute_object(pc, 0, args);
	}
}
