package de.uni_leipzig.simba.genetics.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.io.KBInfo;
/**
 * Class to define a mapping of the properties of 2 knowledge bases.
 * @author Klaus Lyko *
 */
public class PropertyMapping {
	Logger logger = Logger.getLogger("Limes");
	private boolean aMatchWasSet = false;
	private Mapping propMapping = new Mapping();
	private Mapping numberProps = new Mapping();
	private Mapping dateProps = new Mapping();
	private Mapping pointsetProps = new Mapping();
	
	public List<Pair<String>> stringPropPairs = new ArrayList<Pair<String>>();
	public List<Pair<String>> pointsetPropPairs = new ArrayList<Pair<String>>();
	public List<Pair<String>> numberPropPairs = new ArrayList<Pair<String>>();
	public List<Pair<String>> datePropPairs = new ArrayList<Pair<String>>();
	
	public HashSet<String> sourceStringProps = new HashSet<String>();
	public HashSet<String> targetStringProps = new HashSet<String>();
	public HashSet<String> sourcePointsetProps = new HashSet<String>();
	public HashSet<String> targetPointsetProps = new HashSet<String>();
	public HashSet<String> sourceNumberProps = new HashSet<String>();
	public HashSet<String> targetNumberProps = new HashSet<String>();
	public HashSet<String> sourceDateProps = new HashSet<String>();
	public HashSet<String> targetDateProps = new HashSet<String>();
	
	
	/**
	 * Add a match between two String properties.
	 * @param sourceProp Name of the property of the source knowledge base.
	 * @param targetProp Name of the property of the target knowledge base.
	 */
	public void addStringPropertyMatch(String sourceProp, String targetProp) {
		if(!propMapping.contains(sourceProp, targetProp)) {
			propMapping.add(sourceProp, targetProp, 1);
			sourceStringProps.add(sourceProp);
			targetStringProps.add(targetProp);
			aMatchWasSet = true;
			Pair<String> pair = new Pair<String>(sourceProp, targetProp);
			if(!stringPropPairs.contains(pair))
				stringPropPairs.add(pair);
			}		
	}
	/**
	 * Add a match between two number properties.
	 * @param sourceProp Name of the property of the source knowledge base.
	 * @param targetProp Name of the property of the target knowledge base.
	 */
	public void addNumberPropertyMatch(String sourceProp, String targetProp) {
		if(!numberProps.contains(sourceProp, targetProp)) {
			numberProps.add(sourceProp, targetProp, 1);
			sourceNumberProps.add(sourceProp);
			targetNumberProps.add(targetProp);
			aMatchWasSet = true;
			Pair<String> pair = new Pair<String>(sourceProp, targetProp);
			if(!numberPropPairs.contains(pair))
				numberPropPairs.add(pair);
		}
	}
	/**
	 * Add a match between two date properties.
	 * @param sourceProp Name of the property of the source knowledge base.
	 * @param targetProp Name of the property of the target knowledge base.
	 */
	public void addDatePropertyMatch(String sourceProp, String targetProp) {
		if(!dateProps.contains(sourceProp, targetProp)) {
			dateProps.add(sourceProp, targetProp, 1);
			sourceDateProps.add(sourceProp);
			targetDateProps.add(targetProp);
			aMatchWasSet = true;
			Pair<String> pair = new Pair<String>(sourceProp, targetProp);
			if(!datePropPairs.contains(pair))
				datePropPairs.add(pair);
		}
	}
	/**
	 * Add a match between two pointset properties.
	 * @param sourceProp Name of the property of the source knowledge base.
	 * @param targetProp Name of the property of the target knowledge base.
	 */
	public void addPointsetPropertyMatch(String sourceProp, String targetProp) {
		if(!pointsetProps.contains(sourceProp, targetProp)) {
			pointsetProps.add(sourceProp, targetProp, 1);
			sourcePointsetProps.add(sourceProp);
			targetPointsetProps.add(targetProp);
			aMatchWasSet = true;
			Pair<String> pair = new Pair<String>(sourceProp, targetProp);
			if(!pointsetPropPairs.contains(pair))
				pointsetPropPairs.add(pair);
		}
	}
	
