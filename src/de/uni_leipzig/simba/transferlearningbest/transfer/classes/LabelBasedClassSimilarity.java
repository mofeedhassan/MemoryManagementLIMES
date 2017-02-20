/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.transferlearningbest.transfer.classes;

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
public class LabelBasedClassSimilarity implements ClassSimilarity{
    
    public int SAMPLING_RATE = 100;
    public double THRESHOLD = 0.25;
    public static Map<String, String> prefixes = new HashMap<String, String>();
    static Logger logger = Logger.getLogger("LIMES");

    private static Cache getLabels(String c, String endpoint) {
        Cache cache = new HybridCache();
        String folder=System.getProperty("user.dir"); //check it
    	// 1. Try to get content from a serialization
    	String hash = c.hashCode() + endpoint.hashCode() + "";

    	File cacheFile = new File(folder + File.separatorChar + "labelcache/" + hash + ".ser");
    	logger.info("Checking for class label file " + cacheFile.getAbsolutePath());
    	try {
    	    if (cacheFile.exists()) {
    		logger.info("Found cached class label data. Loading data from file " + cacheFile.getAbsolutePath());
    		cache = HybridCache.loadFromFile(cacheFile);
    	    }
    	    if (cache.size() == 0) {
    		throw new Exception();
    	    } else {
    		logger.info("Cached class label data loaded successfully from file " + cacheFile.getAbsolutePath());
    		logger.info("Size = " + cache.size());
    	    }
    	} // 2. If it does not work, then get it from data sourceInfo as
    	  // specified
    	catch (Exception e) {
    	    // e.printStackTrace();
    	    // need to add a QueryModuleFactory
    	    logger.info("No cached class label data found for " + c + " from "+ endpoint);
    	    String query = "SELECT ?l "
                    + "WHERE { <"+c+"> <http://www.w3.org/2000/01/rdf-schema#label> ?l. }";
            
    	    Query sparqlQuery = QueryFactory.create(query);
            QueryExecution qexec;
            qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
            ResultSet results = qexec.execSelect();

            String x, a, b;
            QuerySolution soln;
            while (results.hasNext()) {
                soln = results.nextSolution();
                if (soln.get("l").isLiteral()) {
                    cache.addTriple(c, "p", ((Literal) soln.get("l")).getLexicalForm());
                }
            }
    	    if (!new File(folder + File.separatorChar + "labelcache").exists()  || !new File(folder + File.separatorChar + "labelcache").isDirectory()) 
    	    {
    	    	new File(folder + File.separatorChar + "labelcache").mkdir();
    	    }
    	    ((HybridCache)cache).saveToFile(new File(folder + File.separatorChar + "labelcache/" + hash + ".ser"));
    	}
    	
        /*//check the combined code if the file exist
        // if exist load the data into cache and return it
        String query = "SELECT ?l "
                + "WHERE { <"+c+"> <http://www.w3.org/2000/01/rdf-schema#label> ?l. }";
        Query sparqlQuery = QueryFactory.create(query);

        QueryExecution qexec;
        qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
        ResultSet results = qexec.execSelect();

        String x, a, b;
        QuerySolution soln;
        while (results.hasNext()) {
            soln = results.nextSolution();
            if (soln.get("l").isLiteral()) {
                cache.addTriple(c, "p", ((Literal) soln.get("l")).getLexicalForm());
            }
        }*/
        //serialize cache
        return cache;
    }

    @Override
    public double getSimilarity(String class1, String class2, Configuration config2) {
    	if (class1 == null || class2 == null) {
            System.err.println("One of " + class1 + " and " + class2 + " is " + null);
            return 0D;
        }
    	if(class1.contains(":"))
    		class1=LabelBasedClassSimilarity.expand(class1,config2);
    	if(class2.contains(":"))
    		class2=LabelBasedClassSimilarity.expand(class2,config2);
    	
        Cache target = getLabels(class1, config2.target.endpoint);//c1
    	Cache source = getLabels(class2, config2.source.endpoint);//c2
    	
        Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
        System.out.println(source+"\n"+target+"\n"+m);
        double max = 0.0, sim = 0.0;
        //we could use max, min instead
        //could also
        for (String s : m.map.keySet()) {
            for (String t : m.map.get(s).keySet()) {
                sim = m.getSimilarity(s, t);
                if(sim > max)
                    max = sim;
            }
        }
        return max;        
    }
    
	@Override
	public double getSimilarity(String class1,  Configuration config1,String class2, Configuration config2,boolean isSource) {
		if (class1 == null || class2 == null) {
            System.err.println("One of " + class1 + " and " + class2 + " is " + null);
            return 0D;
        }
    	if(class1.contains(":"))
    		class1=LabelBasedClassSimilarity.expand(class1,config1);
    	if(class2.contains(":"))
    		class2=LabelBasedClassSimilarity.expand(class2,config2);
    	
    	Cache source=null;
    	Cache target=null;
    	if(isSource)
    	{
    		source = getLabels(class1, config1.source.endpoint);//c1
    		target = getLabels(class2, config2.source.endpoint);//c2
    	}
    	else
    	{
    		source = getLabels(class1, config1.target.endpoint);//c1
    		target = getLabels(class2, config2.target.endpoint);//c2
    	}
    	double max = 0.0;
    	if(source != null && target != null)
    	{
    		 Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
    	        System.out.println(source+"\n"+target+"\n"+m.size);
    	        double sim = 0.0;
    	        //we could use max, min instead
    	        //could also
    	        for (String s : m.map.keySet()) {
    	            for (String t : m.map.get(s).keySet()) {
    	                sim = m.getSimilarity(s, t);
    	                if(sim > max)
    	                    max = sim;
    	            }
    	        }
    	}
       
        return max;        
	}
    public static String expand(String property, Configuration config) {
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
    }
    public static void main(String[] args)
    {
        System.out.println(getLabels("http://dbpedia.org/resource/Leipzig", "http://live.dbpedia.org/sparql"));
    }


}
