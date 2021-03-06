/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.transferlearning.transfer.classes;

import de.uni_leipzig.simba.transferlearning.transfer.config.Configuration;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

/**
 *
 * @author ngonga
 */
public class UriBasedClassSimilarity implements ClassSimilarity {

	/**
	 * get the classes labels then find similarity between the labels using QGrams metric
	 */
    @Override
    public double getSimilarity(String class1, String class2, Configuration config) {
        if (class1 == null || class2 == null) {
            System.err.println("One of " + class1 + " and " + class2 + " is " + null);
            return 0D;
        }
        class1 = cleanUri(class1);
        class2 = cleanUri(class2);

        return new QGramsDistance().getSimilarity(class1, class2);
    }
    
	@Override
	public double getSimilarity(String class1, String class2, Configuration config1, Configuration config2, boolean isSource) {
		
		return getSimilarity(class1, class2, config1);
	}

    public String cleanUri(String classLabel) {
        if (classLabel.contains("/")) {
            classLabel = classLabel.substring(classLabel.indexOf("/") + 1);
        }
        if (classLabel.contains("#")) {
            classLabel = classLabel.substring(classLabel.indexOf("#") + 1);
        }
        if (classLabel.contains(":")) {
            classLabel = classLabel.substring(classLabel.indexOf("#") + 1);
        }
        return classLabel;
    }


}