	/**
	 * Are two properties part of the Property Mapping.
	 * @param sourceProp Name of the property of the source knowledge base.
	 * @param targetProp Name of the property of the target knowledge base.
	 * @return true if the given property match, false otherwise.
	 */
	public boolean isMatch(String sourceProp, String targetProp) {
		return propMapping.contains(sourceProp, targetProp) || 
				numberProps.contains(sourceProp, targetProp) ||
				dateProps.contains(sourceProp, targetProp) ||
				pointsetProps.contains(sourceProp, targetProp);
	}
	
	/**
	 * Is the given property specified as a number Property
	 * @param name Name of the property.
	 * @return True in case it was specified as a number, false otherwise.
	 */
	public boolean isNumberProp(String name) {
		return numberProps.map.containsKey(name) || numberProps.map.containsValue(name);
	}
	
	/**
	 * Is the given property specified as a number Property
	 * @param name Name of the property.
	 * @return True in case it was specified as a number, false otherwise.
	 */
	public boolean isDateProp(String name) {
		return dateProps.map.containsKey(name) || dateProps.map.containsValue(name);
	}
	
	/**
	 * Is the given property specified as a pointset Property
	 * @param name Name of the property.
	 * @return True in case it was specified as a pointset, false otherwise.
	 */
	public boolean isPointsetProp(String name) {
		return pointsetProps.map.containsKey(name) || pointsetProps.map.containsValue(name);
	}
	
	public Mapping getCompletePropMapping() {
		Mapping m = propMapping;
		for(String uri1:numberProps.map.keySet())
			m.add(uri1, numberProps.map.get(uri1));
		for(String uri1:dateProps.map.keySet())
			m.add(uri1, dateProps.map.get(uri1));
		for(String uri1:pointsetProps.map.keySet())
			m.add(uri1, pointsetProps.map.get(uri1));
		return m;
	}
	public Mapping getStringPropMapping() {
		return propMapping;
	}
	public Mapping getNumberPropMapping() {
		return numberProps;
	}
	public Mapping getDatePropMapping() {
		return dateProps;
	}
	
	public Mapping getPointsetPropMapping() {
		return pointsetProps;
	}
	
	public boolean wasSet() {
		return aMatchWasSet;
	}
	
	public String toString() {
		String out = "STRING\n";
		out += this.propMapping.toString();
		out += "\nNUMBER\n";
		out += this.numberProps.toString();
		out += "\nDATE\n";
		out += this.dateProps.toString();
		out += "\nPOINTSET\n";
		out += this.pointsetProps.toString();
		return out;
	}
	/**
	 * Just a littler help function to set default property mapping.
	 * @param source
	 * @param target
	 */
	public void setDefault(KBInfo source, KBInfo target) {
//		just for the evaluation!!!
		if(source.type != null && source.type.equalsIgnoreCase("csv") &&
				target.type != null && target.type.equalsIgnoreCase("csv")) {
			int max = Math.min(source.properties.size(), target.properties.size());
			for(int i = 0; i < max; i++) {
				this.addStringPropertyMatch(source.properties.get(i), target.properties.get(i));
			}
		} else {
			// just take all into account
//			int max = Math.min(source.properties.size(), target.properties.size());
			for(int i = 0; i < source.properties.size(); i++) 
				for(int j = 0; j < target.properties.size(); j++){
					this.addStringPropertyMatch(source.properties.get(i), target.properties.get(j));
				}
		}
		
	}
	
	
	public boolean containsSourceProp(String uri) {
		return sourceDateProps.contains(uri) || sourceNumberProps.contains(uri) || 
				sourceStringProps.contains(uri) || sourcePointsetProps.contains(uri);
	}
	public boolean containsTargetProp(String uri) {
		return targetDateProps.contains(uri) || targetNumberProps.contains(uri) || 
				targetStringProps.contains(uri) || targetPointsetProps.contains(uri);
	}
}
