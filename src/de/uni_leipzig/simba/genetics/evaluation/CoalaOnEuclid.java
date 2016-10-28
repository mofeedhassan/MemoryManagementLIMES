package de.uni_leipzig.simba.genetics.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.data.Triple;
import de.uni_leipzig.simba.evaluation.PRFCalculator;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser.DataSets;
import de.uni_leipzig.simba.genetics.evaluation.basics.EvaluationData;
import de.uni_leipzig.simba.genetics.learner.coala.IterativeCoala;
import de.uni_leipzig.simba.genetics.learner.coala.MappingCorrelation;
import de.uni_leipzig.simba.learning.oracle.oracle.Oracle;
import de.uni_leipzig.simba.learning.oracle.oracle.SimpleOracle;
import de.uni_leipzig.simba.mapper.SetOperations;
import de.uni_leipzig.simba.selfconfig.ComplexClassifier;
import de.uni_leipzig.simba.selfconfig.DisjunctiveMeshSelfConfigurator;
import de.uni_leipzig.simba.selfconfig.LinearMeshSelfConfigurator;
import de.uni_leipzig.simba.selfconfig.MeshBasedSelfConfigurator;
import de.uni_leipzig.simba.selfconfig.SimpleClassifier;

public class CoalaOnEuclid {
	public static final Logger logger = Logger.getLogger(MappingCorrelation.class);
	
	// EUCLID parameters
	int steps = 5;
	int gridPoints = 5;
	double coverage = 0.6;
	double beta = 1.0;
	//Active Learning parameters
	int inqueries = 10;
	int examples = 10;
	
	
	
	
	public String run(EvaluationData data) {
		String answer = "\n --- "+data.getName()+" --- \n";
		logger.info("RUNNNING "+data.getName());
	    MeshBasedSelfConfigurator lsc;
	    
        logger.info("Running linear active EUCLID....");
        answer += "linear active EUCLID\n";
        lsc = new LinearMeshSelfConfigurator(data.getSourceCache(), data.getTargetCache(), coverage, beta);
        answer += run(lsc, data);
        
        logger.info("Running disjunctive active EUCLID....");
        answer += "disjunctive active EUCLID\n";
        lsc = new DisjunctiveMeshSelfConfigurator(data.getSourceCache(), data.getTargetCache(), coverage, beta);
        answer += run(lsc, data);
    
        answer += "linear active EUCLID\n";
        logger.info("Running conjunctive/mesh active EUCLID....");
        lsc = new MeshBasedSelfConfigurator(data.getSourceCache(), data.getTargetCache(), coverage, beta);
        answer += run(lsc, data);
        
        return answer;
	}
	
