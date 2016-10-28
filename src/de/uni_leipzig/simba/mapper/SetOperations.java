/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.mapper;

import de.uni_leipzig.simba.data.Mapping;
import org.apache.log4j.Logger;

/**
 *
 * @author ngonga
 */
public class SetOperations {

    // static Logger logger = Logger.getLogger("LIMES");

    public enum Operator {
	AND, OR, DIFF, XOR
    };

    /**
     * Relies on operators to perform set operations on mappings
     * 
     * @param source
     *            Source mapping
     * @param target
     *            Target mapping
     * @param op
     *            Set pperator
     * @return Resulting mapping
     */
    public static Mapping getMapping(Mapping source, Mapping target, Operator op) {
	if (op.equals(Operator.AND))
	    return intersection(source, target);
	if (op.equals(Operator.OR))
	    return union(source, target);
	if (op.equals(Operator.DIFF))
	    return difference(source, target);
	if (op.equals(Operator.XOR))
	    return union(union(source, target), intersection(source, target));
	return new Mapping();
    }

    public static double getRuntimeApproximation(Operator op, int mappingSize1, int mappingSize2) {
	if (op.equals(Operator.AND)) {
	    return 1d;
	}
	if (op.equals(Operator.OR)) {
	    return 1d;
	}
	return 1d;
    }

    public static double getMappingSizeApproximation(Operator op, int mappingSize1, int mappingSize2) {
	if (op.equals(Operator.AND))
	    return Math.min(mappingSize1, mappingSize2);
	if (op.equals(Operator.OR))
	    return Math.max(mappingSize1, mappingSize2);
	if (op.equals(Operator.DIFF) || op.equals(Operator.XOR))
	    return Math.max(mappingSize1, mappingSize2) - Math.min(mappingSize1, mappingSize2);
	else
	    return 0d;
    }

    /**
     * Computes the difference of two mappings.
     *
     * @param map1
     *            First mapping
     * @param map2
     *            Second mapping
     * @return map1 \ map2
     */
    public static Mapping difference(Mapping map1, Mapping map2) {
	Mapping map = new Mapping();

	// go through all the keys in map1
	for (String key : map1.map.keySet()) {
	    // if the first term (key) can also be found in map2
	    if (map2.map.containsKey(key)) {
		// then go through the second terms and checks whether they can
		// be found in map2 as well
		for (String value : map1.map.get(key).keySet()) {
		    // if yes, take the highest similarity
		    if (!map2.map.get(key).containsKey(value)) {
			map.add(key, value, map1.map.get(key).get(value));
		    }
		}
	    } else {
		map.add(key, map1.map.get(key));
	    }
	}
	return map;
    }

    /**
     * Computes the intersection of two mappings. In case an entry exists in
     * both mappings the minimal similarity is taken
     *
     * @param map1
     *            First mapping
     * @param map2
     *            Second mapping
     * @return Intersection of map1 and map2
     */
    public static Mapping intersection(Mapping map1, Mapping map2) {
	Mapping map = new Mapping();
	// takes care of not running the filter if some set is empty
	if (map1.size() == 0 || map2.size() == 0) {
	    return new Mapping();
	}
	// go through all the keys in map1
	for (String key : map1.map.keySet()) {
	    // if the first term (key) can also be found in map2
	    if (map2.map.containsKey(key)) {
		// then go through the second terms and checks whether they can
		// be found in map2 as well
		for (String value : map1.map.get(key).keySet()) {
		    // if yes, take the lowest similarity
		    if (map2.map.get(key).containsKey(value)) {
			if (map1.map.get(key).get(value) < map2.map.get(key).get(value)) {
			    map.add(key, value, map1.map.get(key).get(value));
			} else {
			    map.add(key, value, map2.map.get(key).get(value));
			}
		    }
		}
	    }
	}
	return map;
    }

    /**
     * Computes the union of two mappings. In case an entry exists in both
     * mappings the maximal similarity is taken
     *
     * @param map1
     *            First mapping
     * @param map2
     *            Second mapping
     * @return Union of map1 and map2
     */
    public static Mapping union(Mapping map1, Mapping map2) {
	Mapping map = new Mapping();
	// go through all the keys in map1
	for (String key : map1.map.keySet()) {
	    // if the first term (key) can also be found in map2
	    for (String value : map1.map.get(key).keySet()) {
		map.add(key, value, map1.getSimilarity(key, value));
	    }
	}
	for (String key : map2.map.keySet()) {
	    // if the first term (key) can also be found in map2
	    for (String value : map2.map.get(key).keySet()) {
		map.add(key, value, map2.getSimilarity(key, value));
	    }
	}
	// logger.info("\n******\nMap1\n"+map1);
	// logger.info("\n******\nMap2\n"+map2);
	// logger.info("\n******\nMap\n"+map);
	return map;
    }

    /**
     * Implements the exclusive or operator
     * 
     * @param map1
     *            First map
     * @param map2
     *            Second map
     * @return XOR(map1, map2)
     */
    public static Mapping xor(Mapping map1, Mapping map2) {
	return difference(union(map1, map2), intersection(map1, map2));
    }

    public static void main(String args[]) {
	Mapping a = new Mapping();
	Mapping b = new Mapping();
	a.add("c", "c", 0.5);
	a.add("a", "z", 0.5);
	a.add("a", "b", 0.5);
	b.add("a", "c", 0.5);
	b.add("a", "b", 0.7);
	b.add("b", "y", 0.7);
	System.out.println(union(a, b));
    }
}
