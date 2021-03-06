/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.transferlearningbest.transfer.properties;

import de.uni_leipzig.simba.transferlearningbest.transfer.config.Configuration;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

/**
 *
 * @author ngonga
 */
public class UriBasedPropertySimilarity implements PropertySimilarity{
    
    @Override
    public double getSimilarity(String property1, String property2, String class1, String class2, Configuration config) {
        String p1 = cleanUri(property1);
        String p2 = cleanUri(property2);
        return new QGramsDistance().getSimilarity(p1, p2);
    }
//(String property1, String property2, String class1, String class2, Configuration config)
	@Override
	public double getSimilarity(String property1, String class1,Configuration config1,String property2,  String class2, 
			Configuration config2, boolean isSource) {
		return getSimilarity(property1, property2, class1, class2, config1);
	}
	
    public String cleanUri(String propertyLabel) {
        if (propertyLabel.contains("/")) {
            propertyLabel = propertyLabel.substring(propertyLabel.indexOf("/") + 1);
        }
        if (propertyLabel.contains("#")) {
            propertyLabel = propertyLabel.substring(propertyLabel.indexOf("#") + 1);
        }
        if (propertyLabel.contains(":")) {
            propertyLabel = propertyLabel.substring(propertyLabel.indexOf(":") + 1);
        }
        return propertyLabel;
    }




}