	public String run(MeshBasedSelfConfigurator lsc,  EvaluationData data) {
		String answer= "";
		answer+="Iterations\tF-Score\tRecall\tPrecision\tDuration\tTrainingData\tPositives\t" +
		"CC\n";
	
		// first run unsupervised
		lsc.setSupervision(false);
	   	List<SimpleClassifier> cp = lsc.getBestInitialClassifiers();
	   	ComplexClassifier cc = lsc.getZoomedHillTop(5, steps, cp);
	   	String mExpr = cc.classifiers.get(0).getMetricExpression();
   		IterativeCoala itCoala = new IterativeCoala(data.getConfigReader().sourceInfo,data.getConfigReader().targetInfo,
   				mExpr, data.getSourceCache(), data.getTargetCache());  

	   	Oracle o = new SimpleOracle(data.getReferenceMapping());
	   	Set<Triple> asked = new HashSet<Triple>();
	   	lsc.setSupervision(true);

		long start = System.currentTimeMillis();	
	   	long middle = System.currentTimeMillis();
	   	
	   	// learning steps
	   	for(int i=1; i<=inqueries; i++) {
	   		try {
		   		middle = System.currentTimeMillis();
		   		//first get answers
		   		mExpr = cc.classifiers.get(0).getMetricExpression();
			   	itCoala.setCurrentMetric(mExpr);
		   		Mapping lesser = computeLessStrictMapping(lsc, cc);
		   		logger.info("Computed");
		   		List<Triple> triples = transformMapping(cc.mapping, lesser);
		   		List<Triple> stepAsked = itCoala.iterativlyAskOracle(triples, o, examples);
		   		boolean  added = asked.addAll(stepAsked);
		   		logger.info(added+" --- Adding asked "+stepAsked.size()+" instances to already known."+": "+stepAsked);
		   		Mapping tmp = new Mapping();
		   		for(Triple t : stepAsked) {
		   			tmp.add(t.getSourceUri(), t.getTargetUri(), t.getSimilarity());
		   		}
		   		lsc.setSupervisedBatch(tmp);
		   		// learning step
		   		cp = lsc.getBestInitialClassifiers();
			   	cc = lsc.getZoomedHillTop(5, steps, cp);	   	
			   	
			   	//evaluate
			   	long end = System.currentTimeMillis();
			   	double f = PRFCalculator.fScore(cc.mapping, data.getReferenceMapping());
			   	double p = PRFCalculator.precision(cc.mapping, data.getReferenceMapping());
			   	double r = PRFCalculator.recall(cc.mapping, data.getReferenceMapping());
			   	int positives = 0;
			   	for(Triple t : asked)
			   		if(t.getSimilarity()>0)
			   			positives++;
			   	String step =""+i+"\t"+f+"\t"+r+"\t"+p+"\t"+((end-middle)/1000)+"\t"+asked.size()+"\t"+positives+"\t"+cc+"\n";
			   	logger.info(step);
			   	answer+=step;
	   		}catch(Exception e) {
	   			logger.error("Error iteration "+i+": "+e.getMessage());
	   			answer += "Error Step i\n";
	   		}
	   	}	   	
	   	answer+="\n";
	    return answer;
	}
	
//	
//	public String run(Cache s, Cache t, EvaluationData data, double coverage, double beta, String type, Mapping trainingData) {
//            MeshBasedSelfConfigurator lsc;
//	        if (type.toLowerCase().startsWith("l")) {
//	        	System.out.println("Running linear EUCLID....");
//	            lsc = new LinearMeshSelfConfigurator(s, t, coverage, beta);
//	        } else if (type.toLowerCase().startsWith("d")) {
//	        	System.out.println("Running disjunctive EUCLID....");
//	            lsc = new DisjunctiveMeshSelfConfigurator(s, t, coverage, beta);            
//	        } else {
//	        	System.out.println("Running mesh EUCLID....");
//	            lsc = new MeshBasedSelfConfigurator(s, t, coverage, beta);
//	        }
//
//	        lsc.setSupervisedBatch(trainingData);
//	        
//	        return run(lsc, trainingData, data);
//	       
//	}
//	
	
//	public String run(MeshBasedSelfConfigurator lsc, Mapping trainingData, EvaluationData data) {
//		String output ="reference.size="+trainingData.size()+" Trimmed Caches to "+lsc.source.size()+" and "+lsc.target.size()+" \n";
//	    long begin = System.currentTimeMillis();
////	    double delta = 0.1;  
//	    for(int inq = 1; inq <= inqueries; inq++) {
//	    	List<SimpleClassifier> cp = lsc.getBestInitialClassifiers();
//	    	ComplexClassifier cc = lsc.getZoomedHillTop(gridPoints, steps, cp);
//	    	ComplexClassifier cc_copy = cc.clone();
//		    long end = System.currentTimeMillis();
//		    Mapping map = cc.mapping.clone();
//		    Mapping lesser = computeLessStrictMapping(lsc, cc);
//		     	     
//		     
//		     
//		     
//		    Mapping substr = SetOperations.difference(lesser, map);
//		    System.out.println("substr (lesser-map)= "+substr.size());
//	    }
//	    List<SimpleClassifier> cp = lsc.getBestInitialClassifiers();
////	    long middle = System.currentTimeMillis();
//	    output += "Type\tIterations\tMapping time\tRuntime\tTotaltime\tPseudo-F" +
//	        		"\tPrecision_1to1\tRecall_1to1\tReal F_1to1" +
//	        		"\tPrecision\tRecall\tReal F\t" +
//	        		"CC\n";
////	        long duration = 0;
//
//	    double f = 0d;
//	    ComplexClassifier cc = lsc.getZoomedHillTop(5, steps, cp);
//	    long end = System.currentTimeMillis();
//	    Mapping m = cc.mapping;
//
//	    return output;
//	}
//	
	/**
	 * Transform a single Mapping into an List of Triples COALA works with.
	 * E.g. converts thresholds such that values lower then 0.5 are considered
	 * no match.
	 * @param map The Mapping returned by a learner
	 * @param lesser A Mapping that is lesser strict, such that |lesser|>|map|. So
	 * 			all instances in lesser-map are no matches.
	 * @return
	 */
	 private List<Triple> transformMapping(Mapping map, Mapping lesser) {
		 List<Triple> result = new ArrayList<Triple>();
		 if(lesser.size()>0) {
			 Mapping substr = new Mapping();
			 for(String sUri:lesser.map.keySet())
				 for(String tUri:lesser.map.get(sUri).keySet()) {
					 if(Math.abs(map.getSimilarity(sUri, tUri)-lesser.getSimilarity(sUri, tUri)) > 0.1)
						 substr.add(sUri, tUri, lesser.getSimilarity(sUri, tUri));
				 }
			 
			 
//			 SetOperations.difference(lesser, map);
			 // negatives are considered to have a similarity lower then 0.5
			 for(String sUri:substr.map.keySet()) {
				 for(String tUri:substr.map.get(sUri).keySet()) {
					 double sim = substr.getSimilarity(sUri, tUri);
					 if(sim>=0.5) {
						 sim = sim-0.5;
					 }
					 Float f = Float.parseFloat(""+sim);
					 result.add(new Triple(sUri,tUri,f));
				 }
			 } 
		 }
		 // converts the Mapping such that all links have a confidence beyond 0.5
		 for(String sUri:map.map.keySet()) {
			 for(String tUri:map.map.get(sUri).keySet()) {
				 double sim = map.getSimilarity(sUri, tUri);
				 if(sim<0.5) {
					 sim = sim+0.5;
				 }
				 Float f = Float.parseFloat(""+sim);
				 result.add(new Triple(sUri,tUri,Math.min(f,1)));
			 }
		 }
		 return result;
	 }
	 
