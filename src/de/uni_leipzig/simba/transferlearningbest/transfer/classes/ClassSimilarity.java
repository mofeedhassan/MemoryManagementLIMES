package de.uni_leipzig.simba.transferlearningbest.transfer.classes;

import de.uni_leipzig.simba.transferlearningbest.transfer.config.Configuration;

/**
 * Measures similarity between classes.
 * 
 * @author Jens Lehmann
 * @author Axel Ngonga
 */
public interface ClassSimilarity {

	public double getSimilarity(String class1, String class2, Configuration config2);// not used b any similarity type, left for securing an possible conflict
	/**
	 * used by all types of similarities. UriBasedClass Similarity uses only the first two parameters and the rest are useless.
	 * @param class1  It represents the class of the data instances of the main configuration where candidate configuration is compared to
	 * @param config1 It represents the main configuration where candidate configuration is compared to
	 * @param class2 It represents the class of the data instances of the candidate configuration 
	 * @param config2 It represents the candidate configuration
	 * @param isSource It shows if the class belongs to source or target data set
	 * @return the degree of similarity between the candidate configuration' class and the main configuration's class
	 */
	public double getSimilarity(String class1, Configuration config1, String class2,Configuration config2,boolean isSource);

	
}
