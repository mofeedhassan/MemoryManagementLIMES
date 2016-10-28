/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.lgg;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.uni_leipzig.simba.data.Mapping;

/**
 *
 * @author ngonga
 */
public interface Wombat {   
	public enum Operator {
		AND, OR, MINUS
	};
	
	Set<String> measures = new HashSet<>(Arrays.asList(
			"jaccard"
			,"trigrams"
			,"cosine"
			,"ngrams"
			));
	
	Mapping getMapping();
    String getMetricExpression();
}