	 /**
	  * Computes a less strict Mapping. That is done by iteratively lower the thresholds
	  * of the given classifier.
	  * @param lsc Learner which computed the Mapping
	  * @param cc Complex Classifier building the Mapping
	  * @return A Mapping that contains more links then the given one.
	  */
	 private Mapping computeLessStrictMapping(MeshBasedSelfConfigurator lsc, ComplexClassifier cc) {
		 double delta = 0.1; double maxThreshold = 0;
		 for(SimpleClassifier sc : cc.classifiers) {
			 if(sc.threshold>maxThreshold) {
				 maxThreshold = sc.threshold;
			 }
		 }
//		 Mapping original = cc.mapping;
		 Mapping lesser = new Mapping();
		 while(delta<maxThreshold) {
			 ComplexClassifier cc_copy = cc.clone();
			 for(SimpleClassifier sc : cc_copy.classifiers) {
				 sc.threshold = Math.max(sc.threshold-delta, 0.1);
			 }
			 lesser = lsc.getMapping(cc_copy.classifiers);
//			 if(lesser.size()>original.size()))
//				 return lesser;
			 delta += 0.1;
		 }
		 
		 // enforce size
		 if(lesser.size() < inqueries*examples) {
			 logger.info("Forcing filling up Negatives ... ");
			 cc.mapping.initReversedMap();
			 HashMap<Double, HashMap<String, TreeSet<String>>> reverse = cc.mapping.reversedMap;
			 List<Double> thresholds = new ArrayList<Double>();
			 thresholds.addAll(reverse.keySet());
			 Collections.sort(thresholds);
			 int i = 0;
			 while(lesser.size()<(inqueries*examples)) {
				 HashMap<String, TreeSet<String>> bucket = reverse.get(thresholds.get(i));
				 for(String sUri : bucket.keySet())
					 for(String tUri : bucket.get(sUri))
						 lesser.add(sUri, tUri, cc.mapping.getSimilarity(sUri, tUri)-0.5);
				 i++;
			 }
		 }
		 
		 return lesser;
	 }
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EvaluationData data = DataSetChooser.getData(DataSets.DBLPACM);
		CoalaOnEuclid cOe = new CoalaOnEuclid();
	
		String res = cOe.run(data);
		
		System.out.println(res);
	}
		

}
