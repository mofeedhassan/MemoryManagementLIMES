package de.uni_leipzig.simba.genetics.learner.coala;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.data.Triple;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser;
import de.uni_leipzig.simba.genetics.evaluation.basics.DataSetChooser.DataSets;
import de.uni_leipzig.simba.genetics.evaluation.basics.EvaluationData;
import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.learning.oracle.oracle.Oracle;

public class IterativeCoala extends MappingCorrelation{
	public static Logger logger = Logger.getLogger("LIMES");	//(Logger) LoggerFactory.getLogger("LIMES");
	List<Triple> assumedPositives = new ArrayList<Triple>();
	List<Triple> assumedNegatives = new ArrayList<Triple>();
	Set<Triple> alreadyKnown = new HashSet<Triple>();
	
	public IterativeCoala(KBInfo source, KBInfo target, String metric, Cache sC, Cache tC) {
		super(source, target, metric, sC, tC);
	}
	
	public List<Triple> iterativlyAskOracle(List<Triple> mostInformatives, Oracle o, int k) {
		 List<Triple> answer = new ArrayList<Triple>();
		 logger.info("iterativly Ask Oracle about ...."+mostInformatives.size());
		Triple lastTriple = chooseFirstTriple(mostInformatives);
		alreadyKnown.add(lastTriple);
		boolean match = o.ask(lastTriple.getSourceUri(), lastTriple.getTargetUri());
		if(match) {
			answer.add(new Triple(lastTriple.getSourceUri(), lastTriple.getTargetUri(),1));
		} else {
			answer.add(new Triple(lastTriple.getSourceUri(), lastTriple.getTargetUri(),-1));
		}
		Triple nextTriple;
		
		
		for(int i = 0; i<k-1; i++) {
			logger.info("Iteration "+i+" last confidence="+lastTriple.getSimilarity()+" match?"+match);
			logger.info("Iteration "+i+" positives:"+assumedPositives.size()+" assumedNegatives:"+assumedNegatives.size());
			if(lastTriple.getSimilarity()>=0.5) { // assumed positive
				if(match) { //assumed correct: it was indeed a match
					nextTriple=chooseNext(assumedNegatives, lastTriple, true);
				}else {// assumed false
					nextTriple=chooseNext(assumedPositives, lastTriple, false);
				}
			}			
			else{//assumed negative
				if(match) { //assumed false: it was indeed a match
					nextTriple=chooseNext(assumedNegatives, lastTriple, false);
				}else {// assumed correct
					nextTriple=chooseNext(assumedPositives, lastTriple, true);
				}
			}
			
			match = o.ask(nextTriple.getSourceUri(), nextTriple.getTargetUri());
			if(match) {
				answer.add(new Triple(nextTriple.getSourceUri(), nextTriple.getTargetUri(),1));
			} else {
				answer.add(new Triple(nextTriple.getSourceUri(), nextTriple.getTargetUri(),-1));
			}
			alreadyKnown.add(nextTriple);
			lastTriple = nextTriple;
		}
		logger.info("answer.size()"+answer.size());
		return answer;
	}
	
	/**
	 * Chooses next triple to be annotated based upon the distance/similarity to the former Triple lastTriple
	 * from the given classList
	 * @param classList
	 * @param lastTriple
	 * @param correct
	 * @return
	 */
	private Triple chooseNext(List<Triple> classList, Triple lastTriple, boolean correct) {
		List<Triple> toChooseFrom;
		List<Triple> classCopy = new ArrayList<Triple>(classList.size());
		classCopy.addAll(classList);
		
		classCopy.removeAll(alreadyKnown);
		logger.info("Remove from classList ("+classList.size()+ ") known ("+alreadyKnown.size()+") result: "+classCopy.size());
		TreeMap<Double, List<Triple>> distanceMap = getDistances(lastTriple, classCopy);
		if(correct) {
			toChooseFrom = distanceMap.lastEntry().getValue();
		} else {
			toChooseFrom = distanceMap.firstEntry().getValue();
		}
		
		Triple nextTriple = toChooseFrom.get(0);
		return nextTriple;
	}
	
	private TreeMap<Double, List<Triple>> getDistances(Triple original, List<Triple> compareTo) {
		TreeMap<Double, List<Triple>> distanceMap = new TreeMap<Double, List<Triple>>();
		compareTo.add(original);
		TreeMap<Float,List<Triple>> tripleMap = this.initInformativeTripleMap(compareTo);
		HashMap<Integer,HashMap<String,Double>> similarityCache  = this.initSimilarityCache(compareTo);
				for (int i= 0; i<compareTo.size();i++){
//					if(!compareTo.get(i).equals(original)) {
						double sim = calculateDistance(original, compareTo.get(i), similarityCache);
						List<Triple> bucket = distanceMap.get(sim);
						if(bucket == null)
							bucket = new ArrayList<Triple>();
						bucket.add(compareTo.get(i));
						distanceMap.put(sim, bucket);
//					}					
				}
		logger.info("Distance Map computed... size"+distanceMap.size());
		return distanceMap;
	}
	
	/**
	 * Chooses the first triple to be 
	 * @param mostInformatives
	 * @return
	 */
	private Triple chooseFirstTriple(List<Triple> mostInformatives) {
		
		Collections.sort(mostInformatives, new Comparator<Triple>(){
			@Override
			public int compare(Triple o1, Triple o2) {
				if(o1.getSimilarity() == o2.getSimilarity())
					return 0;				
				else if(o1.getSimilarity() < o2.getSimilarity())
					return -1;
				else 
					return 1;
			}			
		}
		);
		
		for(Triple t : mostInformatives) {
			if(t.getSimilarity()>=0.5)
				assumedPositives.add(t);
			else
				assumedNegatives.add(t);
		}
		
		return mostInformatives.get(mostInformatives.size()-1);
	}
	
	public void setCurrentMetric(String metric) {
		this.metric = metric;
		propMeasureMap = getStringMeasures(metric);
	}
	
	public static void main(String args[]) {
		EvaluationData data = DataSetChooser.getData(DataSets.PERSON1);
		//TODO implement
	}
	
	
}
