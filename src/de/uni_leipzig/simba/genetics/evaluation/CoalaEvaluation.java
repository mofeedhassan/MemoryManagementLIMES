package de.uni_leipzig.simba.genetics.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jgap.InvalidConfigurationException;
import org.jgap.gp.GPProblem;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.impl.GPPopulation;
import org.jgap.gp.impl.GPProgram;
import org.jgap.gp.impl.ProgramChromosome;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.data.Triple;
import de.uni_leipzig.simba.evaluation.PRFCalculator;
import de.uni_leipzig.simba.genetics.core.ALDecider;
import de.uni_leipzig.simba.genetics.core.ALFitnessFunction;
import de.uni_leipzig.simba.genetics.core.ExpressionProblem;
import de.uni_leipzig.simba.genetics.core.LinkSpecGeneticLearnerConfig;
import de.uni_leipzig.simba.genetics.core.Metric;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser.DataSets;
import de.uni_leipzig.simba.genetics.evaluation.basics.EvaluationData;
import de.uni_leipzig.simba.genetics.evaluation.pseudomeasures.EvaluationPseudoMemory;
import de.uni_leipzig.simba.genetics.evaluation.pseudomeasures.PseudoEvalFileLogger;
import de.uni_leipzig.simba.genetics.learner.coala.GlobalDissimilarity;
import de.uni_leipzig.simba.genetics.learner.coala.IterativeCoala;
import de.uni_leipzig.simba.genetics.learner.coala.MappingCorrelation;
import de.uni_leipzig.simba.genetics.learner.coala.TerritoryExpansion;
import de.uni_leipzig.simba.genetics.learner.coala.WeightDecayCorrelation;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.learning.oracle.oracle.Oracle;
import de.uni_leipzig.simba.learning.oracle.oracle.SimpleOracle;
import de.uni_leipzig.simba.util.Clock;

/**
 * Runs AL Evaluation and already computes all means and standard derivations
 * @author Lyko
 *
 */
public class CoalaEvaluation {

