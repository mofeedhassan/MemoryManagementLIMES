package de.uni_leipzig.simba.transferlearningbest.transfer.properties;

import de.uni_leipzig.simba.transferlearningbest.transfer.config.Configuration;

/**
 * Measures similarity between classes.
 * 
 * @author Jens Lehmann
 * @author Axel Ngonga
 */
public interface PropertySimilarity {

	public double getSimilarity(String property1, String property2, String class1, String class2, Configuration config);
	public double getSimilarity(String property1, String property2, String class1, String class2, Configuration config1,Configuration config2);

	
}
