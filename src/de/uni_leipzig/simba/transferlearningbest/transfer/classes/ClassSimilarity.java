package de.uni_leipzig.simba.transferlearningbest.transfer.classes;

import de.uni_leipzig.simba.transferlearningbest.transfer.config.Configuration;

/**
 * Measures similarity between classes.
 * 
 * @author Jens Lehmann
 * @author Axel Ngonga
 */
public interface ClassSimilarity {

	public double getSimilarity(String class1, String class2, Configuration config2);
	public double getSimilarity(String class1, String class2, Configuration config1,Configuration config2,boolean isSource);

	
}