	public static Logger logger = Logger.getLogger("LIMES");	//(Logger) LoggerFactory.getLogger("LIMES");
	public static final int RUNS = 3;
	public static final String AL ="AL_Normal";
	public static final String AL_CLUSTERING ="_Cluster";
	public static final String COALA_ITERATIVE="_Coala2Iterative";
	public static final String AL_WEIGHTDECAY ="_WD";
	public static final String AL_TERRITORY = "_Territory";
	public static final String AL_GLOBAL = "_Global";
	public static String[] methods = {AL_GLOBAL,COALA_ITERATIVE,AL_CLUSTERING,AL_WEIGHTDECAY,AL};
	public static String methodType;
	public static  double rValueWD = 2;
	public static  int edgeCountCluster= 3;
	public static float territoryParam = 0.3f;
	public static final int oracleQuestions = 3;
	public DataSets currentDataSet;
	EvaluationData params;
	public ALFitnessFunction fitness;
	Oracle o;
	ConfigReader cR;
	/**List for the detailed file logs*/
	List<EvaluationPseudoMemory> perRunMemory = new LinkedList<EvaluationPseudoMemory>();
	/**List for the combined results*/
	List<EvaluationPseudoMemory> perDatasetMemory = new LinkedList<EvaluationPseudoMemory>();
	protected float mutationCrossRate = 0.6f;
	protected float reproduction = 0.7f;
//	protected List<Mapping> startedMappings;
	/**
	 * Restrict the number of full maps computed to get controversy instances? 
	 */
	public boolean restrictGetAllMaps = true;
	/**
	 * If a restrictGetAllMaps is set set the maximum of maps to be considered
	 */
	public int maxMaps = 10;

	
	/**
	 * Runs different rValues and edge counter on DBLP-ACM.
	 */
	public void runParameterdecision() {
		
		methods = new String[] {AL_WEIGHTDECAY};
		int[] rValues = {2, 4, 8, 16, 32};
		EvaluationData data = DataSetChooser.getData(DataSets.DBLPACM);
		Oracle oo = new SimpleOracle(data.getReferenceMapping());
		Mapping start = ExampleOracleTrimmer.trimExamplesRandomly(o.getMapping(), 2);
		for(int rValue : rValues) {
			rValueWD = rValue;
			try {
				run(DataSets.DBLPACM, 2, 2, 2, 2, start);
			} catch (InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		methods = new String[] {AL_CLUSTERING};
		for(int edgeCount = 1; edgeCount<= 5; edgeCount++) {
			edgeCountCluster=edgeCount;
			try {
				run(DataSets.DBLPACM, 2, 2, 2, 2, start);
			} catch (InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param data The Dataset.
	 * @param popSize Population size.
	 * @param gens Number of generations run before asking for more data
	 * @param numberOfExamples Number instances to be labeled by the user each inquery.
	 * @param inquiries How many inquiries.
	 * @throws InvalidConfigurationException
	 */
	public void run(DataSets data, int popSize, int gens, int numberOfExamples, int inquiries, Mapping start) throws InvalidConfigurationException {
		logger.info("Running dataset "+data);
		currentDataSet = data;
		params = DataSetChooser.getData(data);
		String name = params.getEvaluationResultFileName().replaceAll("Pseudo_eval_", "AL_");
		params.setEvaluationResultFileName(name);
		cR = params.getConfigReader();
		o = new SimpleOracle(params.getReferenceMapping());		
		for(int run = 1; run <= RUNS; run++) {
			LinkSpecGeneticLearnerConfig config = new LinkSpecGeneticLearnerConfig(cR.sourceInfo, cR.targetInfo, params.getPropertyMapping());
			config.reset();
//			fitness.destroy();
			config.setSelectFromPrevGen(reproduction);
			config.setCrossoverProb(mutationCrossRate);
			config.setMutationProb(mutationCrossRate);
			config.setReproductionProb(reproduction);
			config.setPopulationSize(popSize);
			config.setPreservFittestIndividual(true);
			config.setAlwaysCaculateFitness(true);
			config.sC = params.getSourceCache();
			config.tC = params.getTargetCache();
			logger.info("Starting run "+run);
			fitness = new ALFitnessFunction(config, o, params.getSourceCache(), params.getTargetCache(), "f-score", numberOfExamples);
			fitness.useFScore();
			if(start.size()==0) {
				start = ExampleOracleTrimmer.trimExamplesRandomly(params.getReferenceMapping(), numberOfExamples);
				System.out.println("Empty start Mapping was submitted");
			}
			if(start.size()>numberOfExamples) {
				System.err.println("TOO  much start data:"+start.size());
//				System.exit(0);
			}
//			
			System.out.println("Start data (size="+start.size()+")");
			fitness.addToReference(start);
			fitness.trimKnowledgeBases(start);
			config.setFitnessFunction(fitness);
			
			GPProblem gpP = new ExpressionProblem(config);
			
			GPGenotype gp = gpP.create();
			Clock clock = new Clock();
			ALDecider aLD  = new ALDecider();
			
				
				for(int inquery = 1; inquery <= inquiries; inquery++) {
					logger.info("Inquery nr "+inquery+" on dataset"+data+" run "+run+ " method " +
							methodType +" parameter "+((methodType.equals(AL_CLUSTERING))?edgeCountCluster
									:rValueWD));
					System.out.println("Inquery nr "+inquery+" on dataset"+data+" run "+run+ " method " +
							methodType +" parameter "+((methodType.equals(AL_CLUSTERING))?edgeCountCluster
									:rValueWD));
					for(int gen = 0; gen < gens; gen++) {
						gp.evolve();
						gp.calcFitness();
					}
					examineGen(run, inquery*numberOfExamples, gp, clock);
					// query Oracle
					
					if(inquery != inquiries) {
						List<Mapping> mapsOfPop = getMaps(gp.getGPPopulation());
						if(mapsOfPop.size() > 0) {	
							List<Triple> toAsk = new LinkedList<Triple>(); 
							if (methodType.equals(AL_CLUSTERING)){
								MappingCorrelation cor= new MappingCorrelation(cR.sourceInfo,cR.targetInfo,
										fitness.getMetric(gp.getFittestProgram()).getExpression(), params.getSourceCache(), params.getTargetCache());
								toAsk = cor.getDisimilarMappings(aLD.getControversyCandidates(mapsOfPop), numberOfExamples,edgeCountCluster);
								aLD.retrieved.addAll(toAsk);
							}else if (methodType.equals(AL_WEIGHTDECAY)){
								WeightDecayCorrelation wdc = new WeightDecayCorrelation (cR.sourceInfo,cR.targetInfo,
										fitness.getMetric(gp.getFittestProgram()).getExpression());
								toAsk = wdc.getDisimilarMappings(aLD.getControversyCandidates(mapsOfPop), numberOfExamples,rValueWD);
								aLD.retrieved.addAll(toAsk);
							}else if (methodType.equals(AL_TERRITORY)) {
								TerritoryExpansion exp = new TerritoryExpansion(cR.sourceInfo,cR.targetInfo,
										fitness.getMetric(gp.getFittestProgram()).getExpression());
								toAsk = exp.getDisimilarMappings(aLD.getControversyCandidates(mapsOfPop), numberOfExamples, edgeCountCluster);
								aLD.retrieved.addAll(toAsk);
							}
							else if (methodType.equals(AL_GLOBAL)) {
								GlobalDissimilarity exp = new GlobalDissimilarity(cR.sourceInfo,cR.targetInfo,
										fitness.getMetric(gp.getFittestProgram()).getExpression(), params.getSourceCache(), params.getTargetCache());
								toAsk = exp.getDisimilarMappings(aLD.getControversyCandidates(mapsOfPop), numberOfExamples, edgeCountCluster);
								aLD.retrieved.addAll(toAsk);
							}else if (methodType.equals(COALA_ITERATIVE)) {
								IterativeCoala itCoala = new IterativeCoala(cR.sourceInfo,cR.targetInfo,
										fitness.getMetric(gp.getFittestProgram()).getExpression(), params.getSourceCache(), params.getTargetCache());
								toAsk = itCoala.iterativlyAskOracle(aLD.getControversyCandidates(mapsOfPop), o, numberOfExamples);
								aLD.retrieved.addAll(toAsk);
							}
							else {
								logger.error("No matching method  '"+methodType+"'COALA found ");
								toAsk =aLD.getControversyCandidates(mapsOfPop, numberOfExamples);
								aLD.retrieved.addAll(toAsk);
							}
											
							logger.info("Asking about "+toAsk.size()+" instances.");
							if(toAsk.size() == 0)
								aLD.maxCount += 100;
							Mapping oracleAnswers = new Mapping();
							for(Triple t : toAsk) {					
								if(o.ask(t.getSourceUri(), t.getTargetUri()))
									oracleAnswers.add(t.getSourceUri(), t.getTargetUri(), 1d);
							}
							fitness.addToReference(oracleAnswers);
							fitness.fillCachesIncrementally(toAsk);	
						}// if we got anything to ask about
					}	//inqueries != queries		
					logger.info("Finished inquery nr "+inquery);
				}// per inquery
			finishRun();
			fitness.destroy();
			LinkSpecGeneticLearnerConfig.reset();
			logger.info("Finisched run "+run);
		}// per run
		finishDataset(RUNS, popSize, gens, params);		
	}// end of run()
	
	public void runEUCLID(DataSets data, int steps, int numberOfExamples, int inquiries, Mapping start) throws InvalidConfigurationException {
		logger.info("Running EUCLIDS on dataset "+data);
		currentDataSet = data;
		params = DataSetChooser.getData(data);
		String name = params.getEvaluationResultFileName().replaceAll("Pseudo_eval_", "AL_");
		params.setEvaluationResultFileName(name);
		cR = params.getConfigReader();
		o = new SimpleOracle(params.getReferenceMapping());		
	}
	
	
	/**
	 * Examines this generation and memorizes best program so far.
	 * @param run
	 * @param gen
	 * @param gp
	 * @param clock
	 */
	private void examineGen(int run, int gen, GPGenotype gp, Clock clock) {
		IGPProgram pBest;// = (GPProgram) gp.getFittestProgram();
		GPPopulation pop = gp.getGPPopulation();
		pop.sortByFitness();
		double fittest = Double.MAX_VALUE;
		pBest = pop.determineFittestProgram();
		fittest = fitness.calculateRawFitness(pBest);
		// to do recalculate older programs
		for(GPProgram p:(GPProgram[])pop.getGPPrograms())
		{
			double actFit = fitness.calculateRawFitness(p);
			if(fittest>actFit && getMetric(p).getExpression().indexOf("falseProp") == -1) {
				pBest = p;
				fittest = actFit;
				System.out.println("Setting to fitter program then JGAP");
			}			
		}
		boolean getBetterOld = false;
		for(EvaluationPseudoMemory mem : perRunMemory) {
			// re use older ones
			double oldFit = fitness.calculateRawFitness(mem.program);
			if(oldFit < fittest) {
				fittest = oldFit;
				pBest = mem.program;
				getBetterOld = true;
			}
		}
		if(getBetterOld) {
			System.out.println("Reusing older program...");
//			pop.addFittestProgram((IGPProgram) pBest.clone());
		}
		EvaluationPseudoMemory mem = new EvaluationPseudoMemory(run, gen, getMetric(pBest), 
				fittest, 
				1d-fittest,
				clock.totalDuration());
		mem.knownInstances = fitness.getReferenceMapping().size();
		mem.program = pBest;
		perRunMemory.add(mem);
	}



	/**
	 * Method to write the detailed log of this dataset log
	 */
	private void finishRun() {
		logger.info("Fininshing run...");
		// get full Maps and calculate prf
		logger.info("size of eval data:"+perRunMemory.size());
		for(EvaluationPseudoMemory mem : perRunMemory) {
			
			Mapping fullMap = fitness.getMapping(mem.metric.getExpression(), mem.metric.getThreshold(), true);
			Mapping best1to1Map = Mapping.getBestOneToOneMappings(fullMap);
			
			PRFCalculator pRF = new PRFCalculator();
			mem.fmeasue_1to1 = pRF.fScore(best1to1Map, o.getMapping());
			mem.recall_1to1 = pRF.recall(best1to1Map, o.getMapping());
			mem.precision_1to1 = pRF.precision(best1to1Map, o.getMapping());
			mem.fmeasue = pRF.fScore(fullMap, o.getMapping());
			mem.recall = pRF.recall(fullMap, o.getMapping());
			mem.precision = pRF.precision(fullMap, o.getMapping());
			logger.info("mem"+mem.runTime+"instances"+mem.knownInstances+"\t"+mem.fmeasue);
			
		}
		// add data to the complete memory
		this.perDatasetMemory.addAll(perRunMemory);// clear per run Memory
		perRunMemory.clear();
	}
	
//	@SuppressWarnings("unchecked")
	private void finishDataset(int maxRuns, int popSize, int gens, EvaluationData params2) {
		// sort and calc the 
		Collections.sort(perDatasetMemory);
		PseudoEvalFileLogger fileLog = new PseudoEvalFileLogger(params.getEvauationResultFolder(), params2.getEvaluationResultFileName());
		fileLog.nameExtansion = "AL_pop="+popSize+"_gens="+gens+"_meth="+methodType;
		if (methodType.equals(AL_CLUSTERING)){
			fileLog.nameExtansion += "ECount="+edgeCountCluster;
		}else if (methodType.equals(AL_WEIGHTDECAY)){
			fileLog.nameExtansion+="rValue="+rValueWD;
		}else if (methodType.equals(AL_TERRITORY)) {
			fileLog.nameExtansion+="territoryParam="+territoryParam;
		}
		fileLog.log(perDatasetMemory, maxRuns, gens, params2);
		perDatasetMemory.clear();
	}
	
	
	public List<Mapping> getMaps(GPPopulation pop) {
		List<Mapping> allMaps = new LinkedList<Mapping>();
		Set<String> expressions = new HashSet<String>();
		List<IGPProgram> considered = new LinkedList<IGPProgram>();
		int max = pop.size();
		if(restrictGetAllMaps) {
			pop.sortByFitness();
			pop.sort(new Comparator<IGPProgram>() {
//				@Override
				public int compare(IGPProgram o1, IGPProgram o2) {
					if(o1.getFitnessValue()<o2.getFitnessValue())
						return -1;
					if(o1.getFitnessValue()>o2.getFitnessValue())
						return 1;
					return 0;
				}
			});
			max = Math.min(maxMaps, pop.size());	
		} 
		for(int i = 0; i<max; i++) {
			considered.add(pop.getGPProgram(i));
		}
	//	for(int i = 0; i<Math.min(pop.size(), 20); i++) {		
			for(IGPProgram gProg : considered) {
				String expr="falseProp";
				double d=0.9d;
				try {	
					Metric metric = getMetric(gProg);
					expr = metric.getExpression();
					d = metric.getThreshold();
				}
				catch(IllegalStateException e)  {
					continue;
				}
				if(expr.indexOf("falseProp")>-1)
					continue;
				if(expressions.contains(expr+d)) {
					maxMaps++;
					continue;
				}
				else
					expressions.add(expr+d);
				try {
					allMaps.add(fitness.getMapping(expr, d, true));
				} catch (OutOfMemoryError bounded) {
					logger.info("Encountered Memory error...");
					continue;
				}
			}
		//}
		return allMaps;
	}
	
	private Metric getMetric(IGPProgram p) {
		Object[] args = {};
		ProgramChromosome pc = p.getChromosome(0);
		return (Metric) pc.getNode(0).execute_object(pc, 0, args);
	}
	
	/**
	 * Restrict the number of individuals executed (full mappings to be calculated) to generate
	 * new data for a user to label to the given value;
	 * @param maximum The maximal number of (different) individuals in the population to be considered
	 */
	public void restrictNumberOfMaps(int maximum) {
		if(maximum>0) {
			restrictGetAllMaps = true;
			maxMaps = maximum;
		}
	}
	
	public static void runEval(DataSets data) {
		EvaluationData ds = DataSetChooser.getData(data);
		Mapping start = ExampleOracleTrimmer.trimExamplesRandomly(ds.getReferenceMapping(), 10);
		if(start.size() > 10) {
			System.err.println("Too much training Data: "+start.size());
			System.exit(0);
		}
		for(String method : new String[]{AL_GLOBAL, AL, AL_CLUSTERING, AL_WEIGHTDECAY, AL_TERRITORY}) {
			for(int pop : new int[]{20}) {
				CoalaEvaluation.methodType = method;
				CoalaEvaluation.edgeCountCluster = 3;
				CoalaEvaluation.rValueWD = 2.0d;
				CoalaEvaluation eval = new CoalaEvaluation();
				try {
					System.out.println("Running "+data+" with method: " +  method +" for popSize="+pop);
					
					eval.run(data, pop, 50, 10, 10, start);
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
				}catch (Exception e) {
		            e.printStackTrace();
		        } catch (OutOfMemoryError e) {
		            e.printStackTrace();
		        }
			}
		}
	}
	// use CoalaEvaluation dataset methodInt pop
	public static void main(String args[]) {
		logger.setLevel(Level.WARN);
		DataSets[] datas = new DataSets[] {
				DataSets.PERSON1,		//0
				DataSets.PERSON2,		//1
				DataSets.RESTAURANTS,	//2
				DataSets.DBLPACM,		//3
				DataSets.ABTBUY,		//4
				DataSets.AMAZONGOOGLE,	//5
				DataSets.DBLPSCHOLAR	//6
		};
		if(args.length == 3) {
			
			int dataset = Integer.parseInt(args[0]);
			int methodInt = Integer.parseInt(args[1]);
			int pop = Integer.parseInt(args[2]);
			DataSets data = DataSetChooser.DataSets.PERSON1;
			if(dataset>1)
				data = datas[dataset];
			String method = AL;
			if(methodInt == 1)
				method = AL_CLUSTERING;
			if(methodInt == 2)
				method = AL_WEIGHTDECAY;
			if(methodInt == 3)
				method = AL_TERRITORY;
			if(methodInt == 4)
				method = AL_GLOBAL;
			if(methodInt >= 5)
				method = COALA_ITERATIVE;
			System.out.println("Running "+data+" with "+method+" with pop="+pop);						
			
			
			EvaluationData ds = DataSetChooser.getData(data);
			Oracle oo = new SimpleOracle(ds.getReferenceMapping());
			Mapping start = ExampleOracleTrimmer.trimExamplesRandomly(oo.getMapping(), 10);
			CoalaEvaluation eval = new CoalaEvaluation();
			eval.methodType = method;
			try {
				eval.run(data, pop, 20, 5, 3, start);
			} catch (InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} // 3 arguments
		if(args.length == 1 && args[0].equalsIgnoreCase("persons")) {
			datas = new DataSets[] {DataSets.PERSON1, DataSets.PERSON2};
			for(DataSets data : datas)
				runEval(data);
		} else
		if(args.length == 1 && args[0].equalsIgnoreCase("abtbuy")) {
			runEval(DataSets.ABTBUY);
		} else
		if(args.length == 1 && args[0].equalsIgnoreCase("restaurants")) {
			runEval(DataSets.RESTAURANTS);
		} else
		if(args.length == 1 && args[0].equalsIgnoreCase("books")) {
			datas = new DataSets[] {DataSets.DBLPACM, DataSets.DBLPSCHOLAR};
			for(DataSets data : datas)
				runEval(data);
		} else
			if(args.length == 1 && args[0].equalsIgnoreCase("movies")) {
				datas = new DataSets[] {DataSets.DBPLINKEDMDB};
				for(DataSets data : datas)
					runEval(data);
			} else	
		if(args.length == 1 && args[0].equalsIgnoreCase("eswc")) {
			datas = new DataSets[] {DataSets.PERSON1, DataSets.PERSON2, DataSets.RESTAURANTS, 
					DataSets.DBLPACM, 
					DataSets.ABTBUY, DataSets.MOVIES, 
					DataSets.AMAZONGOOGLE, DataSets.DBLPSCHOLAR, 
					};
			for(DataSets data : datas)
				runEval(data);				
		}		
		else 
		if(args.length == 1 && args[0].equalsIgnoreCase("paramtest")) {				
				try {
					// for every dataset
					for(DataSets data : datas) {
						EvaluationData ds = DataSetChooser.getData(data);
						Oracle oo = new SimpleOracle(ds.getReferenceMapping());
						Mapping start = ExampleOracleTrimmer.trimExamplesRandomly(oo.getMapping(), 10);
					// pop: 20 vs. 100
						for (String method: new String[]{AL_CLUSTERING, AL_WEIGHTDECAY, AL_TERRITORY}){
							CoalaEvaluation.methodType = method;
							if (method.equals(CoalaEvaluation.AL_CLUSTERING)){
								for (int edges: new int[]{1,2,3,4,5}){
									CoalaEvaluation.edgeCountCluster =edges;
									for(int pop : new int[] {20}) {
										CoalaEvaluation eval = new CoalaEvaluation();
										eval.run(data,pop, 50, 10, 10, start);
									}
								}// edges per node
							}else if (method.equals(CoalaEvaluation.AL_WEIGHTDECAY)){
								for (double rValue: new double[]{2,4,8,16,32}){
									CoalaEvaluation.rValueWD=rValue;
									for(int pop : new int[] {20}) {
										CoalaEvaluation eval = new CoalaEvaluation();
										eval.run(data,pop, 50, 10, 10, start);
									}//pop
								}//exponent variation
							}// WeightDecay
							else if (method.equals(AL_TERRITORY)) {
								for (int edges: new int[]{1,2,3,4,5}){
									CoalaEvaluation.edgeCountCluster =edges;
									for(int pop : new int[] {20}) {
										CoalaEvaluation eval = new CoalaEvaluation();
										eval.run(data,pop, 50, 10, 10, start);
									}
								}// edges per node
							}// prototype TerritoryExpansion
							else if (method.equals(AL_GLOBAL)) {
								for(int pop : new int[] {20}) {
									CoalaEvaluation eval = new CoalaEvaluation();
									eval.run(data,pop, 50, 10, 10, start);
								}
							}
						}
					}//for dataset
					} catch (InvalidConfigurationException e) {
						e.printStackTrace();
					}
				} // if argslength == 1 && paramtes					
	}	
}
