package de.uni_leipzig.simba.transferlearning.transfer.config;

/**
 * Measures similarity between classes.
 * 
 * @author Jens Lehmann
 * @author Axel Ngonga
 */
public interface ConfigAccuracy {

	public double getAccuracy(Configuration configuration, String posExamplesFile, String negExamplesFile);
	
}
