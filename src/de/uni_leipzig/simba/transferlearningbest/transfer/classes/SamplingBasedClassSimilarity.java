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
import de.uni_leipzig.simba.transferlearningbest.transfer.config.Configuration;
import de.uni_leipzig.simba.transferlearningbest.transfer.properties.SamplingBasedPropertySimilarity;
import de.uni_leipzig.simba.transferlearningbest.util.Execution;

/**
 *
 * @author ngonga
 */
public class SamplingBasedClassSimilarity implements ClassSimilarity {

    public int SAMPLING_RATE = 200;
    public double THRESHOLD = 0.5;

    private static Cache getPropertyValues(String c, String endpoint, int size) {
        Cache cache = new HybridCache();
        String query;
        if(c.startsWith("http")) query = "SELECT ?x ?a "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + c + ">. "
                + "?a ?p ?x .} LIMIT " + size;
        else query = "SELECT ?x ?a "
                + "WHERE { ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + c + ". "
                + "?a ?p ?x .} LIMIT " + size;
        Query sparqlQuery = QueryFactory.create(query);

        QueryExecution qexec;
        System.out.println(endpoint);
        qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
        if(query.contains("http://dbpedia.org/ontology/Country"))
            System.out.println(sparqlQuery);

        ResultSet results = qexec.execSelect();

        String x, a, b;
        QuerySolution soln;
        while (results.hasNext()) {
            soln = results.nextSolution();
            if (soln.get("x").isLiteral()) {
                cache.addTriple(soln.get("a").toString(), "p", ((Literal) soln.get("x")).getLexicalForm());
            }
        }
        return cache;
    }

    @Override
    public double getSimilarity(String class1, String class2, Configuration config2) {
        String c1 = SamplingBasedPropertySimilarity.expand(class1, config2);
        String c2 = SamplingBasedPropertySimilarity.expand(class2, config2);
       
        Cache target = getPropertyValues(c1, config2.target.endpoint, SAMPLING_RATE);//c1
        Cache source = getPropertyValues(c2, config2.source.endpoint, SAMPLING_RATE);//c2
        
        Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
        //System.out.println(source+"\n"+target+"\n"+m);
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
    
	@Override
	public double getSimilarity(String class1, String class2, Configuration config1, Configuration config2,boolean isSource) {
		
		String c1 = SamplingBasedPropertySimilarity.expand(class1, config2);
        String c2 = SamplingBasedPropertySimilarity.expand(class2, config2);
        
        Cache source=null;
    	Cache target=null;
    	
       if(isSource)
       {
    	   source = getPropertyValues(c1, config1.source.endpoint, SAMPLING_RATE);//c1
    	   target = getPropertyValues(c2, config2.source.endpoint, SAMPLING_RATE);//c2
       }
       else
       {
    	   source = getPropertyValues(c1, config1.target.endpoint, SAMPLING_RATE);//c1
    	   target = getPropertyValues(c2, config2.target.endpoint, SAMPLING_RATE);//c2
       }
        
       double counter = 0.0;
       double total = 0.0;
       if(source != null && target != null)
       {
    	   Mapping m = Execution.execute(source, target, "trigrams", THRESHOLD);
           //System.out.println(source+"\n"+target+"\n"+m);
           
           //we could use max, min instead
           //could also
           for (String s : m.map.keySet()) {
               for (String t : m.map.get(s).keySet()) {
                   counter++;
                   total = total + m.getSimilarity(s, t);
               }
           }
       }
       
       if (counter == 0)
           return counter;
       
        return total/(Math.min((double)source.getAllUris().size(),(double)target.getAllUris().size()));
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
