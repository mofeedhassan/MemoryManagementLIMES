/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.transferlearning.transfer.classes;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import de.uni_leipzig.simba.cache.Cache;
import de.uni_leipzig.simba.cache.HybridCache;
import de.uni_leipzig.simba.data.Instance;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.query.QueryModule;
import de.uni_leipzig.simba.query.QueryModuleFactory;
//import net.sf.saxon.expr.instruct.ForEach;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.uni_leipzig.simba.transferlearning.transfer.config.Configuration;
import de.uni_leipzig.simba.transferlearning.transfer.properties.SamplingBasedPropertySimilarity;
import de.uni_leipzig.simba.transferlearning.util.Execution;
import de.uni_leipzig.simba.transferlearning.util.SparqlUtils;

/**
 *
 * @author ngonga
 */
public class SamplingBasedClassSimilarity implements ClassSimilarity {

    public int SAMPLING_RATE = 100;
    public double THRESHOLD = 0.25;
	boolean isSource=true;

    /**
     * get a cache of instances of certain size from a given class containing all their object values
     * This is the original getPropertValues
     * No caching used, just online
     * Original method
     * @param c
     * @param endpoint
     * @param size
     * @return
     */
    private static Cache getPropertyValues(String c, String endpoint, int size) {
        
    	Cache cache = new HybridCache();

        String query;
        //get 'size' number of instances from the c class and all object values of these instances
        if(c.startsWith("http")) query = "SELECT ?x ?a "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + c + ">. "
                + "?a ?p ?x .} LIMIT " + size;
        else query = "SELECT ?x ?a "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + c + ". "
                + "?a ?p ?x .} LIMIT " + size;
        Query sparqlQuery = QueryFactory.create(query);

        QueryExecution qexec;
        qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
        ResultSet results = qexec.execSelect();

        String x, a, b;
        QuerySolution soln;
        while (results.hasNext()) {
            soln = results.nextSolution();
            if (soln.get("x").isLiteral()) {
                cache.addTriple(soln.get("a").toString(), "p", ((Literal) soln.get("x")).getLexicalForm());
            }
        }
        return cache;// triples as: ?a "p" ?x
    }
    
    /**
     * get a cache of instances of certain size from a given class containing all their object values
     * It uses configuration class and its sub-class HybridCache that is by default  get the whole data
     * either from endpoint or cached .ser file 
     * It serializes only the literal data
     * Permutation of parameters is made to differentiate between it and the other nethod that samples the data 
     * without filtering the literals
     * @param c class to select
     * @param conf configuration
     * @param size sample size
     * @param isSource is it source in configuration
     * @return
     */
    public static Cache getPropertyValues(String c,int size, Configuration conf, boolean isSource) {
        Cache cache = null;
        String endpoint="";
        // which endpoint to use?
        if(isSource)
        	endpoint =  conf.getSource().endpoint;
        else
        	endpoint =  conf.getTarget().endpoint;
        //prepare the serializing file cache
        String hash = c.hashCode() +endpoint.hashCode()+"";
        File cacheFile = new File(new File("") + "cache/" + hash + ".ser");
        try
        {
	        if (cacheFile.exists())
	        {
	    		cache = HybridCache.loadFromFile(cacheFile);
	    	    if (cache.size() == 0) {
	        		throw new Exception();
	        	    } else {
	        		System.out.println("Size = " + cache.size());
	        	    }
	        }
	        else throw new Exception();

        }
        catch(Exception e)
        {
            if(SparqlUtils.isAlive(endpoint,null))
            {
            	cache = new HybridCache();
            	String query;
                //get 'size' number of instances from the c class and all object values of these instances
                if(c.startsWith("http")) query = "SELECT ?x ?a "
                        + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + c + ">. "
                        + "?a ?p ?x . FILTER isLiteral(?x) .} LIMIT " + size;
                else query = "SELECT ?x ?a "
                        + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + c + ". "
                        + "?a ?p ?x . FILTER isLiteral(?x).} LIMIT " + size;
                Query sparqlQuery = QueryFactory.create(query);

                QueryExecution qexec;
                qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
                ResultSet results = qexec.execSelect();

                String x, a, b;
                QuerySolution soln;
                while (results.hasNext()) {
                    soln = results.nextSolution();
                    if (soln.get("x").isLiteral()) {
                        cache.addTriple(soln.get("a").toString(), "p", ((Literal) soln.get("x")).getLexicalForm());
                    }
                }
                System.out.println(cache.size());
                Path currentRelativePath = Paths.get("");
                String path = currentRelativePath.toAbsolutePath().toString();

        	    if (!new File(path + File.separatorChar + "cache").exists()  || !new File(path + File.separatorChar + "cache").isDirectory()) 
        	    {
        	    	new File(path + File.separatorChar + "cache").mkdir();
        	    }
        	    ((HybridCache)cache).saveToFile(new File(path + File.separatorChar + "cache/" + hash + ".ser"));
        	    
            }
        }
        return cache;// triples as: ?a "p" ?x
    }
    
