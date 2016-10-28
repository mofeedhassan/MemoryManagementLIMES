package de.uni_leipzig.simba.genetics.learner.coala;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.data.Triple;
import de.uni_leipzig.simba.genetics.evaluation.statistics.Statistics;
import de.uni_leipzig.simba.io.KBInfo;

/**
 * Basic ideas of this class is to calculate a global dissimilarity based upon a Link Specification
 * and a classifier boundary:
 * STEPS:
 * 1st expand classifier boundary using some delta +/- applied upon the thresholds
 * k - times:
 * 2. pick one representative inside this similarity space around the border
 * 3. compare it to all others inside the boundary and pick the furtherst away
 * 
 * @author Klaus Lyko
 *
 */
public class GlobalDissimilarity extends MappingCorrelation{
	public GlobalDissimilarity(KBInfo source, KBInfo target, String metric, Cache sC, Cache tC) {
		super(source, target, metric, sC, tC);
	}
	
	@Override
	public List <Triple> getDisimilarMappings(List <Triple> tripleList, int trainingDataSize, int edgeCountPerNode) {
		return getDissimilarTriples(tripleList, trainingDataSize);
	}

	/**
	 * Main method:
	 * 1. pick random candidate
	 * 2.1 for all candidates pick a new one which is the furtherst away
	 * @param triples
	 * @param k
	 * @return
	 */
	public List<Triple> getDissimilarTriples(List<Triple> triples, int k) {
		System.out.println("Running global dissimilarity on "+triples.size()+" for "+k+" examples");
		Set<Triple> picked = new HashSet<Triple>(k);
		List<Triple> pickedOrder = new LinkedList<Triple>();
		// pick first one
		Triple first = triples.get(0);
		picked.add(first);
		pickedOrder.add(first);
		// sim space
		int max = Math.min(k, triples.size());
		while(picked.size()<max) {
			TreeMap<Float, List<Triple>> simSpace = compareSimilarity(picked, triples);
			Entry<Float, List<Triple>> le = simSpace.lastEntry(); // TreeMap sorting
			// pick one FIXME random / first ???
			Triple nextTriple = le.getValue().get(0);
			picked.add(nextTriple);
			pickedOrder.add(nextTriple);
		}
		return pickedOrder;
	}
	
	private TreeMap<Float, List<Triple>> compareSimilarity(Set<Triple> alreadyPicked, List<Triple> others) {
		// Maps
		TreeMap<Float, List<Triple>> similaritySpace = new TreeMap<Float, List<Triple>>();
		
		// cache for a triple and his similarity values for each property
		HashMap<Integer,HashMap<String,Double>> similarityCache  = this.initSimilarityCache(others);
		
		// at least n comparisons
		for(Triple other : others) { // don't pick already picked ones
			if(alreadyPicked.contains(other)) {
				continue;
			}
			Statistics stat = new Statistics();
			// for each Triple already chosen
			for(Triple picked : alreadyPicked) {
				double sim = this.calculateDistance(other, picked, similarityCache);
				stat.add(sim);
			}
			float distance = (float) (stat.mean*100);
			if(similaritySpace.containsKey(distance)) {
				List<Triple> tl = similaritySpace.get(distance);
				tl.add(other);
			} else {
				List<Triple> tl = new LinkedList<Triple>();
				tl.add(other);
				similaritySpace.put(distance, tl);
			}			
		}
		
		return similaritySpace;
	}

	/**
	 * Global dissimilarity calculation for a single Mapping (e.g. EUCLID sup.)
	 * less-map => positive border
	 * plus-map => negative border
	 * @param minus less strict Mapping, calculate by decreasing threshold(s) of map
	 * @param map original Mapping
	 * @param plus more strict Mapping, calculate by increasing threshold(s) of map
	 * @param k number of instances to annotate
	 * @return
	 */
	public List<Triple> singleGlobalDissimilarity(Mapping minus, Mapping map, Mapping plus, int k) {
		List<Triple> searchSpace = new LinkedList<Triple>();
		// 1. compute negative border.
		for(String sUri : minus.map.keySet())
			for(String tUri : minus.map.get(sUri).keySet())
				if(!map.contains(sUri, tUri)) {
					Triple negExample = new Triple(sUri, tUri, (float)minus.getSimilarity(sUri, tUri));
					searchSpace.add(negExample);
				}
		// 2. compute positive border.
		for(String sUri : plus.map.keySet())
			for(String tUri : plus.map.get(sUri).keySet()) {
				Triple posExample = new Triple(sUri, tUri, (float)plus.getSimilarity(sUri, tUri));
				searchSpace.add(posExample);
			}
		return getDissimilarTriples(searchSpace, k);
	}
	
}
