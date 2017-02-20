/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.transferlearningbest.transfer.properties;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;

import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Mapping;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.uni_leipzig.simba.transferlearningbest.transfer.config.Configuration;
import de.uni_leipzig.simba.transferlearningbest.util.Execution;

/**
 *
 * @author ngonga
 */
public class SamplingBasedPropertySimilarity implements PropertySimilarity {

    public int SAMPLING_RATE = 200;
    public double THRESHOLD = 0.5;
    public static Map<String, String> prefixes = new HashMap<String, String>();
    static Logger logger = Logger.getLogger("LIMES");

    private static Cache getPropertyValues(String c, String p, String endpoint, int size) {
    	

        Cache cache = new HybridCache();
        String folder=System.getProperty("user.dir"); //check it
    	// 1. Try to get content from a serialization
    	String hash = c.hashCode() + p.hashCode() + endpoint.hashCode() + new Integer(size).hashCode()+"";

    	File cacheFile = new File(folder  + File.separatorChar +"samplecache/" + hash + ".ser");
    	logger.info("Checking for sample property data file " + cacheFile.getAbsolutePath());
    	try {
    	    if (cacheFile.exists()) {
    		logger.info("Found cached sample property data. Loading data from file " + cacheFile.getAbsolutePath());
    		cache = HybridCache.loadFromFile(cacheFile);
    	    }
    	    if (cache.size() == 0) {
    		throw new Exception();
    	    } else {
    		logger.info("Cached sample property data loaded successfully from file " + cacheFile.getAbsolutePath());
    		logger.info("Size = " + cache.size());
    	    }
    	} // 2. If it does not work, then get it from data sourceInfo as
    	  // specified
    	catch (Exception e) {
    	    // e.printStackTrace();
    	    // need to add a QueryModuleFactory
    	    logger.info("No cached sample property data found for " + c + " from "+ endpoint);
    	    String query;
    	    String expandedProperty="";
    	    
    	    if(p.contains("?e0"))//cascaded properties
    	    	expandedProperty=p;
    	    else
    	    	expandedProperty="<"+p+">";
    	    
    	    if(c.startsWith("http")) query = "SELECT ?a ?x "
                    + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + c + ">. "
                    + "?a " + expandedProperty + " ?x . FILTER(isLiteral(?x))} LIMIT " + size;
            else query = "SELECT ?a ?x "
                    + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + c + ". "
                    + "?a " + expandedProperty + " ?x . FILTER(isLiteral(?x))} LIMIT " + size;
    	    
    	    
    	   /* if(c.startsWith("http")) query = "SELECT ?a ?x "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + c + ">. "
                + "?a <" + p + "> ?x . FILTER(isLiteral(?x))} LIMIT " + size;
        else query = "SELECT ?a ?x "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + c + ". "
                + "?a <" + p + "> ?x . FILTER(isLiteral(?x))} LIMIT " + size;*/
    	    
        System.out.println(query);

        Query sparqlQuery = QueryFactory.create(query);

        QueryExecution qexec;
        qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
        ResultSet results = qexec.execSelect();

        String x, a, b;
        QuerySolution soln;
        while (results.hasNext()) {
            soln = results.nextSolution();
            cache.addTriple(soln.get("a").toString(), "p", soln.get("x").toString());
        }

    	    if (!new File(folder + File.separatorChar + "samplecache").exists()  || !new File(folder + File.separatorChar + "samplecache").isDirectory()) 
    	    {
    	    	new File(folder + File.separatorChar + "samplecache").mkdir();
    	    }
    	    ((HybridCache)cache).saveToFile(new File(folder + File.separatorChar + "samplecache/" + hash + ".ser"));
    	}
    	
       /* Cache cache = new HybridCache();
        String query = "SELECT ?a ?x "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + c + ">. "
                + "?a <" + p + "> ?x .} LIMIT " + size;
        Query sparqlQuery = QueryFactory.create(query);

        QueryExecution qexec;
        qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
        ResultSet results = qexec.execSelect();

        String x, a, b;
        QuerySolution soln;
        while (results.hasNext()) {
            soln = results.nextSolution();
            cache.addTriple(soln.get("a").toString(), "p", soln.get("x").toString());
        }*/
        return cache;
    }

