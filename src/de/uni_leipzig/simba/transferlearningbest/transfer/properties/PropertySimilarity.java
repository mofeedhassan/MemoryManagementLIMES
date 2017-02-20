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
	/**
	 * used by all types of similarities. UriBasedClass Similarity uses only the first two parameters and the rest are useless.
	 * @param property1 It represents the property focused on in the main configuration
	 * @param class1  It represents the class of the data instances of the main configuration where candidate configuration is compared to
	 * @param config1 It represents the main configuration where candidate configuration is compared to
	 * @param property2 It represents the property focused on in the candidate configuration
	 * @param class2 It represents the class of the data instances of the candidate configuration 
	 * @param config2 It represents the candidate configuration
	 * @param isSource It shows if the class belongs to source or target data set
	 * @return the degree of similarity between the candidate configuration' class and the main configuration's class
	 */
	
	public double getSimilarity(String property1, String class1, Configuration config1,String property2,  String class2, Configuration config2, boolean isSource);

	
}
