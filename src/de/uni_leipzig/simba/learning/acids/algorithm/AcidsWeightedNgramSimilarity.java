package de.uni_leipzig.simba.learning.acids.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import de.uni_leipzig.simba.learning.acids.data.Resource;

public class AcidsWeightedNgramSimilarity extends AcidsStringSimilarity {
	
	public AcidsWeightedNgramSimilarity(AcidsProperty property, int index) {
		super(property, index);
		
		// load tf-idf weights
//		init(property.getMeasures().getSetting().getSources(), property.getMeasures().getSetting().getTargets());
	}

	@Override
	public String getName() {
		return "Weighted N-gram Similarity";
	}

	@Override
	public int getDatatype() {
		return AcidsDatatype.TYPE_STRING;
	}

	@Override
	public double getSimilarity(String a, String b) {
		return this.computeSimilarity(a, b, n);
	}
	
	@Override
	public AcidsProperty getProperty() {
		return property;
	}

	/**
	 * The "n" of n-grams. Default is trigram.
	 */
	private int n = 3;
	
	/**
	 * Update interval for weights.
	 */
	private final double DELTA = 0.05;
	
	private HashMap<String, Double> weights = new HashMap<String, Double>();
	private TreeSet<String> ngCacheIncrease = new TreeSet<String>();
	private TreeSet<String> ngCacheDecrease = new TreeSet<String>();
	
	private AcidsCrowFilter filter = new AcidsCrowFilter(this); 
	
	@SuppressWarnings("unused")
	private void init(ArrayList<Resource> sources, ArrayList<Resource> targets) {
		ArrayList<Resource> all = new ArrayList<Resource>();
		all.addAll(sources);
		all.addAll(targets);
		
		HashMap<String, Integer> tf_gen = new HashMap<String, Integer>();
		HashMap<String, Integer> idf_den = new HashMap<String, Integer>();

		
		for(Resource r : all) {
			HashMap<String, Integer> tf_p = new HashMap<String, Integer>();
			ArrayList<String> ngs = getNgrams(r.getPropertyValue(property.getName()), n);
			for(String ng : ngs) {
				Integer cnt = tf_p.get(ng);
				if(cnt == null)
					tf_p.put(ng, 1);
				else
					tf_p.put(ng, cnt+1);
			}
			for(String ng : tf_p.keySet()) {
				Integer part = tf_gen.get(ng);
				if(part == null)
					tf_gen.put(ng, tf_p.get(ng));
				else
					tf_gen.put(ng, part + tf_p.get(ng));
				
				Integer cnt = idf_den.get(ng);
				if(cnt == null)
					idf_den.put(ng, 1);
				else
					idf_den.put(ng, cnt+1);
			}
		}
		
		for(String ng : idf_den.keySet()) {
			double tf = (double) tf_gen.get(ng);
			double idf = Math.log((double) all.size() / (double) idf_den.get(ng));
			weights.put(ng, tf * idf);
		}
		
		double max = 0.0;
		for(Double d : weights.values())
			if(d > max)
				max = d;
		for(String k : weights.keySet())
			weights.put(k, weights.get(k) / max);
		
		System.out.println(weights);
	}
	
	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	private double computeSimilarity(String s1, String s2, int n) {
		ArrayList<String> ng1 = getNgrams(s1, n);
		ArrayList<String> ng2 = getNgrams(s2, n);
		ArrayList<String> ngint = intersect(ng1, ng2);
		double w1 = 0.0, w2 = 0.0, wint = 0.0;
		for(String ng : ng1)
			w1 += getWeight(ng);
		for(String ng : ng2)
			w2 += getWeight(ng);
		for(String ng : ngint)
			wint += getWeight(ng);
		if(w1+w2 == 0)
			return 1.0;
		return 2 * wint / (w1 + w2);
	}

	protected ArrayList<String> intersect(ArrayList<String> set1, ArrayList<String> set2) {
		ArrayList<String> intset = new ArrayList<String>(set1);
		ArrayList<String> temp = new ArrayList<String>();
		for(String s : set2)
			temp.add(s);
	    Iterator<String> e = intset.iterator();
	    while (e.hasNext()) {
	    	String item = e.next();
	        if (!temp.contains(item))
		        e.remove();
	        else
	        	temp.remove(item);
	    }
	    return intset;
	}
	
	protected ArrayList<String> getNgrams(String s, int n) {
		s = s.toLowerCase();
		ArrayList<String> ngrams = new ArrayList<String>();
		for(int i=0; i<n-1; i++)
			s = "-" + s + "-";
		for(int i=0; i<=s.length()-n; i++)
			ngrams.add(s.substring(i, i+n));
		return ngrams;
	}

	protected double getWeight(String ng) {
		Double d = weights.get(ng.toLowerCase());
		if(d == null)
			return 1.0;
		else
			return d;
	}
	
	public void prepareNgCache(String s1, String s2, boolean increase, int n) {
		ArrayList<String> ng1 = getNgrams(s1, n);
		ArrayList<String> ng2 = getNgrams(s2, n);
		if(increase)
			ngCacheIncrease.addAll(intersect(ng1, ng2));
		else
			ngCacheDecrease.addAll(intersect(ng1, ng2));
	}
	
	public void updateWeights() {
		for(String ng : ngCacheIncrease) {
			Double d = getWeight(ng);
			double dnew = d + DELTA;
			weights.put(ng, dnew);
//			System.out.println("COST("+ng+") = "+dnew);
		}
		for(String ng : ngCacheDecrease) {
			Double d = getWeight(ng);
			double dnew = d - DELTA;
			if(dnew < 0) dnew = 0;
			weights.put(ng, dnew);
//			System.out.println("COST("+ng+") = "+dnew);
		}
		
		ngCacheIncrease.clear();
		ngCacheDecrease.clear();
	}

	public HashMap<String, Double> getWeights() {
		return weights;
	}
	
	public double getMinWeight() {
		double min = 1.0;
		for(double d : weights.values())
			if(d < min)
				min = d;
		return min;
	}

	public double getMaxWeight() {
		double max = 1.0;
		for(double d : weights.values())
			if(d > max)
				max = d;
		return max;
	}
	
	public void setWeights(HashMap<String, Double> weights) {
		this.weights = weights;
	}

	@Override
	public AcidsFilter getFilter() {
		return filter;
	}

}