    @Override
    public double getSimilarity(String property1, String property2, String class1, String class2, Configuration config) {
        String p1 = expand(property1, config);
        String p2 = expand(property2, config);
        String c1 = expand(class1, config);
        String c2 = expand(class2, config);
        
        Cache source = getPropertyValues(c1, p1, config.source.endpoint, SAMPLING_RATE);
        Cache target = getPropertyValues(c2, p2, config.target.endpoint, SAMPLING_RATE);

        Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
        //System.out.println(source + "\n" + target + "\n" + m);
        double counter = 0.0;
        double total = 0.0;
        //we could use max, min instead
        //could also
        for (String s : m.map.keySet()) {
            for (String t : m.map.get(s).keySet()) {
                counter++;
                total = total + m.getSimilarity(s, t);
            }
        }
        if (counter == 0) {
            return counter;
        }
        return total / (Math.min((double) source.getAllUris().size(), (double) target.getAllUris().size()));
    }
	@Override
	public double getSimilarity(String property1, String class1, Configuration config1,String property2,  String class2, Configuration config2, boolean isSource) {
		 	String p1 = expand(property1, config1);
	        String p2 = expand(property2, config2);
	        String c1 = expand(class1, config1);
	        String c2 = expand(class2, config2);
	        
	        Cache source =null;
	        Cache target =null;
	        if(isSource)
	        {
	        	source = getPropertyValues(c1, p1, config1.source.endpoint, SAMPLING_RATE);
		        target = getPropertyValues(c2, p2, config2.source.endpoint, SAMPLING_RATE);
	        }
	        else
	        {
	        	source = getPropertyValues(c1, p1, config1.target.endpoint, SAMPLING_RATE);
		        target = getPropertyValues(c2, p2, config2.target.endpoint, SAMPLING_RATE);
	        }
	        

	        Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
	        //System.out.println(source + "\n" + target + "\n" + m);
	        double counter = 0.0;
	        double total = 0.0;
	        //we could use max, min instead
	        //could also
	        for (String s : m.map.keySet()) {
	            for (String t : m.map.get(s).keySet()) {
	                counter++;
	                total = total + m.getSimilarity(s, t);
	            }
	        }
	        if (counter == 0) {
	            return counter;
	        }
	        return total / (Math.min((double) source.getAllUris().size(), (double) target.getAllUris().size()));
	}

    public static void test() {
        System.out.println(getPropertyValues("http://dbpedia.org/ontology/Place", "http://www.w3.org/2000/01/rdf-schema#label", "http://live.dbpedia.org/sparql", 100));
    }

    public static void main(String args[]) {
        test();
    }

    public static String expand(String property, Configuration config) {
    	
        if(property.startsWith("http")) return property;
        //to handle propert1/property2 case
        if(property.contains(":") && property.contains("/") && property.lastIndexOf(":") > property.lastIndexOf("/")) // dbpedia:director/rdfs:label OR property.lastIndexOf(":") != property.indexOf(":") two occurences
        {
        	System.out.println("cascading properies");
        	String[] properties = property.split("/");
        	String[] expandedProperties = new String[properties.length];
        	//expand each property
        	for(int i=0;i<properties.length;i++)
        	{
        		String prefix = properties[i].substring(0, properties[i].indexOf(":"));
                String name = properties[i].substring(properties[i].indexOf(":") + 1);
                
        		expandedProperties[i] = expandProperty(properties[i], prefix, name, config);
        	}
        	
        	int variableCount=0;
        	String varName="?e";
        	String propertiesSequence ="";
        	String propertySub="";
        	
        	// combine them
        	for(int i=0;i<expandedProperties.length-1;i++)
        	{
        		propertiesSequence+=propertySub+"\t<"+expandedProperties[i]+">\t"+varName+variableCount+".\n";
        		propertySub = varName+variableCount;
        		variableCount++;
        	}
        	propertiesSequence+=propertySub+"\t<"+expandedProperties[expandedProperties.length-1]+">\t"; // the last cascading property has no variable
        	return propertiesSequence;
        }
        
        String prefix = property.substring(0, property.indexOf(":"));
        String name = property.substring(property.indexOf(":") + 1);
  

        return expandProperty(property, prefix, name, config);
        
    }
    
    private static String expandProperty(String property, String prefix, String name, Configuration config)
    {
    	if(prefixes.containsKey(prefix)) return prefixes.get(prefix)+name;
        if(config.source.prefixes.containsKey(prefix)) return config.source.prefixes.get(prefix)+name;
        if(config.target.prefixes.containsKey(prefix)) return config.target.prefixes.get(prefix)+name;
        if(prefix.equals("dbpedia-owl")) return "http://dbpedia.org/ontology/"+name;
        if(prefix.equals("lgdo")) return "http://linkedgeodata.org/ontology/"+name;
        if(prefix.equals("yago")) return "http://dbpedia.org/class/yago/"+name;
        if(prefix.equals("BibTex")) return "http://data.bibbase.org/ontology/#";
        
        if(prefix.equals("administrative-geography")) return "http://statistics.data.gov.uk/def/administrative-geography/";
        else System.err.println("Prefix "+prefix+" not found for property "+property);
        System.exit(1);
        return null;
    }
    
/* public static String expand(String property, Configuration config) {
    	
        if(property.startsWith("http")) return property;
        
        String prefix = property.substring(0, property.indexOf(":"));
        String name = property.substring(property.indexOf(":") + 1);
        
        if(prefixes.containsKey(prefix)) return prefixes.get(prefix)+name;
        if(config.source.prefixes.containsKey(prefix)) return config.source.prefixes.get(prefix)+name;
        if(config.target.prefixes.containsKey(prefix)) return config.target.prefixes.get(prefix)+name;
        if(prefix.equals("dbpedia-owl")) return "http://dbpedia.org/ontology/"+name;
        if(prefix.equals("lgdo")) return "http://linkedgeodata.org/ontology/"+name;
        if(prefix.equals("yago")) return "http://dbpedia.org/class/yago/"+name;
        if(prefix.equals("BibTex")) return "http://data.bibbase.org/ontology/#";
        
        if(prefix.equals("administrative-geography")) return "http://statistics.data.gov.uk/def/administrative-geography/";
        else System.err.println("Prefix "+prefix+" not found for property "+property);
        System.exit(1);
        return null;
    }*/


}