    /**
     * get a cache of instances of certain size from a given class containing all their object values
     * It uses configuration class and its sub-class HybridCache that is by default  get the whole data
     * either from endpoint or cached .ser file 
     * It gets a sample from the data (no restriction on type of data) and return it
     * @param c class to select
     * @param conf configuration
     * @param size sample size
     * @param isSource is it source in configuration
     * @return
     */
    public static Cache getPropertyValues(String c, Configuration conf, int size, boolean isSource) {
    	
        Cache cache = new HybridCache();
        HybridCache cachedData;
        if(isSource)
        	cachedData = HybridCache.getData(conf.getSource());
       	else
        	cachedData = HybridCache.getData(conf.getTarget());
    	
        cache =cachedData.getSample(size); //object values are always literal when selected for linking
        return cache;// triples as: ?a "p" ?x
    }
    /**
     * get a cache of instances of certain size from a given class containing all their object values
     * It uses configuration class and its sub-class HybridCache that is by default  get the whole data
     * either from endpoint or cached .ser file 
     * It gets a sample from the data (no restriction on type of data) and return it
     * @param c
     * @param endpoint
     * @param size
     * @configuration  
     * @return
     */
/*    private static Cache getPropertyValues(String c, String endpoint, int size, Configuration configuration) {
        Cache cacheFromFile =null;
        
        if(configuration.getSource().getClassOfendpoint().equals(c))
        	cacheFromFile = HybridCache.getData(configuration.getSource());
        else
        	cacheFromFile = HybridCache.getData(configuration.getTarget());
        
        return cacheFromFile.getSample(size);
        
        Cache cache = new HybridCache();
        String query;
        //get 'size' number of instances from the c class and all object values of these instances
        if(c.startsWith("http")) query = "SELECT ?x ?a "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + c + ">. "
                + "?a ?p ?x .} LIMIT " + size;
        else query = "SELECT ?x ?a "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + c + ". "
                + "?a ?p ?x .} LIMIT " + size;
        Query sparqlQuery = QueryFactory.create(query);

        QueryExecution qexec;
        qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
        ResultSet results = qexec.execSelect();

        String x, a, b;
        QuerySolution soln;
        while (results.hasNext()) {
            soln = results.nextSolution();
            if (soln.get("x").isLiteral()) {
                cache.addTriple(soln.get("a").toString(), "p", ((Literal) soln.get("x")).getLexicalForm());
            }
        }
        return cache;// triples as: ?a "p" ?x
    }*/
    
