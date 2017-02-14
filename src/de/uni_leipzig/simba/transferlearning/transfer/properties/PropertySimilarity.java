package de.uni_leipzig.simba.transferlearning.transfer.properties;

import de.uni_leipzig.simba.transferlearning.transfer.config.Configuration;

/**
 * Measures similarity between classes.
 * 
 * @author Jens Lehmann
 * @author Axel Ngonga
 */
public interface PropertySimilarity {

	public double getSimilarity(String property1, String property2, String class1, String class2, Configuration config);
	
}
