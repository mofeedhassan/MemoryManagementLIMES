/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.filter;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.mapper.SetOperations;
import de.uni_leipzig.simba.measures.MeasureProcessor;
import org.apache.log4j.Logger;

/**
 * Implements filtering functionality. The input is a map, a threshold and some
 * expression. All the pairs (s,t) in the map such that expression(s,t) >
 * threshold are returned.
 * 
 * @author ngonga
 */
public class LinearFilter implements Filter {

    static Logger logger = Logger.getLogger("LIMES");

    /**
     * Filter a mapping with respect to an expression. If the expression is
     * null, then all the pairs with a similarity above threshold are returned
     */
    public Mapping filter(Mapping map, String condition, double threshold, Cache source, Cache target, String sourceVar,
	    String targetVar) {
	double sim = 0.0;
	Instance s, t;
	//
	if (threshold <= 0.0) {
	    return map;
	}
	Mapping result = new Mapping();
	// 2. run on all pairs and remove those
	for (String key : map.map.keySet()) {
	    s = source.getInstance(key);
	    for (String value : map.map.get(key).keySet()) {
		t = target.getInstance(value);
		sim = MeasureProcessor.getSimilarity(s, t, condition, sourceVar, targetVar);
		// System.out.println("Similarity of "+s.getUri()+" and
		// "+t.getUri()+" w.r.t. "+condition+" is "+sim);
		if (sim >= threshold) {
		    result.add(s.getUri(), t.getUri(), sim);
		}
	    }
	}
	// logger.info(result);
	return result;
    }

    /**
     * FOR HELIOS/DYNAMIC planner. FOR AND OPERATOR. Filter a mapping with
     * respect to an expression.
     */
    public Mapping filter(Mapping map, String condition, double threshold, double mainThreshold, Cache source,
	    Cache target, String sourceVar, String targetVar) {

	double sim = 0.0;
	Instance s, t;

	Mapping result = new Mapping();
	// 2. run on all pairs and remove those
	for (String key : map.map.keySet()) {
	    s = source.getInstance(key);
	    for (String value : map.map.get(key).keySet()) {
		t = target.getInstance(value);
		sim = MeasureProcessor.getSimilarity(s, t, condition, threshold, sourceVar, targetVar);
		// result must pass the filter threshold first!
		if (sim >= threshold) {
		    double sim2 = map.map.get(s.getUri()).get(t.getUri());
		    double minSim = Math.min(sim, sim2);
		    // min similarity because of AND operator
		    // check if min sim passes the bigger threshold
		    if (minSim >= mainThreshold) {
			result.add(s.getUri(), t.getUri(), minSim);
		    }
		}

	    }
	}
	return result;

    }

    /**
     * FOR DYNAMIC planner. FOR MINUS OPERATOR. Filter a mapping with respect to
     * an expression.
     */
    public Mapping reversefilter(Mapping map, String condition, double threshold, double mainThreshold, Cache source,
	    Cache target, String sourceVar, String targetVar) {

	double sim = 0.0;
	Instance s, t;
	Mapping result = new Mapping();
	// 2. run on all pairs and remove those
	for (String key : map.map.keySet()) {
	    s = source.getInstance(key);
	    for (String value : map.map.get(key).keySet()) {
		t = target.getInstance(value);
		sim = MeasureProcessor.getSimilarity(s, t, condition, threshold, sourceVar, targetVar);
		// check if sim is lower than the second's child threshold.
		// special case: threshold and sim are 0, then the link is
		// accepted 
		if (sim < threshold || (sim == 0.0 && threshold == 0.0)) {
		    double sim2 = map.map.get(s.getUri()).get(t.getUri());
		    if (sim2 >= mainThreshold) {
			result.add(s.getUri(), t.getUri(), sim2);
		    }
		} 
	    }
	}
	return result;

    }

    /**
     * Filter a mapping solely with respect to a threshold
     *
     * @param map
     *            Input mapping
     * @param threshold
     *            Similarity threshold
     * @return All mapping from map such that sim >= threshold
     */
    public Mapping filter(Mapping map, double threshold) {
	// logger.info("Filtering out all pairs whose similarity is below " +
	// threshold);
	double sim = 0.0;
	if (threshold <= 0.0) {
	    return map;
	} else {
	    Mapping result = new Mapping();
	    // 1. Run on all pairs and remove those whose similarity is below
	    // the threshold
	    for (String key : map.map.keySet()) {
		for (String value : map.map.get(key).keySet()) {
		    sim = map.getSimilarity(key, value);
		    if (sim >= threshold) {
			result.add(key, value, sim);
		    }
		}
	    }
	    return result;
	}
    }

    /**
     * Implements a filter for the special case of linear combinations and
     * multiplications. The straight forward way would be to compute
     * filter(intersection(m1, m2), linear_combination_condition) leading to
     * recomputations. This implementation avoid that by reusing the
     * similarities that have already been computed
     */
    public Mapping filter(Mapping m1, Mapping m2, double coef1, double coef2, double threshold, String operation) {
	Mapping m = SetOperations.intersection(m1, m2);
	Mapping result = new Mapping();
	double sim;
	// we can be sure that each key in m is also in m1 and m2 as we used
	// intersection
	if (operation.equalsIgnoreCase("add")) {
	    for (String key : m.map.keySet()) {
		for (String value : m.map.get(key).keySet()) {
		    sim = coef1 * m1.getSimilarity(key, value) + coef2 * m2.getSimilarity(key, value);
		    if (sim >= threshold) {
			result.add(key, value, sim);
		    }
		}
	    }
	} else {
	    for (String key : m.map.keySet()) {
		for (String value : m.map.get(key).keySet()) {
		    sim = coef1 * coef2 * m1.getSimilarity(key, value) * m2.getSimilarity(key, value);
		    if (sim >= threshold) {
			result.add(key, value, sim);
		    }
		}
	    }
	}
	return result;
    }
}