    /**
     * return the average similarity value between certain number of mapping instances between
     * two datasets each from specific class
     * This is the modified getSimilarity, so it gets two configurations to each onws one of the classes either
     * as a source class or target class
     * It uses getPropertyValues giving it (class,configuration,,samplesize,isSource)
     * @param class1 first configuration's class (source or target)
     * @param class2 second configuration's class (source or target)
     * @param config1 first configuration
     * @param config2 first configuration
     * @param isSource boolean to specify the type of passed classes
     */
    @Override
    public double getSimilarity(String class2, String class1, Configuration config1, Configuration config2, boolean isSource) {
    	//expand the class into its fullname http:....(property or class)
        String c1 = SamplingBasedPropertySimilarity.expand(class1, config1);
        String c2 = SamplingBasedPropertySimilarity.expand(class2, config2);
        Cache peer1,peer2;
        //get the cache instances of the given class focusing on subjects' and objects' values
        if(isSource)
        {
        	/*peer1 = getPropertyValues(c1, config1, SAMPLING_RATE,isSource);
        	peer2 = getPropertyValues(c2, config2, SAMPLING_RATE,isSource); //both classes are sources in their configurations
*/       
        	peer1 = getPropertyValues(c1, SAMPLING_RATE,config1,isSource);
        	peer2 = getPropertyValues(c2, SAMPLING_RATE,config2,isSource); //both classes are sources in their configurations	
        }
        else
        {
        	peer1 = getPropertyValues(c1, config1, SAMPLING_RATE,!isSource);
        	peer2 = getPropertyValues(c2, config2, SAMPLING_RATE,!isSource); //both classes are targets in their configurations
        }
        

        //System.out.println(source+"\n"+target+"\n"+m);
        double counter = 0.0;
        double total = 0.0;
        
        if(peer1.size() !=0 && peer2.size()!= 0) // the condition block is added by Mofeed to avoid mapping when source/target is empty
        {
        	Mapping m = Execution.execute(peer1, peer2, "trigrams", THRESHOLD);
            
            //we could use max, min instead
            //could also
            for (String s : m.map.keySet()) {
                for (String t : m.map.get(s).keySet()) {
                    counter++;
                    total = total + m.getSimilarity(s, t);
                }
            }
        }
        
        if (counter == 0) {
            return counter;
        }
        return total/(Math.min((double)peer1.getAllUris().size(),(double)peer2.getAllUris().size()));//return average similarity value
    }
    

    /**
     * return the average similarity value between certain number of mapping instances between
     * two datasets each from specific class
     * This is the original getSimilarity method
     * <-> both classes retrieve the classes from different confs using single confs that leads to one empty cache 
     */
    @Override
    public double getSimilarity(String class1, String class2, Configuration config) {
    	//expand the class into its fullname http:....(property or class)
        String c1 = SamplingBasedPropertySimilarity.expand(class1, config);
        String c2 = SamplingBasedPropertySimilarity.expand(class2, config);
        //get the cache instances of the given class focusing on subjects' and objects' values
        Cache source = getPropertyValues(c1, config.source.endpoint, SAMPLING_RATE);
        Cache target = getPropertyValues(c2, config.target.endpoint, SAMPLING_RATE);
 
        //System.out.println(source+"\n"+target+"\n"+m);
        double counter = 0.0;
        double total = 0.0;
        
        if(source.size() !=0 && target.size()!= 0) // the condition block is added by Mofeed to avoid mapping when source/target is empty
        {
        	Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
            
            //we could use max, min instead
            //could also
            for (String s : m.map.keySet()) {
                for (String t : m.map.get(s).keySet()) {
                    counter++;
                    total = total + m.getSimilarity(s, t);
                }
            }
        }
        
        if (counter == 0) {
            return counter;
        }
        return total/(Math.min((double)source.getAllUris().size(),(double)target.getAllUris().size()));//return average similarity value
    }
    
    /** More for testing than anything else
     * 
     * @param class1 
     * @param class2
     * @param endpoint1
     * @param endpoint2
     * @return 
     */
    public double getSimilarity(String class1, String class2, String endpoint1, String endpoint2) {
        Cache source = getPropertyValues(class1, endpoint1, SAMPLING_RATE);
        Cache target = getPropertyValues(class2, endpoint2, SAMPLING_RATE);
        
        Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
        System.out.println(source+"\n"+target+"\n"+m);
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
        return total/(Math.min((double)source.getAllUris().size(),(double)target.getAllUris().size()));
    }

    public static void test() {
        SamplingBasedClassSimilarity sbc = new SamplingBasedClassSimilarity();
        double value = sbc.getSimilarity("http://dbpedia.org/ontology/Place", "http://dbpedia.org/ontology/Town", "http://live.dbpedia.org/sparql", "http://live.dbpedia.org/sparql");
        System.out.println(value);

    }

    public static void main(String args[]) {
        test();
    }
}
