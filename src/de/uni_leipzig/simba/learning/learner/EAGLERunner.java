package de.uni_leipzig.simba.learning.learner;

import java.util.LinkedList;

import org.jgap.Configuration;
import org.jgap.InvalidConfigurationException;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.impl.GPGenotype;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.genetics.core.ExpressionProblem;
import de.uni_leipzig.simba.genetics.core.LinkSpecGeneticLearnerConfig;
import de.uni_leipzig.simba.genetics.core.Metric;
import de.uni_leipzig.simba.genetics.core.PseudoFMeasureFitnessFunction;
import de.uni_leipzig.simba.genetics.evaluation.pseudomeasures.EvaluationPseudoMemory;
import de.uni_leipzig.simba.genetics.evaluation.pseudomeasures.PseudoBaseEvaluator;
import de.uni_leipzig.simba.genetics.util.PropertyMapping;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.selfconfig.PseudoMeasures;
import de.uni_leipzig.simba.util.Clock;

public class EAGLERunner extends PseudoBaseEvaluator {

	public EAGLERunner() {
		population = 10;
		generations = 15;
		
	}
	
	public Mapping runEAGLE(Cache sC, Cache tC, PropertyMapping pM, ConfigReader cR) throws InvalidConfigurationException {
		bestEverFitness = Double.MAX_VALUE;
		bestEver = null;
//		System.out.println("Running passage "+run+" on "+param.getConfigFileName()+ " with beta="+beta);
		perRunAndDataSet = new LinkedList<EvaluationPseudoMemory>();
		Clock clock = new Clock();

//		ConfigReader cR = param.getConfigReader();
//		Cache sC = param.getSourceCache();
//		Cache tC = param.getTargetCache();
//		PropertyMapping pM = param.getPropertyMapping();
	
		LinkSpecGeneticLearnerConfig config = new LinkSpecGeneticLearnerConfig(cR.sourceInfo, cR.targetInfo, pM);
		config.setCrossoverProb(crossover);
		config.setMutationProb(mutation);
		config.setReproductionProb(reproduction);
		config.setPopulationSize(population);
		config.setPreservFittestIndividual(true);
		fitness = PseudoFMeasureFitnessFunction.getInstance(config, new PseudoMeasures(), sC, tC);
//		fitness = new PseudoFMeasureFitnessFunction(config, measure, sC, tC);
		fitness.setBeta(1d);
		config.setFitnessFunction(fitness);
		// evolve
		ExpressionProblem gpP = new ExpressionProblem(config);
		GPGenotype gp = gpP.create();
		for(int gen=1; gen<generations; gen++) {
			gp.evolve();
//			if(gen % 5 == 0 || gen == 1) {
				IGPProgram p = processGeneration(gp, gen, 0);
				System.out.println("Finished generation "+gen+" / "+generations);
				System.out.println("best:="+getMetric(p));
//			}
		}
		
		// last generation: determine the best
		IGPProgram bestProgram  = processGeneration(gp, generations, 0);
		if(bestProgram == null)
			logger.error("No best Program found");
		Metric bestMetric = getMetric(bestProgram);
		if(bestMetric == null)
			logger.error("No bestMetric found");
		else
			logger.info("Best metric: "+bestMetric);
		EvaluationPseudoMemory mem = new EvaluationPseudoMemory(0, generations, bestMetric, 
				fitness.calculateRawFitness(bestProgram),
				fitness.calculatePseudoMeasure(bestProgram),
				clock.totalDuration());
		mem.pseudoFMeasure = fitness.calculatePseudoMeasure(bestProgram);
		mem.betaValue = 1;
		mem.fullMapping = fitness.getMapping(mem.metric.getExpression(), mem.metric.getThreshold());
//		fitness.destroy();
//		fitness = null;
//		Configuration.reset();
//		LinkSpecGeneticLearnerConfig.reset();
		return mem.fullMapping;
	}
}
